package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.enums.DataSourceArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType.NbtPath;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class DataNode implements Node {
    private static final CommandSyntaxException NO_NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.nonbt")).create();
    private static final CommandSyntaxException NO_SUCH_NBT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.nosuchnbt")).create();
    private static final CommandSyntaxException NOT_LIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.notlist")).create();
    private static final CommandSyntaxException NOT_COMPOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.notcompound")).create();
    private static final CommandSyntaxException MERGE_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.mergealreadyhas")).create();
    private static final CommandSyntaxException SET_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.setalreadyhas")).create();
    private static final CommandSyntaxException NOT_APPLICABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.data.error.notapplicable")).create();
    private static final String OUTPUT_GET = "commands.edit.data.get";
    private static final String OUTPUT_APPEND = "commands.edit.data.append";
    private static final String OUTPUT_INSERT = "commands.edit.data.insert";
    private static final String OUTPUT_MERGE_PATH = "commands.edit.data.mergepath";
    private static final String OUTPUT_PREPEND = "commands.edit.data.prepend";
    private static final String OUTPUT_SET = "commands.edit.data.set";
    private static final String OUTPUT_MERGE = "commands.edit.data.merge";
    private static final String OUTPUT_REMOVE = "commands.edit.data.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.data.clear";

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("data").build();

        CommandNode<S> sourceNode = commandManager.argument("source", DataSourceArgumentType.dataSource()).build();

        CommandNode<S> sourceGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, TextUtil.copyable(source.get(stack))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceGetPathNode = commandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
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

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, output));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceAppendNode = commandManager.literal("append").build();

        CommandNode<S> sourceAppendPathNode = commandManager.argument("path", NbtPathArgumentType.nbtPath()).build();

        CommandNode<S> sourceAppendPathValueNode = commandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
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
                    list.add(element);
                } else {
                    throw NOT_LIST_EXCEPTION;
                }
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_APPEND, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceInsertNode = commandManager.literal("insert").build();

        CommandNode<S> sourceInsertPathNode = commandManager.argument("path", NbtPathArgumentType.nbtPath()).build();

        CommandNode<S> sourceInsertPathIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).build();

        CommandNode<S> sourceInsertPathIndexValueNode = commandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_INSERT, TextUtil.copyable(element), index));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourcePrependNode = commandManager.literal("prepend").build();

        CommandNode<S> sourcePrependPathNode = commandManager.argument("path", NbtPathArgumentType.nbtPath()).build();

        CommandNode<S> sourcePrependPathValueNode = commandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
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
                    list.add(0, element);
                } else {
                    throw NOT_LIST_EXCEPTION;
                }
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PREPEND, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceSetNode = commandManager.literal("set").build();

        CommandNode<S> sourceSetPathNode = commandManager.argument("path", NbtPathArgumentType.nbtPath()).build();

        CommandNode<S> sourceSetPathValueNode = commandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceMergeNode = commandManager.literal("merge").build();

        CommandNode<S> sourceMergeValueNode = commandManager.argument("value", NbtCompoundArgumentType.nbtCompound()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_MERGE, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceMergeValuePathNode = commandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_MERGE_PATH, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceRemoveNode = commandManager.literal("remove").build();

        CommandNode<S> sourceRemovePathNode = commandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceClearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = DataSourceArgumentType.getDataSource(context, "source");
            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            source.set(stack, new NbtCompound());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... <source> ...
        node.addChild(sourceNode);
        // ... get [<path>]
        sourceNode.addChild(sourceGetNode);
        sourceGetNode.addChild(sourceGetPathNode);
        // ... append <path> <value>
        sourceNode.addChild(sourceAppendNode);
        sourceAppendNode.addChild(sourceAppendPathNode);
        sourceAppendPathNode.addChild(sourceAppendPathValueNode);
        // ... insert <path> <index> <value>
        sourceNode.addChild(sourceInsertNode);
        sourceInsertNode.addChild(sourceInsertPathNode);
        sourceInsertPathNode.addChild(sourceInsertPathIndexNode);
        sourceInsertPathIndexNode.addChild(sourceInsertPathIndexValueNode);
        // ... prepend <path> <value>
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

        return node;
    }

    public enum DataSource {
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
                    DynamicRegistryManager registryManager = MinecraftClient.getInstance().getNetworkHandler().getRegistryManager();
                    EntityType<?> entityType = ((SpawnEggItem)item).getEntityType(registryManager, ItemStack.EMPTY);
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

        final ComponentType<NbtComponent> component;

        DataSource(ComponentType<NbtComponent> component) {
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

        public void set(ItemStack stack, NbtCompound nbt) {
            preprocess(stack, nbt);
            NbtComponent.set(component, stack, nbt);
        }

        public boolean isApplicable(ItemStack stack) {
            return true;
        }

        protected void preprocess(ItemStack stack, NbtCompound nbt) { }
    }
}
