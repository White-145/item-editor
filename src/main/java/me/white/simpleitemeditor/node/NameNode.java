package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.argument.TextArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class NameNode implements Node {
    public static final CommandSyntaxException NO_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.noname")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.name.get";
    private static final String OUTPUT_SET = "commands.edit.name.set";
    private static final String OUTPUT_REMOVE = "commands.edit.name.remove";

    private static boolean hasName(ItemStack stack) {
        return stack.contains(DataComponentTypes.ITEM_NAME);
    }

    private static Text getName(ItemStack stack) {
        if (!hasName(stack)) {
            return null;
        }
        return stack.get(DataComponentTypes.ITEM_NAME);
    }

    private static void setName(ItemStack stack, Text name) {
        if (name == null) {
            stack.remove(DataComponentTypes.ITEM_NAME);
        } else {
            stack.set(DataComponentTypes.ITEM_NAME, name);
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("name").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasName(stack)) {
                throw NO_NAME_EXCEPTION;
            }
            Text name = getName(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (Text.empty().equals(getName(stack))) {
                throw ALREADY_IS_EXCEPTION;
            }
            setName(stack, Text.empty());

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Text> setNameNode = ClientCommandManager.argument("name", TextArgumentType.text()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            Text name = TextArgumentType.getText(context, "name");
            if (name.equals(getName(stack))) {
                throw ALREADY_IS_EXCEPTION;
            }
            setName(stack, name);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasName(stack)) {
                throw NO_NAME_EXCEPTION;
            }
            setName(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<name>]
        node.addChild(setNode);
        setNode.addChild(setNameNode);

        // ... remove
        node.addChild(removeNode);
    }
}
