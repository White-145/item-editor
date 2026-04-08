package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.argument.LegacyTextArgumentType;
import me.white.sie.util.EditorUtil;
import me.white.sie.util.TextUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class NameNode implements Node {
    private static final CommandSyntaxException NO_ITEM_NAME_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.name.error.noitemname")).create();
    private static final CommandSyntaxException ITEM_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.name.error.itemalreadyis")).create();
    private static final CommandSyntaxException NO_CUSTOM_NAME_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.name.error.nocustomname")).create();
    private static final CommandSyntaxException CUSTOM_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.name.error.customalreadyis")).create();
    private static final String OUTPUT_ITEM_GET = "commands.edit.name.getitem";
    private static final String OUTPUT_ITEM_SET = "commands.edit.name.setitem";
    private static final String OUTPUT_ITEM_RESET = "commands.edit.name.resetitem";
    private static final String OUTPUT_CUSTOM_GET = "commands.edit.name.getcustom";
    private static final String OUTPUT_CUSTOM_SET = "commands.edit.name.setcustom";
    private static final String OUTPUT_CUSTOM_REMOVE = "commands.edit.name.removecustom";

    private static boolean hasItemName(ItemStack stack) {
        return stack.has(DataComponents.ITEM_NAME);
    }

    private static Component getItemName(ItemStack stack) {
        if (!hasItemName(stack)) {
            return null;
        }
        return stack.get(DataComponents.ITEM_NAME);
    }

    private static void setItemName(ItemStack stack, Component name) {
        if (name == null) {
            stack.remove(DataComponents.ITEM_NAME);
        } else {
            stack.set(DataComponents.ITEM_NAME, name);
        }
    }

    private static void resetItemName(ItemStack stack) {
        stack.set(DataComponents.ITEM_NAME, stack.getPrototype().get(DataComponents.ITEM_NAME));
    }

    private static boolean hasCustomName(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_NAME);
    }

    private static Component getCustomName(ItemStack stack) {
        if (!hasCustomName(stack)) {
            return null;
        }
        return stack.get(DataComponents.CUSTOM_NAME);
    }

    private static void setCustomName(ItemStack stack, Component name) {
        if (name == null) {
            stack.remove(DataComponents.CUSTOM_NAME);
        } else {
            stack.set(DataComponents.CUSTOM_NAME, name);
        }
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("name").build();

        CommandNode<S> itemNode = commandManager.literal("item").build();

        CommandNode<S> itemGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasItemName(stack)) {
                throw NO_ITEM_NAME_EXCEPTION;
            }
            Component name = getItemName(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_ITEM_GET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> itemSetNode = commandManager.literal("set").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (hasItemName(stack) && Component.empty().equals(getItemName(stack))) {
                throw ITEM_ALREADY_IS_EXCEPTION;
            }
            setItemName(stack, Component.empty());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_ITEM_SET, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> itemSetNameNode = commandManager.argument("name", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Component name = LegacyTextArgumentType.getText(context, "name");
            if (hasItemName(stack) && name.equals(getItemName(stack))) {
                throw ITEM_ALREADY_IS_EXCEPTION;
            }
            setItemName(stack, name);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_ITEM_SET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> itemResetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasItemName(stack)) {
                throw NO_ITEM_NAME_EXCEPTION;
            }
            resetItemName(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_ITEM_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> customNode = commandManager.literal("custom").build();

        CommandNode<S> customGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasCustomName(stack)) {
                throw NO_CUSTOM_NAME_EXCEPTION;
            }
            Component name = getCustomName(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CUSTOM_GET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> customSetNode = commandManager.literal("set").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (hasCustomName(stack) && Component.empty().equals(getCustomName(stack))) {
                throw CUSTOM_ALREADY_IS_EXCEPTION;
            }
            setCustomName(stack, Component.empty());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CUSTOM_SET, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> customSetNameNode = commandManager.argument("name", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Component name = LegacyTextArgumentType.getText(context, "name");
            if (hasCustomName(stack) && name.equals(getCustomName(stack))) {
                throw CUSTOM_ALREADY_IS_EXCEPTION;
            }
            setCustomName(stack, name);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CUSTOM_SET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> customRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasCustomName(stack)) {
                throw NO_CUSTOM_NAME_EXCEPTION;
            }
            setCustomName(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CUSTOM_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... item ...
        node.addChild(itemNode);
        // ... get
        itemNode.addChild(itemGetNode);
        // ... set [<name>]
        itemNode.addChild(itemSetNode);
        itemSetNode.addChild(itemSetNameNode);
        // ... reset
        itemNode.addChild(itemResetNode);

        // ... custom ...
        node.addChild(customNode);
        // ... get
        customNode.addChild(customGetNode);
        // ... set [<name>]
        customNode.addChild(customSetNode);
        customSetNode.addChild(customSetNameNode);
        // ... remove
        customNode.addChild(customRemoveNode);

        return node;
    }
}
