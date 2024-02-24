package me.white.simpleitemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.node.entity.*;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.node.entity.DataNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.*;
import net.minecraft.text.Text;

public class EntityNode implements Node {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.error.cannotedit")).create();

    public static boolean canEdit(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SpawnEggItem ||
                item instanceof ArmorStandItem ||
                item instanceof ItemFrameItem;
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("entity")
                .build();

        for (Node entityNode : new Node[]{
                new AbsorptionNode(),
                new AirNode(),
                new DataNode(),
                new GlowNode(),
                new GravityNode(),
                new HealthNode(),
                new IntellectNode(),
                new InvisibilityNode(),
                new InvulnerabilityNode(),
                new MotionNode(),
                new PersistanceNode(),
                new PickingUpNode(),
                new PositionNode(),
                new RotationNode(),
                new SilenceNode(),
                new TypeNode(),
        }) {
            try {
                entityNode.register(node, registryAccess);
            } catch (IllegalStateException e) {
                SimpleItemEditor.LOGGER.error("Failed to register EntityNode/" + entityNode.getClass().getName() + ": " + e);
            }
        }

        // I HATE working with entities in minecraft. This cannot be made elegant
        // TODO:
        // ... fire ...       - entity fire and visual fire
        // ... name ...       - entity name and its visibility
        // ... loot ...       - entity loot table
        // ... tag ...        - command tags
        // ... hand ...       - left handed
        // ... effect ...     - entity potion effects
        // ... equipment ...  - entity equipment
        // ... attribute ...  - entity attributes
        // ... power ...      - acceleration
        // ... villager ...   - villager trades, profession
        // ... drops ...      - drop chances
        // ... anger ...      - entity anger
        // ... horse ...      - horse type, speed, jump height
        // ... tame ...       - taming
        // ... size ...       - slime size
        // ... block ...      - falling blocks
        // ... explosion ...  - creepers and tnt fuse time, explosion power
        // ... frame ...      - item in item frame, its rotation
        // ... cloud ...      - effect in effect cloud, radius, time
        // ... armorstand ... - armor stand pose, tags
        // ... display ...    - interpolation, transformation, display entity tags
        // n lots more cringe tags
        // will not do it until i find some cool way of organizing it

        rootNode.addChild(node);
    }
}
