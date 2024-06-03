package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class CountNode implements Node {
    private static final CommandSyntaxException OVERFLOW_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.count.error.overflow")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.count.error.alreadyis")).create();
    private static final CommandSyntaxException MAX_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.count.error.maxalreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.count.get";
    private static final String OUTPUT_SET = "commands.edit.count.set";
    private static final String OUTPUT_MAX_GET = "commands.edit.count.getmax";
    private static final String OUTPUT_MAX_SET = "commands.edit.count.setmax";
    private static final String OUTPUT_MAX_RESET = "commands.edit.count.resetmax";

    private static boolean hasMaxCount(ItemStack stack) {
        return stack.contains(DataComponentTypes.MAX_STACK_SIZE);
    }

    private static void setMaxCount(ItemStack stack, int count) {
        stack.set(DataComponentTypes.MAX_STACK_SIZE, count);
    }

    private static void removeMaxCount(ItemStack stack) {
        stack.remove(DataComponentTypes.MAX_STACK_SIZE);
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("count").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int count = stack.getCount();

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (stack.getCount() == 1) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setCount(1);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, 1));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setCountNode = ClientCommandManager.argument("count", IntegerArgumentType.integer(0, 99)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int count = IntegerArgumentType.getInteger(context, "count");
            if (count > stack.getMaxCount()) {
                throw OVERFLOW_EXCEPTION;
            }
            if (stack.getCount() == count) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setCount(count);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager.literal("add").executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int count = stack.getCount() + 1;
            if (count > 99) throw OVERFLOW_EXCEPTION;
            stack.setCount(count);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> addCountNode = ClientCommandManager.argument("count", IntegerArgumentType.integer(-98, 98)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int count = IntegerArgumentType.getInteger(context, "count");
            int newCount = stack.getCount() + count;
            if (newCount > 99 || newCount < 0) {
                throw OVERFLOW_EXCEPTION;
            }
            stack.setCount(newCount);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, newCount));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int count = stack.getCount() - 1;
            if (count < 0) {
                throw OVERFLOW_EXCEPTION;
            }
            stack.setCount(count);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> removeCountNode = ClientCommandManager.argument("count", IntegerArgumentType.integer(-126, 126)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int count = IntegerArgumentType.getInteger(context, "count");
            int newCount = stack.getCount() - count;
            if (newCount > 99 || newCount < 0) {
                throw OVERFLOW_EXCEPTION;
            }
            stack.setCount(newCount);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, newCount));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> maxNode = ClientCommandManager.literal("max").build();

        LiteralCommandNode<FabricClientCommandSource> maxGetNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int count = stack.getMaxCount();

            context.getSource().sendFeedback(Text.translatable(OUTPUT_MAX_GET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> maxSetNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> maxSetCountNode = ClientCommandManager.argument("count", IntegerArgumentType.integer(1, 99)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int count = IntegerArgumentType.getInteger(context, "count");
            if (count == stack.getMaxCount()) {
                throw ALREADY_IS_EXCEPTION;
            }
            setMaxCount(stack, count);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_MAX_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> maxResetNode = ClientCommandManager.literal("reset").executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!stack.contains(DataComponentTypes.MAX_STACK_SIZE)) {
                throw MAX_ALREADY_IS_EXCEPTION;
            }
            removeMaxCount(stack);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_MAX_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> stackNode = ClientCommandManager.literal("stack").executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (stack.getCount() == stack.getMaxCount()) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setCount(stack.getMaxCount());

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, stack.getMaxCount()));
            return Command.SINGLE_SUCCESS;
        }).build();

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

        // ... max ...
        node.addChild(maxNode);
        // ... get
        maxNode.addChild(maxGetNode);
        // ... set <count>
        maxNode.addChild(maxSetNode);
        maxSetNode.addChild(maxSetCountNode);
        // ... reset
        maxNode.addChild(maxResetNode);

        // ... stack
        node.addChild(stackNode);
    }
}
