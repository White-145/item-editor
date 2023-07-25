package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class DurabilityNode {
	public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.durability.error.cannotedit")).create();
    private static final String OUTPUT_GET = "commands.edit.durability.get";
    private static final String OUTPUT_SET = "commands.edit.durability.set";
    private static final String OUTPUT_RESET = "commands.edit.durability.reset";
    private static final String OUTPUT_PERCENT = "commands.edit.durability.percent";

    private static boolean canEdit(ItemStack stack) {
        return stack.isDamageable();
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("durability")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack stack = EditorUtil.getStack(context.getSource());
                if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                int damage = stack.getDamage();

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, damage, stack.getMaxDamage(), (1 - (double)damage / stack.getMaxDamage()) * 100));
                return damage;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                stack.setDamage(0);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                EditorUtil.setStack(context.getSource(), stack);
                return stack.getMaxDamage();
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> setDurabilityNode = ClientCommandManager
            .argument("durability", IntegerArgumentType.integer())
            .executes(context -> {
                ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                int damage = IntegerArgumentType.getInteger(context, "durability");
                stack.setDamage(stack.getMaxDamage() - damage);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, damage));
                EditorUtil.setStack(context.getSource(), stack);
                return stack.getMaxDamage() - damage;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> percentNode = ClientCommandManager
            .literal("percent")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Double> percentDurabilityNode = ClientCommandManager
            .argument("percentage", DoubleArgumentType.doubleArg(0, 100))
            .executes(context -> {
                ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                double percentage = DoubleArgumentType.getDouble(context, "percentage");

                int old = (int)((double)stack.getDamage() / stack.getMaxDamage() * 100);
                stack.setDamage((int)(stack.getMaxDamage() * (1 - percentage / 100)));

                context.getSource().sendFeedback(Text.translatable(OUTPUT_PERCENT, percentage));
                EditorUtil.setStack(context.getSource(), stack);
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
