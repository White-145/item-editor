package me.white.simpleitemeditor.argument.enums;

import com.mojang.brigadier.context.CommandContext;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import net.minecraft.entity.attribute.EntityAttributeModifier;

public class AttributeOperationArgumentType extends EnumArgumentType<EntityAttributeModifier.Operation> {
    private AttributeOperationArgumentType() {
        super(EntityAttributeModifier.Operation.class, AttributeOperationArgumentType::formatter);
    }

    public static AttributeOperationArgumentType attributeOperation() {
        return new AttributeOperationArgumentType();
    }

    public static EntityAttributeModifier.Operation getAttributeOperation(CommandContext<?> context, String name) {
        return context.getArgument(name, EntityAttributeModifier.Operation.class);
    }

    private static String formatter(EntityAttributeModifier.Operation operation) {
        return switch (operation) {
            case ADD_VALUE -> "add";
            case ADD_MULTIPLIED_BASE -> "mult_base";
            case ADD_MULTIPLIED_TOTAL -> "mult_total";
        };
    }
}
