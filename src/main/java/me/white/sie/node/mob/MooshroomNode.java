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
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.item.ItemStack;

public class MooshroomNode implements Node {
    private static final CommandSyntaxException ISNT_MOOSHROOM_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.mooshroom.error.isntmooshroom")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.mooshroom.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.mooshroom.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.mooshroom.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.mooshroom.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.mooshroom.variantremove";
    private static final String VARIANT_RED = "variant.minecraft.mooshroom.red";
    private static final String VARIANT_BROWN = "variant.minecraft.mooshroom.brown";

    private static Component translation(MushroomCow.Variant variant) {
        return switch (variant) {
            case RED -> Component.translatable(VARIANT_RED);
            case BROWN -> Component.translatable(VARIANT_BROWN);
        };
    }

    private static boolean isMooshroom(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityTypes.MOOSHROOM;
    }

    private static boolean hasVariant(ItemStack stack) {
        return stack.has(DataComponents.MOOSHROOM_VARIANT);
    }

    private static MushroomCow.Variant getVariant(ItemStack stack) {
        return stack.get(DataComponents.MOOSHROOM_VARIANT);
    }

    private static void setVariant(ItemStack stack, MushroomCow.Variant variant) {
        stack.set(DataComponents.MOOSHROOM_VARIANT, variant);
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponents.MOOSHROOM_VARIANT);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("mooshroom").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isMooshroom(stack)) {
                throw ISNT_MOOSHROOM_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            MushroomCow.Variant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(MushroomCow.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isMooshroom(stack)) {
                throw ISNT_MOOSHROOM_EXCEPTION;
            }
            MushroomCow.Variant variant = context.getArgument("variant", MushroomCow.Variant.class);
            if (hasVariant(stack)) {
                MushroomCow.Variant oldVariant = getVariant(stack);
                if (variant == oldVariant) {
                    throw VARIANT_ALREADY_IS_EXCEPTION;
                }
            }
            setVariant(stack, variant);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_VARIANT, translation(variant)));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isMooshroom(stack)) {
                throw ISNT_MOOSHROOM_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            removeVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE_VARIANT));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... variant
        node.addChild(variantNode);
        // ... get
        variantNode.addChild(variantGetNode);
        // ... set <variant>
        variantNode.addChild(variantSetNode);
        variantSetNode.addChild(variantSetVariantNode);
        // ... remove
        variantNode.addChild(variantRemoveNode);

        return node;
    }
}
