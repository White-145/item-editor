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
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class DurationArgumentType implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0m", "0s", "0t", "0");
    private static final SimpleCommandExceptionType INVALID_UNIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.duration.error.invalidunit"));
    private static final Dynamic2CommandExceptionType DURATION_TOO_LOW_EXCEPTION = new Dynamic2CommandExceptionType((value, minimum) -> Text.translatable("argument.duration.error.toolow", minimum, value));
    private static final String SUGGESTION_TICKS = "argument.duration.suggestionticks";
    private static final String SUGGESTION_SECONDS = "argument.duration.suggestionseconds";
    private static final String SUGGESTION_MINUTES = "argument.duration.suggestionminutes";
    private static final String SUGGESTION_HOURS = "argument.duration.suggestionhours";
    private final int min;
    private final boolean allowInfinity;

    private DurationArgumentType(int min, boolean allowInfinity) {
        this.min = min;
        this.allowInfinity = allowInfinity;
    }

    public static DurationArgumentType duration() {
        return new DurationArgumentType(0, false);
    }

    public static DurationArgumentType duration(int min) {
        return new DurationArgumentType(min, false);
    }

    public static DurationArgumentType duration(int min, boolean allowInfinity) {
        return new DurationArgumentType(min, allowInfinity);
    }

    public static Integer getDuration(CommandContext<?> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        if (allowInfinity) {
            int cursor = reader.getCursor();
            String string = reader.readString();
            if (string.toLowerCase(Locale.ROOT).equals("infinity")) {
                return -1;
            }
            reader.setCursor(cursor);
        }
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
        String remaining = builder.getRemaining();
        StringReader stringReader = new StringReader(remaining);
        try {
            stringReader.readFloat();
        } catch (CommandSyntaxException ignored) {
            if ("infinity".startsWith(remaining.toLowerCase(Locale.ROOT))) {
                builder.suggest("infinity");
            }
            return builder.buildFuture();
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
