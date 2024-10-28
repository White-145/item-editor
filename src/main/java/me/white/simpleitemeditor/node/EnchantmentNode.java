package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DynamicRegistryManager;
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

    private static RegistryEntry<Enchantment> entryOf(DynamicRegistryManager registryManager, Enchantment enchantment) {
        return registryManager.getOrThrow(RegistryKeys.ENCHANTMENT).getEntry(enchantment);
    }

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
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> enchantmentEntry : component.getEnchantmentEntries()) {
            enchantments.put(enchantmentEntry.getKey().value(), enchantmentEntry.getIntValue());
        }
        return enchantments;
    }

    private static void setEnchantments(DynamicRegistryManager registryManager, ItemStack stack, Map<Enchantment, Integer> enchantments) {
        if (enchantments == null || enchantments.isEmpty()) {
            stack.remove(DataComponentTypes.ENCHANTMENTS);
        } else {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                builder.set(entryOf(registryManager, entry.getKey()), entry.getValue());
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

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("enchantment").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            Map<Enchantment, Integer> enchantments = getEnchantments(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET));
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                EditorUtil.sendFeedback(context.getSource(), Text.empty().append(Text.literal("- ").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(Enchantment.getName(RegistryEntry.of(entry.getKey()), entry.getValue())));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> getEnchantmentNode = commandManager.argument("enchantment", RegistryArgumentType.registryEntry(RegistryKeys.ENCHANTMENT, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", RegistryKeys.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = getEnchantments(stack);
            if (!enchantments.containsKey(enchantment)) {
                throw NO_SUCH_ENCHANTMENTS_EXCEPTION;
            }

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_ENCHANTMENT, Enchantment.getName(RegistryEntry.of(enchantment), enchantments.get(enchantment))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setEnchantmentNode = commandManager.argument("enchantment", RegistryArgumentType.registryEntry(RegistryKeys.ENCHANTMENT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", RegistryKeys.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchantments(stack));
            if (enchantments.containsKey(enchantment) && enchantments.get(enchantment) == 1) {
                throw ALREADY_IS_EXCEPTION;
            }
            enchantments.put(enchantment, 1);
            setEnchantments(context.getSource().getRegistryManager(), stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, Enchantment.getName(RegistryEntry.of(enchantment), 1)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEnchantmentLevelNode = commandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", RegistryKeys.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchantments(stack));
            int level = IntegerArgumentType.getInteger(context, "level");
            if (enchantments.containsKey(enchantment) && enchantments.get(enchantment) == level) {
                throw ALREADY_IS_EXCEPTION;
            }
            enchantments.put(enchantment, level);
            setEnchantments(context.getSource().getRegistryManager(), stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, Enchantment.getName(RegistryEntry.of(enchantment), level)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").build();

        CommandNode<S> removeEnchantmentNode = commandManager.argument("enchantment", RegistryArgumentType.registryEntry(RegistryKeys.ENCHANTMENT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", RegistryKeys.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchantments(stack));
            if (!enchantments.containsKey(enchantment)) {
                throw NO_SUCH_ENCHANTMENTS_EXCEPTION;
            }
            enchantments.remove(enchantment);
            setEnchantments(context.getSource().getRegistryManager(), stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE, enchantment.description().copy()));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> glintNode = commandManager.literal("glint").build();

        CommandNode<S> glintGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasGlintOverride(stack)) {
                throw NO_GLINT_OVERRIDE_EXCEPTION;
            }
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(getGlintOverride(stack) ? OUTPUT_GLINT_GET_ENABLED : OUTPUT_GLINT_GET_DISABLED));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> glintSetNode = commandManager.literal("set").build();

        CommandNode<S> glintSetGlintNode = commandManager.argument("glint", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            boolean glint = BoolArgumentType.getBool(context, "glint");
            if (hasGlintOverride(stack) && glint == getGlintOverride(stack)) {
                throw GLINT_ALREADY_IS_EXCEPTION;
            }
            setGlint(stack, glint);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(glint ? OUTPUT_GLINT_ENABLE : OUTPUT_GLINT_DISABLE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> glintResetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasGlintOverride(stack)) {
                throw NO_GLINT_OVERRIDE_EXCEPTION;
            }
            removeGlintOverride(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GLINT_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            setEnchantments(context.getSource().getRegistryManager(), stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

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

        return node;
    }
}
