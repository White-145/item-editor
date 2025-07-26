package me.white.simpleitemeditor.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
//? if >=1.21.5 {
import net.minecraft.component.DataComponentTypes;
 //?} else {
/*import me.white.simpleitemeditor.node.DataNode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
*///?}
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public class ShulkerNode implements Node {
    private static final CommandSyntaxException ISNT_SHULKER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.shulker.error.isntshulker")).create();
    private static final CommandSyntaxException COLOR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.shulker.error.coloralreadyis")).create();
    private static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.shulker.error.nocolor")).create();
    private static final String OUTPUT_GET_COLOR = "commands.edit.mob.shulker.colorget";
    private static final String OUTPUT_SET_COLOR = "commands.edit.mob.shulker.colorset";
    private static final String OUTPUT_REMOVE_COLOR = "commands.edit.mob.shulker.colorremove";
    //? if <1.21.5 {
    /*private static final String COLOR_KEY = "Color";
    *///?}

    private static boolean isShulker(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityType.SHULKER;
    }

    private static boolean hasColor(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.contains(DataComponentTypes.SHULKER_COLOR);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(COLOR_KEY, NbtElement.BYTE_TYPE)) {
            return false;
        }
        int id = nbt.getByte(COLOR_KEY);
        for (DyeColor color : DyeColor.values()) {
            if (color.getId() == id) {
                return true;
            }
        }
        return false;
        *///?}
    }

    private static DyeColor getColor(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.get(DataComponentTypes.SHULKER_COLOR);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(COLOR_KEY, NbtElement.BYTE_TYPE)) {
            return null;
        }
        int id = nbt.getByte(COLOR_KEY);
        for (DyeColor color : DyeColor.values()) {
            if (color.getId() == id) {
                return color;
            }
        }
        return null;
        *///?}
    }

    private static void setColor(ItemStack stack, DyeColor color) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.SHULKER_COLOR, color);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.putByte(COLOR_KEY, (byte)color.getId());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeColor(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.SHULKER_COLOR);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(COLOR_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("shulker").build();

        CommandNode<S> colorNode = commandManager.literal("color").build();

        CommandNode<S> colorGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isShulker(stack)) {
                throw ISNT_SHULKER_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            DyeColor color = getColor(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_COLOR, EditorUtil.colorTranslation(color)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorSetNode = commandManager.literal("set").build();

        CommandNode<S> colorSetColorNode = commandManager.argument("color", EnumArgumentType.enums(DyeColor.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isShulker(stack)) {
                throw ISNT_SHULKER_EXCEPTION;
            }
            DyeColor color = context.getArgument("color", DyeColor.class);
            if (hasColor(stack)) {
                DyeColor oldColor = getColor(stack);
                if (color == oldColor) {
                    throw COLOR_ALREADY_IS_EXCEPTION;
                }
            }
            setColor(stack, color);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_COLOR, EditorUtil.colorTranslation(color)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isShulker(stack)) {
                throw ISNT_SHULKER_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            removeColor(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE_COLOR));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... color
        node.addChild(colorNode);
        // ... get
        colorNode.addChild(colorGetNode);
        // ... set <color>
        colorNode.addChild(colorSetNode);
        colorSetNode.addChild(colorSetColorNode);
        // ... remove
        colorNode.addChild(colorRemoveNode);

        return node;
    }
}