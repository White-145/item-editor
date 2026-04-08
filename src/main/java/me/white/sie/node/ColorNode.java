package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.argument.ColorArgumentType;
import me.white.sie.util.EditorUtil;
import me.white.sie.util.TextUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class ColorNode implements Node {
    private static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.color.error.nocolor")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.color.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.color.get";
    private static final String OUTPUT_SET = "commands.edit.color.set";
    private static final String OUTPUT_REMOVE = "commands.edit.color.remove";

    private static boolean hasColor(ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR);
    }

    private static int getColor(ItemStack stack) {
        if (!hasColor(stack)) {
            return -1;
        }
        return stack.get(DataComponents.DYED_COLOR).rgb();
    }

    private static void resetColor(ItemStack stack) {
        stack.remove(DataComponents.DYED_COLOR);
    }

    private static void setColor(ItemStack stack, int color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("color").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            EditorUtil.checkHasItem(stack);
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            int color = getColor(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setColorNode = commandManager.argument("color", ColorArgumentType.color()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            int color = ColorArgumentType.getColor(context, "color");
            if (color == getColor(stack)) {
                throw ALREADY_IS_EXCEPTION;
            }
            setColor(stack, color);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            resetColor(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get
        node.addChild(getNode);

        // ... set <color>
        node.addChild(setNode);
        setNode.addChild(setColorNode);

        // ... remove
        node.addChild(removeNode);

        return node;
    }
}
