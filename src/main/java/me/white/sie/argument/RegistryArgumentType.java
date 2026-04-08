package me.white.sie.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegistryArgumentType<T> implements ArgumentType<Holder<T>> {
    private static final SimpleCommandExceptionType INVALID_ENTRY_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.registry.error.invalidentry"));
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
    private final CommandBuildContext registryAccess;
    private final ResourceKey<? extends Registry<T>> registry;

    private RegistryArgumentType(ResourceKey<? extends Registry<T>> registry, CommandBuildContext registryAccess) {
        this.registry = registry;
        this.registryAccess = registryAccess;
    }

    private HolderLookup<T> getWrapper() {
        return registryAccess.lookupOrThrow(registry);
    }

    public static <T> RegistryArgumentType<T> registryEntry(ResourceKey<? extends Registry<T>> registry, CommandBuildContext registryAccess) {
        return new RegistryArgumentType<>(registry, registryAccess);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getRegistryEntry(CommandContext<?> context, String name, ResourceKey<? extends Registry<T>> registry) throws CommandSyntaxException {
        Holder.Reference<?> reference = context.getArgument(name, Holder.Reference.class);
        ResourceKey<?> registryKey = reference.key();
        if (!registryKey.registry().equals(registry.identifier())) {
            throw INVALID_ENTRY_EXCEPTION.create();
        }
        return (T)reference.value();
    }

    @Override
    public Holder<T> parse(StringReader stringReader) throws CommandSyntaxException {
        Identifier identifier = Identifier.read(stringReader);

        Optional<Holder.Reference<T>> optional = getWrapper().get(ResourceKey.create(registry, identifier));
        return optional.orElseThrow(INVALID_ENTRY_EXCEPTION::create);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SharedSuggestionProvider.suggestResource(getWrapper().listElementIds().map(ResourceKey::identifier), builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
