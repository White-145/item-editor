package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.argument.EnumArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.argument.RegistryArgumentType;
import me.white.sie.util.EditorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import java.util.ArrayList;
import java.util.List;

public class BannerNode implements Node {
    private static final CommandSyntaxException ISNT_BANNER_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.banner.error.isntbanner")).create();
    private static final CommandSyntaxException NO_LAYERS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.banner.error.nolayers")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.banner.error.alreadyis")).create();
    private static final CommandSyntaxException NO_BASE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.banner.error.nobase")).create();
    private static final CommandSyntaxException BASE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.banner.error.basealreadyis")).create();
    private static final CommandSyntaxException ISNT_SHIELD_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.banner.error.isntshield")).create();
    private static final String OUTPUT_GET = "commands.edit.banner.get";
    private static final String OUTPUT_GET_LAYER = "commands.edit.banner.getlayer";
    private static final String OUTPUT_SET = "commands.edit.banner.set";
    private static final String OUTPUT_REMOVE = "commands.edit.banner.remove";
    private static final String OUTPUT_ADD = "commands.edit.banner.add";
    private static final String OUTPUT_INSERT = "commands.edit.banner.insert";
    private static final String OUTPUT_BASE_SET = "commands.edit.banner.baseset";
    private static final String OUTPUT_BASE_REMOVE = "commands.edit.banner.baseremove";
    private static final String OUTPUT_CLEAR = "commands.edit.banner.clear";
    private static final String OUTPUT_CLEAR_BEFORE = "commands.edit.banner.clearbefore";
    private static final String OUTPUT_CLEAR_AFTER = "commands.edit.banner.clearafter";

    private static boolean isBanner(ItemStack stack) {

        return stack.is(ItemTags.BANNERS) || stack.getItem() == Items.SHIELD;
    }

    private static boolean hasBaseColor(ItemStack stack) {
        if (stack.getItem() != Items.SHIELD) {
            return true;
        }
        return stack.has(DataComponents.BASE_COLOR);
    }

    private static DyeColor getBaseColor(ItemStack stack) {
        if (!hasBaseColor(stack)) {
            return DyeColor.WHITE;
        }
        if (stack.getItem() == Items.SHIELD) {
            return stack.get(DataComponents.BASE_COLOR);
        }
        if (stack.getItem() instanceof BannerItem) {
            return ((BannerItem)stack.getItem()).getColor();
        }
        return null;
    }

    private static ItemStack setBaseColor(ItemStack stack, DyeColor color) {
        if (color == null) {
            stack.remove(DataComponents.BASE_COLOR);
        } else {
            if (stack.getItem() == Items.SHIELD) {
                stack.set(DataComponents.BASE_COLOR, color);
            }
            return stack.transmuteCopy(Items.BANNER.pick(color), stack.getCount());
        }
        return stack;
    }

    private static boolean hasBannerLayers(ItemStack stack) {
        if (!stack.has(DataComponents.BANNER_PATTERNS)) {
            return false;
        }
        return !stack.get(DataComponents.BANNER_PATTERNS).layers().isEmpty();
    }

    private static List<BannerPatternLayers.Layer> getBannerLayers(ItemStack stack) {
        if (!hasBannerLayers(stack)) {
            return List.of();
        }
        return stack.get(DataComponents.BANNER_PATTERNS).layers();
    }

    private static void setBannerLayers(ItemStack stack, List<BannerPatternLayers.Layer> layers) {
        if (layers == null || layers.isEmpty()) {
            stack.remove(DataComponents.BANNER_PATTERNS);
        } else {
            stack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers(layers));
        }
    }

    private static BannerPatternLayers.Layer getLayer(RegistryAccess registryManager, BannerPattern pattern, DyeColor color) {
        Holder<BannerPattern> patternEntry = EditorUtil.getRegistry(Registries.BANNER_PATTERN).wrapAsHolder(pattern);
        return new BannerPatternLayers.Layer(patternEntry, color);
    }

    private static Component translation(BannerPatternLayers.Layer layer) {
        return layer.description();
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("banner").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            List<BannerPatternLayers.Layer> layers = getBannerLayers(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET));
            for (int i = 0; i < layers.size(); ++i) {
                BannerPatternLayers.Layer pattern = layers.get(i);
                EditorUtil.sendFeedback(context.getSource(), Component.empty().append(Component.literal(i + ". ").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))).append(translation(pattern)));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> getIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<BannerPatternLayers.Layer> layers = getBannerLayers(stack);
            if (layers.size() <= index) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            BannerPatternLayers.Layer layer = layers.get(index);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_LAYER, translation(layer)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).build();

        CommandNode<S> setIndexPatternNode = commandManager.argument("pattern", RegistryArgumentType.registryEntry(Registries.BANNER_PATTERN, registryAccess)).build();

        CommandNode<S> setIndexPatternColorNode = commandManager.argument("color", EnumArgumentType.enums(DyeColor.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            List<BannerPatternLayers.Layer> layers = new ArrayList<>(getBannerLayers(stack));
            int index = IntegerArgumentType.getInteger(context, "index");
            if (layers.isEmpty() && index != 0) {
                throw NO_LAYERS_EXCEPTION;
            }
            BannerPattern pattern = RegistryArgumentType.getRegistryEntry(context, "pattern", Registries.BANNER_PATTERN);
            DyeColor color = context.getArgument("color", DyeColor.class);
            BannerPatternLayers.Layer layer = getLayer(context.getSource().registryAccess(), pattern, color);
            if (layers.size() < index) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            if (layers.size() == index) {
                layers.add(layer);
            } else {
                BannerPatternLayers.Layer oldBannerLayer = layers.get(index);
                if (oldBannerLayer.equals(layer)) {
                    throw ALREADY_IS_EXCEPTION;
                }
                layers.set(index, layer);
            }
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, translation(layer)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").build();

        CommandNode<S> removeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            List<BannerPatternLayers.Layer> layers = new ArrayList<>(getBannerLayers(stack));
            int index = IntegerArgumentType.getInteger(context, "index");
            if (index >= layers.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            layers.remove(index);
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> addNode = commandManager.literal("add").build();

        CommandNode<S> addPatternNode = commandManager.argument("pattern", RegistryArgumentType.registryEntry(Registries.BANNER_PATTERN, registryAccess)).build();

        CommandNode<S> addPatternColorNode = commandManager.argument("color", EnumArgumentType.enums(DyeColor.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            BannerPattern pattern = RegistryArgumentType.getRegistryEntry(context, "pattern", Registries.BANNER_PATTERN);
            DyeColor color = context.getArgument("color", DyeColor.class);
            BannerPatternLayers.Layer layer = getLayer(context.getSource().registryAccess(), pattern, color);
            List<BannerPatternLayers.Layer> layers = new ArrayList<>(getBannerLayers(stack));
            layers.add(layer);
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_ADD, translation(layer)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> insertNode = commandManager.literal("insert").build();

        CommandNode<S> insertIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0, 255)).build();

        CommandNode<S> insertIndexPatternNode = commandManager.argument("pattern", RegistryArgumentType.registryEntry(Registries.BANNER_PATTERN, registryAccess)).build();

        CommandNode<S> insertIndexPatternColorNode = commandManager.argument("color", EnumArgumentType.enums(DyeColor.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            BannerPattern pattern = RegistryArgumentType.getRegistryEntry(context, "pattern", Registries.BANNER_PATTERN);
            DyeColor color = context.getArgument("color", DyeColor.class);
            BannerPatternLayers.Layer layer = getLayer(context.getSource().registryAccess(), pattern, color);
            List<BannerPatternLayers.Layer> layers = new ArrayList<>(getBannerLayers(stack));
            if (index >= layers.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            layers.add(index, layer);
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_INSERT, translation(layer)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> baseNode = commandManager.literal("base").build();

        CommandNode<S> baseSetNode = commandManager.literal("set").build();

        CommandNode<S> baseSetColorNode = commandManager.argument("color", EnumArgumentType.enums(DyeColor.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            DyeColor color = context.getArgument("color", DyeColor.class);
            if (hasBaseColor(stack) && getBaseColor(stack) == color) {
                throw BASE_ALREADY_IS_EXCEPTION;
            }
            ItemStack newStack = setBaseColor(stack, color);

            EditorUtil.setStack(context.getSource(), newStack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_BASE_SET));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> baseRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (stack.getItem() != Items.SHIELD) {
                throw ISNT_SHIELD_EXCEPTION;
            }
            if (!hasBaseColor(stack)) {
                throw BASE_ALREADY_IS_EXCEPTION;
            }
            ItemStack newStack = setBaseColor(stack, null);

            EditorUtil.setStack(context.getSource(), newStack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_BASE_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            setBannerLayers(stack, List.of());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearBeforeNode = commandManager.literal("before").build();

        CommandNode<S> clearBeforeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<BannerPatternLayers.Layer> layers = getBannerLayers(stack);
            if (index > layers.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            layers = layers.subList(index, layers.size());
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR_BEFORE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearAfterNode = commandManager.literal("after").build();

        CommandNode<S> clearAfterIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<BannerPatternLayers.Layer> layers = getBannerLayers(stack);
            if (index >= layers.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            layers = layers.subList(0, index + 1);
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR_AFTER, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get [<index>]
        node.addChild(getNode);
        getNode.addChild(getIndexNode);

        // ... set <index> <pattern> <color>
        node.addChild(setNode);
        setNode.addChild(setIndexNode);
        setIndexNode.addChild(setIndexPatternNode);
        setIndexPatternNode.addChild(setIndexPatternColorNode);

        // ... remove <index>
        node.addChild(removeNode);
        removeNode.addChild(removeIndexNode);

        // ... add <pattern> <color>
        node.addChild(addNode);
        addNode.addChild(addPatternNode);
        addPatternNode.addChild(addPatternColorNode);

        // ... insert <index> <pattern> <color>
        node.addChild(insertNode);
        insertNode.addChild(insertIndexNode);
        insertIndexNode.addChild(insertIndexPatternNode);
        insertIndexPatternNode.addChild(insertIndexPatternColorNode);

        // ... base ...
        node.addChild(baseNode);
        // ... set <color>
        baseNode.addChild(baseSetNode);
        baseSetNode.addChild(baseSetColorNode);
        // ... remove
        baseNode.addChild(baseRemoveNode);

        // ... clear
        node.addChild(clearNode);

        // ... clear before <index>
        clearNode.addChild(clearBeforeNode);
        clearBeforeNode.addChild(clearBeforeIndexNode);
        // ... clear after <index>
        clearNode.addChild(clearAfterNode);
        clearAfterNode.addChild(clearAfterIndexNode);

        return node;
    }
}
