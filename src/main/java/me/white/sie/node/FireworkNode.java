package me.white.sie.node;

import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.util.CommonCommandManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;

import java.util.List;

public class FireworkNode implements Node {
    private static boolean hasFirework(ItemStack stack) {
        return stack.has(DataComponents.FIREWORKS);
    }

    private static int getFlight(ItemStack stack) {
        return stack.get(DataComponents.FIREWORKS).flightDuration();
    }

    private static void setFlight(ItemStack stack, int flight) {
        List<FireworkExplosion> explosions = List.of();
        if (stack.has(DataComponents.FIREWORKS)) {
            explosions = stack.get(DataComponents.FIREWORKS).explosions();
        }
        stack.set(DataComponents.FIREWORKS, new Fireworks(flight, explosions));
    }

    private static List<FireworkExplosion> getExplosions(ItemStack stack) {
        return stack.get(DataComponents.FIREWORKS).explosions();
    }

    private static void setExplosions(ItemStack stack, List<FireworkExplosion> explosions) {
        int flight = 0;
        if (stack.has(DataComponents.FIREWORKS)) {
            flight = stack.get(DataComponents.FIREWORKS).flightDuration();
        }
        stack.set(DataComponents.FIREWORKS, new Fireworks(flight, explosions));
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
//        CommandNode<S> node = commandManager.literal("firework").build();
//
//        CommandNode<S> flightNode = commandManager.literal("flight").build();
//
//        CommandNode<S> flightGetNode = commandManager.literal("get").build();
//
//        CommandNode<S> flightSetNode = commandManager.literal("set").build();
//
//        CommandNode<S> flightSetFlightNode = commandManager.argument("flight", IntegerArgumentType.integer()).build();
//
//        CommandNode<S> explosionNode = commandManager.literal("explosion").build();
//
//        CommandNode<S> explosionGetNode = commandManager.literal("get").build();
//
//        CommandNode<S> explosionGetIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).build();
//
//        CommandNode<S> explosionSetNode = commandManager.literal("set").build();
//
//        CommandNode<S> explosionSetIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).build();
//
//        CommandNode<S> explosionAddNode = commandManager.literal("add").build();
//
//        CommandNode<S> explosionRemoveNode = commandManager.literal("remove").build();
//
//        CommandNode<S> explosionRemoveIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).build();
//
//        CommandNode<S> explosionClearNode = commandManager.literal("clear").build();
//
//        CommandNode<S> explosionClearBeforeNode = commandManager.literal("before").build();
//
//        CommandNode<S> explosionClearBeforeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).build();
//
//        CommandNode<S> explosionClearAfterNode = commandManager.literal("after").build();
//
//        CommandNode<S> explosionClearAfterIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).build();

        CommandNode<S> node = commandManager.literal("locate").build();

        CommandNode<S> playerNode = commandManager.argument("player", EntityArgument.player()).build();

        // ... flight
        // ... get
        // ... set <flight>

        // ... explosion
        // ... get [<index>]
        // ... set <index> <shape> <colors> [<trail>] <twinkle> <fadeColors>
        // ... add <shape> <colors> [<trail>] <twinkle> <fadeColors>
        // ... remove <index>
        // ... clear
        // ... before <index>
        // ... after <index>

        return node;
    }
}
