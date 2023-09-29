package me.white.simpleitemeditor.node.entity;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.node.EntityNode;
import me.white.simpleitemeditor.node.Node;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class AirNode implements Node {
    public static final CommandSyntaxException NO_AIR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.air.error.noair")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.air.error.alreadyis")).create();
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.entity.air.error.cannotedit")).create();
    private static final String OUTPUT_GET = "commands.edit.entity.air.get";
    private static final String OUTPUT_RESET = "commands.edit.entity.air.reset";
    private static final String OUTPUT_SET = "commands.edit.entity.air.set";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("air")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityAir(stack)) throw NO_AIR_EXCEPTION;
                    int air = ItemUtil.getEntityAir(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, air));
                    return air;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasEntityAir(stack)) throw NO_AIR_EXCEPTION;
                    ItemUtil.setEntityAir(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setAirNode = ClientCommandManager
                .argument("air", IntegerArgumentType.integer(0))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EntityNode.canEdit(stack)) throw EntityNode.CANNOT_EDIT_EXCEPTION;
                    int air = IntegerArgumentType.getInteger(context, "air");
                    if (ItemUtil.hasEntityAir(stack) && ItemUtil.getEntityAir(stack) == air) throw ALREADY_IS_EXCEPTION;
                    ItemUtil.setEntityAir(stack, air);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, air));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<air>]
        node.addChild(setNode);
        setNode.addChild(setAirNode);
    }
}
