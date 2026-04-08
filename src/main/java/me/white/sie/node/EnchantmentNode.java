package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.argument.RegistryArgumentType;
import me.white.sie.util.EditorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.HashMap;
import java.util.Map;

public class EnchantmentNode implements Node {
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.enchantment.error.alreadyis")).create();
    private static final CommandSyntaxException NO_SUCH_ENCHANTMENTS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.enchantment.error.nosuchenchantments")).create();
    private static final CommandSyntaxException NO_ENCHANTMENTS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.enchantment.error.noenchantments")).create();
    private static final CommandSyntaxException NO_GLINT_OVERRIDE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.enchantment.error.noglintoverride")).create();
    private static final CommandSyntaxException GLINT_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.enchantment.error.glintalreadyis")).create();
    private static final CommandSyntaxException STORED_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.enchantment.error.storedalreadyis")).create();
    private static final CommandSyntaxException NO_SUCH_STORED_ENCHANTMENTS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.enchantment.error.nosuchstoredenchantments")).create();
    private static final CommandSyntaxException NO_STORED_ENCHANTMENTS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.enchantment.error.nostoredenchantments")).create();
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
    private static final String OUTPUT_GET_STORED = "commands.edit.enchantment.getstored";
    private static final String OUTPUT_GET_STORED_ENCHANTMENT = "commands.edit.enchantment.getstoredenchantment";
    private static final String OUTPUT_SET_STORED = "commands.edit.enchantment.setstored";
    private static final String OUTPUT_REMOVE_STORED = "commands.edit.enchantment.removestored";
    private static final String OUTPUT_CLEAR_STORED = "commands.edit.enchantment.clearstored";

    private static Holder<Enchantment> entryOf(Enchantment enchantment) {
        return EditorUtil.getRegistry(Registries.ENCHANTMENT).wrapAsHolder(enchantment);
    }

    private static boolean hasEnchantments(ItemStack stack) {
        if (!stack.has(DataComponents.ENCHANTMENTS)) {
            return false;
        }
        return !stack.get(DataComponents.ENCHANTMENTS).isEmpty();
    }

    private static Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        if (!hasEnchantments(stack)) {
            return Map.of();
        }
        ItemEnchantments component = stack.get(DataComponents.ENCHANTMENTS);
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for (Object2IntMap.Entry<Holder<Enchantment>> enchantmentEntry : component.entrySet()) {
            enchantments.put(enchantmentEntry.getKey().value(), enchantmentEntry.getIntValue());
        }
        return enchantments;
    }

    private static void setEnchantments(ItemStack stack, Map<Enchantment, Integer> enchantments) {
        if (enchantments == null) {
            enchantments = Map.of();
        }
        ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            builder.set(entryOf(entry.getKey()), entry.getValue());
        }
        ItemEnchantments component = builder.toImmutable();
        stack.set(DataComponents.ENCHANTMENTS, component);
    }

    private static boolean hasGlintOverride(ItemStack stack) {
        return stack.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private static boolean getGlintOverride(ItemStack stack) {
        if (!hasGlintOverride(stack)) {
            return false;
        }
        return stack.get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private static void setGlint(ItemStack stack, boolean glint) {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, glint);
    }

    private static void removeGlintOverride(ItemStack stack) {
        stack.remove(DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
    }

    private static boolean hasStoredEnchantments(ItemStack stack) {
        if (!stack.has(DataComponents.STORED_ENCHANTMENTS)) {
            return false;
        }
        return !stack.get(DataComponents.STORED_ENCHANTMENTS).isEmpty();
    }

    private static Map<Enchantment, Integer> getStoredEnchantments(ItemStack stack) {
        if (!hasStoredEnchantments(stack)) {
            return Map.of();
        }
        ItemEnchantments component = stack.get(DataComponents.STORED_ENCHANTMENTS);
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        for (Object2IntMap.Entry<Holder<Enchantment>> enchantmentEntry : component.entrySet()) {
            enchantments.put(enchantmentEntry.getKey().value(), enchantmentEntry.getIntValue());
        }
        return enchantments;
    }

    private static void setStoredEnchantments(RegistryAccess registryManager, ItemStack stack, Map<Enchantment, Integer> enchantments) {
        if (enchantments == null || enchantments.isEmpty()) {
            stack.remove(DataComponents.STORED_ENCHANTMENTS);
        } else {
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                builder.set(entryOf(entry.getKey()), entry.getValue());
            }
            stack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        }
    }

    private static Component getEnchantmentName(Enchantment enchantment, int level) {
        return Enchantment.getFullname(Holder.direct(enchantment), level);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("enchantment").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            Map<Enchantment, Integer> enchantments = getEnchantments(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET));
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                EditorUtil.sendFeedback(context.getSource(), Component.empty().append(Component.literal("- ").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))).append(getEnchantmentName(entry.getKey(), entry.getValue())));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> getEnchantmentNode = commandManager.argument("enchantment", RegistryArgumentType.registryEntry(Registries.ENCHANTMENT, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", Registries.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = getEnchantments(stack);
            if (!enchantments.containsKey(enchantment)) {
                throw NO_SUCH_ENCHANTMENTS_EXCEPTION;
            }

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_ENCHANTMENT, getEnchantmentName(enchantment, enchantments.get(enchantment))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setEnchantmentNode = commandManager.argument("enchantment", RegistryArgumentType.registryEntry(Registries.ENCHANTMENT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", Registries.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchantments(stack));
            if (enchantments.containsKey(enchantment) && enchantments.get(enchantment) == 1) {
                throw ALREADY_IS_EXCEPTION;
            }
            enchantments.put(enchantment, 1);
            setEnchantments(stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, getEnchantmentName(enchantment, 1)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEnchantmentLevelNode = commandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", Registries.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchantments(stack));
            int level = IntegerArgumentType.getInteger(context, "level");
            if (enchantments.containsKey(enchantment) && enchantments.get(enchantment) == level) {
                throw ALREADY_IS_EXCEPTION;
            }
            enchantments.put(enchantment, level);
            setEnchantments(stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, getEnchantmentName(enchantment, level)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").build();

        CommandNode<S> removeEnchantmentNode = commandManager.argument("enchantment", RegistryArgumentType.registryEntry(Registries.ENCHANTMENT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", Registries.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getEnchantments(stack));
            if (!enchantments.containsKey(enchantment)) {
                throw NO_SUCH_ENCHANTMENTS_EXCEPTION;
            }
            enchantments.remove(enchantment);
            setEnchantments(stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            Component description = enchantment.description().copy();
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE, description));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> glintNode = commandManager.literal("glint").build();

        CommandNode<S> glintGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasGlintOverride(stack)) {
                throw NO_GLINT_OVERRIDE_EXCEPTION;
            }
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(getGlintOverride(stack) ? OUTPUT_GLINT_GET_ENABLED : OUTPUT_GLINT_GET_DISABLED));
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
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(glint ? OUTPUT_GLINT_ENABLE : OUTPUT_GLINT_DISABLE));
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
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GLINT_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasEnchantments(stack)) {
                throw NO_ENCHANTMENTS_EXCEPTION;
            }
            setEnchantments(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> storedNode = commandManager.literal("stored").build();

        CommandNode<S> storedGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasStoredEnchantments(stack)) {
                throw NO_STORED_ENCHANTMENTS_EXCEPTION;
            }
            Map<Enchantment, Integer> enchantments = getStoredEnchantments(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_STORED));
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                EditorUtil.sendFeedback(context.getSource(), Component.empty().append(Component.literal("- ").setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))).append(getEnchantmentName(entry.getKey(), entry.getValue())));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> storedGetEnchantmentNode = commandManager.argument("enchantment", RegistryArgumentType.registryEntry(Registries.ENCHANTMENT, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasStoredEnchantments(stack)) {
                throw NO_STORED_ENCHANTMENTS_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", Registries.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = getStoredEnchantments(stack);
            if (!enchantments.containsKey(enchantment)) {
                throw NO_SUCH_STORED_ENCHANTMENTS_EXCEPTION;
            }

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET_STORED_ENCHANTMENT, getEnchantmentName(enchantment, enchantments.get(enchantment))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> storedSetNode = commandManager.literal("set").build();

        CommandNode<S> storedSetEnchantmentNode = commandManager.argument("enchantment", RegistryArgumentType.registryEntry(Registries.ENCHANTMENT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", Registries.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getStoredEnchantments(stack));
            if (enchantments.containsKey(enchantment) && enchantments.get(enchantment) == 1) {
                throw STORED_ALREADY_IS_EXCEPTION;
            }
            enchantments.put(enchantment, 1);
            setStoredEnchantments(context.getSource().registryAccess(), stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_STORED, getEnchantmentName(enchantment, 1)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> storedSetEnchantmentLevelNode = commandManager.argument("level", IntegerArgumentType.integer(0, 255)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", Registries.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getStoredEnchantments(stack));
            int level = IntegerArgumentType.getInteger(context, "level");
            if (enchantments.containsKey(enchantment) && enchantments.get(enchantment) == level) {
                throw STORED_ALREADY_IS_EXCEPTION;
            }
            enchantments.put(enchantment, level);
            setStoredEnchantments(context.getSource().registryAccess(), stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET_STORED, getEnchantmentName(enchantment, level)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> storedRemoveNode = commandManager.literal("remove").build();

        CommandNode<S> storedRemoveEnchantmentNode = commandManager.argument("enchantment", RegistryArgumentType.registryEntry(Registries.ENCHANTMENT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasStoredEnchantments(stack)) {
                throw NO_STORED_ENCHANTMENTS_EXCEPTION;
            }
            Enchantment enchantment = RegistryArgumentType.getRegistryEntry(context, "enchantment", Registries.ENCHANTMENT);
            Map<Enchantment, Integer> enchantments = new HashMap<>(getStoredEnchantments(stack));
            if (!enchantments.containsKey(enchantment)) {
                throw NO_SUCH_STORED_ENCHANTMENTS_EXCEPTION;
            }
            enchantments.remove(enchantment);
            setStoredEnchantments(context.getSource().registryAccess(), stack, enchantments);

            EditorUtil.setStack(context.getSource(), stack);
            Component description = enchantment.description().copy();
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE_STORED, description));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> storedClearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasStoredEnchantments(stack)) {
                throw NO_STORED_ENCHANTMENTS_EXCEPTION;
            }
            setStoredEnchantments(context.getSource().registryAccess(), stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_CLEAR_STORED));
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

        // ... stored ...
        node.addChild(storedNode);
        // ... get [<enchantment>]
        storedNode.addChild(storedGetNode);
        storedGetNode.addChild(storedGetEnchantmentNode);
        // ... set <enchantment> [<level>]
        storedNode.addChild(storedSetNode);
        storedSetNode.addChild(storedSetEnchantmentNode);
        storedSetEnchantmentNode.addChild(storedSetEnchantmentLevelNode);
        // ... remove <enchantment>
        storedNode.addChild(storedRemoveNode);
        storedRemoveNode.addChild(storedRemoveEnchantmentNode);
        // ... clear
        storedNode.addChild(storedClearNode);

        return node;
    }
}
