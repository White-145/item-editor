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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class AxolotlNode implements Node {
    private static final CommandSyntaxException ISNT_AXOLOTL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.axolotl.error.isntaxolotl")).create();
    private static final CommandSyntaxException VARIANT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.axolotl.error.variantalreadyis")).create();
    private static final String OUTPUT_GET_VARIANT = "command.edit.mob.axolotl.variantget";
    private static final String OUTPUT_SET_VARIANT = "command.edit.mob.axolotl.variantset";
    private static final String VARIANT_BLUE = "variant.minecraft.axolotl.blue";
    private static final String VARIANT_LUCY = "variant.minecraft.axolotl.lucy";
    private static final String VARIANT_WILD = "variant.minecraft.axolotl.wild";
    private static final String VARIANT_GOLD = "variant.minecraft.axolotl.gold";
    private static final String VARIANT_CYAN = "variant.minecraft.axolotl.cyan";

    private static Text translation(AxolotlEntity.Variant variant) {
        return switch (variant) {
            case BLUE -> Text.translatable(VARIANT_BLUE);
            case LUCY -> Text.translatable(VARIANT_LUCY);
            case WILD -> Text.translatable(VARIANT_WILD);
            case GOLD -> Text.translatable(VARIANT_GOLD);
            case CYAN -> Text.translatable(VARIANT_CYAN);
        };
    }

    private static boolean isAxolotl(ItemStack stack) {
        EntityType<?> entityType = EditorUtil.getEntityType(stack);
        return entityType.getBaseClass().equals(AxolotlEntity.class);
    }

    private static AxolotlEntity.Variant getVariant(ItemStack stack) {
        if (stack.contains(DataComponentTypes.AXOLOTL_VARIANT)) {
            return AxolotlEntity.Variant.DEFAULT;
        }
        return stack.get(DataComponentTypes.AXOLOTL_VARIANT);
    }

    private static void setVariant(ItemStack stack, AxolotlEntity.Variant variant) {
        stack.set(DataComponentTypes.AXOLOTL_VARIANT, variant);
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("axolotl").build();

        CommandNode<S> variantNode = commandManager.literal("variant").build();

        CommandNode<S> variantGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isAxolotl(stack)) {
                throw ISNT_AXOLOTL_EXCEPTION;
            }
            AxolotlEntity.Variant variant = getVariant(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_VARIANT, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> variantSetNode = commandManager.literal("set").build();

        CommandNode<S> variantSetVariantNode = commandManager.argument("variant", EnumArgumentType.enums(AxolotlEntity.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isAxolotl(stack)) {
                throw ISNT_AXOLOTL_EXCEPTION;
            }
            AxolotlEntity.Variant variant = context.getArgument("variant", AxolotlEntity.Variant.class);
            AxolotlEntity.Variant oldVariant = getVariant(stack);
            if (variant == oldVariant) {
                throw VARIANT_ALREADY_IS_EXCEPTION;
            }
            setVariant(stack, variant);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_VARIANT, translation(variant)));
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

        return node;
    }
}
