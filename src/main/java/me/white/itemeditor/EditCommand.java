package me.white.itemeditor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.editnodes.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.world.GameMode;

public class EditCommand {
	public record Feedback(ItemStack result, int value) {};

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> editNode = ClientCommandManager
			.literal("edit")
			.requires(context -> context.getClient().interactionManager.getCurrentGameMode().equals(GameMode.CREATIVE))
			.build();

		LiteralCommandNode<FabricClientCommandSource> materialNode = ClientCommandManager
			.literal("material")
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> nameNode = ClientCommandManager
			.literal("name")
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> loreNode = ClientCommandManager
			.literal("lore")
			.build();
	
		LiteralCommandNode<FabricClientCommandSource> countNode = ClientCommandManager
			.literal("count")
			.build();

		LiteralCommandNode<FabricClientCommandSource> modelNode = ClientCommandManager
			.literal("model")
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> enchantmentNode = ClientCommandManager
			.literal("enchantment")
			.build();
	
		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.build();
	
		LiteralCommandNode<FabricClientCommandSource> attributeNode = ClientCommandManager
			.literal("attribute")
			.build();
			

		MaterialNode.register(materialNode, registryAccess);
		NameNode.register(nameNode, registryAccess);
		LoreNode.register(loreNode, registryAccess);
		CountNode.register(countNode, registryAccess);
		ModelNode.register(modelNode, registryAccess);
		EnchantmentNode.register(enchantmentNode, registryAccess);
		GetNode.register(getNode, registryAccess);
		AttributeNode.register(attributeNode, registryAccess);
		
		dispatcher.getRoot().addChild(editNode);
		// ... material ...
		editNode.addChild(materialNode);
		// ... name ...
		editNode.addChild(nameNode);
		// ... lore ...
		editNode.addChild(loreNode);
		// ... count ...
		editNode.addChild(countNode);
		// ... model ...
		editNode.addChild(modelNode);
		// ... enchantment ...
		editNode.addChild(enchantmentNode);
		// ... get ...
		editNode.addChild(getNode);
		// ... attribute ...
		editNode.addChild(attributeNode);
		// TODO ... book ...
		// TODO ... color ...
		// TODO ... durability ...
		// TODO ... firework ...
		// TODO ... head ...
		// TODO ... hideflags ...
		// TODO ... data ...
		// TODO ... unbreakable ...
		// TODO ... whitelist ...
		// TODO ... entity ...
	}
	
	public static ItemStack getItemStack(FabricClientCommandSource context) {
		return context.getPlayer().getInventory().getMainHandStack();
	}

	public static void setItemStack(FabricClientCommandSource context, ItemStack item) {
		var inventory = context.getPlayer().getInventory();
		int slot = inventory.selectedSlot;
		inventory.setStack(slot, item);
		inventory.updateItems();
		context.getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, item));
	}
}
