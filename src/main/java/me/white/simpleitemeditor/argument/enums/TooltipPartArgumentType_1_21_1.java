package me.white.simpleitemeditor.argument.enums;

//? if <1.21.6 {
/*import com.mojang.brigadier.context.CommandContext;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.node.tooltip.TooltipNode_1_21_1;

public class TooltipPartArgumentType_1_21_1 extends EnumArgumentType<TooltipNode_1_21_1.TooltipPart> {
    private TooltipPartArgumentType_1_21_1() {
        super(TooltipNode_1_21_1.TooltipPart.class);
    }

    public static TooltipPartArgumentType_1_21_1 tooltipPart() {
        return new TooltipPartArgumentType_1_21_1();
    }

    public static TooltipNode_1_21_1.TooltipPart getTooltipPart(CommandContext<?> context, String name) {
        return context.getArgument(name, TooltipNode_1_21_1.TooltipPart.class);
    }
}
*///?}

