package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.ItemManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.text.Text;

public class ModelNode {
	private static final CommandSyntaxException NO_MODEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.error.nomodel")).create();
	private static final String OUTPUT_GET = "commands.edit.model.get";
	private static final String OUTPUT_SET = "commands.edit.model.set";
	private static final String OUTPUT_RESET = "commands.edit.model.reset";

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("model")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource());
				NbtCompound nbt = item.getNbt();
				if (nbt == null || !nbt.contains("CustomModelData")) throw NO_MODEL_EXCEPTION;
				int model = nbt.getInt("CustomModelData");
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, model));
				return model;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setModelNode = ClientCommandManager
			.argument("model", IntegerArgumentType.integer(0, 65535))
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				int value = IntegerArgumentType.getInteger(context, "model");
				NbtCompound nbt = item.getOrCreateNbt();
				int result = value;
				if (nbt.contains("CustomModelData", NbtElement.INT_TYPE)) result = nbt.getInt("CustomModelData");
				if (value == 0) {
					nbt.remove("CustomModelData");
				} else {
					nbt.put("CustomModelData", NbtInt.of(value));
				}
				item.setNbt(nbt);
				ItemManager.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(value == 0 ? Text.translatable(OUTPUT_RESET) : Text.translatable(OUTPUT_SET, value));
				return result;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
			.literal("reset")
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getOrCreateNbt();
				if (!nbt.contains("CustomModelData", NbtElement.INT_TYPE)) throw NO_MODEL_EXCEPTION;
				int result = nbt.getInt("CustomModelData");
				nbt.remove("CustomModelData");
				item.setNbt(nbt);
				ItemManager.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
				return result;
			})
			.build();
		
		rootNode.addChild(node);

		// ... get
		node.addChild(getNode);

		// ... set <model>
		node.addChild(setNode);
		setNode.addChild(setModelNode);

		// ... reset
		node.addChild(resetNode);
	}
}
