package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.text.Text;

public class NameNode {
	public static final CommandSyntaxException NO_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.noname")).create();
	private static final String OUTPUT_GET = "commands.edit.name.get";
	private static final String OUTPUT_SET = "commands.edit.name.set";
	private static final String OUTPUT_RESET = "commands.edit.name.reset";
	private static final String DISPLAY_KEY = "display";
	private static final String NAME_KEY = "Name";

	private static void checkHasName(FabricClientCommandSource context) throws CommandSyntaxException {
		ItemStack item = ItemUtil.getItemStack(context);
		if (!item.hasNbt()) throw NO_NAME_EXCEPTION;
		NbtCompound nbt = item.getNbt();
		if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) throw NO_NAME_EXCEPTION;
		NbtCompound display = nbt.getCompound(DISPLAY_KEY);
		if (!display.contains(NAME_KEY, NbtElement.STRING_TYPE)) throw NO_NAME_EXCEPTION;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("name")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemUtil.checkHasItem(context.getSource());
				checkHasName(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource());
				Text name = Text.Serializer.fromJson(item.getSubNbt(DISPLAY_KEY).getString(NAME_KEY));
				
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, name));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				display.put(NAME_KEY, Colored.EMPTY_LINE);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, ""));
				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> setNameNode = ClientCommandManager
			.argument("name", StringArgumentType.greedyString())
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				Text name = Colored.of(StringArgumentType.getString(context, "name"));

				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				display.putString(NAME_KEY, Text.Serializer.toJson(name));
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, name));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
			.literal("reset")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				checkHasName(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				NbtCompound display = item.getSubNbt(DISPLAY_KEY);
				display.remove(NAME_KEY);
				item.setSubNbt(DISPLAY_KEY, display);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
				return 1;
			})
			.build();

		rootNode.addChild(node);

		// ... get
		node.addChild(getNode);

		// ... set [<name>]
		node.addChild(setNode);
		setNode.addChild(setNameNode);

		// ... reset
		node.addChild(resetNode);
	}
}
