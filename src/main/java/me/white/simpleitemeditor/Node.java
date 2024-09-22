package me.white.simpleitemeditor;

import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;

public interface Node {
    void register(CommonCommandManager<CommandSource> commandManager, CommandNode<CommandSource> rootNode, CommandRegistryAccess registryAccess);
}
