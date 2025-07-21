package me.white.simpleitemeditor.node.mob;

//? if >=1.21.5 {
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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public class CatNode implements Node {
    private static final CommandSyntaxException ISNT_CAT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cat.error.isntaxolotl")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cat.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cat.error.novariant")).create();
    private static final CommandSyntaxException COLLAR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.cat.error.collaralreadyis")).create();
    private static final String OUTPUT_GET_VARIANT = "command.edit.mob.cat.variantget";
    private static final String OUTPUT_SET_VARIANT = "command.edit.mob.cat.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.cat.variantremove";
    private static final String OUTPUT_GET_COLLAR = "command.edit.mob.cat.collarget";
    private static final String OUTPUT_SET_COLLAR = "command.edit.mob.cat.collarset";

    private static Text colorTranslation(DyeColor color) {
        return Text.translatable("color.minecraft." + color.getId());
    }

    private static boolean isCat(ItemStack stack) {
        EntityType<?> entityType = EditorUtil.getEntityType(stack);
        return entityType == EntityType.CAT;
    }

    private static boolean hasVariant(ItemStack stack) {
        return stack.contains(DataComponentTypes.CAT_VARIANT);
    }

    private static CatVariant getVariant(ItemStack stack) {
        return stack.get(DataComponentTypes.CAT_VARIANT).value();
    }

    private static DyeColor getCollar(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.CAT_COLLAR)) {
            return DyeColor.RED;
        }
        return stack.get(DataComponentTypes.CAT_COLLAR);
    }

    private static void setVariant(ItemStack stack, CatVariant variant) {
        stack.set(DataComponentTypes.CAT_VARIANT, RegistryEntry.of(variant));
    }

    private static void setCollar(ItemStack stack, DyeColor collar) {
        stack.set(DataComponentTypes.CAT_COLLAR, collar);
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponentTypes.CAT_VARIANT);
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

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, variant.assetInfo().id()));
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

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_VARIANT, variant.assetInfo().id()));
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

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_COLLAR, colorTranslation(collar)));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_COLLAR, colorTranslation(collar)));
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
//?}
