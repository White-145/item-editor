package me.white.itemeditor.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.ColorArgumentType;
import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
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

public class BannerNode {
    private static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.cannotedit")).create();
    private static final CommandSyntaxException NO_PATTERNS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.nopatterns")).create();
    private static final String OUTPUT_GET = "commands.edit.banner.get";
    private static final String OUTPUT_GET_PATTERN = "commands.edit.banner.getpattern";
    private static final String OUTPUT_SET = "commands.edit.banner.set";
    private static final String OUTPUT_REMOVE = "commands.edit.banner.remove";
    private static final String OUTPUT_ADD = "commands.edit.banner.add";
    private static final String OUTPUT_INSERT = "commands.edit.banner.insert";
    private static final String OUTPUT_CLEAR = "commands.edit.banner.clear";
    private static final String OUTPUT_CLEAR_BEFORE = "commands.edit.banner.clearbefore";
    private static final String OUTPUT_CLEAR_AFTER = "commands.edit.banner.clearafter";

    private static boolean canEdit(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof BannerItem;
    }

    private static Text translation(Identifier id, int color) {
        return Text.translatable(String.format("block.%s.banner.%s.%s", id.getNamespace(), id.getPath(), ColorArgumentType.NAMED_COLORS[color]));
    }

    private static Text translation(BannerPattern pattern, int color) {
        return translation(Registries.BANNER_PATTERN.getId(pattern), color);
    }

    private static Text translation(Pair<BannerPattern, Integer> pattern) {
        return translation(pattern.getLeft(), pattern.getRight());
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("banner")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				if (!EditHelper.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
				List<Pair<BannerPattern, Integer>> patterns = EditHelper.getBannerPatterns(stack);

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
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				if (!EditHelper.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				List<Pair<BannerPattern, Integer>> patterns = EditHelper.getBannerPatterns(stack);
				if (patterns.size() <= index) throw Util.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
				Pair<BannerPattern, Integer> pattern = patterns.get(index);

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_PATTERN, translation(pattern)));
				return patterns.size();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> setIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 255))
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Reference<BannerPattern>> setIndexPatternNode = ClientCommandManager
			.argument("pattern", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BANNER_PATTERN))
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> setIndexPatternColorNode = ClientCommandManager
			.argument("color", ColorArgumentType.named())
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				if (!EditHelper.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				BannerPattern pattern = Util.getRegistryEntryArgument(context, "pattern", RegistryKeys.BANNER_PATTERN);
				int color = ColorArgumentType.getColor(context, "color");
				List<Pair<BannerPattern, Integer>> patterns = new ArrayList<>(EditHelper.getBannerPatterns(stack));
				if (patterns.size() <= index) throw Util.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
				patterns.set(index, Pair.of(pattern, color));
				EditHelper.setBannerPatterns(stack, patterns);

				Util.setItemStack(context.getSource(), stack);
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
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				if (!EditHelper.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				List<Pair<BannerPattern, Integer>> patterns = new ArrayList<>(EditHelper.getBannerPatterns(stack));
				if (patterns.size() <= index) throw Util.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
				patterns.remove(index);
				EditHelper.setBannerPatterns(stack, patterns);

				Util.setItemStack(context.getSource(), stack);
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
			
		ArgumentCommandNode<FabricClientCommandSource, Integer> addPatternColorNode = ClientCommandManager
			.argument("color", ColorArgumentType.named())
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				BannerPattern pattern = Util.getRegistryEntryArgument(context, "pattern", RegistryKeys.BANNER_PATTERN);
				int color = ColorArgumentType.getColor(context, "color");
				List<Pair<BannerPattern, Integer>> patterns = new ArrayList<>(EditHelper.getBannerPatterns(stack));
				patterns.add(Pair.of(pattern, color));
				EditHelper.setBannerPatterns(stack, patterns);

				Util.setItemStack(context.getSource(), stack);
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
			
		ArgumentCommandNode<FabricClientCommandSource, Integer> insertIndexPatternColorNode = ClientCommandManager
			.argument("color", ColorArgumentType.named())
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				BannerPattern pattern = Util.getRegistryEntryArgument(context, "pattern", RegistryKeys.BANNER_PATTERN);
				int color = ColorArgumentType.getColor(context, "color");
				List<Pair<BannerPattern, Integer>> patterns = new ArrayList<>(EditHelper.getBannerPatterns(stack));
				if (patterns.size() <= index) throw Util.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
				patterns.add(index, Pair.of(pattern, color));
				EditHelper.setBannerPatterns(stack, patterns);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, index, translation(Pair.of(pattern, color))));
				return patterns.size();
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
				int oldSize = EditHelper.getBannerPatterns(stack).size();
				EditHelper.setBannerPatterns(stack, null);

				Util.setItemStack(context.getSource(), stack);
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
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				List<Pair<BannerPattern, Integer>> patterns = EditHelper.getBannerPatterns(stack);
				if (patterns.size() <= index) throw Util.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
				int off = patterns.size() - index;
				patterns = patterns.subList(index, patterns.size());
				EditHelper.setBannerPatterns(stack, patterns);

				Util.setItemStack(context.getSource(), stack);
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
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasBannerPatterns(stack)) throw NO_PATTERNS_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				List<Pair<BannerPattern, Integer>> patterns = EditHelper.getBannerPatterns(stack);
				if (patterns.size() <= index) throw Util.OUT_OF_BOUNDS_EXCEPTION.create(index, patterns.size());
				patterns = patterns.subList(0, index + 1);
				EditHelper.setBannerPatterns(stack, patterns);

				Util.setItemStack(context.getSource(), stack);
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
