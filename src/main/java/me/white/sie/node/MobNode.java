package me.white.sie.node;

import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.SimpleItemEditor;
import me.white.sie.node.mob.*;
import me.white.sie.util.CommonCommandManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;

public class MobNode implements Node {
    private static final Node[] NODES = new Node[]{
            new AxolotlNode(),
            new CatNode(),
            new ChickenNode(),
            new CowNode(),
            new PigNode(),
            new FoxNode(),
            new FrogNode(),
            new HorseNode(),
            new LlamaNode(),
            new MooshroomNode(),
            new ParrotNode(),
            new RabbitNode(),
            new SalmonNode(),
            new SheepNode(),
            new ShulkerNode()
    };

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("mob").build();

        for (Node childNode : NODES) {
            try {
                node.addChild(childNode.register(commandManager, registryAccess));
            } catch (IllegalStateException e) {
                SimpleItemEditor.LOGGER.error("Failed to register {}", childNode.getClass().getName(), e);
            }
        }

        return node;
    }
}
