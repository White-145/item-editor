package me.white.itemeditor.editnodes;

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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class CountNode {
	private static final CommandSyntaxException OVERFLOW_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.count.overflow")).create();;
	private static final String OUTPUT_GET = "commands.edit.count.get";
	private static final String OUTPUT_SET = "commands.edit.count.set";

    public static boolean requirement(FabricClientCommandSource context) {
		return true;
	}

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

	public static int executeGet(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = EditCommand.getItemStack(context.getSource());
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, item.getCount()));
		return item.getCount();
	}

	public static int executeSet(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
		int value = IntegerArgumentType.getInteger(context, "value");
		Feedback result = set(item, value);
		EditCommand.setItemStack(context.getSource(), result.result());
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, value));
		return result.value();
	}

	public static int executeSetEmpty(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
		Feedback result = set(item, 1);
		EditCommand.setItemStack(context.getSource(), result.result());
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, 1));
		return result.value();
	}

	public static int executeAdd(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
		int value = IntegerArgumentType.getInteger(context, "value");
		Feedback result = add(item, value);
		EditCommand.setItemStack(context.getSource(), result.result());
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, result.result().getCount()));
		return result.value();
	}

	public static int executeAddEmpty(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
		Feedback result = add(item, 1);
		EditCommand.setItemStack(context.getSource(), result.result());
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, result.result().getCount()));
		return result.value();
	}

	public static int executeRemove(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
		int value = IntegerArgumentType.getInteger(context, "value");
		Feedback result = add(item, -value);
		EditCommand.setItemStack(context.getSource(), result.result());
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, result.result().getCount()));
		return result.value();
	}

	public static int executeRemoveEmpty(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
		Feedback result = add(item, -1);
		EditCommand.setItemStack(context.getSource(), result.result());
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, result.result().getCount()));
		return result.value();
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(CountNode::executeSetEmpty)
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setValueNode = ClientCommandManager
			.argument("value", IntegerArgumentType.integer(0, 127))
			.executes(CountNode::executeSet)
			.build();

		LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
			.literal("add")
			.executes(CountNode::executeAddEmpty)
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> addValueNode = ClientCommandManager
			.argument("value", IntegerArgumentType.integer(-126, 126))
			.executes(CountNode::executeAdd)
			.build();

		LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
			.literal("remove")
			.executes(CountNode::executeRemoveEmpty)
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> removeValueNode = ClientCommandManager
			.argument("value", IntegerArgumentType.integer(-126, 126))
			.executes(CountNode::executeRemove)
			.build();

		// ... set [<value>]
		node.addChild(setNode);
		setNode.addChild(setValueNode);

		// ... add [<value>]
		node.addChild(addNode);
		addNode.addChild(addValueNode);

		// ... remove [<value>]
		node.addChild(removeNode);
		removeNode.addChild(removeValueNode);
	}
}
