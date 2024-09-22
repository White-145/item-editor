package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.argument.enums.DyeColorArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class BannerNode implements Node {
    private static final CommandSyntaxException ISNT_BANNER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.isntbanner")).create();
    private static final CommandSyntaxException NO_LAYERS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.nolayers")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.alreadyis")).create();
    private static final CommandSyntaxException NO_BASE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.nobase")).create();
    private static final CommandSyntaxException BASE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.basealreadyis")).create();
    private static final CommandSyntaxException ISNT_SHIELD_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.banner.error.isntshield")).create();
    private static final String OUTPUT_GET = "commands.edit.banner.get";
    private static final String OUTPUT_GET_LAYER = "commands.edit.banner.getlayer";
    private static final String OUTPUT_SET = "commands.edit.banner.set";
    private static final String OUTPUT_REMOVE = "commands.edit.banner.remove";
    private static final String OUTPUT_ADD = "commands.edit.banner.add";
    private static final String OUTPUT_INSERT = "commands.edit.banner.insert";
    private static final String OUTPUT_BASE_GET = "commands.edit.banner.baseget";
    private static final String OUTPUT_BASE_SET = "commands.edit.banner.baseset";
    private static final String OUTPUT_BASE_REMOVE = "commands.edit.banner.baseremove";
    private static final String OUTPUT_CLEAR = "commands.edit.banner.clear";
    private static final String OUTPUT_CLEAR_BEFORE = "commands.edit.banner.clearbefore";
    private static final String OUTPUT_CLEAR_AFTER = "commands.edit.banner.clearafter";

    private static boolean isBanner(ItemStack stack) {
        return stack.isIn(ItemTags.BANNERS) || stack.getItem() == Items.SHIELD;
    }

    private static boolean hasBaseColor(ItemStack stack) {
        if (stack.getItem() != Items.SHIELD) {
            return true;
        }
        return stack.contains(DataComponentTypes.BASE_COLOR);
    }

    private static DyeColor getBaseColor(ItemStack stack) {
        if (!hasBaseColor(stack)) {
            return DyeColor.WHITE;
        }
        if (stack.getItem() != Items.SHIELD) {
            Item item = stack.getItem();
            if (item == Items.WHITE_BANNER) {
                return DyeColor.WHITE;
            }
            if (item == Items.ORANGE_BANNER) {
                return DyeColor.ORANGE;
            }
            if (item == Items.MAGENTA_BANNER) {
                return DyeColor.MAGENTA;
            }
            if (item == Items.LIGHT_BLUE_BANNER) {
                return DyeColor.LIGHT_BLUE;
            }
            if (item == Items.YELLOW_BANNER) {
                return DyeColor.YELLOW;
            }
            if (item == Items.LIME_BANNER) {
                return DyeColor.LIME;
            }
            if (item == Items.PINK_BANNER) {
                return DyeColor.PINK;
            }
            if (item == Items.GRAY_BANNER) {
                return DyeColor.GRAY;
            }
            if (item == Items.LIGHT_GRAY_BANNER) {
                return DyeColor.LIGHT_GRAY;
            }
            if (item == Items.CYAN_BANNER) {
                return DyeColor.CYAN;
            }
            if (item == Items.PURPLE_BANNER) {
                return DyeColor.PURPLE;
            }
            if (item == Items.BLUE_BANNER) {
                return DyeColor.BLUE;
            }
            if (item == Items.BROWN_BANNER) {
                return DyeColor.BROWN;
            }
            if (item == Items.GREEN_BANNER) {
                return DyeColor.GREEN;
            }
            if (item == Items.RED_BANNER) {
                return DyeColor.RED;
            }
            if (item == Items.BLACK_BANNER) {
                return DyeColor.BLACK;
            }
        }
        return stack.get(DataComponentTypes.BASE_COLOR);
    }

    private static ItemStack setBaseColor(ItemStack stack, DyeColor color) {
        if (color == null) {
            stack.remove(DataComponentTypes.BASE_COLOR);
        } else {
            if (stack.getItem() != Items.SHIELD) {
                return switch (color) {
                    case WHITE -> stack.copyComponentsToNewStack(Items.WHITE_BANNER, stack.getCount());
                    case ORANGE -> stack.copyComponentsToNewStack(Items.ORANGE_BANNER, stack.getCount());
                    case MAGENTA -> stack.copyComponentsToNewStack(Items.MAGENTA_BANNER, stack.getCount());
                    case LIGHT_BLUE -> stack.copyComponentsToNewStack(Items.LIGHT_BLUE_BANNER, stack.getCount());
                    case YELLOW -> stack.copyComponentsToNewStack(Items.YELLOW_BANNER, stack.getCount());
                    case LIME -> stack.copyComponentsToNewStack(Items.LIME_BANNER, stack.getCount());
                    case PINK -> stack.copyComponentsToNewStack(Items.PINK_BANNER, stack.getCount());
                    case GRAY -> stack.copyComponentsToNewStack(Items.GRAY_BANNER, stack.getCount());
                    case LIGHT_GRAY -> stack.copyComponentsToNewStack(Items.LIGHT_GRAY_BANNER, stack.getCount());
                    case CYAN -> stack.copyComponentsToNewStack(Items.CYAN_BANNER, stack.getCount());
                    case PURPLE -> stack.copyComponentsToNewStack(Items.PURPLE_BANNER, stack.getCount());
                    case BLUE -> stack.copyComponentsToNewStack(Items.BLUE_BANNER, stack.getCount());
                    case BROWN -> stack.copyComponentsToNewStack(Items.BROWN_BANNER, stack.getCount());
                    case GREEN -> stack.copyComponentsToNewStack(Items.GREEN_BANNER, stack.getCount());
                    case RED -> stack.copyComponentsToNewStack(Items.RED_BANNER, stack.getCount());
                    case BLACK -> stack.copyComponentsToNewStack(Items.BLACK_BANNER, stack.getCount());
                };
            }
            stack.set(DataComponentTypes.BASE_COLOR, color);
        }
        return stack;
    }

    private static boolean hasBannerLayers(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.BANNER_PATTERNS)) {
            return false;
        }
        return !stack.get(DataComponentTypes.BANNER_PATTERNS).layers().isEmpty();
    }

    private static List<BannerPatternsComponent.Layer> getBannerLayers(ItemStack stack) {
        if (!hasBannerLayers(stack)) {
            return List.of();
        }
        return stack.get(DataComponentTypes.BANNER_PATTERNS).layers();
    }

    private static void setBannerLayers(ItemStack stack, List<BannerPatternsComponent.Layer> layers) {
        if (layers == null || layers.isEmpty()) {
            stack.remove(DataComponentTypes.BANNER_PATTERNS);
        } else {
            stack.set(DataComponentTypes.BANNER_PATTERNS, new BannerPatternsComponent(layers));
        }
    }

    private static BannerPatternsComponent.Layer getLayer(DynamicRegistryManager registryManager, BannerPattern pattern, DyeColor color) {
        RegistryEntry<BannerPattern> patternEntry = registryManager.get(RegistryKeys.BANNER_PATTERN).getEntry(pattern);
        return new BannerPatternsComponent.Layer(patternEntry, color);
    }

    private static Text translation(BannerPatternsComponent.Layer layer) {
        String string = layer.pattern().value().translationKey();
        return Text.translatable(string + "." + layer.color().getName());
    }

    @Override
    public void register(CommonCommandManager<CommandSource> commandManager, CommandNode<CommandSource> rootNode, CommandRegistryAccess registryAccess) {
        CommandNode<CommandSource> node = commandManager.literal("banner").build();

        CommandNode<CommandSource> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            List<BannerPatternsComponent.Layer> layers = getBannerLayers(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET));
            for (int i = 0; i < layers.size(); ++i) {
                BannerPatternsComponent.Layer pattern = layers.get(i);
                EditorUtil.sendFeedback(context.getSource(), Text.empty().append(Text.literal(i + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(translation(pattern)));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> getIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<BannerPatternsComponent.Layer> layers = getBannerLayers(stack);
            if (layers.size() <= index) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            BannerPatternsComponent.Layer layer = layers.get(index);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_LAYER, translation(layer)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> setNode = commandManager.literal("set").build();

        CommandNode<CommandSource> setIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).build();

        CommandNode<CommandSource> setIndexPatternNode = commandManager.argument("pattern", RegistryArgumentType.registryEntry(RegistryKeys.BANNER_PATTERN, registryAccess)).build();

        CommandNode<CommandSource> setIndexPatternColorNode = commandManager.argument("color", DyeColorArgumentType.dyeColor()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            List<BannerPatternsComponent.Layer> layers = new ArrayList<>(getBannerLayers(stack));
            int index = IntegerArgumentType.getInteger(context, "index");
            if (layers.isEmpty() && index != 0) {
                throw NO_LAYERS_EXCEPTION;
            }
            BannerPattern pattern = RegistryArgumentType.getRegistryEntry(context, "pattern", RegistryKeys.BANNER_PATTERN);
            DyeColor color = DyeColorArgumentType.getDyeColor(context, "color");
            BannerPatternsComponent.Layer layer = getLayer(context.getSource().getRegistryManager(), pattern, color);
            if (layers.size() < index) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            if (layers.size() == index) {
                layers.add(layer);
            } else {
                BannerPatternsComponent.Layer oldBannerLayer = layers.get(index);
                if (oldBannerLayer.equals(layer)) {
                    throw ALREADY_IS_EXCEPTION;
                }
                layers.set(index, layer);
            }
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, translation(layer)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> removeNode = commandManager.literal("remove").build();

        CommandNode<CommandSource> removeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            List<BannerPatternsComponent.Layer> layers = new ArrayList<>(getBannerLayers(stack));
            int index = IntegerArgumentType.getInteger(context, "index");
            if (index >= layers.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            layers.remove(index);
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> addNode = commandManager.literal("add").build();

        CommandNode<CommandSource> addPatternNode = commandManager.argument("pattern", RegistryArgumentType.registryEntry(RegistryKeys.BANNER_PATTERN, registryAccess)).build();

        CommandNode<CommandSource> addPatternColorNode = commandManager.argument("color", DyeColorArgumentType.dyeColor()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            BannerPattern pattern = RegistryArgumentType.getRegistryEntry(context, "pattern", RegistryKeys.BANNER_PATTERN);
            DyeColor color = DyeColorArgumentType.getDyeColor(context, "color");
            BannerPatternsComponent.Layer layer = getLayer(context.getSource().getRegistryManager(), pattern, color);
            List<BannerPatternsComponent.Layer> layers = new ArrayList<>(getBannerLayers(stack));
            layers.add(layer);
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_ADD, translation(layer)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> insertNode = commandManager.literal("insert").build();

        CommandNode<CommandSource> insertIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0, 255)).build();

        CommandNode<CommandSource> insertIndexPatternNode = commandManager.argument("pattern", RegistryArgumentType.registryEntry(RegistryKeys.BANNER_PATTERN, registryAccess)).build();

        CommandNode<CommandSource> insertIndexPatternColorNode = commandManager.argument("color", DyeColorArgumentType.dyeColor()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            BannerPattern pattern = RegistryArgumentType.getRegistryEntry(context, "pattern", RegistryKeys.BANNER_PATTERN);
            DyeColor color = DyeColorArgumentType.getDyeColor(context, "color");
            BannerPatternsComponent.Layer layer = getLayer(context.getSource().getRegistryManager(), pattern, color);
            List<BannerPatternsComponent.Layer> layers = new ArrayList<>(getBannerLayers(stack));
            if (index >= layers.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            layers.add(index, layer);
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_INSERT, translation(layer)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> baseNode = commandManager.literal("base").build();

        CommandNode<CommandSource> baseGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBaseColor(stack)) {
                throw NO_BASE_EXCEPTION;
            }
            DyeColor color = getBaseColor(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_BASE_GET, color.getName()));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> baseSetNode = commandManager.literal("set").build();

        CommandNode<CommandSource> baseSetColorNode = commandManager.argument("color", DyeColorArgumentType.dyeColor()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            DyeColor color = DyeColorArgumentType.getDyeColor(context, "color");
            if (hasBaseColor(stack) && getBaseColor(stack) == color) {
                throw BASE_ALREADY_IS_EXCEPTION;
            }
            ItemStack newStack = setBaseColor(stack, color);

            EditorUtil.setStack(context.getSource(), newStack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_BASE_SET, color.getName()));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> baseRemoveNode = commandManager.literal("remove").executes(context -> {
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_BASE_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> clearNode = commandManager.literal("clear").executes(context -> {
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> clearBeforeNode = commandManager.literal("before").build();

        CommandNode<CommandSource> clearBeforeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<BannerPatternsComponent.Layer> layers = getBannerLayers(stack);
            if (index > layers.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            layers = layers.subList(index, layers.size());
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CLEAR_BEFORE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> clearAfterNode = commandManager.literal("after").build();

        CommandNode<CommandSource> clearAfterIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBanner(stack)) {
                throw ISNT_BANNER_EXCEPTION;
            }
            if (!hasBannerLayers(stack)) {
                throw NO_LAYERS_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<BannerPatternsComponent.Layer> layers = getBannerLayers(stack);
            if (index >= layers.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, layers.size());
            }
            layers = layers.subList(0, index + 1);
            setBannerLayers(stack, layers);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CLEAR_AFTER, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

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
        // ... get
        baseNode.addChild(baseGetNode);
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
    }
}
