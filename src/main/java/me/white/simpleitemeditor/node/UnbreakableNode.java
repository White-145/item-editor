package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class UnbreakableNode implements Node {
    public static final CommandSyntaxException ISNT_DAMAGABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.unbreakable.error.isntdamagable")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.unbreakable.error.alreadyis")).create();
    public static final CommandSyntaxException NOT_UNBREAKABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.unbreakable.error.notunbreakable")).create();
    public static final CommandSyntaxException TOOLTIP_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.unbreakable.error.tooltipalreadyis")).create();
    private static final String OUTPUT_GET_ENABLED = "commands.edit.unbreakable.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.unbreakable.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.unbreakable.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.unbreakable.disable";
    private static final String OUTPUT_TOOLTIP_GET_ENABLED = "commands.edit.unbreakable.tooltipgetenabled";
    private static final String OUTPUT_TOOLTIP_GET_DISABLED = "commands.edit.unbreakable.tooltipgetdisabled";
    private static final String OUTPUT_TOOLTIP_ENABLE = "commands.edit.unbreakable.tooltipenable";
    private static final String OUTPUT_TOOLTIP_DISABLE = "commands.edit.unbreakable.tooltipdisable";

    private static boolean isDamagable(ItemStack stack) {
        return stack.getMaxDamage() != 0;
    }

    private static boolean isUnbreakable(ItemStack stack) {
        return stack.contains(DataComponentTypes.UNBREAKABLE);
    }

    private static void setUnbreakable(ItemStack stack, boolean isUnbreakable) {
        if (isUnbreakable) {
            stack.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(hasTooltip(stack)));
        } else {
            stack.remove(DataComponentTypes.UNBREAKABLE);
        }
    }

    private static boolean hasTooltip(ItemStack stack) {
        return !stack.contains(DataComponentTypes.UNBREAKABLE) || stack.get(DataComponentTypes.UNBREAKABLE).showInTooltip();
    }

    private static void setTooltip(ItemStack stack, boolean showTooltip) {
        if (!isUnbreakable(stack)) {
            return;
        }
        UnbreakableComponent component = stack.get(DataComponentTypes.UNBREAKABLE);
        stack.set(DataComponentTypes.UNBREAKABLE, component.withShowInTooltip(showTooltip));
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("unbreakable").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGABLE_EXCEPTION;
            }
            boolean isUnbreakable = isUnbreakable(stack);

            context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> setUnbreakableNode = ClientCommandManager.argument("unbreakable", BoolArgumentType.bool()).executes(context -> {
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
            boolean isUnbreakable = BoolArgumentType.getBool(context, "unbreakable");
            if (isUnbreakable == isUnbreakable(stack)) {
                throw ALREADY_IS_EXCEPTION;
            }
            setUnbreakable(stack, isUnbreakable);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_ENABLE : OUTPUT_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> tooltipNode = ClientCommandManager.literal("tooltip").build();

        LiteralCommandNode<FabricClientCommandSource> tooltipGetNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isUnbreakable(stack)) {
                throw NOT_UNBREAKABLE_EXCEPTION;
            }
            boolean showTooltip = hasTooltip(stack);

            context.getSource().sendFeedback(Text.translatable(showTooltip ? OUTPUT_TOOLTIP_GET_ENABLED : OUTPUT_TOOLTIP_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> tooltipSetNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> tooltipSetShowNode = ClientCommandManager.argument("show", BoolArgumentType.bool()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isUnbreakable(stack)) {
                throw NOT_UNBREAKABLE_EXCEPTION;
            }
            boolean showTooltip = BoolArgumentType.getBool(context, "show");
            if (showTooltip == hasTooltip(stack)) {
                throw TOOLTIP_ALREADY_IS_EXCEPTION;
            }
            setTooltip(stack, showTooltip);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(showTooltip ? OUTPUT_TOOLTIP_ENABLE : OUTPUT_TOOLTIP_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set <unbreakable>
        node.addChild(setNode);
        setNode.addChild(setUnbreakableNode);

        // ... tooltip ...
        node.addChild(tooltipNode);
        // ... get
        tooltipNode.addChild(tooltipGetNode);
        // ... set <show>
        tooltipNode.addChild(tooltipSetNode);
        tooltipSetNode.addChild(tooltipSetShowNode);
    }
}
