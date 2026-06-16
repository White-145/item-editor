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
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.item.ItemStack;

public class SalmonNode implements Node {
    private static final CommandSyntaxException ISNT_SALMON_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.salmon.error.isntsalmon")).create();
    private static final CommandSyntaxException SIZE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.salmon.error.sizealreadyis")).create();
    private static final CommandSyntaxException NO_SIZE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.salmon.error.nosize")).create();
    private static final String OUTPUT_GET_SIZE = "commands.edit.mob.salmon.sizeget";
    private static final String OUTPUT_SET_SIZE = "commands.edit.mob.salmon.sizeset";
    private static final String OUTPUT_REMOVE_SIZE = "commands.edit.mob.salmon.sizeremove";
    private static final String SIZE_SMALL = "variant.minecraft.salmon.small";
    private static final String SIZE_MEDIUM = "variant.minecraft.salmon.medium";
    private static final String SIZE_LARGE = "variant.minecraft.salmon.large";

    private static Component translation(Salmon.Variant variant) {
        return switch (variant) {
            case SMALL -> Component.translatable(SIZE_SMALL);
            case MEDIUM -> Component.translatable(SIZE_MEDIUM);
            case LARGE -> Component.translatable(SIZE_LARGE);
        };
    }

    private static boolean isSalmon(ItemStack stack) {
        //?if >=26.2 {
        return EditorUtil.getEntityType(stack) == EntityTypes.SALMON;
        //?} else {
        /*return EditorUtil.getEntityType(stack) == EntityType.SALMON;
        *///?}
    }

    private static boolean hasSize(ItemStack stack) {
        return stack.has(DataComponents.SALMON_SIZE);
    }

    private static Salmon.Variant getSize(ItemStack stack) {
        return stack.get(DataComponents.SALMON_SIZE);
    }

    private static void setSize(ItemStack stack, Salmon.Variant size) {
        stack.set(DataComponents.SALMON_SIZE, size);
    }

    private static void removeSize(ItemStack stack) {
        stack.remove(DataComponents.SALMON_SIZE);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("salmon").build();

        CommandNode<S> sizeNode = commandManager.literal("size").build();

        CommandNode<S> sizeGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isSalmon(stack)) {
                throw ISNT_SALMON_EXCEPTION;
            }
            if (!hasSize(stack)) {
                throw NO_SIZE_EXCEPTION;
            }
            Salmon.Variant variant = getSize(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_SIZE, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sizeSetNode = commandManager.literal("set").build();

        CommandNode<S> sizeSetSizeNode = commandManager.argument("size", EnumArgumentType.enums(Salmon.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isSalmon(stack)) {
                throw ISNT_SALMON_EXCEPTION;
            }
            Salmon.Variant size = context.getArgument("size", Salmon.Variant.class);
            if (hasSize(stack)) {
                Salmon.Variant oldSize = getSize(stack);
                if (size == oldSize) {
                    throw SIZE_ALREADY_IS_EXCEPTION;
                }
            }
            setSize(stack, size);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_SIZE, translation(size)));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sizeRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isSalmon(stack)) {
                throw ISNT_SALMON_EXCEPTION;
            }
            if (!hasSize(stack)) {
                throw NO_SIZE_EXCEPTION;
            }
            removeSize(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE_SIZE));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... size
        node.addChild(sizeNode);
        // ... get
        sizeNode.addChild(sizeGetNode);
        // ... set <size>
        sizeNode.addChild(sizeSetNode);
        sizeSetNode.addChild(sizeSetSizeNode);
        // ... remove
        sizeNode.addChild(sizeRemoveNode);

        return node;
    }
}
