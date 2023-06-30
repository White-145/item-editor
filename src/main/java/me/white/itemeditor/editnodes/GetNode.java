package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class GetNode {
	private static final CommandSyntaxException HAND_NOT_EMPTY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.get.handnotempty")).create();
	private static final String OUTPUT_GET = "commands.edit.get.get";

	public static int execute(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		if (!EditCommand.getItemStack(context.getSource()).isEmpty()) throw HAND_NOT_EMPTY_EXCEPTION;
		ItemStackArgument itemArgument = ItemStackArgumentType.getItemStackArgument(context, "item");
		ItemStack item = itemArgument.createStack(1, false);
		EditCommand.setItemStack(context.getSource(), item);
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, 1, item.getName()));
		return 1;
	}

	public static int executeCount(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		if (!EditCommand.getItemStack(context.getSource()).isEmpty()) throw HAND_NOT_EMPTY_EXCEPTION;
		ItemStackArgument itemArgument = ItemStackArgumentType.getItemStackArgument(context, "item");
		int count = IntegerArgumentType.getInteger(context, "count");
		ItemStack item = itemArgument.createStack(count, false);
		EditCommand.setItemStack(context.getSource(), item);
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, count, item.getName()));
		return 1;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		ArgumentCommandNode<FabricClientCommandSource, ItemStackArgument> itemNode = ClientCommandManager
			.argument("item", ItemStackArgumentType.itemStack(registryAccess))
			.executes(GetNode::execute)
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> itemCountNode = ClientCommandManager
			.argument("count", IntegerArgumentType.integer(0, 64))
			.executes(GetNode::executeCount)
			.build();

		// ... <item> [<count>]
		node.addChild(itemNode);
		itemNode.addChild(itemCountNode);
	}
}
