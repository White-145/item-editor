package me.white.sie.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.argument.EnumArgumentType;
import me.white.sie.argument.RegistryArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.util.EditorUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class CatNode implements Node {
    private static final CommandSyntaxException ISNT_CAT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.cat.error.isntcat")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.cat.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.cat.error.novariant")).create();
    private static final CommandSyntaxException COLLAR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.cat.error.collaralreadyis")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.cat.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.cat.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.cat.variantremove";
    private static final String OUTPUT_GET_COLLAR = "commands.edit.mob.cat.collarget";
    private static final String OUTPUT_SET_COLLAR = "commands.edit.mob.cat.collarset";

    private static boolean isCat(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityTypes.CAT;
    }

    private static Identifier getId(CatVariant variant) {
        Registry<CatVariant> registry = EditorUtil.getRegistry(Registries.CAT_VARIANT);
        return registry.getKey(variant);
    }

    private static boolean hasVariant(ItemStack stack) {
        return stack.has(DataComponents.CAT_VARIANT);
    }

    private static CatVariant getVariant(ItemStack stack) {
        return stack.get(DataComponents.CAT_VARIANT).value();
    }

    private static DyeColor getCollar(ItemStack stack) {
        if (!stack.has(DataComponents.CAT_COLLAR)) {
            return DyeColor.RED;
        }
        return stack.get(DataComponents.CAT_COLLAR);
    }

    private static void setVariant(ItemStack stack, CatVariant variant) {
        stack.set(DataComponents.CAT_VARIANT, Holder.direct(variant));
    }

    private static void setCollar(ItemStack stack, DyeColor collar) {
        stack.set(DataComponents.CAT_COLLAR, collar);
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponents.CAT_VARIANT);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("cat").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            CatVariant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", RegistryArgumentType.registryEntry(Registries.CAT_VARIANT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            CatVariant variant = RegistryArgumentType.getRegistryEntry(context, "variant", Registries.CAT_VARIANT);
            if (hasVariant(stack)) {
                CatVariant oldVariant = getVariant(stack);
                if (variant.equals(oldVariant)) {
                    throw VARIANT_ALREADY_IS_EXCEPTION;
                }
            }
            setVariant(stack, variant);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_VARIANT, getId(variant)));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            removeVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE_VARIANT));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> collarNode = commandManager.literal("collar").build();

        CommandNode<S> collarGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            DyeColor collar = getCollar(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_COLLAR, EditorUtil.colorTranslation(collar)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> collarSetNode = commandManager.literal("set").build();

        CommandNode<S> collarSetColorNode = commandManager.argument("collar", EnumArgumentType.enums(DyeColor.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            DyeColor collar = context.getArgument("collar", DyeColor.class);
            DyeColor oldCollar = getCollar(stack);
            if (collar == oldCollar) {
                throw COLLAR_ALREADY_IS_EXCEPTION;
            }
            setCollar(stack, collar);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_COLLAR, EditorUtil.colorTranslation(collar)));
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

        // ... collar
        node.addChild(collarNode);
        // ... get
        collarNode.addChild(collarGetNode);
        // ... set <color>
        collarNode.addChild(collarSetNode);
        collarSetNode.addChild(collarSetColorNode);

        return node;
    }
}