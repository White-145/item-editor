package me.white.simpleitemeditor.node.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.argument.Vec3ArgumentType;
import me.white.simpleitemeditor.node.EntityNode;
import me.white.simpleitemeditor.node.Node;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class MotionNode implements Node {
    public static final CommandSyntaxException NO_MOTION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.motion.error.nomotion")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.motion.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.entity.motion.get";
    private static final String OUTPUT_RESET = "commands.edit.entity.motion.reset";
    private static final String OUTPUT_SET = "commands.edit.entity.motion.set";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("motion")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityMotion(stack)) throw NO_MOTION_EXCEPTION;
                    Vec3d motion = ItemUtil.getEntityMotion(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(motion)));
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
                    if (!ItemUtil.hasEntityMotion(stack, false)) throw NO_MOTION_EXCEPTION;
                    ItemUtil.setEntityMotion(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Vec3d> setMotionNode = ClientCommandManager
                .argument("motion", Vec3ArgumentType.vec3d())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    Vec3d motion = Vec3ArgumentType.getVec3dArgument(context, "motion");
                    if (ItemUtil.hasEntityMotion(stack)) {
                        Vec3d oldMotion = ItemUtil.getEntityMotion(stack);
                        if (oldMotion.equals(motion)) throw ALREADY_IS_EXCEPTION;
                    }
                    ItemUtil.setEntityMotion(stack, motion);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(motion)));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<motion>]
        node.addChild(setNode);
        setNode.addChild(setMotionNode);
    }
}
