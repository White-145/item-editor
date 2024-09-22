package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class MaterialNode implements Node {
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.material.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.material.get";
    private static final String OUTPUT_SET = "commands.edit.material.set";

    @Override
    public void register(CommonCommandManager<CommandSource> commandManager, CommandNode<CommandSource> rootNode, CommandRegistryAccess registryAccess) {
        CommandNode<CommandSource> node = commandManager.literal("material").build();

        CommandNode<CommandSource> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, TextUtil.copyable(stack.getItem())));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> setNode = commandManager.literal("set").build();

        CommandNode<CommandSource> setMaterialNode = commandManager.argument("material", RegistryArgumentType.registryEntry(RegistryKeys.ITEM, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            Item item = RegistryArgumentType.getRegistryEntry(context, "material", RegistryKeys.ITEM);
            if (stack.getItem() == item) {
                throw ALREADY_IS_EXCEPTION;
            }
            ItemStack newStack = stack.copyComponentsToNewStack(item, 1);

            EditorUtil.setStack(context.getSource(), newStack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, TextUtil.copyable(item)));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set <material>
        node.addChild(setNode);
        setNode.addChild(setMaterialNode);
    }
}
