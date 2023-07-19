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
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class CountNode {
	public static final CommandSyntaxException OVERFLOW_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.count.error.overflow")).create();;
	private static final String OUTPUT_GET = "commands.edit.count.get";
	private static final String OUTPUT_SET = "commands.edit.count.set";

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("count")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemUtil.checkHasItem(context.getSource());

				int count = ItemUtil.getItemStack(context.getSource()).getCount();

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, count));
				return count;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

				int oldCount = item.getCount();
				item.setCount(1);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, 1));
				return oldCount;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setCountNode = ClientCommandManager
			.argument("count", IntegerArgumentType.integer(0, 127))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int count = IntegerArgumentType.getInteger(context, "count");

				int oldCount = item.getCount();
				item.setCount(count);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, count));
				return oldCount;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
			.literal("add")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

				if (item.getCount() + 1 > 127) throw OVERFLOW_EXCEPTION;
				item.setCount(item.getCount() + 1);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, item.getCount()));
				return item.getCount();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> addCountNode = ClientCommandManager
			.argument("count", IntegerArgumentType.integer(-126, 126))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int count = IntegerArgumentType.getInteger(context, "count");

				if (item.getCount() + count > 127 || item.getCount() + count < 0) throw OVERFLOW_EXCEPTION;
				item.setCount(item.getCount() + count);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, item.getCount()));
				return item.getCount();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

				if (item.getCount() - 1 < 0) throw OVERFLOW_EXCEPTION;
				item.setCount(item.getCount() - 1);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, item.getCount()));
				return item.getCount();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> removeCountNode = ClientCommandManager
			.argument("count", IntegerArgumentType.integer(-126, 126))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int count = IntegerArgumentType.getInteger(context, "count");

				if (item.getCount() - count > 127 || item.getCount() - count < 0) throw OVERFLOW_EXCEPTION;
				item.setCount(item.getCount() - count);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, item.getCount()));
				return item.getCount();
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> stackNode = ClientCommandManager
			.literal("stack")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

				int maxCount = item.getMaxCount();
				item.setCount(maxCount);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, maxCount));
				return maxCount;
			})
			.build();

		rootNode.addChild(node);

		// ... get
		node.addChild(getNode);

		// ... set [<count>]
		node.addChild(setNode);
		setNode.addChild(setCountNode);

		// ... add [<count>]
		node.addChild(addNode);
		addNode.addChild(addCountNode);

		// ... remove [<count>]
		node.addChild(removeNode);
		removeNode.addChild(removeCountNode);

		// ... stack
		node.addChild(stackNode);
	}
}
