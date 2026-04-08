package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.argument.EnumArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.util.EditorUtil;
import me.white.sie.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.List;

public class DataNode implements Node {
    private static final CommandSyntaxException NO_NBT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.data.error.nonbt")).create();
    private static final CommandSyntaxException NO_SUCH_NBT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.data.error.nosuchnbt")).create();
    private static final CommandSyntaxException NOT_LIST_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.data.error.notlist")).create();
    private static final CommandSyntaxException NOT_COMPOUND_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.data.error.notcompound")).create();
    private static final CommandSyntaxException MERGE_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.data.error.mergealreadyhas")).create();
    private static final CommandSyntaxException SET_ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.data.error.setalreadyhas")).create();
    private static final CommandSyntaxException NOT_APPLICABLE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.data.error.notapplicable")).create();
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
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("data").build();

        CommandNode<S> sourceNode = commandManager.argument("source", EnumArgumentType.enums(DataSource.class)).build();

        CommandNode<S> sourceGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            DataSource source = context.getArgument("source", DataSource.class);
            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET, TextUtil.copyable(source.get(stack))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceGetPathNode = commandManager.argument("path", NbtPathArgument.nbtPath()).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            DataSource source = context.getArgument("source", DataSource.class);
            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            NbtPathArgument.NbtPath path = context.getArgument("path", NbtPathArgument.NbtPath.class);
            List<Tag> elements;
            try {
                elements = path.get(source.get(stack));
            } catch (CommandSyntaxException ignored) {
                throw NO_SUCH_NBT_EXCEPTION;
            }

            MutableComponent output = Component.empty();
            for (int i = 0; i < elements.size(); ++i) {
                output.append(TextUtil.copyable(elements.get(i)));
                if (i != elements.size() - 1) {
                    output.append(Component.literal(", ").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
                }
            }

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET, output));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceAppendNode = commandManager.literal("append").build();

        CommandNode<S> sourceAppendPathNode = commandManager.argument("path", NbtPathArgument.nbtPath()).build();

        CommandNode<S> sourceAppendPathValueNode = commandManager.argument("value", NbtTagArgument.nbtTag()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = context.getArgument("source", DataSource.class);
            NbtPathArgument.NbtPath path = context.getArgument("path", NbtPathArgument.NbtPath.class);
            Tag element = NbtTagArgument.getNbtTag(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            CompoundTag nbt = source.get(stack);
            List<Tag> elements;
            try {
                elements = path.get(nbt);
            } catch (CommandSyntaxException ignored) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            for (Tag el : elements) {
                if (el instanceof ListTag list) {
                    list.add(element);
                } else {
                    throw NOT_LIST_EXCEPTION;
                }
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_APPEND, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceInsertNode = commandManager.literal("insert").build();

        CommandNode<S> sourceInsertPathNode = commandManager.argument("path", NbtPathArgument.nbtPath()).build();

        CommandNode<S> sourceInsertPathIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).build();

        CommandNode<S> sourceInsertPathIndexValueNode = commandManager.argument("value", NbtTagArgument.nbtTag()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = context.getArgument("source", DataSource.class);
            NbtPathArgument.NbtPath path = context.getArgument("path", NbtPathArgument.NbtPath.class);
            int index = IntegerArgumentType.getInteger(context, "index");
            Tag element = NbtTagArgument.getNbtTag(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            CompoundTag nbt = source.get(stack);
            List<Tag> elements;
            try {
                elements = path.get(nbt);
            } catch (CommandSyntaxException ignored) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            for (Tag el : elements) {
                if (el instanceof ListTag list) {
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
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_INSERT, TextUtil.copyable(element), index));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourcePrependNode = commandManager.literal("prepend").build();

        CommandNode<S> sourcePrependPathNode = commandManager.argument("path", NbtPathArgument.nbtPath()).build();

        CommandNode<S> sourcePrependPathValueNode = commandManager.argument("value", NbtTagArgument.nbtTag()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = context.getArgument("source", DataSource.class);
            NbtPathArgument.NbtPath path = context.getArgument("path", NbtPathArgument.NbtPath.class);
            Tag element = NbtTagArgument.getNbtTag(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            CompoundTag nbt = source.get(stack);
            List<Tag> elements;
            try {
                elements = path.get(nbt);
            } catch (CommandSyntaxException ignored) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            for (Tag el : elements) {
                if (el instanceof ListTag list) {
                    list.add(0, element);
                } else {
                    throw NOT_LIST_EXCEPTION;
                }
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_PREPEND, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceSetNode = commandManager.literal("set").build();

        CommandNode<S> sourceSetPathNode = commandManager.argument("path", NbtPathArgument.nbtPath()).build();

        CommandNode<S> sourceSetPathValueNode = commandManager.argument("value", NbtTagArgument.nbtTag()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = context.getArgument("source", DataSource.class);
            NbtPathArgument.NbtPath path = context.getArgument("path", NbtPathArgument.NbtPath.class);
            Tag element = NbtTagArgument.getNbtTag(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            CompoundTag nbt = source.has(stack) ? source.get(stack) : new CompoundTag();
            check:
            if (path.countMatching(nbt) > 0) {
                for (Tag el : path.get(nbt)) {
                    if (!el.equals(element)) {
                        break check;
                    }
                }
                throw SET_ALREADY_HAS_EXCEPTION;
            }
            path.set(nbt, element);
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceMergeNode = commandManager.literal("merge").build();

        CommandNode<S> sourceMergeValueNode = commandManager.argument("value", CompoundTagArgument.compoundTag()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = context.getArgument("source", DataSource.class);
            CompoundTag element = CompoundTagArgument.getCompoundTag(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            CompoundTag nbt = source.has(stack) ? source.get(stack) : new CompoundTag();
            CompoundTag old = nbt.copy();
            nbt.merge(element);
            if (nbt.equals(old)) {
                throw MERGE_ALREADY_HAS_EXCEPTION;
            }
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_MERGE, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceMergeValuePathNode = commandManager.argument("path", NbtPathArgument.nbtPath()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = context.getArgument("source", DataSource.class);
            NbtPathArgument.NbtPath path = context.getArgument("path", NbtPathArgument.NbtPath.class);
            CompoundTag element = CompoundTagArgument.getCompoundTag(context, "value");

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            CompoundTag nbt = source.get(stack);
            List<Tag> elements;
            try {
                elements = path.get(nbt);
            } catch (CommandSyntaxException ignored) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            boolean hadEffect = false;
            for (Tag el : elements) {
                if (el instanceof CompoundTag compound) {
                    CompoundTag old = compound.copy();
                    compound.merge(element);
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
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_MERGE_PATH, TextUtil.copyable(element)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceRemoveNode = commandManager.literal("remove").build();

        CommandNode<S> sourceRemovePathNode = commandManager.argument("path", NbtPathArgument.nbtPath()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = context.getArgument("source", DataSource.class);
            NbtPathArgument.NbtPath path = context.getArgument("path", NbtPathArgument.NbtPath.class);

            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            CompoundTag nbt = source.get(stack);
            if (path.countMatching(nbt) == 0) {
                throw NO_SUCH_NBT_EXCEPTION;
            }
            path.remove(nbt);
            source.set(stack, nbt);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sourceClearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            DataSource source = context.getArgument("source", DataSource.class);
            if (!source.isApplicable(stack)) {
                throw NOT_APPLICABLE_EXCEPTION;
            }
            if (!source.has(stack)) {
                throw NO_NBT_EXCEPTION;
            }
            source.set(stack, new CompoundTag());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR));
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
        CUSTOM(DataComponents.CUSTOM_DATA),
        ENTITY(DataComponents.ENTITY_DATA) {
            @Override
            public boolean isApplicable(ItemStack stack) {
                Item item = stack.getItem();
                return item instanceof SpawnEggItem;
            }

            @Override
            public CompoundTag get(ItemStack stack) {
                return ((DataSource)this).getTyped(stack);
            }

            @Override
            public void set(ItemStack stack, CompoundTag nbt) {
                Item item = stack.getItem();
                if (!(item instanceof SpawnEggItem)) {
                    return;
                }
                EntityType<?> type = EditorUtil.getEntityType(stack);
                if (nbt.contains("id")) {
                    Identifier id = Identifier.tryParse(nbt.getString("id").get());
                    if (BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                        type = BuiltInRegistries.ENTITY_TYPE.get(id).get().value();
                    }
                }
                ((DataSource)this).setTyped(stack, type, nbt);
            }

            @Override
            protected void preprocess(ItemStack stack, CompoundTag nbt) {
                if (!nbt.contains("id")) {
                    Item item = stack.getItem();
                    if (!(item instanceof SpawnEggItem)) {
                        return;
                    }
                    EntityType<?> entityType = EditorUtil.getEntityType(stack);
                    Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
                    nbt.putString("id", id.toString());
                }
            }
        },
        BLOCK(DataComponents.BLOCK_ENTITY_DATA) {
            @Override
            public boolean isApplicable(ItemStack stack) {
                Item item = stack.getItem();
                if (!(item instanceof BlockItem)) {
                    return false;
                }
                Block block = ((BlockItem)item).getBlock();
                return block instanceof EntityBlock;
            }

            @Override
            public CompoundTag get(ItemStack stack) {
                return ((DataSource)this).getTyped(stack);
            }

            @Override
            public void set(ItemStack stack, CompoundTag nbt) {
                Item item = stack.getItem();
                if (!(item instanceof BlockItem)) {
                    return;
                }
                Block block = ((BlockItem)stack.getItem()).getBlock();
                if (!(block instanceof EntityBlock)) {
                    return;
                }
                BlockEntityType<?> type = ((EntityBlock)block).newBlockEntity(BlockPos.ZERO, block.defaultBlockState()).getType();
                ((DataSource)this).setTyped(stack, type, nbt);
            }
        },
        BUCKET(DataComponents.BUCKET_ENTITY_DATA) {
            @Override
            public boolean isApplicable(ItemStack stack) {
                Item item = stack.getItem();
                return item instanceof MobBucketItem;
            }
        };

        final DataComponentType<?> component;

        DataSource(DataComponentType<?> component) {
            this.component = component;
        }

        public boolean has(ItemStack stack) {
            return stack.has(component) && !get(stack).isEmpty();
        }

        public CompoundTag get(ItemStack stack) {
            return getPlain(stack);
        }

        public void set(ItemStack stack, CompoundTag nbt) {
            setPlain(stack, nbt);
        }

        private CompoundTag getPlain(ItemStack stack) {
            if (!stack.has(component)) {
                return new CompoundTag();
            }
            return ((CustomData)stack.get(component)).copyTag();
        }

        @SuppressWarnings("unchecked")
        private void setPlain(ItemStack stack, CompoundTag nbt) {
            stack.set((DataComponentType<CustomData>)component, CustomData.of(nbt));
        }

        public boolean isApplicable(ItemStack stack) {
            return true;
        }

        protected void preprocess(ItemStack stack, CompoundTag nbt) { }

        private CompoundTag getTyped(ItemStack stack) {
            if (!stack.has(component)) {
                return new CompoundTag();
            }
            return ((TypedEntityData<?>)stack.get(component)).copyTagWithoutId();
        }

        @SuppressWarnings("unchecked")
        private void setTyped(ItemStack stack, Object type, CompoundTag nbt) {
            stack.set((DataComponentType<TypedEntityData<?>>)component, TypedEntityData.of(type, nbt));
        }
    }
}
