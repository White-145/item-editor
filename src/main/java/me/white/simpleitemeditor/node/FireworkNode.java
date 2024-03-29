package me.white.simpleitemeditor.node;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.argument.ColorArgumentType;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.argument.ListArgumentType;
import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import oshi.util.tuples.Quintet;

public class FireworkNode implements Node {
    public static final CommandSyntaxException ISNT_FIREWORK_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.isntfirework")).create();
    public static final CommandSyntaxException NO_EXPLOSIONS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.noexplosions")).create();
    public static final CommandSyntaxException FLIGHT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.flightalreadyis")).create();
    private static final String OUTPUT_FLIGHT_GET = "commands.edit.firework.flightget";
    private static final String OUTPUT_FLIGHT_SET = "commands.edit.firework.flightset";
    private static final String OUTPUT_EXPLOSION = "commands.edit.firework.explosion";
    private static final String OUTPUT_EXPLOSION_FADE = "commands.edit.firework.explosionfade";
    private static final String OUTPUT_EXPLOSION_GET = "commands.edit.firework.explosionget";
    private static final String OUTPUT_EXPLOSION_ADD = "commands.edit.firework.explosionadd";
    private static final String OUTPUT_EXPLOSION_REMOVE = "commands.edit.firework.explosionremove";
    private static final String OUTPUT_EXPLOSION_CLEAR = "commands.edit.firework.explosionclear";

    private enum Type {
        SMALL("commands.edit.firework.typesmall"),
        LARGE("commands.edit.firework.typelarge"),
        STAR("commands.edit.firework.typestar"),
        CREEPER("commands.edit.firework.typecreeper"),
        BURST("commands.edit.firework.typeburst");

        final String translationKey;

        Type(String translation) {
            this.translationKey = translation;
        }
    }

    private static boolean isFirework(ItemStack stack) {
        return stack.getItem() instanceof FireworkRocketItem;
    }

    private static Text starTranslation(Type type, List<Integer> colors) {
        String[] colorFormatted = new String[colors.size()];
        for (int i = 0; i < colors.size(); ++i) colorFormatted[i] = EditorUtil.formatColor(colors.get(i));
        return Text.translatable(OUTPUT_EXPLOSION, Text.translatable(type.translationKey), String.join(", ", colorFormatted));
    }

    private static Text starTranslation(Type type, List<Integer> colors, List<Integer> fadeColors) {
        String[] colorsFormatted = new String[colors.size()];
        for (int i = 0; i < colors.size(); ++i) colorsFormatted[i] = EditorUtil.formatColor(colors.get(i));
        String[] fadeColorsFormatted = new String[fadeColors.size()];
        for (int i = 0; i < fadeColors.size(); ++i) fadeColorsFormatted[i] = EditorUtil.formatColor(fadeColors.get(i));
        return Text.translatable(OUTPUT_EXPLOSION_FADE, Text.translatable(type.translationKey), String.join(", ", colorsFormatted), String.join(", ", fadeColorsFormatted));
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("firework")
                .build();

        LiteralCommandNode<FabricClientCommandSource> flightNode = ClientCommandManager
                .literal("flight")
                .build();

        LiteralCommandNode<FabricClientCommandSource> flightGetNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    int flight = ItemUtil.getFireworkFlight(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_GET, flight));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> flightSetNode = ClientCommandManager
                .literal("set")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    int old = ItemUtil.getFireworkFlight(stack);
                    if (old == 0) throw FLIGHT_ALREADY_IS_EXCEPTION;
                    ItemUtil.setFireworkFlight(stack, 0);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_SET, 0));
                    return old;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> flightSetFlightNode = ClientCommandManager
                .argument("flight", IntegerArgumentType.integer(-128, 127))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    int flight = IntegerArgumentType.getInteger(context, "flight");
                    int old = ItemUtil.getFireworkFlight(stack);
                    if (old == flight) throw FLIGHT_ALREADY_IS_EXCEPTION;
                    ItemUtil.setFireworkFlight(stack, flight);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_SET, flight));
                    return old;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> explosionNode = ClientCommandManager
                .literal("explosion")
                .build();

        LiteralCommandNode<FabricClientCommandSource> explosionGetNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    if (!ItemUtil.hasFireworkExplosions(stack)) throw NO_EXPLOSIONS_EXCEPTION;
                    List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = ItemUtil.getFireworkExplosions(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_EXPLOSION_GET));
                    for (int i = 0; i < explosions.size(); ++i) {
                        Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>> explosion = explosions.get(i);
                        Type type = Type.values()[explosion.getA()];
                        List<Integer> colors = explosion.getB();
                        List<Integer> fadeColors = explosion.getE();
                        context.getSource().sendFeedback(Text.empty()
                                .append(Text.literal(i + ". ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
                                .append(fadeColors.isEmpty() ? starTranslation(type, colors) : starTranslation(type, colors, fadeColors))
                        );
                    }
                    return explosions.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> explosionAddNode = ClientCommandManager
                .literal("add")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Type> explosionAddTypeNode = ClientCommandManager
                .argument("type", EnumArgumentType.enumArgument(Type.class))
                .build();

        ArgumentCommandNode<FabricClientCommandSource, List<Integer>> explosionAddTypeColorsNode = ClientCommandManager
                .argument("colors", ListArgumentType.listArgument(ColorArgumentType.color()))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                    List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                    List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(ItemUtil.getFireworkExplosions(stack));
                    explosions.add(new Quintet<>(type.ordinal(), colors, false, false, List.of()));
                    ItemUtil.setFireworkExplosions(stack, explosions);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_EXPLOSION_ADD, starTranslation(type, colors)));
                    return explosions.size() - 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> explosionAddTypeColorsFlickerNode = ClientCommandManager
                .argument("flicker", BoolArgumentType.bool())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                    List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                    boolean flicker = BoolArgumentType.getBool(context, "flicker");
                    List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(ItemUtil.getFireworkExplosions(stack));
                    explosions.add(new Quintet<>(type.ordinal(), colors, flicker, false, List.of()));
                    ItemUtil.setFireworkExplosions(stack, explosions);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_EXPLOSION_ADD, starTranslation(type, colors)));
                    return explosions.size() - 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> explosionAddTypeColorsFlickerTrailNode = ClientCommandManager
                .argument("trail", BoolArgumentType.bool())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                    List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                    boolean flicker = BoolArgumentType.getBool(context, "flicker");
                    boolean trail = BoolArgumentType.getBool(context, "trail");
                    List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(ItemUtil.getFireworkExplosions(stack));
                    explosions.add(new Quintet<>(type.ordinal(), colors, flicker, trail, List.of()));
                    ItemUtil.setFireworkExplosions(stack, explosions);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_EXPLOSION_ADD, starTranslation(type, colors)));
                    return explosions.size() - 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, List<Integer>> explosionAddTypeColorsFlickerTrailFadecolorsNode = ClientCommandManager
                .argument("fadeColors", ListArgumentType.listArgument(ColorArgumentType.color()))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy().copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    Type type = EnumArgumentType.getEnum(context, "type", Type.class);
                    List<Integer> colors = ListArgumentType.getListArgument(context, "colors");
                    boolean flicker = BoolArgumentType.getBool(context, "flicker");
                    boolean trail = BoolArgumentType.getBool(context, "trail");
                    List<Integer> fadeColors = ListArgumentType.getListArgument(context, "fadeColors");
                    List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(ItemUtil.getFireworkExplosions(stack));
                    explosions.add(new Quintet<>(type.ordinal(), colors, flicker, trail, fadeColors));
                    ItemUtil.setFireworkExplosions(stack, explosions);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_EXPLOSION_ADD, starTranslation(type, colors, fadeColors)));
                    return explosions.size() - 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> explosionRemoveNode = ClientCommandManager
                .literal("remove")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> explosionRemoveIndexNode = ClientCommandManager
                .argument("index", IntegerArgumentType.integer(0))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    if (!ItemUtil.hasFireworkExplosions(stack)) throw NO_EXPLOSIONS_EXCEPTION;
                    int index = IntegerArgumentType.getInteger(context, "index");
                    List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions = new ArrayList<>(ItemUtil.getFireworkExplosions(stack));
                    if (explosions.size() <= index)
                        throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, explosions.size());
                    explosions.remove(index);
                    ItemUtil.setFireworkExplosions(stack, explosions);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_EXPLOSION_REMOVE, index));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> explosionClearNode = ClientCommandManager
                .literal("clear")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isFirework(stack)) throw ISNT_FIREWORK_EXCEPTION;
                    if (!ItemUtil.hasFireworkExplosions(stack)) throw NO_EXPLOSIONS_EXCEPTION;
                    ItemUtil.setFireworkExplosions(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_EXPLOSION_CLEAR));
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

        // ... explosion ...
        node.addChild(explosionNode);
        // ... get
        explosionNode.addChild(explosionGetNode);
        // ... add <type> <colors> [<flicker>] [<trail>] [<fadecolors>]
        explosionNode.addChild(explosionAddNode);
        explosionAddNode.addChild(explosionAddTypeNode);
        explosionAddTypeNode.addChild(explosionAddTypeColorsNode);
        explosionAddTypeColorsNode.addChild(explosionAddTypeColorsFlickerNode);
        explosionAddTypeColorsFlickerNode.addChild(explosionAddTypeColorsFlickerTrailNode);
        explosionAddTypeColorsFlickerTrailNode.addChild(explosionAddTypeColorsFlickerTrailFadecolorsNode);
        // ... remove <index>
        explosionNode.addChild(explosionRemoveNode);
        explosionRemoveNode.addChild(explosionRemoveIndexNode);
        // ... clear
        explosionNode.addChild(explosionClearNode);
    }
}
