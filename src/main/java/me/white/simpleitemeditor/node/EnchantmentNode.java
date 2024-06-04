package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentNode implements Node {
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.alreadyis")).create();
    private static final CommandSyntaxException NO_SUCH_ENCHANTMENTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.nosuchenchantments")).create();
    private static final CommandSyntaxException NO_ENCHANTMENTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.noenchantments")).create();
    private static final CommandSyntaxException NO_GLINT_OVERRIDE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.noglintoverride")).create();
    private static final CommandSyntaxException GLINT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.glintalreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.enchantment.get";
    private static final String OUTPUT_GET_ENCHANTMENT = "commands.edit.enchantment.getenchantment";
    private static final String OUTPUT_SET = "commands.edit.enchantment.set";
    private static final String OUTPUT_REMOVE = "commands.edit.enchantment.remove";
    private static final String OUTPUT_GLINT_GET_ENABLED = "commands.edit.enchantment.getglintenabled";
    private static final String OUTPUT_GLINT_GET_DISABLED = "commands.edit.enchantment.getglintdisabled";
    private static final String OUTPUT_GLINT_ENABLE = "commands.edit.enchantment.glintenable";
    private static final String OUTPUT_GLINT_DISABLE = "commands.edit.enchantment.glintdisable";
    private static final String OUTPUT_GLINT_RESET = "commands.edit.enchantment.glintreset";
    private static final String OUTPUT_CLEAR = "commands.edit.enchantment.clear";

    private static boolean hasEnchantments(ItemStack stack) {
        if (!stack.contains(DataComponentTypes.ENCHANTMENTS)) {
            return false;
        }
        return !stack.get(DataComponentTypes.ENCHANTMENTS).isEmpty();
    }

    private static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        if (!hasEnchantments(stack)) {
            return Map.of();
        }
        ItemEnchantmentsComponent component = stack.get(DataComponentTypes.ENCHANTMENTS);
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> enchantmentEntry : component.getEnchantmentsMap()) {
            enchantments.put(enchantmentEntry.getKey().value(), enchantmentEntry.getIntValue());
        }
        return enchantments;
    }

    private static void setEnchantments(ItemStack stack, Map<Enchantment, Integer> enchantments) {
        if (enchantments == null || enchantments.isEmpty()) {
            stack.remove(DataComponentTypes.ENCHANTMENTS);
        } else {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                builder.set(entry.getKey(), entry.getValue());
            }
            stack.set(DataComponentTypes.ENCHANTMENTS, builder.build().withShowInTooltip(TooltipNode.TooltipPart.ENCHANTMENT.get(stack)));
        }
    }

    private static boolean hasGlintOverride(ItemStack stack) {
        return stack.contains(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private static boolean getGlintOverride(ItemStack stack) {
        if (!hasGlintOverride(stack)) {
            return false;
        }
        return stack.get(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private static void setGlint(ItemStack stack, boolean glint) {
        stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);
    }

    private static void removeGlintOverride(ItemStack stack) {
        stack.remove(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("enchantment").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            Map<Enchantment, Integer> enchantments = getEnchantments(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                context.getSource().sendFeedback(Text.empty().append(Text.literal("- ").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(entry.getKey().getName(entry.getValue())));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<Enchantment>> getEnchantmentNode = ClientCommandManager.argument("enchantment", RegistryArgumentType.registryEntry(RegistryKeys.ENCHANTMENT, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", RegistryKeys.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = getEnchantments(stack);
            if (!enchantments.containsKey(enchantment)) {
                throw NO_SUCH_ENCHANTMENTS_EXCEPTION;
            }

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_ENCHANTMENT, enchantment.getName(enchantments.get(enchantment))));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<Enchantment>> setEnchantmentNode = ClientCommandManager.argument("enchantment", RegistryArgumentType.registryEntry(RegistryKeys.ENCHANTMENT, registryAccess)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", RegistryKeys.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchantments(stack));
            if (enchantments.containsKey(enchantment) && enchantments.get(enchantment) == 1) {
                throw ALREADY_IS_EXCEPTION;
            }
            enchantments.put(enchantment, 1);
            setEnchantments(stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, enchantment.getName(1)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setEnchantmentLevelNode = ClientCommandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", RegistryKeys.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchantments(stack));
            int level = IntegerArgumentType.getInteger(context, "level");
            if (enchantments.containsKey(enchantment) && enchantments.get(enchantment) == level) {
                throw ALREADY_IS_EXCEPTION;
            }
            enchantments.put(enchantment, level);
            setEnchantments(stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, enchantment.getName(level)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<Enchantment>> removeEnchantmentNode = ClientCommandManager.argument("enchantment", RegistryArgumentType.registryEntry(RegistryKeys.ENCHANTMENT, registryAccess)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", RegistryKeys.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchantments(stack));
            if (!enchantments.containsKey(enchantment)) {
                throw NO_SUCH_ENCHANTMENTS_EXCEPTION;
            }
            enchantments.remove(enchantment);
            setEnchantments(stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, Text.translatable(enchantment.getTranslationKey())));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> glintNode = ClientCommandManager.literal("glint").build();

        LiteralCommandNode<FabricClientCommandSource> glintGetNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasGlintOverride(stack)) {
                throw NO_GLINT_OVERRIDE_EXCEPTION;
            }
            context.getSource().sendFeedback(Text.translatable(getGlintOverride(stack) ? OUTPUT_GLINT_GET_ENABLED : OUTPUT_GLINT_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> glintSetNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> glintSetGlintNode = ClientCommandManager.argument("glint", BoolArgumentType.bool()).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            boolean glint = BoolArgumentType.getBool(context, "glint");
            if (hasGlintOverride(stack) && glint == getGlintOverride(stack)) {
                throw GLINT_ALREADY_IS_EXCEPTION;
            }
            setGlint(stack, glint);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(glint ? OUTPUT_GLINT_ENABLE : OUTPUT_GLINT_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> glintResetNode = ClientCommandManager.literal("reset").executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasGlintOverride(stack)) {
                throw NO_GLINT_OVERRIDE_EXCEPTION;
            }
            removeGlintOverride(stack);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_GLINT_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager.literal("clear").executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            setEnchantments(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get [<enchantment>]
        node.addChild(getNode);
        getNode.addChild(getEnchantmentNode);

        // ... set <enchantment> [<level>]
        node.addChild(setNode);
        setNode.addChild(setEnchantmentNode);
        setEnchantmentNode.addChild(setEnchantmentLevelNode);

        // ... remove <enchantment>
        node.addChild(removeNode);
        removeNode.addChild(removeEnchantmentNode);

        // ... glint ...
        node.addChild(glintNode);
        // ... get
        glintNode.addChild(glintGetNode);
        // ... set <glint>
        glintNode.addChild(glintSetNode);
        glintSetNode.addChild(glintSetGlintNode);
        // ... reset
        glintNode.addChild(glintResetNode);

        // ... clear
        node.addChild(clearNode);
    }
}
