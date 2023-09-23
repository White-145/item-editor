package me.white.simpleitemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.argument.TextArgumentType;
import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class NameNode implements Node {
	public static final CommandSyntaxException NO_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.noname")).create();
	public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.alreadyis")).create();
	private static final String OUTPUT_GET = "commands.edit.name.get";
	private static final String OUTPUT_SET = "commands.edit.name.set";
	private static final String OUTPUT_RESET = "commands.edit.name.reset";

	public NameNode() { }

	public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
				.literal("name")
				.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
				.literal("get")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource());
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!ItemUtil.hasName(stack)) throw NO_NAME_EXCEPTION;
					Text name = ItemUtil.getName(stack);

					context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(name)));
					return 1;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
				.literal("set")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (Text.empty().equals(ItemUtil.getName(stack))) throw ALREADY_IS_EXCEPTION;
					ItemUtil.setName(stack, Text.empty());

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, ""));
					return 1;
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Text> setNameNode = ClientCommandManager
				.argument("name", TextArgumentType.text())
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					Text name = TextArgumentType.getText(context, "name");
					if (name.equals(ItemUtil.getName(stack))) throw ALREADY_IS_EXCEPTION;
					ItemUtil.setName(stack, name);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(name)));
					return 1;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
				.literal("reset")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!ItemUtil.hasName(stack)) throw NO_NAME_EXCEPTION;
					ItemUtil.setName(stack, null);

					EditorUtil.setStack(context.getSource(), stack);
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
