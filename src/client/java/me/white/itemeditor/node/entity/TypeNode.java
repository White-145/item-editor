package me.white.itemeditor.node.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.itemeditor.node.EntityNode;
import me.white.itemeditor.util.EditorUtil;
import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class TypeNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.type.error.cannotedit")).create();
    public static final CommandSyntaxException NO_ENTITY_TYPE = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.type.error.noentitytype")).create();
    private static final String OUTPUT_GET = "commands.edit.entity.type.get";
    private static final String OUTPUT_RESET = "commands.edit.entity.type.reset";
    private static final String OUTPUT_SET = "commands.edit.entity.type.set";

    public static boolean canEdit(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SpawnEggItem;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("type")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityType(stack)) throw NO_ENTITY_TYPE;
                    EntityType<?> type = ItemUtil.getEntityType(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, type.getName()));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityType(stack)) throw NO_ENTITY_TYPE;
                    ItemUtil.setEntityType(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<EntityType<?>>> setTypeNode = ClientCommandManager
                .argument("type", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    EntityType<?> type = EditorUtil.getRegistryEntryArgument(context, "type", RegistryKeys.ENTITY_TYPE);
                    ItemUtil.setEntityType(stack, type);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, type.getName()));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<type>]
        node.addChild(setNode);
        setNode.addChild(setTypeNode);
    }
}
