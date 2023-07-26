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

public class EditorUtil {
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

	public static ItemStack getStack(FabricClientCommandSource source) {
		return source.getPlayer().getInventory().getMainHandStack();
	}

	public static void setStack(FabricClientCommandSource source, ItemStack stack) throws CommandSyntaxException {
		if (!hasCreative(source)) throw NOT_CREATIVE_EXCEPTION;
		PlayerInventory inventory = source.getPlayer().getInventory();
		int slot = inventory.selectedSlot;
		inventory.setStack(slot, stack);
		source.getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, stack));
	}

	public static String formatColor(int color) {
		return String.format("#%06X", (0xFFFFFF & color));
	}

	public static int meanColor(int[] colors) {
		int r = 0;
		int g = 0;
		int b = 0;
		for (int color : colors) {
			r += (color & 0xFF0000) >> 16;
			g += (color & 0x00FF00) >> 8;
			b += color & 0x0000FF;
		}
		return ((r / colors.length) << 16) + ((g / colors.length) << 8) + b / colors.length;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getRegistryEntryArgument(CommandContext<FabricClientCommandSource> context, String key, RegistryKey<Registry<T>> registryEntryKey) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
		RegistryKey<?> registryKey = reference.registryKey();
		if (!registryKey.isOf(registryEntryKey)) throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), registryEntryKey.getValue());
		return (T)reference.value();
	}
}
