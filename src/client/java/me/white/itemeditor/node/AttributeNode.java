package me.white.itemeditor.node;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.EnumArgumentType;
import me.white.itemeditor.util.ItemUtil;
import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AttributeNode {
	public static final CommandSyntaxException NO_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.noattributes")).create();
	public static final CommandSyntaxException NO_SUCH_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.nosuchattributes")).create();
	private static final String OUTPUT_GET = "commands.edit.attribute.get";
	private static final String OUTPUT_SET = "commands.edit.attribute.add";
	private static final String OUTPUT_SET_PLACEHOLDER_ENABLE = "commands.edit.attribute.placeholderenable";
	private static final String OUTPUT_SET_PLACEHOLDER_DISABLE = "commands.edit.attribute.placeholderdisable";
	private static final String OUTPUT_REMOVE = "commands.edit.attribute.remove";
	private static final String OUTPUT_CLEAR = "commands.edit.attribute.clear";
	private static final String OUTPUT_ATTRIBUTE = "commands.edit.attribute.attribute";
	private static final String OUTPUT_ATTRIBUTE_SLOT = "commands.edit.attribute.attributeslot";

	private static String operationFormatter(EntityAttributeModifier.Operation operation) {
		return switch (operation) {
			case ADDITION -> "add";
			case MULTIPLY_BASE -> "base";
			case MULTIPLY_TOTAL -> "total";
		};
	}

	private static Text translate(Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot> attribute) {
		Text name = Text.translatable(attribute.getLeft().getTranslationKey());
		Text value = Text.empty()
				.append(attribute.getMiddle().getValue() > 0 ? "+" : "")
				.append(attribute.getMiddle().getOperation() == EntityAttributeModifier.Operation.ADDITION
						? Text.empty()
						.append(String.valueOf(attribute.getMiddle().getValue()))
						: Text.empty()
						.append(String.valueOf(attribute.getMiddle().getValue() * 100))
						.append("%")
				);
		return attribute.getRight() == null
				? Text.translatable(OUTPUT_ATTRIBUTE, name, value)
				: Text.translatable(OUTPUT_ATTRIBUTE_SLOT, name, value, attribute.getRight().getName());
	}

	private static int get(FabricClientCommandSource source, EntityAttribute attribute, EquipmentSlot slot) throws CommandSyntaxException {
		ItemStack stack = EditorUtil.getStack(source);
		if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
		if (!ItemUtil.hasAttributes(stack, true)) throw NO_ATTRIBUTES_EXCEPTION;
		List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> attributes = new ArrayList<>();
		for (Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot> dirtyAttribute : ItemUtil.getAttributes(stack)) {
			if ((attribute == null || attribute.equals(dirtyAttribute.getLeft())) && (slot == null || slot.equals(dirtyAttribute.getRight()))) {
				attributes.add(dirtyAttribute);
			}
		}
		if (attributes.isEmpty()) throw NO_SUCH_ATTRIBUTES_EXCEPTION;

		source.sendFeedback(Text.translatable(OUTPUT_GET));
		for (Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot> triple : attributes) {
			source.sendFeedback(Text.empty()
					.append(Text.literal("- ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
					.append(translate(triple))
			);
		}
		return attributes.size();
	}

	private static int remove(FabricClientCommandSource source, EntityAttribute attribute, EquipmentSlot slot) throws CommandSyntaxException {
		ItemStack stack = EditorUtil.getStack(source).copy();
		if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
		if (!EditorUtil.hasCreative(source)) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
		if (!ItemUtil.hasAttributes(stack, true)) throw NO_ATTRIBUTES_EXCEPTION;
		List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> attributes = new ArrayList<>();
		List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> dirtyAttributes = ItemUtil.getAttributes(stack);
		for (Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot> dirtyAttribute : dirtyAttributes) {
			if ((attribute != null && !attribute.equals(dirtyAttribute.getLeft())) || (slot != null && !slot.equals(dirtyAttribute.getRight()))) {
				attributes.add(dirtyAttribute);
			}
		}
		if (attributes.equals(dirtyAttributes)) throw NO_SUCH_ATTRIBUTES_EXCEPTION;
		int dif = dirtyAttributes.size() - attributes.size();
		ItemUtil.setAttributes(stack, attributes);

		EditorUtil.setStack(source, stack);
		source.sendFeedback(Text.translatable(OUTPUT_REMOVE, dif));
		return dif;
	}

	private static int set(FabricClientCommandSource source, EntityAttribute attribute, EntityAttributeModifier modifier, EquipmentSlot slot) throws CommandSyntaxException {
		ItemStack stack = EditorUtil.getStack(source).copy();
		if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
		if (!EditorUtil.hasCreative(source)) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
		if (attribute == null) {
			boolean hasPlaceholder = ItemUtil.hasAttributes(stack, false);
			ItemUtil.setAttributePlaceholder(stack, !hasPlaceholder);

			source.sendFeedback(Text.translatable(hasPlaceholder ? OUTPUT_SET_PLACEHOLDER_DISABLE : OUTPUT_SET_PLACEHOLDER_ENABLE));
		} else {
			List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> attributes = new ArrayList<>(ItemUtil.getAttributes(stack));
			Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot> triple = Triple.of(attribute, modifier, slot);
			attributes.add(triple);
			ItemUtil.setAttributes(stack, attributes);

			source.sendFeedback(Text.translatable(OUTPUT_SET, translate(triple)));
		}
		EditorUtil.setStack(source, stack);
		return 1;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
				.literal("attribute")
				.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
				.literal("get")
				.executes(context -> get(context.getSource(), null, null))
				.build();

		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> getSlotNode = ClientCommandManager
				.argument("slot", EnumArgumentType.enumArgument(EquipmentSlot.class))
				.executes(context -> {
					EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);
					return get(context.getSource(), null, slot);
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> getSlotAttributeNode = ClientCommandManager
				.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
				.executes(context -> {
					EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);
					EntityAttribute attribute = EditorUtil.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
					return get(context.getSource(), attribute, slot);
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
				.literal("remove")
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> removeAttributeNode = ClientCommandManager
				.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
				.executes(context -> {
					EntityAttribute attribute = EditorUtil.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
					return remove(context.getSource(), attribute, null);
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> removeAttributeSlotNode = ClientCommandManager
				.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
				.executes(context -> {
					EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);
					EntityAttribute attribute = EditorUtil.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
					return remove(context.getSource(), attribute, slot);
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
				.literal("add")
				.executes(context -> set(context.getSource(), null, null, null))
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> addAttributeNode = ClientCommandManager
				.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Float> addAttributeAmountNode = ClientCommandManager
				.argument("amount", FloatArgumentType.floatArg())
				.executes(context -> {
					EntityAttribute attribute = EditorUtil.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
					float amount = FloatArgumentType.getFloat(context, "amount");
					EntityAttributeModifier modifier = new EntityAttributeModifier(Registries.ATTRIBUTE.getId(attribute).toString(), amount, EntityAttributeModifier.Operation.ADDITION);
					return set(context.getSource(), attribute, modifier, null);
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, EntityAttributeModifier.Operation> addAttributeAmountOperationNode = ClientCommandManager
				.argument("operation", EnumArgumentType.enumArgument(EntityAttributeModifier.Operation.class, AttributeNode::operationFormatter))
				.executes(context -> {
					EntityAttribute attribute = EditorUtil.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
					float amount = FloatArgumentType.getFloat(context, "amount");
					EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", EntityAttributeModifier.Operation.class);
					EntityAttributeModifier modifier = new EntityAttributeModifier(Registries.ATTRIBUTE.getId(attribute).toString(), amount, operation);
					return set(context.getSource(), attribute, modifier, null);
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> addAttributeAmountOperationSlotNode = ClientCommandManager
				.argument("slot", EnumArgumentType.enumArgument(EquipmentSlot.class))
				.executes(context -> {
					EntityAttribute attribute = EditorUtil.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
					float amount = FloatArgumentType.getFloat(context, "amount");
					EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", EntityAttributeModifier.Operation.class);
					EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);
					EntityAttributeModifier modifier = new EntityAttributeModifier(Registries.ATTRIBUTE.getId(attribute).toString(), amount, operation);
					return set(context.getSource(), attribute, modifier, slot);
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> addAttributeInfinityNode = ClientCommandManager
				.literal("infinity")
				.executes(context -> {
					EntityAttribute attribute = EditorUtil.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
					EntityAttributeModifier modifier = new EntityAttributeModifier(Registries.ATTRIBUTE.getId(attribute).toString(), Double.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION);
					return set(context.getSource(), attribute, modifier, null);
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, EntityAttributeModifier.Operation> addAttributeInfinityOperationNode = ClientCommandManager
				.argument("operation", EnumArgumentType.enumArgument(EntityAttributeModifier.Operation.class, AttributeNode::operationFormatter))
				.executes(context -> {
					EntityAttribute attribute = EditorUtil.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
					EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", EntityAttributeModifier.Operation.class);
					EntityAttributeModifier modifier = new EntityAttributeModifier(Registries.ATTRIBUTE.getId(attribute).toString(), Double.POSITIVE_INFINITY, operation);
					return set(context.getSource(), attribute, modifier, null);
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> addAttributeInfinityOperationSlotNode = ClientCommandManager
				.argument("slot", EnumArgumentType.enumArgument(EquipmentSlot.class))
				.executes(context -> {
					EntityAttribute attribute = EditorUtil.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
					EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", EntityAttributeModifier.Operation.class);
					EntityAttributeModifier modifier = new EntityAttributeModifier(Registries.ATTRIBUTE.getId(attribute).toString(), Double.POSITIVE_INFINITY, operation);
					EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);
					return set(context.getSource(), attribute, modifier, slot);
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
				.literal("clear")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!ItemUtil.hasAttributes(stack, false)) throw NO_ATTRIBUTES_EXCEPTION;
					ItemUtil.setAttributes(stack, null);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
					return 1;
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> clearSlotNode = ClientCommandManager
				.argument("slot", EnumArgumentType.enumArgument(EquipmentSlot.class))
				.executes(context -> {
					EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);
					return remove(context.getSource(), null, slot);
				})
				.build();

		rootNode.addChild(node);

		// ... attribute get [<slot>] [<attribute>]
		node.addChild(getNode);
		getNode.addChild(getSlotNode);
		getSlotNode.addChild(getSlotAttributeNode);

		// ... attribute add [<attribute> <amount>|infinity] [<operation>] [<slot>]
		node.addChild(addNode);
		addNode.addChild(addAttributeNode);
		addAttributeNode.addChild(addAttributeAmountNode);
		addAttributeAmountNode.addChild(addAttributeAmountOperationNode);
		addAttributeAmountOperationNode.addChild(addAttributeAmountOperationSlotNode);
		addAttributeNode.addChild(addAttributeInfinityNode);
		addAttributeInfinityNode.addChild(addAttributeInfinityOperationNode);
		addAttributeInfinityOperationNode.addChild(addAttributeInfinityOperationSlotNode);

		// ... attribute remove <attribute> [<slot>]
		node.addChild(removeNode);
		removeNode.addChild(removeAttributeNode);
		removeAttributeNode.addChild(removeAttributeSlotNode);

		// ... attribute clear [<slot>]
		node.addChild(clearNode);
		clearNode.addChild(clearSlotNode);
	}
}
