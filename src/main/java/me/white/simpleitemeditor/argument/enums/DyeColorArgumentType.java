package me.white.simpleitemeditor.argument.enums;

import com.mojang.brigadier.context.CommandContext;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import net.minecraft.util.DyeColor;

public class DyeColorArgumentType extends EnumArgumentType<DyeColor> {
    private DyeColorArgumentType() {
        super(DyeColor.class);
    }

    public static DyeColorArgumentType dyeColor() {
        return new DyeColorArgumentType();
    }

    public static DyeColor getDyeColor(CommandContext<?> context, String name) {
        return context.getArgument(name, DyeColor.class);
    }
}
