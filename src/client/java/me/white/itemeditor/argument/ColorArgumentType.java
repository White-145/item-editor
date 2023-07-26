package me.white.itemeditor.argument;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class ColorArgumentType implements ArgumentType<Integer> {
    private static final CommandSyntaxException INVALID_HEX_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.color.invalidhex")).create();
    private static final CommandSyntaxException INVALID_NAMED_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.color.invalidnamed")).create();

    public static final String[] NAMED_COLORS = new String[] {
            "white",
            "orange",
            "magenta",
            "light_blue",
            "yellow",
            "lime",
            "pink",
            "gray",
            "light_gray",
            "cyan",
            "purple",
            "blue",
            "brown",
            "green",
            "red",
            "black"
    };

    private enum Type {
        NAMED,
        HEX
    }

    private static final Collection<String> HEX_EXAMPLES = List.of(
            "#FF0000",
            "#00bb88",
            "#b8b8b8"
    );

    private static final Collection<String> NAMED_EXAMPLES = List.of(
            "red",
            "blue",
            "white"
    );

    private Type type;

    private ColorArgumentType(Type type) {
        this.type = type;
    }

    public static ColorArgumentType hex() {
        return new ColorArgumentType(Type.HEX);
    }

    public static ColorArgumentType named() {
        return new ColorArgumentType(Type.NAMED);
    }

    public static Integer getColor(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        if (type == Type.HEX) {
            if (!reader.canRead() || reader.peek() != '#') {
                throw INVALID_HEX_COLOR_EXCEPTION;
            }
            reader.skip();
            int rgb = 0;
            if (!reader.canRead(6)) throw INVALID_HEX_COLOR_EXCEPTION;
            for (int i = 0; i < 6; ++i) {
                char ch = Character.toLowerCase(reader.read());
                if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'))) {
                    throw INVALID_HEX_COLOR_EXCEPTION;
                } else {
                    rgb *= 16;
                    rgb += ch <= '9' ? ch - '0' : ch - 'a' + 10;
                }
            }
            return rgb;
        } else {
            String remaining = reader.readString().toLowerCase(Locale.ROOT);
            for (int i = 0; i < NAMED_COLORS.length; ++i) {
                String namedColor = NAMED_COLORS[i];
                if (namedColor.equals(remaining)) return i;
            }
            throw INVALID_NAMED_COLOR_EXCEPTION;
        }
    }

    @Override
    public Collection<String> getExamples() {
        return switch(type) {
            case HEX -> HEX_EXAMPLES;
            default -> NAMED_EXAMPLES;
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (type == Type.NAMED) {
            String remaining = builder.getRemainingLowerCase();
            for (String namedColor : NAMED_COLORS) if (namedColor.startsWith(remaining)) builder.suggest(namedColor);
        }
        return builder.buildFuture();
    }
}
