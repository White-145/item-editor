package me.white.itemeditor.node;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.ItemManager;
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
	public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.material.error.alreadyis")).create();
	private static final String OUTPUT_GET = "commands.edit.material.get";
	private static final String OUTPUT_SET = "commands.edit.material.set";

	private static Item getItemArgument(CommandContext<FabricClientCommandSource> context, String key) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.isOf(RegistryKeys.ITEM)) {
            throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), RegistryKeys.ITEM.getValue());
        }
		return (Item)reference.value();
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("material")
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemManager.checkHasItem(context.getSource());

				Item type = ItemManager.getItemStack(context.getSource()).getItem();
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, type.getName()));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Reference<Item>> setMaterialNode = ClientCommandManager
			.argument("material", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ITEM))
			.executes(context -> {
				ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
				Item type = getItemArgument(context, "material");
				if (item.getItem() == type) throw ALREADY_IS_EXCEPTION;
				ItemStack newItem = new ItemStack(type, item.getCount());
				newItem.setNbt(item.getNbt());
				ItemManager.setItemStack(context.getSource(), newItem);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, type.getName()));
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
