package me.white.simpleitemeditor.argument.enums;

import com.mojang.brigadier.context.CommandContext;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.node.EquipNode;

public class ExclusiveSlotArgumentType extends EnumArgumentType<EquipNode.ExclusiveSlot> {
    private ExclusiveSlotArgumentType() {
        super(EquipNode.ExclusiveSlot.class);
    }

    public static ExclusiveSlotArgumentType exclusiveSlot() {
        return new ExclusiveSlotArgumentType();
    }

    public static EquipNode.ExclusiveSlot getExclusiveSlot(CommandContext<?> context, String name) {
        return context.getArgument(name, EquipNode.ExclusiveSlot.class);
    }
}
