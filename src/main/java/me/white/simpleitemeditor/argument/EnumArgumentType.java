package me.white.simpleitemeditor.argument;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class EnumArgumentType<T extends Enum<T>> implements ArgumentType<T> {
    public static final SimpleCommandExceptionType INVALID_ENUM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.enum.error.invalidenum"));

    private HashMap<String, T> suggestions;

    private EnumArgumentType(Class<T> clazz, Function<T, String> formatter) {
        suggestions = new HashMap<>();

        T[] constants = clazz.getEnumConstants();
        for (T constant : constants) {
            suggestions.put(formatter.apply(constant), constant);
        }
    }

    public static <T extends Enum<T>> EnumArgumentType<T> enumArgument(Class<T> clazz) {
        return new EnumArgumentType<>(clazz, t -> t.name().toLowerCase());
    }

    public static <T extends Enum<T>> EnumArgumentType<T> enumArgument(Class<T> clazz, Function<T, String> formatter) {
        return new EnumArgumentType<>(clazz, formatter);
    }

    public static <T> T getEnum(CommandContext<FabricClientCommandSource> context, String name, Class<T> clazz) {
        return context.getArgument(name, clazz);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String remaining = reader.readString();
        for (String suggestion : suggestions.keySet()) {
            if (suggestion.equals(remaining)) return suggestions.get(suggestion);
        }
        reader.setCursor(cursor);
        throw INVALID_ENUM_EXCEPTION.createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (String suggestion : suggestions.keySet()) if (suggestion.toLowerCase(Locale.ROOT).startsWith(remaining)) builder.suggest(suggestion);
        return builder.buildFuture();
    }
}
