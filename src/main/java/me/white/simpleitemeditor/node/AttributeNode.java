package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.argument.AlternativeArgumentType;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.argument.IdentifierArgumentType;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.*;

public class AttributeNode implements Node {
    private static final CommandSyntaxException NO_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.noattributes")).create();
    private static final CommandSyntaxException NO_SUCH_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.nosuchattributes")).create();
    private static final String OUTPUT_GET = "commands.edit.attribute.get";
    private static final String OUTPUT_GET_ATTRIBUTE = "commands.edit.attribute.getattribute";
    private static final String OUTPUT_SET = "commands.edit.attribute.set";
    private static final String OUTPUT_REMOVE = "commands.edit.attribute.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.attribute.clear";
    private static final String OUTPUT_ATTRIBUTE = "commands.edit.attribute.attribute";
    private static final String OUTPUT_ATTRIBUTE_SLOT = "commands.edit.attribute.attributeslot";
    private static final Map<String, Double> VALUE_CONSTS = new HashMap<>();

    static {
        VALUE_CONSTS.put("infinity", Double.POSITIVE_INFINITY);
        VALUE_CONSTS.put("-infinity", Double.NEGATIVE_INFINITY);
    }

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

    private static boolean removeId(List<AttributeModifiersComponent.Entry> attributes, Identifier id) {
        Iterator<AttributeModifiersComponent.Entry> iterator = attributes.iterator();
        boolean wasSuccessful = false;
        while (iterator.hasNext()) {
            AttributeModifiersComponent.Entry attributeEntry = iterator.next();
            Identifier attributeId = attributeEntry.modifier().id();
            if (attributeId.equals(id)) {
                iterator.remove();
                wasSuccessful = true;
            }
        }
        return wasSuccessful;
    }

    private static RegistryEntry<EntityAttribute> entryOf(DynamicRegistryManager registryManager, EntityAttribute attribute) {
        return registryManager.get(RegistryKeys.ATTRIBUTE).getEntry(attribute);
    }

    private static boolean hasAttributes(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
            return false;
        }
        return !stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().isEmpty();
    }

    private static List<AttributeModifiersComponent.Entry> getAttributes(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.ATTRIBUTE_MODIFIERS)) {
            return List.of();
        }
        return stack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers();
    }

    private static void setAttributes(ItemStack stack, List<AttributeModifiersComponent.Entry> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            stack.remove(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        } else {
            stack.set(DataComponentTypes.ATTRIBUTE_MODIFIERS, new AttributeModifiersComponent(attributes, TooltipNode.TooltipPart.ATTRIBUTE.get(stack)));
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("attribute").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            EditorUtil.checkHasItem(stack);
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            List<AttributeModifiersComponent.Entry> attributes = getAttributes(stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
            for (AttributeModifiersComponent.Entry entry : attributes) {
                Identifier id = entry.modifier().id();
                context.getSource().sendFeedback(Text.empty()
                        .append(Text.literal(id.toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                        .append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                        .append(translate(entry))
                );
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Identifier> getIdNode = ClientCommandManager.argument("id", IdentifierArgumentType.identifier()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            EditorUtil.checkHasItem(stack);
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            List<AttributeModifiersComponent.Entry> attributes = getAttributes(stack);
            List<AttributeModifiersComponent.Entry> matching = new ArrayList<>();
            for (AttributeModifiersComponent.Entry entry : attributes) {
                Identifier attributeId = entry.modifier().id();
                if (attributeId.equals(id)) {
                    matching.add(entry);
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
                    Identifier attributeId = entry.modifier().id();
                    context.getSource().sendFeedback(Text.empty()
                            .append(Text.literal(attributeId.toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                            .append(translate(entry))
                    );
                }
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Identifier> setIdNode = ClientCommandManager.argument("id", IdentifierArgumentType.identifier()).build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<EntityAttribute>> setIdAttributeNode = ClientCommandManager.argument("attribute", RegistryArgumentType.registryEntry(RegistryKeys.ATTRIBUTE, registryAccess)).build();

        ArgumentCommandNode<FabricClientCommandSource, Double> setIdentifierAttributeAmountNode = ClientCommandManager.argument("amount", AlternativeArgumentType.argument(DoubleArgumentType.doubleArg(), VALUE_CONSTS)).executes(context -> {
            EditorUtil.checkHasCreative(context.getSource());
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            EntityAttribute attribute = RegistryArgumentType.getRegistryEntry(context, "attribute", RegistryKeys.ATTRIBUTE);
            double amount = DoubleArgumentType.getDouble(context, "amount");
            EntityAttributeModifier modifier = new EntityAttributeModifier(id, amount, EntityAttributeModifier.Operation.ADD_VALUE);
            AttributeModifiersComponent.Entry entry = new AttributeModifiersComponent.Entry(entryOf(context.getSource().getRegistryManager(), attribute), modifier, AttributeModifierSlot.ANY);
            List<AttributeModifiersComponent.Entry> attributes = new ArrayList<>(getAttributes(stack));
            removeId(attributes, id);
            attributes.add(entry);
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, translate(entry)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, EntityAttributeModifier.Operation> setIdAttributeAmountOperationNode = ClientCommandManager.argument("operation", EnumArgumentType.enumArgument(EntityAttributeModifier.Operation.class, AttributeNode::operationFormatter)).executes(context -> {
            EditorUtil.checkHasCreative(context.getSource());
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            EntityAttribute attribute = RegistryArgumentType.getRegistryEntry(context, "attribute", RegistryKeys.ATTRIBUTE);
            double amount = DoubleArgumentType.getDouble(context, "amount");
            EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", EntityAttributeModifier.Operation.class);
            EntityAttributeModifier modifier = new EntityAttributeModifier(id, amount, operation);
            AttributeModifiersComponent.Entry entry = new AttributeModifiersComponent.Entry(entryOf(context.getSource().getRegistryManager(), attribute), modifier, AttributeModifierSlot.ANY);
            List<AttributeModifiersComponent.Entry> attributes = new ArrayList<>(getAttributes(stack));
            removeId(attributes, id);
            attributes.add(entry);
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, translate(entry)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, AttributeModifierSlot> setNameAttributeAmountOperationSlotNode = ClientCommandManager.argument("slot", EnumArgumentType.enumArgument(AttributeModifierSlot.class)).executes(context -> {
            EditorUtil.checkHasCreative(context.getSource());
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            EntityAttribute attribute = RegistryArgumentType.getRegistryEntry(context, "attribute", RegistryKeys.ATTRIBUTE);
            double amount = DoubleArgumentType.getDouble(context, "amount");
            EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", EntityAttributeModifier.Operation.class);
            AttributeModifierSlot slot = EnumArgumentType.getEnum(context, "slot", AttributeModifierSlot.class);
            EntityAttributeModifier modifier = new EntityAttributeModifier(id, amount, operation);
            AttributeModifiersComponent.Entry entry = new AttributeModifiersComponent.Entry(entryOf(context.getSource().getRegistryManager(), attribute), modifier, slot);
            List<AttributeModifiersComponent.Entry> attributes = new ArrayList<>(getAttributes(stack));
            removeId(attributes, id);
            attributes.add(entry);
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, translate(entry)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").build();

        ArgumentCommandNode<FabricClientCommandSource, Identifier> removeIdNode = ClientCommandManager.argument("id", IdentifierArgumentType.identifier()).executes(context -> {
            EditorUtil.checkHasCreative(context.getSource());
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            List<AttributeModifiersComponent.Entry> attributes = new ArrayList<>(getAttributes(stack));
            if (!removeId(attributes, id)) {
                throw NO_SUCH_ATTRIBUTES_EXCEPTION;
            }
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager.literal("clear").executes(context -> {
            EditorUtil.checkHasCreative(context.getSource());
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            setAttributes(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get [<name>]
        node.addChild(getNode);
        getNode.addChild(getIdNode);

        // ... set <name> <attribute> <amount> [<operation>] [<slot>]
        node.addChild(setNode);
        setNode.addChild(setIdNode);
        setIdNode.addChild(setIdAttributeNode);
        setIdAttributeNode.addChild(setIdentifierAttributeAmountNode);
        setIdentifierAttributeAmountNode.addChild(setIdAttributeAmountOperationNode);
        setIdAttributeAmountOperationNode.addChild(setNameAttributeAmountOperationSlotNode);

        // ... remove <name>
        node.addChild(removeNode);
        removeNode.addChild(removeIdNode);

        // ... clear
        node.addChild(clearNode);
    }
}
