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
import net.minecraft.entity.passive.ChickenVariant;
import net.minecraft.item.EggItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.LazyRegistryEntryReference;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class ChickenNode implements Node {
    private static final CommandSyntaxException ISNT_CHICKEN_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.chicken.error.isntchicken")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.chicken.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.chicken.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.chicken.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.chicken.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.chicken.variantremove";

    private static boolean isChicken(ItemStack stack) {
        if (stack.getItem() instanceof EggItem) {
            return true;
        }
        return EditorUtil.getEntityType(stack) == EntityType.CHICKEN;
    }

    private static Identifier getId(ChickenVariant variant) {
        return EditorUtil.getRegistry(RegistryKeys.CHICKEN_VARIANT).getId(variant);
    }

    private static boolean hasVariant(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.CHICKEN_VARIANT)) {
            return false;
        }
        Optional<RegistryKey<ChickenVariant>> optional = stack.get(DataComponentTypes.CHICKEN_VARIANT).getKey();
        return optional.isPresent();
    }

    private static ChickenVariant getVariant(ItemStack stack) {
        Optional<RegistryKey<ChickenVariant>> optional = stack.get(DataComponentTypes.CHICKEN_VARIANT).getKey();
        if (optional.isEmpty()) {
            return null;
        }
        return EditorUtil.getRegistry(RegistryKeys.CHICKEN_VARIANT).get(optional.get());
    }

    private static void setVariant(ItemStack stack, ChickenVariant variant) {
        stack.set(DataComponentTypes.CHICKEN_VARIANT, new LazyRegistryEntryReference<>(EditorUtil.getRegistry(RegistryKeys.CHICKEN_VARIANT).getEntry(variant)));
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponentTypes.CHICKEN_VARIANT);
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("chicken").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isChicken(stack)) {
                throw ISNT_CHICKEN_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            ChickenVariant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", RegistryArgumentType.registryEntry(RegistryKeys.CHICKEN_VARIANT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isChicken(stack)) {
                throw ISNT_CHICKEN_EXCEPTION;
            }
            ChickenVariant variant = RegistryArgumentType.getRegistryEntry(context, "variant", RegistryKeys.CHICKEN_VARIANT);
            setVariant(stack, variant);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isChicken(stack)) {
                throw ISNT_CHICKEN_EXCEPTION;
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
