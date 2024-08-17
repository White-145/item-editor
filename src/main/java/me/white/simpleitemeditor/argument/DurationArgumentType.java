package me.white.simpleitemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DurationArgumentType implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0m", "0s", "0t", "0");
    private static final SimpleCommandExceptionType INVALID_UNIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.duration.error.invalidunit"));
    private static final Dynamic2CommandExceptionType DURATION_TOO_LOW_EXCEPTION = new Dynamic2CommandExceptionType((value, minimum) -> Text.translatable("argument.duration.error.toolow", minimum, value));
    private static final String SUGGESTION_TICKS = "argument.duration.suggestionticks";
    private static final String SUGGESTION_SECONDS = "argument.duration.suggestionseconds";
    private static final String SUGGESTION_MINUTES = "argument.duration.suggestionminutes";
    private static final String SUGGESTION_HOURS = "argument.duration.suggestionhours";
    private int min;

    private DurationArgumentType(int min) {
        this.min = min;
    }

    public static DurationArgumentType duration() {
        return new DurationArgumentType(0);
    }

    public static DurationArgumentType duration(int min) {
        return new DurationArgumentType(min);
    }

    public static Integer getDuration(CommandContext<?> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        float time = reader.readFloat();
        String unitSuffix = reader.readUnquotedString();
        if (!unitSuffix.isEmpty()) {
            Unit unit = Unit.bySuffix(unitSuffix);
            if (unit == null) {
                throw INVALID_UNIT_EXCEPTION.createWithContext(reader);
            }
            time *= unit.ticks;
        }
        int ticks = Math.round(time);
        if (ticks < min) {
            throw DURATION_TOO_LOW_EXCEPTION.createWithContext(reader, ticks, min);
        }
        return ticks;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getRemaining());
        try {
            stringReader.readFloat();
        } catch (CommandSyntaxException commandSyntaxException) {
            return Suggestions.empty();
        }
        if (stringReader.readUnquotedString().isEmpty()) {
            for (Unit unit : Unit.values()) {
                builder.suggest(unit.suffix, Text.translatable(unit.suggestion));
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private enum Unit {
        TICK("t", 1, SUGGESTION_TICKS),
        SECOND("s", 20, SUGGESTION_SECONDS),
        MINUTE("m", 1200, SUGGESTION_MINUTES),
        HOUR("h", 72000, SUGGESTION_HOURS);

        final String suffix;
        final int ticks;
        final String suggestion;

        Unit(String suffix, int ticks, String suggestion) {
            this.suffix = suffix;
            this.ticks = ticks;
            this.suggestion = suggestion;
        }

        public static Unit bySuffix(String suffix) {
            for (Unit unit : Unit.values()) {
                if (unit.suffix.equals(suffix)) {
                    return unit;
                }
            }
            return null;
        }
    }
}
