package me.white.simpleitemeditor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.node.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class EditCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("edit").build();

        for (Node editNode : new Node[]{
                new AttributeNode(),
                new BannerNode(),
                new ColorNode(),
                new CountNode(),
                new DataNode(),
                new DurabilityNode(),
                new EnchantmentNode(),
                new EquipNode(),
                new GetNode(),
                new HeadNode(),
                new LoreNode(),
                new MaterialNode(),
                new NameNode(),
                new RarityNode(),
                new TrimNode(),
                new UnbreakableNode()
        }) {
            try {
                editNode.register(node, registryAccess);
            } catch (IllegalStateException e) {
                SimpleItemEditor.LOGGER.error("Failed to register " + editNode.getClass().getName() + ": " + e);
            }
        }

        // TODO:
        // ... block ... - edit block data (lectern book, items, metadata)
        // ... script ... - store/execute list of actions to perform. Possibly parameters
        // some way to prevent `/edit` server command to be overriden

//        if (dispatcher.getRoot().getChild("edit") == null) {
            dispatcher.getRoot().addChild(node);
//        }
    }
}
