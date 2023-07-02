package me.white.itemeditor.editnodes;

import java.util.Map;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import me.white.itemeditor.EditCommand.Feedback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnchantmentNode {
	private static final CommandSyntaxException EXISTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.enchantment.alreadyexists")).create();
	private static final CommandSyntaxException DOESNT_EXIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.enchantment.doesntexist")).create();
	private static final CommandSyntaxException NO_ENCHANTMENTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.enchantment.noenchantments")).create();
	private static final CommandSyntaxException HAS_GLINT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.enchantment.hasglint")).create();
	private static final String OUTPUT_GET = "commands.edit.enchantment.get";
	private static final String OUTPUT_GET_ENCHANTMENT = "commands.edit.enchantment.getenchantment";
	private static final String OUTPUT_SET = "commands.edit.enchantment.set";
	private static final String OUTPUT_REMOVE = "commands.edit.enchantment.remove";
	private static final String OUTPUT_CLEAR = "commands.edit.enchantment.clear";
	private static final String OUTPUT_GLINT = "commands.edit.enchantment.glint";

	private static final String ENCHANTMENTS_KEY = "Enchantments";
	private static final String ID_KEY = "id";
	private static final String LVL_KEY = "lvl";

	private static Enchantment getEnchantmentArgument(CommandContext<FabricClientCommandSource> context, String key) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.isOf(RegistryKeys.ENCHANTMENT)) {
            throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), RegistryKeys.ENCHANTMENT.getValue());
        }
		return (Enchantment)reference.value();
	}

	public static Feedback set(ItemStack item, Enchantment enchantment, int level) throws CommandSyntaxException {
		NbtList enchantments = item.getEnchantments();
		Identifier id = Registries.ENCHANTMENT.getKey(enchantment).get().getValue();
		for (NbtElement oldEnchantment : enchantments) {
			String oldId = ((NbtCompound)oldEnchantment).getString(ID_KEY);
			if (oldId.equals(String.valueOf(id))) {
				int oldLvl = ((NbtCompound)oldEnchantment).getShort(LVL_KEY);
				if (oldLvl == level) {
					throw EXISTS_EXCEPTION;
				}
				enchantments.remove(oldEnchantment);
			}
		}
		if (!(enchantments.size() == 1 && ((NbtCompound)enchantments.get(0)).isEmpty())) {
			enchantments.clear();
		}
		enchantments.add(EnchantmentHelper.createNbt(id, level));
		item.setSubNbt(ENCHANTMENTS_KEY, enchantments);
		return new Feedback(item, 1);
	}

	public static Feedback remove(ItemStack item, Enchantment enchantment) throws CommandSyntaxException {
		NbtList enchantments = item.getEnchantments();
		Identifier id = Registries.ENCHANTMENT.getKey(enchantment).get().getValue();
		for (NbtElement oldEnchantment : enchantments) {
			if (((NbtCompound)oldEnchantment).isEmpty()) continue;
			String oldId = ((NbtCompound)oldEnchantment).getString(ID_KEY);
			if (oldId.equals(String.valueOf(id))) {
				int lvl = ((NbtCompound)oldEnchantment).getInt(LVL_KEY);
				enchantments.remove(oldEnchantment);
				item.setSubNbt(ENCHANTMENTS_KEY, enchantments);
				return new Feedback(item, lvl);
			}
		}

		throw DOESNT_EXIST_EXCEPTION;
	}

	public static Feedback clear(ItemStack item) throws CommandSyntaxException {
		if (!item.hasEnchantments()) {
			throw NO_ENCHANTMENTS_EXCEPTION;
		}
		item.removeSubNbt(ENCHANTMENTS_KEY);
		return new Feedback(item, 1);
	}

	public static Feedback glint(ItemStack item) throws CommandSyntaxException {
		if (item.hasEnchantments()) {
			NbtList enchantments = item.getEnchantments();
			if (!(enchantments.size() == 1 && ((NbtCompound)enchantments.get(0)).isEmpty())) {
				throw HAS_GLINT_EXCEPTION;
			}
			item.removeSubNbt(ENCHANTMENTS_KEY);
			return new Feedback(item, 1);
		}
		NbtList enchantments = new NbtList();
		enchantments.add(new NbtCompound());
		item.setSubNbt(ENCHANTMENTS_KEY, enchantments);
		return new Feedback(item, 0);
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource());
				if (!item.hasEnchantments()) {
					throw NO_ENCHANTMENTS_EXCEPTION;
				}
				if (item.getEnchantments().size() == 1 && ((NbtCompound)item.getEnchantments().get(0)).isEmpty()) {
					throw NO_ENCHANTMENTS_EXCEPTION;
				}
				ClientPlayerEntity player = context.getSource().getPlayer();
				player.sendMessage(Text.translatable(OUTPUT_GET));
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.fromNbt(item.getEnchantments());
				for (Enchantment enchantment : enchantments.keySet()) {
					int lvl = enchantments.get(enchantment);
					player.sendMessage(Text.empty().append("- ").append(enchantment.getName(lvl)));
				}
				return 1;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> getEnchantmentNode = ClientCommandManager
			.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource());
				if (!item.hasEnchantments()) {
					throw NO_ENCHANTMENTS_EXCEPTION;
				}
				if (item.getEnchantments().size() == 1 && ((NbtCompound)item.getEnchantments().get(0)).isEmpty()) {
					throw NO_ENCHANTMENTS_EXCEPTION;
				}
				Enchantment enchantment = getEnchantmentArgument(context, "enchantment");
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.fromNbt(item.getEnchantments());
				if (!enchantments.containsKey(enchantment)) {
					throw DOESNT_EXIST_EXCEPTION;
				}
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET_ENCHANTMENT, Text.translatable(enchantment.getTranslationKey()), enchantments.get(enchantment)));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> setEnchantmentNode = ClientCommandManager
			.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Enchantment enchantment = getEnchantmentArgument(context, "enchantment");
				Feedback result = set(item, enchantment, 1);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, enchantment.getTranslationKey(), 1));
				return result.value();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setEnchantmentLevelNode = ClientCommandManager
			.argument("level", IntegerArgumentType.integer(0, 255))
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Enchantment enchantment = getEnchantmentArgument(context, "enchantment");
				int level = IntegerArgumentType.getInteger(context, "level");
				Feedback result = set(item, enchantment, level);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Text.translatable(enchantment.getTranslationKey()), level));
				return result.value();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> removeEnchantmentNode = ClientCommandManager
			.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Enchantment enchantment = getEnchantmentArgument(context, "enchantment");
				Feedback result = remove(item, enchantment);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_REMOVE, Text.translatable(enchantment.getTranslationKey())));
				return result.value();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Feedback result = clear(item);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_CLEAR));
				return result.value();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> glintNode = ClientCommandManager
			.literal("glint")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Feedback result = glint(item);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GLINT));
				return result.value();
			})
			.build();

		// ... get [<enchantment>]
		node.addChild(getNode);
		getNode.addChild(getEnchantmentNode);

		// ... set <enchantment> [<level>]
		node.addChild(setNode);
		setNode.addChild(setEnchantmentNode);
		setEnchantmentNode.addChild(setEnchantmentLevelNode);

		// ... remove <enchantment>
		node.addChild(removeNode);
		removeNode.addChild(removeEnchantmentNode);

		// ... clear
		node.addChild(clearNode);

		// ... glint
		node.addChild(glintNode);
	}
}
