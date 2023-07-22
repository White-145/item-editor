package me.white.itemeditor.node;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

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
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

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
    private static final String FIREWORKS_KEY = "Fireworks";
    private static final String FLIGHT_KEY = "Flight";
    private static final String EXPLOSIONS_KEY = "Explosions";
    private static final String TYPE_KEY = "Type";
    private static final String COLORS_KEY = "Colors";
    private static final String FLICKER_KEY = "Flicker";
    private static final String TRAIL_KEY = "Trail";
    private static final String FADE_COLORS_KEY = "FadeColors";

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

    private static void checkCanEdit(FabricClientCommandSource source) throws CommandSyntaxException {
        Item item = Util.getItemStack(source).getItem();
        if (!(item instanceof FireworkRocketItem)) throw CANNOT_EDIT_EXCEPTION;
    }

    private static void checkHasStars(FabricClientCommandSource source) throws CommandSyntaxException {
        ItemStack item = Util.getItemStack(source);
        if (!item.hasNbt()) throw NO_STARS_EXCEPTION;
        NbtCompound nbt = item.getNbt();
        if (!nbt.contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) throw NO_STARS_EXCEPTION;
        NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
        if (!fireworks.contains(EXPLOSIONS_KEY, NbtElement.LIST_TYPE)) throw NO_STARS_EXCEPTION;
        NbtList explosions = fireworks.getList(EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);
        if (explosions.isEmpty()) throw NO_STARS_EXCEPTION;
    }

    private static Text starTranslation(int type, Integer[] colors) {
        String[] colorFormatted = new String[colors.length];
        for (int i = 0; i < colors.length; ++i) colorFormatted[i] = Util.formatColor(colors[i]);
        return Text.translatable(OUTPUT_STAR, Text.translatable(Type.byId(type).translation), String.join(", ", colorFormatted));
    }

    private static Text starTranslation(int type, Integer[] colors, Integer[] fadeColors) {
        String[] colorsFormatted = new String[colors.length];
        for (int i = 0; i < colors.length; ++i) colorsFormatted[i] = Util.formatColor(colors[i]);
        String[] fadeColorsFormatted = new String[fadeColors.length];
        for (int i = 0; i < fadeColors.length; ++i) fadeColorsFormatted[i] = Util.formatColor(fadeColors[i]);
        return Text.translatable(OUTPUT_STAR_FADE, Text.translatable(Type.byId(type).translation), String.join(", ", colorsFormatted), String.join(", ", fadeColorsFormatted));
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
                Util.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());

                int flight = 0;
                if (item.hasNbt() && item.getNbt().contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) flight = item.getSubNbt(FIREWORKS_KEY).getInt(FLIGHT_KEY);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_GET, flight));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> flightSetNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                Util.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();

                NbtCompound firework = item.getOrCreateSubNbt(FIREWORKS_KEY);
                if (firework.contains(FLIGHT_KEY, NbtElement.INT_TYPE) && firework.getInt(FLIGHT_KEY) == 1) throw FLIGHT_ALREADY_IS_EXCEPTION;
                firework.putInt(FLIGHT_KEY, 1);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_SET, 1));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> flightSetFlightNode = ClientCommandManager
            .argument("flight", IntegerArgumentType.integer(-128, 127))
            .executes(context -> {
                Util.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                int flight = IntegerArgumentType.getInteger(context, "flight");

                NbtCompound firework = item.getOrCreateSubNbt(FIREWORKS_KEY);
                if (firework.contains(FLIGHT_KEY, NbtElement.INT_TYPE) && firework.getInt(FLIGHT_KEY) == flight) throw FLIGHT_ALREADY_IS_EXCEPTION;
                firework.putInt(FLIGHT_KEY, flight);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_SET, flight));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starNode = ClientCommandManager
            .literal("star")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starGetNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                Util.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());
                checkHasStars(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());

                NbtCompound fireworks = item.getOrCreateSubNbt(FIREWORKS_KEY);
                NbtList explosions = fireworks.getList(EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_GET));
                for (int i = 0; i < explosions.size(); ++i) {
                    NbtCompound explosion = explosions.getCompound(i);
                    int type = explosion.getInt(TYPE_KEY);
                    int[] colors = explosion.getIntArray(COLORS_KEY);
                    int[] fadeColors = explosion.getIntArray(FADE_COLORS_KEY);
                    context.getSource().sendFeedback(Text.empty()
                        .append(Text.literal(String.valueOf(i) + ". ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
                        .append(fadeColors.length == 0 ? starTranslation(type, ArrayUtils.toObject(colors)) : starTranslation(type, ArrayUtils.toObject(fadeColors)))
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
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());
                Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                List<Integer> colors = ListArgumentType.getListArgument(context, "colors");

                NbtCompound fireworks = item.getOrCreateSubNbt(FIREWORKS_KEY);
                NbtList explosions = fireworks.getList(EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);
                NbtCompound explosion = new NbtCompound();
                explosion.putInt(TYPE_KEY, type.id);
                explosion.putIntArray(COLORS_KEY, colors);
                explosions.add(explosion);
                fireworks.put(EXPLOSIONS_KEY, explosions);
                item.setSubNbt(FIREWORKS_KEY, fireworks);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_ADD, starTranslation(type.id, colors.toArray(new Integer[0]))));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Boolean> starAddTypeColorsFlickerNode = ClientCommandManager
            .argument("flicker", BoolArgumentType.bool())
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());
                Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                boolean flicker = BoolArgumentType.getBool(context, "flicker");

                NbtCompound fireworks = item.getOrCreateSubNbt(FIREWORKS_KEY);
                NbtList explosions = fireworks.getList(EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);
                NbtCompound explosion = new NbtCompound();
                explosion.putInt(TYPE_KEY, type.id);
                explosion.putIntArray(COLORS_KEY, colors);
                explosion.putBoolean(FLICKER_KEY, flicker);
                explosions.add(explosion);
                fireworks.put(EXPLOSIONS_KEY, explosions);
                item.setSubNbt(FIREWORKS_KEY, fireworks);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_ADD, starTranslation(type.id, colors.toArray(new Integer[0]))));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Boolean> starAddTypeColorsFlickerTrailNode = ClientCommandManager
            .argument("trail", BoolArgumentType.bool())
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());
                Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                boolean flicker = BoolArgumentType.getBool(context, "flicker");
                boolean trail = BoolArgumentType.getBool(context, "trail");

                NbtCompound fireworks = item.getOrCreateSubNbt(FIREWORKS_KEY);
                NbtList explosions = fireworks.getList(EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);
                NbtCompound explosion = new NbtCompound();
                explosion.putInt(TYPE_KEY, type.id);
                explosion.putIntArray(COLORS_KEY, colors);
                explosion.putBoolean(FLICKER_KEY, flicker);
                explosion.putBoolean(TRAIL_KEY, trail);
                explosions.add(explosion);
                fireworks.put(EXPLOSIONS_KEY, explosions);
                item.setSubNbt(FIREWORKS_KEY, fireworks);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_ADD, starTranslation(type.id, colors.toArray(new Integer[0]))));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, List<Integer>> starAddTypeColorsFlickerTrailFadecolorsNode = ClientCommandManager
            .argument("fadeColors", ListArgumentType.listArgument(ColorArgumentType.hex()))
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());
                Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                boolean flicker = BoolArgumentType.getBool(context, "flicker");
                boolean trail = BoolArgumentType.getBool(context, "trail");
                List<Integer> fadeColors = ListArgumentType.getListArgument(context, "fadeColors");

                NbtCompound fireworks = item.getOrCreateSubNbt(FIREWORKS_KEY);
                NbtList explosions = fireworks.getList(EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);
                NbtCompound explosion = new NbtCompound();
                explosion.putInt(TYPE_KEY, type.id);
                explosion.putIntArray(COLORS_KEY, colors);
                explosion.putBoolean(FLICKER_KEY, flicker);
                explosion.putBoolean(TRAIL_KEY, trail);
                explosion.putIntArray(FADE_COLORS_KEY, fadeColors);
                explosions.add(explosion);
                fireworks.put(EXPLOSIONS_KEY, explosions);
                item.setSubNbt(FIREWORKS_KEY, fireworks);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_ADD, starTranslation(type.id, colors.toArray(new Integer[0]), fadeColors.toArray(new Integer[0]))));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starRemoveNode = ClientCommandManager
            .literal("remove")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> starRemoveIndexNode = ClientCommandManager
            .argument("index", IntegerArgumentType.integer(0))
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasStars(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());
                int index = IntegerArgumentType.getInteger(context, "index");

                NbtCompound fireworks = item.getOrCreateSubNbt(FIREWORKS_KEY);
                NbtList explosions = fireworks.getList(EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);
                if (explosions.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, explosions.size());
                explosions.remove(index);
                fireworks.put(EXPLOSIONS_KEY, explosions);
                item.setSubNbt(FIREWORKS_KEY, fireworks);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_STAR_REMOVE));
                return explosions.size();
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starClearNode = ClientCommandManager
            .literal("clear")
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasStars(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());

                NbtCompound fireworks = item.getOrCreateSubNbt(FIREWORKS_KEY);
                fireworks.put(EXPLOSIONS_KEY, new NbtList());
                item.setSubNbt(FIREWORKS_KEY, fireworks);

                Util.setItemStack(context.getSource(), item);
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
