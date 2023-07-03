package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import me.white.itemeditor.EditCommand.Feedback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class CountNode {
	private static final CommandSyntaxException OVERFLOW_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.count.overflow")).create();;
	private static final String OUTPUT_GET = "commands.edit.count.get";
	private static final String OUTPUT_SET = "commands.edit.count.set";

	private static Feedback set(ItemStack item, int count) {
		int oldCount = item.getCount();
		item.setCount(count);
		return new Feedback(item, oldCount);
	}

	private static Feedback add(ItemStack item, int count) throws CommandSyntaxException {
		if (item.getCount() + count > 127 || item.getCount() + count < 0) {
			throw OVERFLOW_EXCEPTION;
		}
		item.setCount(item.getCount() + count);
		return new Feedback(item, item.getCount());
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("count")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, item.getCount()));
				return item.getCount();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Feedback result = set(item, 1);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, 1));
				return result.value();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setCountNode = ClientCommandManager
			.argument("count", IntegerArgumentType.integer(0, 127))
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int value = IntegerArgumentType.getInteger(context, "count");
				Feedback result = set(item, value);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, value));
				return result.value();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
			.literal("add")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Feedback result = add(item, 1);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, result.result().getCount()));
				return result.value();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> addCountNode = ClientCommandManager
			.argument("count", IntegerArgumentType.integer(-126, 126))
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int value = IntegerArgumentType.getInteger(context, "count");
				Feedback result = add(item, value);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, result.result().getCount()));
				return result.value();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Feedback result = add(item, -1);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, result.result().getCount()));
				return result.value();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> removeCountNode = ClientCommandManager
			.argument("count", IntegerArgumentType.integer(-126, 126))
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int value = IntegerArgumentType.getInteger(context, "count");
				Feedback result = add(item, -value);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, result.result().getCount()));
				return result.value();
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> stackNode = ClientCommandManager
			.literal("stack")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Feedback result = set(item, item.getMaxCount());
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, item.getMaxCount()));
				return result.value();
			})
			.build();

		rootNode.addChild(node);

		// ... get
		node.addChild(getNode);

		// ... set [<count>]
		node.addChild(setNode);
		setNode.addChild(setCountNode);

		// ... add [<count>]
		node.addChild(addNode);
		addNode.addChild(addCountNode);

		// ... remove [<count>]
		node.addChild(removeNode);
		removeNode.addChild(removeCountNode);

		// ... stack
		node.addChild(stackNode);
	}
}
