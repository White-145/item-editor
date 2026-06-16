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
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.ItemStack;

public class AxolotlNode implements Node {
    private static final CommandSyntaxException ISNT_AXOLOTL_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.axolotl.error.isntaxolotl")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.axolotl.error.variantalreadyis")).create();
    private static final CommandSyntaxException NO_VARIANT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.mob.axolotl.error.novariant")).create();
    private static final String OUTPUT_GET_VARIANT = "commands.edit.mob.axolotl.variantget";
    private static final String OUTPUT_SET_VARIANT = "commands.edit.mob.axolotl.variantset";
    private static final String OUTPUT_REMOVE_VARIANT = "commands.edit.mob.axolotl.variantremove";
    private static final String VARIANT_BLUE = "variant.minecraft.axolotl.blue";
    private static final String VARIANT_LUCY = "variant.minecraft.axolotl.lucy";
    private static final String VARIANT_WILD = "variant.minecraft.axolotl.wild";
    private static final String VARIANT_GOLD = "variant.minecraft.axolotl.gold";
    private static final String VARIANT_CYAN = "variant.minecraft.axolotl.cyan";

    private static Component translation(Axolotl.Variant variant) {
        return switch (variant) {
            case BLUE -> Component.translatable(VARIANT_BLUE);
            case LUCY -> Component.translatable(VARIANT_LUCY);
            case WILD -> Component.translatable(VARIANT_WILD);
            case GOLD -> Component.translatable(VARIANT_GOLD);
            case CYAN -> Component.translatable(VARIANT_CYAN);
        };
    }

    private static boolean isAxolotl(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityTypes.AXOLOTL;
    }

    private static boolean hasVariant(ItemStack stack) {
        return stack.has(DataComponents.AXOLOTL_VARIANT);
    }

    private static Axolotl.Variant getVariant(ItemStack stack) {
        return stack.get(DataComponents.AXOLOTL_VARIANT);
    }

    private static void setVariant(ItemStack stack, Axolotl.Variant variant) {
        stack.set(DataComponents.AXOLOTL_VARIANT, variant);
    }

    private static void removeVariant(ItemStack stack) {
        stack.remove(DataComponents.AXOLOTL_VARIANT);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("axolotl").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isAxolotl(stack)) {
                throw ISNT_AXOLOTL_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            Axolotl.Variant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(Axolotl.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isAxolotl(stack)) {
                throw ISNT_AXOLOTL_EXCEPTION;
            }
            Axolotl.Variant variant = context.getArgument("variant", Axolotl.Variant.class);
            if (hasVariant(stack)) {
                Axolotl.Variant oldVariant = getVariant(stack);
                if (variant == oldVariant) {
                    throw VARIANT_ALREADY_IS_EXCEPTION;
                }
            }
            setVariant(stack, variant);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_VARIANT, translation(variant)));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isAxolotl(stack)) {
                throw ISNT_AXOLOTL_EXCEPTION;
            }
            if (!hasVariant(stack)) {
                throw NO_VARIANT_EXCEPTION;
            }
            removeVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE_VARIANT));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... variant
        node.addChild(variantNode);
        // ... get
        variantNode.addChild(variantGetNode);
        // ... set <variant>
        variantNode.addChild(variantSetNode);
        variantSetNode.addChild(variantSetVariantNode);
        // ... remove
        variantNode.addChild(variantRemoveNode);

        return node;
    }
}
