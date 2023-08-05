package me.white.itemeditor.util;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.net.URL;

public class TextUtil {
    private static final String OUTPUT_COPY = "chat.copyable.copy";

    public static Text itemStackComponent(ItemStack stack) {
        String copied = Registries.ITEM.getId(stack.getItem()).toString();
        if (stack.hasNbt()) copied += stack.getNbt().toString();

        return Text.empty()
                .append(stack.getName())
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copied))
                        .withInsertion(copied)
                );
    }

    public static Text copyableTextComponent(Text text) {
        String copied = EditorUtil.textToString(text);

        return Text.empty()
                .append(text)
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(OUTPUT_COPY)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copied))
                        .withInsertion(copied)
                );
    }

    public static Text copyableTextComponent(String str) {
        return Text.empty()
                .append(str)
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(OUTPUT_COPY)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, str))
                        .withInsertion(str)
                );
    }

    public static Text urlComponent(URL url) {
        String copied = url.toString();

        return Text.empty()
                .append(url.toString())
                .setStyle(Style.EMPTY
                        .withUnderline(true)
                        .withColor(Formatting.BLUE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, copied))
                        .withInsertion(copied)
                );
    }
}
