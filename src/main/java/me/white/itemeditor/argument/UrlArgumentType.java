package me.white.itemeditor.argument;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

public class UrlArgumentType implements ArgumentType<URL> {
    private static final DynamicCommandExceptionType INVALID_URL_EXCEPTION = new DynamicCommandExceptionType((arg) -> Text.translatable("argument.url.invalid", arg));

    private static final Collection<String> EXAMPLES = List.of(
        "https://example.com/",
        "example.com/",
        "http://example.com/path"
    );

    private UrlArgumentType() { }

    private String read(StringReader reader) throws CommandSyntaxException {
        if (!reader.canRead()) {
            return "";
        }
        final char next = reader.peek();
        if (StringReader.isQuotedStringStart(next)) {
            reader.skip();
            return reader.readStringUntil(next);
        }
        StringBuilder result = new StringBuilder();
        while (reader.canRead() && reader.peek() != ' ') {
            result.append(reader.read());
        }
        return result.toString();
    }

    @Override
    public URL parse(StringReader reader) throws CommandSyntaxException {
        // TODO: do better
        int cursor = reader.getCursor();
        try {
            String url = read(reader);
            if (url.isEmpty()) throw INVALID_URL_EXCEPTION.create(url);
            System.out.println(url);
            return new URL(url);
        } catch (MalformedURLException e) {
            reader.setCursor(cursor);
            throw INVALID_URL_EXCEPTION.create(reader.getRemaining());
        }
    }

    public static UrlArgumentType url() {
        return new UrlArgumentType();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static URL getUrl(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, URL.class);
    }
}
