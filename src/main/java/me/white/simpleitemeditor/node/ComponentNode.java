package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.DataResult;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.component.ComponentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ComponentNode implements Node {
    private static final DynamicCommandExceptionType MALFORMED_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(error -> Text.translatable("commands.edit.component.error.malformedcomponent", error));
    private static final DynamicCommandExceptionType BROKEN_COMPONENT_EXCEPTION = new DynamicCommandExceptionType(error -> Text.translatable("commands.edit.component.error.brokencomponent", error));
    private static final CommandSyntaxException NO_COMPONENT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.component.error.nocomponent")).create();
    private static final String OUTPUT_GET = "commands.edit.component.get";
    private static final String OUTPUT_GET_COMPONENT = "commands.edit.component.getcomponent";
    private static final String OUTPUT_SET = "commands.edit.component.set";
    private static final String OUTPUT_REMOVE = "commands.edit.component.remove";

    public static <T> void setFromNbt(ItemStack stack, ComponentType<T> component, NbtElement element, DynamicRegistryManager registryManager) throws CommandSyntaxException {
        DataResult<T> value = component.getCodecOrThrow().parse(registryManager.getOps(NbtOps.INSTANCE), element);
        stack.set(component, value.getOrThrow(MALFORMED_COMPONENT_EXCEPTION::create));
    }

    public static <T> NbtElement getFromComponent(ItemStack stack, ComponentType<T> component, DynamicRegistryManager registryManager) throws CommandSyntaxException {
        DataResult<NbtElement> element = component.getCodecOrThrow().encodeStart(registryManager.getOps(NbtOps.INSTANCE), stack.get(component));
        return element.getOrThrow(BROKEN_COMPONENT_EXCEPTION::create);
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("component").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            Map<Identifier, NbtElement> components = EditorUtil.getComponents(stack, context.getSource().getRegistryManager(), true);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET));
            for (Map.Entry<Identifier, NbtElement> entry : components.entrySet()) {
                if (entry.getValue() != null) {
                    EditorUtil.sendFeedback(context.getSource(), Text.empty()
                            .append(TextUtil.copyable(entry.getKey()))
                            .append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                            .append(TextUtil.copyable(entry.getValue()))
                    );
                }
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> getComponentNode = commandManager.argument("component", RegistryArgumentType.registryEntry(RegistryKeys.DATA_COMPONENT_TYPE, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            ComponentType<?> component = RegistryArgumentType.getRegistryEntry(context, "component", RegistryKeys.DATA_COMPONENT_TYPE);
            if (!stack.contains(component)) {
                throw NO_COMPONENT_EXCEPTION;
            }
            NbtElement element = getFromComponent(stack, component, context.getSource().getRegistryManager());

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_COMPONENT, TextUtil.copyable(Registries.DATA_COMPONENT_TYPE.getId(component)), TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setComponentNode = commandManager.argument("component", RegistryArgumentType.registryEntry(RegistryKeys.DATA_COMPONENT_TYPE, registryAccess)).build();

        CommandNode<S> setComponentValueNode = commandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            ComponentType<?> component = RegistryArgumentType.getRegistryEntry(context, "component", RegistryKeys.DATA_COMPONENT_TYPE);
            NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");
            setFromNbt(stack, component, element, context.getSource().getRegistryManager());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, TextUtil.copyable(Registries.DATA_COMPONENT_TYPE.getId(component)), TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").build();

        CommandNode<S> removeComponentNode = commandManager.argument("component", RegistryArgumentType.registryEntry(RegistryKeys.DATA_COMPONENT_TYPE, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            ComponentType<?> component = RegistryArgumentType.getRegistryEntry(context, "component", RegistryKeys.DATA_COMPONENT_TYPE);
            if (!stack.contains(component)) {
                throw NO_COMPONENT_EXCEPTION;
            }
            stack.remove(component);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE, TextUtil.copyable(Registries.DATA_COMPONENT_TYPE.getId(component))));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get <component>
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
