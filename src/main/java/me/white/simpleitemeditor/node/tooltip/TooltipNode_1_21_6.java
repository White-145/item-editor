package me.white.simpleitemeditor.node.tooltip;

//? if >=1.21.6 {
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.Set;

public class TooltipNode_1_21_6 implements Node {
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.tooltip.error.alreadyis")).create();
    private static final CommandSyntaxException NO_COMPONENT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.tooltip.error.nocomponent")).create();
    private static final String OUTPUT_GET_ENABLED = "commands.edit.tooltip.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.tooltip.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.tooltip.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.tooltip.disable";
    private static final String OUTPUT_ALL_GET_ENABLED = "commands.edit.tooltip.allgetenabled";
    private static final String OUTPUT_ALL_GET_DISABLED = "commands.edit.tooltip.allgetdisabled";
    private static final String OUTPUT_ALL_ENABLE = "commands.edit.tooltip.allenable";
    private static final String OUTPUT_ALL_DISABLE = "commands.edit.tooltip.alldisable";

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("tooltip").build();

        CommandNode<S> getNode = commandManager.literal("get").build();

        CommandNode<S> getComponentNode = commandManager.argument("component", RegistryArgumentType.registryEntry(RegistryKeys.DATA_COMPONENT_TYPE, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            ComponentType<?> component = RegistryArgumentType.getRegistryEntry(context, "component", RegistryKeys.DATA_COMPONENT_TYPE);
            Set<ComponentType<?>> set = stack.get(DataComponentTypes.TOOLTIP_DISPLAY).hiddenComponents();
            boolean shown = !set.contains(component);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(shown ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setComponent = commandManager.argument("component", RegistryArgumentType.registryEntry(RegistryKeys.DATA_COMPONENT_TYPE, registryAccess)).build();

        CommandNode<S> setComponentShown = commandManager.argument("shown", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            ComponentType<?> component = RegistryArgumentType.getRegistryEntry(context, "component", RegistryKeys.DATA_COMPONENT_TYPE);
            TooltipDisplayComponent display = stack.get(DataComponentTypes.TOOLTIP_DISPLAY);
            boolean is = !display.hiddenComponents().contains(component);

            boolean shown = BoolArgumentType.getBool(context, "shown");
            if (is == shown) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.set(DataComponentTypes.TOOLTIP_DISPLAY, display.with(component, !shown));

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(shown ? OUTPUT_ENABLE : OUTPUT_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> allNode = commandManager.literal("all").build();

        CommandNode<S> allGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            boolean shown = !stack.get(DataComponentTypes.TOOLTIP_DISPLAY).hideTooltip();

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(shown ? OUTPUT_ALL_GET_ENABLED : OUTPUT_ALL_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> allSetNode = commandManager.literal("set").build();

        CommandNode<S> allSetShownNode = commandManager.argument("shown", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            TooltipDisplayComponent component = stack.get(DataComponentTypes.TOOLTIP_DISPLAY);

            boolean shown = BoolArgumentType.getBool(context, "shown");
            if (!component.hideTooltip() == shown) {
                throw ALREADY_IS_EXCEPTION;
            }
            stack.set(DataComponentTypes.TOOLTIP_DISPLAY, new TooltipDisplayComponent(!shown, component.hiddenComponents()));

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(shown ? OUTPUT_ALL_ENABLE : OUTPUT_ALL_DISABLE));
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
//?}