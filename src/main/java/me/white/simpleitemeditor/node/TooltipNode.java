package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;

public class TooltipNode implements Node {
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.tooltip.error.alreadyis")).create();
    private static final CommandSyntaxException NO_COMPONENT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.tooltip.error.nocomponent")).create();
    private static final String OUTPUT_GET_ENABLED = "commands.edit.tooltip.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.tooltip.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.tooltip.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.tooltip.disable";

    @Override
    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("tooltip").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").build();

        ArgumentCommandNode<FabricClientCommandSource, TooltipPart> getPartNode = ClientCommandManager.argument("part", EnumArgumentType.enumArgument(TooltipPart.class)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            EditorUtil.checkHasItem(stack);
            TooltipPart part = EnumArgumentType.getEnum(context, "part", TooltipPart.class);
            boolean tooltip = part.get(stack);

            context.getSource().sendFeedback(Text.translatable(tooltip ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, TooltipPart> setPartNode = ClientCommandManager.argument("part", EnumArgumentType.enumArgument(TooltipPart.class)).build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> setPartTooltipNode = ClientCommandManager.argument("tooltip", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkHasCreative(context.getSource());
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            TooltipPart part = EnumArgumentType.getEnum(context, "part", TooltipPart.class);
            boolean tooltip = BoolArgumentType.getBool(context, "tooltip");
            if (part.get(stack) == tooltip) {
                throw ALREADY_IS_EXCEPTION;
            }
            System.out.println(part.name());
            part.set(stack, tooltip);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(tooltip ? OUTPUT_ENABLE : OUTPUT_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get <part>
        node.addChild(getNode);
        getNode.addChild(getPartNode);

        // ... set <part> <tooltip>
        node.addChild(setNode);
        setNode.addChild(setPartNode);
        setPartNode.addChild(setPartTooltipNode);
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
                return !stack.contains(DataComponentTypes.TRIM) || stack.get(DataComponentTypes.TRIM).showInTooltip;
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
}
