package me.white.simpleitemeditor.node.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.node.EntityNode;
import me.white.simpleitemeditor.node.Node;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class GravityNode implements Node {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.gravity.error.cannotedit")).create();
    private static final String OUTPUT_GET_ENABLED = "commands.edit.entity.gravity.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.entity.gravity.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.entity.gravity.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.entity.gravity.disable";

    private static boolean canEdit(ItemStack stack) {
        return EditorUtil.isType(LivingEntity.class, stack);
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("gravity")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    boolean gravity = ItemUtil.getEntityGravity(stack);

                    context.getSource().sendFeedback(Text.translatable(gravity ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
                    return gravity ? 1 : 0;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    boolean gravity = ItemUtil.getEntityGravity(stack);
                    ItemUtil.setEntityGravity(stack, !gravity);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(gravity ? OUTPUT_DISABLE : OUTPUT_ENABLE));
                    return gravity ? 0 : 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... toggle
        node.addChild(toggleNode);
    }
}
