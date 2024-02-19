package me.white.simpleitemeditor.node.entity;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.node.Node;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.ItemUtil;
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
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class DataNode implements Node {
    public static final CommandSyntaxException NO_NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.data.error.nonbt")).create();
    public static final CommandSyntaxException NO_ENTITY_NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.data.error.noentitynbt")).create();
    public static final CommandSyntaxException NO_SUCH_NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.data.error.nosuchnbt")).create();
    public static final CommandSyntaxException TYPE_MISMATCH_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.data.error.typemismatch")).create();
    public static final CommandSyntaxException NOT_LIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.data.error.notlist")).create();
    public static final CommandSyntaxException NOT_COMPOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.data.error.notcompound")).create();
    public static final CommandSyntaxException MERGE_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.data.error.mergealreadyhas")).create();
    public static final CommandSyntaxException SET_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.data.error.setalreadyhas")).create();
    private static final String OUTPUT_GET = "commands.edit.entity.data.get";
    private static final String OUTPUT_APPEND = "commands.edit.entity.data.append";
    private static final String OUTPUT_INSERT = "commands.edit.entity.data.insert";
    private static final String OUTPUT_MERGE_PATH = "commands.edit.entity.data.mergepath";
    private static final String OUTPUT_PREPEND = "commands.edit.entity.data.prepend";
    private static final String OUTPUT_SET = "commands.edit.entity.data.set";
    private static final String OUTPUT_MERGE = "commands.edit.entity.data.merge";
    private static final String OUTPUT_REMOVE = "commands.edit.entity.data.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.entity.data.clear";

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

                    NbtCompound nbt = stack.getNbt();
                    if (!nbt.contains(ItemUtil.ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_ENTITY_NBT_EXCEPTION;
                    NbtCompound entityTag = nbt.getCompound(ItemUtil.ENTITY_TAG_KEY);
                    // consistensy with /edit data
                    if (entityTag.isEmpty()) throw NO_ENTITY_NBT_EXCEPTION;

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(entityTag)));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPathArgumentType.NbtPath> getPathNode = ClientCommandManager
                .argument("path", NbtPathArgumentType.nbtPath())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;

                    NbtCompound nbt = stack.getNbt();
                    if (!nbt.contains(ItemUtil.ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_ENTITY_NBT_EXCEPTION;
                    NbtCompound entityTag = nbt.getCompound(ItemUtil.ENTITY_TAG_KEY);
                    NbtPath path = context.getArgument("path", NbtPath.class);
                    List<NbtElement> elements;
                    try {
                        elements = path.get(entityTag);
                    } catch (CommandSyntaxException ignored) {
                        throw NO_SUCH_NBT_EXCEPTION;
                    }

                    MutableText output = Text.empty();
                    for (int i = 0; i < elements.size(); ++i) {
                        output.append(TextUtil.copyable(elements.get(i)));
                        if (i != elements.size() - 1) {
                            output.append(Text.literal(", ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                        }
                    }

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, output));
                    return elements.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> appendNode = ClientCommandManager
                .literal("append")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> appendPathNode = ClientCommandManager
                .argument("path", NbtPathArgumentType.nbtPath())
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> appendPathValueNode = ClientCommandManager
                .argument("value", NbtElementArgumentType.nbtElement())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    NbtPath path = context.getArgument("path", NbtPath.class);
                    NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");

                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;
                    NbtCompound nbt = stack.getNbt();
                    if (!nbt.contains(ItemUtil.ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_ENTITY_NBT_EXCEPTION;
                    NbtCompound entityTag = nbt.getCompound(ItemUtil.ENTITY_TAG_KEY);
                    List<NbtElement> elements;
                    try {
                        elements = path.get(entityTag);
                    } catch (CommandSyntaxException ignored) {
                        throw NO_SUCH_NBT_EXCEPTION;
                    }
                    for (NbtElement el : elements) {
                        if (el instanceof NbtList list) {
                            if (!list.isEmpty() && list.getHeldType() != element.getType()) throw TYPE_MISMATCH_EXCEPTION;
                            list.add(element);
                        } else {
                            throw NOT_LIST_EXCEPTION;
                        }
                    }
                    nbt.put(ItemUtil.ENTITY_TAG_KEY, entityTag);
                    stack.setNbt(nbt);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_APPEND, TextUtil.copyable(element)));
                    return elements.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> insertNode = ClientCommandManager
                .literal("insert")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> insertPathNode = ClientCommandManager
                .argument("path", NbtPathArgumentType.nbtPath())
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> insertPathIndexNode = ClientCommandManager
                .argument("index", IntegerArgumentType.integer(0))
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> insertPathIndexValueNode = ClientCommandManager
                .argument("value", NbtElementArgumentType.nbtElement())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    NbtPath path = context.getArgument("path", NbtPath.class);
                    int index = IntegerArgumentType.getInteger(context, "index");
                    NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");

                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;
                    NbtCompound nbt = stack.getNbt();
                    if (!nbt.contains(ItemUtil.ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_ENTITY_NBT_EXCEPTION;
                    NbtCompound entityTag = nbt.getCompound(ItemUtil.ENTITY_TAG_KEY);
                    List<NbtElement> elements;
                    try {
                        elements = path.get(entityTag);
                    } catch (CommandSyntaxException ignored) {
                        throw NO_SUCH_NBT_EXCEPTION;
                    }
                    for (NbtElement el : elements) {
                        if (el instanceof NbtList list) {
                            if (!list.isEmpty() && list.getHeldType() != element.getType()) throw TYPE_MISMATCH_EXCEPTION;
                            if (index > list.size()) throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, list.size());
                            list.add(index, element);
                        } else {
                            throw NOT_LIST_EXCEPTION;
                        }
                    }
                    nbt.put(ItemUtil.ENTITY_TAG_KEY, entityTag);
                    stack.setNbt(nbt);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, TextUtil.copyable(element)));
                    return elements.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> prependNode = ClientCommandManager
                .literal("prepend")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> prependPathNode = ClientCommandManager
                .argument("path", NbtPathArgumentType.nbtPath())
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> prependPathValueNode = ClientCommandManager
                .argument("value", NbtElementArgumentType.nbtElement())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    NbtPath path = context.getArgument("path", NbtPath.class);
                    NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");

                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;
                    NbtCompound nbt = stack.getNbt();
                    if (!nbt.contains(ItemUtil.ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_ENTITY_NBT_EXCEPTION;
                    NbtCompound entityTag = nbt.getCompound(ItemUtil.ENTITY_TAG_KEY);
                    List<NbtElement> elements;
                    try {
                        elements = path.get(nbt.getCompound(ItemUtil.ENTITY_TAG_KEY));
                    } catch (CommandSyntaxException ignored) {
                        throw NO_SUCH_NBT_EXCEPTION;
                    }
                    for (NbtElement el : elements) {
                        if (el instanceof NbtList list) {
                            if (!list.isEmpty() && list.getHeldType() != element.getType()) throw TYPE_MISMATCH_EXCEPTION;
                            list.add(0, element);
                        } else {
                            throw NOT_LIST_EXCEPTION;
                        }
                    }
                    nbt.put(ItemUtil.ENTITY_TAG_KEY, entityTag);
                    stack.setNbt(nbt);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_PREPEND, TextUtil.copyable(element)));
                    return elements.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> setPathNode = ClientCommandManager
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
                    NbtCompound entityTag = nbt.getCompound(ItemUtil.ENTITY_TAG_KEY);
                    check: if (path.count(entityTag) > 0) {
                        for (NbtElement el : path.get(entityTag)) {
                            if (!el.equals(element)) {
                                break check;
                            }
                        }
                        throw SET_ALREADY_HAS_EXCEPTION;
                    }
                    int result = path.put(entityTag, element);
                    nbt.put(ItemUtil.ENTITY_TAG_KEY, entityTag);
                    stack.setNbt(nbt);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(element)));
                    return result;
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
                    NbtCompound element = NbtCompoundArgumentType.getNbtCompound(context, "value");

                    NbtCompound nbt = stack.getOrCreateNbt();
                    NbtCompound entityTag = nbt.getCompound(ItemUtil.ENTITY_TAG_KEY);
                    NbtCompound old = entityTag.copy();
                    entityTag.copyFrom(element);
                    if (entityTag.equals(old)) throw MERGE_ALREADY_HAS_EXCEPTION;
                    nbt.put(ItemUtil.ENTITY_TAG_KEY, entityTag);
                    stack.setNbt(nbt);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_MERGE, TextUtil.copyable(element)));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> mergeValuePathNode = ClientCommandManager
                .argument("path", NbtPathArgumentType.nbtPath())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    NbtPath path = context.getArgument("path", NbtPath.class);
                    NbtCompound element = NbtCompoundArgumentType.getNbtCompound(context, "value");

                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;
                    NbtCompound nbt = stack.getNbt();
                    if (!nbt.contains(ItemUtil.ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_ENTITY_NBT_EXCEPTION;
                    NbtCompound entityTag = nbt.getCompound(ItemUtil.ENTITY_TAG_KEY);
                    List<NbtElement> elements;
                    try {
                        elements = path.get(entityTag);
                    } catch (CommandSyntaxException ignored) {
                        throw NO_SUCH_NBT_EXCEPTION;
                    }
                    boolean hadEffect = false;
                    for (NbtElement el : elements) {
                        if (el instanceof NbtCompound compound) {
                            NbtCompound old = compound.copy();
                            compound.copyFrom(element);
                            if (!compound.equals(old)) hadEffect = true;
                        } else {
                            throw NOT_COMPOUND_EXCEPTION;
                        }
                    }
                    if (!hadEffect) throw MERGE_ALREADY_HAS_EXCEPTION;
                    nbt.put(ItemUtil.ENTITY_TAG_KEY, entityTag);
                    stack.setNbt(nbt);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_MERGE_PATH, TextUtil.copyable(element)));
                    return elements.size();
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;

                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;
                    NbtCompound nbt = stack.getNbt();
                    if (!nbt.contains(ItemUtil.ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_ENTITY_NBT_EXCEPTION;
                    nbt.remove(ItemUtil.ENTITY_TAG_KEY);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> removePathNode = ClientCommandManager
                .argument("path", NbtPathArgumentType.nbtPath())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    NbtPath path = context.getArgument("path", NbtPath.class);

                    if (!stack.hasNbt()) throw NO_NBT_EXCEPTION;
                    NbtCompound nbt = stack.getNbt();
                    if (!nbt.contains(ItemUtil.ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_ENTITY_NBT_EXCEPTION;
                    NbtCompound entityTag = nbt.getCompound(ItemUtil.ENTITY_TAG_KEY);
                    if (path.count(entityTag) == 0) throw NO_SUCH_NBT_EXCEPTION;
                    path.remove(entityTag);
                    nbt.put(ItemUtil.ENTITY_TAG_KEY, entityTag);
                    stack.setNbt(nbt);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get [<path>]
        node.addChild(getNode);
        getNode.addChild(getPathNode);

        // ... append <value>
        node.addChild(appendNode);
        appendNode.addChild(appendPathNode);
        appendPathNode.addChild(appendPathValueNode);

        // ... prepend <value>
        node.addChild(prependNode);
        prependNode.addChild(prependPathNode);
        prependPathNode.addChild(prependPathValueNode);

        // ... insert <index> <value>
        node.addChild(insertNode);
        insertNode.addChild(insertPathNode);
        insertPathNode.addChild(insertPathIndexNode);
        insertPathIndexNode.addChild(insertPathIndexValueNode);

        // ... set <value>
        node.addChild(setNode);
        setNode.addChild(setPathNode);
        setPathNode.addChild(setPathValueNode);

        // ... merge <value> [<path>]
        node.addChild(mergeNode);
        mergeNode.addChild(mergeValueNode);
        mergeValueNode.addChild(mergeValuePathNode);

        // ... remove [<path>]
        node.addChild(removeNode);
        removeNode.addChild(removePathNode);
    }
}
