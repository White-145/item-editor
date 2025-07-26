package me.white.simpleitemeditor.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
//? if >=1.21.5 {
import net.minecraft.component.DataComponentTypes;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
//?} else {
/*import me.white.simpleitemeditor.node.DataNode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
*///?}
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class CatNode implements Node {
    private static final CommandSyntaxException ISNT_CAT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cat.error.isntcat")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cat.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cat.error.novariant")).create();
    private static final CommandSyntaxException COLLAR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cat.error.collaralreadyis")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.cat.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.cat.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.cat.variantremove";
    private static final String OUTPUT_GET_COLLAR = "commands.edit.mob.cat.collarget";
    private static final String OUTPUT_SET_COLLAR = "commands.edit.mob.cat.collarset";
    //? if <1.21.5 {
    /*private static final String VARIANT_KEY = "variant";
    private static final String COLLAR_KEY = "CollarColor";
    *///?}

    private static boolean isCat(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityType.CAT;
    }

    private static Identifier getId(CatVariant variant) {
        //? if >=1.21.5 {
        Registry<CatVariant> registry = EditorUtil.getRegistry(RegistryKeys.CAT_VARIANT);
        return registry.getId(variant);
        //?} else {
        /*return Registries.CAT_VARIANT.getId(variant);
        *///?}
    }

    private static boolean hasVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.contains(DataComponentTypes.CAT_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.STRING_TYPE)) {
            return false;
        }
        Identifier variant = Identifier.tryParse(nbt.getString(VARIANT_KEY));
        return Registries.CAT_VARIANT.containsId(variant);
        *///?}
    }

    private static CatVariant getVariant(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.get(DataComponentTypes.CAT_VARIANT).value();
        //?} else {
        /*if (!hasVariant(stack)) {
            return null;
        }
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        Identifier variant = Identifier.tryParse(nbt.getString(VARIANT_KEY));
        return Registries.CAT_VARIANT.get(variant);
        *///?}
    }

    private static DyeColor getCollar(ItemStack stack) {
        //? if >=1.21.5 {
        if (!stack.contains(DataComponentTypes.CAT_COLLAR)) {
            return DyeColor.RED;
        }
        return stack.get(DataComponentTypes.CAT_COLLAR);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(COLLAR_KEY, NbtElement.BYTE_TYPE)) {
            return DyeColor.RED;
        }
        return DyeColor.byId(nbt.getByte(COLLAR_KEY));
        *///?}
    }

    private static void setVariant(ItemStack stack, CatVariant variant) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.CAT_VARIANT, RegistryEntry.of(variant));
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.putString(VARIANT_KEY, Registries.CAT_VARIANT.getId(variant).toString());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void setCollar(ItemStack stack, DyeColor collar) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.CAT_COLLAR, collar);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.putByte(COLLAR_KEY, (byte)collar.getId());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeVariant(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.CAT_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(VARIANT_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("cat").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            CatVariant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, getId(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", RegistryArgumentType.registryEntry(RegistryKeys.CAT_VARIANT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            CatVariant variant = RegistryArgumentType.getRegistryEntry(context, "variant", RegistryKeys.CAT_VARIANT);
            if (hasVariant(stack)) {
                CatVariant oldVariant = getVariant(stack);
                if (variant.equals(oldVariant)) {
                    throw VARIANT_ALREADY_IS_EXCEPTION;
                }
            }
            setVariant(stack, variant);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_VARIANT, getId(variant)));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            removeVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE_VARIANT));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> collarNode = commandManager.literal("collar").build();

        CommandNode<S> collarGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            DyeColor collar = getCollar(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_COLLAR, EditorUtil.colorTranslation(collar)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> collarSetNode = commandManager.literal("set").build();

        CommandNode<S> collarSetColorNode = commandManager.argument("collar", EnumArgumentType.enums(DyeColor.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isCat(stack)) {
                throw ISNT_CAT_EXCEPTION;
            }
            DyeColor collar = context.getArgument("collar", DyeColor.class);
            DyeColor oldCollar = getCollar(stack);
            if (collar == oldCollar) {
                throw COLLAR_ALREADY_IS_EXCEPTION;
            }
            setCollar(stack, collar);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_COLLAR, EditorUtil.colorTranslation(collar)));
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

        // ... collar
        node.addChild(collarNode);
        // ... get
        collarNode.addChild(collarGetNode);
        // ... set <color>
        collarNode.addChild(collarSetNode);
        collarSetNode.addChild(collarSetColorNode);

        return node;
    }
}