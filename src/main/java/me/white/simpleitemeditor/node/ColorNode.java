package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.argument.ColorArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

public class ColorNode implements Node {
    public static final CommandSyntaxException ISNT_COLORABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.isntcolorable")).create();
    public static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.nocolor")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.color.get";
    private static final String OUTPUT_SET = "commands.edit.color.set";
    private static final String OUTPUT_REMOVE = "commands.edit.color.remove";

    private static boolean isColorable(ItemStack stack) {
        return stack.isIn(ItemTags.DYEABLE);
    }

    private static boolean hasColor(ItemStack stack) {
        return stack.contains(DataComponentTypes.DYED_COLOR);
    }

    private static int getColor(ItemStack stack) {
        if (!hasColor(stack)) {
            return -1;
        }
        return stack.get(DataComponentTypes.DYED_COLOR).rgb();
    }

    private static void removeColor(ItemStack stack) {
        stack.remove(DataComponentTypes.DYED_COLOR);
    }

    private static void setColor(ItemStack stack, int color) {
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, true));
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("color").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isColorable(stack)) {
                throw ISNT_COLORABLE_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            int color = getColor(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setColorNode = ClientCommandManager.argument("color", ColorArgumentType.color()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!isColorable(stack)) {
                throw ISNT_COLORABLE_EXCEPTION;
            }
            int color = ColorArgumentType.getColor(context, "color");
            if (color == getColor(stack)) {
                throw ALREADY_IS_EXCEPTION;
            }
            setColor(stack, color);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!isColorable(stack)) {
                throw ISNT_COLORABLE_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            removeColor(stack);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set <color>
        node.addChild(setNode);
        setNode.addChild(setColorNode);

        // ... remove
        node.addChild(removeNode);
    }
}
