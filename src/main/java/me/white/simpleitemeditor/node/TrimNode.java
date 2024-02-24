package me.white.simpleitemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.text.Text;

public class TrimNode implements Node {
    public static final CommandSyntaxException ISNT_ARMOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.trim.error.isntarmor")).create();
    public static final CommandSyntaxException NO_TRIM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.trim.error.notrim")).create();
    private static final String OUTPUT_GET = "commands.edit.trim.get";
    private static final String OUTPUT_REMOVE = "commands.edit.trim.remove";
    private static final String OUTPUT_SET = "commands.edit.trim.set";

    private static boolean isArmor(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("trim")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isArmor(stack)) throw ISNT_ARMOR_EXCEPTION;
                    if (!ItemUtil.hasTrim(stack, null)) throw NO_TRIM_EXCEPTION;
                    ArmorTrim trim = ItemUtil.getTrim(stack, context.getSource().getRegistryManager());
                    ArmorTrimPattern pattern = trim.getPattern().value();
                    ArmorTrimMaterial material = trim.getMaterial().value();

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, pattern.description(), material.description()));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isArmor(stack)) throw ISNT_ARMOR_EXCEPTION;
                    if (!ItemUtil.hasTrim(stack, context.getSource().getRegistryManager())) throw NO_TRIM_EXCEPTION;
                    ItemUtil.setTrim(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Reference<ArmorTrimPattern>> setPatternNode = ClientCommandManager
                .argument("pattern", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.TRIM_PATTERN))
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Reference<ArmorTrimMaterial>> setPatternMaterialNode = ClientCommandManager
                .argument("material", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.TRIM_MATERIAL))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isArmor(stack)) throw ISNT_ARMOR_EXCEPTION;
                    ArmorTrimPattern pattern = EditorUtil.getRegistryEntryArgument(context, "pattern", RegistryKeys.TRIM_PATTERN);
                    ArmorTrimMaterial material = EditorUtil.getRegistryEntryArgument(context, "material", RegistryKeys.TRIM_MATERIAL);
                    ItemUtil.setTrim(stack, pattern, material);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, pattern.description(), material.description()));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isArmor(stack)) throw ISNT_ARMOR_EXCEPTION;
                    if (!ItemUtil.hasTrim(stack, context.getSource().getRegistryManager())) throw NO_TRIM_EXCEPTION;
                    ItemUtil.setTrim(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<pattern> <material>]
        node.addChild(setNode);
        setNode.addChild(setPatternNode);
        setPatternNode.addChild(setPatternMaterialNode);

        // ... remove
        node.addChild(removeNode);
    }
}
