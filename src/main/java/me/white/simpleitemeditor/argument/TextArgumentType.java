package me.white.simpleitemeditor.argument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

public class TextArgumentType implements ArgumentType<Text> {
    public static SimpleCommandExceptionType INVALID_HEX_CHARACTER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.error.invalidhex"));
    public static SimpleCommandExceptionType INVALID_UNICODE_CHARACTER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.error.invalidunicode"));
    public static SimpleCommandExceptionType INVALID_ESCAPE_SEQUENCE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.error.invalidescape"));
    public static SimpleCommandExceptionType INVALID_PLACEHOLDER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.error.invalidplaceholder"));
    public static SimpleCommandExceptionType UNCLOSED_KEYBIND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.error.unclosedkeybind"));
    public static SimpleCommandExceptionType UNCLOSED_TRANSLATION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.error.unclosedtranslation"));
    public static final Style EMPTY_STYLE = Style.EMPTY.withObfuscated(false).withBold(false).withStrikethrough(false).withUnderline(false).withItalic(false);
    private static final String SUGGESTION_HEX = "argument.text.suggestionhex";
    private static final String SUGGESTION_SPACE = "argument.text.suggestionspace";
    private static final String SUGGESTION_KEYBIND = "argument.text.suggestionkeybind";
    private static final String SUGGESTION_TRANSLATION = "argument.text.suggestiontranslation";
    private static final String SUGGESTION_RESET = "argument.text.suggestionreset";

    public static TextArgumentType text() {
        return new TextArgumentType();
    }

    public static Text getText(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Text.class);
    }

    public static String readEscaped(StringReader reader) throws CommandSyntaxException {
        char result;
        if (reader.peek() == '\\') reader.skip();
        int cursor = reader.getCursor();
        switch (reader.peek()) {
            case 'n' -> {
                reader.skip();
                return "\n";
            }
            case 'x' -> {
                reader.skip();
                if (!reader.canRead(2)) {
                    reader.setCursor(cursor + 1);
                    throw INVALID_HEX_CHARACTER_EXCEPTION.createWithContext(reader);
                }
                result = 0;
                for (int i = 0; i < 2; ++i) {
                    char ch = Character.toLowerCase(reader.read());
                    if (isHex(ch)) {
                        result += Math.pow(16, 1 - i) * (ch >= '0' && ch <= '9' ? ch - '0' : ch - 'a' + 10);
                    } else {
                        reader.setCursor(cursor + 1);
                        throw INVALID_HEX_CHARACTER_EXCEPTION.createWithContext(reader);
                    }
                }
                return String.valueOf(result);
            }
            case 'u' -> {
                reader.skip();
                if (!reader.canRead(4)) {
                    reader.setCursor(cursor + 1);
                    throw INVALID_UNICODE_CHARACTER_EXCEPTION.createWithContext(reader);
                }
                result = 0;
                for (int i = 0; i < 4; ++i) {
                    char ch = reader.read();
                    if (isHex(ch)) {
                        result += Math.pow(16, 3 - i) * (ch >= '0' && ch <= '9' ? ch - '0' : ch - 'a' + 10);
                    } else {
                        reader.setCursor(cursor + 1);
                        throw INVALID_UNICODE_CHARACTER_EXCEPTION.createWithContext(reader);
                    }
                }
                return String.valueOf(result);
            }
            case '\\', '&' -> {
                return String.valueOf(reader.read());
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
            case '0' -> EMPTY_STYLE.withColor(Formatting.BLACK);
            case '1' -> EMPTY_STYLE.withColor(Formatting.DARK_BLUE);
            case '2' -> EMPTY_STYLE.withColor(Formatting.DARK_GREEN);
            case '3' -> EMPTY_STYLE.withColor(Formatting.DARK_AQUA);
            case '4' -> EMPTY_STYLE.withColor(Formatting.DARK_RED);
            case '5' -> EMPTY_STYLE.withColor(Formatting.DARK_PURPLE);
            case '6' -> EMPTY_STYLE.withColor(Formatting.GOLD);
            case '7' -> EMPTY_STYLE.withColor(Formatting.GRAY);
            case '8' -> EMPTY_STYLE.withColor(Formatting.DARK_GRAY);
            case '9' -> EMPTY_STYLE.withColor(Formatting.BLUE);
            case 'a' -> EMPTY_STYLE.withColor(Formatting.GREEN);
            case 'b' -> EMPTY_STYLE.withColor(Formatting.AQUA);
            case 'c' -> EMPTY_STYLE.withColor(Formatting.RED);
            case 'd' -> EMPTY_STYLE.withColor(Formatting.LIGHT_PURPLE);
            case 'e' -> EMPTY_STYLE.withColor(Formatting.YELLOW);
            case 'f' -> EMPTY_STYLE.withColor(Formatting.WHITE);
            case 'k' -> style.withObfuscated(true);
            case 'l' -> style.withBold(true);
            case 'm' -> style.withStrikethrough(true);
            case 'n' -> style.withUnderline(true);
            case 'o' -> style.withItalic(true);
            case 'r' -> EMPTY_STYLE;
            default -> style;
        };
    }

    @Override
    public Text parse(StringReader reader) throws CommandSyntaxException {
        List<Text> texts = new ArrayList<>();
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
                        if (!builder.isEmpty()) texts.add(Text.literal(builder.toString()).setStyle(style));
                        builder = new StringBuilder();
                        style = EMPTY_STYLE.withColor(color);
                    }
                    case '_' -> {  // space
                        reader.skip();
                        builder.append(' ');
                    }
                    case '<' -> {  // keybind
                        reader.skip();
                        if (!builder.isEmpty()) texts.add(Text.literal(builder.toString()).setStyle(style));
                        builder = new StringBuilder();
                        StringBuilder keybind = new StringBuilder();
                        int start = reader.getCursor();
                        keybindReader:
                        {
                            while (reader.canRead()) {
                                char ch = reader.read();
                                if (ch == '>') break keybindReader;
                                keybind.append(ch);
                            }
                            EditorUtil.throwWithContext(UNCLOSED_KEYBIND_EXCEPTION, reader, start);
                        }
                        texts.add(Text.keybind(keybind.toString()).setStyle(style));
                    }
                    case '[' -> {  // translation
                        reader.skip();
                        if (!builder.isEmpty()) texts.add(Text.literal(builder.toString()).setStyle(style));
                        builder = new StringBuilder();
                        StringBuilder translation = new StringBuilder();
                        int start = reader.getCursor();
                        keybindReader:
                        {
                            while (reader.canRead()) {
                                char ch = reader.read();
                                if (ch == ']') break keybindReader;
                                translation.append(ch);
                            }
                            EditorUtil.throwWithContext(UNCLOSED_KEYBIND_EXCEPTION, reader, start);
                        }
                        texts.add(Text.translatable(translation.toString()).setStyle(style));
                    }
                    default -> {  // color code
                        if (!reader.canRead()) throw INVALID_PLACEHOLDER_EXCEPTION.createWithContext(reader);
                        int cursor = reader.getCursor();
                        char ch = reader.read();
                        if (!isHex(ch) && !isModifier(ch)) {
                            reader.setCursor(cursor);
                            throw INVALID_PLACEHOLDER_EXCEPTION.createWithContext(reader);
                        }
                        if (!builder.isEmpty()) texts.add(Text.literal(builder.toString()).setStyle(style));
                        builder = new StringBuilder();
                        style = modifyStyleWith(style, Character.toLowerCase(ch));
                    }
                }
            } else {
                builder.append(reader.read());
            }
        }
        if (!builder.isEmpty()) texts.add(Text.literal(builder.toString()).setStyle(style));
        MutableText result = Text.empty();
        for (Text part : texts) {
            result.append(part);
        }
        return result;
    }

    private static int readUntil(String str, int i, char terminator) {
        for (int j = i; j < str.length(); ++j) {
            if (str.charAt(j) == terminator) return j - i;
        }
        return -1;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        if (remaining.isEmpty()) return Suggestions.empty();
        for (int i = 0; i < remaining.length(); ++i) {
            if (remaining.charAt(i) == '&') {
                if (i == remaining.length() - 1) {
                    builder = builder.createOffset(builder.getStart() + builder.getRemaining().length() - 1);
                    builder.suggest("&#", Text.translatable(SUGGESTION_HEX));
                    builder.suggest("&_", Text.translatable(SUGGESTION_SPACE));
                    builder.suggest("&<", Text.translatable(SUGGESTION_KEYBIND));
                    builder.suggest("&[", Text.translatable(SUGGESTION_TRANSLATION));
                    builder.suggest("&r", Text.translatable(SUGGESTION_RESET));
                    return builder.buildFuture();
                }
                i += 2;
                String last = remaining.substring(i);
                switch (remaining.charAt(i - 1)) {
                    case '<' -> {
                        int j = readUntil(last, 0, '>');
                        if (j != -1) {
                            i = i + j;
                            continue;
                        }
                        MinecraftClient client = MinecraftClient.getInstance();
                        String[] keybinds = Arrays
                                .stream(client.options.allKeys)
                                .map(KeyBinding::getTranslationKey)
                                .toArray(String[]::new);
                        builder = builder.createOffset(builder.getStart() + i);
                        for (String key : keybinds) {
                            if (key.startsWith(last)) builder.suggest(key);
                        }
                        return builder.buildFuture();
                    }
                    case '[' -> {
                        int j = readUntil(last, 0, ']');
                        if (j != -1) {
                            i = i + j;
                            continue;
                        }
                        MinecraftClient client = MinecraftClient.getInstance();
                        Set<String> keys = ((TranslationStorage) Language.getInstance()).translations.keySet();
                        builder = builder.createOffset(builder.getStart() + i);
                        for (String key : keys) {
                            if (key.startsWith(last)) builder.suggest(key);
                        }
                        return builder.buildFuture();
                    }
                }
            }
        }

        return Suggestions.empty();
    }
}
