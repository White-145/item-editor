package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.Colored;
import me.white.itemeditor.util.ItemUtil;
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
	private static final String DISPLAY_KEY = "display";
	private static final String LORE_KEY = "Lore";

	private static void checkHasLore(FabricClientCommandSource context) throws CommandSyntaxException {
		ItemStack item = ItemUtil.getItemStack(context);
		if (!item.hasNbt()) throw NO_LORE_EXCEPTION;
		NbtCompound nbt = item.getNbt();
		if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) throw NO_LORE_EXCEPTION;
		NbtCompound display = nbt.getCompound(DISPLAY_KEY);
		if (!display.contains(LORE_KEY, NbtElement.LIST_TYPE)) throw NO_LORE_EXCEPTION;
		NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
		if (lore.size() == 0) throw NO_LORE_EXCEPTION;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("lore")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemUtil.checkHasItem(context.getSource());
				checkHasLore(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource());
				NbtList lore = item.getSubNbt(DISPLAY_KEY).getList(LORE_KEY, NbtElement.STRING_TYPE);

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
				for (int i = 0; i < lore.size(); ++i) {
					context.getSource().sendFeedback(Text.empty()
						.append(Text.literal(String.valueOf(i) + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
						.append(Text.Serializer.fromJson(((NbtString)lore.get(i)).asString()))
					);
				}
				return lore.size();
			})
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Integer> getIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemUtil.checkHasItem(context.getSource());
				checkHasLore(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource());
				int index = IntegerArgumentType.getInteger(context, "index");

				NbtList lore = item.getSubNbt(DISPLAY_KEY).getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
				Text textLine = Text.Serializer.fromJson(((NbtString)lore.get(index)).asString());

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_LINE, index, textLine));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> setIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 63))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int index = IntegerArgumentType.getInteger(context, "index");

				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore.size() < index) {
					lore.set(index, Colored.EMPTY_LINE);
				} else {
					int off = index - lore.size() + 1;
					for (int i = 0; i < off; ++i) lore.add(Colored.EMPTY_LINE);
				}
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, index, ""));
				return 1;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> setIndexLineNode = ClientCommandManager
			.argument("line", StringArgumentType.greedyString())
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int index = IntegerArgumentType.getInteger(context, "index");
				Text line = Colored.of(StringArgumentType.getString(context, "line"));

				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore.size() < index) {
					lore.set(index, NbtString.of(Text.Serializer.toJson(line)));
				} else {
					int off = index - lore.size();
					for (int i = 0; i < off; ++i) lore.add(Colored.EMPTY_LINE);
					lore.add(NbtString.of(Text.Serializer.toJson(line)));
				}
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, index, line));
				return 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> removeIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				checkHasLore(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int index = IntegerArgumentType.getInteger(context, "index");

				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(lore.size(), index);
				lore.remove(index);
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, index));
				return 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
			.literal("add")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				lore.add(Colored.EMPTY_LINE);
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD, ""));
				return lore.size() - 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> addLineNode = ClientCommandManager
			.argument("line", StringArgumentType.greedyString())
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				Text line = Colored.of(StringArgumentType.getString(context, "line"));

				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				lore.add(NbtString.of(Text.Serializer.toJson(line)));
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD, line));
				return lore.size() - 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> insertNode = ClientCommandManager
			.literal("insert")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> insertIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 63))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int index = IntegerArgumentType.getInteger(context, "index");

				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore.size() < index) {
					lore.add(index, Colored.EMPTY_LINE);
				} else {
					int off = index - lore.size();
					for (int j = 0; j < off; ++j) lore.add(Colored.EMPTY_LINE);
				}
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, "", index));
				return 1;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> insertIndexLineNode = ClientCommandManager
			.argument("line", StringArgumentType.greedyString())
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int index = IntegerArgumentType.getInteger(context, "index");
				Text line = Colored.of(StringArgumentType.getString(context, "line"));

				NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				if (lore.size() < index) {
					lore.add(index, NbtString.of(Text.Serializer.toJson(line)));
				} else {
					int off = index - lore.size() - 1;
					for (int j = 0; j < off; ++j) lore.add(Colored.EMPTY_LINE);
					lore.add(NbtString.of(Text.Serializer.toJson(line)));
				}
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, line, index));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				checkHasLore(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				display.put(LORE_KEY, new NbtList());
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
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
				ItemUtil.checkCanEdit(context.getSource());
				checkHasLore(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int index = IntegerArgumentType.getInteger(context, "index");

				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				for (int i = 0; i < index; ++i) lore.remove(0);
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
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
				ItemUtil.checkCanEdit(context.getSource());
				checkHasLore(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int index = IntegerArgumentType.getInteger(context, "index");

				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				NbtList lore = display.getList(LORE_KEY, NbtElement.STRING_TYPE);
				while (lore.size() > index) lore.remove(lore.size());
				display.put(LORE_KEY, lore);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_AFTER, index));
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

		// ... clear [before|after <index>]
		node.addChild(clearNode);
		clearNode.addChild(clearBeforeNode);
		clearBeforeNode.addChild(clearBeforeIndexNode);
		clearNode.addChild(clearAfterNode);
		clearAfterNode.addChild(clearAfterIndexNode);
	}
}
