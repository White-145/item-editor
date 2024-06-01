package me.white.simpleitemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DoubleArgumentType implements ArgumentType<Double> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0", ".5", "Infinity");

    private boolean allowInf;
    private boolean allowNan;

    private DoubleArgumentType(boolean allowInf, boolean allowNan) {
        this.allowInf = allowInf;
        this.allowNan = allowNan;
    }

    public static DoubleArgumentType doubleArg() {
        return new DoubleArgumentType(false, false);
    }

    public static DoubleArgumentType doubleArg(boolean allowInf, boolean allowNan) {
        return new DoubleArgumentType(allowInf, allowNan);
    }

    public static double getDouble(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Double.class);
    }

    @Override
    public Double parse(final StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String str = reader.readString();
        if (allowInf && (str.equalsIgnoreCase("infinity") || str.equalsIgnoreCase("inf"))) {
            return Double.POSITIVE_INFINITY;
        }
        if (allowInf && (str.equalsIgnoreCase("-infinity") || str.equalsIgnoreCase("-inf"))) {
            return Double.NEGATIVE_INFINITY;
        }
        if (allowNan && str.equalsIgnoreCase("nan")) {
            return Double.NaN;
        }
        reader.setCursor(start);
        return reader.readDouble();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (allowInf) {
            builder.suggest("infinity");
            builder.suggest("-infinity");
        }
        if (allowNan) {
            builder.suggest("nan");
        }
        return builder.buildFuture();
    }
}
