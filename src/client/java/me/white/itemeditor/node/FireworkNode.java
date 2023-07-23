package me.white.itemeditor.node;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.ColorArgumentType;
import me.white.itemeditor.argument.EnumArgumentType;
import me.white.itemeditor.argument.ListArgumentType;
import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import oshi.util.tuples.Quintet;

public class FireworkNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.cannoteddit")).create();
    public static final CommandSyntaxException NO_FLIGHT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.noflight")).create();
    public static final CommandSyntaxException NO_STARS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.nostars")).create();
    public static final CommandSyntaxException FLIGHT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.flightalreadyis")).create();
    public static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((i, size) -> Text.translatable("commands.edit.firework.error.outofbounds", i, size));
    private static final String OUTPUT_FLIGHT_GET = "commands.edit.firework.flightget";
    private static final String OUTPUT_FLIGHT_SET = "commands.edit.firework.flightset";
    private static final String OUTPUT_STAR = "commands.edit.firework.stargetstar";
    private static final String OUTPUT_STAR_FADE = "commands.edit.firework.stargetstarfade";
    private static final String OUTPUT_STAR_GET = "commands.edit.firework.starget";
    private static final String OUTPUT_STAR_ADD = "commands.edit.firework.staradd";
    private static final String OUTPUT_STAR_REMOVE = "commands.edit.firework.remove";
    private static final String OUTPUT_STAR_CLEAR = "commands.edit.firework.clear";

    private static enum Type {
        SMALL(0, "commands.edit.firework.typesmall"),
        LARGE(1, "commands.edit.firework.typelarge"),
        STAR(2, "commands.edit.firework.typestar"),
        CREEPER(3, "commands.edit.firework.typecreeper"),
        BURST(4, "commands.edit.firework.typeburst");
        
        int id;
        String translation;

        private Type(int id, String translation) {
            this.id = id;
            this.translation = translation;
        }

        public static Type byId(int id) {
            return switch (id) {
                case 1 -> LARGE;
                case 2 -> STAR;
                case 3 -> CREEPER;
                case 4 -> BURST;
                default -> SMALL;
            };
        }
    }

    private static boolean canEdit(ItemStack stack) {
        return stack.getItem() instanceof FireworkRocketItem;
    }

    private static Text starTranslation(Type type, List<Integer> colors) {
        String[] colorFormatted = new String[colors.size()];
        for (int i = 0; i < colors.size(); ++i) colorFormatted[i] = Util.formatColor(colors.get(i));
        return Text.translatable(OUTPUT_STAR, Text.translatable(type.translation), String.join(", ", colorFormatted));
    }

    private static Text starTranslation(Type type, List<Integer> colors, List<Integer> fadeColors) {
        String[] colorsFormatted = new String[colors.size()];
        for (int i = 0; i < colors.size(); ++i) colorsFormatted[i] = Util.formatColor(colors.get(i));
        String[] fadeColorsFormatted = new String[fadeColors.size()];
        for (int i = 0; i < fadeColors.size(); ++i) fadeColorsFormatted[i] = Util.formatColor(fadeColors.get(i));
        return Text.translatable(OUTPUT_STAR_FADE, Text.translatable(type.translation), String.join(", ", colorsFormatted), String.join(", ", fadeColorsFormatted));
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("firework")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> flightNode = ClientCommandManager
            .literal("flight")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> flightGetNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                int flight = EditHelper.getFireworkFlight(stack);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_GET, flight));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> flightSetNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                int old = EditHelper.getFireworkFlight(stack);
                if (old == 0) throw FLIGHT_ALREADY_IS_EXCEPTION;
                EditHelper.setFireworkFlight(stack, 0);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_SET, 0));
                return old;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> flightSetFlightNode = ClientCommandManager
            .argument("flight", IntegerArgumentType.integer(-128, 127))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                int flight = IntegerArgumentType.getInteger(context, "flight");
                int old = EditHelper.getFireworkFlight(stack);
                if (old == flight) throw FLIGHT_ALREADY_IS_EXCEPTION;
                EditHelper.setFireworkFlight(stack, flight);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_SET, flight));
                return old;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starNode = ClientCommandManager
            .literal("star")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starGetNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!EditHelper.hasFireworkExplosions(stack)) throw NO_STARS_EXCEPTION;
                List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = EditHelper.getFireworkExplosions(stack);
                
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_GET));
                for (int i = 0; i < explosions.size(); ++i) {
                    Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>> explosion = explosions.get(i);
                    Type type = Type.byId(explosion.getA());
                    List<Integer> colors = explosion.getB();
                    List<Integer> fadeColors = explosion.getE();
                    context.getSource().sendFeedback(Text.empty()
                        .append(Text.literal(String.valueOf(i) + ". ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
                        .append(fadeColors.isEmpty() ? starTranslation(type, colors) : starTranslation(type, colors, fadeColors))
                    );
                }
                return explosions.size();
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starAddNode = ClientCommandManager
            .literal("add")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Type> starAddTypeNode = ClientCommandManager
            .argument("type", EnumArgumentType.enumArgument(Type.class))
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, List<Integer>> starAddTypeColorsNode = ClientCommandManager
            .argument("colors", ListArgumentType.listArgument(ColorArgumentType.hex()))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(EditHelper.getFireworkExplosions(stack));
                explosions.add(new Quintet<>(type.id, colors, false, false, List.of()));
                EditHelper.setFireworkExplosions(stack, explosions);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_ADD, starTranslation(type, colors)));
                return explosions.size() - 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Boolean> starAddTypeColorsFlickerNode = ClientCommandManager
            .argument("flicker", BoolArgumentType.bool())
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                boolean flicker = BoolArgumentType.getBool(context, "flicker");
                List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(EditHelper.getFireworkExplosions(stack));
                explosions.add(new Quintet<>(type.id, colors, flicker, false, List.of()));
                EditHelper.setFireworkExplosions(stack, explosions);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_ADD, starTranslation(type, colors)));
                return explosions.size() - 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Boolean> starAddTypeColorsFlickerTrailNode = ClientCommandManager
            .argument("trail", BoolArgumentType.bool())
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                boolean flicker = BoolArgumentType.getBool(context, "flicker");
                boolean trail = BoolArgumentType.getBool(context, "trail");
                List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(EditHelper.getFireworkExplosions(stack));
                explosions.add(new Quintet<>(type.id, colors, flicker, trail, List.of()));
                EditHelper.setFireworkExplosions(stack, explosions);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_ADD, starTranslation(type, colors)));
                return explosions.size() - 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, List<Integer>> starAddTypeColorsFlickerTrailFadecolorsNode = ClientCommandManager
            .argument("fadeColors", ListArgumentType.listArgument(ColorArgumentType.hex()))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy().copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                boolean flicker = BoolArgumentType.getBool(context, "flicker");
                boolean trail = BoolArgumentType.getBool(context, "trail");
                List<Integer> fadeColors = ListArgumentType.getListArgument(context, "fadeColors");
                List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(EditHelper.getFireworkExplosions(stack));
                explosions.add(new Quintet<>(type.id, colors, flicker, trail, fadeColors));
                EditHelper.setFireworkExplosions(stack, explosions);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_ADD, starTranslation(type, colors, fadeColors)));
                return explosions.size() - 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starRemoveNode = ClientCommandManager
            .literal("remove")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> starRemoveIndexNode = ClientCommandManager
            .argument("index", IntegerArgumentType.integer(0))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!EditHelper.hasFireworkExplosions(stack)) throw NO_STARS_EXCEPTION;
                int index = IntegerArgumentType.getInteger(context, "index");
                List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(EditHelper.getFireworkExplosions(stack));
                if (explosions.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, explosions.size());
                explosions.remove(index);
                EditHelper.setFireworkExplosions(stack, explosions);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_REMOVE, index));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starClearNode = ClientCommandManager
            .literal("clear")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!EditHelper.hasFireworkExplosions(stack)) throw NO_STARS_EXCEPTION;
                EditHelper.setFireworkExplosions(stack, null);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_CLEAR));
                return 1;
            })
            .build();

        rootNode.addChild(node);

        // ... flight ...
        node.addChild(flightNode);
        // ... get
        flightNode.addChild(flightGetNode);
        // ... set [<flight>]
        flightNode.addChild(flightSetNode);
        flightSetNode.addChild(flightSetFlightNode);

        // ... star ...
        node.addChild(starNode);
        // ... get
        starNode.addChild(starGetNode);
        // ... add <type> <colors> [<flicker>] [<trail>] [<fadecolors>]
        starNode.addChild(starAddNode);
        starAddNode.addChild(starAddTypeNode);
        starAddTypeNode.addChild(starAddTypeColorsNode);
        starAddTypeColorsNode.addChild(starAddTypeColorsFlickerNode);
        starAddTypeColorsFlickerNode.addChild(starAddTypeColorsFlickerTrailNode);
        starAddTypeColorsFlickerTrailNode.addChild(starAddTypeColorsFlickerTrailFadecolorsNode);
        // ... remove <index>
        starNode.addChild(starRemoveNode);
        starRemoveNode.addChild(starRemoveIndexNode);
        // ... clear
        starNode.addChild(starClearNode);
    }
}
