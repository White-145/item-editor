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
			.requires(MaterialNode::requirement)
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> nameNode = ClientCommandManager
			.literal("name")
			.requires(NameNode::requirement)
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> loreNode = ClientCommandManager
			.literal("lore")
			.requires(LoreNode::requirement)
			.build();
	
		LiteralCommandNode<FabricClientCommandSource> countNode = ClientCommandManager
			.literal("count")
			.requires(CountNode::requirement)
			.build();

		LiteralCommandNode<FabricClientCommandSource> modelNode = ClientCommandManager
			.literal("model")
			.requires(ModelNode::requirement)
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> enchantmentNode = ClientCommandManager
			.literal("enchantment")
			.requires(EnchantmentNode::requirement)
			.build();

		MaterialNode.register(materialNode, registryAccess);
		NameNode.register(nameNode, registryAccess);
		LoreNode.register(loreNode, registryAccess);
		CountNode.register(countNode, registryAccess);
		ModelNode.register(modelNode, registryAccess);
		EnchantmentNode.register(enchantmentNode, registryAccess);
		
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
		// TODO ... attribute ...
		// TODO ... book ...
		// TODO ... color ...
		// TODO ... durability ...
		// TODO ... firework ...
		// TODO ... get ...
		// TODO ... head ...
		// TODO ... hideflags ...
		// TODO ... data ...
		// TODO ... unbreakable ...
		// TODO ... whitelist ...
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
