package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class UnbreakableNode implements Node {
    public static final CommandSyntaxException ISNT_DAMAGABLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.unbreakable.error.isntdamagable")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.unbreakable.error.alreadyis")).create();
    private static final String OUTPUT_GET_ENABLED = "commands.edit.unbreakable.getenabled";
    private static final String OUTPUT_GET_DISABLED = "commands.edit.unbreakable.getdisabled";
    private static final String OUTPUT_ENABLE = "commands.edit.unbreakable.enable";
    private static final String OUTPUT_DISABLE = "commands.edit.unbreakable.disable";

    private static boolean isDamagable(ItemStack stack) {
        return stack.getMaxDamage() != 0;
    }

    private static boolean isUnbreakable(ItemStack stack) {
        return stack.contains(DataComponentTypes.UNBREAKABLE);
    }

    private static void setUnbreakable(ItemStack stack, boolean isUnbreakable) {
        if (isUnbreakable) {
            stack.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
        } else {
            stack.remove(DataComponentTypes.UNBREAKABLE);
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("unbreakable").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGABLE_EXCEPTION;
            }
            boolean isUnbreakable = isUnbreakable(stack);

            context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> setUnbreakableNode = ClientCommandManager.argument("unbreakable", BoolArgumentType.bool()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isDamagable(stack)) {
                throw ISNT_DAMAGABLE_EXCEPTION;
            }
            boolean isUnbreakable = BoolArgumentType.getBool(context, "unbreakable");
            if (isUnbreakable == isUnbreakable(stack)) {
                throw ALREADY_IS_EXCEPTION;
            }
            setUnbreakable(stack, isUnbreakable);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_ENABLE : OUTPUT_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set <unbreakable>
        node.addChild(setNode);
        setNode.addChild(setUnbreakableNode);
    }
}
