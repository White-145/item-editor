package me.white.itemeditor.node;

import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.ItemManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

public class EquipNode {
	private static final String OUTPUT = "commands.edit.equip";

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("equip")
			.executes(context -> {
                ItemManager.checkCanEdit(context.getSource());

				ItemStack item = ItemManager.getItemStack(context.getSource());
				PlayerInventory inventory = context.getSource().getPlayer().getInventory();
				ItemStack headItem = inventory.getArmorStack(3);
				ItemManager.setItemStack(context.getSource(), headItem);
				inventory.setStack(39, item);
				inventory.updateItems();
				context.getSource().getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(5, item));
				context.getSource().sendFeedback(Text.translatable(OUTPUT));
				return headItem.isEmpty() ? 0 : 1;
			})
            .build();
        
        rootNode.addChild(node);
    }
}
