package me.white.itemeditor.node;

import java.util.Optional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.EnumArgumentType;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class AttributeNode {
	public static final CommandSyntaxException ALREADY_HAS_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.alreadyexists")).create();
	public static final CommandSyntaxException NO_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.noattributes")).create();
	public static final CommandSyntaxException NO_SUCH_ATTRIBUTE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.nosuchattribute")).create();
	public static final CommandSyntaxException NO_SUCH_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.nosuchattributes")).create();
	private static final String OUTPUT_GET = "commands.edit.attribute.get";
	private static final String OUTPUT_SET = "commands.edit.attribute.set";
	private static final String OUTPUT_SET_PERCENT = "commands.edit.attribute.setpercent";
	private static final String OUTPUT_SET_SLOT = "commands.edit.attribute.setslot";
	private static final String OUTPUT_SET_PERCENT_SLOT = "commands.edit.attribute.setpercentslot";
	private static final String OUTPUT_REMOVE = "commands.edit.attribute.remove";
	private static final String OUTPUT_RESET = "commands.edit.attribute.reset";
	private static final String OUTPUT_CLEAR = "commands.edit.attribute.clear";
	private static final String OUTPUT_CLEAR_SLOT = "commands.edit.attribute.clearslot";
	private static final String OPERATION_BASE = "commands.edit.attribute.operationbase";
	private static final String OPERATION_MULTIPLY = "commands.edit.attribute.operationmultiply";
	private static final String OPERATION_TOTAL = "commands.edit.attribute.operationtotal";
	private static final String ATTRIBUTE_MODIFIERS_KEY = "AttributeModifiers";

	private static enum Operation {
		BASE(EntityAttributeModifier.Operation.ADDITION),
		MULTIPLY(EntityAttributeModifier.Operation.MULTIPLY_BASE),
		TOTAL(EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

		public EntityAttributeModifier.Operation operation;

		private Operation(EntityAttributeModifier.Operation operation) {
			this.operation = operation;
		}
	}

	private static void checkHasAttributes(FabricClientCommandSource source) throws CommandSyntaxException {
		ItemStack item = Util.getItemStack(source);
		if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
		NbtCompound nbt = item.getNbt();
		if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY)) throw NO_ATTRIBUTES_EXCEPTION;
		NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
		if (attributes.isEmpty()) throw NO_ATTRIBUTES_EXCEPTION;
	}

	private static int get(FabricClientCommandSource source, EntityAttribute attribute, EquipmentSlot slot) throws CommandSyntaxException {
		ItemStack item = Util.getItemStack(source);
		Multimap<EntityAttribute, EntityAttributeModifier> attributes;
		if (slot != null) {
			Multimap<EntityAttribute, EntityAttributeModifier> dirtyAttributes = item.getAttributeModifiers(slot);
			if (attribute != null) {
				String id = Registries.ATTRIBUTE.getId(attribute).toString();
				attributes = HashMultimap.create();
				dirtyAttributes.forEach((attr, mod) -> {
					if (mod.getName().equals(id)) attributes.put(attr, mod);
				});
			} else {
				attributes = dirtyAttributes;
			}
		} else {
			String id = null;
			if (attribute != null) id = Registries.ATTRIBUTE.getId(attribute).toString();
			attributes = HashMultimap.create();
            NbtList nbtList = item.getNbt().getList("AttributeModifiers", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < nbtList.size(); ++i) {
                NbtCompound nbtCompound = nbtList.getCompound(i);
                EntityAttributeModifier entityAttributeModifier = EntityAttributeModifier.fromNbt(nbtCompound);
                Optional<EntityAttribute> optional = Registries.ATTRIBUTE.getOrEmpty(Identifier.tryParse(nbtCompound.getString("AttributeName")));
                if (
					!optional.isPresent() ||
					entityAttributeModifier == null ||
					(attribute != null && !entityAttributeModifier.getName().equals(id)) ||
					entityAttributeModifier.getId().getLeastSignificantBits() == 0L ||
					entityAttributeModifier.getId().getMostSignificantBits() == 0L
				) continue;
                attributes.put(optional.get(), entityAttributeModifier);
            }
		}
		if (attributes.isEmpty()) throw NO_SUCH_ATTRIBUTES_EXCEPTION;
		source.sendFeedback(Text.translatable(OUTPUT_GET));
		attributes.forEach((attr, mod) -> {
			source.sendFeedback(Text.empty()
				.append(Text.literal("- ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
				.append(switch (mod.getOperation()) {
					case MULTIPLY_BASE -> OPERATION_MULTIPLY;
					case MULTIPLY_TOTAL -> OPERATION_TOTAL;
					default -> OPERATION_BASE;
				})
				.append(" ")
				.append(Text.translatable(attr.getTranslationKey()))
				.append(" ")
				.append(mod.getValue() > 0 ? "+" : "")
				.append(mod.getOperation() == EntityAttributeModifier.Operation.ADDITION
					? Text.empty()
						.append(String.valueOf(mod.getValue()))
					: Text.empty()
						.append(String.valueOf(mod.getValue() * 100))
						.append("%")
				)
			);
		});
		return attributes.size();
	}

	private static int remove(FabricClientCommandSource source, EntityAttribute attribute, EquipmentSlot slot) throws CommandSyntaxException {
		ItemStack item = Util.getItemStack(source).copy();
		String id = Registries.ATTRIBUTE.getId(attribute).toString();

		NbtList attributes = item.getNbt().getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
		NbtList newAttributes = new NbtList();
		for (NbtElement nbtAttr : attributes) {
			String nbtId = ((NbtCompound)nbtAttr).getString("Name");
			String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
			if (nbtId.equals(id) && (slot == null || nbtSlot.isEmpty() || slot.getName().equals(nbtSlot))) continue;
			newAttributes.add(nbtAttr);
		}
		if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTES_EXCEPTION;
		item.setSubNbt(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
		Util.setItemStack(source, item);
		int dif = attributes.size() - newAttributes.size();
		source.sendFeedback(Text.translatable(OUTPUT_REMOVE, dif));
		return dif;
	}

	private static ItemStack set(ItemStack item, EntityAttribute attribute, float amount, EntityAttributeModifier.Operation operation, EquipmentSlot slot) throws CommandSyntaxException {
		if (attribute == null) {
			if (item.getSubNbt(ATTRIBUTE_MODIFIERS_KEY) != null) throw ALREADY_HAS_ATTRIBUTES_EXCEPTION;
			NbtList attributes = new NbtList();
			attributes.add(new NbtCompound());
			item.setSubNbt(ATTRIBUTE_MODIFIERS_KEY, attributes);
			return item;
		}
		EntityAttributeModifier modifier = new EntityAttributeModifier(Registries.ATTRIBUTE.getId(attribute).toString(), (double)amount, operation);
		item.addAttributeModifier(attribute, modifier, slot);
		return item;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("attribute")
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				Util.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				return get(context.getSource(), null, null);
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> getSlotNode = ClientCommandManager
			.argument("slot", EnumArgumentType.enumArgument(EquipmentSlot.class))
			.executes(context -> {
				Util.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);
				return get(context.getSource(), null, slot);
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> getSlotAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.executes(context -> {
				Util.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);
				EntityAttribute attribute = Util.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
				return get(context.getSource(), attribute, slot);
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> removeAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.executes(context -> {
				Util.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				EntityAttribute attribute = Util.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
				return remove(context.getSource(), attribute, null);
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> removeAttributeSlotNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.executes(context -> {
				Util.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);
				EntityAttribute attribute = Util.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
				return remove(context.getSource(), attribute, slot);
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				Util.checkCanEdit(context.getSource());

				ItemStack result = set(Util.getItemStack(context.getSource()).copy(), null, 0, null, null);
				Util.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> setAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Float> setAttributeAmountNode = ClientCommandManager
			.argument("amount", FloatArgumentType.floatArg())
			.executes(context -> {
				Util.checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = Util.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, null);

				Util.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), amount));
				return 1;
			})
			.build();
			
		ArgumentCommandNode<FabricClientCommandSource, Operation> setAttributeAmountOperationNode = ClientCommandManager
			.argument("operation", EnumArgumentType.enumArgument(Operation.class))
			.executes(context -> {
				Util.checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = Util.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
				float amount = FloatArgumentType.getFloat(context, "amount");
				EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", Operation.class).operation;

				ItemStack result = set(item, attribute, amount, operation, null);

				Util.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(
					operation == EntityAttributeModifier.Operation.ADDITION ? OUTPUT_SET : OUTPUT_SET_PERCENT,
					Text.translatable(attribute.getTranslationKey()), amount
				));
				return 1;
			})
			.build();
			
		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> setAttributeAmountOperationSlotNode = ClientCommandManager
			.argument("slot", EnumArgumentType.enumArgument(EquipmentSlot.class))
			.executes(context -> {
				Util.checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = Util.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
				float amount = FloatArgumentType.getFloat(context, "amount");
				EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", Operation.class).operation;
				EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);

				ItemStack result = set(item, attribute, amount, operation, slot);

				Util.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(
					operation == EntityAttributeModifier.Operation.ADDITION ? OUTPUT_SET_SLOT : OUTPUT_SET_PERCENT_SLOT,
					Text.translatable(attribute.getTranslationKey()), amount, slot.getName()
				));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityNode = ClientCommandManager
			.literal("infinity")
			.executes(context -> {
				Util.checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = Util.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, null);

				Util.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, Registries.ATTRIBUTE.getId(attribute), "Infinity"));
				return 1;
			})
			.build();
			
		ArgumentCommandNode<FabricClientCommandSource, Operation> setAttributeInfinityOperationNode = ClientCommandManager
			.argument("operation", EnumArgumentType.enumArgument(Operation.class))
			.executes(context -> {
				Util.checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = Util.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
				EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", Operation.class).operation;

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, operation, null);

				Util.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(
					operation == EntityAttributeModifier.Operation.ADDITION ? OUTPUT_SET : OUTPUT_SET_PERCENT,
					Text.translatable(attribute.getTranslationKey()), "Infinity"
				));
				return 1;
			})
			.build();
			
		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> setAttributeInfinityOperationSlotNode = ClientCommandManager
			.argument("slot", EnumArgumentType.enumArgument(EquipmentSlot.class))
			.executes(context -> {
				Util.checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = Util.getRegistryEntryArgument(context, "attribute", RegistryKeys.ATTRIBUTE);
				EntityAttributeModifier.Operation operation = EnumArgumentType.getEnum(context, "operation", Operation.class).operation;
				EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, operation, slot);

				Util.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(
					operation == EntityAttributeModifier.Operation.ADDITION ? OUTPUT_SET_SLOT : OUTPUT_SET_PERCENT_SLOT,
					Text.translatable(attribute.getTranslationKey()), "Infinity", slot.getName()
				));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				Util.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();

				NbtCompound nbt = item.getNbt();
				nbt.remove(ATTRIBUTE_MODIFIERS_KEY);
				item.setNbt(nbt);

				Util.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> clearSlotNode = ClientCommandManager
			.argument("slot", EnumArgumentType.enumArgument(EquipmentSlot.class))
			.executes(context -> {
				Util.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				EquipmentSlot slot = EnumArgumentType.getEnum(context, "slot", EquipmentSlot.class);

				NbtCompound nbt = item.getNbt();
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);

				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(slot.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);

				Util.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		rootNode.addChild(node);

		// ... attribute get [<slot>] [<attribute>]
		node.addChild(getNode);
		getNode.addChild(getSlotNode);
		getSlotNode.addChild(getSlotAttributeNode);

		// ... attribute set [<attribute> <amount>|infinity] [<operation>] [<slot>]
		node.addChild(setNode);
		setNode.addChild(setAttributeNode);
		setAttributeNode.addChild(setAttributeAmountNode);
		setAttributeAmountNode.addChild(setAttributeAmountOperationNode);
		setAttributeAmountOperationNode.addChild(setAttributeAmountOperationSlotNode);
		setAttributeNode.addChild(setAttributeInfinityNode);
		setAttributeInfinityNode.addChild(setAttributeInfinityOperationNode);
		setAttributeInfinityOperationNode.addChild(setAttributeInfinityOperationSlotNode);

		// ... attribute remove <attribute> [<slot>]
		node.addChild(removeNode);
		removeNode.addChild(removeAttributeNode);
		removeAttributeNode.addChild(removeAttributeSlotNode);

		// ... attribute clear [<slot>]
		node.addChild(clearNode);
		clearNode.addChild(clearSlotNode);
	}
}
