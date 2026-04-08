package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.util.EditorUtil;
import me.white.sie.util.TextUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class GetNode implements Node {
    private static final CommandSyntaxException HAS_ITEM_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.get.error.hasitem")).create();
    private static final String OUTPUT_ITEM = "commands.edit.get.item";
    private static final String OUTPUT_GET = "commands.edit.get.get";

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_ITEM, TextUtil.copyable(stack, context.getSource().registryAccess())));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> itemNode = commandManager.argument("item", ItemArgument.item(registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            if (EditorUtil.hasItem(EditorUtil.getStack(context.getSource()))) {
                throw HAS_ITEM_EXCEPTION;
            }
            ItemStack stack = ItemArgument.getItem(context, "item").createItemStack(1);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET, 1, TextUtil.copyable(stack, context.getSource().registryAccess())));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> itemCountNode = commandManager.argument("count", IntegerArgumentType.integer(0, 99)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            if (EditorUtil.hasItem(EditorUtil.getStack(context.getSource()))) {
                throw HAS_ITEM_EXCEPTION;
            }
            int count = IntegerArgumentType.getInteger(context, "count");
            ItemStack stack = ItemArgument.getItem(context, "item").createItemStack(count);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET, count, TextUtil.copyable(stack, context.getSource().registryAccess())));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... [<item>] [<count>]
        node.addChild(itemNode);
        itemNode.addChild(itemCountNode);

        return node;
    }
}
