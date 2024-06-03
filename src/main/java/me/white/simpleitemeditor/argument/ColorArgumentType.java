package me.white.simpleitemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;

public class ColorArgumentType implements ArgumentType<Integer> {
    private static final SimpleCommandExceptionType INVALID_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.color.error.invalidcolor"));
    private static final Collection<String> EXAMPLES = List.of("#FF0000", "#00bb88", "#b8b8b8");

    public static ColorArgumentType color() {
        return new ColorArgumentType();
    }

    public static Integer getColor(CommandContext<?> context, String name) {
        return context.getArgument(name, Integer.class);
    }

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead() || reader.peek() != '#') {
            throw INVALID_COLOR_EXCEPTION.createWithContext(reader);
        }
        reader.skip();
        int rgb = 0;
        if (!reader.canRead(6)) {
            throw INVALID_COLOR_EXCEPTION.createWithContext(reader);
        }
        int cursor = reader.getCursor();
        for (int i = 0; i < 6; ++i) {
            char ch = Character.toLowerCase(reader.read());
            if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'))) {
                reader.setCursor(cursor);
                throw INVALID_COLOR_EXCEPTION.createWithContext(reader);
            } else {
                rgb <<= 4;
                rgb += ch <= '9' ? ch - '0' : ch - 'a' + 10;
            }
        }
        return rgb;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
