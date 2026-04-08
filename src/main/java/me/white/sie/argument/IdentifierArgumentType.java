package me.white.sie.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Collection;

public class IdentifierArgumentType implements ArgumentType<Identifier> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    private IdentifierArgumentType() { }

    public static IdentifierArgumentType identifier() {
        return new IdentifierArgumentType();
    }

    public static Identifier getIdentifier(CommandContext<?> context, String name) {
        return context.getArgument(name, Identifier.class);
    }

    @Override
    public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
        return Identifier.read(stringReader);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
