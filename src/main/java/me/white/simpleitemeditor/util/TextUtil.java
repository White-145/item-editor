package me.white.simpleitemeditor.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

//? if >= 1.21.6 {
import java.net.URI;
//?}
import java.util.Map;

public class TextUtil {
    private static final String SUGGESTION_COPY = "chat.copyable.copy";

    private static Style hoverShowText(Style style, Text text) {
        //? if >= 1.21.6 {
        return style.withHoverEvent(new HoverEvent.ShowText(text));
        //?} else {
        /*return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text));
        *///?}
    }

    private static Style hoverShowItem(Style style, ItemStack stack) {
        //? if >= 1.21.6 {
        return style.withHoverEvent(new HoverEvent.ShowItem(stack));
        //?} else {
        /*return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack)));
        *///?}
    }

    private static Style clickCopy(Style style, String text) {
        //? if >= 1.21.6 {
        return style.withClickEvent(new ClickEvent.CopyToClipboard(text));
        //?} else {
        /*return style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text));
        *///?}
    }

    private static Style clickOpen(Style style, String url) {
        //? if >= 1.21.6 {
        return style.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)));
        //?} else {
        /*return style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        *///?}
    }

    public static MutableText copyable(Text text, String copy) {
        return Text.empty().append(text).setStyle(clickCopy(hoverShowText(Style.EMPTY, Text.translatable(SUGGESTION_COPY)), copy).withInsertion(copy));
    }

    public static MutableText copyable(ItemStack stack) {
        String copied = Registries.ITEM.getId(stack.getItem()).toString();

        return Text.empty().append(stack.getName()).setStyle(clickCopy(hoverShowItem(Style.EMPTY, stack), copied).withInsertion(copied));
    }

    public static MutableText copyable(ItemStack stack, DynamicRegistryManager registryManager) throws CommandSyntaxException {
        StringBuilder builder = new StringBuilder(Registries.ITEM.getId(stack.getItem()).toString());
        Map<Identifier, NbtElement> components = EditorUtil.getComponents(stack, registryManager, false);
        if (!components.isEmpty()) {
            builder.append("[");
            for (Map.Entry<Identifier, NbtElement> entry : components.entrySet()) {
                if (entry.getValue() != null) {
                    builder.append(entry.getKey());
                    builder.append("=");
                    builder.append(entry.getValue().toString());
                } else {
                    builder.append("!");
                    builder.append(entry.getKey());
                }
                builder.append(",");
            }
            // sketchy join() parody
            builder.deleteCharAt(builder.length() - 1);
            builder.append("]");
        }
        String copied = builder.toString();

        return Text.empty().append(stack.getName()).setStyle(clickCopy(hoverShowItem(Style.EMPTY, stack), copied).withInsertion(copied));
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
        try {
            return Text.empty().append(url).setStyle(clickOpen(Style.EMPTY.withColor(Formatting.BLUE).withUnderline(true), url).withInsertion(url));
        } catch (IllegalArgumentException ignored) {
            return Text.empty().append(url).setStyle(Style.EMPTY.withColor(Formatting.BLUE).withUnderline(true).withInsertion(url));
        }
    }
}
