package me.white.simpleitemeditor.node;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class LoreNode implements Node {
    public static final CommandSyntaxException NO_LORE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.lore.error.nolore")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.lore.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.lore.get";
    private static final String OUTPUT_GET_LINE = "commands.edit.lore.getline";
    private static final String OUTPUT_SET = "commands.edit.lore.set";
    private static final String OUTPUT_INSERT = "commands.edit.lore.insert";
    private static final String OUTPUT_ADD = "commands.edit.lore.add";
    private static final String OUTPUT_REMOVE = "commands.edit.lore.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.lore.clear";
    private static final String OUTPUT_CLEAR_BEFORE = "commands.edit.lore.clearbefore";
    private static final String OUTPUT_CLEAR_AFTER = "commands.edit.lore.clearafter";

    public static boolean hasLore(ItemStack stack) {
        if (!stack.getComponents().contains(DataComponentTypes.LORE)) {
            return false;
        }
        return !stack.get(DataComponentTypes.LORE).lines().isEmpty();
    }

    public static List<Text> getLore(ItemStack stack) {
        if (!hasLore(stack)) {
            return List.of();
        }
        return stack.get(DataComponentTypes.LORE).lines();
    }

    public static void setLore(ItemStack stack, List<Text> lore) {
        if (lore == null || lore.isEmpty()) {
            stack.remove(DataComponentTypes.LORE);
        } else {
            stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("lore").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Text> lore = getLore(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
            for (int i = 0; i < lore.size(); ++i) {
                context.getSource().sendFeedback(Text.empty().append(Text.literal(i + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(TextUtil.copyable(lore.get(i))));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> getIndexNode = ClientCommandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Text> lore = getLore(stack);
            if (index >= lore.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
            }
            Text line = lore.get(index);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_LINE, index, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setIndexNode = ClientCommandManager.argument("index", IntegerArgumentType.integer(0, 255)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<Text> lore = new ArrayList<>(getLore(stack));
            if (index >= lore.size()) {
                int off = index - lore.size() + 1;
                for (int i = 0; i < off; ++i) {
                    lore.add(Text.empty());
                }
            } else {
                Text oldLine = lore.get(index);
                if (oldLine.equals(Text.empty())) {
                    throw ALREADY_IS_EXCEPTION;
                }
            }
            lore.set(index, Text.empty());
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, index, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Text> setIndexLineNode = ClientCommandManager.argument("line", TextArgumentType.text()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            Text line = TextArgumentType.getText(context, "line");
            List<Text> lore = new ArrayList<>(getLore(stack));
            if (index >= lore.size()) {
                int off = index - lore.size() + 1;
                for (int i = 0; i < off; ++i) {
                    lore.add(Text.empty());
                }
            } else {
                Text oldLine = lore.get(index);
                if (oldLine.equals(line)) {
                    throw ALREADY_IS_EXCEPTION;
                }
            }
            lore.set(index, line);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, index, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> removeIndexNode = ClientCommandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Text> lore = new ArrayList<>(getLore(stack));
            if (index >= lore.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
            }
            lore.remove(index);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager.literal("add").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            List<Text> lore = new ArrayList<>(getLore(stack));
            lore.add(Text.empty());
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Text> addLineNode = ClientCommandManager.argument("line", TextArgumentType.text()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            Text line = TextArgumentType.getText(context, "line");
            List<Text> lore = new ArrayList<>(getLore(stack));
            lore.add(line);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> insertNode = ClientCommandManager.literal("insert").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> insertIndexNode = ClientCommandManager.argument("index", IntegerArgumentType.integer(0, 255)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<Text> lore = new ArrayList<>(getLore(stack));
            if (index > lore.size()) {
                int off = index - lore.size();
                for (int i = 0; i < off; ++i) {
                    lore.add(Text.empty());
                }
            }
            lore.add(index, Text.empty());
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, "", index));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Text> insertIndexLineNode = ClientCommandManager.argument("line", TextArgumentType.text()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            Text line = TextArgumentType.getText(context, "line");
            List<Text> lore = new ArrayList<>(getLore(stack));
            if (index > lore.size()) {
                int off = index - lore.size();
                for (int i = 0; i < off; ++i) {
                    lore.add(Text.empty());
                }
            }
            lore.add(index, line);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, TextUtil.copyable(line), index));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager.literal("clear").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            setLore(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> clearBeforeNode = ClientCommandManager.literal("before").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> clearBeforeIndexNode = ClientCommandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Text> lore = getLore(stack);
            if (index > lore.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
            }
            lore = lore.subList(index, lore.size());
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_BEFORE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> clearAfterNode = ClientCommandManager.literal("after").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> clearAfterIndexNode = ClientCommandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Text> lore = getLore(stack);
            if (index >= lore.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
            }
            lore = lore.subList(0, index + 1);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_AFTER, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get [<index>]
        node.addChild(getNode);
        getNode.addChild(getIndexNode);

        // ... set <index> [<line>]
        node.addChild(setNode);
        setNode.addChild(setIndexNode);
        setIndexNode.addChild(setIndexLineNode);

        // ... remove <index>
        node.addChild(removeNode);
        removeNode.addChild(removeIndexNode);

        // ... add [<line>]
        node.addChild(addNode);
        addNode.addChild(addLineNode);

        // ... insert <index> [<line>]
        node.addChild(insertNode);
        insertNode.addChild(insertIndexNode);
        insertIndexNode.addChild(insertIndexLineNode);

        // ... clear
        node.addChild(clearNode);
        // ... [before] <index>
        clearNode.addChild(clearBeforeNode);
        clearBeforeNode.addChild(clearBeforeIndexNode);
        // ... [after] <index>
        clearNode.addChild(clearAfterNode);
        clearAfterNode.addChild(clearAfterIndexNode);
    }
}
