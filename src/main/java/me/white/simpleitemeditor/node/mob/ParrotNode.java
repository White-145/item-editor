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
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ParrotNode implements Node {
    private static final CommandSyntaxException ISNT_PARROT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.parrot.error.isntparrot")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.parrot.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.parrot.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.parrot.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.parrot.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.parrot.variantremove";
    private static final String VARIANT_RED_BLUE = "variant.minecraft.parrot.redblue";
    private static final String VARIANT_BLUE = "variant.minecraft.parrot.blue";
    private static final String VARIANT_GREEN = "variant.minecraft.parrot.green";
    private static final String VARIANT_YELLOW_BLUE = "variant.minecraft.parrot.yellowblue";
    private static final String VARIANT_GRAY = "variant.minecraft.parrot.gray";
    //? if <1.21.5 {
    /*private static final String VARIANT_KEY = "Variant";
    *///?}

    private static Text translation(ParrotEntity.Variant variant) {
        return switch (variant) {
            case RED_BLUE -> Text.translatable(VARIANT_RED_BLUE);
            case BLUE -> Text.translatable(VARIANT_BLUE);
            case GREEN -> Text.translatable(VARIANT_GREEN);
            case YELLOW_BLUE -> Text.translatable(VARIANT_YELLOW_BLUE);
            case GRAY -> Text.translatable(VARIANT_GRAY);
        };
    }

    private static boolean isParrot(ItemStack stack) {
        EntityType<?> entityType = EditorUtil.getEntityType(stack);
        return entityType == EntityType.PARROT;
    }

    private static boolean hasVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.contains(DataComponentTypes.PARROT_VARIANT);
        //?} else {
        /*if (!stack.contains(DataComponentTypes.ENTITY_DATA)) {
            return false;
        }
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.INT_TYPE)) {
            return false;
        }
        int variant = nbt.getInt(VARIANT_KEY);
        return variant >= 0 && variant < ParrotEntity.Variant.values().length;
        *///?}
    }

    private static ParrotEntity.Variant getVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.get(DataComponentTypes.PARROT_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        return ParrotEntity.Variant.byIndex(nbt.getInt(VARIANT_KEY));
        *///?}
    }

    private static void setVariant(ItemStack stack, ParrotEntity.Variant variant) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.PARROT_VARIANT, variant);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.putInt(VARIANT_KEY, variant.getId());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeVariant(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.PARROT_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(VARIANT_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("parrot").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isParrot(stack)) {
                throw ISNT_PARROT_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            ParrotEntity.Variant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(ParrotEntity.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isParrot(stack)) {
                throw ISNT_PARROT_EXCEPTION;
            }
            ParrotEntity.Variant variant = context.getArgument("variant", ParrotEntity.Variant.class);
            if (hasVariant(stack)) {
                ParrotEntity.Variant oldVariant = getVariant(stack);
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
            if (!isParrot(stack)) {
                throw ISNT_PARROT_EXCEPTION;
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
}
