package me.white.simpleitemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ModelNode implements Node {
    public static final CommandSyntaxException NO_MODEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.error.nomodel")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.model.get";
    private static final String OUTPUT_SET = "commands.edit.model.set";
    private static final String OUTPUT_REMOVE = "commands.edit.model.remove";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("model")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasModel(stack)) throw NO_MODEL_EXCEPTION;
                    int model = ItemUtil.getModel(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, model));
                    return model;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setModelNode = ClientCommandManager
                .argument("model", IntegerArgumentType.integer(0))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    int model = IntegerArgumentType.getInteger(context, "model");
                    int old = ItemUtil.getModel(stack);
                    if (model == old) throw ALREADY_IS_EXCEPTION;
                    if (model == 0) {
                        if (!ItemUtil.hasModel(stack)) throw NO_MODEL_EXCEPTION;
                        ItemUtil.setModel(stack, null);

                        context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
                    } else {
                        ItemUtil.setModel(stack, model);

                        context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, model));
                    }

                    EditorUtil.setStack(context.getSource(), stack);
                    return old;
                })
                .build();


        ArgumentCommandNode<FabricClientCommandSource, Integer> removeModelNode = ClientCommandManager
                .argument("remove", IntegerArgumentType.integer(0))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!ItemUtil.hasModel(stack)) throw NO_MODEL_EXCEPTION;
                    int old = ItemUtil.getModel(stack);
                    ItemUtil.setModel(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
                    return old;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set <model>
        node.addChild(setNode);
        setNode.addChild(setModelNode);

        // ... remove
        node.addChild(removeModelNode);
    }
}
