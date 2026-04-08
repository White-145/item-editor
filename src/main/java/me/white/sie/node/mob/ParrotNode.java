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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.item.ItemStack;

public class ParrotNode implements Node {
    private static final CommandSyntaxException ISNT_PARROT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.parrot.error.isntparrot")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.parrot.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.parrot.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.parrot.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.parrot.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.parrot.variantremove";
    private static final String VARIANT_RED_BLUE = "variant.minecraft.parrot.redblue";
    private static final String VARIANT_BLUE = "variant.minecraft.parrot.blue";
    private static final String VARIANT_GREEN = "variant.minecraft.parrot.green";
    private static final String VARIANT_YELLOW_BLUE = "variant.minecraft.parrot.yellowblue";
    private static final String VARIANT_GRAY = "variant.minecraft.parrot.gray";

    private static Component translation(Parrot.Variant variant) {
        return switch (variant) {
            case RED_BLUE -> Component.translatable(VARIANT_RED_BLUE);
            case BLUE -> Component.translatable(VARIANT_BLUE);
            case GREEN -> Component.translatable(VARIANT_GREEN);
            case YELLOW_BLUE -> Component.translatable(VARIANT_YELLOW_BLUE);
            case GRAY -> Component.translatable(VARIANT_GRAY);
        };
    }

    private static boolean isParrot(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityType.PARROT;
    }

    private static boolean hasVariant(ItemStack stack) {
        return stack.has(DataComponents.PARROT_VARIANT);
    }

    private static Parrot.Variant getVariant(ItemStack stack) {
        return stack.get(DataComponents.PARROT_VARIANT);
    }

    private static void setVariant(ItemStack stack, Parrot.Variant variant) {
        stack.set(DataComponents.PARROT_VARIANT, variant);
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponents.PARROT_VARIANT);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("parrot").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isParrot(stack)) {
                throw ISNT_PARROT_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            Parrot.Variant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(Parrot.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isParrot(stack)) {
                throw ISNT_PARROT_EXCEPTION;
            }
            Parrot.Variant variant = context.getArgument("variant", Parrot.Variant.class);
            if (hasVariant(stack)) {
                Parrot.Variant oldVariant = getVariant(stack);
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
            if (!isParrot(stack)) {
                throw ISNT_PARROT_EXCEPTION;
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
