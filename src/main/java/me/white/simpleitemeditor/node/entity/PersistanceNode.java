package me.white.simpleitemeditor.node.entity;

import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.node.EntityNode;
import me.white.simpleitemeditor.node.Node;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class PersistanceNode implements Node {
    private static final String OUTPUT_GET_ENABLED = "commands.edit.entity.persistance.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.entity.persistance.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.entity.persistance.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.entity.persistance.disable";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("persistance")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    boolean persistance = ItemUtil.getEntityPersistance(stack);

                    context.getSource().sendFeedback(Text.translatable(persistance ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
                    return persistance ? 1 : 0;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    boolean persistance = ItemUtil.getEntityPersistance(stack);
                    ItemUtil.setEntityPersistance(stack, !persistance);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(persistance ? OUTPUT_DISABLE : OUTPUT_ENABLE));
                    return persistance ? 0 : 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... toggle
        node.addChild(toggleNode);
    }
}
