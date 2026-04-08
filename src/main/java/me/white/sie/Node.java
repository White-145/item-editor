package me.white.sie;

import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.util.CommonCommandManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public interface Node {
    <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess);
}
