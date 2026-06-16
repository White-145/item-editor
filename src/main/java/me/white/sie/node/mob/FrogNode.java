package me.white.sie.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.argument.RegistryArgumentType;

import java.util.Optional;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.util.EditorUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.item.ItemStack;

public class FrogNode implements Node {
    private static final CommandSyntaxException ISNT_FROG_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.frog.error.isntfrog")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.frog.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.frog.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.frog.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.frog.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.frog.variantremove";

    private static boolean isFrog(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityTypes.FROG;
    }

    private static Identifier getId(FrogVariant variant) {
        return EditorUtil.getRegistry(Registries.FROG_VARIANT).getKey(variant);
    }

    private static boolean hasVariant(ItemStack stack) {
        if (!stack.has(DataComponents.FROG_VARIANT)) {
            return false;
        }
        Optional<ResourceKey<FrogVariant>> optional = stack.get(DataComponents.FROG_VARIANT).unwrapKey();
        return optional.isPresent();
    }

    private static FrogVariant getVariant(ItemStack stack) {
        Optional<ResourceKey<FrogVariant>> optional = stack.get(DataComponents.FROG_VARIANT).unwrapKey();
        if (optional.isEmpty()) {
            return null;
        }
        return EditorUtil.getRegistry(Registries.FROG_VARIANT).get(optional.get()).get().value();
    }

    private static void setVariant(ItemStack stack, FrogVariant variant) {
        stack.set(DataComponents.FROG_VARIANT, EditorUtil.getRegistry(Registries.FROG_VARIANT).wrapAsHolder(variant));
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponents.FROG_VARIANT);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("frog").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isFrog(stack)) {
                throw ISNT_FROG_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            FrogVariant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", RegistryArgumentType.registryEntry(Registries.FROG_VARIANT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isFrog(stack)) {
                throw ISNT_FROG_EXCEPTION;
            }
            FrogVariant variant = RegistryArgumentType.getRegistryEntry(context, "variant", Registries.FROG_VARIANT);
            setVariant(stack, variant);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isFrog(stack)) {
                throw ISNT_FROG_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            removeVariant(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE_VARIANT));
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
