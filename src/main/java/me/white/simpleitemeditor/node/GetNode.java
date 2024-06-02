package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class GetNode implements Node {
    public static final CommandSyntaxException HAS_ITEM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.get.error.hasitem")).create();
    private static final String OUTPUT_ITEM = "commands.edit.get.item";
    private static final String OUTPUT_GET = "commands.edit.get.get";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }

            context.getSource().sendFeedback(Text.translatable(OUTPUT_ITEM, TextUtil.copyable(stack)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, ItemStackArgument> itemNode = ClientCommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (EditorUtil.hasItem(EditorUtil.getStack(context.getSource()))) {
                throw HAS_ITEM_EXCEPTION;
            }
            ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, 1, TextUtil.copyable(stack)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> itemCountNode = ClientCommandManager.argument("count", IntegerArgumentType.integer(0, 99)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (EditorUtil.hasItem(EditorUtil.getStack(context.getSource()))) {
                throw HAS_ITEM_EXCEPTION;
            }
            int count = IntegerArgumentType.getInteger(context, "count");
            ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(count, true);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, count, TextUtil.copyable(stack)));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... [<item>] [<count>]
        node.addChild(itemNode);
        itemNode.addChild(itemCountNode);
    }
}
