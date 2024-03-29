package me.white.simpleitemeditor.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.white.simpleitemeditor.argument.EnumArgumentType;
import org.apache.commons.lang3.tuple.Pair;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class BannerNode implements Node {
    private static final CommandSyntaxException ISNT_BANNER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.isntbanner")).create();
    private static final CommandSyntaxException NO_PATTERNS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.nopatterns")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.banner.get";
    private static final String OUTPUT_GET_PATTERN = "commands.edit.banner.getpattern";
    private static final String OUTPUT_SET = "commands.edit.banner.set";
    private static final String OUTPUT_REMOVE = "commands.edit.banner.remove";
    private static final String OUTPUT_ADD = "commands.edit.banner.add";
    private static final String OUTPUT_INSERT = "commands.edit.banner.insert";
    private static final String OUTPUT_CLEAR = "commands.edit.banner.clear";
    private static final String OUTPUT_CLEAR_BEFORE = "commands.edit.banner.clearbefore";
    private static final String OUTPUT_CLEAR_AFTER = "commands.edit.banner.clearafter";

    private enum Color {
        WHITE,
        ORANGE,
        MAGENTA,
        LIGHT_BLUE,
        YELLOW,
        LIME,
        PINK,
        GRAY,
        LIGHT_GRAY,
        CYAN,
        PURPLE,
        BLUE,
        BROWN,
        GREEN,
        RED,
        BLACK
    }

    private static boolean isBanner(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BannerItem;
    }

    private static Text translation(Identifier id, int color) {
        return Text.translatable(String.format("block.%s.banner.%s.%s", id.getNamespace(), id.getPath(), Color.values()[color].name().toLowerCase(Locale.ROOT)));
    }

    private static Text translation(BannerPattern pattern, int color) {
        return translation(Registries.BANNER_PATTERN.getId(pattern), color);
    }

    private static Text translation(Pair<BannerPattern, Integer> pattern) {
        return translation(pattern.getLeft(), pattern.getRight());
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("banner")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isBanner(stack)) throw ISNT_BANNER_EXCEPTION;
                    if (!ItemUtil.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
                    List<Pair<BannerPattern, Integer>> patterns = ItemUtil.getBannerPatterns(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                    for (int i = 0; i < patterns.size(); ++i) {
                        Pair<BannerPattern, Integer> pattern = patterns.get(i);
                        context.getSource().sendFeedback(Text.empty()
                                .append(Text.literal(String.format("%d. ", i)).setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                .append(translation(pattern))
                        );
                    }
                    return patterns.size();
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> getIndexNode = ClientCommandManager
                .argument("index", IntegerArgumentType.integer(0))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isBanner(stack)) throw ISNT_BANNER_EXCEPTION;
                    if (!ItemUtil.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
                    int index = IntegerArgumentType.getInteger(context, "index");
                    List<Pair<BannerPattern, Integer>> patterns = ItemUtil.getBannerPatterns(stack);
                    if (patterns.size() <= index)
                        throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                    Pair<BannerPattern, Integer> pattern = patterns.get(index);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_PATTERN, translation(pattern)));
                    return patterns.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setIndexNode = ClientCommandManager
                .argument("index", IntegerArgumentType.integer(0))
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Reference<BannerPattern>> setIndexPatternNode = ClientCommandManager
                .argument("pattern", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BANNER_PATTERN))
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Color> setIndexPatternColorNode = ClientCommandManager
                .argument("color", EnumArgumentType.enumArgument(Color.class))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isBanner(stack)) throw ISNT_BANNER_EXCEPTION;
                    if (!ItemUtil.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
                    int index = IntegerArgumentType.getInteger(context, "index");
                    BannerPattern pattern = EditorUtil.getRegistryEntryArgument(context, "pattern", RegistryKeys.BANNER_PATTERN);
                    int color = EnumArgumentType.getEnum(context, "color", Color.class).ordinal();
                    Pair<BannerPattern, Integer> bannerPattern = Pair.of(pattern, color);
                    List<Pair<BannerPattern, Integer>> patterns = new ArrayList<>(ItemUtil.getBannerPatterns(stack));
                    if (patterns.size() <= index)
                        throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                    Pair<BannerPattern, Integer> oldBannerPattern = patterns.get(index);
                    if (oldBannerPattern.equals(bannerPattern)) throw ALREADY_IS_EXCEPTION;
                    patterns.set(index, bannerPattern);
                    ItemUtil.setBannerPatterns(stack, patterns);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, index, translation(Pair.of(pattern, color))));
                    return patterns.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> removeIndexNode = ClientCommandManager
                .argument("index", IntegerArgumentType.integer(0))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isBanner(stack)) throw ISNT_BANNER_EXCEPTION;
                    if (!ItemUtil.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
                    int index = IntegerArgumentType.getInteger(context, "index");
                    List<Pair<BannerPattern, Integer>> patterns = new ArrayList<>(ItemUtil.getBannerPatterns(stack));
                    if (patterns.size() <= index)
                        throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                    patterns.remove(index);
                    ItemUtil.setBannerPatterns(stack, patterns);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, index));
                    return patterns.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Reference<BannerPattern>> addPatternNode = ClientCommandManager
                .argument("pattern", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BANNER_PATTERN))
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Color> addPatternColorNode = ClientCommandManager
                .argument("color", EnumArgumentType.enumArgument(Color.class))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    BannerPattern pattern = EditorUtil.getRegistryEntryArgument(context, "pattern", RegistryKeys.BANNER_PATTERN);
                    int color = EnumArgumentType.getEnum(context, "color", Color.class).ordinal();
                    List<Pair<BannerPattern, Integer>> patterns = new ArrayList<>(ItemUtil.getBannerPatterns(stack));
                    patterns.add(Pair.of(pattern, color));
                    ItemUtil.setBannerPatterns(stack, patterns);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD, translation(Pair.of(pattern, color))));
                    return patterns.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> insertNode = ClientCommandManager
                .literal("insert")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> insertIndexNode = ClientCommandManager
                .argument("index", IntegerArgumentType.integer(0, 255))
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Reference<BannerPattern>> insertIndexPatternNode = ClientCommandManager
                .argument("pattern", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BANNER_PATTERN))
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Color> insertIndexPatternColorNode = ClientCommandManager
                .argument("color", EnumArgumentType.enumArgument(Color.class))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
                    int index = IntegerArgumentType.getInteger(context, "index");
                    BannerPattern pattern = EditorUtil.getRegistryEntryArgument(context, "pattern", RegistryKeys.BANNER_PATTERN);
                    int color = EnumArgumentType.getEnum(context, "color", Color.class).ordinal();
                    List<Pair<BannerPattern, Integer>> patterns = new ArrayList<>(ItemUtil.getBannerPatterns(stack));
                    if (patterns.size() <= index)
                        throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                    patterns.add(index, Pair.of(pattern, color));
                    ItemUtil.setBannerPatterns(stack, patterns);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, translation(Pair.of(pattern, color)), index));
                    return patterns.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
                .literal("clear")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasBannerPatterns(stack, false)) throw NO_PATTERNS_EXCEPTION;
                    int oldSize = ItemUtil.getBannerPatterns(stack).size();
                    ItemUtil.setBannerPatterns(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
                    return oldSize;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearBeforeNode = ClientCommandManager
                .literal("before")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> clearBeforeIndexNode = ClientCommandManager
                .argument("index", IntegerArgumentType.integer(1))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
                    int index = IntegerArgumentType.getInteger(context, "index");
                    List<Pair<BannerPattern, Integer>> patterns = ItemUtil.getBannerPatterns(stack);
                    if (patterns.size() <= index)
                        throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                    int off = patterns.size() - index;
                    patterns = patterns.subList(index, patterns.size());
                    ItemUtil.setBannerPatterns(stack, patterns);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_BEFORE, index));
                    return off;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearAfterNode = ClientCommandManager
                .literal("after")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> clearAfterIndexNode = ClientCommandManager
                .argument("index", IntegerArgumentType.integer(0))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
                    int index = IntegerArgumentType.getInteger(context, "index");
                    List<Pair<BannerPattern, Integer>> patterns = ItemUtil.getBannerPatterns(stack);
                    if (patterns.size() <= index)
                        throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                    patterns = patterns.subList(0, index + 1);
                    ItemUtil.setBannerPatterns(stack, patterns);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_AFTER, index));
                    return patterns.size();
                })
                .build();

        rootNode.addChild(node);

        // ... get [<index>]
        node.addChild(getNode);
        getNode.addChild(getIndexNode);

        // ... set <index> <pattern> <color>
        node.addChild(setNode);
        setNode.addChild(setIndexNode);
        setIndexNode.addChild(setIndexPatternNode);
        setIndexPatternNode.addChild(setIndexPatternColorNode);

        // ... remove <index>
        node.addChild(removeNode);
        removeNode.addChild(removeIndexNode);

        // ... add <pattern> <color>
        node.addChild(addNode);
        addNode.addChild(addPatternNode);
        addPatternNode.addChild(addPatternColorNode);

        // ... insert <index> <pattern> <color>
        node.addChild(insertNode);
        insertNode.addChild(insertIndexNode);
        insertIndexNode.addChild(insertIndexPatternNode);
        insertIndexPatternNode.addChild(insertIndexPatternColorNode);

        // ... clear
        node.addChild(clearNode);
        // ... clear before <index>
        clearNode.addChild(clearBeforeNode);
        clearBeforeNode.addChild(clearBeforeIndexNode);
        // ... clear after <index>
        clearNode.addChild(clearAfterNode);
        clearAfterNode.addChild(clearAfterIndexNode);
    }
}
