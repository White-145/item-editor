package me.white.simpleitemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AlternativeArgumentType<T> implements ArgumentType<T> {
    private ArgumentType<T> argumentType;
    private Map<String, T> consts;

    private AlternativeArgumentType(ArgumentType<T> argumentType, Map<String, T> consts) {
        this.argumentType = argumentType;
        this.consts = consts;
    }

    public static <T> AlternativeArgumentType<T> argument(ArgumentType<T> argumentType) {
        return new AlternativeArgumentType<>(argumentType, Map.of());
    }

    public static <T> AlternativeArgumentType<T> argument(ArgumentType<T> argumentType, Map<String, T> consts) {
        return new AlternativeArgumentType<>(argumentType, consts);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String remaining = reader.readString().toLowerCase(Locale.ROOT);
        for (Map.Entry<String, T> entry : consts.entrySet()) {
            if (remaining.equals(entry.getKey().toLowerCase(Locale.ROOT))) {
                return entry.getValue();
            }
        }
        reader.setCursor(cursor);
        return argumentType.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String name : consts.keySet()) {
            if (name.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(name);
            }
        }
        return argumentType.listSuggestions(context, builder);
    }
}
