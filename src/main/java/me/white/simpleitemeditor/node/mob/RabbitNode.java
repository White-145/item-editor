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
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class RabbitNode implements Node {
    private static final CommandSyntaxException ISNT_RABBIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.rabbit.error.isntrabbit")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.rabbit.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.rabbit.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.rabbit.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.rabbit.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.rabbit.variantremove";
    private static final String VARIANT_RED = "variant.minecraft.rabbit.red";
    private static final String VARIANT_WHITE = "variant.minecraft.rabbit.white";
    private static final String VARIANT_BLACK = "variant.minecraft.rabbit.black";
    private static final String VARIANT_WHITE_SPLOTCHED = "variant.minecraft.rabbit.white_splotched";
    private static final String VARIANT_GOLD = "variant.minecraft.rabbit.gold";
    private static final String VARIANT_SALT = "variant.minecraft.rabbit.salt";
    private static final String VARIANT_EVIL = "variant.minecraft.rabbit.evil";
    //? if <1.21.5 {
    /*private static final String VARIANT_KEY = "RabbitType";
    *///?}

    private static Text translation(RabbitVariant variant) {
        return switch (variant) {
            case RED -> Text.translatable(VARIANT_RED);
            case WHITE -> Text.translatable(VARIANT_WHITE);
            case BLACK -> Text.translatable(VARIANT_BLACK);
            case WHITE_SPLOTCHED -> Text.translatable(VARIANT_WHITE_SPLOTCHED);
            case GOLD -> Text.translatable(VARIANT_GOLD);
            case SALT -> Text.translatable(VARIANT_SALT);
            case EVIL -> Text.translatable(VARIANT_EVIL);
        };
    }

    private static boolean isRabbit(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityType.RABBIT;
    }

    private static boolean hasVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.contains(DataComponentTypes.RABBIT_VARIANT);
        //?} else {
        /*if (!stack.contains(DataComponentTypes.ENTITY_DATA)) {
            return false;
        }
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.INT_TYPE)) {
            return false;
        }
        int id = nbt.getInt(VARIANT_KEY);
        for (RabbitEntity.RabbitType variant : RabbitEntity.RabbitType.values()) {
            if (variant.getId() == id) {
                return true;
            }
        }
        return false;
        *///?}
    }

    private static RabbitVariant getVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return RabbitVariant.byVariant(stack.get(DataComponentTypes.RABBIT_VARIANT));
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        int id = nbt.getInt(VARIANT_KEY);
        for (RabbitEntity.RabbitType variant : RabbitEntity.RabbitType.values()) {
            if (variant.getId() == id) {
                return RabbitVariant.byVariant(variant);
            }
        }
        return null;
        *///?}
    }

    private static void setVariant(ItemStack stack, RabbitVariant variant) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.RABBIT_VARIANT, variant.variant);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.putInt(VARIANT_KEY, variant.variant.getId());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeVariant(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.RABBIT_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(VARIANT_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("rabbit").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isRabbit(stack)) {
                throw ISNT_RABBIT_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            RabbitVariant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(RabbitVariant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isRabbit(stack)) {
                throw ISNT_RABBIT_EXCEPTION;
            }
            RabbitVariant variant = context.getArgument("variant", RabbitVariant.class);
            if (hasVariant(stack)) {
                RabbitVariant oldVariant = getVariant(stack);
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
            if (!isRabbit(stack)) {
                throw ISNT_RABBIT_EXCEPTION;
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
    private enum RabbitVariant {
        //? if >=1.21.5 {
        RED(RabbitEntity.Variant.BROWN),
        WHITE(RabbitEntity.Variant.WHITE),
        BLACK(RabbitEntity.Variant.BLACK),
        WHITE_SPLOTCHED(RabbitEntity.Variant.WHITE_SPLOTCHED),
        GOLD(RabbitEntity.Variant.GOLD),
        SALT(RabbitEntity.Variant.SALT),
        EVIL(RabbitEntity.Variant.EVIL);

        final RabbitEntity.Variant variant;

        RabbitVariant(RabbitEntity.Variant variant) {
            this.variant = variant;
        }

        public static RabbitVariant byVariant(RabbitEntity.Variant variant) {
            for (RabbitVariant value : RabbitVariant.values()) {
                if (value.variant == variant) {
                    return value;
                }
            }
            return null;
        }
        //?} else {
        /*RED(RabbitEntity.RabbitType.BROWN),
        WHITE(RabbitEntity.RabbitType.WHITE),
        BLACK(RabbitEntity.RabbitType.BLACK),
        WHITE_SPLOTCHED(RabbitEntity.RabbitType.WHITE_SPLOTCHED),
        GOLD(RabbitEntity.RabbitType.GOLD),
        SALT(RabbitEntity.RabbitType.SALT),
        EVIL(RabbitEntity.RabbitType.EVIL);

        final RabbitEntity.RabbitType variant;

        RabbitVariant(RabbitEntity.RabbitType variant) {
            this.variant = variant;
        }

        public static RabbitVariant byVariant(RabbitEntity.RabbitType variant) {
            for (RabbitVariant value : RabbitVariant.values()) {
                if (value.variant == variant) {
                    return value;
                }
            }
            return null;
        }
        *///?}
    }
}
