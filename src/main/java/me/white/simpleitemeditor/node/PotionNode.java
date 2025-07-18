package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.ColorArgumentType;
import me.white.simpleitemeditor.argument.DurationArgumentType;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.potion.Potion;
//? if <1.21.2 {
/*import net.minecraft.registry.Registries;
*///?}
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class PotionNode implements Node {
    private static final CommandSyntaxException ISNT_POTION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.isntpotion")).create();
    private static final CommandSyntaxException NO_EFFECTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.noeffects")).create();
    private static final CommandSyntaxException NO_SUCH_EFFECT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.nosucheffect")).create();
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.alreadyis")).create();
    private static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.nocolor")).create();
    private static final CommandSyntaxException COLOR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.coloralreadyis")).create();
    private static final CommandSyntaxException NO_TYPE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.notype")).create();
    private static final CommandSyntaxException TYPE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.typealreadyis")).create();
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

    private static void setPotionComponent(ItemStack stack, Optional<RegistryEntry<Potion>> potion, Optional<Integer> customColor, List<StatusEffectInstance> effects) {
        //? if >=1.21.2 {
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion, customColor, effects, Optional.empty()));
        //?} else {
        /*stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(potion, customColor, effects));
        *///?}
    }

    private static Text getTranslation(StatusEffectInstance effect) {
        return Text.translatable(OUTPUT_EFFECT, effect.getEffectType().value().getName(), effect.getAmplifier() + 1, effect.getDuration() < 0 ? "Infinity" : effect.getDuration());
    }

    private static Text getTranslation(ItemStack stack, Potion potion) {
        //? if >=1.21.2 {
        return Text.translatable(stack.getItem().getTranslationKey() + ".effect." + potion.getBaseName());
        //?} else {
        /*return Text.translatable(Potion.finishTranslationKey(Optional.of(Registries.POTION.getEntry(potion)), stack.getItem().getTranslationKey() + ".effect."));
        *///?}
    }

    private static boolean isPotion(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof PotionItem || item instanceof TippedArrowItem;
    }

    private static boolean hasPotionContents(ItemStack stack) {
        return stack.contains(DataComponentTypes.POTION_CONTENTS);
    }

    private static boolean hasEffects(ItemStack stack) {
        if (!hasPotionContents(stack)) {
            return false;
        }
        return !stack.get(DataComponentTypes.POTION_CONTENTS).customEffects().isEmpty();
    }

    private static List<StatusEffectInstance> getEffects(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).customEffects();
    }

    private static void setEffects(ItemStack stack, List<StatusEffectInstance> effects) {
        if (effects == null) {
            effects = List.of();
        }
        PotionContentsComponent component = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        setPotionComponent(stack, component.potion(), component.customColor(), effects);
    }

    private static boolean hasType(ItemStack stack) {
        if (!hasPotionContents(stack)) {
            return false;
        }
        return stack.get(DataComponentTypes.POTION_CONTENTS).potion().isPresent();
    }

    private static Potion getType(ItemStack stack) {
        if (!hasType(stack)) {
            return null;
        }
        return stack.get(DataComponentTypes.POTION_CONTENTS).potion().orElseThrow().value();
    }

    private static void setType(ItemStack stack, Potion potion) {
        Optional<RegistryEntry<Potion>> componentPotion = potion == null ? Optional.empty() : Optional.of(RegistryEntry.of(potion));
        PotionContentsComponent component = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        setPotionComponent(stack, componentPotion, component.customColor(), component.customEffects());
    }

    private static boolean hasColor(ItemStack stack) {
        if (!hasPotionContents(stack)) {
            return false;
        }
        return stack.get(DataComponentTypes.POTION_CONTENTS).customColor().isPresent();
    }

    private static int getColor(ItemStack stack) {
        if (!hasColor(stack)) {
            return -1;
        }
        return stack.get(DataComponentTypes.POTION_CONTENTS).customColor().orElseThrow();
    }

    private static void setColor(ItemStack stack, int color) {
        PotionContentsComponent component = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        setPotionComponent(stack, component.potion(), Optional.of(color), component.customEffects());
    }

    private static void removeColor(ItemStack stack) {
        PotionContentsComponent component = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        setPotionComponent(stack, component.potion(), Optional.empty(), component.customEffects());
    }

    private static void addEffect(List<StatusEffectInstance> effects, StatusEffectInstance effect) throws CommandSyntaxException {
        Iterator<StatusEffectInstance> iterator = effects.iterator();
        while (iterator.hasNext()) {
            StatusEffectInstance instance = iterator.next();
            if (instance.getEffectType().value() == effect.getEffectType().value()) {
                if (instance.getDuration() == effect.getDuration() && instance.getAmplifier() == effect.getAmplifier() && instance.isAmbient() == effect.isAmbient() && instance.shouldShowParticles() == effect.shouldShowParticles() && instance.shouldShowIcon() == effect.shouldShowIcon()) {
                    throw ALREADY_IS_EXCEPTION;
                }
                iterator.remove();
            }
        }
        effects.add(effect);
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("potion").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            List<StatusEffectInstance> effects = getEffects(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET));
            for (StatusEffectInstance instance : effects) {
                EditorUtil.sendFeedback(context.getSource(), Text.empty().append(Text.literal("- ").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))).append(getTranslation(instance)));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> getEffectNode = commandManager.argument("effect", RegistryArgumentType.registryEntry(RegistryKeys.STATUS_EFFECT, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            List<StatusEffectInstance> effects = getEffects(stack);
            StatusEffectInstance matching = null;
            for (StatusEffectInstance instance : effects) {
                if (instance.getEffectType().value() == effect) {
                    matching = instance;
                    break;
                }
            }
            if (matching == null) {
                throw NO_SUCH_EFFECT_EXCEPTION;
            }

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_EFFECT, getTranslation(matching)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setEffectNode = commandManager.argument("effect", RegistryArgumentType.registryEntry(RegistryKeys.STATUS_EFFECT, registryAccess)).build();

        CommandNode<S> setEffectDurationNode = commandManager.argument("duration", DurationArgumentType.duration()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            int duration = DurationArgumentType.getDuration(context, "duration");
            List<StatusEffectInstance> effects = new ArrayList<>(getEffects(stack));
            StatusEffectInstance instance = new StatusEffectInstance(RegistryEntry.of(effect), duration);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEffectDurationAmplifierNode = commandManager.argument("amplifier", IntegerArgumentType.integer(StatusEffectInstance.MIN_AMPLIFIER, StatusEffectInstance.MAX_AMPLIFIER)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            int duration = DurationArgumentType.getDuration(context, "duration");
            int amplifier = IntegerArgumentType.getInteger(context, "amplifier");
            List<StatusEffectInstance> effects = new ArrayList<>(getEffects(stack));
            StatusEffectInstance instance = new StatusEffectInstance(RegistryEntry.of(effect), duration, amplifier);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEffectDurationAmplifierParticlesNode = commandManager.argument("particles", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            int duration = DurationArgumentType.getDuration(context, "duration");
            int amplifier = IntegerArgumentType.getInteger(context, "amplifier");
            boolean particles = BoolArgumentType.getBool(context, "particles");
            List<StatusEffectInstance> effects = new ArrayList<>(getEffects(stack));
            StatusEffectInstance instance = new StatusEffectInstance(RegistryEntry.of(effect), duration, amplifier, false, particles, true);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEffectDurationAmplifierParticlesIconNode = commandManager.argument("icon", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            int duration = DurationArgumentType.getDuration(context, "duration");
            int amplifier = IntegerArgumentType.getInteger(context, "amplifier");
            boolean particles = BoolArgumentType.getBool(context, "particles");
            boolean icon = BoolArgumentType.getBool(context, "icon");
            List<StatusEffectInstance> effects = new ArrayList<>(getEffects(stack));
            StatusEffectInstance instance = new StatusEffectInstance(RegistryEntry.of(effect), duration, amplifier, false, particles, icon);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setEffectDurationAmplifierParticlesIconAmbientNode = commandManager.argument("ambient", BoolArgumentType.bool()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            int duration = DurationArgumentType.getDuration(context, "duration");
            int amplifier = IntegerArgumentType.getInteger(context, "amplifier");
            boolean particles = BoolArgumentType.getBool(context, "particles");
            boolean icon = BoolArgumentType.getBool(context, "icon");
            boolean ambient = BoolArgumentType.getBool(context, "ambient");
            List<StatusEffectInstance> effects = new ArrayList<>(getEffects(stack));
            StatusEffectInstance instance = new StatusEffectInstance(RegistryEntry.of(effect), duration, amplifier, ambient, particles, icon);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").build();

        CommandNode<S> removeEffectNode = commandManager.argument("effect", RegistryArgumentType.registryEntry(RegistryKeys.STATUS_EFFECT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            boolean wasSuccessful = false;
            List<StatusEffectInstance> effects = new ArrayList<>(getEffects(stack));
            if (hasEffects(stack)) {
                Iterator<StatusEffectInstance> iterator = effects.iterator();
                while (iterator.hasNext()) {
                    StatusEffectInstance instance = iterator.next();
                    if (instance.getEffectType().value() == effect) {
                        wasSuccessful = true;
                        iterator.remove();
                    }
                }
            }
            if (!wasSuccessful) {
                throw NO_SUCH_EFFECT_EXCEPTION;
            }
            setEffects(stack, effects);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> clearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            setEffects(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorNode = commandManager.literal("color").build();

        CommandNode<S> colorGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            int color = getColor(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_COLOR_GET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorSetNode = commandManager.literal("set").build();

        CommandNode<S> colorSetColorNode = commandManager.argument("color", ColorArgumentType.color()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            int color = ColorArgumentType.getColor(context, "color");
            if (hasColor(stack) && color == getColor(stack)) {
                throw COLOR_ALREADY_IS_EXCEPTION;
            }
            setColor(stack, color);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_COLOR_SET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> colorRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            removeColor(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_COLOR_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> typeNode = commandManager.literal("type").build();

        CommandNode<S> typeGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasType(stack)) {
                throw NO_TYPE_EXCEPTION;
            }
            Potion potion = getType(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_TYPE_GET, getTranslation(stack, potion)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> typeSetNode = commandManager.literal("set").build();

        CommandNode<S> typeSetTypeNode = commandManager.argument("type", RegistryArgumentType.registryEntry(RegistryKeys.POTION, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            Potion potion = RegistryArgumentType.getRegistryEntry(context, "type", RegistryKeys.POTION);
            if (hasType(stack) && potion == getType(stack)) {
                throw TYPE_ALREADY_IS_EXCEPTION;
            }
            setType(stack, potion);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_TYPE_SET, getTranslation(stack, potion)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> typeResetNode = commandManager.literal("reset").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isPotion(stack)) {
                throw ISNT_POTION_EXCEPTION;
            }
            if (!hasType(stack)) {
                throw NO_TYPE_EXCEPTION;
            }
            setType(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_TYPE_RESET));
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
