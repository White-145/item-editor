package me.white.simpleitemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class RegistryArgumentType<T> implements ArgumentType<RegistryEntry<T>> {
    public static final SimpleCommandExceptionType INVALID_ENTRY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.registryargument.error.invalidentry"));
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private final CommandRegistryAccess registryAccess;
    private final RegistryKey<? extends Registry<T>> registry;

    private RegistryArgumentType(RegistryKey<? extends Registry<T>> registry, CommandRegistryAccess registryAccess) {
        this.registry = registry;
        this.registryAccess = registryAccess;
    }

    public static <T> RegistryArgumentType<T> registryEntry(RegistryKey<? extends Registry<T>> registry, CommandRegistryAccess registryAccess) {
        return new RegistryArgumentType<>(registry, registryAccess);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getRegistryEntry(CommandContext<FabricClientCommandSource> context, RegistryKey<? extends Registry<T>> registry, String name) throws CommandSyntaxException {
        RegistryEntry.Reference<?> reference = context.getArgument(name, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.getRegistry().equals(registry.getValue())) {
            throw INVALID_ENTRY_EXCEPTION.create();
        }
        return (T) reference.value();
    }

    @Override
    public RegistryEntry<T> parse(StringReader stringReader) throws CommandSyntaxException {
        Identifier identifier = Identifier.fromCommandInput(stringReader);
        return registryAccess.getWrapperOrThrow(registry).getOrThrow(RegistryKey.of(registry, identifier));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        DynamicRegistryManager registryManager = MinecraftClient.getInstance().world.getRegistryManager();
        CommandSource.suggestIdentifiers(registryManager.get(registry).getIds(), builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
