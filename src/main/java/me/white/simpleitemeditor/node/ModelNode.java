package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ModelNode implements Node {
    private static final CommandSyntaxException NO_MODEL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.error.nomodel")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.model.error.alreadyis")).create();
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

    @Override
    public void register(CommonCommandManager<CommandSource> commandManager, CommandNode<CommandSource> rootNode, CommandRegistryAccess registryAccess) {
        CommandNode<CommandSource> node = commandManager.literal("model").build();

        CommandNode<CommandSource> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasModel(stack)) {
                throw NO_MODEL_EXCEPTION;
            }
            int model = getModel(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, model));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> setNode = commandManager.literal("set").build();

        CommandNode<CommandSource> setModelNode = commandManager.argument("model", IntegerArgumentType.integer(1)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int model = IntegerArgumentType.getInteger(context, "model");
            if (model == getModel(stack)) {
                throw ALREADY_IS_EXCEPTION;
            }
            setModel(stack, model);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, model));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> resetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasModel(stack)) {
                throw NO_MODEL_EXCEPTION;
            }
            setModel(stack, 0);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_RESET));
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
