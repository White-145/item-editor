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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class ColorNode {
    public static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.nocolor")).create();
    private static final String OUTPUT_GET = "commands.edit.color.get";
    private static final String OUTPUT_SET = "commands.edit.color.set";
    private static final String OUTPUT_RESET = "commands.edit.color.reset";
    private static final String DISPLAY_KEY = "display";
    private static final String MAP_COLOR_KEY = "MapColor";
    private static final String COLOR_KEY = "color";

    private static void checkCanEdit(FabricClientCommandSource context) throws CommandSyntaxException {
        // TODO
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

                ItemStack item = ItemManager.getItemStack(context.getSource());
                String colorKey = COLOR_KEY;
                if (item.getItem() == Items.FILLED_MAP) {
                    colorKey = MAP_COLOR_KEY;
                }
                NbtCompound display = item.getSubNbt(DISPLAY_KEY);
                if (display == null) throw NO_COLOR_EXCEPTION;
                if (!display.contains(colorKey)) throw NO_COLOR_EXCEPTION;
                int color = display.getInt(colorKey);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, Integer.toHexString(color)));
                return color;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setHexColorNode = ClientCommandManager
            .argument(COLOR_KEY, HexColorArgumentType.hexColor())
            .executes(context -> {
                ItemManager.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
                String colorKey = COLOR_KEY;
                if (item.getItem() == Items.FILLED_MAP) {
                    colorKey = MAP_COLOR_KEY;
                }
                int color = HexColorArgumentType.getHexColor(context, "color");
                NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
                display.putInt(colorKey, color);
                ItemManager.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, Integer.toHexString(color)));
                return 1;
            })
            .build();

        
        LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
            .literal("reset")
            .executes(context -> {
                ItemManager.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemManager.getItemStack(context.getSource()).copy();
                NbtCompound display = item.getSubNbt(DISPLAY_KEY);
                if (display == null) throw NO_COLOR_EXCEPTION;
                if (!display.contains(COLOR_KEY) || !display.contains(MAP_COLOR_KEY)) throw NO_COLOR_EXCEPTION;
                display.remove(COLOR_KEY);
                display.remove(MAP_COLOR_KEY);
                item.setSubNbt(DISPLAY_KEY, display);
                ItemManager.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                return 1;
            })
            .build();

        rootNode.addChild(node);

        // ... color get
        node.addChild(getNode);

        // ... color set <color>
        node.addChild(setNode);
        setNode.addChild(setHexColorNode);
        node.addChild(resetNode);
    }
}
