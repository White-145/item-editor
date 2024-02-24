package me.white.simpleitemeditor.node;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;

public class EquipNode implements Node {
    private static final String OUTPUT = "commands.edit.equip";

    private enum ExclusiveSlot {
        OFFHAND(4, 40, 45),
        HEAD(3, 39, 5),
        CHEST(2, 38, 6),
        LEGS(1, 37, 7),
        FEET(0, 36, 8);

        final int i;
        final int mainSlot;
        final int packetSlot;

        ExclusiveSlot(int i, int mainSlot, int packetSlot) {
            this.i = i;
            this.mainSlot = mainSlot;
            this.packetSlot = packetSlot;
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("equip")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    ExclusiveSlot slot = ExclusiveSlot.HEAD;
                    PlayerInventory inventory = context.getSource().getPlayer().getInventory();
                    ItemStack equippedStack = inventory.getArmorStack(slot.i).copy();

                    EditorUtil.setStack(context.getSource(), equippedStack);
                    inventory.setStack(slot.mainSlot, stack);
                    context.getSource().getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(slot.packetSlot, stack));
                    context.getSource().sendFeedback(Text.translatable(OUTPUT));
                    return equippedStack.isEmpty() ? 0 : 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, ExclusiveSlot> slotNode = ClientCommandManager
                .argument("slot", EnumArgumentType.enumArgument(ExclusiveSlot.class))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    ExclusiveSlot slot = EnumArgumentType.getEnum(context, "slot", ExclusiveSlot.class);
                    PlayerInventory inventory = context.getSource().getPlayer().getInventory();
                    ItemStack equippedStack = slot == ExclusiveSlot.OFFHAND
                            ? inventory.offHand.get(0).copy()
                            : inventory.getArmorStack(slot.i).copy();

                    EditorUtil.setStack(context.getSource(), equippedStack);
                    inventory.setStack(slot.mainSlot, stack);
                    context.getSource().getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(slot.packetSlot, stack));
                    context.getSource().sendFeedback(Text.translatable(OUTPUT));
                    return equippedStack.isEmpty() ? 0 : 1;
                })
                .build();

        rootNode.addChild(node);

        // ... [<slot>]
        node.addChild(slotNode);
    }
}
