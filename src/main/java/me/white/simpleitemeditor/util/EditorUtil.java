package me.white.simpleitemeditor.util;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import me.white.simpleitemeditor.SimpleItemEditor;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Optional;

public class EditorUtil {
    public static final CommandSyntaxException NOT_CREATIVE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.notcreative")).create();
    public static final CommandSyntaxException NO_ITEM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.noitem")).create();
    public static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((index, size) -> Text.translatable("commands.edit.error.outofbounds", index, size));

    public static boolean hasItem(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    public static boolean hasCreative(FabricClientCommandSource source) {
        MinecraftClient client = source.getClient();
        return client.interactionManager.getCurrentGameMode().isCreative();
    }

    public static ItemStack getSecondaryStack(FabricClientCommandSource source) {
        return source.getPlayer().getOffHandStack();
    }

    public static void setSecondaryStack(FabricClientCommandSource source, ItemStack stack) throws CommandSyntaxException {
        if (getSecondaryStack(source) == stack) {
            SimpleItemEditor.LOGGER.warn("Using setSecondaryStack without clonning result of getSecondaryStack (If you see it report to github pls)");
        }
        if (!hasCreative(source)) {
            throw NOT_CREATIVE_EXCEPTION;
        }
        PlayerInventory inventory = source.getPlayer().getInventory();
        inventory.setStack(40, stack);
        source.getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(45, stack));
    }

    public static ItemStack getStack(FabricClientCommandSource source) {
        return source.getPlayer().getMainHandStack();
    }

    public static void setStack(FabricClientCommandSource source, ItemStack stack) throws CommandSyntaxException {
        if (getStack(source) == stack) {
            SimpleItemEditor.LOGGER.warn("Using setStack without clonning result of getStack (If you see it report to github pls)");
        }
        if (!hasCreative(source)) {
            throw NOT_CREATIVE_EXCEPTION;
        }
        PlayerInventory inventory = source.getPlayer().getInventory();
        int slot = inventory.selectedSlot;
        inventory.setStack(slot, stack);
        source.getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, stack));
    }

    public static String formatColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static int meanColor(int[] colors) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int color : colors) {
            r += (color & 0xFF0000) >> 16;
            g += (color & 0x00FF00) >> 8;
            b += color & 0x0000FF;
        }
        return ((r / colors.length) << 16) + ((g / colors.length) << 8) + b / colors.length;
    }

    public static String textToString(Text text) {
        StringBuilder result = new StringBuilder();
        Style[] prevStyle = new Style[]{ Style.EMPTY };
        text.visit((style, literal) -> {
            if (literal.isEmpty()) return Optional.empty();
            if (style != null) {
                TextColor color = style.getColor();

                if (!prevStyle[0].equals(style)) {
                    result.append("&");
                    if (color == null) {
                        result.append("r");
                    } else if (color.getName().startsWith("#")) {
                        result.append(color.getName());
                    } else {
                        result.append(Formatting.byName(color.getName()).getCode());
                    }
                }
                if (style.isObfuscated()) result.append("&k");
                if (style.isBold()) result.append("&l");
                if (style.isStrikethrough()) result.append("&m");
                if (style.isUnderlined()) result.append("&n");
                if (style.isItalic()) result.append("&o");

                prevStyle[0] = style;
            }
            result.append(literal);
            return Optional.empty();
        }, Style.EMPTY);
        return result.toString();
    }

    public static void throwWithContext(SimpleCommandExceptionType exception, StringReader reader, int cursor) throws CommandSyntaxException {
        reader.setCursor(cursor);
        throw exception.createWithContext(reader);
    }
}
