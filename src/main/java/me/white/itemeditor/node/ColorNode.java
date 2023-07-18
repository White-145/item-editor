package me.white.itemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.ItemManager;
import me.white.itemeditor.argument.HexColorArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
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
        Item type = ItemManager.getItemStack(context).getItem();
        if (!(
            type.equals(Items.LEATHER_HORSE_ARMOR) ||
            type.equals(Items.LEATHER_HELMET) ||
            type.equals(Items.LEATHER_CHESTPLATE) ||
            type.equals(Items.LEATHER_BOOTS) ||
            type.equals(Items.TIPPED_ARROW) ||
            type.equals(Items.POTION) ||
            type.equals(Items.SPLASH_POTION) ||
            type.equals(Items.LINGERING_POTION) ||
            type.equals(Items.FILLED_MAP)
        )) throw CANNOT_EDIT_EXCEPTION;
    }

    private static void checkHasColor(FabricClientCommandSource context) throws CommandSyntaxException {
        ItemStack item = ItemManager.getItemStack(context);
        NbtCompound nbt = item.getNbt();
        if (isInDisplay(item)) nbt = item.getSubNbt(DISPLAY_KEY);
        if (nbt == null) throw NO_COLOR_EXCEPTION;
        if (!nbt.contains(getColorKey(item))) throw NO_COLOR_EXCEPTION;
    }

    private static String getColorKey(ItemStack item) throws CommandSyntaxException {
        Item type = item.getItem();
        if (
            type.equals(Items.TIPPED_ARROW) ||
            type.equals(Items.POTION) ||
            type.equals(Items.SPLASH_POTION) ||
            type.equals(Items.LINGERING_POTION)
        ) return CUSTOM_POTION_COLOR_KEY;
        if (type.equals(Items.FILLED_MAP)) return MAP_COLOR_KEY;
        return COLOR_KEY;
    }

    private static boolean isInDisplay(ItemStack item) {
        Item type = item.getItem();
        return !(type.equals(Items.TIPPED_ARROW) ||
            type.equals(Items.POTION) ||
            type.equals(Items.SPLASH_POTION) ||
            type.equals(Items.LINGERING_POTION));
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("color")
            .build();
            
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemManager.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());
                checkHasColor(context.getSource());

                ItemStack item = ItemManager.getItemStack(context.getSource());

                String colorKey = getColorKey(item);
                NbtCompound nbt = item.getNbt();
                if (isInDisplay(item)) nbt = item.getSubNbt(DISPLAY_KEY);
                if (nbt == null) throw NO_COLOR_EXCEPTION;
                if (!nbt.contains(colorKey)) throw NO_COLOR_EXCEPTION;
                int color = nbt.getInt(colorKey);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, Integer.toHexString(color)));
                return color;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemManager.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemManager.getItemStack(context.getSource()).copy();

                String colorKey = getColorKey(item);
                if (isInDisplay(item)) {
                    NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
                    display.remove(colorKey);
                    item.setSubNbt(DISPLAY_KEY, display);
                } else {
                    NbtCompound nbt = item.getOrCreateNbt();
                    nbt.remove(colorKey);
                    item.setNbt(nbt);
                }

                ItemManager.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                return 1;
            })
            .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setHexColorNode = ClientCommandManager
            .argument("color", HexColorArgumentType.hexColor())
            .executes(context -> {
                ItemManager.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
                int color = HexColorArgumentType.getHexColor(context, "color");

                String colorKey = getColorKey(item);
                if (isInDisplay(item)) {
                    NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
                    display.putInt(colorKey, color);
                    item.setSubNbt(DISPLAY_KEY, display);
                } else {
                    NbtCompound nbt = item.getOrCreateNbt();
                    nbt.putInt(colorKey, color);
                    item.setNbt(nbt);
                }

                ItemManager.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, Integer.toHexString(color)));
                return 1;
            })
            .build();

        rootNode.addChild(node);

        // ... color get
        node.addChild(getNode);

        // ... color set [<color>]
        node.addChild(setNode);
        setNode.addChild(setHexColorNode);
    }
}
