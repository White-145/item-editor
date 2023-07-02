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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.text.Text;

public class ModelNode {
	private static final CommandSyntaxException NO_MODEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.nomodel")).create();
	private static final String OUTPUT_GET = "commands.edit.name.get";
	private static final String OUTPUT_SET = "commands.edit.model.set";
	private static final String OUTPUT_RESET = "commands.edit.model.reset";

	private static Feedback set(ItemStack item, int value) {
		NbtCompound nbt = item.getOrCreateNbt();
		int result = value;
		if (nbt.contains("CustomModelData", NbtElement.INT_TYPE)) {
			result = nbt.getInt("CustomModelData");
		}
		if (value == 0) {
			nbt.remove("CustomModelData");
		} else {
			nbt.put("CustomModelData", NbtInt.of(value));
		}
		item.setNbt(nbt);
		return new Feedback(item, result);
	}

	private static Feedback reset(ItemStack item) throws CommandSyntaxException {
		NbtCompound nbt = item.getOrCreateNbt();
		if (!nbt.contains("CustomModelData", NbtElement.INT_TYPE)) {
			throw NO_MODEL_EXCEPTION;
		}
		int result = nbt.getInt("CustomModelData");
		nbt.remove("CustomModelData");
		item.setNbt(nbt);
		return new Feedback(item, result);
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource());
				NbtCompound nbt = item.getNbt();
				if (nbt == null || !nbt.contains("CustomModelData")) {
					throw NO_MODEL_EXCEPTION;
				}
				int model = nbt.getInt("CustomModelData");
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, model));
				return model;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setModelNode = ClientCommandManager
			.argument("model", IntegerArgumentType.integer(0, 65535))
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				int value = IntegerArgumentType.getInteger(context, "model");
				Feedback result = set(item, value);
				EditCommand.setItemStack(context.getSource(), result.result());
				if (value == 0) {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
				} else {
					context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, value));
				}
				return result.value();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
			.literal("reset")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Feedback result = reset(item);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
				return result.value();
			})
			.build();

		// ... get
		node.addChild(getNode);

		// ... set <model>
		node.addChild(setNode);
		setNode.addChild(setModelNode);

		// ... reset
		node.addChild(resetNode);
	}
}
