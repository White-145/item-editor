package me.white.itemeditor.node;

import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class FireworkNode {
    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("firework")
            .build();

        rootNode.addChild(node);

        // ... flight ...
        // ... get
        // ... set <flight>

        // ... star ...
        // ... get
        // ... add <type> <color>
        // ... remove <index>
        // ... clear
    }
}
