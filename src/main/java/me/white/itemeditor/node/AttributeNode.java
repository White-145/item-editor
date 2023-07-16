package me.white.itemeditor.node;

import java.util.Optional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.ItemManager;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
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
	private static final String OUTPUT_RESET = "commands.edit.attribute.reset";
	private static final String OUTPUT_CLEAR = "commands.edit.attribute.clear";
	private static final String OUTPUT_CLEAR_SLOT = "commands.edit.attribute.clearslot";
	private static final String OPERATION_BASE = "commands.edit.attribute.operationbase";
	private static final String OPERATION_MULTIPLY = "commands.edit.attribute.operationmultiply";
	private static final String OPERATION_TOTAL = "commands.edit.attribute.operationtotal";
	private static final String ATTRIBUTE_MODIFIERS_KEY = "AttributeModifiers";

	private static void get(FabricClientCommandSource context, EntityAttribute attribute, EquipmentSlot slot) throws CommandSyntaxException {
		ItemStack item = ItemManager.getItemStack(context);
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
			String id = "";
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
		context.sendFeedback(Text.translatable(OUTPUT_GET));
		attributes.forEach((attr, mod) -> {
			context.sendFeedback(Text.empty()
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

	private static EntityAttribute getAttributeArgument(CommandContext<FabricClientCommandSource> context, String key) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.isOf(RegistryKeys.ATTRIBUTE)) {
            throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), RegistryKeys.ATTRIBUTE.getValue());
        }
		return (EntityAttribute)reference.value();
	}

	private static void checkHasAttributes(FabricClientCommandSource context) throws CommandSyntaxException {
		ItemStack item = ItemManager.getItemStack(context);
		if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
		NbtCompound nbt = item.getNbt();
		if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY)) throw NO_ATTRIBUTES_EXCEPTION;
		NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
		if (attributes.isEmpty()) throw NO_ATTRIBUTES_EXCEPTION;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("attribute")
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				get(context.getSource(), null, null);

				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> getMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				get(context.getSource(), null, EquipmentSlot.MAINHAND);

				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> getOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				get(context.getSource(), null, EquipmentSlot.OFFHAND);

				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> getHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				get(context.getSource(), null, EquipmentSlot.HEAD);

				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> getChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				get(context.getSource(), null, EquipmentSlot.CHEST);

				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> getLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				get(context.getSource(), null, EquipmentSlot.LEGS);

				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> getFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				get(context.getSource(), null, EquipmentSlot.FEET);

				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> getMainhandAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				get(context.getSource(), attribute, EquipmentSlot.MAINHAND);

				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> getOffhandAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				get(context.getSource(), attribute, EquipmentSlot.OFFHAND);

				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> getHeadAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				get(context.getSource(), attribute, EquipmentSlot.HEAD);

				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> getChestAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				get(context.getSource(), attribute, EquipmentSlot.CHEST);

				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> getLegsAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				get(context.getSource(), attribute, EquipmentSlot.LEGS);

				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> getFeetAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());
				checkHasAttributes(context.getSource());

				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				get(context.getSource(), attribute, EquipmentSlot.FEET);

				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack result = set(ItemManager.getItemStack(context.getSource()).copy(), null, 0, null, null);
				ItemManager.setItemStack(context.getSource(), result);
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
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, null);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), amount));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseNode = ClientCommandManager
			.literal("base")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, null);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), amount));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.MAINHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.OFFHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.HEAD);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.CHEST);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.LEGS);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.FEET);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyNode = ClientCommandManager
			.literal("multiply")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, null);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT, Text.translatable(attribute.getTranslationKey()), amount * 100));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.MAINHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.OFFHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.HEAD);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.CHEST);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.LEGS);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.FEET);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalNode = ClientCommandManager
			.literal("total")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, null);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT, Text.translatable(attribute.getTranslationKey()), amount * 100));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.MAINHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.OFFHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.HEAD);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.CHEST);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.LEGS);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");

				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.FEET);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityNode = ClientCommandManager
			.literal("infinity")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, null);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, Registries.ATTRIBUTE.getId(attribute), "Infinity"));
				return 1;
			})
			.build();
			
			LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseNode = ClientCommandManager
			.literal("base")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, null);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), "Infinity"));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.MAINHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.OFFHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.HEAD);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.CHEST);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.LEGS);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.FEET);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyNode = ClientCommandManager
			.literal("multiply")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, null);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT, Text.translatable(attribute.getTranslationKey()), "Infinity"));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.MAINHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.OFFHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.HEAD);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.CHEST);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.LEGS);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.FEET);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalNode = ClientCommandManager
			.literal("total")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, null);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT, Text.translatable(attribute.getTranslationKey()), "Infinity"));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.MAINHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.OFFHAND);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.HEAD);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.CHEST);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.LEGS);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");

				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.FEET);

				ItemManager.setItemStack(context.getSource(), result);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();

				NbtCompound nbt = item.getNbt();
				nbt.remove(ATTRIBUTE_MODIFIERS_KEY);
				item.setNbt(nbt);

				ItemManager.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getNbt();
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);

				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.MAINHAND.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);

				ItemManager.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearOffhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getNbt();
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);

				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.OFFHAND.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);

				ItemManager.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearHeadNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getNbt();
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);

				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.HEAD.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);

				ItemManager.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearChestNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getNbt();
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);

				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.CHEST.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);

				ItemManager.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearLegsNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getNbt();
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);

				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.LEGS.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);

				ItemManager.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearFeetNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());
				checkHasAttributes(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getNbt();
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);

				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.FEET.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);

				ItemManager.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		rootNode.addChild(node);

		// ... attribute get [<slot>] [<attribute>]
		node.addChild(getNode);
		getNode.addChild(getMainhandNode);
		getMainhandNode.addChild(getMainhandAttributeNode);
		getNode.addChild(getOffhandNode);
		getOffhandNode.addChild(getOffhandAttributeNode);
		getNode.addChild(getHeadNode);
		getHeadNode.addChild(getHeadAttributeNode);
		getNode.addChild(getChestNode);
		getChestNode.addChild(getChestAttributeNode);
		getNode.addChild(getLegsNode);
		getLegsNode.addChild(getLegsAttributeNode);
		getNode.addChild(getFeetNode);
		getFeetNode.addChild(getFeetAttributeNode);

		// ... attribute set [<attribute> <amount>|infinity] [<operation>] [<slot>]
		node.addChild(setNode);
		setNode.addChild(setAttributeNode);
		setAttributeNode.addChild(setAttributeAmountNode);
		setAttributeAmountNode.addChild(setAttributeAmountBaseNode);
		setAttributeAmountBaseNode.addChild(setAttributeAmountBaseMainhandNode);
		setAttributeAmountBaseNode.addChild(setAttributeAmountBaseOffhandNode);
		setAttributeAmountBaseNode.addChild(setAttributeAmountBaseHeadNode);
		setAttributeAmountBaseNode.addChild(setAttributeAmountBaseChestNode);
		setAttributeAmountBaseNode.addChild(setAttributeAmountBaseLegsNode);
		setAttributeAmountBaseNode.addChild(setAttributeAmountBaseFeetNode);
		setAttributeAmountNode.addChild(setAttributeAmountMultiplyNode);
		setAttributeAmountMultiplyNode.addChild(setAttributeAmountMultiplyMainhandNode);
		setAttributeAmountMultiplyNode.addChild(setAttributeAmountMultiplyOffhandNode);
		setAttributeAmountMultiplyNode.addChild(setAttributeAmountMultiplyHeadNode);
		setAttributeAmountMultiplyNode.addChild(setAttributeAmountMultiplyChestNode);
		setAttributeAmountMultiplyNode.addChild(setAttributeAmountMultiplyLegsNode);
		setAttributeAmountMultiplyNode.addChild(setAttributeAmountMultiplyFeetNode);
		setAttributeAmountNode.addChild(setAttributeAmountTotalNode);
		setAttributeAmountTotalNode.addChild(setAttributeAmountTotalMainhandNode);
		setAttributeAmountTotalNode.addChild(setAttributeAmountTotalOffhandNode);
		setAttributeAmountTotalNode.addChild(setAttributeAmountTotalHeadNode);
		setAttributeAmountTotalNode.addChild(setAttributeAmountTotalChestNode);
		setAttributeAmountTotalNode.addChild(setAttributeAmountTotalLegsNode);
		setAttributeAmountTotalNode.addChild(setAttributeAmountTotalFeetNode);
		setAttributeNode.addChild(setAttributeInfinityNode);
		setAttributeInfinityNode.addChild(setAttributeInfinityBaseNode);
		setAttributeInfinityBaseNode.addChild(setAttributeInfinityBaseMainhandNode);
		setAttributeInfinityBaseNode.addChild(setAttributeInfinityBaseOffhandNode);
		setAttributeInfinityBaseNode.addChild(setAttributeInfinityBaseHeadNode);
		setAttributeInfinityBaseNode.addChild(setAttributeInfinityBaseChestNode);
		setAttributeInfinityBaseNode.addChild(setAttributeInfinityBaseLegsNode);
		setAttributeInfinityBaseNode.addChild(setAttributeInfinityBaseFeetNode);
		setAttributeInfinityNode.addChild(setAttributeInfinityMultiplyNode);
		setAttributeInfinityMultiplyNode.addChild(setAttributeInfinityMultiplyMainhandNode);
		setAttributeInfinityMultiplyNode.addChild(setAttributeInfinityMultiplyOffhandNode);
		setAttributeInfinityMultiplyNode.addChild(setAttributeInfinityMultiplyHeadNode);
		setAttributeInfinityMultiplyNode.addChild(setAttributeInfinityMultiplyChestNode);
		setAttributeInfinityMultiplyNode.addChild(setAttributeInfinityMultiplyLegsNode);
		setAttributeInfinityMultiplyNode.addChild(setAttributeInfinityMultiplyFeetNode);
		setAttributeInfinityNode.addChild(setAttributeInfinityTotalNode);
		setAttributeInfinityTotalNode.addChild(setAttributeInfinityTotalMainhandNode);
		setAttributeInfinityTotalNode.addChild(setAttributeInfinityTotalOffhandNode);
		setAttributeInfinityTotalNode.addChild(setAttributeInfinityTotalHeadNode);
		setAttributeInfinityTotalNode.addChild(setAttributeInfinityTotalChestNode);
		setAttributeInfinityTotalNode.addChild(setAttributeInfinityTotalLegsNode);
		setAttributeInfinityTotalNode.addChild(setAttributeInfinityTotalFeetNode);

		// ... attribute remove [<attribute>] [<slot>]
		// TODO

		// ... attribute clear [<slot>]
		node.addChild(clearNode);
		clearNode.addChild(clearMainhandNode);
		clearNode.addChild(clearOffhandNode);
		clearNode.addChild(clearHeadNode);
		clearNode.addChild(clearChestNode);
		clearNode.addChild(clearLegsNode);
		clearNode.addChild(clearFeetNode);
	}
}
