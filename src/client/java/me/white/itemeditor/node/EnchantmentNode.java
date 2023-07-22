package me.white.itemeditor.node;

import java.util.Map;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class EnchantmentNode {
	public static final CommandSyntaxException EXISTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.alreadyexists")).create();
	public static final CommandSyntaxException DOESNT_EXIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.doesntexist")).create();
	public static final CommandSyntaxException NO_ENCHANTMENTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.noenchantments")).create();
	public static final CommandSyntaxException HAS_GLINT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.hasglint")).create();
	private static final String OUTPUT_GET = "commands.edit.enchantment.get";
	private static final String OUTPUT_GET_ENCHANTMENT = "commands.edit.enchantment.getenchantment";
	private static final String OUTPUT_SET = "commands.edit.enchantment.set";
	private static final String OUTPUT_REMOVE = "commands.edit.enchantment.remove";
	private static final String OUTPUT_CLEAR = "commands.edit.enchantment.clear";
	private static final String OUTPUT_GLINT_ENABLE = "commands.edit.enchantment.glintenable";
	private static final String OUTPUT_GLINT_DISABLE = "commands.edit.enchantment.glintdisable";
	private static final String ENCHANTMENTS_KEY = "Enchantments";
	private static final String ID_KEY = "id";
	private static final String LVL_KEY = "lvl";

	private static void checkHasEnchantments(FabricClientCommandSource source) throws CommandSyntaxException {
		ItemStack item = Util.getItemStack(source);
		if (!item.hasNbt()) throw NO_ENCHANTMENTS_EXCEPTION;
		NbtCompound nbt = item.getNbt();
		if (!nbt.contains(ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) throw NO_ENCHANTMENTS_EXCEPTION;
		NbtList enchantments = nbt.getList(ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
		if (enchantments.isEmpty() || ((NbtCompound)enchantments.get(0)).isEmpty()) throw NO_ENCHANTMENTS_EXCEPTION;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("enchantment")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
                Util.checkHasItem(context.getSource());
				checkHasEnchantments(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource());

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.fromNbt(item.getEnchantments());
				for (Enchantment enchantment : enchantments.keySet()) {
					context.getSource().sendFeedback(Text.empty()
						.append(Text.literal("- ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
						.append(enchantment.getName(enchantments.get(enchantment)))
					);
				}
				return 1;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> getEnchantmentNode = ClientCommandManager
			.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
			.executes(context -> {
                Util.checkHasItem(context.getSource());
				checkHasEnchantments(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource());
				Enchantment enchantment = Util.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);

				Map<Enchantment, Integer> enchantments = EnchantmentHelper.fromNbt(item.getEnchantments());
				if (!enchantments.containsKey(enchantment)) throw DOESNT_EXIST_EXCEPTION;
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_ENCHANTMENT, enchantment.getName(enchantments.get(enchantment))));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> setEnchantmentNode = ClientCommandManager
			.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
			.executes(context -> {
                Util.checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				Enchantment enchantment = Util.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);

				NbtList enchantments = item.getEnchantments();
				Identifier id = Registries.ENCHANTMENT.getKey(enchantment).get().getValue();
				int oldLvl = 0;
				for (NbtElement oldEnchantment : enchantments) {
					String oldId = ((NbtCompound)oldEnchantment).getString(ID_KEY);
					if (oldId.equals(String.valueOf(id))) {
						oldLvl = ((NbtCompound)oldEnchantment).getShort(LVL_KEY);
						if (oldLvl == 1) throw EXISTS_EXCEPTION;
						enchantments.remove(oldEnchantment);
					}
				}
				enchantments.add(EnchantmentHelper.createNbt(id, 1));
				item.setSubNbt(ENCHANTMENTS_KEY, enchantments);
				Util.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, enchantment.getTranslationKey(), 1));
				return oldLvl;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setEnchantmentLevelNode = ClientCommandManager
			.argument("level", IntegerArgumentType.integer(0, 255))
			.executes(context -> {
                Util.checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				Enchantment enchantment = Util.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
				int level = IntegerArgumentType.getInteger(context, "level");

				NbtList enchantments = item.getEnchantments();
				Identifier id = Registries.ENCHANTMENT.getKey(enchantment).get().getValue();
				int oldLvl = 0;
				for (NbtElement oldEnchantment : enchantments) {
					String oldId = ((NbtCompound)oldEnchantment).getString(ID_KEY);
					if (oldId.equals(String.valueOf(id))) {
						oldLvl = ((NbtCompound)oldEnchantment).getShort(LVL_KEY);
						if (oldLvl == level) throw EXISTS_EXCEPTION;
						enchantments.remove(oldEnchantment);
					}
				}
				enchantments.add(EnchantmentHelper.createNbt(id, level));
				item.setSubNbt(ENCHANTMENTS_KEY, enchantments);

				Util.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, enchantment.getTranslationKey(), 1));
				return oldLvl;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> removeEnchantmentNode = ClientCommandManager
			.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
			.executes(context -> {
                Util.checkCanEdit(context.getSource());
				checkHasEnchantments(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				Enchantment enchantment = Util.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
				
				NbtList enchantments = item.getEnchantments();
				Identifier id = Registries.ENCHANTMENT.getKey(enchantment).get().getValue();
				int lvl;
				removeEnchantment: {
					for (NbtElement oldEnchantment : enchantments) {
						if (((NbtCompound)oldEnchantment).isEmpty()) continue;
						String oldId = ((NbtCompound)oldEnchantment).getString(ID_KEY);
						if (oldId.equals(String.valueOf(id))) {
							lvl = ((NbtCompound)oldEnchantment).getInt(LVL_KEY);
							enchantments.remove(oldEnchantment);
							item.setSubNbt(ENCHANTMENTS_KEY, enchantments);
							break removeEnchantment;
						}
					}

					throw DOESNT_EXIST_EXCEPTION;
				}

				Util.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, Text.translatable(enchantment.getTranslationKey())));
				return lvl;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
                Util.checkCanEdit(context.getSource());
				checkHasEnchantments(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				item.removeSubNbt(ENCHANTMENTS_KEY);

				Util.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> glintNode = ClientCommandManager
			.literal("glint")
			.executes(context -> {
                Util.checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();
				if (item.hasEnchantments()) {
					NbtList enchantments = item.getEnchantments();
					if (!(enchantments.size() == 1 && ((NbtCompound)enchantments.get(0)).isEmpty())) throw HAS_GLINT_EXCEPTION;
					item.removeSubNbt(ENCHANTMENTS_KEY);
				} else {
					NbtList enchantments = new NbtList();
					enchantments.add(new NbtCompound());
					item.setSubNbt(ENCHANTMENTS_KEY, enchantments);
				}

				Util.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(item.hasEnchantments() ? OUTPUT_GLINT_ENABLE : OUTPUT_GLINT_DISABLE));
				return item.hasEnchantments() ? 1 : 0;
			})
			.build();

		rootNode.addChild(node);

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