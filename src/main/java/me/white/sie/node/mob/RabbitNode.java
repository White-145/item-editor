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
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.item.ItemStack;

public class RabbitNode implements Node {
    private static final CommandSyntaxException ISNT_RABBIT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.rabbit.error.isntrabbit")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.rabbit.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.rabbit.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.rabbit.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.rabbit.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.rabbit.variantremove";
    private static final String VARIANT_RED = "variant.minecraft.rabbit.red";
    private static final String VARIANT_WHITE = "variant.minecraft.rabbit.white";
    private static final String VARIANT_BLACK = "variant.minecraft.rabbit.black";
    private static final String VARIANT_WHITE_SPLOTCHED = "variant.minecraft.rabbit.white_splotched";
    private static final String VARIANT_GOLD = "variant.minecraft.rabbit.gold";
    private static final String VARIANT_SALT = "variant.minecraft.rabbit.salt";
    private static final String VARIANT_EVIL = "variant.minecraft.rabbit.evil";

    private static Component translation(Rabbit.Variant variant) {
        return switch (variant) {
            case BROWN -> Component.translatable(VARIANT_RED);
            case WHITE -> Component.translatable(VARIANT_WHITE);
            case BLACK -> Component.translatable(VARIANT_BLACK);
            case WHITE_SPLOTCHED -> Component.translatable(VARIANT_WHITE_SPLOTCHED);
            case GOLD -> Component.translatable(VARIANT_GOLD);
            case SALT -> Component.translatable(VARIANT_SALT);
            case EVIL -> Component.translatable(VARIANT_EVIL);
        };
    }

    private static boolean isRabbit(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityTypes.RABBIT;
    }

    private static boolean hasVariant(ItemStack stack) {
        return stack.has(DataComponents.RABBIT_VARIANT);
    }

    private static Rabbit.Variant getVariant(ItemStack stack) {
        return stack.get(DataComponents.RABBIT_VARIANT);
    }

    private static void setVariant(ItemStack stack, Rabbit.Variant variant) {
        stack.set(DataComponents.RABBIT_VARIANT, variant);
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponents.RABBIT_VARIANT);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("rabbit").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isRabbit(stack)) {
                throw ISNT_RABBIT_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            Rabbit.Variant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(Rabbit.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isRabbit(stack)) {
                throw ISNT_RABBIT_EXCEPTION;
            }
            Rabbit.Variant variant = context.getArgument("variant", Rabbit.Variant.class);
            if (hasVariant(stack)) {
                Rabbit.Variant oldVariant = getVariant(stack);
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
            if (!isRabbit(stack)) {
                throw ISNT_RABBIT_EXCEPTION;
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
