package me.white.itemeditor.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
	public static CommandSyntaxException NOT_CREATIVE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.notcreative")).create();
	public static CommandSyntaxException NO_ITEM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.noitem")).create();

	public static void checkCanEdit(FabricClientCommandSource source) throws CommandSyntaxException {
		checkHasCreative(source);
		checkHasItem(source);
	}

	public static void checkHasCreative(FabricClientCommandSource source) throws CommandSyntaxException {
		MinecraftClient client = source.getClient();
		if (!client.interactionManager.getCurrentGameMode().isCreative()) throw NOT_CREATIVE_EXCEPTION;
	}

	public static void checkHasItem(FabricClientCommandSource source) throws CommandSyntaxException {
		ItemStack mainhand = getItemStack(source);
		if (mainhand == null || mainhand.isEmpty()) throw NO_ITEM_EXCEPTION;
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

    public static int meanColor(int[] colors) {
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int color : colors) {
            red += (color & 0xFF0000) >> 16;
            green += (color & 0x00FF00) >> 8;
            blue += (color & 0x0000FF);
        }
        return ((red / colors.length) << 16) + ((green / colors.length) << 8) + blue / colors.length;
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
