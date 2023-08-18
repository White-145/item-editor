package me.white.itemeditor.node.entity;

import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.itemeditor.node.EntityNode;
import me.white.itemeditor.node.Node;
import me.white.itemeditor.util.EditorUtil;
import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class GlowNode implements Node {
    private static final String OUTPUT_GET_ENABLED = "commands.edit.entity.glow.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.entity.glow.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.entity.glow.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.entity.glow.disable";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("glow")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    boolean glow = ItemUtil.getEntityGlow(stack);

                    context.getSource().sendFeedback(Text.translatable(glow ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
                    return glow ? 1 : 0;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    boolean glow = ItemUtil.getEntityGlow(stack);
                    ItemUtil.setEntityGlow(stack, !glow);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(glow ? OUTPUT_DISABLE : OUTPUT_ENABLE));
                    return glow ? 0 : 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... toggle
        node.addChild(toggleNode);
    }
}
