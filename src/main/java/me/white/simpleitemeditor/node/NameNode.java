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
    private static final CommandSyntaxException NO_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.noname")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.alreadyis")).create();
    private static final CommandSyntaxException NO_CUSTOM_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.nocustomname")).create();
    private static final CommandSyntaxException CUSTOM_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.customalreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.name.get";
    private static final String OUTPUT_SET = "commands.edit.name.set";
    private static final String OUTPUT_RESET = "commands.edit.name.reset";
    private static final String OUTPUT_CUSTOM_GET = "commands.edit.name.getcustom";
    private static final String OUTPUT_CUSTOM_SET = "commands.edit.name.setcustom";
    private static final String OUTPUT_CUSTOM_RESET = "commands.edit.name.resetcustom";

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
    public void register(CommonCommandManager<CommandSource> commandManager, CommandNode<CommandSource> rootNode, CommandRegistryAccess registryAccess) {
        CommandNode<CommandSource> node = commandManager.literal("name").build();

        CommandNode<CommandSource> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasName(stack)) {
                throw NO_NAME_EXCEPTION;
            }
            Text name = getName(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> setNode = commandManager.literal("set").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (hasName(stack) && Text.empty().equals(getName(stack))) {
                throw ALREADY_IS_EXCEPTION;
            }
            setName(stack, Text.empty());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, ""));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> setNameNode = commandManager.argument("name", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Text name = LegacyTextArgumentType.getText(context, "name");
            if (hasName(stack) && name.equals(getName(stack))) {
                throw ALREADY_IS_EXCEPTION;
            }
            setName(stack, name);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> resetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasName(stack)) {
                throw NO_NAME_EXCEPTION;
            }
            setName(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> customNode = commandManager.literal("custom").build();

        CommandNode<CommandSource> customGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasCustomName(stack)) {
                throw NO_CUSTOM_NAME_EXCEPTION;
            }
            Text name = getCustomName(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CUSTOM_GET, TextUtil.copyable(name)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> customSetNode = commandManager.literal("set").executes(context -> {
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

        CommandNode<CommandSource> customSetNameNode = commandManager.argument("name", LegacyTextArgumentType.text()).executes(context -> {
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

        CommandNode<CommandSource> customResetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasCustomName(stack)) {
                throw NO_CUSTOM_NAME_EXCEPTION;
            }
            setCustomName(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CUSTOM_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<name>]
        node.addChild(setNode);
        setNode.addChild(setNameNode);

        // ... reset
        node.addChild(resetNode);

        // ... custom ...
        node.addChild(customNode);
        // ... get
        customNode.addChild(customGetNode);
        // ... set [<name>]
        customNode.addChild(customSetNode);
        customSetNode.addChild(customSetNameNode);
        // ... reset
        customNode.addChild(customResetNode);
    }
}
