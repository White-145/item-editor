package me.white.sie.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.argument.EnumArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.util.EditorUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
//?if >= 26.2 {
import net.minecraft.world.entity.EntityTypes;
//?} else {
/*import net.minecraft.world.entity.EntityType;
*///?}
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class ShulkerNode implements Node {
    private static final CommandSyntaxException ISNT_SHULKER_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.shulker.error.isntshulker")).create();
    private static final CommandSyntaxException COLOR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.shulker.error.coloralreadyis")).create();
    private static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.shulker.error.nocolor")).create();
    private static final String OUTPUT_GET_COLOR = "commands.edit.mob.shulker.colorget";
    private static final String OUTPUT_SET_COLOR = "commands.edit.mob.shulker.colorset";
    private static final String OUTPUT_REMOVE_COLOR = "commands.edit.mob.shulker.colorremove";

    private static boolean isShulker(ItemStack stack) {
        //?if >=26.2 {
        return EditorUtil.getEntityType(stack) == EntityTypes.SHULKER;
        //?} else {
        /*return EditorUtil.getEntityType(stack) == EntityType.SHULKER;
        *///?}
    }

    private static boolean hasColor(ItemStack stack) {
        return stack.has(DataComponents.SHULKER_COLOR);
    }

    private static DyeColor getColor(ItemStack stack) {
        return stack.get(DataComponents.SHULKER_COLOR);
    }

    private static void setColor(ItemStack stack, DyeColor color) {
        stack.set(DataComponents.SHULKER_COLOR, color);
    }

    private static void removeColor(ItemStack stack) {
        stack.remove(DataComponents.SHULKER_COLOR);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("shulker").build();

        CommandNode<S> colorNode = commandManager.literal("color").build();

        CommandNode<S> colorGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isShulker(stack)) {
                throw ISNT_SHULKER_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            DyeColor color = getColor(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_COLOR, EditorUtil.colorTranslation(color)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorSetNode = commandManager.literal("set").build();

        CommandNode<S> colorSetColorNode = commandManager.argument("color", EnumArgumentType.enums(DyeColor.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isShulker(stack)) {
                throw ISNT_SHULKER_EXCEPTION;
            }
            DyeColor color = context.getArgument("color", DyeColor.class);
            if (hasColor(stack)) {
                DyeColor oldColor = getColor(stack);
                if (color == oldColor) {
                    throw COLOR_ALREADY_IS_EXCEPTION;
                }
            }
            setColor(stack, color);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_COLOR, EditorUtil.colorTranslation(color)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isShulker(stack)) {
                throw ISNT_SHULKER_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            removeColor(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE_COLOR));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... color
        node.addChild(colorNode);
        // ... get
        colorNode.addChild(colorGetNode);
        // ... set <color>
        colorNode.addChild(colorSetNode);
        colorSetNode.addChild(colorSetColorNode);
        // ... remove
        colorNode.addChild(colorRemoveNode);

        return node;
    }
}