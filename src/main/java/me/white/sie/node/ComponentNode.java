package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.DataResult;
import me.white.sie.Node;
import me.white.sie.argument.RegistryArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.util.EditorUtil;
import me.white.sie.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class ComponentNode implements Node {
    private static final DynamicCommandExceptionType MALFORMED_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(error -> Component.translatable("commands.edit.component.error.malformedcomponent", error));
    private static final DynamicCommandExceptionType BROKEN_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(error -> Component.translatable("commands.edit.component.error.brokencomponent", error));
    private static final CommandSyntaxException NO_COMPONENT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.component.error.nocomponent")).create();
    private static final String OUTPUT_GET = "commands.edit.component.get";
    private static final String OUTPUT_GET_COMPONENT = "commands.edit.component.getcomponent";
    private static final String OUTPUT_SET = "commands.edit.component.set";
    private static final String OUTPUT_REMOVE = "commands.edit.component.remove";

    public static <T> void setFromNbt(ItemStack stack, DataComponentType<T> component, Tag element, RegistryAccess registryManager) throws CommandSyntaxException {
        DataResult<T> value = component.codecOrThrow().parse(registryManager.createSerializationContext(NbtOps.INSTANCE), element);
        stack.set(component, value.getOrThrow(MALFORMED_COMPONENT_EXCEPTION::create));
    }

    public static <T> Tag getFromComponent(ItemStack stack, DataComponentType<T> component, RegistryAccess registryManager) throws CommandSyntaxException {
        DataResult<Tag> element = component.codecOrThrow().encodeStart(registryManager.createSerializationContext(NbtOps.INSTANCE), stack.get(component));
        return element.getOrThrow(BROKEN_COMPONENT_EXCEPTION::create);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("component").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            Map<Identifier, Tag> components = EditorUtil.getComponents(stack, context.getSource().registryAccess(), true);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET));
            for (Map.Entry<Identifier, Tag> entry : components.entrySet()) {
                if (entry.getValue() != null) {
                    EditorUtil.sendFeedback(context.getSource(), Component.empty()
                            .append(TextUtil.copyable(entry.getKey()))
                            .append(Component.literal(": ").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))
                            .append(TextUtil.copyable(entry.getValue()))
                    );
                }
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> getComponentNode = commandManager.argument("component", RegistryArgumentType.registryEntry(Registries.DATA_COMPONENT_TYPE, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            DataComponentType<?> component;
            component = RegistryArgumentType.getRegistryEntry(context, "component", Registries.DATA_COMPONENT_TYPE);
            if (!stack.has(component)) {
                throw NO_COMPONENT_EXCEPTION;
            }
            Tag element = getFromComponent(stack, component, context.getSource().registryAccess());

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_COMPONENT, TextUtil.copyable(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component)), TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setComponentNode = commandManager.argument("component", RegistryArgumentType.registryEntry(Registries.DATA_COMPONENT_TYPE, registryAccess)).build();

        CommandNode<S> setComponentValueNode = commandManager.argument("value", NbtTagArgument.nbtTag()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataComponentType<?> component;
            component = RegistryArgumentType.getRegistryEntry(context, "component", Registries.DATA_COMPONENT_TYPE);
            Tag element = NbtTagArgument.getNbtTag(context, "value");
            setFromNbt(stack, component, element, context.getSource().registryAccess());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, TextUtil.copyable(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component)), TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").build();

        CommandNode<S> removeComponentNode = commandManager.argument("component", RegistryArgumentType.registryEntry(Registries.DATA_COMPONENT_TYPE, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataComponentType<?> component;
            component = RegistryArgumentType.getRegistryEntry(context, "component", Registries.DATA_COMPONENT_TYPE);
            if (!stack.has(component)) {
                throw NO_COMPONENT_EXCEPTION;
            }
            stack.remove(component);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE, TextUtil.copyable(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(component))));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get [<component>]
        node.addChild(getNode);
        getNode.addChild(getComponentNode);

        // ... set <component> <value>
        node.addChild(setNode);
        setNode.addChild(setComponentNode);
        setComponentNode.addChild(setComponentValueNode);

        // ... remove <component>
        node.addChild(removeNode);
        removeNode.addChild(removeComponentNode);

        return node;
    }
}
