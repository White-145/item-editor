package me.white.simpleitemeditor.argument.enums;

import com.mojang.brigadier.context.CommandContext;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import net.minecraft.util.Rarity;

public class RarityArgumentType extends EnumArgumentType<Rarity> {
    private RarityArgumentType() {
        super(Rarity.class);
    }

    public static RarityArgumentType rarity() {
        return new RarityArgumentType();
    }

    public static Rarity getRarity(CommandContext<?> context, String name) {
        return context.getArgument(name, Rarity.class);
    }
}
