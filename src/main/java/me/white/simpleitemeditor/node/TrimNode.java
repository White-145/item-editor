package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
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
        stack.set(DataComponentTypes.TRIM, trim.withShowInTooltip(TooltipNode.TooltipPart.TRIM.get(stack)));
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("trim").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            EditorUtil.checkHasItem(stack);
            if (!isArmor(stack)) {
                throw ISNT_ARMOR_EXCEPTION;
            }
            if (!hasTrim(stack)) {
                throw NO_TRIM_EXCEPTION;
            }
            ArmorTrim trim = getTrim(stack);
            ArmorTrimPattern pattern = trim.getPattern().value();
            ArmorTrimMaterial material = trim.getMaterial().value();

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, pattern.description(), material.description()));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<ArmorTrimPattern>> setPatternNode = ClientCommandManager.argument("pattern", RegistryArgumentType.registryEntry(RegistryKeys.TRIM_PATTERN, registryAccess)).build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<ArmorTrimMaterial>> setPatternMaterialNode = ClientCommandManager.argument("material", RegistryArgumentType.registryEntry(RegistryKeys.TRIM_MATERIAL, registryAccess)).executes(context -> {
            EditorUtil.checkHasCreative(context.getSource());
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            if (!isArmor(stack)) {
                throw ISNT_ARMOR_EXCEPTION;
            }
            ArmorTrimPattern pattern = RegistryArgumentType.getRegistryEntry(context, "pattern", RegistryKeys.TRIM_PATTERN);
            ArmorTrimMaterial material = RegistryArgumentType.getRegistryEntry(context, "material", RegistryKeys.TRIM_MATERIAL);
            Registry<ArmorTrimPattern> patternRegistry = context.getSource().getRegistryManager().get(RegistryKeys.TRIM_PATTERN);
            Registry<ArmorTrimMaterial> materialRegistry = context.getSource().getRegistryManager().get(RegistryKeys.TRIM_MATERIAL);
            RegistryEntry<ArmorTrimPattern> patternEntry = patternRegistry.getEntry(pattern);
            RegistryEntry<ArmorTrimMaterial> materialEntry = materialRegistry.getEntry(material);
            ArmorTrim trim = new ArmorTrim(materialEntry, patternEntry);
            if (trim.equals(getTrim(stack))) {
                throw ALREADY_IS_EXCEPTION;
            }
            setTrim(stack, trim);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, pattern.description(), material.description()));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").executes(context -> {
            EditorUtil.checkHasCreative(context.getSource());
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            EditorUtil.checkHasItem(stack);
            if (!isArmor(stack)) {
                throw ISNT_ARMOR_EXCEPTION;
            }
            if (!hasTrim(stack)) {
                throw NO_TRIM_EXCEPTION;
            }
            removeTrim(stack);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set <pattern> <material>
        node.addChild(setNode);
        setNode.addChild(setPatternNode);
        setPatternNode.addChild(setPatternMaterialNode);

        // ... remove
        node.addChild(removeNode);
    }
}
