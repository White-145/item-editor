package me.white.itemeditor.node.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.itemeditor.argument.PositionArgument;
import me.white.itemeditor.argument.PositionArgumentType;
import me.white.itemeditor.node.EntityNode;
import me.white.itemeditor.node.Node;
import me.white.itemeditor.util.EditorUtil;
import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class PositionNode implements Node {
    public static final CommandSyntaxException NO_POSITION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.position.error.noposition")).create();
    private static final String OUTPUT_GET = "commands.edit.entity.position.get";
    private static final String OUTPUT_RESET = "commands.edit.entity.position.reset";
    private static final String OUTPUT_SET = "commands.edit.entity.position.set";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("position")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityPosition(stack, false)) throw NO_POSITION_EXCEPTION;
                    Vec3d pos = ItemUtil.getEntityPosition(stack);
                    String x = String.format("%.2f", pos.x);
                    String y = String.format("%.2f", pos.y);
                    String z = String.format("%.2f", pos.z);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, x, y, z));
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
                    if (!ItemUtil.hasEntityPosition(stack, false)) throw NO_POSITION_EXCEPTION;
                    ItemUtil.setEntityPosition(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, PositionArgument> setPositionNode = ClientCommandManager
                .argument("position", PositionArgumentType.position())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    Vec3d pos = PositionArgumentType.getAbsolutePosition(context, "position");
                    ItemUtil.setEntityPosition(stack, pos);
                    String x = String.format("%.2f", pos.x);
                    String y = String.format("%.2f", pos.y);
                    String z = String.format("%.2f", pos.z);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, x, y, z));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<position>]
        node.addChild(setNode);
        setNode.addChild(setPositionNode);
    }
}
