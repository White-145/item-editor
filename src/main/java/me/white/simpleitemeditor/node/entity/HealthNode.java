package me.white.simpleitemeditor.node.entity;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
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

public class HealthNode implements Node {
    public static final CommandSyntaxException NO_HEALTH_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.health.error.nohealth")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.health.error.alreadyis")).create();
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.health.error.cannotedit")).create();
    private static final String OUTPUT_GET = "commands.edit.entity.health.get";
    private static final String OUTPUT_RESET = "commands.edit.entity.health.reset";
    private static final String OUTPUT_SET = "commands.edit.entity.health.set";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("health")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityHealth(stack)) throw NO_HEALTH_EXCEPTION;
                    float health = ItemUtil.getEntityHealth(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, String.format("%.2f", health)));
                    return (int) health;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityHealth(stack)) throw NO_HEALTH_EXCEPTION;
                    ItemUtil.setEntityHealth(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Float> setHealthNode = ClientCommandManager
                .argument("health", FloatArgumentType.floatArg(0))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    float health = FloatArgumentType.getFloat(context, "health");
                    if (ItemUtil.hasEntityHealth(stack) && ItemUtil.getEntityHealth(stack) == health) throw ALREADY_IS_EXCEPTION;
                    ItemUtil.setEntityHealth(stack, health);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, String.format("%.2f", health)));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<health>]
        node.addChild(setNode);
        setNode.addChild(setHealthNode);
    }
}
