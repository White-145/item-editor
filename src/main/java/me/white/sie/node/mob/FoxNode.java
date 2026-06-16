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
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.item.ItemStack;

public class FoxNode implements Node {
    private static final CommandSyntaxException ISNT_FOX_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.fox.error.isntfox")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.fox.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.fox.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.fox.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.fox.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.fox.variantremove";
    private static final String VARIANT_RED = "variant.minecraft.fox.red";
    private static final String VARIANT_SNOW = "variant.minecraft.fox.snow";

    private static Component translation(Fox.Variant variant) {
        return switch (variant) {
            case RED -> Component.translatable(VARIANT_RED);
            case SNOW -> Component.translatable(VARIANT_SNOW);
        };
    }

    private static boolean isFox(ItemStack stack) {
        //?if >=26.2 {
        return EditorUtil.getEntityType(stack) == EntityTypes.FOX;
        //?} else {
        /*return EditorUtil.getEntityType(stack) == EntityType.FOX;
        *///?}
    }

    private static boolean hasVariant(ItemStack stack) {
        return stack.has(DataComponents.FOX_VARIANT);
    }

    private static Fox.Variant getVariant(ItemStack stack) {
        return stack.get(DataComponents.FOX_VARIANT);
    }

    private static void setVariant(ItemStack stack, Fox.Variant variant) {
        stack.set(DataComponents.FOX_VARIANT, variant);
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponents.FOX_VARIANT);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("fox").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isFox(stack)) {
                throw ISNT_FOX_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            Fox.Variant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(Fox.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isFox(stack)) {
                throw ISNT_FOX_EXCEPTION;
            }
            Fox.Variant variant = context.getArgument("variant", Fox.Variant.class);
            if (hasVariant(stack)) {
                Fox.Variant oldVariant = getVariant(stack);
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
            if (!isFox(stack)) {
                throw ISNT_FOX_EXCEPTION;
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
