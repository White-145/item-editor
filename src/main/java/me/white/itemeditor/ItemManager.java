package me.white.itemeditor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class ItemManager {
	public static CommandSyntaxException NOT_CREATIVE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.notcreative")).create();
	public static CommandSyntaxException NO_ITEM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.noitem")).create();

	public static void checkCanEdit(FabricClientCommandSource context) throws CommandSyntaxException {
		checkHasCreative(context);
		checkHasItem(context);
	}

	public static void checkHasCreative(FabricClientCommandSource context) throws CommandSyntaxException {
		MinecraftClient client = context.getClient();
		if (!client.interactionManager.getCurrentGameMode().isCreative()) throw NOT_CREATIVE_EXCEPTION;
	}

	public static void checkHasItem(FabricClientCommandSource context) throws CommandSyntaxException {
		ItemStack mainhand = getItemStack(context);
		if (mainhand == null || mainhand.isEmpty()) throw NO_ITEM_EXCEPTION;
	}
	
	public static ItemStack getItemStack(FabricClientCommandSource context) {
		ItemStack item = context.getPlayer().getInventory().getMainHandStack();
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
