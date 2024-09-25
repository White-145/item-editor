package me.white.simpleitemeditor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Identifier;

public class ClientCommand {
    private final Identifier id;
    private final ClientCommandProvider nodeProvider;

    public ClientCommand(Identifier id, ClientCommandProvider nodeProvider) {
        this.id = id;
        this.nodeProvider = nodeProvider;
    }

    public void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        String name = id.getPath();
        if (dispatcher.getRoot().getChild(name) != null) {
            name = id.toString();
        }
        CommandNode<FabricClientCommandSource> node = nodeProvider.provide(name, registryAccess);
        dispatcher.getRoot().addChild(node);
    }

    @FunctionalInterface
    public interface ClientCommandProvider {
        CommandNode<FabricClientCommandSource> provide(String name, CommandRegistryAccess registryAccess);
    }
}
