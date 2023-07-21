package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public class ModelNode {
	public static final CommandSyntaxException NO_MODEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.error.nomodel")).create();
	private static final String OUTPUT_GET = "commands.edit.model.get";
	private static final String OUTPUT_SET = "commands.edit.model.set";
	private static final String OUTPUT_RESET = "commands.edit.model.reset";
	private static final String CUSTOM_MODEL_DATA_KEY = "CustomModelData";

	private static void checkHasModel(FabricClientCommandSource source) throws CommandSyntaxException {
		ItemStack item = ItemUtil.getItemStack(source);
		if (!item.hasNbt()) throw NO_MODEL_EXCEPTION;
		NbtCompound nbt = item.getNbt();
		if (!nbt.contains(CUSTOM_MODEL_DATA_KEY, NbtElement.INT_TYPE)) throw NO_MODEL_EXCEPTION;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("model")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemUtil.checkHasItem(context.getSource());
				checkHasModel(context.getSource());

				int model = ItemUtil.getItemStack(context.getSource()).getNbt().getInt(CUSTOM_MODEL_DATA_KEY);

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, model));
				return model;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				checkHasModel(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getNbt();
				int old = nbt.getInt(CUSTOM_MODEL_DATA_KEY);
				nbt.remove(CUSTOM_MODEL_DATA_KEY);
				item.setNbt(nbt);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
				return old;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> setModelNode = ClientCommandManager
			.argument("model", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int model = IntegerArgumentType.getInteger(context, "model");

				if (model == 0) {
					checkHasModel(context.getSource());

					NbtCompound nbt = item.getNbt();
					int old = nbt.getInt(CUSTOM_MODEL_DATA_KEY);
					nbt.remove(CUSTOM_MODEL_DATA_KEY);
					item.setNbt(nbt);

					ItemUtil.setItemStack(context.getSource(), item);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
					return old;
				}
				NbtCompound nbt = item.getOrCreateNbt();
				int old = nbt.getInt(CUSTOM_MODEL_DATA_KEY);
				nbt.putInt(CUSTOM_MODEL_DATA_KEY, model);
				item.setNbt(nbt);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, model));
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
