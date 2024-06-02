package me.white.simpleitemeditor.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.white.simpleitemeditor.argument.DoubleArgumentType;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AttributeNode implements Node {
    public static final CommandSyntaxException NO_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.noattributes")).create();
    public static final CommandSyntaxException NO_SUCH_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.nosuchattributes")).create();
    private static final String OUTPUT_GET = "commands.edit.attribute.get";
    private static final String OUTPUT_GET_ATTRIBUTE = "commands.edit.attribute.getattribute";
    private static final String OUTPUT_SET = "commands.edit.attribute.add";
    private static final String OUTPUT_REMOVE = "commands.edit.attribute.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.attribute.clear";
    private static final String OUTPUT_ATTRIBUTE = "commands.edit.attribute.attribute";
    private static final String OUTPUT_ATTRIBUTE_SLOT = "commands.edit.attribute.attributeslot";

    private static String operationFormatter(EntityAttributeModifier.Operation operation) {
        return switch (operation) {
            case ADD_VALUE -> "add";
            case ADD_MULTIPLIED_BASE -> "mult_base";
            case ADD_MULTIPLIED_TOTAL -> "mult_total";
        };
    }

    private static Text translate(AttributeModifiersComponent.Entry attribute) {
        Text name = Text.translatable(attribute.attribute().value().getTranslationKey());
        Text value = Text.empty().append(attribute.modifier().value() > 0 ? "+" : "").append(attribute.modifier().operation() == EntityAttributeModifier.Operation.ADD_VALUE ? Text.empty().append(String.valueOf(attribute.modifier().value())) : Text.empty().append(String.valueOf(attribute.modifier().value() * 100)).append("%"));
        return attribute.slot() == AttributeModifierSlot.ANY ? Text.translatable(OUTPUT_ATTRIBUTE, name, value) : Text.translatable(OUTPUT_ATTRIBUTE_SLOT, name, value, attribute.slot().asString());
    }

    private static boolean removeName(List<AttributeModifiersComponent.Entry> attributes, String name) {
        Iterator<AttributeModifiersComponent.Entry> iterator = attributes.iterator();
        boolean wasSuccessful = false;
        while (iterator.hasNext()) {
            AttributeModifiersComponent.Entry attributeEntry = iterator.next();
            if (!attributeEntry.modifier().name().isEmpty()) {
                String attributeName = attributeEntry.modifier().name();
                if (attributeName.equals(name)) {
                    iterator.remove();
                    wasSuccessful = true;
                }
            }
        }
        return wasSuccessful;
    }

    private static boolean hasAttributes(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
            return false;
        }
        return !stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().isEmpty();
    }

    private static List<AttributeModifiersComponent.Entry> getAttributes(ItemStack stack) {
        return stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers();
    }

    private static void setAttributes(ItemStack stack, List<AttributeModifiersComponent.Entry> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            stack.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        } else {
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(attributes, true));
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("attribute").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            List<AttributeModifiersComponent.Entry> attributes = getAttributes(stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
            for (AttributeModifiersComponent.Entry entry : attributes) {
                String name = entry.modifier().name().isEmpty() ? "???" : entry.modifier().name();
                context.getSource().sendFeedback(Text.empty()
                        .append(Text.literal(name).setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                        .append(translate(entry))
                );
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, String> getNameNode = ClientCommandManager.argument("name", StringArgumentType.greedyString()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            String name = StringArgumentType.getString(context, "name");
            List<AttributeModifiersComponent.Entry> attributes = getAttributes(stack);
            List<AttributeModifiersComponent.Entry> matching = new ArrayList<>();
            for (AttributeModifiersComponent.Entry entry : attributes) {
                if (!entry.modifier().name().isEmpty()) {
                    String attributeName = entry.modifier().name();
                    if (attributeName.equals(name)) {
                        matching.add(entry);
                    }
                }
            }
            if (matching.isEmpty()) {
                throw NO_SUCH_ATTRIBUTES_EXCEPTION;
            }
            if (matching.size() == 1) {
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_ATTRIBUTE, translate(matching.get(0))));
            } else {
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                for (AttributeModifiersComponent.Entry entry : matching) {
                    String attributeName = entry.modifier().name().isEmpty() ? "???" : entry.modifier().name();
                    context.getSource().sendFeedback(Text.empty()
                            .append(Text.literal(attributeName).setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                            .append(translate(entry))
                    );
                }
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, String> setNameNode = ClientCommandManager.argument("name", StringArgumentType.string()).build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<EntityAttribute>> setNameAttributeNode = ClientCommandManager.argument("attribute", RegistryArgumentType.registryEntry(RegistryKeys.ATTRIBUTE, registryAccess)).build();

        ArgumentCommandNode<FabricClientCommandSource, Double> setNameAttributeAmountNode = ClientCommandManager.argument("amount", DoubleArgumentType.doubleArg(true, false)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            String name = StringArgumentType.getString(context, "name");
            EntityAttribute attribute = RegistryArgumentType.getRegistryEntry(context, "attribute", RegistryKeys.ATTRIBUTE);
            double amount = DoubleArgumentType.getDouble(context, "amount");
            EntityAttributeModifier modifier = new EntityAttributeModifier(name, amount, EntityAttributeModifier.Operation.ADD_VALUE);
            AttributeModifiersComponent.Entry entry = new AttributeModifiersComponent.Entry(RegistryEntry.of(attribute), modifier, AttributeModifierSlot.ANY);
            List<AttributeModifiersComponent.Entry> attributes = new ArrayList<>(getAttributes(stack));
            removeName(attributes, name);
            attributes.add(entry);
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, translate(entry)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, EntityAttributeModifier.Operation> setNameAttributeAmountOperationNode = ClientCommandManager.argument("operation", EnumArgumentType.enumArgument(EntityAttributeModifier.Operation.class, AttributeNode::operationFormatter)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            String name = StringArgumentType.getString(context, "name");
            EntityAttribute attribute = RegistryArgumentType.getRegistryEntry(context, "attribute", RegistryKeys.ATTRIBUTE);
            double amount = DoubleArgumentType.getDouble(context, "amount");
            EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", EntityAttributeModifier.Operation.class);
            EntityAttributeModifier modifier = new EntityAttributeModifier(name, amount, operation);
            AttributeModifiersComponent.Entry entry = new AttributeModifiersComponent.Entry(RegistryEntry.of(attribute), modifier, AttributeModifierSlot.ANY);
            List<AttributeModifiersComponent.Entry> attributes = new ArrayList<>(getAttributes(stack));
            removeName(attributes, name);
            attributes.add(entry);
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, translate(entry)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, AttributeModifierSlot> setNameAttributeAmountOperationSlotNode = ClientCommandManager.argument("slot", EnumArgumentType.enumArgument(AttributeModifierSlot.class)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            String name = StringArgumentType.getString(context, "name");
            EntityAttribute attribute = RegistryArgumentType.getRegistryEntry(context, "attribute", RegistryKeys.ATTRIBUTE);
            double amount = DoubleArgumentType.getDouble(context, "amount");
            EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", EntityAttributeModifier.Operation.class);
            AttributeModifierSlot slot = EnumArgumentType.getEnum(context, "slot", AttributeModifierSlot.class);
            EntityAttributeModifier modifier = new EntityAttributeModifier(name, amount, operation);
            AttributeModifiersComponent.Entry entry = new AttributeModifiersComponent.Entry(RegistryEntry.of(attribute), modifier, slot);
            List<AttributeModifiersComponent.Entry> attributes = new ArrayList<>(getAttributes(stack));
            removeName(attributes, name);
            attributes.add(entry);
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, translate(entry)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").build();

        ArgumentCommandNode<FabricClientCommandSource, String> removeNameNode = ClientCommandManager.argument("name", StringArgumentType.greedyString()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            String name = StringArgumentType.getString(context, "name");
            List<AttributeModifiersComponent.Entry> attributes = new ArrayList<>(getAttributes(stack));
            if (!removeName(attributes, name)) {
                throw NO_SUCH_ATTRIBUTES_EXCEPTION;
            }
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager.literal("clear").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            setAttributes(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... attribute get [<name>]
        node.addChild(getNode);
        getNode.addChild(getNameNode);

        // ... attribute set <name> <attribute> <amount> [<operation>] [<slot>]
        node.addChild(setNode);
        setNode.addChild(setNameNode);
        setNameNode.addChild(setNameAttributeNode);
        setNameAttributeNode.addChild(setNameAttributeAmountNode);
        setNameAttributeAmountNode.addChild(setNameAttributeAmountOperationNode);
        setNameAttributeAmountOperationNode.addChild(setNameAttributeAmountOperationSlotNode);

        // ... attribute remove <name>
        node.addChild(removeNode);
        removeNode.addChild(removeNameNode);

        // ... attribute clear
        node.addChild(clearNode);
    }
}
