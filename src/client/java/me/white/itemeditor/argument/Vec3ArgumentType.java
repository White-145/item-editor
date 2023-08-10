package me.white.itemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class Vec3ArgumentType implements ArgumentType<Vec3d> {
    public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.position.incomplete"));

    private static final String[] EXAMPLES = new String[] {
            "0 0 0",
            "3 10 -3",
            "0 1000 0"
    };

    @Override
    public Vec3d parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        double x = reader.readDouble();
        if (!reader.canRead() || reader.read() != ' ') EditorUtil.throwWithContext(INCOMPLETE_EXCEPTION, reader, cursor);
        double y = reader.readDouble();
        if (!reader.canRead() || reader.read() != ' ') EditorUtil.throwWithContext(INCOMPLETE_EXCEPTION, reader, cursor);
        double z = reader.readDouble();
        return new Vec3d(x, y, z);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ArgumentType.super.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.stream(EXAMPLES).toList();
    }

    public static Vec3ArgumentType vec3d() {
        return new Vec3ArgumentType();
    }

    public static Vec3d getVec3dArgument(CommandContext<FabricClientCommandSource> context, String key) {
        return context.getArgument(key, Vec3d.class);
    }
}
