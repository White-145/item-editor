package me.white.sie.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.argument.RegistryArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.util.EditorUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
//?if >= 26.2 {
import net.minecraft.world.entity.EntityTypes;
//?} else {
/*import net.minecraft.world.entity.EntityType;
*///?}
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class PigNode implements Node {
    private static final CommandSyntaxException ISNT_PIG_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.pig.error.isntpig")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.pig.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.pig.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.pig.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.pig.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.pig.variantremove";

    private static boolean isPig(ItemStack stack) {
        //?if >=26.2 {
        return EditorUtil.getEntityType(stack) == EntityTypes.PIG;
        //?} else {
        /*return EditorUtil.getEntityType(stack) == EntityType.PIG;
        *///?}
    }

    private static Identifier getId(PigVariant variant) {
        return EditorUtil.getRegistry(Registries.PIG_VARIANT).getKey(variant);
    }

    private static boolean hasVariant(ItemStack stack) {
        if (!stack.has(DataComponents.PIG_VARIANT)) {
            return false;
        }
        Optional<ResourceKey<PigVariant>> optional = stack.get(DataComponents.PIG_VARIANT).unwrapKey();
        return optional.isPresent();
    }

    private static PigVariant getVariant(ItemStack stack) {
        Optional<ResourceKey<PigVariant>> optional = stack.get(DataComponents.PIG_VARIANT).unwrapKey();
        if (optional.isEmpty()) {
            return null;
        }
        return EditorUtil.getRegistry(Registries.PIG_VARIANT).get(optional.get()).get().value();
    }

    private static void setVariant(ItemStack stack, PigVariant variant) {
        stack.set(DataComponents.PIG_VARIANT, EditorUtil.getRegistry(Registries.PIG_VARIANT).wrapAsHolder(variant));
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponents.PIG_VARIANT);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("pig").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isPig(stack)) {
                throw ISNT_PIG_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            PigVariant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", RegistryArgumentType.registryEntry(Registries.PIG_VARIANT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPig(stack)) {
                throw ISNT_PIG_EXCEPTION;
            }
            PigVariant variant = RegistryArgumentType.getRegistryEntry(context, "variant", Registries.PIG_VARIANT);
            setVariant(stack, variant);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPig(stack)) {
                throw ISNT_PIG_EXCEPTION;
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
