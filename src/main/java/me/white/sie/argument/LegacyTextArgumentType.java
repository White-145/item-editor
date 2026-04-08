package me.white.sie.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import 	net.minecraft.locale.Language;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LegacyTextArgumentType implements ArgumentType<Component> {
    private static final SimpleCommandExceptionType INVALID_HEX_CHARACTER_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.text.error.invalidhex"));
    private static final SimpleCommandExceptionType INVALID_UNICODE_CHARACTER_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.text.error.invalidunicode"));
    private static final SimpleCommandExceptionType INVALID_ESCAPE_SEQUENCE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.text.error.invalidescape"));
    private static final SimpleCommandExceptionType INVALID_PLACEHOLDER_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.text.error.invalidplaceholder"));
    private static final SimpleCommandExceptionType UNCLOSED_KEYBIND_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.text.error.unclosedkeybind"));
    private static final SimpleCommandExceptionType UNCLOSED_TRANSLATION_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("argument.text.error.unclosedtranslation"));
    private static final String SUGGESTION_RESET = "argument.text.suggestionreset";
    private static final String SUGGESTION_HEX = "argument.text.suggestionhex";
    private static final String SUGGESTION_SPACE = "argument.text.suggestionspace";
    private static final String SUGGESTION_KEYBIND = "argument.text.suggestionkeybind";
    private static final String SUGGESTION_TRANSLATION = "argument.text.suggestiontranslation";
    private static final Style EMPTY_STYLE = Style.EMPTY.withObfuscated(false).withBold(false).withStrikethrough(false).withUnderlined(false).withItalic(false);

    private LegacyTextArgumentType() { }

    public static LegacyTextArgumentType text() {
        return new LegacyTextArgumentType();
    }

    public static Component getText(CommandContext<?> context, String name) {
        return context.getArgument(name, Component.class);
    }

    public static char readEscaped(StringReader reader) throws CommandSyntaxException {
        if (reader.peek() == '\\') {
            reader.skip();
        }
        int cursor = reader.getCursor();
        switch (reader.peek()) {
            case 'n' -> {
                reader.skip();
                return '\n';
            }
            case 'x' -> {
                reader.skip();
                if (!reader.canRead(2)) {
                    throw INVALID_HEX_CHARACTER_EXCEPTION.createWithContext(reader);
                }
                char result = 0;
                for (int i = 0; i < 2; ++i) {
                    char ch = Character.toLowerCase(reader.read());
                    if (isHex(ch)) {
                        result <<= 4;
                        result += (char)(ch <= '9' ? ch - '0' : ch - 'a' + 10);
                    } else {
                        reader.setCursor(cursor + 1);
                        throw INVALID_HEX_CHARACTER_EXCEPTION.createWithContext(reader);
                    }
                }
                return result;
            }
            case 'u' -> {
                reader.skip();
                if (!reader.canRead(4)) {
                    throw INVALID_UNICODE_CHARACTER_EXCEPTION.createWithContext(reader);
                }
                char result = 0;
                for (int i = 0; i < 4; ++i) {
                    char ch = Character.toLowerCase(reader.read());
                    if (isHex(ch)) {
                        result <<= 4;
                        result += (char)(ch <= '9' ? ch - '0' : ch - 'a' + 10);
                    } else {
                        reader.setCursor(cursor + 1);
                        throw INVALID_UNICODE_CHARACTER_EXCEPTION.createWithContext(reader);
                    }
                }
                return result;
            }
            case '\\', '&' -> {
                return reader.read();
            }
            default -> {
                reader.setCursor(cursor);
                throw INVALID_ESCAPE_SEQUENCE_EXCEPTION.createWithContext(reader);
            }
        }
    }

    private static boolean isHex(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    private static boolean isModifier(char ch) {
        return (ch >= 'k' && ch <= 'o') || (ch >= 'K' && ch <= 'O') || ch == 'r' || ch == 'R';
    }

    private static Style modifyStyleWith(Style style, char ch) {
        return switch (ch) {
            case '0' -> EMPTY_STYLE.withColor(ChatFormatting.BLACK);
            case '1' -> EMPTY_STYLE.withColor(ChatFormatting.DARK_BLUE);
            case '2' -> EMPTY_STYLE.withColor(ChatFormatting.DARK_GREEN);
            case '3' -> EMPTY_STYLE.withColor(ChatFormatting.DARK_AQUA);
            case '4' -> EMPTY_STYLE.withColor(ChatFormatting.DARK_RED);
            case '5' -> EMPTY_STYLE.withColor(ChatFormatting.DARK_PURPLE);
            case '6' -> EMPTY_STYLE.withColor(ChatFormatting.GOLD);
            case '7' -> EMPTY_STYLE.withColor(ChatFormatting.GRAY);
            case '8' -> EMPTY_STYLE.withColor(ChatFormatting.DARK_GRAY);
            case '9' -> EMPTY_STYLE.withColor(ChatFormatting.BLUE);
            case 'a', 'A' -> EMPTY_STYLE.withColor(ChatFormatting.GREEN);
            case 'b', 'B' -> EMPTY_STYLE.withColor(ChatFormatting.AQUA);
            case 'c', 'C' -> EMPTY_STYLE.withColor(ChatFormatting.RED);
            case 'd', 'D' -> EMPTY_STYLE.withColor(ChatFormatting.LIGHT_PURPLE);
            case 'e', 'E' -> EMPTY_STYLE.withColor(ChatFormatting.YELLOW);
            case 'f', 'F' -> EMPTY_STYLE.withColor(ChatFormatting.WHITE);
            case 'k', 'K' -> style.withObfuscated(true);
            case 'l', 'L' -> style.withBold(true);
            case 'm', 'M' -> style.withStrikethrough(true);
            case 'n', 'N' -> style.withUnderlined(true);
            case 'o', 'O' -> style.withItalic(true);
            case 'r', 'R' -> EMPTY_STYLE;
            default -> style;
        };
    }

    private static int skipUntil(String str, int i, char terminator) {
        int index = str.indexOf(terminator, i);
        return index == -1 ? -1 : index - i;
    }

    @Override
    public Component parse(StringReader reader) throws CommandSyntaxException {
        List<Component> components = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        Style style = Style.EMPTY;

        while (reader.canRead()) {
            if (reader.peek() == '\\') {
                builder.append(readEscaped(reader));
            } else if (reader.peek() == '&') {
                reader.skip();

                if (!reader.canRead()) {
                    throw INVALID_PLACEHOLDER_EXCEPTION.createWithContext(reader);
                }
                switch (reader.peek()) {
                    case '#' -> {  // hex color
                        int color = ColorArgumentType.color().parse(reader);
                        if (!builder.isEmpty()) {
                            components.add(Component.literal(builder.toString()).setStyle(style));
                            builder = new StringBuilder();
                        }
                        style = EMPTY_STYLE.withColor(color);
                    }
                    case '_' -> {  // space
                        reader.skip();
                        builder.append(' ');
                    }
                    case '<' -> {  // keybind
                        reader.skip();
                        if (!builder.isEmpty()) {
                            components.add(Component.literal(builder.toString()).setStyle(style));
                            builder = new StringBuilder();
                        }
                        components.add(Component.keybind(reader.readStringUntil('>')).setStyle(style));
                    }
                    case '[' -> {  // translation
                        reader.skip();
                        if (!builder.isEmpty()) {
                            components.add(Component.literal(builder.toString()).setStyle(style));
                            builder = new StringBuilder();
                        }
                        components.add(Component.translatable(reader.readStringUntil(']')).setStyle(style));
                    }
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'k', 'l', 'm', 'n', 'o', 'r' -> {  // color code
                        if (!builder.isEmpty()) {
                            components.add(Component.literal(builder.toString()).setStyle(style));
                            builder = new StringBuilder();
                        }
                        char ch = Character.toLowerCase(reader.read());
                        style = modifyStyleWith(style, Character.toLowerCase(ch));
                    }
                    default -> {
                        throw INVALID_PLACEHOLDER_EXCEPTION.createWithContext(reader);
                    }
                }
            } else {
                builder.append(reader.read());
            }
        }
        if (!builder.isEmpty()) {
            components.add(Component.literal(builder.toString()).setStyle(style));
        }
        MutableComponent result = Component.empty();
        for (Component component : components) {
            result.append(component);
        }
        return result;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        if (remaining.isEmpty()) {
            return Suggestions.empty();
        }
        int i = remaining.lastIndexOf('&');
        if (i == remaining.length() - 1) {
            builder = builder.createOffset(builder.getStart() + builder.getRemaining().length() - 1);
            builder.suggest("&#", Component.translatable(SUGGESTION_HEX));
            builder.suggest("&_", Component.translatable(SUGGESTION_SPACE));
            builder.suggest("&<", Component.translatable(SUGGESTION_KEYBIND));
            builder.suggest("&[", Component.translatable(SUGGESTION_TRANSLATION));
            builder.suggest("&r", Component.translatable(SUGGESTION_RESET));
            return builder.buildFuture();
        }
        String last = remaining.substring(i + 2);
        switch (remaining.charAt(i + 1)) {
            case '<' -> {
                if (skipUntil(last, 0, '>') == -1) {
                    Minecraft client = Minecraft.getInstance();
                    List<String> keybinds = Arrays.stream(client.options.keyMappings).map(KeyMapping::getName).toList();
                    builder = builder.createOffset(builder.getStart() + i + 2);
                    for (String key : keybinds) {
                        if (key.startsWith(last)) {
                            builder.suggest(key);
                        }
                    }
                    return builder.buildFuture();
                }
            }
            case '[' -> {
                if (skipUntil(last, 0, ']') == -1) {
                    Minecraft client = Minecraft.getInstance();
                    Language language = Language.getInstance();
                    if (language instanceof ClientLanguage) {
                        Set<String> keys = ((ClientLanguage)language).storage.keySet();
                        builder = builder.createOffset(builder.getStart() + i);
                        for (String key : keys) {
                            if (key.startsWith(last)) {
                                builder.suggest(key);
                            }
                        }
                        return builder.buildFuture();
                    }
                }
            }
        }

        return Suggestions.empty();
    }
}
