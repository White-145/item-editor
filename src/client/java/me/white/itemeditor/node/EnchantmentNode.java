package me.white.itemeditor.node;

import java.util.HashMap;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EnchantmentNode {
	public static final CommandSyntaxException ALREADY_EXISTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.alreadyexists")).create();
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

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("enchantment")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemStack stack = Util.getStack(context.getSource());
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasEnchantments(stack, true)) throw NO_ENCHANTMENTS_EXCEPTION;

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
				HashMap<Enchantment, Integer> enchantments = EditHelper.getEnchantments(stack);
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
				ItemStack stack = Util.getStack(context.getSource());
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasEnchantments(stack, true)) throw NO_ENCHANTMENTS_EXCEPTION;
				Enchantment enchantment = Util.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
				HashMap<Enchantment, Integer> enchantments = EditHelper.getEnchantments(stack);
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
				ItemStack stack = Util.getStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				Enchantment enchantment = Util.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
				HashMap<Enchantment, Integer> enchantments = EditHelper.getEnchantments(stack);
				Integer old = null;
				if (enchantments.containsKey(enchantment)) old = enchantments.get(enchantment);
				if (old != null && old.equals(1)) throw ALREADY_EXISTS_EXCEPTION;
				enchantments.put(enchantment, 1);
				EditHelper.setEnchantments(stack, enchantments);

				Util.setStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, enchantment.getName(1)));
				return old == null ? 0 : old;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setEnchantmentLevelNode = ClientCommandManager
			.argument("level", IntegerArgumentType.integer(0, 255))
			.executes(context -> {
				ItemStack stack = Util.getStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				Enchantment enchantment = Util.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
				HashMap<Enchantment, Integer> enchantments = EditHelper.getEnchantments(stack);
				int level = IntegerArgumentType.getInteger(context, "level");
				Integer old = null;
				if (enchantments.containsKey(enchantment)) old = enchantments.get(enchantment);
				if (old != null && old.equals(level)) throw ALREADY_EXISTS_EXCEPTION;
				enchantments.put(enchantment, level);
				EditHelper.setEnchantments(stack, enchantments);

				Util.setStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, enchantment.getName(level)));
				return old == null ? 0 : old;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> removeEnchantmentNode = ClientCommandManager
			.argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
			.executes(context -> {
				ItemStack stack = Util.getStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!EditHelper.hasEnchantments(stack)) throw NO_ENCHANTMENTS_EXCEPTION;
				Enchantment enchantment = Util.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
				HashMap<Enchantment, Integer> enchantments = EditHelper.getEnchantments(stack);
				if (!enchantments.containsKey(enchantment)) throw DOESNT_EXIST_EXCEPTION;
				int old = enchantments.get(enchantment);
				enchantments.remove(enchantment);
				EditHelper.setEnchantments(stack, enchantments);

				Util.setStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, Text.translatable(enchantment.getTranslationKey())));
				return old;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				ItemStack stack = Util.getStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!EditHelper.hasEnchantments(stack)) throw NO_ENCHANTMENTS_EXCEPTION;
				int old = EditHelper.getEnchantments(stack).size();
				EditHelper.setEnchantments(stack, null);

				Util.setStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
				return old;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> glintNode = ClientCommandManager
			.literal("glint")
			.executes(context -> {
				ItemStack stack = Util.getStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (EditHelper.hasEnchantments(stack, true)) throw HAS_GLINT_EXCEPTION;
				boolean hasGlint = EditHelper.hasEnchantments(stack);
				EditHelper.setEnchantmentGlint(stack, !hasGlint);

				Util.setStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(hasGlint ? OUTPUT_GLINT_DISABLE : OUTPUT_GLINT_ENABLE));
				return 1;
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
