package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.LegacyTextArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class LoreNode implements Node {
    private static final CommandSyntaxException NO_LORE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.lore.error.nolore")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.lore.error.alreadyis")).create();
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

    @Override
    public void register(CommonCommandManager<CommandSource> commandManager, CommandNode<CommandSource> rootNode, CommandRegistryAccess registryAccess) {
        CommandNode<CommandSource> node = commandManager.literal("lore").build();

        CommandNode<CommandSource> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Text> lore = getLore(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET));
            for (int i = 0; i < lore.size(); ++i) {
                EditorUtil.sendFeedback(context.getSource(), Text.empty().append(Text.literal(i + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(TextUtil.copyable(lore.get(i))));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> getIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            List<Text> lore = getLore(stack);
            if (index >= lore.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, lore.size());
            }
            Text line = lore.get(index);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_LINE, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> setNode = commandManager.literal("set").build();

        CommandNode<CommandSource> setIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0, 255)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> setIndexLineNode = commandManager.argument("line", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int index = IntegerArgumentType.getInteger(context, "index");
            Text line = LegacyTextArgumentType.getText(context, "line");
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> removeNode = commandManager.literal("remove").build();

        CommandNode<CommandSource> removeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> addNode = commandManager.literal("add").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            List<Text> lore = new ArrayList<>(getLore(stack));
            lore.add(Text.empty());
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_ADD, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> addLineNode = commandManager.argument("line", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Text line = LegacyTextArgumentType.getText(context, "line");
            List<Text> lore = new ArrayList<>(getLore(stack));
            lore.add(line);
            setLore(stack, lore);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_ADD, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> insertNode = commandManager.literal("insert").build();

        CommandNode<CommandSource> insertIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0, 255)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_INSERT, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> insertIndexLineNode = commandManager.argument("line", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int index = IntegerArgumentType.getInteger(context, "index");
            Text line = LegacyTextArgumentType.getText(context, "line");
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_INSERT, TextUtil.copyable(line)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> clearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasLore(stack)) {
                throw NO_LORE_EXCEPTION;
            }
            setLore(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> clearBeforeNode = commandManager.literal("before").build();

        CommandNode<CommandSource> clearBeforeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CLEAR_BEFORE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> clearAfterNode = commandManager.literal("after").build();

        CommandNode<CommandSource> clearAfterIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CLEAR_AFTER, index));
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

        // ... clear before <index>
        clearNode.addChild(clearBeforeNode);
        clearBeforeNode.addChild(clearBeforeIndexNode);

        // ... clear after <index>
        clearNode.addChild(clearAfterNode);
        clearAfterNode.addChild(clearAfterIndexNode);
    }
}
