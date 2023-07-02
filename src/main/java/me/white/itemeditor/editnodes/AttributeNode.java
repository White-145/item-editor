package me.white.itemeditor.editnodes;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import me.white.itemeditor.EditCommand.Feedback;
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
	public static class SlotArgumentType implements ArgumentType<EquipmentSlot> {
		private static final DynamicCommandExceptionType INVALID_SLOT_EXCEPTION = new DynamicCommandExceptionType((arg) -> Text.translatable("commands.edit.error.attribute.invalidslot", arg));

		private static final Collection<String> EXAMPLES = List.of(
			"any",
			"mainhand",
			"legs"
		);

		public SlotArgumentType() {}

		public static SlotArgumentType slot() {
			return new SlotArgumentType();
		}

		@Override
		public EquipmentSlot parse(StringReader reader) throws CommandSyntaxException {
			int start = reader.getCursor();
			String result = reader.readString();
			switch (result) {
				case "any":
					return null;
				case "mainhand":
					return EquipmentSlot.MAINHAND;
				case "offhand":
					return EquipmentSlot.OFFHAND;
				case "head":
					return EquipmentSlot.HEAD;
				case "chest":
					return EquipmentSlot.CHEST;
				case "legs":
					return EquipmentSlot.LEGS;
				case "feet":
					return EquipmentSlot.FEET;
			}
			reader.setCursor(start);
			throw INVALID_SLOT_EXCEPTION.create(result);
		}

		@Override
		public Collection<String> getExamples() {
			return EXAMPLES;
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			String remaining = builder.getRemainingLowerCase();
			if ("any".startsWith(remaining)) builder.suggest("any");
			if ("mainhand".startsWith(remaining)) builder.suggest("mainhand");
			if ("offhand".startsWith(remaining)) builder.suggest("offhand");
			if ("head".startsWith(remaining)) builder.suggest("head");
			if ("chest".startsWith(remaining)) builder.suggest("chest");
			if ("legs".startsWith(remaining)) builder.suggest("legs");
			if ("feet".startsWith(remaining)) builder.suggest("feet");
			return builder.buildFuture();
		}

		public static EquipmentSlot getSlot(final CommandContext<?> context, final String name) {
			return context.getArgument(name, EquipmentSlot.class);
		}
	}

	public static class OperationArgumentType implements ArgumentType<String> {
		private static final DynamicCommandExceptionType INVALID_OPERATION_EXCEPTION = new DynamicCommandExceptionType((arg) -> Text.translatable("commands.edit.error.attribute.invalidslot", arg));

		private static final Collection<String> EXAMPLES = List.of(
			"base",
			"multiply",
			"total"
		);

		public OperationArgumentType() {}

		public static OperationArgumentType operation() {
			return new OperationArgumentType();
		}

		@Override
		public String parse(StringReader reader) throws CommandSyntaxException {
			int start = reader.getCursor();
			String result = reader.readString();
			switch (result) {
				case "base":
				case "multiply":
				case "total":
					return result;
			}
			reader.setCursor(start);
			throw INVALID_OPERATION_EXCEPTION.create(result);
		}

		@Override
		public Collection<String> getExamples() {
			return EXAMPLES;
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			String remaining = builder.getRemainingLowerCase();
			if ("base".startsWith(remaining)) builder.suggest("base");
			if ("multiply".startsWith(remaining)) builder.suggest("multiply");
			if ("total".startsWith(remaining)) builder.suggest("total");
			return builder.buildFuture();
		}

		public static EntityAttributeModifier.Operation getOperation(final CommandContext<?> context, final String name) {
			switch (context.getArgument(name, String.class)) {
				case "multiply":
					return EntityAttributeModifier.Operation.MULTIPLY_BASE;
				case "total":
					return EntityAttributeModifier.Operation.MULTIPLY_TOTAL;
			}
			return EntityAttributeModifier.Operation.ADDITION;
		}
	}

	public static final CommandSyntaxException ALREADY_HAS_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.attribute.alreadyexists")).create();
	public static final CommandSyntaxException NO_ATTRIBUTES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.attribute.noattributes")).create();
	public static final CommandSyntaxException NO_CLEAR_ATTRIBUTE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.attribute.noclearattribute")).create();
	private static final String OUTPUT_SET = "commands.edit.attribute.set";
	private static final String OUTPUT_SET_PERCENT = "commands.edit.attribute.setpercent";
	private static final String OUTPUT_SET_SLOT = "commands.edit.attribute.setslot";
	private static final String OUTPUT_SET_PERCENT_SLOT = "commands.edit.attribute.setpercentslot";
	private static final String OUTPUT_RESET = "commands.edit.attribute.reset";
	private static final String OUTPUT_CLEAR = "commands.edit.attribute.clear";
	private static final String OUTPUT_CLEAR_SLOT = "commands.edit.attribute.clearslot";
	private static final String ATTRIBUTE_MODIFIERS_KEY = "AttributeModifiers";

	private static Feedback set(ItemStack item, EntityAttribute attribute, float amount, EntityAttributeModifier.Operation operation, EquipmentSlot slot) throws CommandSyntaxException {
		if (attribute == null) {
			if (item.getSubNbt(ATTRIBUTE_MODIFIERS_KEY) != null) throw ALREADY_HAS_ATTRIBUTES_EXCEPTION;
			NbtList attributes = new NbtList();
			attributes.add(new NbtCompound());
			item.setSubNbt(ATTRIBUTE_MODIFIERS_KEY, attributes);
			return new Feedback(item, 1);
		}
		EntityAttributeModifier modifier = new EntityAttributeModifier(Registries.ATTRIBUTE.getId(attribute).toString(), (double)amount, operation);
		item.addAttributeModifier(attribute, modifier, slot);
		return new Feedback(item, 1);
	}

	private static EntityAttribute getAttributeArgument(CommandContext<FabricClientCommandSource> context, String key) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.isOf(RegistryKeys.ATTRIBUTE)) {
            throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), RegistryKeys.ATTRIBUTE.getValue());
        }
		return (EntityAttribute)reference.value();
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				Feedback result = set(EditCommand.getItemStack(context.getSource()).copy(), null, 0, null, null);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
				return result.value();
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<EntityAttribute>> setAttributeNode = ClientCommandManager
			.argument("attribute", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE))
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Float> setAttributeAmountNode = ClientCommandManager
			.argument("amount", FloatArgumentType.floatArg())
			.executes(context -> {
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				Feedback result = set(EditCommand.getItemStack(context.getSource()).copy(), attribute, amount, EntityAttributeModifier.Operation.ADDITION, null);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Registries.ATTRIBUTE.getId(attribute), amount));
				return result.value();
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> setAttributeAmountOperationNode = ClientCommandManager
			.argument("operation", OperationArgumentType.operation())
			.executes(context -> {
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				EntityAttributeModifier.Operation operation = OperationArgumentType.getOperation(context, "operation");
				Feedback result = set(EditCommand.getItemStack(context.getSource()).copy(), attribute, amount, operation, null);
				EditCommand.setItemStack(context.getSource(), result.result());
				if (operation == EntityAttributeModifier.Operation.ADDITION) {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Registries.ATTRIBUTE.getId(attribute), amount));
				} else {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT, Registries.ATTRIBUTE.getId(attribute), amount * 100));
				}
				return result.value();
			})
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> setAttributeAmountOperationSlotNode = ClientCommandManager
			.argument("slot", SlotArgumentType.slot())
			.executes(context -> {
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				float amount = FloatArgumentType.getFloat(context, "amount");
				EntityAttributeModifier.Operation operation = OperationArgumentType.getOperation(context, "operation");
				EquipmentSlot slot = SlotArgumentType.getSlot(context, "slot");
				Feedback result = set(EditCommand.getItemStack(context.getSource()).copy(), attribute, amount, operation, slot);
				EditCommand.setItemStack(context.getSource(), result.result());
				if (operation == EntityAttributeModifier.Operation.ADDITION) {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_SLOT, Registries.ATTRIBUTE.getId(attribute), amount, slot == null ? "Any" : slot.getName()));
				} else {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Registries.ATTRIBUTE.getId(attribute), amount, slot == null ? "Any" : slot.getName()));
				}
				return result.value();
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> setAttributeInfinityNode = ClientCommandManager
			.literal("infinity")
			.executes(context -> {
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				Feedback result = set(EditCommand.getItemStack(context.getSource()).copy(), attribute, Float.POSITIVE_INFINITY, EntityAttributeModifier.Operation.ADDITION, null);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Registries.ATTRIBUTE.getId(attribute), "Infinity"));
				return result.value();
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> setAttributeInfinityOperationNode = ClientCommandManager
			.argument("operation", OperationArgumentType.operation())
			.executes(context -> {
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				EntityAttributeModifier.Operation operation = OperationArgumentType.getOperation(context, "operation");
				Feedback result = set(EditCommand.getItemStack(context.getSource()).copy(), attribute, Float.POSITIVE_INFINITY, operation, null);
				EditCommand.setItemStack(context.getSource(), result.result());
				if (operation == EntityAttributeModifier.Operation.ADDITION) {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Registries.ATTRIBUTE.getId(attribute), "Infinity"));
				} else {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT, Registries.ATTRIBUTE.getId(attribute), "Infinity"));
				}
				return result.value();
			})
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> setAttributeInfinityOperationSlotNode = ClientCommandManager
			.argument("slot", SlotArgumentType.slot())
			.executes(context -> {
				EntityAttribute attribute = getAttributeArgument(context, "attribute");
				EntityAttributeModifier.Operation operation = OperationArgumentType.getOperation(context, "operation");
				EquipmentSlot slot = SlotArgumentType.getSlot(context, "slot");
				Feedback result = set(EditCommand.getItemStack(context.getSource()).copy(), attribute, Float.POSITIVE_INFINITY, operation, slot);
				EditCommand.setItemStack(context.getSource(), result.result());
				if (operation == EntityAttributeModifier.Operation.ADDITION) {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_SLOT, Registries.ATTRIBUTE.getId(attribute), "Infinity", slot == null ? "Any" : slot.getName()));
				} else {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET_PERCENT_SLOT, Registries.ATTRIBUTE.getId(attribute), "Infinity", slot == null ? "Any" : slot.getName()));
				}
				return result.value();
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getNbt();
				if (nbt == null) throw NO_ATTRIBUTES_EXCEPTION;
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
				if (attributes == null) throw NO_ATTRIBUTES_EXCEPTION;
				item.removeSubNbt(ATTRIBUTE_MODIFIERS_KEY);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR));
				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, EquipmentSlot> clearSlotNode = ClientCommandManager
			.argument("slot", SlotArgumentType.slot())
			.executes(context -> {
				EquipmentSlot slot = SlotArgumentType.getSlot(context, "slot");
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				if (!item.hasNbt()) throw NO_ATTRIBUTES_EXCEPTION;
				NbtCompound nbt = item.getNbt();
				NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
				if (attributes == null) throw NO_ATTRIBUTES_EXCEPTION;
				NbtList newAttributes = new NbtList();
				if (slot != null) {
					for (NbtElement nbtAttr : attributes) {
						String nbtSlot = ((NbtCompound)nbtAttr).getString("Slot");
						System.out.println(nbtSlot);
						System.out.println(slot.getName());
						if (!nbtSlot.equals(slot.getName())) {
							newAttributes.add(nbtAttr);
						}
					}
					if (newAttributes.equals(attributes)) throw NO_CLEAR_ATTRIBUTE_EXCEPTION;
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR_SLOT));
				} else {
					newAttributes.add(new NbtCompound());
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR));
				}
				if (newAttributes.isEmpty()) {
					nbt.remove(ATTRIBUTE_MODIFIERS_KEY);
				} else {
					nbt.put(ATTRIBUTE_MODIFIERS_KEY, newAttributes);
				}
				item.setNbt(nbt);
				EditCommand.setItemStack(context.getSource(), item);
				return 1;
			})
			.build();

		// ... attribute get [<attribute>] [<slot>]
		// TODO

		// ... attribute set [<attribute> <amount>|infinity] [<operation>] [<slot>]
		node.addChild(setNode);
		setNode.addChild(setAttributeNode);
		setAttributeNode.addChild(setAttributeAmountNode);
		setAttributeAmountNode.addChild(setAttributeAmountOperationNode);
		setAttributeAmountOperationNode.addChild(setAttributeAmountOperationSlotNode);
		setAttributeNode.addChild(setAttributeInfinityNode);
		setAttributeInfinityNode.addChild(setAttributeInfinityOperationNode);
		setAttributeInfinityOperationNode.addChild(setAttributeInfinityOperationSlotNode);

		// ... attribute remove [<attribute>] [<slot>]
		// TODO

		// ... attribute clear [<slot>]
		node.addChild(clearNode);
		clearNode.addChild(clearSlotNode);
	}
}
