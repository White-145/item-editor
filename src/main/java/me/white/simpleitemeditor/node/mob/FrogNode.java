package me.white.simpleitemeditor.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
//? if >=1.21.5 {
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.RegistryKey;

import java.util.Optional;
//?} else {
/*import me.white.simpleitemeditor.node.DataNode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
*///?}
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FrogNode implements Node {
    private static final CommandSyntaxException ISNT_FROG_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.frog.error.isntfrog")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.frog.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.frog.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.frog.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.frog.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.frog.variantremove";
    //? if <1.21.5 {
    /*private static final String VARIANT_KEY = "variant";
    *///?}

    private static boolean isFrog(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityType.FROG;
    }

    private static Identifier getId(FrogVariant variant) {
        return EditorUtil.getRegistry(RegistryKeys.FROG_VARIANT).getId(variant);
    }

    private static boolean hasVariant(ItemStack stack) {
        //? if >=1.21.5 {
        if (!stack.contains(DataComponentTypes.FROG_VARIANT)) {
            return false;
        }
        Optional<RegistryKey<FrogVariant>> optional = stack.get(DataComponentTypes.FROG_VARIANT).getKey();
        return optional.isPresent();
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.STRING_TYPE)) {
            return false;
        }
        Identifier id = Identifier.tryParse(nbt.getString(VARIANT_KEY));
        if (id == null) {
            return false;
        }
        return EditorUtil.getRegistry(RegistryKeys.FROG_VARIANT).containsId(id);
        *///?}
    }

    private static FrogVariant getVariant(ItemStack stack) {
        //? if >=1.21.5 {
        Optional<RegistryKey<FrogVariant>> optional = stack.get(DataComponentTypes.FROG_VARIANT).getKey();
        if (optional.isEmpty()) {
            return null;
        }
        return EditorUtil.getRegistry(RegistryKeys.FROG_VARIANT).get(optional.get());
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.STRING_TYPE)) {
            return null;
        }
        Identifier id = Identifier.tryParse(nbt.getString(VARIANT_KEY));
        if (id == null) {
            return null;
        }
        return EditorUtil.getRegistry(RegistryKeys.FROG_VARIANT).get(id);
        *///?}
    }

    private static void setVariant(ItemStack stack, FrogVariant variant) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.FROG_VARIANT, EditorUtil.getRegistry(RegistryKeys.FROG_VARIANT).getEntry(variant));
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        Identifier id = EditorUtil.getRegistry(RegistryKeys.FROG_VARIANT).getId(variant);
        nbt.putString(VARIANT_KEY, id.toString());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeVariant(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.FROG_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(VARIANT_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
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

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", RegistryArgumentType.registryEntry(RegistryKeys.FROG_VARIANT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isFrog(stack)) {
                throw ISNT_FROG_EXCEPTION;
            }
            FrogVariant variant = RegistryArgumentType.getRegistryEntry(context, "variant", RegistryKeys.FROG_VARIANT);
            setVariant(stack, variant);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_VARIANT, getId(variant)));
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
