package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;

public class PotionNode {
    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("potion")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> getIndexNode = ClientCommandManager
            .argument("index", IntegerArgumentType.integer(0))
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
            .literal("add")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Reference<StatusEffect>> addEffectNode = ClientCommandManager
            .argument("effect", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT))
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> addEffectLevelNode = ClientCommandManager
            .argument("level", IntegerArgumentType.integer(-128, 127))
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Boolean> addEffectLevelParticlesNode = ClientCommandManager
            .argument("particles", BoolArgumentType.bool())
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
            .literal("remove")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> removeIndexNode = ClientCommandManager
            .argument("index", IntegerArgumentType.integer(0))
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
            .literal("clear")
            .build();
        
        rootNode.addChild(node);
        
        // ... get [<index>]
        node.addChild(getNode);
        getNode.addChild(getIndexNode);

        // ... add <effect> [<level>] [<particles>]
        node.addChild(addNode);
        addNode.addChild(addEffectNode);
        addEffectNode.addChild(addEffectLevelNode);
        addEffectLevelNode.addChild(addEffectLevelParticlesNode);

        // ... remove <index>
        node.addChild(removeNode);
        removeNode.addChild(removeIndexNode);

        // ... clear
        node.addChild(clearNode);
    }
}
