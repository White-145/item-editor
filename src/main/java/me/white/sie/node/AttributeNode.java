package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.argument.EnumArgumentType;
import me.white.sie.argument.IdentifierArgumentType;
import me.white.sie.argument.InfiniteDoubleArgumentType;
import me.white.sie.argument.RegistryArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.util.EditorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AttributeNode implements Node {
    private static final CommandSyntaxException NO_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.attribute.error.noattributes")).create();
    private static final CommandSyntaxException NO_SUCH_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.attribute.error.nosuchattributes")).create();
    private static final String OUTPUT_GET = "commands.edit.attribute.get";
    private static final String OUTPUT_GET_ATTRIBUTE = "commands.edit.attribute.getattribute";
    private static final String OUTPUT_SET = "commands.edit.attribute.set";
    private static final String OUTPUT_REMOVE = "commands.edit.attribute.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.attribute.clear";
    private static final String OUTPUT_ATTRIBUTE = "commands.edit.attribute.attribute";
    private static final String OUTPUT_ATTRIBUTE_SLOT = "commands.edit.attribute.attributeslot";

    private static Component translate(ItemAttributeModifiers.Entry attribute) {
        Component name = Component.translatable(attribute.attribute().value().getDescriptionId());
        Component value = Component.empty().append(attribute.modifier().amount() > 0 ? "+" : "").append(attribute.modifier().operation() == AttributeModifier.Operation.ADD_VALUE ? Component.empty().append(String.valueOf(attribute.modifier().amount())) : Component.empty().append(String.valueOf(attribute.modifier().amount() * 100)).append("%"));
        return attribute.slot() == EquipmentSlotGroup.ANY ? Component.translatable(OUTPUT_ATTRIBUTE, name, value) : Component.translatable(OUTPUT_ATTRIBUTE_SLOT, name, value, attribute.slot().name());
    }

    private static Identifier getId(ItemAttributeModifiers.Entry entry) {
        return entry.modifier().id();
    }

    private static ArgumentType<?> getIdArgumentType() {
        return IdentifierArgumentType.identifier();
    }

    private static boolean removeId(List<ItemAttributeModifiers.Entry> attributes, Identifier id) {
        Iterator<ItemAttributeModifiers.Entry> iterator = attributes.iterator();
        boolean wasSuccessful = false;
        while (iterator.hasNext()) {
            ItemAttributeModifiers.Entry attributeEntry = iterator.next();
            Identifier attributeId = getId(attributeEntry);
            if (attributeId.equals(id)) {
                iterator.remove();
                wasSuccessful = true;
            }
        }
        return wasSuccessful;
    }

    private static Holder<Attribute> entryOf(Attribute attribute) {
        return EditorUtil.getRegistry(Registries.ATTRIBUTE).wrapAsHolder(attribute);
    }

    private static boolean hasAttributes(ItemStack stack) {
        if (!stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
            return false;
        }
        return !stack.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers().isEmpty();
    }

    private static List<ItemAttributeModifiers.Entry> getAttributes(ItemStack stack) {
        if (!stack.has(DataComponents.ATTRIBUTE_MODIFIERS)) {
            return List.of();
        }
        return stack.get(DataComponents.ATTRIBUTE_MODIFIERS).modifiers();
    }

    private static void setAttributes(ItemStack stack, List<ItemAttributeModifiers.Entry> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            stack.remove(DataComponents.ATTRIBUTE_MODIFIERS);
        } else {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(attributes));
        }
    }
    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("attribute").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            List<ItemAttributeModifiers.Entry> attributes = getAttributes(stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET));
            for (ItemAttributeModifiers.Entry entry : attributes) {
                Identifier id = getId(entry);
                EditorUtil.sendFeedback(context.getSource(), Component.empty()
                        .append(Component.literal(id.toString()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))
                        .append(Component.literal(": ").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)))
                        .append(translate(entry))
                );
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> getIdNode = commandManager.argument("id", IdentifierArgumentType.identifier()).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            List<ItemAttributeModifiers.Entry> attributes = getAttributes(stack);
            List<ItemAttributeModifiers.Entry> matching = new ArrayList<>();
            for (ItemAttributeModifiers.Entry entry : attributes) {
                Identifier attributeId = getId(entry);
                if (attributeId.equals(id)) {
                    matching.add(entry);
                }
            }
            if (matching.isEmpty()) {
                throw NO_SUCH_ATTRIBUTES_EXCEPTION;
            }
            if (matching.size() == 1) {
                ItemAttributeModifiers.Entry entry = matching.getFirst();
                EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_ATTRIBUTE, translate(entry)));
            } else {
                EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET));
                for (ItemAttributeModifiers.Entry entry : matching) {
                    Identifier attributeId = getId(entry);
                    EditorUtil.sendFeedback(context.getSource(), Component.empty()
                            .append(Component.literal(attributeId.toString()).setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))
                            .append(Component.literal(": ").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)))
                            .append(translate(entry))
                    );
                }
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setIdNode = commandManager.argument("id", getIdArgumentType()).build();

        CommandNode<S> setIdAttributeNode = commandManager.argument("attribute", RegistryArgumentType.registryEntry(Registries.ATTRIBUTE, registryAccess)).build();

        CommandNode<S> setIdentifierAttributeAmountNode = commandManager.argument("amount", InfiniteDoubleArgumentType.infiniteDouble()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Attribute attribute = RegistryArgumentType.getRegistryEntry(context, "attribute", Registries.ATTRIBUTE);
            double amount = InfiniteDoubleArgumentType.getInfiniteDouble(context, "amount");
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            AttributeModifier modifier = new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE);
            ItemAttributeModifiers.Entry entry = new ItemAttributeModifiers.Entry(entryOf(attribute), modifier, EquipmentSlotGroup.ANY);
            List<ItemAttributeModifiers.Entry> attributes = new ArrayList<>(getAttributes(stack));
            removeId(attributes, id);
            attributes.add(entry);
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, translate(entry)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setIdAttributeAmountOperationNode = commandManager.argument("operation", EnumArgumentType.enums(AttributeModifier.Operation.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Attribute attribute = RegistryArgumentType.getRegistryEntry(context, "attribute", Registries.ATTRIBUTE);
            double amount = InfiniteDoubleArgumentType.getInfiniteDouble(context, "amount");
            AttributeModifier.Operation operation = context.getArgument("operation", AttributeModifier.Operation.class);
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            AttributeModifier modifier = new AttributeModifier(id, amount, operation);
            ItemAttributeModifiers.Entry entry = new ItemAttributeModifiers.Entry(entryOf(attribute), modifier, EquipmentSlotGroup.ANY);
            List<ItemAttributeModifiers.Entry> attributes = new ArrayList<>(getAttributes(stack));
            removeId(attributes, id);
            attributes.add(entry);
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, translate(entry)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNameAttributeAmountOperationSlotNode = commandManager.argument("slot", EnumArgumentType.enums(EquipmentSlotGroup.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Attribute attribute = RegistryArgumentType.getRegistryEntry(context, "attribute", Registries.ATTRIBUTE);
            double amount = InfiniteDoubleArgumentType.getInfiniteDouble(context, "amount");
            AttributeModifier.Operation operation = context.getArgument("operation", AttributeModifier.Operation.class);
            EquipmentSlotGroup slot = context.getArgument("slot", EquipmentSlotGroup.class);
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            AttributeModifier modifier = new AttributeModifier(id, amount, operation);
            ItemAttributeModifiers.Entry entry = new ItemAttributeModifiers.Entry(entryOf(attribute), modifier, slot);
            List<ItemAttributeModifiers.Entry> attributes = new ArrayList<>(getAttributes(stack));
            removeId(attributes, id);
            attributes.add(entry);
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, translate(entry)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").build();

        CommandNode<S> removeIdNode = commandManager.argument("id", getIdArgumentType()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
            List<ItemAttributeModifiers.Entry> attributes = new ArrayList<>(getAttributes(stack));
            if (!removeId(attributes, id)) {
                throw NO_SUCH_ATTRIBUTES_EXCEPTION;
            }
            setAttributes(stack, attributes);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasAttributes(stack)) {
                throw NO_ATTRIBUTES_EXCEPTION;
            }
            setAttributes(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

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

        return node;
    }
}
