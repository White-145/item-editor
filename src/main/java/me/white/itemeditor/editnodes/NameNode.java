package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public class NameNode {
	private static final CommandSyntaxException NO_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.namenoname")).create();
	private static final String OUTPUT_GET = "commands.edit.name.get";
	private static final String OUTPUT_SET = "commands.edit.name.set";
	private static final String OUTPUT_RESET = "commands.edit.name.reset";

    public static boolean requirement(FabricClientCommandSource context) {
		return true;
	}

	private static ItemStack set(ItemStack item, Text name) {
		item.setCustomName(name);
		return item;
	}

	public static int executeGet(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource());
		NbtCompound display = item.getSubNbt("display");
		if (display == null || !display.contains("Name", NbtElement.STRING_TYPE)) {
			throw NO_NAME_EXCEPTION;
		}
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, Text.Serializer.fromJson(display.getString("Name").toString())));
		return 1;
	}

	public static int executeSet(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		Text name = Utils.colorize(StringArgumentType.getString(context, "value"));
		Utils.setItemStack(context.getSource(), set(item, name));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, name));
		return 1;
	}
	
	public static int executeReset(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		Utils.setItemStack(context.getSource(), set(item, null));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
		return 1;
	}
	
	public static int executeSetEmpty(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		Utils.setItemStack(context.getSource(), set(item, Text.empty()));
		return 1;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(NameNode::executeSetEmpty)
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(NameNode::executeGet)
			.build();

		LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
			.literal("reset")
			.executes(NameNode::executeReset)
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> setValueNode = ClientCommandManager
			.argument("value", StringArgumentType.greedyString())
			.executes(NameNode::executeSet)
			.build();

		/// ... get
		node.addChild(getNode);

		/// ... set [<value>]
		node.addChild(setNode);
		setNode.addChild(setValueNode);

		/// ... reset
		node.addChild(resetNode);
	}
}
