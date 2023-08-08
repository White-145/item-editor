package me.white.itemeditor.argument;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextArgumentType implements ArgumentType<Text> {
    public static SimpleCommandExceptionType INVALID_HEX_CHARACTER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.invalidhex"));
    public static SimpleCommandExceptionType INVALID_UNICODE_CHARACTER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.invalidunicode"));
    public static SimpleCommandExceptionType INVALID_ESCAPE_SEQUENCE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.invalidescape"));
    public static SimpleCommandExceptionType INVALID_PLACEHOLDER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.text.invalidplaceholder"));
    public static final Style EMPTY_STYLE = Style.EMPTY.withObfuscated(false).withBold(false).withStrikethrough(false).withUnderline(false).withItalic(false);

    boolean colors;
    boolean keybinds;
    boolean hover;
    boolean click;

    private TextArgumentType(boolean colors, boolean keybinds, boolean hover, boolean click) {
        this.colors = colors;
        this.keybinds = keybinds;
        this.hover = hover;
        this.click = click;
    }

    public static TextArgumentType text(boolean colors, boolean keybinds, boolean hover, boolean click) {
        return new TextArgumentType(colors, keybinds, hover, click);
    }

    public static TextArgumentType all() {
        return new TextArgumentType(true, true, true, true);
    }

    public static TextArgumentType visual() {
        return new TextArgumentType(true, true, false, false);
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
                        int color = ColorArgumentType.hex().parse(reader);
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
                        String keybind = reader.readStringUntil('>');
                        texts.add(Text.keybind(keybind).setStyle(style));
                    }
                    case '[' -> {  // translation
                        reader.skip();
                        if (!builder.isEmpty()) texts.add(Text.literal(builder.toString()).setStyle(style));
                        builder = new StringBuilder();
                        String translation = reader.readStringUntil(']');
                        texts.add(Text.translatable(translation).setStyle(style));
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
}
