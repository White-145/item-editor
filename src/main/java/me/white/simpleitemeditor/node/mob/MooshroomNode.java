package me.white.simpleitemeditor.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.EnumArgumentType;
//? if <1.21.5 {
/*import me.white.simpleitemeditor.node.DataNode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
*///?}
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class MooshroomNode implements Node {
    private static final CommandSyntaxException ISNT_MOOSHROOM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.mooshroom.error.isntmooshroom")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.mooshroom.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.mooshroom.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.mooshroom.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.mooshroom.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.mooshroom.variantremove";
    private static final String VARIANT_RED = "variant.minecraft.mooshroom.red";
    private static final String VARIANT_BROWN = "variant.minecraft.mooshroom.brown";
    //? if <1.21.5 {
    /*private static final String VARIANT_KEY = "Type";
     *///?}

    private static Text translation(MooshroomVariant variant) {
        return switch (variant.variant) {
            case RED -> Text.translatable(VARIANT_RED);
            case BROWN -> Text.translatable(VARIANT_BROWN);
        };
    }

    private static boolean isMooshroom(ItemStack stack) {
        EntityType<?> entityType = EditorUtil.getEntityType(stack);
        return entityType == EntityType.MOOSHROOM;
    }

    private static boolean hasVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.contains(DataComponentTypes.MOOSHROOM_VARIANT);
        //?} else {
        /*if (!stack.contains(DataComponentTypes.ENTITY_DATA)) {
            return false;
        }
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.STRING_TYPE)) {
            return false;
        }
        String variant = nbt.getString(VARIANT_KEY);
        return MooshroomEntity.Type.fromName(variant) != null;
        *///?}
    }

    private static MooshroomVariant getVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return MooshroomVariant.byVariant(stack.get(DataComponentTypes.MOOSHROOM_VARIANT));
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        String variant = nbt.getString(VARIANT_KEY);
        return MooshroomVariant.byVariant(MooshroomEntity.Type.fromName(variant));
        *///?}
    }

    private static void setVariant(ItemStack stack, MooshroomVariant variant) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.MOOSHROOM_VARIANT, variant.variant);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.putString(VARIANT_KEY, variant.variantName);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeVariant(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.MOOSHROOM_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(VARIANT_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("mooshroom").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isMooshroom(stack)) {
                throw ISNT_MOOSHROOM_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            MooshroomVariant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(MooshroomVariant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isMooshroom(stack)) {
                throw ISNT_MOOSHROOM_EXCEPTION;
            }
            MooshroomVariant variant = context.getArgument("variant", MooshroomVariant.class);
            if (hasVariant(stack)) {
                MooshroomVariant oldVariant = getVariant(stack);
                if (variant == oldVariant) {
                    throw VARIANT_ALREADY_IS_EXCEPTION;
                }
            }
            setVariant(stack, variant);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_VARIANT, translation(variant)));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isMooshroom(stack)) {
                throw ISNT_MOOSHROOM_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            removeVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE_VARIANT));
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

    // somewhere along the way Type was renamed to Variant
    private enum MooshroomVariant {
        //? if >=1.21.5 {
        RED(MooshroomEntity.Variant.RED, "red"),
        BROWN(MooshroomEntity.Variant.BROWN, "brown");

        final MooshroomEntity.Variant variant;
        final String variantName;

        MooshroomVariant(MooshroomEntity.Variant variant, String variantName) {
            this.variant = variant;
            this.variantName = variantName;
        }

        public static MooshroomVariant byVariant(MooshroomEntity.Variant variant) {
            for (MooshroomVariant value : MooshroomVariant.values()) {
                if (value.variant == variant) {
                    return value;
                }
            }
            return null;
        }
        //?} else {
        /*RED(MooshroomEntity.Type.RED, "red"),
        BROWN(MooshroomEntity.Type.BROWN, "brown");

        final MooshroomEntity.Type variant;
        final String variantName;

        MooshroomVariant(MooshroomEntity.Type variant, String variantName) {
            this.variant = variant;
            this.variantName = variantName;
        }

        public static MooshroomVariant byVariant(MooshroomEntity.Type variant) {
            for (MooshroomVariant value : MooshroomVariant.values()) {
                if (value.variant == variant) {
                    return value;
                }
            }
            return null;
        }
        *///?}
    }
}
