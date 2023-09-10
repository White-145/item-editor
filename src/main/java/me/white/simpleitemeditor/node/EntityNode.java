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

        for (Node entityNode : new Node[] {
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

        // TODO:
        // ... fire ... - entity fire and visual fire
        // ... name ... - entity name and its visibility
        // ... loot ... - entity loot table
        // ... tag ... - command tags
        // ... hand ... - left handed
        // ... effect ... - entity potion effects
        // ... equipment ... - entity equipment
        // ... attribute ... - entity attributes
        // and other entity-specific tags, like bat flags, entity variants, display entities and so on. Will figure this out after im done with common tags

        rootNode.addChild(node);
    }
}
