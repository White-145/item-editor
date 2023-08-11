package me.white.itemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

import java.util.Arrays;
import java.util.Collection;

public class Vec2ArgumentType implements ArgumentType<Vec2f> {
    public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.vec2.incomplete"));
    public static final SimpleCommandExceptionType MISSING_COORDINATE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.vec2.missing"));

    private static final String[] EXAMPLES = new String[] {
            "0 0",
            "3 10",
            "0 1000"
    };

    @Override
    public Vec2f parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        if (!reader.canRead()) throw MISSING_COORDINATE_EXCEPTION.createWithContext(reader);
        float x = reader.readFloat();
        if (!reader.canRead() || reader.read() != ' ') EditorUtil.throwWithContext(INCOMPLETE_EXCEPTION, reader, cursor);
        if (!reader.canRead()) throw MISSING_COORDINATE_EXCEPTION.createWithContext(reader);
        float y = reader.readFloat();
        return new Vec2f(x, y);
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.stream(EXAMPLES).toList();
    }

    public static Vec2ArgumentType vec2f() {
        return new Vec2ArgumentType();
    }

    public static Vec2f getVec2fArgument(CommandContext<FabricClientCommandSource> context, String key) {
        return context.getArgument(key, Vec2f.class);
    }
}
