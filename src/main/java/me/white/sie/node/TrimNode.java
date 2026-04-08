package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.argument.RegistryArgumentType;
import me.white.sie.util.EditorUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public class TrimNode implements Node {
    private static final CommandSyntaxException ISNT_ARMOR_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.trim.error.isntarmor")).create();
    private static final CommandSyntaxException NO_TRIM_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.trim.error.notrim")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.trim.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.trim.get";
    private static final String OUTPUT_REMOVE = "commands.edit.trim.remove";
    private static final String OUTPUT_SET = "commands.edit.trim.set";

    private static boolean isArmor(ItemStack stack) {
        return stack.is(ItemTags.TRIMMABLE_ARMOR);
    }

    private static boolean hasTrim(ItemStack stack) {
        return stack.has(DataComponents.TRIM);
    }

    private static ArmorTrim getTrim(ItemStack stack) {
        if (!hasTrim(stack)) {
            return null;
        }
        return stack.get(DataComponents.TRIM);
    }

    private static void removeTrim(ItemStack stack) {
        stack.remove(DataComponents.TRIM);
    }

    private static void setTrim(ItemStack stack, ArmorTrim trim) {
        stack.set(DataComponents.TRIM, trim);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
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
            TrimPattern pattern = trim.pattern().value();
            TrimMaterial material = trim.material().value();

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET, pattern.description(), material.description()));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setPatternNode = commandManager.argument("pattern", RegistryArgumentType.registryEntry(Registries.TRIM_PATTERN, registryAccess)).build();

        CommandNode<S> setPatternMaterialNode = commandManager.argument("material", RegistryArgumentType.registryEntry(Registries.TRIM_MATERIAL, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isArmor(stack)) {
                throw ISNT_ARMOR_EXCEPTION;
            }
            TrimPattern pattern = RegistryArgumentType.getRegistryEntry(context, "pattern", Registries.TRIM_PATTERN);
            TrimMaterial material = RegistryArgumentType.getRegistryEntry(context, "material", Registries.TRIM_MATERIAL);
            Registry<TrimPattern> patternRegistry = context.getSource().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
            Registry<TrimMaterial> materialRegistry = context.getSource().registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL);
            Holder<TrimPattern> patternEntry = patternRegistry.wrapAsHolder(pattern);
            Holder<TrimMaterial> materialEntry = materialRegistry.wrapAsHolder(material);
            ArmorTrim trim = new ArmorTrim(materialEntry, patternEntry);
            if (trim.equals(getTrim(stack))) {
                throw ALREADY_IS_EXCEPTION;
            }
            setTrim(stack, trim);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, pattern.description(), material.description()));
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
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE));
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
