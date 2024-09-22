package me.white.simpleitemeditor.argument.enums;

import com.mojang.brigadier.context.CommandContext;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import net.minecraft.component.type.AttributeModifierSlot;

public class AttributeSlotArgumentType extends EnumArgumentType<AttributeModifierSlot> {
    private AttributeSlotArgumentType() {
        super(AttributeModifierSlot.class);
    }

    public static AttributeSlotArgumentType attributeSlot() {
        return new AttributeSlotArgumentType();
    }

    public static AttributeModifierSlot getSlot(CommandContext<?> context, String name) {
        return context.getArgument(name, AttributeModifierSlot.class);
    }
}
