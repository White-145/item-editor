package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import me.white.itemeditor.Colored;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LoreNode {
	private static final CommandSyntaxException NO_LORE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.lore.error.nolore")).create();
	private static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((index, size) -> Text.translatable("commands.edit.lore.error.outofbounds", index, size));
	private static final String OUTPUT_GET = "commands.edit.lore.get";
	private static final String OUTPUT_GET_LINE = "commands.edit.lore.getline";
	private static final String OUTPUT_SET = "commands.edit.lore.set";
	private static final String OUTPUT_INSERT = "commands.edit.lore.insert";
	private static final String OUTPUT_ADD = "commands.edit.lore.add";
	private static final String OUTPUT_REMOVE = "commands.edit.lore.remove";
	private static final String OUTPUT_CLEAR = "commands.edit.lore.clear";
	private static final String OUTPUT_CLEAR_BEFORE = "commands.edit.lore.clearbefore";
	private static final String OUTPUT_CLEAR_AFTER = "commands.edit.lore.clearafter";
	private static final String DISPLAY_KEY = "display";
	private static final String LORE_KEY = "Lore";

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("lore")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				EditCommand.checkHasItem(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource());
				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				if (display == null || !display.contains(LORE_KEY, NbtElement.LIST_TYPE)) throw NO_LORE_EXCEPTION;
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null || lore.size() == 0) throw NO_LORE_EXCEPTION;
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
				for (int i = 0; i < lore.size(); ++i) {
					Text textLine = Text.Serializer.fromJson(((NbtString)lore.get(i)).asString());
					Text feedback = Text.empty().append(Text.empty().append(String.valueOf(i) + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(textLine);
					context.getSource().sendFeedback(feedback);
				}
				return lore.size();
			})
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Integer> getIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				EditCommand.checkHasItem(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource());
				int i = IntegerArgumentType.getInteger(context, "index");
				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null) lore = new NbtList();
				if (lore.size() <= i) throw OUT_OF_BOUNDS_EXCEPTION.create(i, lore.size());
				Text textLine = Text.Serializer.fromJson(((NbtString)lore.get(i)).asString());
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_LINE, i, textLine));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> setIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 63))
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");
				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null) lore = new NbtList();
				if (i < lore.size()) {
					lore.set(i, Colored.EMPTY_LINE);
				} else {
					int off = i - lore.size() + 1;
					for (int j = 0; j < off; ++j) lore.add(Colored.EMPTY_LINE);
				}
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, i, ""));
				return 1;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> setLineNode = ClientCommandManager
			.argument("line", StringArgumentType.greedyString())
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");
				Text line = Colored.of(StringArgumentType.getString(context, "line"));
				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null) lore = new NbtList();
				if (i < lore.size()) {
					lore.set(i, NbtString.of(Text.Serializer.toJson(line)));
				} else {
					int off = i - lore.size();
					for (int j = 0; j < off; ++j) lore.add(Colored.EMPTY_LINE);
					lore.add(NbtString.of(Text.Serializer.toJson(line)));
				}
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, i, line));
				return 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> removeIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");
				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				if (display == null || !display.contains(LORE_KEY, NbtElement.LIST_TYPE)) throw NO_LORE_EXCEPTION;
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null || lore.size() == 0) throw NO_LORE_EXCEPTION;
				if (lore.size() < i) throw OUT_OF_BOUNDS_EXCEPTION.create(lore.size(), i);
				lore.remove(i);
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_REMOVE, i));
				return 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
			.literal("add")
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null) lore = new NbtList();
				lore.add(Colored.EMPTY_LINE);
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_ADD, ""));
				return lore.size();
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> addLineNode = ClientCommandManager
			.argument("line", StringArgumentType.greedyString())
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Text textLine = Colored.of(StringArgumentType.getString(context, "line"));
				NbtString line = NbtString.of(Text.Serializer.toJson(textLine));
				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null) lore = new NbtList();
				lore.add(line);
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_ADD, textLine));
				return lore.size();
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> insertNode = ClientCommandManager
			.literal("insert")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> insertIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 63))
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");
				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null) lore = new NbtList();
				if (i < lore.size()) {
					lore.add(i, Colored.EMPTY_LINE);
				} else {
					int off = i - lore.size();
					for (int j = 0; j < off; ++j) lore.add(Colored.EMPTY_LINE);
				}
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_INSERT, "", i));
				return 1;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> insertLineNode = ClientCommandManager
			.argument("line", StringArgumentType.greedyString())
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");
				Text textLine = Colored.of(StringArgumentType.getString(context, "line"));
				NbtString line = NbtString.of(Text.Serializer.toJson(textLine));
				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null) lore = new NbtList();
				if (i < lore.size()) {
					lore.add(i, line);
				} else {
					int off = i - lore.size() - 1;
					for (int j = 0; j < off; ++j) lore.add(Colored.EMPTY_LINE);
					lore.add(line);
				}
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_INSERT, textLine, i));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				if (display == null || !display.contains(LORE_KEY, NbtElement.LIST_TYPE)) throw NO_LORE_EXCEPTION;
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null || lore.size() == 0) throw NO_LORE_EXCEPTION;
				lore.clear();
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR));
				return 1;
			})
			.build();
	
		LiteralCommandNode<FabricClientCommandSource> clearBeforeNode = ClientCommandManager
			.literal("before")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> clearBeforeIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");
				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				if (display == null || !display.contains(LORE_KEY, NbtElement.LIST_TYPE)) throw NO_LORE_EXCEPTION;
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null || lore.size() == 0) throw NO_LORE_EXCEPTION;
				for (int j = 0; j < i; ++j) lore.remove(0);
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_BEFORE, i));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> clearAfterNode = ClientCommandManager
			.literal("after")
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Integer> clearAfterIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");
				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				if (display == null || !display.contains(LORE_KEY, NbtElement.LIST_TYPE)) throw NO_LORE_EXCEPTION;
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore == null || lore.size() == 0) throw NO_LORE_EXCEPTION;
				if (i >= lore.size()) throw OUT_OF_BOUNDS_EXCEPTION.create(i, lore.size());
				int off = lore.size() - i - 1;
				for (int j = 0; j < off; ++j) lore.remove(lore.size() - 1);
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_AFTER, i));
				return 1;
			})
			.build();
		
		rootNode.addChild(node);
		
		// ... get [<index>]
		node.addChild(getNode);
		getNode.addChild(getIndexNode);

		// ... set <index> [<line>]
		node.addChild(setNode);
		setNode.addChild(setIndexNode);
		setIndexNode.addChild(setLineNode);

		// ... remove <index>
		node.addChild(removeNode);
		removeNode.addChild(removeIndexNode);

		// ... add [<line>]
		node.addChild(addNode);
		addNode.addChild(addLineNode);

		// ... insert <index> [<line>]
		node.addChild(insertNode);
		insertNode.addChild(insertIndexNode);
		insertIndexNode.addChild(insertLineNode);

		// ... clear [before|after <index>]
		node.addChild(clearNode);
		clearNode.addChild(clearBeforeNode);
		clearBeforeNode.addChild(clearBeforeIndexNode);
		clearNode.addChild(clearAfterNode);
		clearAfterNode.addChild(clearAfterIndexNode);
	}
}
