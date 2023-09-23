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

public class SilenceNode implements Node {
    private static final String OUTPUT_GET_ENABLED = "commands.edit.entity.silence.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.entity.silence.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.entity.silence.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.entity.silence.disable";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("silence")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    boolean silence = ItemUtil.getEntitySilence(stack);

                    context.getSource().sendFeedback(Text.translatable(silence ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
                    return silence ? 1 : 0;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    boolean silence = ItemUtil.getEntitySilence(stack);
                    ItemUtil.setEntitySilence(stack, !silence);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(silence ? OUTPUT_DISABLE : OUTPUT_ENABLE));
                    return silence ? 0 : 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... toggle
        node.addChild(toggleNode);
    }
}
