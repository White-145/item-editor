package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
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

public class DurabilityNode implements Node {
    public static final CommandSyntaxException STACKABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.stackable")).create();
    public static final CommandSyntaxException ISNT_DAMAGABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.isntdamagable")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.alreadyis")).create();
    public static final CommandSyntaxException TOO_MUCH_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.toomuch")).create();
    public static final CommandSyntaxException MAX_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.maxalreadyis")).create();
    public static final CommandSyntaxException NO_MAX_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.nomax")).create();
    private static final String OUTPUT_GET = "commands.edit.durability.get";
    private static final String OUTPUT_SET = "commands.edit.durability.set";
    private static final String OUTPUT_RESET = "commands.edit.durability.reset";
    private static final String OUTPUT_PROGRESS = "commands.edit.durability.progress";
    private static final String OUTPUT_MAX_GET = "commands.edit.durability.getmax";
    private static final String OUTPUT_MAX_SET = "commands.edit.durability.setmax";
    private static final String OUTPUT_MAX_RESET = "commands.edit.durability.resetmax";

    private static boolean isUnstackable(ItemStack stack) {
        return stack.getMaxCount() == 1;
    }

    private static boolean isDamagable(ItemStack stack) {
        return isUnstackable(stack) && hasMaxDamage(stack);
    }

    private static boolean hasMaxDamage(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.MAX_DAMAGE)) {
            return false;
        }
        return stack.get(DataComponentTypes.MAX_DAMAGE) != 0;
    }

    private static void setMaxDamage(ItemStack stack, int maxDamage) {
        stack.set(DataComponentTypes.MAX_DAMAGE, maxDamage);
    }

    private static void removeMaxDamage(ItemStack stack) {
        stack.remove(DataComponentTypes.MAX_DAMAGE);
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("durability").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGABLE_EXCEPTION;
            }
            int damage = stack.getDamage();

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, stack.getMaxDamage() - damage, stack.getMaxDamage(), String.format("%.1f", (1 - (double) damage / stack.getMaxDamage()) * 100)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setDurabilityNode = ClientCommandManager.argument("durability", IntegerArgumentType.integer(0)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGABLE_EXCEPTION;
            }
            int durability = IntegerArgumentType.getInteger(context, "durability");
            if (durability > stack.getMaxDamage()) {
                throw TOO_MUCH_EXCEPTION;
            }
            int damage = stack.getMaxDamage() - durability;
            if (stack.getDamage() == damage) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setDamage(stack.getMaxDamage() - durability);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, durability));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> progressNode = ClientCommandManager.literal("progress").build();

        ArgumentCommandNode<FabricClientCommandSource, Double> progressProgressNode = ClientCommandManager.argument("progress", DoubleArgumentType.doubleArg(0, 100)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGABLE_EXCEPTION;
            }
            double progress = DoubleArgumentType.getDouble(context, "progress");
            int newDamage = (int) (stack.getMaxDamage() * (1 - progress / 100));
            if (stack.getDamage() == newDamage) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setDamage(newDamage);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_PROGRESS, progress));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager.literal("reset").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGABLE_EXCEPTION;
            }
            if (stack.getDamage() == 0) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setDamage(0);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> maxNode = ClientCommandManager.literal("max").build();

        LiteralCommandNode<FabricClientCommandSource> maxGetNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isUnstackable(stack)) {
                throw STACKABLE_EXCEPTION;
            }
            if (!hasMaxDamage(stack)) {
                throw NO_MAX_EXCEPTION;
            }
            int maxDamage = stack.getMaxDamage();

            context.getSource().sendFeedback(Text.translatable(OUTPUT_MAX_GET, maxDamage));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> maxSetNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> maxSetDurabilityNode = ClientCommandManager.argument("durability", IntegerArgumentType.integer(1)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isUnstackable(stack)) {
                throw STACKABLE_EXCEPTION;
            }
            int maxDurability = IntegerArgumentType.getInteger(context, "durability");
            if (hasMaxDamage(stack) && stack.getMaxDamage() == maxDurability) {
                throw MAX_ALREADY_IS_EXCEPTION;
            }
            setMaxDamage(stack, maxDurability);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_MAX_SET, maxDurability));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> maxResetNode = ClientCommandManager.literal("reset").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isUnstackable(stack)) {
                throw STACKABLE_EXCEPTION;
            }
            if (!hasMaxDamage(stack)) {
                throw NO_MAX_EXCEPTION;
            }
            removeMaxDamage(stack);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_MAX_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set <durability>
        node.addChild(setNode);
        setNode.addChild(setDurabilityNode);

        // ... progress <progress>
        node.addChild(progressNode);
        progressNode.addChild(progressProgressNode);

        // ... reset
        node.addChild(resetNode);

        // ... max ...
        node.addChild(maxNode);
        // ... get
        maxNode.addChild(maxGetNode);
        // ... set <durability>
        maxNode.addChild(maxSetNode);
        maxSetNode.addChild(maxSetDurabilityNode);
        // ... reset
        maxNode.addChild(maxResetNode);
    }
}
