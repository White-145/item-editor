package me.white.itemeditor.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class Util {
	public static final CommandSyntaxException NOT_CREATIVE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.notcreative")).create();
	public static final CommandSyntaxException NO_ITEM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.noitem")).create();
	public static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((index, size) -> Text.translatable("commands.edit.error.outofbounds", index, size));

	public static boolean hasItem(ItemStack stack) {
		return stack != null && !stack.isEmpty();
	}

	public static boolean hasCreative(FabricClientCommandSource source) {
		MinecraftClient client = source.getClient();
		return client.interactionManager.getCurrentGameMode().isCreative();
	}
	
	public static ItemStack getItemStack(FabricClientCommandSource source) {
		ItemStack item = source.getPlayer().getInventory().getMainHandStack();
		return item;
	}

	public static void setItemStack(FabricClientCommandSource source, ItemStack item) throws CommandSyntaxException {
		MinecraftClient client = source.getClient();
		if (client.interactionManager.getCurrentGameMode() != GameMode.CREATIVE) throw NOT_CREATIVE_EXCEPTION;
		PlayerInventory inventory = source.getPlayer().getInventory();
		int slot = inventory.selectedSlot;
		inventory.setStack(slot, item);
		inventory.updateItems();
		source.getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, item));
	}

    public static String formatColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

	@SuppressWarnings("unchecked")
	public static <T> T getRegistryEntryArgument(CommandContext<FabricClientCommandSource> source, String key, RegistryKey<Registry<T>> registryEntryKey) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = source.getArgument(key, RegistryEntry.Reference.class);
	    RegistryKey<?> registryKey = reference.registryKey();
	    if (!registryKey.isOf(registryEntryKey)) throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), registryEntryKey.getValue());
		return (T)reference.value();
	}
}
