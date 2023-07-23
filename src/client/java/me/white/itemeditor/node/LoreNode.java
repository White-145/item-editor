package me.white.itemeditor.node;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.Colored;
import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LoreNode {
	public static final CommandSyntaxException NO_LORE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.lore.error.nolore")).create();
	public static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((index, size) -> Text.translatable("commands.edit.lore.error.outofbounds", index, size));
	private static final String OUTPUT_GET = "commands.edit.lore.get";
	private static final String OUTPUT_GET_LINE = "commands.edit.lore.getline";
	private static final String OUTPUT_SET = "commands.edit.lore.set";
	private static final String OUTPUT_INSERT = "commands.edit.lore.insert";
	private static final String OUTPUT_ADD = "commands.edit.lore.add";
	private static final String OUTPUT_REMOVE = "commands.edit.lore.remove";
	private static final String OUTPUT_CLEAR = "commands.edit.lore.clear";
	private static final String OUTPUT_CLEAR_BEFORE = "commands.edit.lore.clearbefore";
	private static final String OUTPUT_CLEAR_AFTER = "commands.edit.lore.clearafter";

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("lore")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasLore(stack)) throw NO_LORE_EXCEPTION;
				List<Text> lore = EditHelper.getLore(stack);

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
				for (int i = 0; i < lore.size(); ++i) {
					context.getSource().sendFeedback(Text.empty()
						.append(Text.literal(String.valueOf(i) + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
						.append(lore.get(i))
					);
				}
				return lore.size();
			})
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Integer> getIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				if (!EditHelper.hasLore(stack)) throw NO_LORE_EXCEPTION;
				List<Text> lore = EditHelper.getLore(stack);
				if (lore.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
				Text line = lore.get(index);

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_LINE, line));
				return lore.size();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> setIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 255))
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				List<Text> lore = new ArrayList<>(EditHelper.getLore(stack));
				if (lore.size() <= index) {
					int off = lore.size() - index + 1;
					for (int i = 0; i < off; ++i) {
						lore.add(Text.empty());
					}
				}
				lore.set(index, Text.empty());
				EditHelper.setLore(stack, lore);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, index, ""));
				return lore.size();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> setIndexLineNode = ClientCommandManager
			.argument("line", StringArgumentType.greedyString())
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				Text line = Colored.of(StringArgumentType.getString(context, "line"));
				List<Text> lore = new ArrayList<>(EditHelper.getLore(stack));
				if (lore.size() <= index) {
					int off = lore.size() - index + 1;
					for (int i = 0; i < off; ++i) {
						lore.add(Text.empty());
					}
				}
				lore.set(index, line);
				EditHelper.setLore(stack, lore);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, index, line));
				return lore.size();
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
				int index = IntegerArgumentType.getInteger(context, "index");
				if (!EditHelper.hasLore(stack)) throw NO_LORE_EXCEPTION;
				List<Text> lore = new ArrayList<>(EditHelper.getLore(stack));
				if (lore.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
				lore.remove(index);
				EditHelper.setLore(stack, lore);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, index));
				return lore.size();
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
			.literal("add")
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				List<Text> lore = new ArrayList<>(EditHelper.getLore(stack));
				lore.add(Text.empty());
				EditHelper.setLore(stack, lore);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD, ""));
				return lore.size() - 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> addLineNode = ClientCommandManager
			.argument("line", StringArgumentType.greedyString())
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				Text line = Colored.of(StringArgumentType.getString(context, "line"));
				List<Text> lore = new ArrayList<>(EditHelper.getLore(stack));
				lore.add(line);
				EditHelper.setLore(stack, lore);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD, line));
				return lore.size() - 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> insertNode = ClientCommandManager
			.literal("insert")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> insertIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 255))
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				List<Text> lore = new ArrayList<>(EditHelper.getLore(stack));
				if (lore.size() <= index) {
					int off = lore.size() - index;
					for (int i = 0; i < off; ++i) {
						lore.add(Text.empty());
					}
				}
				lore.add(index, Text.empty());
				EditHelper.setLore(stack, lore);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, index, ""));
				return lore.size();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> insertIndexLineNode = ClientCommandManager
			.argument("line", StringArgumentType.greedyString())
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				Text line = Colored.of(StringArgumentType.getString(context, "line"));
				List<Text> lore = new ArrayList<>(EditHelper.getLore(stack));
				if (lore.size() <= index) {
					int off = lore.size() - index;
					for (int i = 0; i < off; ++i) {
						lore.add(Text.empty());
					}
				}
				lore.add(index, line);
				EditHelper.setLore(stack, lore);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, index, line));
				return lore.size();
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasLore(stack)) throw NO_LORE_EXCEPTION;
				int old = EditHelper.getLore(stack).size();
				EditHelper.setLore(stack, null);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
				return old;
			})
			.build();
	
		LiteralCommandNode<FabricClientCommandSource> clearBeforeNode = ClientCommandManager
			.literal("before")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> clearBeforeIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				if (!EditHelper.hasLore(stack)) throw NO_LORE_EXCEPTION;
				List<Text> lore = EditHelper.getLore(stack);
				if (lore.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
				lore = lore.subList(index, lore.size());
				EditHelper.setLore(stack, lore);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_BEFORE, index));
				return lore.size();
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
				int index = IntegerArgumentType.getInteger(context, "index");
				if (!EditHelper.hasLore(stack)) throw NO_LORE_EXCEPTION;
				List<Text> lore = EditHelper.getLore(stack);
				if (lore.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
				lore = lore.subList(0, index + 1);
				EditHelper.setLore(stack, lore);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_AFTER, index));
				return lore.size();
			})
			.build();
		
		rootNode.addChild(node);
		
		// ... get [<index>]
		node.addChild(getNode);
		getNode.addChild(getIndexNode);

		// ... set <index> [<line>]
		node.addChild(setNode);
		setNode.addChild(setIndexNode);
		setIndexNode.addChild(setIndexLineNode);

		// ... remove <index>
		node.addChild(removeNode);
		removeNode.addChild(removeIndexNode);

		// ... add [<line>]
		node.addChild(addNode);
		addNode.addChild(addLineNode);

		// ... insert <index> [<line>]
		node.addChild(insertNode);
		insertNode.addChild(insertIndexNode);
		insertIndexNode.addChild(insertIndexLineNode);

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
