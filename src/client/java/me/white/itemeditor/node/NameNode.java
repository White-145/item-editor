package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.Colored;
import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class NameNode {
	public static final CommandSyntaxException NO_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.noname")).create();
	private static final String OUTPUT_GET = "commands.edit.name.get";
	private static final String OUTPUT_SET = "commands.edit.name.set";
	private static final String OUTPUT_RESET = "commands.edit.name.reset";

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("name")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasName(stack)) throw NO_NAME_EXCEPTION;
				Text name = EditHelper.getName(stack);
				
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, name));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				EditHelper.setName(stack, Text.empty());

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, ""));
				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> setNameNode = ClientCommandManager
			.argument("name", StringArgumentType.greedyString())
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				Text name = Colored.of(StringArgumentType.getString(context, "name"));
				EditHelper.setName(stack, name);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, name));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
			.literal("reset")
			.executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasName(stack)) throw NO_NAME_EXCEPTION;
				EditHelper.setName(stack, null);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
				return 1;
			})
			.build();

		rootNode.addChild(node);

		// ... get
		node.addChild(getNode);

		// ... set [<name>]
		node.addChild(setNode);
		setNode.addChild(setNameNode);

		// ... reset
		node.addChild(resetNode);
	}
}
