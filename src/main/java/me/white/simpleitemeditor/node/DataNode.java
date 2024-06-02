package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType.NbtPath;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class DataNode implements Node {
    public static final CommandSyntaxException NO_NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.nonbt")).create();
    public static final CommandSyntaxException NO_SUCH_NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.nosuchnbt")).create();
    public static final CommandSyntaxException TYPE_MISMATCH_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.typemismatch")).create();
    public static final CommandSyntaxException NOT_LIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.notlist")).create();
    public static final CommandSyntaxException NOT_COMPOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.notcompound")).create();
    public static final CommandSyntaxException MERGE_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.mergealreadyhas")).create();
    public static final CommandSyntaxException SET_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.setalreadyhas")).create();
    public static final CommandSyntaxException NOT_APPLICABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.notapplicable")).create();
    private static final String OUTPUT_GET = "commands.edit.data.get";
    private static final String OUTPUT_APPEND = "commands.edit.data.append";
    private static final String OUTPUT_INSERT = "commands.edit.data.insert";
    private static final String OUTPUT_MERGE_PATH = "commands.edit.data.mergepath";
    private static final String OUTPUT_PREPEND = "commands.edit.data.prepend";
    private static final String OUTPUT_SET = "commands.edit.data.set";
    private static final String OUTPUT_MERGE = "commands.edit.data.merge";
    private static final String OUTPUT_REMOVE = "commands.edit.data.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.data.clear";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("data").build();

        ArgumentCommandNode<FabricClientCommandSource, DataSource> sourceNode = ClientCommandManager.argument("source", EnumArgumentType.enumArgument(DataSource.class)).build();

        LiteralCommandNode<FabricClientCommandSource> sourceGetNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(source.get(stack))));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPathArgumentType.NbtPath> sourceGetPathNode = ClientCommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            NbtPath path = context.getArgument("path", NbtPath.class);
            List<NbtElement> elements;
            try {
                elements = path.get(source.get(stack));
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
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> sourceAppendNode = ClientCommandManager.literal("append").build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> sourceAppendPathNode = ClientCommandManager.argument("path", NbtPathArgumentType.nbtPath()).build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> sourceAppendPathValueNode = ClientCommandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            NbtPath path = context.getArgument("path", NbtPath.class);
            NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            NbtCompound nbt = source.get(stack);
            List<NbtElement> elements;
            try {
                elements = path.get(nbt);
            } catch (CommandSyntaxException ignored) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            for (NbtElement el : elements) {
                if (el instanceof NbtList list) {
                    if (!list.isEmpty() && list.getHeldType() != element.getType()) {
                        throw TYPE_MISMATCH_EXCEPTION;
                    }
                    list.add(element);
                } else {
                    throw NOT_LIST_EXCEPTION;
                }
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_APPEND, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> sourceInsertNode = ClientCommandManager.literal("insert").build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> sourceInsertPathNode = ClientCommandManager.argument("path", NbtPathArgumentType.nbtPath()).build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> sourceInsertPathIndexNode = ClientCommandManager.argument("index", IntegerArgumentType.integer(0)).build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> sourceInsertPathIndexValueNode = ClientCommandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            NbtPath path = context.getArgument("path", NbtPath.class);
            int index = IntegerArgumentType.getInteger(context, "index");
            NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            NbtCompound nbt = source.get(stack);
            List<NbtElement> elements;
            try {
                elements = path.get(nbt);
            } catch (CommandSyntaxException ignored) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            for (NbtElement el : elements) {
                if (el instanceof NbtList list) {
                    if (!list.isEmpty() && list.getHeldType() != element.getType()) {
                        throw TYPE_MISMATCH_EXCEPTION;
                    }
                    if (index > list.size()) {
                        throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, list.size());
                    }
                    list.add(index, element);
                } else {
                    throw NOT_LIST_EXCEPTION;
                }
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_INSERT, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> sourcePrependNode = ClientCommandManager.literal("prepend").build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> sourcePrependPathNode = ClientCommandManager.argument("path", NbtPathArgumentType.nbtPath()).build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> sourcePrependPathValueNode = ClientCommandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            NbtPath path = context.getArgument("path", NbtPath.class);
            NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            NbtCompound nbt = source.get(stack);
            List<NbtElement> elements;
            try {
                elements = path.get(nbt);
            } catch (CommandSyntaxException ignored) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            for (NbtElement el : elements) {
                if (el instanceof NbtList list) {
                    if (!list.isEmpty() && list.getHeldType() != element.getType()) {
                        throw TYPE_MISMATCH_EXCEPTION;
                    }
                    list.add(0, element);
                } else {
                    throw NOT_LIST_EXCEPTION;
                }
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_PREPEND, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> sourceSetNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> sourceSetPathNode = ClientCommandManager.argument("path", NbtPathArgumentType.nbtPath()).build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> sourceSetPathValueNode = ClientCommandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            NbtPath path = context.getArgument("path", NbtPath.class);
            NbtElement element = NbtElementArgumentType.getNbtElement(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            NbtCompound nbt = source.has(stack) ? source.get(stack) : new NbtCompound();
            check:
            if (path.count(nbt) > 0) {
                for (NbtElement el : path.get(nbt)) {
                    if (!el.equals(element)) {
                        break check;
                    }
                }
                throw SET_ALREADY_HAS_EXCEPTION;
            }
            path.put(nbt, element);
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> sourceMergeNode = ClientCommandManager.literal("merge").build();

        ArgumentCommandNode<FabricClientCommandSource, NbtCompound> sourceMergeValueNode = ClientCommandManager.argument("value", NbtCompoundArgumentType.nbtCompound()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            NbtCompound element = NbtCompoundArgumentType.getNbtCompound(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            NbtCompound nbt = source.has(stack) ? source.get(stack) : new NbtCompound();
            NbtCompound old = nbt.copy();
            nbt.copyFrom(element);
            if (nbt.equals(old)) {
                throw MERGE_ALREADY_HAS_EXCEPTION;
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_MERGE, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> sourceMergeValuePathNode = ClientCommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            NbtPath path = context.getArgument("path", NbtPath.class);
            NbtCompound element = NbtCompoundArgumentType.getNbtCompound(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            NbtCompound nbt = source.get(stack);
            List<NbtElement> elements;
            try {
                elements = path.get(nbt);
            } catch (CommandSyntaxException ignored) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            boolean hadEffect = false;
            for (NbtElement el : elements) {
                if (el instanceof NbtCompound compound) {
                    NbtCompound old = compound.copy();
                    compound.copyFrom(element);
                    if (!compound.equals(old)) {
                        hadEffect = true;
                    }
                } else {
                    throw NOT_COMPOUND_EXCEPTION;
                }
            }
            if (!hadEffect) {
                throw MERGE_ALREADY_HAS_EXCEPTION;
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_MERGE_PATH, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> sourceRemoveNode = ClientCommandManager.literal("remove").build();

        ArgumentCommandNode<FabricClientCommandSource, NbtPath> sourceRemovePathNode = ClientCommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            NbtPath path = context.getArgument("path", NbtPath.class);

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            NbtCompound nbt = source.get(stack);
            if (path.count(nbt) == 0) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            path.remove(nbt);
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> sourceClearNode = ClientCommandManager.literal("clear").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            DataSource source = EnumArgumentType.getEnum(context, "source", DataSource.class);
            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            source.set(stack, new NbtCompound());

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... <source> ...
        node.addChild(sourceNode);
        // ... get [<path>]
        sourceNode.addChild(sourceGetNode);
        sourceGetNode.addChild(sourceGetPathNode);
        // ... append <value>
        sourceNode.addChild(sourceAppendNode);
        sourceAppendNode.addChild(sourceAppendPathNode);
        sourceAppendPathNode.addChild(sourceAppendPathValueNode);
        // ... insert <index> <value>
        sourceNode.addChild(sourceInsertNode);
        sourceInsertNode.addChild(sourceInsertPathNode);
        sourceInsertPathNode.addChild(sourceInsertPathIndexNode);
        sourceInsertPathIndexNode.addChild(sourceInsertPathIndexValueNode);
        // ... prepend <value>
        sourceNode.addChild(sourcePrependNode);
        sourcePrependNode.addChild(sourcePrependPathNode);
        sourcePrependPathNode.addChild(sourcePrependPathValueNode);
        // ... set <path> <value>
        sourceNode.addChild(sourceSetNode);
        sourceSetNode.addChild(sourceSetPathNode);
        sourceSetPathNode.addChild(sourceSetPathValueNode);
        // ... merge <value> [<path>]
        sourceNode.addChild(sourceMergeNode);
        sourceMergeNode.addChild(sourceMergeValueNode);
        sourceMergeValueNode.addChild(sourceMergeValuePathNode);
        // ... remove <path>
        sourceNode.addChild(sourceRemoveNode);
        sourceRemoveNode.addChild(sourceRemovePathNode);
        // ... clear
        sourceNode.addChild(sourceClearNode);
    }

    private enum DataSource {
        CUSTOM(DataComponentTypes.CUSTOM_DATA),
        ENTITY(DataComponentTypes.ENTITY_DATA) {
            @Override
            public boolean isApplicable(ItemStack stack) {
                Item item = stack.getItem();
                return item instanceof SpawnEggItem;
            }

            @Override
            protected void preprocess(ItemStack stack, NbtCompound nbt) {
                if (!nbt.contains("id")) {
                    Item item = stack.getItem();
                    if (!(item instanceof SpawnEggItem)) {
                        return;
                    }
                    EntityType<?> entityType = ((SpawnEggItem)item).getEntityType(ItemStack.EMPTY);
                    Identifier id = Registries.ENTITY_TYPE.getId(entityType);
                    nbt.putString("id", id.toString());
                }
            }
        },
        BLOCK(DataComponentTypes.BLOCK_ENTITY_DATA) {
            @Override
            public boolean isApplicable(ItemStack stack) {
                Item item = stack.getItem();
                if (!(item instanceof BlockItem)) {
                    return false;
                }
                Block block = ((BlockItem)item).getBlock();
                return block instanceof BlockWithEntity;
            }

            @Override
            protected void preprocess(ItemStack stack, NbtCompound nbt) {
                if (!nbt.contains("id")) {
                    Item item = stack.getItem();
                    if (!(item instanceof BlockItem)) {
                        return;
                    }
                    Block block = ((BlockItem)item).getBlock();
                    if (!(block instanceof BlockWithEntity)) {
                        return;
                    }
                    Identifier id = Registries.BLOCK.getId(block);
                    nbt.putString("id", id.toString());
                }
            }
        },
        BUCKET(DataComponentTypes.BUCKET_ENTITY_DATA) {
            @Override
            public boolean isApplicable(ItemStack stack) {
                Item item = stack.getItem();
                return item instanceof EntityBucketItem;
            }
        };

        final DataComponentType<NbtComponent> component;

        DataSource(DataComponentType<NbtComponent> component) {
            this.component = component;
        }

        public boolean has(ItemStack stack) {
            if (!stack.contains(component)) {
                return false;
            }
            return !stack.get(component).isEmpty();
        }

        public NbtCompound get(ItemStack stack) {
            if (!has(stack)) {
                return new NbtCompound();
            }
            return stack.get(component).copyNbt();
        }

        public void set(ItemStack stack, NbtCompound nbt) throws CommandSyntaxException {
            preprocess(stack, nbt);
            NbtComponent.set(component, stack, nbt);
        }

        public boolean isApplicable(ItemStack stack) {
            return true;
        }

        protected void preprocess(ItemStack stack, NbtCompound nbt) { }
    }
}
