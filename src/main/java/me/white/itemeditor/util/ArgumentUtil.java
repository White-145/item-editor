package me.white.itemeditor.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class ArgumentUtil {
    @SuppressWarnings("unchecked")
    public static <T> T getRegistryEntryArgument(CommandContext<FabricClientCommandSource> context, String key, RegistryKey<Registry<T>> registryEntryKey) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.isOf(registryEntryKey)) throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), registryEntryKey.getValue());
		return (T)reference.value();
	}
}
