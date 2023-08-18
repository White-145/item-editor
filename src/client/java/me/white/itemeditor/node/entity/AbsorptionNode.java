package me.white.itemeditor.node.entity;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.itemeditor.node.EntityNode;
import me.white.itemeditor.node.Node;
import me.white.itemeditor.util.EditorUtil;
import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class AbsorptionNode implements Node {
    public static final CommandSyntaxException NO_ABSORPTION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.absorption.error.noabsorption")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.absorption.error.alreadyis")).create();
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.absorption.error.cannotedit")).create();
    private static final String OUTPUT_GET = "commands.edit.entity.absorption.get";
    private static final String OUTPUT_RESET = "commands.edit.entity.absorption.reset";
    private static final String OUTPUT_SET = "commands.edit.entity.absorption.set";

    private static boolean canEdit(ItemStack stack) {
        return EditorUtil.isType(LivingEntity.class, stack);
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("absorption")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityAbsorption(stack)) throw NO_ABSORPTION_EXCEPTION;
                    float absorption = ItemUtil.getEntityAbsorption(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, String.format("%.2f", absorption)));
                    return (int) absorption;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityAbsorption(stack)) throw NO_ABSORPTION_EXCEPTION;
                    ItemUtil.setEntityAbsorption(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Float> setAbsorptionNode = ClientCommandManager
                .argument("absorption", FloatArgumentType.floatArg(0))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    float absorption = FloatArgumentType.getFloat(context, "absorption");
                    if (ItemUtil.hasEntityAbsorption(stack) && ItemUtil.getEntityAbsorption(stack) == absorption) throw ALREADY_IS_EXCEPTION;
                    ItemUtil.setEntityAbsorption(stack, absorption);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, String.format("%.2f", absorption)));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<absorption>]
        node.addChild(setNode);
        setNode.addChild(setAbsorptionNode);
    }
}
