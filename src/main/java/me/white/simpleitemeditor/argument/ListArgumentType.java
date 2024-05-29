package me.white.simpleitemeditor.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

public class ListArgumentType<T, U extends ArgumentType<T>> implements ArgumentType<List<T>> {
    private U argumentType;
    private char delimeter;

    private ListArgumentType(U argumentType, char delimeter) {
        this.argumentType = argumentType;
        this.delimeter = delimeter;
    }

    public static <T, U extends ArgumentType<T>> ListArgumentType<T, U> listArgument(U argumentType) {
        return new ListArgumentType<>(argumentType, ',');
    }

    public static <T, U extends ArgumentType<T>> ListArgumentType<T, U> listArgument(U argumentType, char delimeter) {
        return new ListArgumentType<>(argumentType, delimeter);
    }

    @SuppressWarnings("unchecked")
    public static <T, U extends ArgumentType<T>, S> List<T> getListArgument(CommandContext<S> context, String name) {
        return context.getArgument(name, List.class);
    }

    @Override
    public List<T> parse(StringReader reader) throws CommandSyntaxException {
        List<T> result = new ArrayList<>();
        result.add(argumentType.parse(reader));
        while (reader.canRead() && reader.peek() == delimeter) {
            reader.skip();
            result.add(argumentType.parse(reader));
        }
        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        // i can feel that this is not the best approach, but idk how to implement it differently
        String remaining = builder.getRemaining();
        int lastStart = remaining.lastIndexOf(delimeter) + 1;
        return argumentType.listSuggestions(context, new SuggestionsBuilder(builder.getInput(), builder.getStart() + lastStart));
    }
}
