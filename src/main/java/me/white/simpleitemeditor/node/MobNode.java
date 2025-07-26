package me.white.simpleitemeditor.node;

import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.node.mob.*;
import me.white.simpleitemeditor.util.CommonCommandManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;

public class MobNode implements Node {
    private static final Node[] NODES = new Node[]{
            new AxolotlNode(),
            new CatNode(),
            //? if >=1.21.5 {
            new ChickenNode(),
            new CowNode(),
            //?}
            new FoxNode(),
            new FrogNode(),
            new HorseNode(),
            new LlamaNode(),
            new MooshroomNode(),
            new ParrotNode()
    };

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
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
