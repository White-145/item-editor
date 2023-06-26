package me.white.itemeditor.editnodes;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import me.white.itemeditor.EditCommand.Feedback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.text.Text;

public class MaterialNode {
	private static final String OUTPUT_SET = "commands.edit.material.set";

    public static boolean requirement(FabricClientCommandSource context) {
		return true;
	}

	private static Item getItemArgument(CommandContext<FabricClientCommandSource> context, String key) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.isOf(RegistryKeys.ITEM)) {
            throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), RegistryKeys.ITEM.getValue());
        }
		return (Item)reference.value();
	}

	public static Feedback set(ItemStack item, Item type) {
		ItemStack newItem = new ItemStack(type, item.getCount());
		newItem.setNbt(item.getNbt());
		return new Feedback(newItem, 1);
	}

	public static int execute(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
		Item type = getItemArgument(context, "value");
		Feedback result = set(item, type);
		EditCommand.setItemStack(context.getSource(), result.result());
		context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, type.toString()));
		return result.value();
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		ArgumentCommandNode<FabricClientCommandSource, Reference<Item>> valueNode = ClientCommandManager
			.argument("value", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ITEM))
			.executes(MaterialNode::execute)
			.build();

		// ... material <material>
		node.addChild(valueNode);
	}
}
