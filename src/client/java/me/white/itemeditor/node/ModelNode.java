package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ModelNode {
	public static final CommandSyntaxException NO_MODEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.error.nomodel")).create();
	private static final String OUTPUT_GET = "commands.edit.model.get";
	private static final String OUTPUT_SET = "commands.edit.model.set";
	private static final String OUTPUT_RESET = "commands.edit.model.reset";

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("model")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemStack stack = Util.getStack(context.getSource());
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!EditHelper.hasModel(stack)) throw NO_MODEL_EXCEPTION;
				int model = EditHelper.getModel(stack);

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, model));
				return model;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				ItemStack stack = Util.getStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!EditHelper.hasModel(stack)) throw NO_MODEL_EXCEPTION;
				int old = EditHelper.getModel(stack);
				EditHelper.setModel(stack, null);

				Util.setStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
				return old;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setModelNode = ClientCommandManager
			.argument("model", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemStack stack = Util.getStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				int model = IntegerArgumentType.getInteger(context, "model");
				int old = EditHelper.getModel(stack);
				if (model == 0) {
					if (!EditHelper.hasModel(stack)) throw NO_MODEL_EXCEPTION;
					EditHelper.setModel(stack, null);

					context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
				} else {
					EditHelper.setModel(stack, model);

					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, model));
				}

				Util.setStack(context.getSource(), stack);
				return old;
			})
			.build();
		
		rootNode.addChild(node);

		// ... get
		node.addChild(getNode);

		// ... set [<model>]
		node.addChild(setNode);
		setNode.addChild(setModelNode);
	}
}
