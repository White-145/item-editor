package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
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

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("count").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            int count = stack.getCount();

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, count));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, 1));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> addNode = commandManager.literal("add").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int count = stack.getCount() + 1;
            if (count > 99) throw OVERFLOW_EXCEPTION;
            stack.setCount(count);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, count));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, newCount));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, count));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, newCount));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxNode = commandManager.literal("max").build();

        CommandNode<S> maxGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            int count = stack.getMaxCount();

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_MAX_GET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxSetNode = commandManager.literal("set").build();

        CommandNode<S> maxSetCountNode = commandManager.argument("count", IntegerArgumentType.integer(1, 99)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int count = IntegerArgumentType.getInteger(context, "count");
            if (count == stack.getMaxCount()) {
                throw ALREADY_IS_EXCEPTION;
            }
            setMaxCount(stack, count);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_MAX_SET, count));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxResetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!stack.contains(DataComponentTypes.MAX_STACK_SIZE)) {
                throw MAX_ALREADY_IS_EXCEPTION;
            }
            removeMaxCount(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_MAX_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> stackNode = commandManager.literal("stack").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (stack.getCount() == stack.getMaxCount()) {
                throw MAX_ALREADY_IS_EXCEPTION;
            }
            stack.setCount(stack.getMaxCount());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, stack.getMaxCount()));
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
