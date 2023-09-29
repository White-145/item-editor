package me.white.simpleitemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class CountNode implements Node {
	public static final CommandSyntaxException OVERFLOW_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.count.error.overflow")).create();
	public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.count.error.alreadyis")).create();
	private static final String OUTPUT_GET = "commands.edit.count.get";
	private static final String OUTPUT_SET = "commands.edit.count.set";

	public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
				.literal("count")
				.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
				.literal("get")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource());
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					int count = stack.getCount();

					context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, count));
					return count;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
				.literal("set")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					int old = stack.getCount();
					if (old == 1) throw ALREADY_IS_EXCEPTION;
					stack.setCount(1);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, 1));
					return old;
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setCountNode = ClientCommandManager
				.argument("count", IntegerArgumentType.integer(0, 127))
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					int old = stack.getCount();
					int count = IntegerArgumentType.getInteger(context, "count");
					if (old == count) throw ALREADY_IS_EXCEPTION;
					stack.setCount(count);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, count));
					return old;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
				.literal("add")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					int count = stack.getCount() + 1;
					if (count > 127) throw OVERFLOW_EXCEPTION;
					stack.setCount(count);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, count));
					return count;
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> addCountNode = ClientCommandManager
				.argument("count", IntegerArgumentType.integer(-126, 126))
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					int count = IntegerArgumentType.getInteger(context, "count");
					int newCount = stack.getCount() + count;
					if (newCount > 127 || newCount < 0) throw OVERFLOW_EXCEPTION;
					stack.setCount(newCount);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, newCount));
					return newCount;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
				.literal("remove")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					int count = stack.getCount() - 1;
					if (count < 0) throw OVERFLOW_EXCEPTION;
					stack.setCount(count);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, count));
					return count;
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> removeCountNode = ClientCommandManager
				.argument("count", IntegerArgumentType.integer(-126, 126))
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					int count = IntegerArgumentType.getInteger(context, "count");
					int newCount = stack.getCount() - count;
					if (newCount > 127 || newCount < 0) throw OVERFLOW_EXCEPTION;
					stack.setCount(newCount);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, newCount));
					return newCount;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> stackNode = ClientCommandManager
				.literal("stack")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (stack.getCount() == stack.getMaxCount()) throw ALREADY_IS_EXCEPTION;
					stack.setCount(stack.getMaxCount());

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, stack.getMaxCount()));
					return stack.getMaxCount();
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
