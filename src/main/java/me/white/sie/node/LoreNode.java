package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.argument.LegacyTextArgumentType;
import me.white.sie.util.EditorUtil;
import me.white.sie.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;

public class LoreNode implements Node {
    private static final CommandSyntaxException NO_LORE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.lore.error.nolore")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.lore.error.alreadyis")).create();
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
        if (!stack.getComponents().has(DataComponents.LORE)) {
            return false;
        }
        return !stack.get(DataComponents.LORE).lines().isEmpty();
    }

    public static List<Component> getLore(ItemStack stack) {
        if (!hasLore(stack)) {
            return List.of();
        }
        return stack.get(DataComponents.LORE).lines();
    }

    public static void setLore(ItemStack stack, List<Component> lore) {
        if (lore == null || lore.isEmpty()) {
            stack.remove(DataComponents.LORE);
        } else {
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("lore").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Component> lore = getLore(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET));
            for (int i = 0; i < lore.size(); ++i) {
                EditorUtil.sendFeedback(context.getSource(), Component.empty().append(Component.literal(i + ". ").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))).append(TextUtil.copyable(lore.get(i))));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> getIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Component> lore = getLore(stack);
            if (index >= lore.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
            }
            Component line = lore.get(index);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_LINE, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0, 255)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int index = IntegerArgumentType.getInteger(context, "index");
            List<Component> lore = new ArrayList<>(getLore(stack));
            if (index >= lore.size()) {
                int off = index - lore.size() + 1;
                for (int i = 0; i < off; ++i) {
                    lore.add(Component.empty());
                }
            } else {
                Component oldLine = lore.get(index);
                if (oldLine.equals(Component.empty())) {
                    throw ALREADY_IS_EXCEPTION;
                }
            }
            lore.set(index, Component.empty());
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setIndexLineNode = commandManager.argument("line", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int index = IntegerArgumentType.getInteger(context, "index");
            Component line = LegacyTextArgumentType.getText(context, "line");
            List<Component> lore = new ArrayList<>(getLore(stack));
            if (index >= lore.size()) {
                int off = index - lore.size() + 1;
                for (int i = 0; i < off; ++i) {
                    lore.add(Component.empty());
                }
            } else {
                Component oldLine = lore.get(index);
                if (oldLine.equals(line)) {
                    throw ALREADY_IS_EXCEPTION;
                }
            }
            lore.set(index, line);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").build();

        CommandNode<S> removeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Component> lore = new ArrayList<>(getLore(stack));
            if (index >= lore.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
            }
            lore.remove(index);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> addNode = commandManager.literal("add").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            List<Component> lore = new ArrayList<>(getLore(stack));
            lore.add(Component.empty());
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_ADD, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> addLineNode = commandManager.argument("line", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Component line = LegacyTextArgumentType.getText(context, "line");
            List<Component> lore = new ArrayList<>(getLore(stack));
            lore.add(line);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_ADD, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> insertNode = commandManager.literal("insert").build();

        CommandNode<S> insertIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0, 255)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int index = IntegerArgumentType.getInteger(context, "index");
            List<Component> lore = new ArrayList<>(getLore(stack));
            if (index > lore.size()) {
                int off = index - lore.size();
                for (int i = 0; i < off; ++i) {
                    lore.add(Component.empty());
                }
            }
            lore.add(index, Component.empty());
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_INSERT, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> insertIndexLineNode = commandManager.argument("line", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int index = IntegerArgumentType.getInteger(context, "index");
            Component line = LegacyTextArgumentType.getText(context, "line");
            List<Component> lore = new ArrayList<>(getLore(stack));
            if (index > lore.size()) {
                int off = index - lore.size();
                for (int i = 0; i < off; ++i) {
                    lore.add(Component.empty());
                }
            }
            lore.add(index, line);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_INSERT, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            setLore(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearBeforeNode = commandManager.literal("before").build();

        CommandNode<S> clearBeforeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Component> lore = getLore(stack);
            if (index > lore.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
            }
            lore = lore.subList(index, lore.size());
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR_BEFORE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearAfterNode = commandManager.literal("after").build();

        CommandNode<S> clearAfterIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Component> lore = getLore(stack);
            if (index >= lore.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
            }
            lore = lore.subList(0, index + 1);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR_AFTER, index));
            return Command.SINGLE_SUCCESS;
        }).build();

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

        // ... clear before <index>
        clearNode.addChild(clearBeforeNode);
        clearBeforeNode.addChild(clearBeforeIndexNode);

        // ... clear after <index>
        clearNode.addChild(clearAfterNode);
        clearAfterNode.addChild(clearAfterIndexNode);

        return node;
    }
}
