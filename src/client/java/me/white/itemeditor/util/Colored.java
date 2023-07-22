package me.white.itemeditor.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Colored {
	public static final Style EMPTY_STYLE = Style.EMPTY.withObfuscated(false).withBold(false).withStrikethrough(false).withUnderline(false).withItalic(false);
	public static final NbtString EMPTY_LINE = NbtString.of(Text.Serializer.toJson(Text.empty()));

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

	// Convert string of color codes into text
	// "&atext &nunderlined" -> (Text)"<green>text <underlined>underlined</underlined></green>"
	// hex: "&#FFFFFF"
	public static Text of(String str) {
		List<Text> texts = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		StringReader reader = new StringReader(str);
		Style style = Style.EMPTY;

		readerLoop: while (reader.canRead()) {
			if (reader.peek() == '\\') {
				sb.append(reader.readEscaped());
			} else if (reader.peek() == '&') {
				reader.skip();
				if (reader.peek() == '#') {
					reader.skip();
					int rgb = 0;
					for (int i = 0; i < 6; ++i) {
						if (reader.isHex()) {
							rgb += Math.pow(16, 5 - i) * reader.readHex();
						} else {
							// revert
							sb.append("&#");
							reader.skip(-i);
							continue readerLoop;
						}
					}
					texts.add(Text.literal(sb.toString()).setStyle(style));
					sb = new StringBuilder();
					style = EMPTY_STYLE.withColor(rgb);
				} else {
					char ch = Character.toLowerCase(reader.peek());
					if (reader.isHex() || (ch >= 'k' && ch <= 'o') || ch == 'r') {
						reader.skip();
						texts.add(Text.literal(sb.toString()).setStyle(style));
						sb = new StringBuilder();
						style = modifyStyleWith(style, ch);
					} else {
						// revert
						sb.append("&");
					}
				}
			} else {
				sb.append(String.valueOf(reader.read()));
			}
		}
		texts.add(Text.literal(sb.toString()).setStyle(style));
		MutableText result = Text.empty();
		for (Text part : texts) {
			result.append(part);
		}
		return result;
	}
}
