package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
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
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class NameNode implements Node {
    private static final CommandSyntaxException NO_ITEM_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.noitemname")).create();
    private static final CommandSyntaxException ITEM_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.itemalreadyis")).create();
    private static final CommandSyntaxException NO_CUSTOM_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.nocustomname")).create();
    private static final CommandSyntaxException CUSTOM_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.customalreadyis")).create();
    private static final String OUTPUT_ITEM_GET = "commands.edit.name.getitem";
    private static final String OUTPUT_ITEM_SET = "commands.edit.name.setitem";
    private static final String OUTPUT_ITEM_RESET = "commands.edit.name.resetitem";
    private static final String OUTPUT_CUSTOM_GET = "commands.edit.name.getcustom";
    private static final String OUTPUT_CUSTOM_SET = "commands.edit.name.setcustom";
    private static final String OUTPUT_CUSTOM_REMOVE = "commands.edit.name.removecustom";

    private static boolean hasItemName(ItemStack stack) {
        return stack.contains(DataComponentTypes.ITEM_NAME);
    }

    private static Text getItemName(ItemStack stack) {
        if (!hasItemName(stack)) {
            return null;
        }
        return stack.get(DataComponentTypes.ITEM_NAME);
    }

    private static void setItemName(ItemStack stack, Text name) {
        if (name == null) {
            stack.remove(DataComponentTypes.ITEM_NAME);
        } else {
            stack.set(DataComponentTypes.ITEM_NAME, name);
        }
    }

    private static void resetItemName(ItemStack stack) {
        stack.set(DataComponentTypes.ITEM_NAME, stack.getDefaultComponents().get(DataComponentTypes.ITEM_NAME));
    }

    private static boolean hasCustomName(ItemStack stack) {
        return stack.contains(DataComponentTypes.CUSTOM_NAME);
    }

    private static Text getCustomName(ItemStack stack) {
        if (!hasCustomName(stack)) {
            return null;
        }
        return stack.get(DataComponentTypes.CUSTOM_NAME);
    }

    private static void setCustomName(ItemStack stack, Text name) {
        if (name == null) {
            stack.remove(DataComponentTypes.CUSTOM_NAME);
        } else {
            stack.set(DataComponentTypes.CUSTOM_NAME, name);
        }
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("name").build();

        CommandNode<S> itemNode = commandManager.literal("item").build();

        CommandNode<S> itemGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasItemName(stack)) {
                throw NO_ITEM_NAME_EXCEPTION;
            }
            Text name = getItemName(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_ITEM_GET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> itemSetNode = commandManager.literal("set").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (hasItemName(stack) && Text.empty().equals(getItemName(stack))) {
                throw ITEM_ALREADY_IS_EXCEPTION;
            }
            setItemName(stack, Text.empty());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_ITEM_SET, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> itemSetNameNode = commandManager.argument("name", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Text name = LegacyTextArgumentType.getText(context, "name");
            if (hasItemName(stack) && name.equals(getItemName(stack))) {
                throw ITEM_ALREADY_IS_EXCEPTION;
            }
            setItemName(stack, name);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_ITEM_SET, TextUtil.copyable(name)));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_ITEM_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> customNode = commandManager.literal("custom").build();

        CommandNode<S> customGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasCustomName(stack)) {
                throw NO_CUSTOM_NAME_EXCEPTION;
            }
            Text name = getCustomName(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CUSTOM_GET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> customSetNode = commandManager.literal("set").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (hasCustomName(stack) && Text.empty().equals(getCustomName(stack))) {
                throw CUSTOM_ALREADY_IS_EXCEPTION;
            }
            setCustomName(stack, Text.empty());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CUSTOM_SET, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> customSetNameNode = commandManager.argument("name", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Text name = LegacyTextArgumentType.getText(context, "name");
            if (hasCustomName(stack) && name.equals(getCustomName(stack))) {
                throw CUSTOM_ALREADY_IS_EXCEPTION;
            }
            setCustomName(stack, name);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CUSTOM_SET, TextUtil.copyable(name)));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CUSTOM_REMOVE));
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
