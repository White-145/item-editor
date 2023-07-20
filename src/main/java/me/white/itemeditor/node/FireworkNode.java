package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public class FireworkNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.cannoteddit")).create();
    public static final CommandSyntaxException NO_FLIGHT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.cannoteddit")).create();
    public static final CommandSyntaxException NO_STARS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.cannoteddit")).create();
    public static final CommandSyntaxException FLIGHT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.firework.error.flightalreadyis")).create();
    public static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((i, size) -> Text.translatable("commands.edit.firework.error.cannoteddit", i, size));
    private static final String OUTPUT_FLIGHT_GET = "commands.edit.firework.get";
    private static final String OUTPUT_FLIGHT_SET = "commands.edit.firework.set";
    private static final String FIREWORKS_KEY = "Fireworks";
    private static final String FLIGHT_KEY = "Flight";
    private static final String EXPLOSIONS_KEY = "Explosions";
    private static final String TYPE_KEY = "Type";
    private static final String COLORS_KEY = "Colors";

    private static void checkCanEdit(FabricClientCommandSource context) throws CommandSyntaxException {
        Item item = ItemUtil.getItemStack(context).getItem();
        if (!(item instanceof FireworkRocketItem)) throw CANNOT_EDIT_EXCEPTION;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("firework")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> flightNode = ClientCommandManager
            .literal("flight")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> flightGetNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());

                int flight = 0;
                if (item.hasNbt() && item.getNbt().contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) flight = item.getSubNbt(FIREWORKS_KEY).getInt(FLIGHT_KEY);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_GET, flight));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> flightSetNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                NbtCompound firework = item.getOrCreateSubNbt(FIREWORKS_KEY);
                if (firework.contains(FLIGHT_KEY, NbtElement.INT_TYPE) && firework.getInt(FLIGHT_KEY) == 1) throw FLIGHT_ALREADY_IS_EXCEPTION;
                firework.putInt(FLIGHT_KEY, 1);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_SET, 1));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> flightSetFlightNode = ClientCommandManager
            .argument("flight", IntegerArgumentType.integer(-128, 127))
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                int flight = IntegerArgumentType.getInteger(context, "flight");

                NbtCompound firework = item.getOrCreateSubNbt(FIREWORKS_KEY);
                if (firework.contains(FLIGHT_KEY, NbtElement.INT_TYPE) && firework.getInt(FLIGHT_KEY) == flight) throw FLIGHT_ALREADY_IS_EXCEPTION;
                firework.putInt(FLIGHT_KEY, flight);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_FLIGHT_SET, flight));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> starNode = ClientCommandManager
            .literal("star")
            .build();

        rootNode.addChild(node);

        // ... flight ...
        node.addChild(flightNode);
        // ... get
        flightNode.addChild(flightGetNode);
        // ... set [<flight>]
        flightNode.addChild(flightSetNode);
        flightSetNode.addChild(flightSetFlightNode);

        // BIG TODO. i cant figure out how to allow user input multiple colors
        // ... star ...
        node.addChild(starNode);
        // ... get
        // ... add <type> <color>
        // ... remove <index>
        // ... clear
    }
}
