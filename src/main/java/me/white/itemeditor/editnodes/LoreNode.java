package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
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
	private static final CommandSyntaxException NO_LORE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.lorenolore")).create();
	private static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((index, size) -> Text.translatable("commands.edit.error.loreoutofbounds", index, size));
	private static final String OUTPUT_GET = "commands.edit.lore.get";
	private static final String OUTPUT_GET_LINE = "commands.edit.lore.getline";
	private static final String OUTPUT_SET = "commands.edit.lore.set";
	private static final String OUTPUT_INSERT = "commands.edit.lore.insert";
	private static final String OUTPUT_ADD = "commands.edit.lore.add";
	private static final String OUTPUT_REMOVE = "commands.edit.lore.remove";
	private static final String OUTPUT_CLEAR = "commands.edit.lore.clear";
	private static final String OUTPUT_CLEAR_BEFORE = "commands.edit.lore.clearbefore";
	private static final String OUTPUT_CLEAR_AFTER = "commands.edit.lore.clearafter";

    public static boolean requirement(FabricClientCommandSource context) {
		return true;
	}

	private static NbtList getLore(ItemStack item) {
		NbtCompound display = item.getOrCreateSubNbt("display");
		NbtList lore = display.getList("Lore", NbtElement.STRING_TYPE);
		if (lore == null) {
			lore = new NbtList();
			display.put("Lore", lore);
		}
		return lore;
	}

	private static void setLore(ItemStack item, NbtList lore) {
		NbtCompound display = item.getOrCreateSubNbt("display");
		display.put("Lore", lore);
	}

	private static ItemStack set(ItemStack item, int index, NbtString line) {
		NbtList lore = getLore(item);
		if (index < lore.size()) {
			lore.set(index, line);
		} else {
			int off = index - lore.size();
			for (int i = 0; i < off; ++i) {
				lore.add(Utils.EMPTY_LINE);
			}
			lore.add(line);
		}
		setLore(item, lore);
		return item;
	}

	private static ItemStack add(ItemStack item, NbtString line) {
		NbtList lore = getLore(item);
		lore.add(line);
		setLore(item, lore);
		return item;
	}

	private static ItemStack insert(ItemStack item, int index, NbtString line) {
		NbtList lore = getLore(item);
		if (index < lore.size()) {
			lore.add(index, line);
		} else {
			int off = index - lore.size() - 1;
			for (int i = 0; i < off; ++i) {
				lore.add(Utils.EMPTY_LINE);
			}
			lore.add(line);
		}
		setLore(item, lore);
		return item;
	}

	private static ItemStack remove(ItemStack item, int index) throws CommandSyntaxException {
		NbtList lore = getLore(item);
		if (index < lore.size()) {
			lore.remove(index);
		} else {
			throw OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
		}
		setLore(item, lore);
		return item;
	}

	private static ItemStack clear(ItemStack item) {
		NbtList lore = getLore(item);
		lore.clear();
		setLore(item, lore);
		return item;
	}

	private static ItemStack clearBefore(ItemStack item, int value) throws CommandSyntaxException {
		NbtList lore = getLore(item);
		for (int i = 0; i < value; ++i) {
			lore.remove(0);
		}
		setLore(item, lore);
		return item;
	}

	private static ItemStack clearAfter(ItemStack item, int value) throws CommandSyntaxException {
		NbtList lore = getLore(item);
		if (value >= lore.size()) {
			throw OUT_OF_BOUNDS_EXCEPTION.create(value, lore.size());
		}
		int off = lore.size() - value - 1;
		for (int i = 0; i < off; ++i) {
			lore.remove(lore.size() - 1);
		}
		setLore(item, lore);
		return item;
	}

	public static int executeGet(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource());
		NbtCompound display = item.getSubNbt("display");
		if (display == null || !display.contains("Lore", NbtElement.LIST_TYPE)) {
			throw NO_LORE_EXCEPTION;
		}
		NbtList lore = display.getList("Lore", NbtElement.STRING_TYPE);
		if (lore == null) {
			throw NO_LORE_EXCEPTION;
		}
		if (lore.size() == 0) {
			throw NO_LORE_EXCEPTION;
		}

		ClientPlayerEntity player = context.getSource().getPlayer();
		player.sendMessage(Text.translatable(OUTPUT_GET));
		for (int i = 0; i < lore.size(); ++i) {
			Text textLine = Text.Serializer.fromJson(((NbtString)lore.get(i)).asString());
			player.sendMessage(Text.empty().append(Text.empty().append(String.valueOf(i) + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(textLine));
		}

		return 1;
	}

	public static int executeGetLine(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource());
		int i = IntegerArgumentType.getInteger(context, "index");
		NbtCompound display = item.getSubNbt("display");
		if (display == null || !display.contains("Lore", NbtElement.LIST_TYPE)) {
			throw NO_LORE_EXCEPTION;
		}
		NbtList lore = display.getList("Lore", NbtElement.STRING_TYPE);
		if (lore == null) {
			throw NO_LORE_EXCEPTION;
		}
		if (lore.size() <= i) {
			throw OUT_OF_BOUNDS_EXCEPTION.create(i, lore.size());
		}
		ClientPlayerEntity player = context.getSource().getPlayer();
		Text textLine = Text.Serializer.fromJson(((NbtString)lore.get(i)).asString());
		player.sendMessage(Text.translatable(OUTPUT_GET_LINE, i, textLine));
		return 1;
	}

	public static int executeSet(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		int i = IntegerArgumentType.getInteger(context, "index");
		Text valueColor = Utils.colorize(StringArgumentType.getString(context, "value"));
		NbtString value = NbtString.of(Text.Serializer.toJson(valueColor));
		Utils.setItemStack(context.getSource(), set(item, i, value));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, i, valueColor));
		return 1;
	}

	public static int executeSetEmpty(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		int i = IntegerArgumentType.getInteger(context, "index");
		Utils.setItemStack(context.getSource(), set(item, i, Utils.EMPTY_LINE));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, i, ""));
		return 1;
	}

	public static int executeRemove(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		int i = IntegerArgumentType.getInteger(context, "index");
		Utils.setItemStack(context.getSource(), remove(item, i));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_REMOVE, i));
		return 1;
	}

	public static int executeAdd(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		Text valueColor = Utils.colorize(StringArgumentType.getString(context, "value"));
		NbtString value = NbtString.of(Text.Serializer.toJson(valueColor));
		Utils.setItemStack(context.getSource(), add(item, value));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_ADD, valueColor));
		return 1;
	}

	public static int executeAddEmpty(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		Utils.setItemStack(context.getSource(), add(item, Utils.EMPTY_LINE));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_ADD, ""));
		return 1;
	}

	public static int executeInsert(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		int i = IntegerArgumentType.getInteger(context, "index");
		Text valueColor = Utils.colorize(StringArgumentType.getString(context, "value"));
		NbtString value = NbtString.of(Text.Serializer.toJson(valueColor));
		Utils.setItemStack(context.getSource(), insert(item, i, value));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_INSERT, valueColor, i));
		return 1;
	}

	public static int executeInsertEmpty(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		int i = IntegerArgumentType.getInteger(context, "index");
		Utils.setItemStack(context.getSource(), insert(item, i, Utils.EMPTY_LINE));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_INSERT, "", i));
		return 1;
	}

	public static int executeClear(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		Utils.setItemStack(context.getSource(), clear(item));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR));
		return 1;
	}

	public static int executeClearBefore(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		int i = IntegerArgumentType.getInteger(context, "index");
		Utils.setItemStack(context.getSource(), clearBefore(item, i));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR_BEFORE, i));
		return 1;
	}

	public static int executeClearAfter(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		int i = IntegerArgumentType.getInteger(context, "index");
		Utils.setItemStack(context.getSource(), clearAfter(item, i));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR_AFTER, i));
		return 1;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(LoreNode::executeGet)
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Integer> getIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(LoreNode::executeGetLine)
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> setIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 63))
			.executes(LoreNode::executeSetEmpty)
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> setValueNode = ClientCommandManager
			.argument("value", StringArgumentType.greedyString())
			.executes(LoreNode::executeSet)
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> removeIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(LoreNode::executeRemove)
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
			.literal("add")
			.executes(LoreNode::executeAddEmpty)
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> addValueNode = ClientCommandManager
			.argument("value", StringArgumentType.greedyString())
			.executes(LoreNode::executeAdd)
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> insertNode = ClientCommandManager
			.literal("insert")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> insertIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 63))
			.executes(LoreNode::executeInsertEmpty)
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> insertValueNode = ClientCommandManager
			.argument("value", StringArgumentType.greedyString())
			.executes(LoreNode::executeInsert)
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(LoreNode::executeClear)
			.build();
	
		LiteralCommandNode<FabricClientCommandSource> clearBeforeNode = ClientCommandManager
			.literal("before")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> clearBeforeValueNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(LoreNode::executeClearBefore)
			.build();

		LiteralCommandNode<FabricClientCommandSource> clearAfterNode = ClientCommandManager
			.literal("after")
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Integer> clearAfterValueNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(LoreNode::executeClearAfter)
			.build();
		
		// ... get [<index>]
		node.addChild(getNode);
		getNode.addChild(getIndexNode);

		// ... set <index> [<value>]
		node.addChild(setNode);
		setNode.addChild(setIndexNode);
		setIndexNode.addChild(setValueNode);

		// ... remove <index>
		node.addChild(removeNode);
		removeNode.addChild(removeIndexNode);

		// ... add [<value>]
		node.addChild(addNode);
		addNode.addChild(addValueNode);

		// ... insert <index> [<value>]
		node.addChild(insertNode);
		insertNode.addChild(insertIndexNode);
		insertIndexNode.addChild(insertValueNode);

		// ... clear [...]
		node.addChild(clearNode);
		// ... before <value>
		clearNode.addChild(clearBeforeNode);
		clearBeforeNode.addChild(clearBeforeValueNode);
		// ... after <value>
		clearNode.addChild(clearAfterNode);
		clearAfterNode.addChild(clearAfterValueNode);
	}
}
