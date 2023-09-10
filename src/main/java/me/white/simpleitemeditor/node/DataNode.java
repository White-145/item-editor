package me.white.simpleitemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType.NbtPath;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public class DataNode implements Node {
    public static final CommandSyntaxException NO_NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.nonbt")).create();
    public static final CommandSyntaxException NO_SUCH_NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.nosuchnbt")).create();
    public static final CommandSyntaxException SET_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.setalreadyhas")).create();
    public static final CommandSyntaxException MERGE_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.mergealreadyhas")).create();
    private static final String OUTPUT_GET = "commands.edit.data.get";
    private static final String OUTPUT_SET = "commands.edit.data.set";
    private static final String OUTPUT_MERGE = "commands.edit.data.merge";
    private static final String OUTPUT_REMOVE = "commands.edit.data.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.data.clear";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("data")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(stack.getNbt())));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPathArgumentType.NbtPath> getPathNode = ClientCommandManager
                .argument("path", NbtPathArgumentType.nbtPath())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;
                    NbtPath path = context.getArgument("path", NbtPath.class);
                    NbtElement element = path.get(stack.getNbt()).get(0);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(element)));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPathArgumentType.NbtPath> setPathNode = ClientCommandManager
                .argument("path", NbtPathArgumentType.nbtPath())
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> setPathValueNode = ClientCommandManager
                .argument("value", NbtElementArgumentType.nbtElement())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    NbtPath path = context.getArgument("path", NbtPath.class);
                    NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");
                    NbtCompound nbt = stack.getOrCreateNbt();
                    NbtElement previous = null;
                    try {
                        previous = path.get(nbt).get(0);
                    } catch (Exception ignored) { }
                    if (element.equals(previous)) throw SET_ALREADY_HAS_EXCEPTION;
                    path.put(nbt, element);
                    stack.setNbt(nbt);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(element)));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> mergeNode = ClientCommandManager
                .literal("merge")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtCompound> mergeValueNode = ClientCommandManager
                .argument("value", NbtCompoundArgumentType.nbtCompound())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    NbtCompound compound = NbtCompoundArgumentType.getNbtCompound(context, "value");
                    NbtCompound nbt = stack.getOrCreateNbt();
                    NbtCompound merged = nbt.copy().copyFrom(compound);
                    if (nbt.equals(merged)) throw MERGE_ALREADY_HAS_EXCEPTION;
                    stack.setNbt(merged);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_MERGE));
                    EditorUtil.setStack(context.getSource(), stack);
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;
                    stack.setNbt(null);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
                    EditorUtil.setStack(context.getSource(), stack);
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPathArgumentType.NbtPath> removePathNode = ClientCommandManager
                .argument("path", NbtPathArgumentType.nbtPath())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    NbtPath path = context.getArgument("path", NbtPath.class);
                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;
                    NbtCompound nbt = stack.getNbt();
                    if (path.count(nbt) == 0) throw NO_SUCH_NBT_EXCEPTION;
                    path.remove(nbt);
                    stack.setNbt(nbt);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
                    EditorUtil.setStack(context.getSource(), stack);
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get [<path>]
        node.addChild(getNode);
        getNode.addChild(getPathNode);

        // ... set <path> <value>
        node.addChild(setNode);
        setNode.addChild(setPathNode);
        setPathNode.addChild(setPathValueNode);

        // ... merge <value>
        node.addChild(mergeNode);
        mergeNode.addChild(mergeValueNode);

        // ... remove <path>
        node.addChild(removeNode);
        removeNode.addChild(removePathNode);
    }
}