package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.ColorArgumentType;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class BannerNode {
    private static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.cannotedit")).create();
    private static final CommandSyntaxException NO_PATTERNS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.nopatterns")).create();
    private static final CommandSyntaxException INVALID_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.invalid")).create();
    private static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((index, size) -> Text.translatable("commands.edit.banner.error.outofbounds", index, size));
    private static final String OUTPUT_GET = "commands.edit.banner.get";
    private static final String OUTPUT_GET_PATTERN = "commands.edit.banner.getpattern";
    private static final String OUTPUT_SET = "commands.edit.banner.set";
    private static final String OUTPUT_REMOVE = "commands.edit.banner.remove";
    private static final String OUTPUT_ADD = "commands.edit.banner.add";
    private static final String OUTPUT_INSERT = "commands.edit.banner.insert";
    private static final String OUTPUT_CLEAR = "commands.edit.banner.clear";
    private static final String OUTPUT_CLEAR_BEFORE = "commands.edit.banner.clearbefore";
    private static final String OUTPUT_CLEAR_AFTER = "commands.edit.banner.clearafter";
    private static final String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";
    private static final String PATTERNS_KEY = "Patterns";
    private static final String PATTERN_KEY = "Pattern";
    private static final String COLOR_KEY = "Color";

    private static void checkCanEdit(FabricClientCommandSource source) throws CommandSyntaxException {
        Item item = Util.getItemStack(source).getItem();
        if (!(item instanceof BannerItem)) throw CANNOT_EDIT_EXCEPTION;
    }

    private static void checkHasPatterns(FabricClientCommandSource source) throws CommandSyntaxException {
        ItemStack item = Util.getItemStack(source);
        if (!item.hasNbt()) throw NO_PATTERNS_EXCEPTION;
        NbtCompound nbt = item.getNbt();
        if (!nbt.contains(BLOCK_ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_PATTERNS_EXCEPTION;
        NbtCompound blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG_KEY);
        if (!blockEntityTag.contains(PATTERNS_KEY, NbtElement.LIST_TYPE)) throw NO_PATTERNS_EXCEPTION;
        NbtList patterns = blockEntityTag.getList(PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
        if (patterns.isEmpty()) throw NO_PATTERNS_EXCEPTION;
        for (NbtElement pattern : patterns) {
            if (((NbtCompound)pattern).isEmpty()) throw NO_PATTERNS_EXCEPTION;
        }
    }

    private static Text translation(Identifier id, int color) {
        return Text.translatable(String.format("block.%s.banner.%s.%s", id.getNamespace(), id.getPath(), ColorArgumentType.NAMED_COLORS[color]));
    }

    private static Text translation(BannerPattern pattern, int color) {
        return translation(Registries.BANNER_PATTERN.getId(pattern), color);
    }

    private static Text translation(String id, int color) {
        return translation(BannerPattern.byId(id).value(), color);
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("banner")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasPatterns(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());
                
                NbtList patterns = item.getSubNbt(BLOCK_ENTITY_TAG_KEY).getList(PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
                
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                for (int i = 0; i < patterns.size(); ++i) {
                    NbtCompound pattern = (NbtCompound)patterns.get(i);
                    if (!pattern.contains(PATTERN_KEY, NbtElement.STRING_TYPE) || !pattern.contains(COLOR_KEY, NbtElement.INT_TYPE)) continue;
                    String id = pattern.getString(PATTERN_KEY);
                    int color = pattern.getInt(COLOR_KEY);
                    context.getSource().sendFeedback(Text.empty()
                        .append(Text.literal(String.valueOf(i) + ". ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
                        .append(translation(id, color))
                    );
                }
                return patterns.size();
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> getIndexNode = ClientCommandManager
            .argument("index", IntegerArgumentType.integer(0))
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasPatterns(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());
                int index = IntegerArgumentType.getInteger(context, "index");
                
                NbtList patterns = item.getSubNbt(BLOCK_ENTITY_TAG_KEY).getList(PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
                if (patterns.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                NbtCompound pattern = patterns.getCompound(index);
                if (!pattern.contains(PATTERN_KEY, NbtElement.STRING_TYPE) || !pattern.contains(COLOR_KEY, NbtElement.INT_TYPE)) throw INVALID_EXCEPTION;
                String id = pattern.getString(PATTERN_KEY);
                int color = pattern.getInt(COLOR_KEY);
                RegistryEntry<BannerPattern> nbtPattern = BannerPattern.byId(id);
                if (nbtPattern == null) throw INVALID_EXCEPTION;

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_PATTERN, translation(nbtPattern.value(), color)));
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
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> setIndexPatternColorNode = ClientCommandManager
            .argument("color", ColorArgumentType.named())
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasPatterns(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                int index = IntegerArgumentType.getInteger(context, "index");
                BannerPattern pattern = Util.getRegistryEntryArgument(context, "pattern", RegistryKeys.BANNER_PATTERN);
                int color = ColorArgumentType.getColor(context, "color");
                
                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                NbtList patterns = blockEntityTag.getList(PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
                if (patterns.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                NbtCompound patternNbt = new NbtCompound();
                patternNbt.putString(PATTERN_KEY, pattern.getId());
                patternNbt.putInt(COLOR_KEY, color);
                patterns.set(index, patternNbt);
                blockEntityTag.put(PATTERNS_KEY, patterns);
                item.setSubNbt(BLOCK_ENTITY_TAG_KEY, blockEntityTag);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, index, translation(pattern, color)));
                return patterns.size();
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
            .literal("remove")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> removeIndexNode = ClientCommandManager
            .argument("index", IntegerArgumentType.integer(0))
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasPatterns(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                int index = IntegerArgumentType.getInteger(context, "index");
                
                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                NbtList patterns = blockEntityTag.getList(PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
                if (patterns.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                NbtCompound pattern = patterns.getCompound(index);
                String id = pattern.getString(PATTERN_KEY);
                int color = pattern.getInt(COLOR_KEY);
                patterns.remove(index);
                blockEntityTag.put(PATTERNS_KEY, patterns);
                item.setSubNbt(BLOCK_ENTITY_TAG_KEY, blockEntityTag);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, index, translation(id, color)));
                return patterns.size();
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
            .literal("add")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Reference<BannerPattern>> addPatternNode = ClientCommandManager
            .argument("pattern", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BANNER_PATTERN))
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> addPatternColorNode = ClientCommandManager
            .argument("color", ColorArgumentType.named())
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                BannerPattern pattern = Util.getRegistryEntryArgument(context, "pattern", RegistryKeys.BANNER_PATTERN);
                int color = ColorArgumentType.getColor(context, "color");
                
                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                NbtList patterns = blockEntityTag.getList(PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
                NbtCompound patternNbt = new NbtCompound();
                patternNbt.putString(PATTERN_KEY, pattern.getId());
                patternNbt.putInt(COLOR_KEY, color);
                patterns.add(patternNbt);
                blockEntityTag.put(PATTERNS_KEY, patterns);
                item.setSubNbt(BLOCK_ENTITY_TAG_KEY, blockEntityTag);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD, translation(pattern, color)));
                return patterns.size();
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> insertNode = ClientCommandManager
            .literal("insert")
            .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> insertIndexNode = ClientCommandManager
            .argument("index", IntegerArgumentType.integer(0))
            .build();

        ArgumentCommandNode<FabricClientCommandSource, Reference<BannerPattern>> insertIndexPatternNode = ClientCommandManager
            .argument("pattern", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BANNER_PATTERN))
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> insertIndexPatternColorNode = ClientCommandManager
            .argument("color", ColorArgumentType.named())
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasPatterns(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                int index = IntegerArgumentType.getInteger(context, "index");
                BannerPattern pattern = Util.getRegistryEntryArgument(context, "pattern", RegistryKeys.BANNER_PATTERN);
                int color = ColorArgumentType.getColor(context, "color");
                
                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                NbtList patterns = blockEntityTag.getList(PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
                if (patterns.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
                NbtCompound patternNbt = new NbtCompound();
                patternNbt.putString(PATTERN_KEY, pattern.getId());
                patternNbt.putInt(COLOR_KEY, color);
                patterns.add(index, patternNbt);
                blockEntityTag.put(PATTERNS_KEY, patterns);
                item.setSubNbt(BLOCK_ENTITY_TAG_KEY, blockEntityTag);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, index, translation(pattern, color)));
                return patterns.size();
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
            .literal("clear")
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasPatterns(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                
                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                blockEntityTag.remove(PATTERNS_KEY);
                item.setSubNbt(BLOCK_ENTITY_TAG_KEY, blockEntityTag);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> clearBeforeNode = ClientCommandManager
            .literal("before")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> clearBeforeIndexNode = ClientCommandManager
            .argument("index", IntegerArgumentType.integer(0))
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasPatterns(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                int index = IntegerArgumentType.getInteger(context, "index");
                
                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                NbtList patterns = blockEntityTag.getList(PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
                for (int i = 0; i < index; ++i) patterns.remove(0);
                blockEntityTag.put(PATTERNS_KEY, patterns);
                item.setSubNbt(BLOCK_ENTITY_TAG_KEY, blockEntityTag);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_BEFORE, index));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> clearAfterNode = ClientCommandManager
            .literal("after")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> clearAfterIndexNode = ClientCommandManager
            .argument("index", IntegerArgumentType.integer(0))
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasPatterns(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                int index = IntegerArgumentType.getInteger(context, "index");
                
                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                NbtList patterns = blockEntityTag.getList(PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
                int off = patterns.size() - index - 1;
                for (int i = 0; i < off; ++i) patterns.remove(index + 1);
                blockEntityTag.put(PATTERNS_KEY, patterns);
                item.setSubNbt(BLOCK_ENTITY_TAG_KEY, blockEntityTag);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_AFTER, index));
                return 1;
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
