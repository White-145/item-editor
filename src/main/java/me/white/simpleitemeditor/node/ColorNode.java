package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.ColorArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

public class ColorNode implements Node {
    private static final CommandSyntaxException ISNT_COLORABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.isntcolorable")).create();
    private static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.nocolor")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.color.error.alreadyis")).create();
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

    private static void resetColor(ItemStack stack) {
        stack.remove(DataComponentTypes.DYED_COLOR);
    }

    private static void setColor(ItemStack stack, int color) {
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color, TooltipNode.TooltipPart.COLOR.get(stack)));
    }

    @Override
    public void register(CommonCommandManager<CommandSource> commandManager, CommandNode<CommandSource> rootNode, CommandRegistryAccess registryAccess) {
        CommandNode<CommandSource> node = commandManager.literal("color").build();

        CommandNode<CommandSource> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            EditorUtil.checkHasItem(stack);
            if (!isColorable(stack)) {
                throw ISNT_COLORABLE_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            int color = getColor(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> setNode = commandManager.literal("set").build();

        CommandNode<CommandSource> setColorNode = commandManager.argument("color", ColorArgumentType.color()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            if (!isColorable(stack)) {
                throw ISNT_COLORABLE_EXCEPTION;
            }
            int color = ColorArgumentType.getColor(context, "color");
            if (color == getColor(stack)) {
                throw ALREADY_IS_EXCEPTION;
            }
            setColor(stack, color);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> removeNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isColorable(stack)) {
                throw ISNT_COLORABLE_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            resetColor(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE));
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
