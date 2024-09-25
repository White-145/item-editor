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
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("material").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, TextUtil.copyable(stack.getItem())));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setMaterialNode = commandManager.argument("material", RegistryArgumentType.registryEntry(RegistryKeys.ITEM, registryAccess)).executes(context -> {
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

        // ... get
        node.addChild(getNode);

        // ... set <material>
        node.addChild(setNode);
        setNode.addChild(setMaterialNode);

        return node;
    }
}
