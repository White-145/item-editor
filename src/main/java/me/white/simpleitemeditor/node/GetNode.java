package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class GetNode implements Node {
    private static final CommandSyntaxException HAS_ITEM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.get.error.hasitem")).create();
    private static final String OUTPUT_ITEM = "commands.edit.get.item";
    private static final String OUTPUT_GET = "commands.edit.get.get";

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_ITEM, TextUtil.copyable(stack, context.getSource().getRegistryManager())));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> itemNode = commandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            if (EditorUtil.hasItem(EditorUtil.getStack(context.getSource()))) {
                throw HAS_ITEM_EXCEPTION;
            }
            ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, false);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, 1, TextUtil.copyable(stack, context.getSource().getRegistryManager())));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> itemCountNode = commandManager.argument("count", IntegerArgumentType.integer(0, 99)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            if (EditorUtil.hasItem(EditorUtil.getStack(context.getSource()))) {
                throw HAS_ITEM_EXCEPTION;
            }
            int count = IntegerArgumentType.getInteger(context, "count");
            ItemStack stack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(count, true);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, count, TextUtil.copyable(stack, context.getSource().getRegistryManager())));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... [<item>] [<count>]
        node.addChild(itemNode);
        itemNode.addChild(itemCountNode);

        return node;
    }
}
