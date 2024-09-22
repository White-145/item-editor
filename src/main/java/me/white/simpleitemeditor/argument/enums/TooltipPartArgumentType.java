package me.white.simpleitemeditor.argument.enums;

import com.mojang.brigadier.context.CommandContext;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.node.TooltipNode;

public class TooltipPartArgumentType extends EnumArgumentType<TooltipNode.TooltipPart> {
    private TooltipPartArgumentType() {
        super(TooltipNode.TooltipPart.class);
    }

    public static TooltipPartArgumentType tooltipPart() {
        return new TooltipPartArgumentType();
    }

    public static TooltipNode.TooltipPart getTooltipPart(CommandContext<?> context, String name) {
        return context.getArgument(name, TooltipNode.TooltipPart.class);
    }
}
