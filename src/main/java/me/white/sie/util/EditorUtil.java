package me.white.sie.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.white.sie.SimpleItemEditor;
import me.white.sie.node.ComponentNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class EditorUtil {
    public static final CommandSyntaxException NOT_CREATIVE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.error.notcreative")).create();
    public static final CommandSyntaxException NO_ITEM_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.error.noitem")).create();
    public static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((index, size) -> Component.translatable("commands.edit.error.outofbounds", index, size));
    public static final Function<SharedSuggestionProvider, IllegalArgumentException> UNKNOWN_SOURCE_EXCEPTION = source -> new IllegalArgumentException("Unknown command source '" + source.getClass().getName() + "'.");

    private static int getSelectedSlot(Inventory inventory) {
        return inventory.getSelectedSlot();
    }

    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> key) {
        RegistryAccess registryManager = Minecraft.getInstance().getConnection().registryAccess();
        return registryManager.lookupOrThrow(key);
    }

    public static EntityType<?> getEntityType(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof MobBucketItem) {
            if (item == Items.AXOLOTL_BUCKET) {
                return EntityTypes.AXOLOTL;
            }
            if (item == Items.COD_BUCKET) {
                return EntityTypes.COD;
            }
            if (item == Items.PUFFERFISH_BUCKET) {
                return EntityTypes.PUFFERFISH;
            }
            if (item == Items.SALMON_BUCKET) {
                return EntityTypes.SALMON;
            }
            if (item == Items.TADPOLE_BUCKET) {
                return EntityTypes.TADPOLE;
            }
            if (item == Items.TROPICAL_FISH_BUCKET) {
                return EntityTypes.TROPICAL_FISH;
            }
            return null;
        }
        if (!(item instanceof SpawnEggItem)) {
            return null;
        }
        return SpawnEggItem.getType(stack);
    }

    public static boolean isClientSource(SharedSuggestionProvider source) {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && source instanceof FabricClientCommandSource;
    }

    public static boolean hasItem(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    public static boolean canEdit(SharedSuggestionProvider source) {
        if (isClientSource(source)) {
            Minecraft client = ((FabricClientCommandSource)source).getClient();
            return client.gameMode.getPlayerMode().isCreative();
        }
        if (source instanceof CommandSourceStack) {
            return ((CommandSourceStack)source).getPlayer().gameMode().isCreative();
        }
        throw UNKNOWN_SOURCE_EXCEPTION.apply(source);
    }

    public static void checkHasItem(ItemStack stack) throws CommandSyntaxException {
        if (!hasItem(stack)) {
            throw NO_ITEM_EXCEPTION;
        }
    }

    public static ItemStack getCheckedStack(SharedSuggestionProvider source) throws CommandSyntaxException {
        ItemStack stack = getStack(source);
        checkHasItem(stack);
        return stack;
    }

    public static void checkCanEdit(SharedSuggestionProvider source) throws CommandSyntaxException {
        if (!canEdit(source)) {
            throw NOT_CREATIVE_EXCEPTION;
        }
    }

    public static ItemStack getStack(SharedSuggestionProvider source) {
        if (isClientSource(source)) {
            return ((FabricClientCommandSource)source).getPlayer().getMainHandItem();
        }
        if (source instanceof CommandSourceStack) {
            return ((CommandSourceStack)source).getPlayer().getMainHandItem();
        }
        throw UNKNOWN_SOURCE_EXCEPTION.apply(source);
    }

    public static void setStack(SharedSuggestionProvider source, ItemStack stack) throws CommandSyntaxException {
        if (getStack(source) == stack) {
            SimpleItemEditor.LOGGER.warn("Using setStack without clonning result of getStack (If you see this, report to github)");
        }
        if (!canEdit(source)) {
            throw NOT_CREATIVE_EXCEPTION;
        }
        Inventory inventory;
        if (isClientSource(source)) {
            inventory = ((FabricClientCommandSource)source).getPlayer().getInventory();
        } else if (source instanceof CommandSourceStack) {
            inventory = ((CommandSourceStack)source).getPlayer().getInventory();
        } else {
            throw UNKNOWN_SOURCE_EXCEPTION.apply(source);
        }
        int slot = getSelectedSlot(inventory);
        inventory.setItem(slot, stack);
        if (isClientSource(source)) {
            ((FabricClientCommandSource)source).getClient().getConnection().send(new ServerboundSetCreativeModeSlotPacket(36 + slot, stack));
        }
    }

    public static void sendFeedback(SharedSuggestionProvider source, Component message) {
        if (isClientSource(source)) {
            ((FabricClientCommandSource)source).sendFeedback(message);
        } else if (source instanceof CommandSourceStack) {
            ((CommandSourceStack)source).sendSuccess(() -> message, false);
        } else {
            throw UNKNOWN_SOURCE_EXCEPTION.apply(source);
        }
    }

    public static void sendError(SharedSuggestionProvider source, Component message) {
        if (isClientSource(source)) {
            ((FabricClientCommandSource)source).sendError(message);
        } else if (source instanceof CommandSourceStack) {
            ((CommandSourceStack)source).sendFailure(message);
        } else {
            throw UNKNOWN_SOURCE_EXCEPTION.apply(source);
        }
    }

    public static String formatColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static int meanColor(int[] colors) {
        int r = 0;
        int g = 0;
        int b = 0;
        for (int color : colors) {
            r += (color & 0xFF0000) >> 16;
            g += (color & 0x00FF00) >> 8;
            b += color & 0x0000FF;
        }
        return ((r / colors.length) << 16) + ((g / colors.length) << 8) + b / colors.length;
    }

    public static String textToString(Component text) {
        StringBuilder result = new StringBuilder();
        Style[] prevStyle = new Style[]{ Style.EMPTY };
        text.visit((style, literal) -> {
            if (literal.isEmpty()) {
                return Optional.empty();
            }
            TextColor color = style.getColor();

            if (!prevStyle[0].equals(style)) {
                result.append("&");
                if (color == null) {
                    result.append("r");
                } else if (color.formatValue().startsWith("#")) {
                    result.append(color.formatValue());
                } else {
                    ChatFormatting formatting = ChatFormatting.valueOf(ChatFormatting.class, color.formatValue());
                    result.append(Integer.toHexString(formatting.ordinal()));
                }
            }
            if (style.isObfuscated()) {
                result.append("&k");
            }
            if (style.isBold()) {
                result.append("&l");
            }
            if (style.isStrikethrough()) {
                result.append("&m");
            }
            if (style.isUnderlined()) {
                result.append("&n");
            }
            if (style.isItalic()) {
                result.append("&o");
            }

            prevStyle[0] = style;
            result.append(literal);
            return Optional.empty();
        }, Style.EMPTY);
        return result.toString();
    }

    public static Map<Identifier, Tag> getComponents(ItemStack stack, RegistryAccess registryManager, boolean includeDefault) throws CommandSyntaxException {
        ItemStack defaultStack = stack.getItem().getDefaultInstance();
        Map<Identifier, Tag> components = new HashMap<>();
        for (TypedDataComponent<?> component : stack.getComponents()) {
            DataComponentType<?> componentType = component.type();
            Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
            Tag element = ComponentNode.getFromComponent(stack, componentType, registryManager);
            if (includeDefault || !defaultStack.has(componentType)) {
                components.put(id, element);
            } else {
                Tag defaultElement = ComponentNode.getFromComponent(defaultStack, componentType, registryManager);
                if (!element.equals(defaultElement)) {
                    components.put(id, element);
                }
            }
        }
        // get removed components too
        for (TypedDataComponent<?> defaultComponent : defaultStack.getComponents()) {
            DataComponentType<?> componentType = defaultComponent.type();
            Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(componentType);
            if (!stack.has(componentType)) {
                components.put(id, null);
            }
        }
        return components;
    }

    public static Component colorTranslation(DyeColor color) {
        return Component.translatable("color.minecraft." + color.getId());
    }
}
