package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.argument.ColorArgumentType;
import me.white.sie.argument.DurationArgumentType;
import me.white.sie.argument.RegistryArgumentType;
import me.white.sie.util.EditorUtil;
import me.white.sie.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TippedArrowItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.*;

public class PotionNode implements Node {
    private static final CommandSyntaxException ISNT_POTION_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.potion.error.isntpotion")).create();
    private static final CommandSyntaxException NO_EFFECTS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.potion.error.noeffects")).create();
    private static final CommandSyntaxException NO_SUCH_EFFECT_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.potion.error.nosucheffect")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.potion.error.alreadyis")).create();
    private static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.potion.error.nocolor")).create();
    private static final CommandSyntaxException COLOR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.potion.error.coloralreadyis")).create();
    private static final CommandSyntaxException NO_TYPE_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.potion.error.notype")).create();
    private static final CommandSyntaxException TYPE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.potion.error.typealreadyis")).create();
    private static final String OUTPUT_TYPE_GET = "commands.edit.potion.gettype";
    private static final String OUTPUT_TYPE_SET = "commands.edit.potion.settype";
    private static final String OUTPUT_TYPE_RESET = "commands.edit.potion.resettype";
    private static final String OUTPUT_GET = "commands.edit.potion.get";
    private static final String OUTPUT_GET_EFFECT = "commands.edit.potion.geteffect";
    private static final String OUTPUT_SET = "commands.edit.potion.set";
    private static final String OUTPUT_REMOVE = "commands.edit.potion.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.potion.clear";
    private static final String OUTPUT_COLOR_GET = "commands.edit.potion.getcolor";
    private static final String OUTPUT_COLOR_SET = "commands.edit.potion.setcolor";
    private static final String OUTPUT_COLOR_REMOVE = "commands.edit.potion.removecolor";
    private static final String OUTPUT_EFFECT = "commands.edit.potion.effect";

    private static void setPotionComponent(ItemStack stack, Optional<Holder<Potion>> potion, Optional<Integer> customColor, List<MobEffectInstance> effects) {
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion, customColor, effects, Optional.empty()));
    }

    private static Component getTranslation(MobEffectInstance effect) {
        return Component.translatable(OUTPUT_EFFECT, effect.getEffect().value().getDisplayName(), effect.getAmplifier() + 1, effect.getDuration() < 0 ? "Infinity" : effect.getDuration());
    }

    private static Component getTranslation(ItemStack stack, Potion potion) {
        return Component.translatable(stack.getItem().getDescriptionId() + ".effect." + potion.name());
    }

    private static boolean isPotion(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof PotionItem || item instanceof TippedArrowItem;
    }

    private static boolean hasPotionContents(ItemStack stack) {
        return stack.has(DataComponents.POTION_CONTENTS);
    }

    private static boolean hasEffects(ItemStack stack) {
        if (!hasPotionContents(stack)) {
            return false;
        }
        return !stack.get(DataComponents.POTION_CONTENTS).customEffects().isEmpty();
    }

    private static List<MobEffectInstance> getEffects(ItemStack stack) {
        return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).customEffects();
    }

    private static void setEffects(ItemStack stack, List<MobEffectInstance> effects) {
        if (effects == null) {
            effects = List.of();
        }
        PotionContents component = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        setPotionComponent(stack, component.potion(), component.customColor(), effects);
    }

    private static boolean hasType(ItemStack stack) {
        if (!hasPotionContents(stack)) {
            return false;
        }
        return stack.get(DataComponents.POTION_CONTENTS).potion().isPresent();
    }

    private static Potion getType(ItemStack stack) {
        if (!hasType(stack)) {
            return null;
        }
        return stack.get(DataComponents.POTION_CONTENTS).potion().orElseThrow().value();
    }

    private static void setType(ItemStack stack, Potion potion) {
        Optional<Holder<Potion>> componentPotion = potion == null ? Optional.empty() : Optional.of(Holder.direct(potion));
        PotionContents component = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        setPotionComponent(stack, componentPotion, component.customColor(), component.customEffects());
    }

    private static boolean hasColor(ItemStack stack) {
        if (!hasPotionContents(stack)) {
            return false;
        }
        return stack.get(DataComponents.POTION_CONTENTS).customColor().isPresent();
    }

    private static int getColor(ItemStack stack) {
        if (!hasColor(stack)) {
            return -1;
        }
        return stack.get(DataComponents.POTION_CONTENTS).customColor().orElseThrow();
    }

    private static void setColor(ItemStack stack, int color) {
        PotionContents component = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        setPotionComponent(stack, component.potion(), Optional.of(color), component.customEffects());
    }

    private static void removeColor(ItemStack stack) {
        PotionContents component = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        setPotionComponent(stack, component.potion(), Optional.empty(), component.customEffects());
    }

    private static void addEffect(List<MobEffectInstance> effects, MobEffectInstance effect) throws CommandSyntaxException {
        Iterator<MobEffectInstance> iterator = effects.iterator();
        while (iterator.hasNext()) {
            MobEffectInstance instance = iterator.next();
            if (instance.getEffect().value() == effect.getEffect().value()) {
                if (instance.getDuration() == effect.getDuration() && instance.getAmplifier() == effect.getAmplifier() && instance.isAmbient() == effect.isAmbient() && instance.isVisible() == effect.isVisible() && instance.showIcon() == effect.showIcon()) {
                    throw ALREADY_IS_EXCEPTION;
                }
                iterator.remove();
            }
        }
        effects.add(effect);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("potion").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(conComponent -> {
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource());
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            List<MobEffectInstance> effects = getEffects(stack);

            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_GET));
            for (MobEffectInstance instance : effects) {
                EditorUtil.sendFeedback(conComponent.getSource(), Component.empty().append(Component.literal("- ").setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY))).append(getTranslation(instance)));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> getEffectNode = commandManager.argument("effect", RegistryArgumentType.registryEntry(Registries.MOB_EFFECT, registryAccess)).executes(conComponent -> {
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource());
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            MobEffect effect = RegistryArgumentType.getRegistryEntry(conComponent, "effect", Registries.MOB_EFFECT);
            List<MobEffectInstance> effects = getEffects(stack);
            MobEffectInstance matching = null;
            for (MobEffectInstance instance : effects) {
                if (instance.getEffect().value() == effect) {
                    matching = instance;
                    break;
                }
            }
            if (matching == null) {
                throw NO_SUCH_EFFECT_EXCEPTION;
            }

            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_GET_EFFECT, getTranslation(matching)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setEffectNode = commandManager.argument("effect", RegistryArgumentType.registryEntry(Registries.MOB_EFFECT, registryAccess)).build();

        CommandNode<S> setEffectDurationNode = commandManager.argument("duration", DurationArgumentType.duration()).executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            MobEffect effect = RegistryArgumentType.getRegistryEntry(conComponent, "effect", Registries.MOB_EFFECT);
            int duration = DurationArgumentType.getDuration(conComponent, "duration");
            List<MobEffectInstance> effects = new ArrayList<>(getEffects(stack));
            MobEffectInstance instance = new MobEffectInstance(Holder.direct(effect), duration);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEffectDurationAmplifierNode = commandManager.argument("amplifier", IntegerArgumentType.integer(MobEffectInstance.MIN_AMPLIFIER, MobEffectInstance.MAX_AMPLIFIER)).executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            MobEffect effect = RegistryArgumentType.getRegistryEntry(conComponent, "effect", Registries.MOB_EFFECT);
            int duration = DurationArgumentType.getDuration(conComponent, "duration");
            int amplifier = IntegerArgumentType.getInteger(conComponent, "amplifier");
            List<MobEffectInstance> effects = new ArrayList<>(getEffects(stack));
            MobEffectInstance instance = new MobEffectInstance(Holder.direct(effect), duration, amplifier);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEffectDurationAmplifierParticlesNode = commandManager.argument("particles", BoolArgumentType.bool()).executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            MobEffect effect = RegistryArgumentType.getRegistryEntry(conComponent, "effect", Registries.MOB_EFFECT);
            int duration = DurationArgumentType.getDuration(conComponent, "duration");
            int amplifier = IntegerArgumentType.getInteger(conComponent, "amplifier");
            boolean particles = BoolArgumentType.getBool(conComponent, "particles");
            List<MobEffectInstance> effects = new ArrayList<>(getEffects(stack));
            MobEffectInstance instance = new MobEffectInstance(Holder.direct(effect), duration, amplifier, false, particles, true);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEffectDurationAmplifierParticlesIconNode = commandManager.argument("icon", BoolArgumentType.bool()).executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            MobEffect effect = RegistryArgumentType.getRegistryEntry(conComponent, "effect", Registries.MOB_EFFECT);
            int duration = DurationArgumentType.getDuration(conComponent, "duration");
            int amplifier = IntegerArgumentType.getInteger(conComponent, "amplifier");
            boolean particles = BoolArgumentType.getBool(conComponent, "particles");
            boolean icon = BoolArgumentType.getBool(conComponent, "icon");
            List<MobEffectInstance> effects = new ArrayList<>(getEffects(stack));
            MobEffectInstance instance = new MobEffectInstance(Holder.direct(effect), duration, amplifier, false, particles, icon);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEffectDurationAmplifierParticlesIconAmbientNode = commandManager.argument("ambient", BoolArgumentType.bool()).executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            MobEffect effect = RegistryArgumentType.getRegistryEntry(conComponent, "effect", Registries.MOB_EFFECT);
            int duration = DurationArgumentType.getDuration(conComponent, "duration");
            int amplifier = IntegerArgumentType.getInteger(conComponent, "amplifier");
            boolean particles = BoolArgumentType.getBool(conComponent, "particles");
            boolean icon = BoolArgumentType.getBool(conComponent, "icon");
            boolean ambient = BoolArgumentType.getBool(conComponent, "ambient");
            List<MobEffectInstance> effects = new ArrayList<>(getEffects(stack));
            MobEffectInstance instance = new MobEffectInstance(Holder.direct(effect), duration, amplifier, ambient, particles, icon);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").build();

        CommandNode<S> removeEffectNode = commandManager.argument("effect", RegistryArgumentType.registryEntry(Registries.MOB_EFFECT, registryAccess)).executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            MobEffect effect = RegistryArgumentType.getRegistryEntry(conComponent, "effect", Registries.MOB_EFFECT);
            boolean wasSuccessful = false;
            List<MobEffectInstance> effects = new ArrayList<>(getEffects(stack));
            if (hasEffects(stack)) {
                Iterator<MobEffectInstance> iterator = effects.iterator();
                while (iterator.hasNext()) {
                    MobEffectInstance instance = iterator.next();
                    if (instance.getEffect().value() == effect) {
                        wasSuccessful = true;
                        iterator.remove();
                    }
                }
            }
            if (!wasSuccessful) {
                throw NO_SUCH_EFFECT_EXCEPTION;
            }
            setEffects(stack, effects);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearNode = commandManager.literal("clear").executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            setEffects(stack, null);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorNode = commandManager.literal("color").build();

        CommandNode<S> colorGetNode = commandManager.literal("get").executes(conComponent -> {
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource());
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            int color = getColor(stack);

            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_COLOR_GET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorSetNode = commandManager.literal("set").build();

        CommandNode<S> colorSetColorNode = commandManager.argument("color", ColorArgumentType.color()).executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            int color = ColorArgumentType.getColor(conComponent, "color");
            if (hasColor(stack) && color == getColor(stack)) {
                throw COLOR_ALREADY_IS_EXCEPTION;
            }
            setColor(stack, color);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_COLOR_SET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorRemoveNode = commandManager.literal("remove").executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            removeColor(stack);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_COLOR_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> typeNode = commandManager.literal("type").build();

        CommandNode<S> typeGetNode = commandManager.literal("get").executes(conComponent -> {
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource());
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasType(stack)) {
                throw NO_TYPE_EXCEPTION;
            }
            Potion potion = getType(stack);

            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_TYPE_GET, getTranslation(stack, potion)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> typeSetNode = commandManager.literal("set").build();

        CommandNode<S> typeSetTypeNode = commandManager.argument("type", RegistryArgumentType.registryEntry(Registries.POTION, registryAccess)).executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            Potion potion = RegistryArgumentType.getRegistryEntry(conComponent, "type", Registries.POTION);
            if (hasType(stack) && potion == getType(stack)) {
                throw TYPE_ALREADY_IS_EXCEPTION;
            }
            setType(stack, potion);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_TYPE_SET, getTranslation(stack, potion)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> typeResetNode = commandManager.literal("reset").executes(conComponent -> {
            EditorUtil.checkCanEdit(conComponent.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(conComponent.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasType(stack)) {
                throw NO_TYPE_EXCEPTION;
            }
            setType(stack, null);

            EditorUtil.setStack(conComponent.getSource(), stack);
            EditorUtil.sendFeedback(conComponent.getSource(), Component.translatable(OUTPUT_TYPE_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get [<effect>]
        node.addChild(getNode);
        getNode.addChild(getEffectNode);

        // ... set <effect> <duration> [<amplifier>] [<particles>] [<icon>] [<ambient>]
        node.addChild(setNode);
        setNode.addChild(setEffectNode);
        setEffectNode.addChild(setEffectDurationNode);
        setEffectDurationNode.addChild(setEffectDurationAmplifierNode);
        setEffectDurationAmplifierNode.addChild(setEffectDurationAmplifierParticlesNode);
        setEffectDurationAmplifierParticlesNode.addChild(setEffectDurationAmplifierParticlesIconNode);
        setEffectDurationAmplifierParticlesIconNode.addChild(setEffectDurationAmplifierParticlesIconAmbientNode);

        // ... remove <effect>
        node.addChild(removeNode);
        removeNode.addChild(removeEffectNode);

        // ... clear
        node.addChild(clearNode);

        // ... type ...
        node.addChild(typeNode);
        // ... get
        typeNode.addChild(typeGetNode);
        // ... set <type>
        typeNode.addChild(typeSetNode);
        typeSetNode.addChild(typeSetTypeNode);
        // ... reset
        typeNode.addChild(typeResetNode);

        // ... color ...
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
