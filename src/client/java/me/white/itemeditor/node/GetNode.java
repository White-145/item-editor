package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class GetNode {
	public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.get.error.cannotedit")).create();
	private static final String OUTPUT_GET = "commands.edit.get.get";

	private static Text getComponent(ItemStack stack) {
		return Text.empty()
				.append("[").append(stack.getName()).append("]")
				.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack))));
	}

	private static boolean canEdit(FabricClientCommandSource source) {
		ItemStack item = EditorUtil.getStack(source);
		return item == null || item.isEmpty();
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("get")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, ItemStackArgument> itemNode = ClientCommandManager
			.argument("item", ItemStackArgumentType.itemStack(registryAccess))
			.executes(context -> {
				if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
				if (!canEdit(context.getSource())) throw CANNOT_EDIT_EXCEPTION;
				ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);

				EditorUtil.setStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, 1, getComponent(stack)));
				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> itemCountNode = ClientCommandManager
			.argument("count", IntegerArgumentType.integer(0, 127))
			.executes(context -> {
				if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
				if (!canEdit(context.getSource())) throw CANNOT_EDIT_EXCEPTION;
				int count = IntegerArgumentType.getInteger(context, "count");
				ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(count, false);

				EditorUtil.setStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, count, getComponent(stack)));
				return 1;
			})
			.build();

		rootNode.addChild(node);

		// ... get <item> [<count>]
		node.addChild(itemNode);
		itemNode.addChild(itemCountNode);
	}
}
