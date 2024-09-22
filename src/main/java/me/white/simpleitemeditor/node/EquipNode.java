package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.enums.ExclusiveSlotArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class EquipNode implements Node {
    private static final String OUTPUT = "commands.edit.equip";

    private PlayerInventory getInventory(CommandSource source) {
        if (source instanceof FabricClientCommandSource) {
            return ((FabricClientCommandSource)source).getPlayer().getInventory();
        }
        if (source instanceof ServerCommandSource) {
            return ((ServerCommandSource)source).getPlayer().getInventory();
        }
        throw EditorUtil.UNKNOWN_SOURCE_EXCEPTION.apply(source);
    }

    private void equip(CommandSource source, ItemStack stack, ExclusiveSlot slot) throws CommandSyntaxException {
        PlayerInventory inventory = getInventory(source);
        ItemStack equippedStack = slot == ExclusiveSlot.OFFHAND ? inventory.offHand.get(0).copy() : inventory.getArmorStack(slot.armorSlot).copy();
        EditorUtil.setStack(source, equippedStack);
        inventory.setStack(slot.mainSlot, stack);
        if (source instanceof FabricClientCommandSource) {
            ((FabricClientCommandSource)source).getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(slot.packetSlot, stack));
        }
        // should send to client if serverside?
    }

    @Override
    public void register(CommonCommandManager<CommandSource> commandManager, CommandNode<CommandSource> rootNode, CommandRegistryAccess registryAccess) {
        CommandNode<CommandSource> node = commandManager.literal("equip").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            equip(context.getSource(), stack, ExclusiveSlot.HEAD);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<CommandSource> slotNode = commandManager.argument("slot", ExclusiveSlotArgumentType.exclusiveSlot()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            ExclusiveSlot slot = ExclusiveSlotArgumentType.getExclusiveSlot(context, "slot");
            equip(context.getSource(), stack, slot);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... [<slot>]
        node.addChild(slotNode);
    }

    public enum ExclusiveSlot {
        OFFHAND(-1, 40, 45),
        HEAD(3, 39, 5),
        CHEST(2, 38, 6),
        LEGS(1, 37, 7),
        FEET(0, 36, 8);

        final int armorSlot;
        final int mainSlot;
        final int packetSlot;

        ExclusiveSlot(int armorSlot, int mainSlot, int packetSlot) {
            this.armorSlot = armorSlot;
            this.mainSlot = mainSlot;
            this.packetSlot = packetSlot;
        }
    }
}
