package me.white.sie.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

import java.net.URI;
import java.util.Map;

public class TextUtil {
    private static final String SUGGESTION_COPY = "chat.copyable.copy";

    private static Style hoverShowText(Style style, Component text) {
        return style.withHoverEvent(new HoverEvent.ShowText(text));
    }

    private static Style hoverShowItem(Style style, ItemStack stack) {
        return style.withHoverEvent(new HoverEvent.ShowItem(ItemStackTemplate.fromNonEmptyStack(stack)));
    }

    private static Style clickCopy(Style style, String text) {
        return style.withClickEvent(new ClickEvent.CopyToClipboard(text));
    }

    private static Style clickOpen(Style style, String url) {
        return style.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)));
    }

    public static MutableComponent copyable(Component text, String copy) {
        return Component.empty().append(text).setStyle(clickCopy(hoverShowText(Style.EMPTY, Component.translatable(SUGGESTION_COPY)), copy).withInsertion(copy));
    }

    public static MutableComponent copyable(ItemStack stack) {
        String copied = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        return Component.empty().append(stack.getDisplayName()).setStyle(clickCopy(hoverShowItem(Style.EMPTY, stack), copied).withInsertion(copied));
    }

    public static MutableComponent copyable(ItemStack stack, RegistryAccess registryManager) throws CommandSyntaxException {
        StringBuilder builder = new StringBuilder(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        Map<Identifier, Tag> components = EditorUtil.getComponents(stack, registryManager, false);
        if (!components.isEmpty()) {
            builder.append("[");
            for (Map.Entry<Identifier, Tag> entry : components.entrySet()) {
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

        return Component.empty().append(stack.getDisplayName()).setStyle(clickCopy(hoverShowItem(Style.EMPTY, stack), copied).withInsertion(copied));
    }

    public static MutableComponent copyable(Component text) {
        return copyable(text, EditorUtil.textToString(text));
    }

    public static MutableComponent copyable(String str) {
        return copyable(Component.literal(str), str);
    }

    public static MutableComponent copyable(Tag nbt) {
        return copyable(NbtUtils.toPrettyComponent(nbt), nbt.toString());
    }

    public static MutableComponent copyable(Identifier id) {
        return copyable(id.toString());
    }

    public static MutableComponent copyable(Item item) {
        return copyable(item.getName(item.getDefaultInstance()), BuiltInRegistries.ITEM.getKey(item).toString());
    }

    public static MutableComponent url(String url) {
        try {
            return Component.empty().append(url).setStyle(clickOpen(Style.EMPTY.withColor(ChatFormatting.BLUE).withUnderlined(true), url).withInsertion(url));
        } catch (IllegalArgumentException ignored) {
            return Component.empty().append(url).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withUnderlined(true).withInsertion(url));
        }
    }
}
