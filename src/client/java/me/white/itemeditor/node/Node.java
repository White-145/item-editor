package me.white.itemeditor.node;

import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public interface Node {
    void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess);
}
