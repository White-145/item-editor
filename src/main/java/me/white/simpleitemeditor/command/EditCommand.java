package me.white.simpleitemeditor.command;

import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.ClientCommand;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.node.*;
import me.white.simpleitemeditor.util.CommonCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.Identifier;

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

    public static final ClientCommand PROVIDER = new ClientCommand(Identifier.of("sie", "edit"), (name, registryAccess) -> {
        CommonCommandManager<FabricClientCommandSource> commandManager = new CommonCommandManager<>();
        CommandNode<FabricClientCommandSource> node = commandManager.literal(name).build();
        for (Node childNode : NODES) try {
            node.addChild(childNode.register(commandManager, registryAccess));
        } catch (IllegalStateException e) {
            SimpleItemEditor.LOGGER.error("Failed to register {}", childNode.getClass().getName(), e);
        }
        return node;
    });
}
