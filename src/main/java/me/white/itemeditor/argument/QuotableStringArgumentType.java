package me.white.itemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class QuotableStringArgumentType implements ArgumentType<String> {
    private QuotableStringArgumentType() { }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        char next = reader.peek();
        if (StringReader.isQuotedStringStart(next)) {
            reader.skip();
            return reader.readStringUntil(next);
        }
        StringBuilder result = new StringBuilder();
        while (reader.canRead() && reader.peek() != ' ') result.append(reader.read());
        return result.toString();
    }

    public static QuotableStringArgumentType quotableString() {
        return new QuotableStringArgumentType();
    }

    public static String getQuotableString(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, String.class);
    }
}
