package me.white.simpleitemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class UnbreakableNode implements Node {
    public static final CommandSyntaxException ISNT_DAMAGABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.unbreakable.error.isntdamagable")).create();
    private static final String OUTPUT_GET_ENABLED = "commands.edit.unbreakable.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.unbreakable.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.unbreakable.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.unbreakable.disable";

    private static boolean isDamagable(ItemStack stack) {
        return stack.getMaxDamage() != 0;
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("unbreakable")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isDamagable(stack)) throw ISNT_DAMAGABLE_EXCEPTION;
                    boolean isUnbreakable = ItemUtil.getUnbreakable(stack);

                    context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
                    return isUnbreakable ? 1 : 0;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!isDamagable(stack)) throw ISNT_DAMAGABLE_EXCEPTION;
                    boolean isUnbreakable = ItemUtil.getUnbreakable(stack);
                    ItemUtil.setUnbreakable(stack, !isUnbreakable);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_DISABLE : OUTPUT_ENABLE));
                    return isUnbreakable ? 1 : 0;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... toggle
        node.addChild(toggleNode);
    }
}
