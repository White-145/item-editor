package me.white.itemeditor.argument;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TextArgumentType implements ArgumentType<Text> {
    public static DynamicCommandExceptionType INVALID_HEX_CHARACTER_EXCEPTION = new DynamicCommandExceptionType(ch -> Text.translatable("argument.text.invalidhex", ch));
    public static DynamicCommandExceptionType INVALID_UNICODE_CHARACTER_EXCEPTION = new DynamicCommandExceptionType(ch -> Text.translatable("argument.text.invalidunicode", ch));
    public static DynamicCommandExceptionType INVALID_ESCAPE_SEQUENCE_EXCEPTION = new DynamicCommandExceptionType(ch -> Text.translatable("argument.text.invalidescape", ch));
    public static DynamicCommandExceptionType INVALID_COLOR_EXCEPTION = new DynamicCommandExceptionType(ch -> Text.translatable("argument.text.invalidcolor", ch));
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
        switch (reader.peek()) {
            case 'n':
                reader.skip();
                return "\n";
            case 'x':
                reader.skip();
                result = 0;
                for (int i = 0; i < 2; ++i) {
                    char ch = Character.toLowerCase(reader.read());
                    if (isHex(ch)) {
                        result += Math.pow(16, 1 - i) * (ch >= '0' && ch <= '9' ? ch - '0' : ch - 'a' + 10);
                    } else {
                        throw INVALID_HEX_CHARACTER_EXCEPTION.create(ch);
                    }
                }
                return String.valueOf(result);
            case 'u':
                reader.skip();
                result = 0;
                for (int i = 0; i < 4; ++i) {
                    char ch = reader.read();
                    if (isHex(ch)) {
                        result += Math.pow(16, 3 - i) * (ch >= '0' && ch <= '9' ? ch - '0' : ch - 'a' + 10);
                    } else {
                        throw INVALID_UNICODE_CHARACTER_EXCEPTION.create(ch);
                    }
                }
                return String.valueOf(result);
            case '\\':
            case '&':
                return String.valueOf(reader.read());
            default:
                throw INVALID_ESCAPE_SEQUENCE_EXCEPTION.create(reader.read());
        }
    }

    private static boolean isHex(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
    }

    private static boolean isModifier(char ch) {
        return (ch >= 'k' && ch <= 'o') || (ch >= 'K' && ch <= 'O') || ch == 'r' || ch == 'R';
    }

    private static Style modifyStyleWith(Style style, char ch) {
		switch (ch) {
			case '0':
				return EMPTY_STYLE.withColor(Formatting.BLACK);
			case '1':
				return EMPTY_STYLE.withColor(Formatting.DARK_BLUE);
			case '2':
				return EMPTY_STYLE.withColor(Formatting.DARK_GREEN);
			case '3':
				return EMPTY_STYLE.withColor(Formatting.DARK_AQUA);
			case '4':
				return EMPTY_STYLE.withColor(Formatting.DARK_RED);
			case '5':
				return EMPTY_STYLE.withColor(Formatting.DARK_PURPLE);
			case '6':
				return EMPTY_STYLE.withColor(Formatting.GOLD);
			case '7':
				return EMPTY_STYLE.withColor(Formatting.GRAY);
			case '8':
				return EMPTY_STYLE.withColor(Formatting.DARK_GRAY);
			case '9':
				return EMPTY_STYLE.withColor(Formatting.BLUE);
			case 'a':
				return EMPTY_STYLE.withColor(Formatting.GREEN);
			case 'b':
				return EMPTY_STYLE.withColor(Formatting.AQUA);
			case 'c':
				return EMPTY_STYLE.withColor(Formatting.RED);
			case 'd':
				return EMPTY_STYLE.withColor(Formatting.LIGHT_PURPLE);
			case 'e':
				return EMPTY_STYLE.withColor(Formatting.YELLOW);
			case 'f':
				return EMPTY_STYLE.withColor(Formatting.WHITE);
			case 'k':
				return style.withObfuscated(true);
			case 'l':
				return style.withBold(true);
			case 'm':
				return style.withStrikethrough(true);
			case 'n':
				return style.withUnderline(true);
			case 'o':
				return style.withItalic(true);
			case 'r':
				return EMPTY_STYLE;
			default:
				return style;
		}
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
                
                switch (reader.peek()) {
                    case '#': {
                        int color = ColorArgumentType.hex().parse(reader);
                        texts.add(Text.literal(builder.toString()).setStyle(style));
                        builder = new StringBuilder();
                        style = EMPTY_STYLE.withColor(color);
                        break;
                    }
                    case '_': {
                        builder.append(' ');
                        break;
                    }
                    default: {
                        char ch = reader.read();
                        if (!isHex(ch) && !isModifier(ch)) throw INVALID_COLOR_EXCEPTION.create(ch);
                        texts.add(Text.literal(builder.toString()).setStyle(style));
                        builder = new StringBuilder();
                        style = modifyStyleWith(style, Character.toLowerCase(ch));
                        break;
                    }
                }
			} else {
				builder.append(reader.read());
			}
		}
		texts.add(Text.literal(builder.toString()).setStyle(style));
		MutableText result = Text.empty();
		for (Text part : texts) {
			result.append(part);
		}
		return result;
    }
}
