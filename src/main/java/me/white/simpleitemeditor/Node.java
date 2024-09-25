package me.white.simpleitemeditor;

import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;

public interface Node {
    <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess);
}
