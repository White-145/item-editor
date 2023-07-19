package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class DurabilityNode {
	public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.cannotedit")).create();
	public static final CommandSyntaxException TOO_MUCH = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.toomuch")).create();
    private static final String OUTPUT_GET = "commands.edit.durability.get";
    private static final String OUTPUT_SET = "commands.edit.durability.set";
    private static final String OUTPUT_RESET = "commands.edit.durability.reset";
    private static final String OUTPUT_PERCENT = "commands.edit.durability.percent";

    private static void checkCanEdit(FabricClientCommandSource context) throws CommandSyntaxException {
        if (!ItemUtil.getItemStack(context).isDamageable()) throw CANNOT_EDIT_EXCEPTION;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("durability")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                int damage = item.getDamage();

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, damage, item.getMaxDamage(), (double)damage / item.getMaxDamage()));
                return damage;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                item.setDamage(0);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                ItemUtil.setItemStack(context.getSource(), item);
                return item.getMaxDamage();
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> setDurabilityNode = ClientCommandManager
            .argument("durability", IntegerArgumentType.integer())
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                int damage = IntegerArgumentType.getInteger(context, "durability");

                int fixed = damage < 0 ? -damage : item.getMaxDamage() - damage;
                if (fixed > item.getMaxDamage() || fixed < 0) throw TOO_MUCH;
                int old = item.getDamage();
                item.setDamage(fixed);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, fixed));
                ItemUtil.setItemStack(context.getSource(), item);
                return old;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> percentNode = ClientCommandManager
            .literal("percent")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Double> percentDurabilityNode = ClientCommandManager
            .argument("percentage", DoubleArgumentType.doubleArg(0, 100))
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                double percentage = DoubleArgumentType.getDouble(context, "percentage");

                int old = (int)((double)item.getDamage() / item.getMaxDamage() * 100);
                item.setDamage((int)(item.getMaxDamage() * (1 - percentage / 100)));

                context.getSource().sendFeedback(Text.translatable(OUTPUT_PERCENT, percentage));
                ItemUtil.setItemStack(context.getSource(), item);
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
