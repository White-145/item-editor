package me.white.simpleitemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.argument.ColorArgumentType;
import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.*;
import net.minecraft.text.Text;

public class ColorNode implements Node {
    public static final CommandSyntaxException ISNT_COLORABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.isntcolorable")).create();
    public static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.nocolor")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.color.get";
    private static final String OUTPUT_SET = "commands.edit.color.set";
    private static final String OUTPUT_REMOVE = "commands.edit.color.remove";

    private static boolean isColorable(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof DyeableArmorItem ||
                item instanceof DyeableHorseArmorItem ||
                item instanceof FilledMapItem ||
                item instanceof PotionItem ||
                item instanceof TippedArrowItem ||
                item instanceof FireworkStarItem;
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("color")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isColorable(stack)) throw ISNT_COLORABLE_EXCEPTION;
                    if (!ItemUtil.hasColor(stack)) throw NO_COLOR_EXCEPTION;
                    int color = ItemUtil.getColor(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(EditorUtil.formatColor(color))));
                    return color;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isColorable(stack)) throw ISNT_COLORABLE_EXCEPTION;
                    if (!ItemUtil.hasColor(stack)) throw NO_COLOR_EXCEPTION;
                    int old = ItemUtil.getColor(stack);
                    ItemUtil.setColor(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
                    return old;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setColorNode = ClientCommandManager
                .argument("color", ColorArgumentType.color())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isColorable(stack)) throw ISNT_COLORABLE_EXCEPTION;
                    Integer old = ItemUtil.getColor(stack);
                    int color = ColorArgumentType.getColor(context, "color");
                    if (old != null && old.equals(color)) throw ALREADY_IS_EXCEPTION;
                    ItemUtil.setColor(stack, color);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(EditorUtil.formatColor(color))));
                    return old == null ? -1 : old;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!isColorable(stack)) throw ISNT_COLORABLE_EXCEPTION;
                    if (!ItemUtil.hasColor(stack)) throw NO_COLOR_EXCEPTION;
                    int old = ItemUtil.getColor(stack);
                    ItemUtil.setColor(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
                    return old;
                })
                .build();

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
