package me.white.simpleitemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class InfiniteDoubleArgumentType implements ArgumentType<Double> {
    private static final Collection<String> EXAMPLES = List.of(
            "49.5",
            "infinity",
            "-.98"
    );
    private final double min;
    private final double max;

    private InfiniteDoubleArgumentType(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public static InfiniteDoubleArgumentType infiniteDouble() {
        return new InfiniteDoubleArgumentType(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public static InfiniteDoubleArgumentType infiniteDoubleArgumentType(double min) {
        return new InfiniteDoubleArgumentType(min, Double.POSITIVE_INFINITY);
    }

    public static InfiniteDoubleArgumentType infiniteDoubleArgumentType(double min, double max) {
        return new InfiniteDoubleArgumentType(min, max);
    }

    @Override
    public Double parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String string = reader.readString();
        if (max == Double.POSITIVE_INFINITY && string.toLowerCase(Locale.ROOT).equals("infinity")) {
            return Double.POSITIVE_INFINITY;
        }
        if (min == Double.NEGATIVE_INFINITY && string.toLowerCase(Locale.ROOT).equals("-infinity")) {
            return Double.NEGATIVE_INFINITY;
        }
        reader.setCursor(cursor);
        double value = reader.readDouble();
        if (min != Double.NEGATIVE_INFINITY && value < min) {
            reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooLow().createWithContext(reader, value, min);
        }
        if (max != Double.POSITIVE_INFINITY && value > max) {
            reader.setCursor(cursor);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooHigh().createWithContext(reader, value, max);
        }
        return value;
    }

    public static double getInfiniteDouble(CommandContext<?> context, String name) {
        return context.getArgument(name, Double.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        if (max == Double.POSITIVE_INFINITY && "infinity".startsWith(remaining)) {
            builder.suggest("infinity");
        }
        if (min == Double.NEGATIVE_INFINITY && "-infinity".startsWith(remaining)) {
            builder.suggest("-infinity");
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
