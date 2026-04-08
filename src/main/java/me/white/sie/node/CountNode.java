package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.util.EditorUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CountNode implements Node {
    private static final CommandSyntaxException OVERFLOW_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.count.error.overflow")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.count.error.alreadyis")).create();
    private static final CommandSyntaxException MAX_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.count.error.maxalreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.count.get";
    private static final String OUTPUT_SET = "commands.edit.count.set";
    private static final String OUTPUT_MAX_GET = "commands.edit.count.getmax";
    private static final String OUTPUT_MAX_SET = "commands.edit.count.setmax";
    private static final String OUTPUT_MAX_RESET = "commands.edit.count.resetmax";

    private static boolean hasMaxCount(ItemStack stack) {
        return stack.has(DataComponents.MAX_STACK_SIZE);
    }

    private static void setMaxCount(ItemStack stack, int count) {
        stack.set(DataComponents.MAX_STACK_SIZE, count);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("count").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            int count = stack.getCount();

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (stack.getCount() == 1) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setCount(1);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, 1));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setCountNode = commandManager.argument("count", IntegerArgumentType.integer(0, 99)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int count = IntegerArgumentType.getInteger(context, "count");
            if (count > 99) {
                throw OVERFLOW_EXCEPTION;
            }
            if (stack.getCount() == count) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setCount(count);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> addNode = commandManager.literal("add").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int count = stack.getCount() + 1;
            if (count > 99) throw OVERFLOW_EXCEPTION;
            stack.setCount(count);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> addCountNode = commandManager.argument("count", IntegerArgumentType.integer(-98, 98)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int count = IntegerArgumentType.getInteger(context, "count");
            int newCount = stack.getCount() + count;
            if (newCount > 99 || newCount < 0) {
                throw OVERFLOW_EXCEPTION;
            }
            stack.setCount(newCount);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, newCount));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> takeNode = commandManager.literal("take").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int count = stack.getCount() - 1;
            if (count < 0) {
                throw OVERFLOW_EXCEPTION;
            }
            stack.setCount(count);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> takeCountNode = commandManager.argument("count", IntegerArgumentType.integer(-126, 126)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int count = IntegerArgumentType.getInteger(context, "count");
            int newCount = stack.getCount() - count;
            if (newCount > 99 || newCount < 0) {
                throw OVERFLOW_EXCEPTION;
            }
            stack.setCount(newCount);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, newCount));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxNode = commandManager.literal("max").build();

        CommandNode<S> maxGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            int count = stack.getMaxStackSize();

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_MAX_GET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxSetNode = commandManager.literal("set").build();

        CommandNode<S> maxSetCountNode = commandManager.argument("count", IntegerArgumentType.integer(1, 99)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int count = IntegerArgumentType.getInteger(context, "count");
            if (count == stack.getMaxStackSize()) {
                throw ALREADY_IS_EXCEPTION;
            }
            setMaxCount(stack, count);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_MAX_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxResetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int initial = stack.getPrototype().get(DataComponents.MAX_STACK_SIZE);
            if (stack.has(DataComponents.MAX_STACK_SIZE) && stack.get(DataComponents.MAX_STACK_SIZE).equals(initial)) {
                throw MAX_ALREADY_IS_EXCEPTION;
            }
            setMaxCount(stack, initial);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_MAX_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> stackNode = commandManager.literal("stack").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (stack.getCount() == stack.getMaxStackSize()) {
                throw MAX_ALREADY_IS_EXCEPTION;
            }
            stack.setCount(stack.getMaxStackSize());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, stack.getMaxStackSize()));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get
        node.addChild(getNode);

        // ... set [<count>]
        node.addChild(setNode);
        setNode.addChild(setCountNode);

        // ... add [<count>]
        node.addChild(addNode);
        addNode.addChild(addCountNode);

        // ... take [<count>]
        node.addChild(takeNode);
        takeNode.addChild(takeCountNode);

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

        return node;
    }
}
