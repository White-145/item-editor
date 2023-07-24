package me.white.itemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
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

public class TrimNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.trim.error.cannotedit")).create();
    public static final CommandSyntaxException NO_TRIM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.trim.error.notrim")).create();
    private static final String OUTPUT_GET = "commands.edit.trim.get";
    private static final String OUTPUT_RESET = "commands.edit.trim.reset";
    private static final String OUTPUT_SET = "commands.edit.trim.set";

    private static boolean canEdit(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("trim")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!EditHelper.hasTrim(stack)) throw NO_TRIM_EXCEPTION;
                ArmorTrim trim = EditHelper.getTrim(stack, context.getSource().getRegistryManager());
                ArmorTrimPattern pattern = trim.getPattern().value();
                ArmorTrimMaterial material = trim.getMaterial().value();

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, pattern.description(), material.description()));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!EditHelper.hasTrim(stack)) throw NO_TRIM_EXCEPTION;
                EditHelper.setTrim(stack, null);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Reference<ArmorTrimPattern>> setPatternNode = ClientCommandManager
            .argument("pattern", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.TRIM_PATTERN))
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Reference<ArmorTrimMaterial>> setPatternMaterialNode = ClientCommandManager
            .argument("material", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.TRIM_MATERIAL))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                ArmorTrimPattern pattern = Util.getRegistryEntryArgument(context, "pattern", RegistryKeys.TRIM_PATTERN);
                ArmorTrimMaterial material = Util.getRegistryEntryArgument(context, "material", RegistryKeys.TRIM_MATERIAL);
                EditHelper.setTrim(stack, pattern, material);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, pattern.description(), material.description()));
                return 1;
            })
            .build();
        
        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<pattern>] <material>
        node.addChild(setNode);
        setNode.addChild(setPatternNode);
        setPatternNode.addChild(setPatternMaterialNode);
    }
}
