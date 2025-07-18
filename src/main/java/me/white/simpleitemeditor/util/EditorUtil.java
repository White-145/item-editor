package me.white.simpleitemeditor.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.node.ComponentNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.component.Component;
//? if >=1.21.1 {
import net.minecraft.component.ComponentType;
//?} else {
/*import net.minecraft.component.DataComponentType;
*///?}
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class EditorUtil {
    public static final CommandSyntaxException NOT_CREATIVE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.notcreative")).create();
    public static final CommandSyntaxException NO_ITEM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.noitem")).create();
    public static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((index, size) -> Text.translatable("commands.edit.error.outofbounds", index, size));
    public static final Function<CommandSource, IllegalArgumentException> UNKNOWN_SOURCE_EXCEPTION = source -> new IllegalArgumentException("Unknown command source '" + source.getClass().getName() + "'.");

    private static int getSelectedSlot(PlayerInventory inventory) {
        //? if >=1.21.5 {
        return inventory.getSelectedSlot();
        //?} else {
        /*return inventory.selectedSlot;
        *///?}
    }

    public static <T> Registry<T> getRegistry(DynamicRegistryManager registryManager, RegistryKey<Registry<T>> key) {
        //? if >=1.21.2 {
        return registryManager.getOrThrow(key);
        //?} else {
        /*return registryManager.get(key);
        *///?}
    }

    public static boolean isClientSource(CommandSource source) {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && source instanceof FabricClientCommandSource;
    }

    public static boolean hasItem(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    public static boolean canEdit(CommandSource source) {
        if (isClientSource(source)) {
            MinecraftClient client = ((FabricClientCommandSource)source).getClient();
            return client.interactionManager.getCurrentGameMode().isCreative();
        }
        if (source instanceof ServerCommandSource) {
            return ((ServerCommandSource)source).getPlayer().interactionManager.getGameMode().isCreative();
        }
        throw UNKNOWN_SOURCE_EXCEPTION.apply(source);
    }

    public static void checkHasItem(ItemStack stack) throws CommandSyntaxException {
        if (!hasItem(stack)) {
            throw NO_ITEM_EXCEPTION;
        }
    }

    public static ItemStack getCheckedStack(CommandSource source) throws CommandSyntaxException {
        ItemStack stack = getStack(source);
        checkHasItem(stack);
        return stack;
    }

    public static void checkCanEdit(CommandSource source) throws CommandSyntaxException {
        if (!canEdit(source)) {
            throw NOT_CREATIVE_EXCEPTION;
        }
    }

    public static ItemStack getStack(CommandSource source) {
        if (isClientSource(source)) {
            return ((FabricClientCommandSource)source).getPlayer().getMainHandStack();
        }
        if (source instanceof ServerCommandSource) {
            return ((ServerCommandSource)source).getPlayer().getMainHandStack();
        }
        throw UNKNOWN_SOURCE_EXCEPTION.apply(source);
    }

    public static void setStack(CommandSource source, ItemStack stack) throws CommandSyntaxException {
        if (getStack(source) == stack) {
            SimpleItemEditor.LOGGER.warn("Using setStack without clonning result of getStack (If you see this, report to github)");
        }
        if (!canEdit(source)) {
            throw NOT_CREATIVE_EXCEPTION;
        }
        PlayerInventory inventory;
        if (isClientSource(source)) {
            inventory = ((FabricClientCommandSource)source).getPlayer().getInventory();
        } else if (source instanceof ServerCommandSource) {
            inventory = ((ServerCommandSource)source).getPlayer().getInventory();
        } else {
            throw UNKNOWN_SOURCE_EXCEPTION.apply(source);
        }
        int slot = getSelectedSlot(inventory);
        inventory.setStack(slot, stack);
        if (isClientSource(source)) {
            ((FabricClientCommandSource)source).getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, stack));
        }
    }

    public static void sendFeedback(CommandSource source, Text message) {
        if (isClientSource(source)) {
            ((FabricClientCommandSource)source).sendFeedback(message);
        } else if (source instanceof ServerCommandSource) {
            ((ServerCommandSource)source).sendFeedback(() -> message, false);
        } else {
            throw UNKNOWN_SOURCE_EXCEPTION.apply(source);
        }
    }

    public static void sendError(CommandSource source, Text message) {
        if (isClientSource(source)) {
            ((FabricClientCommandSource)source).sendError(message);
        } else if (source instanceof ServerCommandSource) {
            ((ServerCommandSource)source).sendError(message);
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

    public static String textToString(Text text) {
        StringBuilder result = new StringBuilder();
        Style[] prevStyle = new Style[]{ Style.EMPTY };
        text.visit((style, literal) -> {
            if (literal.isEmpty()) {
                return Optional.empty();
            }
            if (style != null) {
                TextColor color = style.getColor();

                if (!prevStyle[0].equals(style)) {
                    result.append("&");
                    if (color == null) {
                        result.append("r");
                    } else if (color.getName().startsWith("#")) {
                        result.append(color.getName());
                    } else {
                        result.append(Formatting.byName(color.getName()).getCode());
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
            }
            result.append(literal);
            return Optional.empty();
        }, Style.EMPTY);
        return result.toString();
    }

    public static Map<Identifier, NbtElement> getComponents(ItemStack stack, DynamicRegistryManager registryManager, boolean includeDefault) throws CommandSyntaxException {
        ItemStack defaultStack = stack.getItem().getDefaultStack();
        Map<Identifier, NbtElement> components = new HashMap<>();
        for (Component<?> component : stack.getComponents()) {
            //? if >=1.21.1 {
            ComponentType<?> componentType = component.type();
            //?} else {
            /*DataComponentType<?> componentType = component.type();
            *///?}
            Identifier id = Registries.DATA_COMPONENT_TYPE.getId(componentType);
            NbtElement element = ComponentNode.getFromComponent(stack, componentType, registryManager);
            if (includeDefault || !defaultStack.contains(componentType)) {
                components.put(id, element);
            } else {
                NbtElement defaultElement = ComponentNode.getFromComponent(defaultStack, componentType, registryManager);
                if (!element.equals(defaultElement)) {
                    components.put(id, element);
                }
            }
        }
        // get removed components too
        for (Component<?> defaultComponent : defaultStack.getComponents()) {
            //? if >=1.21.1 {
            ComponentType<?> componentType = defaultComponent.type();
            //?} else {
            /*DataComponentType<?> componentType = defaultComponent.type();
            *///?}
            Identifier id = Registries.DATA_COMPONENT_TYPE.getId(componentType);
            if (!stack.contains(componentType)) {
                components.put(id, null);
            }
        }
        return components;
    }
}
