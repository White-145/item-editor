package me.white.itemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.white.itemeditor.ItemEditor;
import me.white.itemeditor.node.entity.*;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ArmorStandItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.text.Text;

public class EntityNode implements Node {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.error.cannotedit")).create();

    public static boolean canEdit(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SpawnEggItem ||
                item instanceof ArmorStandItem;
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("entity")
                .build();

        for (Node entityNode : new Node[] {
                new GlowNode(),
                new GravityNode(),
                new IntellectNode(),
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
                ItemEditor.LOGGER.error("Failed to register EntityNode/" + entityNode.getClass().getName() + ": " + e);
            }
        }

        // TODO:
        // NUMBER
        // ... fire ...
        // ... air ...
        // ... absorption ...
        // ... health ...
        // STRING / REGISTRY
        // ... name ...
        // ... loot ...
        // VECTOR
        // ... leashed ...
        // ... sleep ...
        // OTHER
        // ... tag ...
        // ... hand ...
        // ... effect ...
        // ... equipment ...
        // ... attribute ...
        // and other entity-specific tags, like bat flags, entity variants, display entities and so on (ye thats hell)

        rootNode.addChild(node);
    }
}
