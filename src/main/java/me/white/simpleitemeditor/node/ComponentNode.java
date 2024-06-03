package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.DataResult;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class ComponentNode implements Node {
    private static final DynamicCommandExceptionType MALFORMED_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(error -> Text.translatable("commands.edit.component.error.malformedcomponent", error));
    private static final String OUTPUT_SET = "commands.edit.component.set";
    private static final String OUTPUT_REMOVE = "commands.edit.component.remove";

    private static <T> void setFromNbt(ItemStack stack, DataComponentType<T> component, NbtElement element, CommandRegistryAccess registryAccess) throws CommandSyntaxException {
        DataResult<T> value = component.getCodecOrThrow().parse(registryAccess.getOps(NbtOps.INSTANCE), element);
        if (value.isError()) {
            throw MALFORMED_COMPONENT_EXCEPTION.create(value.error().orElseThrow().message());
        }
        stack.set(component, value.getOrThrow());
    }

    @Override
    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("component").build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<DataComponentType<?>>> setComponentNode = ClientCommandManager.argument("component", RegistryArgumentType.registryEntry(RegistryKeys.DATA_COMPONENT_TYPE, registryAccess)).build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> setComponentValueNode = ClientCommandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataComponentType<?> component = RegistryArgumentType.getRegistryEntry(context, "component", RegistryKeys.DATA_COMPONENT_TYPE);
            NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");
            setFromNbt(stack, component, element, registryAccess);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(Registries.DATA_COMPONENT_TYPE.getId(component)), TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<DataComponentType<?>>> removeComponentNode = ClientCommandManager.argument("component", RegistryArgumentType.registryEntry(RegistryKeys.DATA_COMPONENT_TYPE, registryAccess)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataComponentType<?> component = RegistryArgumentType.getRegistryEntry(context, "component", RegistryKeys.DATA_COMPONENT_TYPE);
            stack.remove(component);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, TextUtil.copyable(Registries.DATA_COMPONENT_TYPE.getId(component))));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... set <component> <value>
        node.addChild(setNode);
        setNode.addChild(setComponentNode);
        setComponentNode.addChild(setComponentValueNode);

        // ... remove <component>
        node.addChild(removeNode);
        removeNode.addChild(removeComponentNode);
    }
}
