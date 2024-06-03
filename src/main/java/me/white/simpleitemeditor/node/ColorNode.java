package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.argument.ColorArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

public class ColorNode implements Node {
    public static final CommandSyntaxException ISNT_COLORABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.isntcolorable")).create();
    public static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.nocolor")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.alreadyis")).create();
    public static final CommandSyntaxException TOOLTIP_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.tooltipalreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.color.get";
    private static final String OUTPUT_SET = "commands.edit.color.set";
    private static final String OUTPUT_RESET = "commands.edit.color.reset";
    private static final String OUTPUT_TOOLTIP_GET_ENABLED = "commands.edit.color.tooltipgetenabled";
    private static final String OUTPUT_TOOLTIP_GET_DISABLED = "commands.edit.color.tooltipgetdisabled";
    private static final String OUTPUT_TOOLTIP_ENABLE = "commands.edit.color.tooltipenable";
    private static final String OUTPUT_TOOLTIP_DISABLE = "commands.edit.color.tooltipdisable";

    private static boolean isColorable(ItemStack stack) {
        return stack.isIn(ItemTags.DYEABLE);
    }

    private static boolean hasColor(ItemStack stack) {
        return stack.contains(DataComponentTypes.DYED_COLOR);
    }

    private static int getColor(ItemStack stack) {
        if (!hasColor(stack)) {
            return -1;
        }
        return stack.get(DataComponentTypes.DYED_COLOR).rgb();
    }

    private static void resetColor(ItemStack stack) {
        stack.remove(DataComponentTypes.DYED_COLOR);
    }

    private static void setColor(ItemStack stack, int color) {
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, hasTooltip(stack)));
    }

    private static boolean hasTooltip(ItemStack stack) {
        return !stack.contains(DataComponentTypes.DYED_COLOR) || stack.get(DataComponentTypes.DYED_COLOR).showInTooltip();
    }

    private static void setTooltip(ItemStack stack, boolean showTooltip) {
        if (!hasColor(stack)) {
            return;
        }
        DyedColorComponent component = stack.get(DataComponentTypes.DYED_COLOR);
        stack.set(DataComponentTypes.DYED_COLOR, component.withShowInTooltip(showTooltip));
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("color").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isColorable(stack)) {
                throw ISNT_COLORABLE_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            int color = getColor(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setColorNode = ClientCommandManager.argument("color", ColorArgumentType.color()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!isColorable(stack)) {
                throw ISNT_COLORABLE_EXCEPTION;
            }
            int color = ColorArgumentType.getColor(context, "color");
            if (color == getColor(stack)) {
                throw ALREADY_IS_EXCEPTION;
            }
            setColor(stack, color);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager.literal("reset").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!isColorable(stack)) {
                throw ISNT_COLORABLE_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            resetColor(stack);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> tooltipNode = ClientCommandManager.literal("tooltip").build();

        LiteralCommandNode<FabricClientCommandSource> tooltipGetNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
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
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
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

        // ... set <color>
        node.addChild(setNode);
        setNode.addChild(setColorNode);

        // ... reset
        node.addChild(resetNode);

        // ... tooltip ...
        node.addChild(tooltipNode);
        // ... get
        tooltipNode.addChild(tooltipGetNode);
        // ... set <show>
        tooltipNode.addChild(tooltipSetNode);
        tooltipSetNode.addChild(tooltipSetShowNode);
    }
}
