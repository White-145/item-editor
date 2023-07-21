package me.white.itemeditor.argument;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.google.common.base.Function;
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
    public static final CommandSyntaxException INVALID_ENUM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("arguments.enum.invalidenum")).create();

    private HashMap<String, T> suggestions;

    private EnumArgumentType(Class<T> clazz, Function<String, String> formatter) {
        suggestions = new HashMap<>();

        T[] constants = clazz.getEnumConstants();
        for (T constant : constants) {
            suggestions.put(formatter.apply(constant.name()), constant);
        }
    }

    public static <T extends Enum<T>> EnumArgumentType<T> enumArgument(Class<T> clazz) {
        return new EnumArgumentType<T>(clazz, (name) -> name.toLowerCase());
    }

    public static <T extends Enum<T>> EnumArgumentType<T> enumArgument(Class<T> clazz, Function<String, String> formatter) {
        return new EnumArgumentType<T>(clazz, formatter);
    }

    public static <T> T getEnum(CommandContext<FabricClientCommandSource> context, String name, Class<T> clazz) {
        return context.getArgument(name, clazz);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        String remaining = reader.readString();
        for (String suggestion : suggestions.keySet()) {
            if (suggestion.equals(remaining)) return suggestions.get(suggestion);
        }
        throw INVALID_ENUM_EXCEPTION;
    }
    
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();
        for (String suggestion : suggestions.keySet()) if (suggestion.toLowerCase(Locale.ROOT).startsWith(remaining)) builder.suggest(suggestion);
        return builder.buildFuture();
    }
}
