package me.white.sie;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.resources.Identifier;

public class ClientCommand {
    private final Identifier id;
    private final ClientCommandProvider nodeProvider;

    public ClientCommand(Identifier id, ClientCommandProvider nodeProvider) {
        this.id = id;
        this.nodeProvider = nodeProvider;
    }

    public CommandNode<FabricClientCommandSource> register(CommandDispatcher<?> dispatcher, CommandBuildContext registryAccess) {
        String name = id.getPath();
        if (dispatcher.getRoot().getChild(name) != null) {
            name = id.toString();
        }
        return nodeProvider.provide(name, registryAccess);
    }

    @FunctionalInterface
    public interface ClientCommandProvider {
        CommandNode<FabricClientCommandSource> provide(String name, CommandBuildContext registryAccess);
    }
}
