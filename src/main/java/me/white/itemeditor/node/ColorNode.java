package me.white.itemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.HexColorArgumentType;
import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.DyeableHorseArmorItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.LingeringPotionItem;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public class ColorNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.cannotedit")).create();
    public static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.nocolor")).create();
    private static final String OUTPUT_GET = "commands.edit.color.get";
    private static final String OUTPUT_SET = "commands.edit.color.set";
    private static final String OUTPUT_RESET = "commands.edit.color.reset";
    private static final String CUSTOM_POTION_COLOR_KEY = "CustomPotionColor";
    private static final String DISPLAY_KEY = "display";
    private static final String COLOR_KEY = "color";
    private static final String MAP_COLOR_KEY = "MapColor";

    private static void checkCanEdit(FabricClientCommandSource context) throws CommandSyntaxException {
        Item type = ItemUtil.getItemStack(context).getItem();
        if (!(
            type instanceof DyeableArmorItem ||
            type instanceof DyeableHorseArmorItem ||
            type instanceof TippedArrowItem ||
            type instanceof PotionItem ||
            type instanceof SplashPotionItem ||
            type instanceof LingeringPotionItem ||
            type instanceof FilledMapItem
        )) throw CANNOT_EDIT_EXCEPTION;
    }

    private static void checkHasColor(FabricClientCommandSource context) throws CommandSyntaxException {
        ItemStack item = ItemUtil.getItemStack(context);
        NbtCompound nbt = item.getNbt();
        if (isInDisplay(item)) nbt = item.getSubNbt(DISPLAY_KEY);
        if (nbt == null) throw NO_COLOR_EXCEPTION;
        if (!nbt.contains(getColorKey(context), NbtElement.INT_TYPE)) throw NO_COLOR_EXCEPTION;
    }

    private static String getColorKey(FabricClientCommandSource context) throws CommandSyntaxException {
        Item type = ItemUtil.getItemStack(context).getItem();
        if (
            type instanceof TippedArrowItem ||
            type instanceof PotionItem ||
            type instanceof SplashPotionItem ||
            type instanceof LingeringPotionItem
        ) return CUSTOM_POTION_COLOR_KEY;
        if (type instanceof FilledMapItem) return MAP_COLOR_KEY;
        return COLOR_KEY;
    }

    private static boolean isInDisplay(ItemStack item) {
        Item type = item.getItem();
        return !(
            type instanceof TippedArrowItem ||
            type instanceof PotionItem ||
            type instanceof SplashPotionItem ||
            type instanceof LingeringPotionItem
        );
    }

    private static String getHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("color")
            .build();
            
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());
                checkHasColor(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());

                String colorKey = getColorKey(context.getSource());
                NbtCompound nbt = item.getNbt();
                if (isInDisplay(item)) nbt = item.getSubNbt(DISPLAY_KEY);
                int color = nbt.getInt(colorKey);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, getHex(color)));
                return color;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                String colorKey = getColorKey(context.getSource());
                if (isInDisplay(item)) {
                    NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
                    display.remove(colorKey);
                    item.setSubNbt(DISPLAY_KEY, display);
                } else {
                    NbtCompound nbt = item.getOrCreateNbt();
                    nbt.remove(colorKey);
                    item.setNbt(nbt);
                }

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                return 1;
            })
            .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setHexColorNode = ClientCommandManager
            .argument("color", HexColorArgumentType.hexColor())
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                int color = HexColorArgumentType.getHexColor(context, "color");

                String colorKey = getColorKey(context.getSource());
                if (isInDisplay(item)) {
                    NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
                    display.putInt(colorKey, color);
                    item.setSubNbt(DISPLAY_KEY, display);
                } else {
                    NbtCompound nbt = item.getOrCreateNbt();
                    nbt.putInt(colorKey, color);
                    item.setNbt(nbt);
                }

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getHex(color)));
                return 1;
            })
            .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<color>]
        node.addChild(setNode);
        setNode.addChild(setHexColorNode);
    }
}
