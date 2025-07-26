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
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class LlamaNode implements Node {
    private static final CommandSyntaxException ISNT_LLAMA_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.llama.error.isntllama")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.llama.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.llama.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.llama.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.llama.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.llama.variantremove";
    private static final String VARIANT_CREAMY = "variant.minecraft.llama.creamy";
    private static final String VARIANT_WHITE = "variant.minecraft.llama.white";
    private static final String VARIANT_BROWN = "variant.minecraft.llama.brown";
    private static final String VARIANT_GRAY = "variant.minecraft.llama.gray";
    //? if <1.21.5 {
    /*private static final String VARIANT_KEY = "Variant";
     *///?}

    private static Text translation(LlamaEntity.Variant variant) {
        return switch (variant) {
            case CREAMY -> Text.translatable(VARIANT_CREAMY);
            case WHITE -> Text.translatable(VARIANT_WHITE);
            case BROWN -> Text.translatable(VARIANT_BROWN);
            case GRAY -> Text.translatable(VARIANT_GRAY);
        };
    }

    private static boolean isLlama(ItemStack stack) {
        EntityType<?> entityType = EditorUtil.getEntityType(stack);
        return entityType == EntityType.LLAMA;
    }

    private static boolean hasVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.contains(DataComponentTypes.LLAMA_VARIANT);
        //?} else {
        /*if (!stack.contains(DataComponentTypes.ENTITY_DATA)) {
            return false;
        }
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.INT_TYPE)) {
            return false;
        }
        int variant = nbt.getInt(VARIANT_KEY);
        return variant >= 0 && variant < Llama.Variant.values().length;
        *///?}
    }

    private static LlamaEntity.Variant getVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.get(DataComponentTypes.LLAMA_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        return AxolotlEntity.Variant.byId(nbt.getInt(VARIANT_KEY));
        *///?}
    }

    private static void setVariant(ItemStack stack, LlamaEntity.Variant variant) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.LLAMA_VARIANT, variant);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.putInt(VARIANT_KEY, variant.getIndex());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeVariant(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.LLAMA_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(VARIANT_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("llama").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isLlama(stack)) {
                throw ISNT_LLAMA_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            LlamaEntity.Variant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(LlamaEntity.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isLlama(stack)) {
                throw ISNT_LLAMA_EXCEPTION;
            }
            LlamaEntity.Variant variant = context.getArgument("variant", LlamaEntity.Variant.class);
            if (hasVariant(stack)) {
                LlamaEntity.Variant oldVariant = getVariant(stack);
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
            if (!isLlama(stack)) {
                throw ISNT_LLAMA_EXCEPTION;
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
