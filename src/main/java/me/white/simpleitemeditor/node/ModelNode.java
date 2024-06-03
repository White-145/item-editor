package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ModelNode implements Node {
    public static final CommandSyntaxException NO_MODEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.error.nomodel")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.model.get";
    private static final String OUTPUT_SET = "commands.edit.model.set";
    private static final String OUTPUT_RESET = "commands.edit.model.reset";

    private static boolean hasModel(ItemStack stack) {
        return stack.contains(DataComponentTypes.CUSTOM_MODEL_DATA);
    }

    private static int getModel(ItemStack stack) {
        if (!hasModel(stack)) {
            return 0;
        }
        return stack.get(DataComponentTypes.CUSTOM_MODEL_DATA).value();
    }

    private static void setModel(ItemStack stack, int model) {
        if (model <= 0) {
            stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
        } else {
            stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(model));
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("model").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasModel(stack)) {
                throw NO_MODEL_EXCEPTION;
            }
            int model = getModel(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, model));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setModelNode = ClientCommandManager.argument("model", IntegerArgumentType.integer(1)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            int model = IntegerArgumentType.getInteger(context, "model");
            if (model == getModel(stack)) {
                throw ALREADY_IS_EXCEPTION;
            }
            setModel(stack, model);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, model));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager.literal("reset").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!hasModel(stack)) {
                throw NO_MODEL_EXCEPTION;
            }
            setModel(stack, 0);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set <model>
        node.addChild(setNode);
        setNode.addChild(setModelNode);

        // ... reset
        node.addChild(resetNode);
    }
}
