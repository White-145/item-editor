package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
//? if <1.21.6 {
/*import me.white.simpleitemeditor.node.tooltip.TooltipNode_1_21_1;
*///?}
//? if >=1.21.4 {
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
//?} else {
/*import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.item.trim.ArmorTrimMaterial;
*///?}
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;

public class TrimNode implements Node {
    private static final CommandSyntaxException ISNT_ARMOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.trim.error.isntarmor")).create();
    private static final CommandSyntaxException NO_TRIM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.trim.error.notrim")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.trim.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.trim.get";
    private static final String OUTPUT_REMOVE = "commands.edit.trim.remove";
    private static final String OUTPUT_SET = "commands.edit.trim.set";

    private static boolean isArmor(ItemStack stack) {
        return stack.isIn(ItemTags.TRIMMABLE_ARMOR);
    }

    private static boolean hasTrim(ItemStack stack) {
        return stack.contains(DataComponentTypes.TRIM);
    }

    private static ArmorTrim getTrim(ItemStack stack) {
        if (!hasTrim(stack)) {
            return null;
        }
        return stack.get(DataComponentTypes.TRIM);
    }

    private static void removeTrim(ItemStack stack) {
        stack.remove(DataComponentTypes.TRIM);
    }

    private static void setTrim(ItemStack stack, ArmorTrim trim) {
        //? if <1.21.6 {
        /*trim = trim.withShowInTooltip(TooltipNode_1_21_1.TooltipPart.TRIM.get(stack));
        *///?}
        stack.set(DataComponentTypes.TRIM, trim);
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("trim").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isArmor(stack)) {
                throw ISNT_ARMOR_EXCEPTION;
            }
            if (!hasTrim(stack)) {
                throw NO_TRIM_EXCEPTION;
            }
            ArmorTrim trim = getTrim(stack);
            //? if >=1.21.4 {
            ArmorTrimPattern pattern = trim.pattern().value();
            ArmorTrimMaterial material = trim.material().value();
            //?} else {
            /*ArmorTrimPattern pattern = trim.getPattern().value();
            ArmorTrimMaterial material = trim.getMaterial().value();
            *///?}

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, pattern.description(), material.description()));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setPatternNode = commandManager.argument("pattern", RegistryArgumentType.registryEntry(RegistryKeys.TRIM_PATTERN, registryAccess)).build();

        CommandNode<S> setPatternMaterialNode = commandManager.argument("material", RegistryArgumentType.registryEntry(RegistryKeys.TRIM_MATERIAL, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isArmor(stack)) {
                throw ISNT_ARMOR_EXCEPTION;
            }
            ArmorTrimPattern pattern = RegistryArgumentType.getRegistryEntry(context, "pattern", RegistryKeys.TRIM_PATTERN);
            ArmorTrimMaterial material = RegistryArgumentType.getRegistryEntry(context, "material", RegistryKeys.TRIM_MATERIAL);
            //? if >=1.21.4 {
            Registry<ArmorTrimPattern> patternRegistry = context.getSource().getRegistryManager().getOrThrow(RegistryKeys.TRIM_PATTERN);
            Registry<ArmorTrimMaterial> materialRegistry = context.getSource().getRegistryManager().getOrThrow(RegistryKeys.TRIM_MATERIAL);
            //?} else {
            /*Registry<ArmorTrimPattern> patternRegistry = context.getSource().getRegistryManager().get(RegistryKeys.TRIM_PATTERN);
            Registry<ArmorTrimMaterial> materialRegistry = context.getSource().getRegistryManager().get(RegistryKeys.TRIM_MATERIAL);
            *///?}
            RegistryEntry<ArmorTrimPattern> patternEntry = patternRegistry.getEntry(pattern);
            RegistryEntry<ArmorTrimMaterial> materialEntry = materialRegistry.getEntry(material);
            ArmorTrim trim = new ArmorTrim(materialEntry, patternEntry);
            if (trim.equals(getTrim(stack))) {
                throw ALREADY_IS_EXCEPTION;
            }
            setTrim(stack, trim);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, pattern.description(), material.description()));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isArmor(stack)) {
                throw ISNT_ARMOR_EXCEPTION;
            }
            if (!hasTrim(stack)) {
                throw NO_TRIM_EXCEPTION;
            }
            removeTrim(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get
        node.addChild(getNode);

        // ... set <pattern> <material>
        node.addChild(setNode);
        setNode.addChild(setPatternNode);
        setPatternNode.addChild(setPatternMaterialNode);

        // ... remove
        node.addChild(removeNode);

        return node;
    }
}
