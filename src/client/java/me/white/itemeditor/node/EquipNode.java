package me.white.itemeditor.node;

import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

public class EquipNode {
	private static final String OUTPUT = "commands.edit.equip";

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("equip")
			.executes(context -> {
				ItemStack stack = Util.getStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				PlayerInventory inventory = context.getSource().getPlayer().getInventory();
				ItemStack headItem = inventory.getArmorStack(3).copy();
				
				Util.setStack(context.getSource(), headItem);
				inventory.setStack(39, stack);
				context.getSource().getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(5, stack));
				context.getSource().sendFeedback(Text.translatable(OUTPUT));
				return headItem.isEmpty() ? 0 : 1;
			})
            .build();
        
        rootNode.addChild(node);
    }
}
