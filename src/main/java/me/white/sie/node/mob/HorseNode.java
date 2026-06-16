package me.white.sie.node.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.argument.EnumArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.util.EditorUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
//?if >= 26.2 {
import net.minecraft.world.entity.EntityTypes;
//?} else {
/*import net.minecraft.world.entity.EntityType;
*///?}
import net.minecraft.world.entity.animal.equine.Variant;
import net.minecraft.world.item.ItemStack;

public class HorseNode implements Node {
    private static final CommandSyntaxException ISNT_HORSE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.horse.error.isnthorse")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.horse.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.horse.error.novariant")).create();
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

    private static Component translation(Variant color) {
        return switch (color) {
            case WHITE -> Component.translatable(COLOR_WHITE);
            case CREAMY -> Component.translatable(COLOR_CREAMY);
            case CHESTNUT -> Component.translatable(COLOR_CHESTNUT);
            case BROWN -> Component.translatable(COLOR_BROWN);
            case BLACK -> Component.translatable(COLOR_BLACK);
            case GRAY -> Component.translatable(COLOR_GRAY);
            case DARK_BROWN -> Component.translatable(COLOR_DARK_BROWN);
        };
    }

    private static boolean isHorse(ItemStack stack) {
        //?if >=26.2 {
        return EditorUtil.getEntityType(stack) == EntityTypes.HORSE;
        //?} else {
        /*return EditorUtil.getEntityType(stack) == EntityType.HORSE;
        *///?}
    }

    private static boolean hasColor(ItemStack stack) {
        return stack.has(DataComponents.HORSE_VARIANT);
    }

    private static Variant getColor(ItemStack stack) {
        return stack.get(DataComponents.HORSE_VARIANT);
    }

    private static void setColor(ItemStack stack, Variant color) {
        stack.set(DataComponents.HORSE_VARIANT, color);
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponents.HORSE_VARIANT);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
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
            Variant color = getColor(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_COLOR, translation(color)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorSetNode = commandManager.literal("set").build();

        CommandNode<S> colorSetVariantNode = commandManager.argument("color", EnumArgumentType.enums(Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isHorse(stack)) {
                throw ISNT_HORSE_EXCEPTION;
            }
            Variant color = context.getArgument("color", Variant.class);
            if (hasColor(stack)) {
                Variant oldVariant = getColor(stack);
                if (color == oldVariant) {
                    throw VARIANT_ALREADY_IS_EXCEPTION;
                }
            }
            setColor(stack, color);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_COLOR, translation(color)));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isHorse(stack)) {
                throw ISNT_HORSE_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            removeVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE_VARIANT));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... color
        node.addChild(colorNode);
        // ... get
        colorNode.addChild(colorGetNode);
        // ... set <color>
        colorNode.addChild(colorSetNode);
        colorSetNode.addChild(colorSetVariantNode);
        // ... remove
        colorNode.addChild(colorRemoveNode);

        return node;
    }
}
