package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.text.Text;

public class ModelNode {
	private static final CommandSyntaxException NO_MODEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.nomodel")).create();
	private static final String OUTPUT_GET = "commands.edit.name.get";
	private static final String OUTPUT_SET = "commands.edit.model.set";
	private static final String OUTPUT_RESET = "commands.edit.model.reset";

    public static boolean requirement(FabricClientCommandSource context) {
		return true;
	}

	private static ItemStack set(ItemStack item, int value) {
		NbtCompound nbt = item.getOrCreateNbt();
		if (value == 0) {
			nbt.remove("CustomModelData");
		} else {
			nbt.put("CustomModelData", NbtInt.of(value));
		}
		item.setNbt(nbt);
		return item;
	}

	private static ItemStack reset(ItemStack item) throws CommandSyntaxException {
		NbtCompound nbt = item.getOrCreateNbt();
		if (!nbt.contains("CustomModelData", NbtElement.INT_TYPE)) {
			throw NO_MODEL_EXCEPTION;
		}
		nbt.remove("CustomModelData");
		item.setNbt(nbt);
		return item;
	}

	public static int executeGet(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource());
		NbtCompound nbt = item.getNbt();
		if (nbt == null || !nbt.contains("CustomModelData")) {
			throw NO_MODEL_EXCEPTION;
		}
		int model = nbt.getInt("CustomModelData");
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, model));
		return 1;
	}


	public static int executeSet(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		int value = IntegerArgumentType.getInteger(context, "value");
		Utils.setItemStack(context.getSource(), set(item, value));
		if (value == 0) {
			context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
		} else {
			context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, value));
		}
		return 1;
	}

	public static int executeReset(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		Utils.setItemStack(context.getSource(), reset(item));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
		return 1;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setValueNode = ClientCommandManager
			.argument("value", IntegerArgumentType.integer(0, 65535))
			.executes(ModelNode::executeSet)
			.build();

		LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
			.literal("reset")
			.executes(ModelNode::executeReset)
			.build();

		// ... set <value>
		node.addChild(setNode);
		setNode.addChild(setValueNode);

		// ... reset
		node.addChild(resetNode);
	}
}
