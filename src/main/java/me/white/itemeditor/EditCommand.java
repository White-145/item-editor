package me.white.itemeditor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.editnodes.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class EditCommand {
	public static CommandSyntaxException NOT_CREATIVE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.notcreative")).create();
	public static CommandSyntaxException NO_ITEM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.noitem")).create();

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> editNode = ClientCommandManager
			.literal("edit")
			.build();

		// ... material ...
		MaterialNode.register(editNode, registryAccess);
		// ... name ...
		NameNode.register(editNode, registryAccess);
		// ... lore ...
		LoreNode.register(editNode, registryAccess);
		// ... count ...
		CountNode.register(editNode, registryAccess);
		// ... model ...
		ModelNode.register(editNode, registryAccess);
		// ... enchantment ...
		EnchantmentNode.register(editNode, registryAccess);
		// ... get ...
		GetNode.register(editNode, registryAccess);
		// ... attribute ...
		AttributeNode.register(editNode, registryAccess);
		// ... color ...
		ColorNode.register(editNode, registryAccess);
		// ... hideflags ...
		FlagsNode.register(editNode, registryAccess);
		// ... equip
		EquipNode.register(editNode, registryAccess);
		// ... unbreakable
		UnbreakableNode.register(editNode, registryAccess);
		// ... whitelist ...
		// TODO
		// ... durability ...
		// TODO
		// ... data ...
		// TODO
		// ... book ...
		// TODO
		// ... firework ...
		// TODO
		// ... head ...
		// TODO
		// ... trim ...
		// TODO
		// ... entity ...
		// TODO
		
		dispatcher.getRoot().addChild(editNode);
	}
	
	public static ItemStack getItemStack(FabricClientCommandSource context) throws CommandSyntaxException {
		MinecraftClient client = context.getClient();
		if (client.interactionManager.getCurrentGameMode() != GameMode.CREATIVE) throw NOT_CREATIVE_EXCEPTION;
		ItemStack item = context.getPlayer().getInventory().getMainHandStack();
		if (item.isEmpty()) throw NO_ITEM_EXCEPTION;
		return item;
	}

	public static void setItemStack(FabricClientCommandSource context, ItemStack item) throws CommandSyntaxException {
		MinecraftClient client = context.getClient();
		if (client.interactionManager.getCurrentGameMode() != GameMode.CREATIVE) throw NOT_CREATIVE_EXCEPTION;
		PlayerInventory inventory = context.getPlayer().getInventory();
		int slot = inventory.selectedSlot;
		inventory.setStack(slot, item);
		inventory.updateItems();
		context.getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, item));
	}
}
