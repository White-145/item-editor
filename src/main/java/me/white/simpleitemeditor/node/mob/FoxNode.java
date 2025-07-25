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
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class FoxNode implements Node {
    private static final CommandSyntaxException ISNT_FOX_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.fox.error.isntfox")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.fox.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.fox.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.fox.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.fox.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.fox.variantremove";
    private static final String VARIANT_RED = "variant.minecraft.fox.red";
    private static final String VARIANT_SNOW = "variant.minecraft.fox.snow";
    //? if <1.21.5 {
    /*private static final String VARIANT_KEY = "Type";
    *///?}

    private static Text translation(FoxVariant variant) {
        return switch (variant) {
            case RED -> Text.translatable(VARIANT_RED);
            case SNOW -> Text.translatable(VARIANT_SNOW);
        };
    }

    private static boolean isFox(ItemStack stack) {
        EntityType<?> entityType = EditorUtil.getEntityType(stack);
        return entityType == EntityType.FOX;
    }

    private static boolean hasVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.contains(DataComponentTypes.FOX_VARIANT);
        //?} else {
        /*if (!stack.contains(DataComponentTypes.ENTITY_DATA)) {
            return false;
        }
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.STRING_TYPE)) {
            return false;
        }
        return FoxEntity.Type.byName(nbt.getString(VARIANT_KEY)) != null;
        *///?}
    }

    private static FoxVariant getVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return FoxVariant.byVariant(stack.get(DataComponentTypes.FOX_VARIANT));
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        return FoxVariant.byVariant(FoxEntity.Type.byName(nbt.getString(VARIANT_KEY)));
        *///?}
    }

    private static void setVariant(ItemStack stack, FoxVariant variant) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.FOX_VARIANT, variant.variant);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.putString(VARIANT_KEY, variant.variant.asString());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeVariant(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.FOX_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(VARIANT_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("fox").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isFox(stack)) {
                throw ISNT_FOX_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            FoxVariant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(FoxVariant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isFox(stack)) {
                throw ISNT_FOX_EXCEPTION;
            }
            FoxVariant variant = context.getArgument("variant", FoxVariant.class);
            if (hasVariant(stack)) {
                FoxVariant oldVariant = getVariant(stack);
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
            if (!isFox(stack)) {
                throw ISNT_FOX_EXCEPTION;
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
    private enum FoxVariant {
        //? if >=1.21.5 {
        RED(FoxEntity.Variant.RED),
        SNOW(FoxEntity.Variant.SNOW);

        final FoxEntity.Variant variant;

        FoxVariant(FoxEntity.Variant variant) {
            this.variant = variant;
        }

        public static FoxVariant byVariant(FoxEntity.Variant variant) {
            for (FoxVariant value : FoxVariant.values()) {
                if (value.variant == variant) {
                    return value;
                }
            }
            return null;
        }
        //?} else {
        /*RED(FoxEntity.Type.RED),
        SNOW(FoxEntity.Type.SNOW);

        final FoxEntity.Type variant;

        FoxVariant(FoxEntity.Type variant) {
            this.variant = variant;
        }

        public static FoxVariant byVariant(FoxEntity.Type variant) {
            for (FoxVariant value : FoxVariant.values()) {
                if (value.variant == variant) {
                    return value;
                }
            }
            return null;
        }
        *///?}
    }
}
