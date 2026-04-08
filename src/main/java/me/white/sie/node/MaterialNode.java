package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.argument.RegistryArgumentType;
import me.white.sie.util.EditorUtil;
import me.white.sie.util.TextUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MaterialNode implements Node {
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.material.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.material.get";
    private static final String OUTPUT_SET = "commands.edit.material.set";

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("material").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET, TextUtil.copyable(stack.getItem())));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setMaterialNode = commandManager.argument("material", RegistryArgumentType.registryEntry(Registries.ITEM, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            Item item = RegistryArgumentType.getRegistryEntry(context, "material", Registries.ITEM);
            if (stack.getItem() == item) {
                throw ALREADY_IS_EXCEPTION;
            }
            ItemStack newStack = stack.transmuteCopy(item, 1);

            EditorUtil.setStack(context.getSource(), newStack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, TextUtil.copyable(item)));
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
