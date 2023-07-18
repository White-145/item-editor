package me.white.itemeditor.argument;

import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class HexColorArgumentType implements ArgumentType<Integer> {
    private static final DynamicCommandExceptionType INVALID_HEX_COLOR_EXCEPTION = new DynamicCommandExceptionType((arg) -> Text.translatable("argument.hexcolor.invalid", arg));

    private static final Collection<String> EXAMPLES = List.of(
        "#FF0000",
        "#00bb88",
        "#b8b8b8"
    );

    private HexColorArgumentType() {}

    @Override
    public Integer parse(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead() || reader.peek() != '#') {
            throw INVALID_HEX_COLOR_EXCEPTION.create(reader.getRemaining());
        }
        reader.skip();
        int rgb = 0;
        if (!reader.canRead(6)) throw INVALID_HEX_COLOR_EXCEPTION.create(reader.getRemaining());
        for (int i = 0; i < 6; ++i) {
            char ch = Character.toLowerCase(reader.read());
            if (!(ch >= '0' || ch <= '9' || ch >= 'a' || ch <= 'f')) {
                throw INVALID_HEX_COLOR_EXCEPTION.create(reader.getRemaining());
            } else {
                rgb += Math.pow(16, 5 - i) * ((ch >= '0' && ch <= '9') ? (int)(ch - '0') : (int)(ch - 'a') + 10);
            }
        }
        return rgb;
    }

    public static HexColorArgumentType hexColor() {
        return new HexColorArgumentType();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static Integer getHexColor(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Integer.class);
    }
}
