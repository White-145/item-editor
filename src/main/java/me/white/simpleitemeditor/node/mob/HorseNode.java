package me.white.simpleitemeditor.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.EnumArgumentType;
//? if <1.21.5 {
/*import me.white.simpleitemeditor.node.DataNode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.entity.passive.HorseMarking;
*///?}
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.HorseColor;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class HorseNode implements Node {
    private static final CommandSyntaxException ISNT_HORSE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.horse.error.isnthorse")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.horse.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.horse.error.novariant")).create();
    private static final String OUTPUT_GET_COLOR = "commands.edit.mob.horse.colorget";
    private static final String OUTPUT_SET_COLOR = "commands.edit.mob.horse.colorset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.horse.variantremove";
    private static final String OUTPUT_GET_MARKING = "commands.edit.mob.horse.markingget";
    private static final String OUTPUT_SET_MARKING = "commands.edit.mob.horse.markingset";
    private static final String COLOR_WHITE = "variant.minecraft.horse.color.white";
    private static final String COLOR_CREAMY = "variant.minecraft.horse.color.creamy";
    private static final String COLOR_CHESTNUT = "variant.minecraft.horse.color.chestnut";
    private static final String COLOR_BROWN = "variant.minecraft.horse.color.brown";
    private static final String COLOR_BLACK = "variant.minecraft.horse.color.black";
    private static final String COLOR_GRAY = "variant.minecraft.horse.color.gray";
    private static final String COLOR_DARK_BROWN = "variant.minecraft.horse.color.darkbrown";
    //? if <1.21.5 {
    /*private static final String MARKING_NONE = "variant.minecraft.horse.marking.none";
    private static final String MARKING_WHITE = "variant.minecraft.horse.marking.white";
    private static final String MARKING_WHITE_FIELD = "variant.minecraft.horse.marking.whitefields";
    private static final String MARKING_WHITE_DOTS = "variant.minecraft.horse.marking.whitedots";
    private static final String MARKING_BLACK_DOTS = "variant.minecraft.horse.marking.blackdots";
    private static final String VARIANT_KEY = "Variant";
    *///?}

    private static Text translation(HorseColor color) {
        return switch (color) {
            case WHITE -> Text.translatable(COLOR_WHITE);
            case CREAMY -> Text.translatable(COLOR_CREAMY);
            case CHESTNUT -> Text.translatable(COLOR_CHESTNUT);
            case BROWN -> Text.translatable(COLOR_BROWN);
            case BLACK -> Text.translatable(COLOR_BLACK);
            case GRAY -> Text.translatable(COLOR_GRAY);
            case DARK_BROWN -> Text.translatable(COLOR_DARK_BROWN);
        };
    }

    //? if <1.21.5 {
    /*private static Text translation(HorseMarking marking) {
        return switch (marking) {
            case NONE -> Text.translatable(MARKING_NONE);
            case WHITE -> Text.translatable(MARKING_WHITE);
            case WHITE_FIELD -> Text.translatable(MARKING_WHITE_FIELD);
            case WHITE_DOTS -> Text.translatable(MARKING_WHITE_DOTS);
            case BLACK_DOTS -> Text.translatable(MARKING_BLACK_DOTS);
        };
    }
    *///?}

    private static boolean isHorse(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityType.HORSE;
    }

    private static boolean hasColor(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.contains(DataComponentTypes.HORSE_VARIANT);
        //?} else {
        /*if (!stack.contains(DataComponentTypes.ENTITY_DATA)) {
            return false;
        }
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.INT_TYPE)) {
            return false;
        }
        int id = nbt.getInt(VARIANT_KEY) & 0xFF;
        for (HorseColor color : HorseColor.values()) {
            if (color.getId() == id) {
                return true;
            }
        }
        return false;
        *///?}
    }

    private static boolean hasMarking(ItemStack stack) {
        //? if >=1.21.5 {
        return false;
        //?} else {
        /*if (!stack.contains(DataComponentTypes.ENTITY_DATA)) {
            return false;
        }
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.INT_TYPE)) {
            return false;
        }
        int id = nbt.getInt(VARIANT_KEY) >> 8;
        for (HorseMarking marking : HorseMarking.values()) {
            if (marking.getId() == id) {
                return true;
            }
        }
        return false;
        *///?}
    }

    private static HorseColor getColor(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.get(DataComponentTypes.HORSE_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        int id = nbt.getInt(VARIANT_KEY) & 0xFF;
        for (HorseColor color : HorseColor.values()) {
            if (color.getId() == id) {
                return color;
            }
        }
        return null;
        *///?}
    }

    private static void setColor(ItemStack stack, HorseColor color) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.HORSE_VARIANT, color);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        int marking = nbt.getInt(VARIANT_KEY) >> 8;
        nbt.putInt(VARIANT_KEY, (marking << 8) | color.getId());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeVariant(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.HORSE_VARIANT);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(VARIANT_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    //? if <1.21.5 {
    /*private static HorseMarking getMarking(ItemStack stack) {
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        int id = nbt.getInt(VARIANT_KEY) >> 8;
        for (HorseMarking marking : HorseMarking.values()) {
            if (marking.getId() == id) {
                return marking;
            }
        }
        return null;
    }

    private static void setMarking(ItemStack stack, HorseMarking marking) {
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        int variant = nbt.getInt(VARIANT_KEY);
        nbt.putInt(VARIANT_KEY, variant & 0xFF | (marking.getId() << 8));
        DataNode.DataSource.ENTITY.set(stack, nbt);
    }
    *///?}

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("horse").build();

        CommandNode<S> colorNode = commandManager.literal("color").build();

        CommandNode<S> colorGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isHorse(stack)) {
                throw ISNT_HORSE_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            HorseColor color = getColor(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_COLOR, translation(color)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorSetNode = commandManager.literal("set").build();

        CommandNode<S> colorSetVariantNode = commandManager.argument("color", EnumArgumentType.enums(HorseColor.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isHorse(stack)) {
                throw ISNT_HORSE_EXCEPTION;
            }
            HorseColor color = context.getArgument("color", HorseColor.class);
            if (hasColor(stack)) {
                HorseColor oldVariant = getColor(stack);
                if (color == oldVariant) {
                    throw VARIANT_ALREADY_IS_EXCEPTION;
                }
            }
            setColor(stack, color);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_COLOR, translation(color)));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isHorse(stack)) {
                throw ISNT_HORSE_EXCEPTION;
            }
            if (!hasColor(stack) && !hasMarking(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            removeVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE_VARIANT));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        //? if <1.21.5 {
        /*CommandNode<S> markingNode = commandManager.literal("marking").build();

        CommandNode<S> markingGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isHorse(stack)) {
                throw ISNT_HORSE_EXCEPTION;
            }
            if (!hasMarking(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            HorseMarking marking = getMarking(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_MARKING, translation(marking)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> markingSetNode = commandManager.literal("set").build();

        CommandNode<S> markingSetMarkingNode = commandManager.argument("marking", EnumArgumentType.enums(HorseMarking.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isHorse(stack)) {
                throw ISNT_HORSE_EXCEPTION;
            }
            HorseMarking marking = context.getArgument("marking", HorseMarking.class);
            if (hasMarking(stack)) {
                HorseMarking oldMarking = getMarking(stack);
                if (marking == oldMarking) {
                    throw VARIANT_ALREADY_IS_EXCEPTION;
                }
            }
            setMarking(stack, marking);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_MARKING, translation(marking)));
            return Command.SINGLE_SUCCESS;
        }).build();
        *///?}

        // ... color
        node.addChild(colorNode);
        // ... get
        colorNode.addChild(colorGetNode);
        // ... set <color>
        colorNode.addChild(colorSetNode);
        colorSetNode.addChild(colorSetVariantNode);
        // ... remove
        colorNode.addChild(colorRemoveNode);

        //? if <1.21.5 {
        /*// ... marking
        node.addChild(markingNode);
        // ... get
        markingNode.addChild(markingGetNode);
        // ... set <marking>
        markingNode.addChild(markingSetNode);
        markingSetNode.addChild(markingSetMarkingNode);
        *///?}

        return node;
    }
}
