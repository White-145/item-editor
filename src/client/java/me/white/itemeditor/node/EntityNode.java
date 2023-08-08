package me.white.itemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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

public class EntityNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.error.cannotedit")).create();

    public static boolean canEdit(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof SpawnEggItem ||
                item instanceof ArmorStandItem;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("entity")
                .build();

        rootNode.addChild(node);

        // ... type ...
        TypeNode.register(node, registryAccess);

        // ... position ...
        PositionNode.register(node, registryAccess);
    }
}
