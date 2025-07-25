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
import net.minecraft.entity.passive.CowVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class CowNode implements Node {
    private static final CommandSyntaxException ISNT_COW_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cow.error.isntcow")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cow.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cow.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.cow.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.cow.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.cow.variantremove";

    private static boolean isCow(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityType.COW;
    }

    private static Identifier getId(CowVariant variant) {
        return EditorUtil.getRegistry(RegistryKeys.COW_VARIANT).getId(variant);
    }

    private static boolean hasVariant(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.COW_VARIANT)) {
            return false;
        }
        Optional<RegistryKey<CowVariant>> optional = stack.get(DataComponentTypes.COW_VARIANT).getKey();
        return optional.isPresent();
    }

    private static CowVariant getVariant(ItemStack stack) {
        Optional<RegistryKey<CowVariant>> optional = stack.get(DataComponentTypes.COW_VARIANT).getKey();
        if (optional.isEmpty()) {
            return null;
        }
        return EditorUtil.getRegistry(RegistryKeys.COW_VARIANT).get(optional.get());
    }

    private static void setVariant(ItemStack stack, CowVariant variant) {
        stack.set(DataComponentTypes.COW_VARIANT, EditorUtil.getRegistry(RegistryKeys.COW_VARIANT).getEntry(variant));
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponentTypes.COW_VARIANT);
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("cow").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isCow(stack)) {
                throw ISNT_COW_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            CowVariant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", RegistryArgumentType.registryEntry(RegistryKeys.COW_VARIANT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isCow(stack)) {
                throw ISNT_COW_EXCEPTION;
            }
            CowVariant variant = RegistryArgumentType.getRegistryEntry(context, "variant", RegistryKeys.COW_VARIANT);
            setVariant(stack, variant);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isCow(stack)) {
                throw ISNT_COW_EXCEPTION;
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
