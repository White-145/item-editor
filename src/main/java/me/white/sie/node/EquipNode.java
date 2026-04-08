package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.argument.EnumArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class EquipNode implements Node {
    private static final String OUTPUT = "commands.edit.equip";

    private Inventory getInventory(SharedSuggestionProvider source) {
        if (EditorUtil.isClientSource(source)) {
            return ((FabricClientCommandSource)source).getPlayer().getInventory();
        }
        if (source instanceof CommandSourceStack) {
            return ((CommandSourceStack)source).getPlayer().getInventory();
        }
        throw EditorUtil.UNKNOWN_SOURCE_EXCEPTION.apply(source);
    }

    private void equip(SharedSuggestionProvider source, ItemStack stack, ExclusiveSlot slot) throws CommandSyntaxException {
        Inventory inventory = getInventory(source);
        ItemStack equippedStack = slot == ExclusiveSlot.OFFHAND ? inventory.getItem(Inventory.SLOT_OFFHAND).copy() : inventory.getItem(slot.mainSlot).copy();
        EditorUtil.setStack(source, equippedStack);
        inventory.setItem(slot.mainSlot, stack);
        if (EditorUtil.isClientSource(source)) {
            ((FabricClientCommandSource)source).getClient().getConnection().send(new ServerboundSetCreativeModeSlotPacket(slot.packetSlot, stack));
        }
        // should it send packets to client if serverside? we'll never know...
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("equip").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            equip(context.getSource(), stack, ExclusiveSlot.HEAD);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> slotNode = commandManager.argument("slot", EnumArgumentType.enums(ExclusiveSlot.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            ExclusiveSlot slot = context.getArgument("slot", ExclusiveSlot.class);
            equip(context.getSource(), stack, slot);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... [<slot>]
        node.addChild(slotNode);

        return node;
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
