package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class GetNode {
	public static final CommandSyntaxException HAND_NOT_EMPTY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.get.error.handnotempty")).create();
	private static final String OUTPUT_GET = "commands.edit.get.get";

	private static void checkCanEdit(FabricClientCommandSource context) throws CommandSyntaxException {
		ItemStack item = ItemUtil.getItemStack(context);
		if (!(item == null || item.isEmpty())) throw HAND_NOT_EMPTY_EXCEPTION;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("get")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, ItemStackArgument> itemNode = ClientCommandManager
			.argument("item", ItemStackArgumentType.itemStack(registryAccess))
			.executes(context -> {
				checkCanEdit(context.getSource());

				ItemStackArgument itemArgument = ItemStackArgumentType.getItemStackArgument(context, "item");
				ItemStack item = itemArgument.createStack(1, false);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, 1, item.getName()));
				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> itemCountNode = ClientCommandManager
			.argument("count", IntegerArgumentType.integer(0, 127))
			.executes(context -> {
				checkCanEdit(context.getSource());
				
				ItemStackArgument itemArgument = ItemStackArgumentType.getItemStackArgument(context, "item");
				int count = IntegerArgumentType.getInteger(context, "count");

				ItemStack item = itemArgument.createStack(count, false);
				
				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, count, item.getName()));
				return 1;
			})
			.build();

		rootNode.addChild(node);

		// ... get <item> [<count>]
		node.addChild(itemNode);
		itemNode.addChild(itemCountNode);
	}
}
