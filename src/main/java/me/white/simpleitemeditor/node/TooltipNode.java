package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
//? if >=1.21.6 {
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.registry.RegistryKeys;

import me.white.simpleitemeditor.argument.RegistryArgumentType;
import java.util.Set;
//?} else {
/*import me.white.simpleitemeditor.argument.EnumArgumentType;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.util.Unit;
//? if >=1.21.2 {
import net.minecraft.item.equipment.trim.ArmorTrim;
//?} else {
/^import net.minecraft.item.trim.ArmorTrim;^/
//?}
*///?}

public class TooltipNode implements Node {
    //? if >=1.21.6 {
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
    //?} else {
    /*private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.tooltip.error.alreadyis")).create();
    private static final CommandSyntaxException NO_COMPONENT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.tooltip.error.nocomponent")).create();
    private static final String OUTPUT_GET_ENABLED = "commands.edit.tooltip.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.tooltip.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.tooltip.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.tooltip.disable";

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("tooltip").build();

        CommandNode<S> getNode = commandManager.literal("get").build();

        CommandNode<S> getPartNode = commandManager.argument("part", EnumArgumentType.enums(TooltipPart.class)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            TooltipPart part = context.getArgument("part", TooltipPart.class);
            boolean tooltip = part.get(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(tooltip ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setPartNode = commandManager.argument("part", EnumArgumentType.enums(TooltipPart.class)).build();

        CommandNode<S> setPartTooltipNode = commandManager.argument("tooltip", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            TooltipPart part = context.getArgument("part", TooltipPart.class);
            boolean tooltip = BoolArgumentType.getBool(context, "tooltip");
            if (part.get(stack) == tooltip) {
                throw ALREADY_IS_EXCEPTION;
            }
            part.set(stack, tooltip);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(tooltip ? OUTPUT_ENABLE : OUTPUT_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get <part>
        node.addChild(getNode);
        getNode.addChild(getPartNode);

        // ... set <part> <tooltip>
        node.addChild(setNode);
        setNode.addChild(setPartNode);
        setPartNode.addChild(setPartTooltipNode);

        return node;
    }

    public enum TooltipPart {
        ALL {
            @Override
            public boolean get(ItemStack stack) {
                return !stack.contains(DataComponentTypes.HIDE_TOOLTIP);
            }

            @Override
            public void set(ItemStack stack, boolean tooltip) throws CommandSyntaxException {
                if (!tooltip) {
                    stack.set(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE);
                } else {
                    stack.remove(DataComponentTypes.HIDE_TOOLTIP);
                }
            }
        },
        ADDITIONAL {
            @Override
            public boolean get(ItemStack stack) {
                return !stack.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
            }

            @Override
            public void set(ItemStack stack, boolean tooltip) throws CommandSyntaxException {
                if (!tooltip) {
                    stack.set(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
                } else {
                    stack.remove(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP);
                }
            }
        },
        ATTRIBUTE {
            @Override
            public boolean get(ItemStack stack) {
                return !stack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS) || stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).showInTooltip();
            }

            @Override
            public void set(ItemStack stack, boolean tooltip) throws CommandSyntaxException {
                AttributeModifiersComponent component = stack.getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT);
                stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, component.withShowInTooltip(tooltip));
            }
        },
        COLOR {
            @Override
            public boolean get(ItemStack stack) {
                return !stack.contains(DataComponentTypes.DYED_COLOR) || stack.get(DataComponentTypes.DYED_COLOR).showInTooltip();
            }

            @Override
            public void set(ItemStack stack, boolean tooltip) throws CommandSyntaxException {
                if (!stack.contains(DataComponentTypes.DYED_COLOR)) {
                    throw NO_COMPONENT_EXCEPTION;
                }
                DyedColorComponent component = stack.get(DataComponentTypes.DYED_COLOR);
                stack.set(DataComponentTypes.DYED_COLOR, component.withShowInTooltip(tooltip));
            }
        },
        ENCHANTMENT {
            @Override
            public boolean get(ItemStack stack) {
                return !stack.contains(DataComponentTypes.ENCHANTMENTS) || stack.get(DataComponentTypes.ENCHANTMENTS).showInTooltip;
            }

            @Override
            public void set(ItemStack stack, boolean tooltip) throws CommandSyntaxException {
                ItemEnchantmentsComponent component = stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
                stack.set(DataComponentTypes.ENCHANTMENTS, component.withShowInTooltip(tooltip));
            }
        },
        TRIM {
            @Override
            public boolean get(ItemStack stack) {
                //? if >=1.21.2 {
                return !stack.contains(DataComponentTypes.TRIM) || stack.get(DataComponentTypes.TRIM).showInTooltip();
                //?} else {
                /^return !stack.contains(DataComponentTypes.TRIM) || stack.get(DataComponentTypes.TRIM).showInTooltip;^/
                //?}
            }

            @Override
            public void set(ItemStack stack, boolean tooltip) throws CommandSyntaxException {
                if (!stack.contains(DataComponentTypes.TRIM)) {
                    throw NO_COMPONENT_EXCEPTION;
                }
                ArmorTrim component = stack.get(DataComponentTypes.TRIM);
                stack.set(DataComponentTypes.TRIM, component.withShowInTooltip(tooltip));
            }
        },
        UNBREAKABLE {
            @Override
            public boolean get(ItemStack stack) {
                return !stack.contains(DataComponentTypes.UNBREAKABLE) || stack.get(DataComponentTypes.UNBREAKABLE).showInTooltip();
            }

            @Override
            public void set(ItemStack stack, boolean tooltip) throws CommandSyntaxException {
                if (!stack.contains(DataComponentTypes.UNBREAKABLE)) {
                    throw NO_COMPONENT_EXCEPTION;
                }
                UnbreakableComponent component = stack.get(DataComponentTypes.UNBREAKABLE);
                stack.set(DataComponentTypes.UNBREAKABLE, component.withShowInTooltip(tooltip));
            }
        };

        public boolean get(ItemStack stack) {
            return true;
        }

        public void set(ItemStack stack, boolean tooltip) throws CommandSyntaxException { }
    }
    *///?}
}