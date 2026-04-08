package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.util.EditorUtil;

import me.white.sie.argument.RegistryArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.Set;

public class TooltipNode implements Node {
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.tooltip.error.alreadyis")).create();
    private static final CommandSyntaxException NO_COMPONENT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.tooltip.error.nocomponent")).create();
    private static final String OUTPUT_GET_ENABLED = "commands.edit.tooltip.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.tooltip.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.tooltip.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.tooltip.disable";
    private static final String OUTPUT_ALL_GET_ENABLED = "commands.edit.tooltip.allgetenabled";
    private static final String OUTPUT_ALL_GET_DISABLED = "commands.edit.tooltip.allgetdisabled";
    private static final String OUTPUT_ALL_ENABLE = "commands.edit.tooltip.allenable";
    private static final String OUTPUT_ALL_DISABLE = "commands.edit.tooltip.alldisable";

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("tooltip").build();

        CommandNode<S> getNode = commandManager.literal("get").build();

        CommandNode<S> getComponentNode = commandManager.argument("component", RegistryArgumentType.registryEntry(Registries.DATA_COMPONENT_TYPE, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            DataComponentType<?> component = RegistryArgumentType.getRegistryEntry(context, "component", Registries.DATA_COMPONENT_TYPE);
            Set<DataComponentType<?>> set = stack.get(DataComponents.TOOLTIP_DISPLAY).hiddenComponents();
            boolean shown = !set.contains(component);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(shown ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setComponent = commandManager.argument("component", RegistryArgumentType.registryEntry(Registries.DATA_COMPONENT_TYPE, registryAccess)).build();

        CommandNode<S> setComponentShown = commandManager.argument("shown", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataComponentType<?> component = RegistryArgumentType.getRegistryEntry(context, "component", Registries.DATA_COMPONENT_TYPE);
            TooltipDisplay display = TooltipDisplay.DEFAULT;
            if (stack.has(DataComponents.TOOLTIP_DISPLAY)) {
                display = stack.get(DataComponents.TOOLTIP_DISPLAY);
            }
            boolean is = !display.hiddenComponents().contains(component);

            boolean shown = BoolArgumentType.getBool(context, "shown");
            if (is == shown) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.set(DataComponents.TOOLTIP_DISPLAY, display.withHidden(component, !shown));

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(shown ? OUTPUT_ENABLE : OUTPUT_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> allNode = commandManager.literal("all").build();

        CommandNode<S> allGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            TooltipDisplay display = TooltipDisplay.DEFAULT;
            if (stack.has(DataComponents.TOOLTIP_DISPLAY)) {
                display = stack.get(DataComponents.TOOLTIP_DISPLAY);
            }
            boolean shown = !display.hideTooltip();

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(shown ? OUTPUT_ALL_GET_ENABLED : OUTPUT_ALL_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> allSetNode = commandManager.literal("set").build();

        CommandNode<S> allSetShownNode = commandManager.argument("shown", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            TooltipDisplay display = TooltipDisplay.DEFAULT;
            if (stack.has(DataComponents.TOOLTIP_DISPLAY)) {
                display = stack.get(DataComponents.TOOLTIP_DISPLAY);
            }

            boolean shown = BoolArgumentType.getBool(context, "shown");
            if (!display.hideTooltip() == shown) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(!shown, display.hiddenComponents()));

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(shown ? OUTPUT_ALL_ENABLE : OUTPUT_ALL_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get <component>
        node.addChild(getNode);
        getNode.addChild(getComponentNode);

        // ... set <component> <shown>
        node.addChild(setNode);
        setNode.addChild(setComponent);
        setComponent.addChild(setComponentShown);

        // ... all ...
        node.addChild(allNode);
        // ... get
        allNode.addChild(allGetNode);
        // ... all set <shown>
        allNode.addChild(allSetNode);
        allSetNode.addChild(allSetShownNode);

        return node;
    }
}