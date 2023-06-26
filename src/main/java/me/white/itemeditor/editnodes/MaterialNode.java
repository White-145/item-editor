package me.white.itemeditor.editnodes;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.Utils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class MaterialNode {
	private static final CommandSyntaxException NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.material.hasNbt")).create();
	private static final String OUTPUT_SET = "commands.edit.material.set";

    public static boolean requirement(FabricClientCommandSource context) {
		return true;
	}

	public static ItemStack set(ItemStack item, Item type) {
		ItemStack newItem = new ItemStack(type, item.getCount());
		newItem.setNbt(item.getNbt());
		return newItem;
	}

	public static int execute(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = Utils.getItemStack(context.getSource()).copy();
		ItemStackArgument arg = ItemStackArgumentType.getItemStackArgument(context, "value");
		// TODO: come up with a better way
		if (arg.createStack(1, false).hasNbt())
			throw NBT_EXCEPTION;
		Item type = arg.getItem();
		Utils.setItemStack(context.getSource(), set(item, type));
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, type.toString()));
		return 1;
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		ArgumentCommandNode<FabricClientCommandSource, ItemStackArgument> valueNode = ClientCommandManager
			.argument("value", ItemStackArgumentType.itemStack(registryAccess))
			.executes(MaterialNode::execute)
			.build();

		/// ... material <material>
		node.addChild(valueNode);
	}
}
