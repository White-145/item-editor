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
    private static final SimpleCommandExceptionType INVALID_HEX_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.color.invalidhex"));
    private static final SimpleCommandExceptionType INVALID_BLOCK_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.color.invalidblock"));

    public static final String[] BLOCK_COLORS = new String[] {
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
        BLOCK,
        HEX
    }

    private static final Collection<String> HEX_EXAMPLES = List.of(
            "#FF0000",
            "#00bb88",
            "#b8b8b8"
    );

    private static final Collection<String> BLOCK_EXAMPLES = List.of(
            "red",
            "blue",
            "brown"
    );

    private Type type;

    private ColorArgumentType(Type type) {
        this.type = type;
    }

    public static ColorArgumentType hex() {
        return new ColorArgumentType(Type.HEX);
    }

    public static ColorArgumentType block() {
        return new ColorArgumentType(Type.BLOCK);
    }

    public static Integer getColor(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        if (type == Type.HEX) {
            if (!reader.canRead() || reader.peek() != '#') {
                throw INVALID_HEX_COLOR_EXCEPTION.createWithContext(reader);
            }
            reader.skip();
            int rgb = 0;
            if (!reader.canRead(6)) throw INVALID_HEX_COLOR_EXCEPTION.createWithContext(reader);
            int cursor = reader.getCursor();
            for (int i = 0; i < 6; ++i) {
                char ch = Character.toLowerCase(reader.read());
                if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'))) {
                    reader.setCursor(cursor);
                    throw INVALID_HEX_COLOR_EXCEPTION.createWithContext(reader);
                } else {
                    rgb *= 16;
                    rgb += ch <= '9' ? ch - '0' : ch - 'a' + 10;
                }
            }
            return rgb;
        } else {
            int cursor = reader.getCursor();
            String remaining = reader.readString().toLowerCase(Locale.ROOT);
            for (int i = 0; i < BLOCK_COLORS.length; ++i) {
                String namedColor = BLOCK_COLORS[i];
                if (namedColor.equals(remaining)) return i;
            }
            reader.setCursor(cursor);
            throw INVALID_BLOCK_COLOR_EXCEPTION.createWithContext(reader);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return switch(type) {
            case HEX -> HEX_EXAMPLES;
            default -> BLOCK_EXAMPLES;
        };
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (type == Type.BLOCK) {
            String remaining = builder.getRemainingLowerCase();
            for (String namedColor : BLOCK_COLORS) if (namedColor.startsWith(remaining)) builder.suggest(namedColor);
        }
        return builder.buildFuture();
    }
}
