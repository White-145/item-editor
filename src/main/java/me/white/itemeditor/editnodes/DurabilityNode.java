package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class DurabilityNode {
	public static final CommandSyntaxException TOO_MUCH = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.durability.toomuch")).create();

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("durability")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                int old = item.getDamage();
                item.setDamage(0);
                EditCommand.setItemStack(context.getSource(), item);
                return old;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> setDurabilityNode = ClientCommandManager
            .argument("durability", IntegerArgumentType.integer())
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                int damage = IntegerArgumentType.getInteger(context, "durability");
                if (damage > item.getMaxDamage()) throw TOO_MUCH;
                if (damage < -item.getMaxDamage()) throw TOO_MUCH;
                int old = item.getDamage();
                if (damage < 0) {
                    item.setDamage(-damage);
                } else {
                    item.setDamage(item.getMaxDamage() - damage);
                }
                EditCommand.setItemStack(context.getSource(), item);
                return old;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> percentNode = ClientCommandManager
            .literal("percent")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Double> percentDurabilityNode = ClientCommandManager
            .argument("durability", DoubleArgumentType.doubleArg(0, 100))
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                double damage = DoubleArgumentType.getDouble(context, "durability");
                int old = (int)((double)item.getDamage() / item.getMaxDamage() * 100);
                item.setDamage((int)(item.getMaxDamage() * (1 - damage / 100)));
                EditCommand.setItemStack(context.getSource(), item);
                return old;
            })
            .build();
        
        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<durability>]
        node.addChild(setNode);
        setNode.addChild(setDurabilityNode);

        // ... percent <durability>
        node.addChild(percentNode);
        percentNode.addChild(percentDurabilityNode);
    }
}
