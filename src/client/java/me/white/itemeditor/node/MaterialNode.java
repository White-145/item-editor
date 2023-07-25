package me.white.itemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.text.Text;

public class MaterialNode {
	public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.material.error.alreadyis")).create();
	private static final String OUTPUT_GET = "commands.edit.material.get";
	private static final String OUTPUT_SET = "commands.edit.material.set";

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("material")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
                ItemStack stack = Util.getStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, stack.getItem().getName()));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<Item>> setMaterialNode = ClientCommandManager
			.argument("material", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ITEM))
			.executes(context -> {
                ItemStack stack = Util.getStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				Item item = Util.getRegistryEntryArgument(context, "material", RegistryKeys.ITEM);
				if (stack.getItem() == item) throw ALREADY_IS_EXCEPTION;
				ItemStack newStack = new ItemStack(item, stack.getCount());
				newStack.setNbt(stack.getNbt());

				Util.setStack(context.getSource(), newStack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, item.getName()));
				return 1;
			})
			.build();
			
		rootNode.addChild(node);

		// ... material get
		node.addChild(getNode);

		// ... material set <material>
		node.addChild(setNode);
		setNode.addChild(setMaterialNode);
	}
}
