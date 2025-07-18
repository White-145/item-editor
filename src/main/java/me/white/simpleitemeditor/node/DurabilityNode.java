package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
//? if >=1.21.5 {
import net.minecraft.util.Unit;
//?} else {
/*import net.minecraft.component.type.UnbreakableComponent;
*///?}

import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class DurabilityNode implements Node {
    private static final CommandSyntaxException STACKABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.stackable")).create();
    private static final CommandSyntaxException ISNT_DAMAGEABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.isntdamageable")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.alreadyis")).create();
    private static final CommandSyntaxException TOO_MUCH_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.toomuch")).create();
    private static final CommandSyntaxException MAX_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.maxalreadyis")).create();
    private static final CommandSyntaxException NO_MAX_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.nomax")).create();
    private static final CommandSyntaxException NO_DEFAULT_MAX_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.nodefaultmax")).create();
    private static final CommandSyntaxException UNBREAKABLE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.unbreakablealreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.durability.get";
    private static final String OUTPUT_SET = "commands.edit.durability.set";
    private static final String OUTPUT_RESET = "commands.edit.durability.reset";
    private static final String OUTPUT_PROGRESS = "commands.edit.durability.progress";
    private static final String OUTPUT_MAX_GET = "commands.edit.durability.getmax";
    private static final String OUTPUT_MAX_SET = "commands.edit.durability.setmax";
    private static final String OUTPUT_MAX_REMOVE = "commands.edit.durability.removemax";
    private static final String OUTPUT_MAX_RESET = "commands.edit.durability.resetmax";
    private static final String OUTPUT_UNBREAKABLE_GET_ENABLED = "commands.edit.durability.unbreakablegetenabled";
    private static final String OUTPUT_UNBREAKABLE_GET_DISABLED = "commands.edit.durability.unbreakablegetdisabled";
    private static final String OUTPUT_UNBREAKABLE_ENABLE = "commands.edit.durability.unbreakableenable";
    private static final String OUTPUT_UNBREAKABLE_DISABLE = "commands.edit.durability.unbreakabledisable";

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

    private static void resetMaxDamage(ItemStack stack) {
        stack.remove(DataComponentTypes.MAX_DAMAGE);
    }

    private static boolean isUnbreakable(ItemStack stack) {
        return stack.contains(DataComponentTypes.UNBREAKABLE);
    }

    private static void setUnbreakable(ItemStack stack, boolean isUnbreakable) {
        if (isUnbreakable) {
            //? if >=1.21.5 {
            stack.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);
            //?} else {
            /*stack.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(TooltipNode.TooltipPart.UNBREAKABLE.get(stack)));
            *///?}
        } else {
            stack.remove(DataComponentTypes.UNBREAKABLE);
        }
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("durability").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGEABLE_EXCEPTION;
            }
            int damage = stack.getDamage();

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, stack.getMaxDamage() - damage, stack.getMaxDamage(), String.format("%.1f", (1 - (double) damage / stack.getMaxDamage()) * 100)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setDurabilityNode = commandManager.argument("durability", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGEABLE_EXCEPTION;
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

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, durability));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> progressNode = commandManager.literal("progress").build();

        CommandNode<S> progressProgressNode = commandManager.argument("progress", DoubleArgumentType.doubleArg(0, 100)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGEABLE_EXCEPTION;
            }
            double progress = DoubleArgumentType.getDouble(context, "progress");
            int newDamage = (int) (stack.getMaxDamage() * (1 - progress / 100));
            if (stack.getDamage() == newDamage) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setDamage(newDamage);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PROGRESS, progress));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> resetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGEABLE_EXCEPTION;
            }
            if (stack.getDamage() == 0) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.setDamage(0);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_RESET));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxNode = commandManager.literal("max").build();

        CommandNode<S> maxGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isUnstackable(stack)) {
                throw STACKABLE_EXCEPTION;
            }
            if (!hasMaxDamage(stack)) {
                throw NO_MAX_EXCEPTION;
            }
            int maxDamage = stack.getMaxDamage();

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_MAX_GET, maxDamage));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxSetNode = commandManager.literal("set").build();

        CommandNode<S> maxSetDurabilityNode = commandManager.argument("durability", IntegerArgumentType.integer(1)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isUnstackable(stack)) {
                throw STACKABLE_EXCEPTION;
            }
            int maxDurability = IntegerArgumentType.getInteger(context, "durability");
            if (hasMaxDamage(stack) && stack.getMaxDamage() == maxDurability) {
                throw MAX_ALREADY_IS_EXCEPTION;
            }
            setMaxDamage(stack, maxDurability);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_MAX_SET, maxDurability));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isUnstackable(stack)) {
                throw STACKABLE_EXCEPTION;
            }
            if (!hasMaxDamage(stack)) {
                throw NO_MAX_EXCEPTION;
            }
            resetMaxDamage(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_MAX_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> maxResetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isUnstackable(stack)) {
                throw STACKABLE_EXCEPTION;
            }
            if (!stack.getDefaultComponents().contains(DataComponentTypes.MAX_DAMAGE)) {
                throw NO_DEFAULT_MAX_EXCEPTION;
            }
            int defaultMaxDamage = stack.getDefaultComponents().get(DataComponentTypes.MAX_DAMAGE);
            if (stack.getMaxDamage() == defaultMaxDamage) {
                throw ALREADY_IS_EXCEPTION;
            }
            setMaxDamage(stack, defaultMaxDamage);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_MAX_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> unbreakableNode = commandManager.literal("unbreakable").build();

        CommandNode<S> unbreakableGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGEABLE_EXCEPTION;
            }
            boolean isUnbreakable = isUnbreakable(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(isUnbreakable ? OUTPUT_UNBREAKABLE_GET_ENABLED : OUTPUT_UNBREAKABLE_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> unbreakableSetNode = commandManager.literal("set").build();

        CommandNode<S> unbreakableSetunbreakableNode = commandManager.argument("unbreakable", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGEABLE_EXCEPTION;
            }
            boolean isUnbreakable = BoolArgumentType.getBool(context, "unbreakable");
            if (isUnbreakable == isUnbreakable(stack)) {
                throw UNBREAKABLE_ALREADY_IS_EXCEPTION;
            }
            setUnbreakable(stack, isUnbreakable);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(isUnbreakable ? OUTPUT_UNBREAKABLE_ENABLE : OUTPUT_UNBREAKABLE_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> resistanceNode = commandManager.literal("resistance").build();

        CommandNode<S> resistanceGetNode = commandManager.literal("get").build();

        CommandNode<S> resistanceAddNode = commandManager.literal("add").build();

        CommandNode<S> resistanceAddTypeNode = commandManager.argument("type", RegistryArgumentType.registryEntry(RegistryKeys.DAMAGE_TYPE, registryAccess)).build();

        CommandNode<S> resistanceRemoveNode = commandManager.literal("remove").build();

        CommandNode<S> resistanceRemoveTypeNode = commandManager.argument("type", RegistryArgumentType.registryEntry(RegistryKeys.DAMAGE_TYPE, registryAccess)).build();

        CommandNode<S> resistanceClearNode = commandManager.literal("clear").build();

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
        // ... remove
        maxNode.addChild(maxRemoveNode);
        // ... reset
        maxNode.addChild(maxResetNode);

        // ... unbreakable ...
        node.addChild(unbreakableNode);
        // ... get
        unbreakableNode.addChild(unbreakableGetNode);
        // ... set <unbreakable>
        unbreakableNode.addChild(unbreakableSetNode);
        unbreakableSetNode.addChild(unbreakableSetunbreakableNode);

        // ... resistance ...
        node.addChild(resistanceNode);
        // ... get
        resistanceNode.addChild(resistanceGetNode);
        // ... add <type>
        resistanceNode.addChild(resistanceAddNode);
        resistanceAddNode.addChild(resistanceAddTypeNode);
        // ... remove <type>
        resistanceNode.addChild(resistanceRemoveNode);
        resistanceRemoveNode.addChild(resistanceRemoveTypeNode);
        // ... clear
        resistanceNode.addChild(resistanceClearNode);

        return node;
    }
}
