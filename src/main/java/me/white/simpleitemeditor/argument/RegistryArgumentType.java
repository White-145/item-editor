package me.white.simpleitemeditor.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RegistryArgumentType<T> implements ArgumentType<RegistryEntry<T>> {
    private static final SimpleCommandExceptionType INVALID_ENTRY_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.registry.error.invalidentry"));
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
    public static <T> T getRegistryEntry(CommandContext<?> context, String name, RegistryKey<? extends Registry<T>> registry) throws CommandSyntaxException {
        RegistryEntry.Reference<?> reference = context.getArgument(name, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.getRegistry().equals(registry.getValue())) {
            throw INVALID_ENTRY_EXCEPTION.create();
        }
        return (T)reference.value();
    }

    @Override
    public RegistryEntry<T> parse(StringReader stringReader) throws CommandSyntaxException {
        Identifier identifier = Identifier.fromCommandInput(stringReader);
        Optional<RegistryEntry.Reference<T>> optional = registryAccess.getOrThrow(registry).getOptional(RegistryKey.of(registry, identifier));
        return optional.orElseThrow(INVALID_ENTRY_EXCEPTION::create);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        DynamicRegistryManager registryManager = MinecraftClient.getInstance().world.getRegistryManager();
        CommandSource.suggestIdentifiers(registryManager.getOrThrow(registry).getIds(), builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static class Serializer implements ArgumentSerializer<RegistryArgumentType<?>, Serializer.Properties> {
        @Override
        public void writePacket(Properties properties, PacketByteBuf buf) {
            buf.writeIdentifier(properties.registry.getValue());
        }

        @Override
        public Properties fromPacket(PacketByteBuf buf) {
            Identifier value = buf.readIdentifier();
            RegistryKey<? extends Registry<?>> key = RegistryKey.ofRegistry(value);
            return new Properties(key);
        }

        @Override
        public void writeJson(Properties properties, JsonObject json) {
            json.addProperty("registry", properties.registry.getValue().toString());
        }

        @Override
        public Properties getArgumentTypeProperties(RegistryArgumentType<?> argumentType) {
            return new Properties(argumentType.registry);
        }

        public class Properties implements ArgumentTypeProperties<RegistryArgumentType<?>> {
            private RegistryKey<? extends Registry<?>> registry;

            public Properties(RegistryKey<? extends Registry<?>> registry) {
                this.registry = registry;
            }

            @SuppressWarnings("unchecked")
            @Override
            public RegistryArgumentType<?> createType(CommandRegistryAccess commandRegistryAccess) {
                return new RegistryArgumentType<>((RegistryKey<? extends Registry<Object>>)registry, commandRegistryAccess);
            }

            @Override
            public ArgumentSerializer<RegistryArgumentType<?>, ?> getSerializer() {
                return Serializer.this;
            }
        }
    }
}
