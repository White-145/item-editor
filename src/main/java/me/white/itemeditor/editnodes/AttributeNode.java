package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
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
import net.minecraft.text.Text;

public class AttributeNode {
	public static final CommandSyntaxException ALREADY_HAS_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.alreadyexists")).create();
	public static final CommandSyntaxException NO_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.noattributes")).create();
	public static final CommandSyntaxException NO_SUCH_ATTRIBUTE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.attribute.error.nosuchattribute")).create();
	private static final String OUTPUT_SET = "commands.edit.attribute.set";
	private static final String OUTPUT_SET_PERCENT = "commands.edit.attribute.setpercent";
	private static final String OUTPUT_SET_SLOT = "commands.edit.attribute.setslot";
	private static final String OUTPUT_SET_PERCENT_SLOT = "commands.edit.attribute.setpercentslot";
	private static final String OUTPUT_RESET = "commands.edit.attribute.reset";
	private static final String OUTPUT_CLEAR = "commands.edit.attribute.clear";
	private static final String OUTPUT_CLEAR_SLOT = "commands.edit.attribute.clearslot";
	private static final String ATTRIBUTE_MODIFIERS_KEY = "AttributeModifiers";

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

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("attribute")
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				ItemStack result = set(EditCommand.getItemStack(context.getSource()).copy(), null, 0, null, null);
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> setAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Float> setAttributeAmountNode = ClientCommandManager
			.argument("amount", FloatArgumentType.floatArg())
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, null);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), amount));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseNode = ClientCommandManager
			.literal("base")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, null);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), amount));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.MAINHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.OFFHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.HEAD);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.CHEST);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.LEGS);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountBaseFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.FEET);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_SLOT, Text.translatable(attribute.getTranslationKey()), amount, EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyNode = ClientCommandManager
			.literal("multiply")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, null);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT, Text.translatable(attribute.getTranslationKey()), amount * 100));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.MAINHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.OFFHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.HEAD);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.CHEST);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.LEGS);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountMultiplyFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.FEET);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalNode = ClientCommandManager
			.literal("total")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, null);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT, Text.translatable(attribute.getTranslationKey()), amount * 100));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.MAINHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.OFFHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.HEAD);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.CHEST);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.LEGS);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeAmountTotalFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				// execute
				ItemStack result = set(item, attribute, amount, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.FEET);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), amount * 100, EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityNode = ClientCommandManager
			.literal("infinity")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, null);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Registries.ATTRIBUTE.getId(attribute), "Infinity"));
				return 1;
			})
			.build();
			
			LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseNode = ClientCommandManager
			.literal("base")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, null);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), "Infinity"));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.MAINHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.OFFHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.HEAD);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.CHEST);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.LEGS);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityBaseFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, EquipmentSlot.FEET);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyNode = ClientCommandManager
			.literal("multiply")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, null);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT, Text.translatable(attribute.getTranslationKey()), "Infinity"));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.MAINHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.OFFHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.HEAD);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.CHEST);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.LEGS);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityMultiplyFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_BASE, EquipmentSlot.FEET);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalNode = ClientCommandManager
			.literal("total")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, null);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT, Text.translatable(attribute.getTranslationKey()), "Infinity"));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.MAINHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.MAINHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalOffhandNode = ClientCommandManager
			.literal("offhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.OFFHAND);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.OFFHAND.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalHeadNode = ClientCommandManager
			.literal("head")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.HEAD);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.HEAD.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalChestNode = ClientCommandManager
			.literal("chest")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.CHEST);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.CHEST.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalLegsNode = ClientCommandManager
			.literal("legs")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.LEGS);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.LEGS.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityTotalFeetNode = ClientCommandManager
			.literal("feet")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				// execute
				ItemStack result = set(item, attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.MULTIPLY_TOTAL, EquipmentSlot.FEET);
				// end
				EditCommand.setItemStack(context.getSource(), result);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Text.translatable(attribute.getTranslationKey()), "Infinity", EquipmentSlot.FEET.getName()));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				// check
				if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
				NbtCompound nbt = item.getNbt();
				if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY)) throw NO_ATTRIBUTES_EXCEPTION;
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
				if (attributes.isEmpty()) throw NO_ATTRIBUTES_EXCEPTION;
				// execute
				nbt.remove(ATTRIBUTE_MODIFIERS_KEY);
				item.setNbt(nbt);
				// end
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearMainhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				// check
				if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
				NbtCompound nbt = item.getNbt();
				if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY)) throw NO_ATTRIBUTES_EXCEPTION;
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
				if (attributes.isEmpty()) throw NO_ATTRIBUTES_EXCEPTION;
				// execute
				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.MAINHAND.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);
				// end
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearOffhandNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				// check
				if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
				NbtCompound nbt = item.getNbt();
				if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY)) throw NO_ATTRIBUTES_EXCEPTION;
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
				if (attributes.isEmpty()) throw NO_ATTRIBUTES_EXCEPTION;
				// execute
				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.OFFHAND.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);
				// end
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearHeadNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				// check
				if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
				NbtCompound nbt = item.getNbt();
				if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY)) throw NO_ATTRIBUTES_EXCEPTION;
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
				if (attributes.isEmpty()) throw NO_ATTRIBUTES_EXCEPTION;
				// execute
				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.HEAD.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);
				// end
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearChestNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				// check
				if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
				NbtCompound nbt = item.getNbt();
				if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY)) throw NO_ATTRIBUTES_EXCEPTION;
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
				if (attributes.isEmpty()) throw NO_ATTRIBUTES_EXCEPTION;
				// execute
				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.CHEST.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);
				// end
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearLegsNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				// check
				if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
				NbtCompound nbt = item.getNbt();
				if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY)) throw NO_ATTRIBUTES_EXCEPTION;
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
				if (attributes.isEmpty()) throw NO_ATTRIBUTES_EXCEPTION;
				// execute
				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.LEGS.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);
				// end
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearFeetNode = ClientCommandManager
			.literal("mainhand")
			.executes(context -> {
				// arguments
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				// check
				if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
				NbtCompound nbt = item.getNbt();
				if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY)) throw NO_ATTRIBUTES_EXCEPTION;
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
				if (attributes.isEmpty()) throw NO_ATTRIBUTES_EXCEPTION;
				// execute
				NbtList newAttributes = new NbtList();
				for (NbtElement nbtAttr : attributes) {
					String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
					if (!nbtSlot.equals(EquipmentSlot.FEET.getName())) newAttributes.add(nbtAttr);
				}
				if (newAttributes.equals(attributes)) throw NO_SUCH_ATTRIBUTE_EXCEPTION;
				nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				item.setNbt(nbt);
				// end
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR_SLOT));
				return 1;
			})
			.build();
		
		rootNode.addChild(node);

		// ... attribute get [<attribute>] [<slot>]
		// TODO

		// ... attribute set [<attribute> <amount>|infinity] [base|multiply|total] [mainhand|offhand|head|chest|legs|feet]
		// TODO: delete this hell and redo in a better way
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
