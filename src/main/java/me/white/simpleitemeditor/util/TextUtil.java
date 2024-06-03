package me.white.simpleitemeditor.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class TextUtil {
    private static final String OUTPUT_COPY = "chat.copyable.copy";

    public static MutableText copyable(Text text, String copy) {
        return Text.empty()
                .append(text)
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(OUTPUT_COPY)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copy))
                        .withInsertion(copy)
                );
    }

    public static MutableText copyable(ItemStack stack) {
        String copied = Registries.ITEM.getId(stack.getItem()).toString();
        // TODO: components

        return Text.empty()
                .append(stack.getName())
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copied))
                        .withInsertion(copied)
                );
    }

    public static MutableText copyable(Text text) {
        return copyable(text, EditorUtil.textToString(text));
    }

    public static MutableText copyable(String str) {
        return copyable(Text.literal(str), str);
    }

    public static MutableText copyable(NbtElement nbt) {
        return copyable(NbtHelper.toPrettyPrintedText(nbt), nbt.toString());
    }

    public static MutableText copyable(Identifier id) {
        return copyable(id.toString());
    }

    public static MutableText copyable(Item item) {
        return copyable(item.getName(), Registries.ITEM.getId(item).toString());
    }

    public static MutableText url(String url) {
        return Text.empty().append(url).setStyle(Style.EMPTY.withColor(Formatting.BLUE).withUnderline(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)).withInsertion(url));
    }
}
