package me.white.simpleitemeditor.node.mob;

//? if >=1.21.5 {
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class PigNode implements Node {
    private static final CommandSyntaxException ISNT_PIG_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.pig.error.isntpig")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.pig.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.pig.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.pig.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.pig.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.pig.variantremove";

    private static boolean isPig(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityType.PIG;
    }

    private static Identifier getId(PigVariant variant) {
        return EditorUtil.getRegistry(RegistryKeys.PIG_VARIANT).getId(variant);
    }

    private static boolean hasVariant(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.PIG_VARIANT)) {
            return false;
        }
        Optional<RegistryKey<PigVariant>> optional = stack.get(DataComponentTypes.PIG_VARIANT).getKey();
        return optional.isPresent();
    }

    private static PigVariant getVariant(ItemStack stack) {
        Optional<RegistryKey<PigVariant>> optional = stack.get(DataComponentTypes.PIG_VARIANT).getKey();
        if (optional.isEmpty()) {
            return null;
        }
        return EditorUtil.getRegistry(RegistryKeys.PIG_VARIANT).get(optional.get());
    }

    private static void setVariant(ItemStack stack, PigVariant variant) {
        stack.set(DataComponentTypes.PIG_VARIANT, EditorUtil.getRegistry(RegistryKeys.PIG_VARIANT).getEntry(variant));
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponentTypes.PIG_VARIANT);
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
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

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", RegistryArgumentType.registryEntry(RegistryKeys.PIG_VARIANT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPig(stack)) {
                throw ISNT_PIG_EXCEPTION;
            }
            PigVariant variant = RegistryArgumentType.getRegistryEntry(context, "variant", RegistryKeys.PIG_VARIANT);
            setVariant(stack, variant);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_VARIANT, getId(variant)));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE_VARIANT));
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
//?}
