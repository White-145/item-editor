package me.white.simpleitemeditor.util;

import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class TextUtil {
    private static final String SUGGESTION_COPY = "chat.copyable.copy";

    public static MutableText copyable(Text text, String copy) {
        return Text.empty().append(text).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable(SUGGESTION_COPY))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copy)).withInsertion(copy));
    }

    public static MutableText copyable(ItemStack stack, CommandRegistryAccess registryAccess) {
//        ItemStack defaultStack = stack.getItem().getDefaultStack();
//        Map<Identifier, NbtElement> components = new HashMap<>();
//        for (Component<?> component : stack.getComponents()) {
//            DataComponentType<?> componentType = component.type();
//            Identifier id = Registries.DATA_COMPONENT_TYPE.getId(componentType);
//            NbtElement element = ComponentNode.getFromComponent(stack, componentType, registryAccess);
//            if (!defaultStack.contains(componentType)) {
//                components.put(id, element);
//            } else {
//                NbtElement defaultElement = ComponentNode.getFromComponent(defaultStack, componentType, registryAccess);
//                if (!element.equals(defaultElement)) {
//                    components.put(id, element);
//                }
//            }
//        }
//        if (!components.isEmpty()) {
//            builder.append("[");
//            for (Map.Entry<Identifier, NbtElement> entry : components.entrySet()) {
//                builder.append(entry.getKey());
//                builder.append("=");
//                builder.append(entry.getValue().toString());
//                builder.append(",");
//            }
//            builder.deleteCharAt(builder.length() - 1);
//            builder.append("]");
//        }
        String copied = Registries.ITEM.getId(stack.getItem()).toString();

        return Text.empty().append(stack.getName()).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackContent(stack))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copied)).withInsertion(copied));
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
