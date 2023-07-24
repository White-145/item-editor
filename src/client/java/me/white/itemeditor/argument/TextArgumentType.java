package me.white.itemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.text.Text;

public class TextArgumentType implements ArgumentType<Text> {
    @Override
    public Text parse(StringReader reader) throws CommandSyntaxException {
        throw new UnsupportedOperationException("Unimplemented method 'parse'");
    }
}
