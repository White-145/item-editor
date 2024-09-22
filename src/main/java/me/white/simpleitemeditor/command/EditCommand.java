package me.white.simpleitemeditor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.node.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class EditCommand {
    // TODO:
    // ... script ... - store/execute list of actions to perform. Possibly parameters
    // ... whitelist ... - candestroy/canplaceon
    // ... firework ... - firework rockets and firework star
    // ... blockstate ...
    // ... bundle ...
    // ... projectile ... - charged projectiles (crossbow)
    // ... container ... - contents and loot
    // ... enchantment value ... - 1.21 enchantable component
    // ... fire_resistance ...
    // ... instrument ...
    // ... jukebox ... - jukebox_playable
    // ... lock ... - container lock
    // ... lodestone ... - lodestone compass
    // ... map ... - map color, decorations, id
    // ... head sound ... - noteblock sound
    // ... recipes ... - knowledge book recipes
    // ... repair/durability repair ... - repair_cost, repairable components
    // ... enchantment stored ...
    // ... tool ...
    // ... book ...
    // ... use ... - consumable, food, use_cooldown, use_remainder

    private static Node[] NODES = new Node[]{
            new AttributeNode(),
            new BannerNode(),
            new ComponentNode(),
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
            new ModelNode(),
            new NameNode(),
            new PotionNode(),
            new RarityNode(),
            new TooltipNode(),
            new TrimNode()
    };

    @SuppressWarnings("unchecked")
    private static void register(CommonCommandManager<? extends CommandSource> commandManager, CommandNode<? extends CommandSource> node, CommandRegistryAccess registryAccess) {
        for (Node childNode : NODES) try {
            childNode.register((CommonCommandManager<CommandSource>)commandManager, (CommandNode<CommandSource>)node, registryAccess);
        } catch (IllegalStateException e) {
            SimpleItemEditor.LOGGER.error("Failed to register {}: {}", childNode.getClass().getName(), e);
        }
    }

    public static void registerClient(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandDispatcher<FabricClientCommandSource> activeDispatcher, CommandRegistryAccess registryAccess) {
        boolean canOverride = dispatcher.getRoot().getChild("edit") == null;
        String name = canOverride ? "edit" : "sie:edit";

        CommandNode<FabricClientCommandSource> node = ClientCommandManager.literal(name).build();
        CommonCommandManager<FabricClientCommandSource> commandManager = new CommonCommandManager<>();
        register(commandManager, node, registryAccess);

        dispatcher.getRoot().addChild(node);
        activeDispatcher.getRoot().addChild(node);
    }

    public static void registerServer(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        CommandNode<ServerCommandSource> node = CommandManager.literal("edit").build();
        CommonCommandManager<ServerCommandSource> commandManager = new CommonCommandManager<>();
        register(commandManager, node, registryAccess);

        dispatcher.getRoot().addChild(node);
    }
}
