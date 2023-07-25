package me.white.itemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.ColorArgumentType;
import me.white.itemeditor.util.ItemUtil;
import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.DyeableHorseArmorItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ColorNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.cannotedit")).create();
    public static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.nocolor")).create();
    private static final String OUTPUT_GET = "commands.edit.color.get";
    private static final String OUTPUT_SET = "commands.edit.color.set";
    private static final String OUTPUT_RESET = "commands.edit.color.reset";

    private static boolean canEdit(ItemStack stack) {
        Item type = stack.getItem();
        return (
            type instanceof DyeableArmorItem ||
            type instanceof DyeableHorseArmorItem ||
            type instanceof FilledMapItem
        );
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("color")
            .build();
            
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack stack = EditorUtil.getStack(context.getSource());
                if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!ItemUtil.hasColor(stack)) throw NO_COLOR_EXCEPTION;
                int color = ItemUtil.getColor(stack);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, EditorUtil.formatColor(color)));
                return color;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!ItemUtil.hasColor(stack)) throw NO_COLOR_EXCEPTION;
                int old = ItemUtil.getColor(stack);
                ItemUtil.setColor(stack, null);

                EditorUtil.setStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                return old;
            })
            .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setHexColorNode = ClientCommandManager
            .argument("color", ColorArgumentType.hex())
            .executes(context -> {
                ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                int old = ItemUtil.getColor(stack);
                int color = ColorArgumentType.getColor(context, "color");
                ItemUtil.setColor(stack, color);

                EditorUtil.setStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, EditorUtil.formatColor(color)));
                return old;
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
