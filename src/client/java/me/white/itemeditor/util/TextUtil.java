package me.white.itemeditor.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.net.URL;

public class TextUtil {
    private static final String OUTPUT_COPY = "chat.copyable.copy";
    private static final String OUTPUT_VEC3 = "chat.copyable.vec3";
    private static final String OUTPUT_VEC2 = "chat.copyable.vec2";

    public static Text copyable(Text text, String copy) {
        return Text.empty()
                .append(text)
                .setStyle(Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(OUTPUT_COPY)))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copy))
                        .withInsertion(copy)
                );
    }

    public static Text copyable(ItemStack stack) {
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

    public static Text copyable(Text text) {
        return copyable(text, EditorUtil.textToString(text));
    }

    public static Text copyable(String str) {
        return copyable(Text.literal(str), str);
    }

    public static Text copyable(Vec3d vec) {
        String x = String.format("%.2f", vec.x);
        String y = String.format("%.2f", vec.y);
        String z = String.format("%.2f", vec.z);
        return copyable(Text.translatable(OUTPUT_VEC3, x, y, z), vec.x + " " + vec.y + " " + vec.z);
    }

    public static Text copyable(Vec2f vec) {
        String x = String.format("%.2f", vec.x);
        String y = String.format("%.2f", vec.y);
        return copyable(Text.translatable(OUTPUT_VEC2, x, y), vec.x + " " + vec.y);
    }

    public static Text copyable(NbtElement nbt) {
        return copyable(NbtHelper.toPrettyPrintedText(nbt), nbt.toString());
    }

    public static Text copyable(Identifier id) {
        return copyable(id.toString());
    }

    public static Text clickable(URL url) {
        String str = url.toString();

        return Text.empty()
                .append(url.toString())
                .setStyle(Style.EMPTY
                        .withColor(Formatting.BLUE)
                        .withUnderline(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, str))
                        .withInsertion(str)
                );
    }
}
