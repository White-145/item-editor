package me.white.simpleitemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class EnumArgumentType<T extends Enum<T>> implements ArgumentType<T> {
    private static final SimpleCommandExceptionType INVALID_ENUM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.enum.error.invalidenum"));
    private Class<T> clazz;
    private List<String> suggestions;

    private EnumArgumentType(Class<T> clazz, Function<T, String> formatter) {
        this.clazz = clazz;
        T[] consts = clazz.getEnumConstants();
        String[] suggestions = new String[consts.length];
        for (int i = 0; i < consts.length; ++i) {
            suggestions[i] = formatter.apply(consts[i]);
        }
        this.suggestions = List.of(suggestions);
    }

    public static <T extends Enum<T>> EnumArgumentType<T> enums(Class<T> clazz) {
        return new EnumArgumentType<>(clazz, value -> value.name().toLowerCase(Locale.ROOT));
    }

    public static <T extends Enum<T>> EnumArgumentType<T> enums(Class<T> clazz, Function<T, String> formatter) {
        return new EnumArgumentType<>(clazz, formatter);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String remaining = reader.readString();
        int index = suggestions.indexOf(remaining);
        if (index == -1) {
            reader.setCursor(cursor);
            throw INVALID_ENUM_EXCEPTION.createWithContext(reader);
        }
        return clazz.getEnumConstants()[index];
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
    }
}
