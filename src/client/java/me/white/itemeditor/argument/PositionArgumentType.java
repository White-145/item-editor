package me.white.itemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PositionArgumentType implements ArgumentType<PositionArgument> {
    public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.position.incomplete"));

    private static final String[] EXAMPLES = new String[] {
            "~ ~ ~",
            "10 10 10",
            "~10 10 ~10"
    };

    private PositionArgumentType() { }

    @Override
    public PositionArgument parse(StringReader reader) throws CommandSyntaxException {
        return PositionArgument.parse(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ArgumentType.super.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.stream(EXAMPLES).toList();
    }

    public static PositionArgumentType position() {
        return new PositionArgumentType();
    }

    public static PositionArgument getPositionArgument(CommandContext<FabricClientCommandSource> context, String key) {
        return context.getArgument(key, PositionArgument.class);
    }

    public static Vec3d getAbsolutePosition(CommandContext<FabricClientCommandSource> context, String key) {
        return context.getArgument(key, PositionArgument.class).toAbsolute(context.getSource().getPosition());
    }
}
