package me.white.itemeditor.node.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.itemeditor.argument.Vec2ArgumentType;
import me.white.itemeditor.node.EntityNode;
import me.white.itemeditor.node.Node;
import me.white.itemeditor.util.EditorUtil;
import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

public class RotationNode implements Node {
    public static final CommandSyntaxException NO_ROTATION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.rotation.error.norotation")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.rotation.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.entity.rotation.get";
    private static final String OUTPUT_RESET = "commands.edit.entity.rotation.reset";
    private static final String OUTPUT_SET = "commands.edit.entity.rotation.set";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("rotation")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityRotation(stack)) throw NO_ROTATION_EXCEPTION;
                    Vec2f motion = ItemUtil.getEntityRotation(stack);
                    String x = String.format("%.2f", motion.x);
                    String y = String.format("%.2f", motion.y);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, x, y));
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
                    if (!ItemUtil.hasEntityRotation(stack, false)) throw NO_ROTATION_EXCEPTION;
                    ItemUtil.setEntityRotation(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Vec2f> setRotationNode = ClientCommandManager
                .argument("rotation", Vec2ArgumentType.vec2f())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    Vec2f rotation = Vec2ArgumentType.getVec2fArgument(context, "rotation");
                    if (ItemUtil.hasEntityRotation(stack)) {
                        Vec2f oldRotation = ItemUtil.getEntityRotation(stack);
                        if (oldRotation.equals(rotation)) throw ALREADY_IS_EXCEPTION;
                    }
                    ItemUtil.setEntityRotation(stack, rotation);
                    String x = String.format("%.2f", rotation.x);
                    String y = String.format("%.2f", rotation.y);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, x, y));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<motion>]
        node.addChild(setNode);
        setNode.addChild(setRotationNode);
    }
}
