package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.argument.AlternativeArgumentType;
import me.white.simpleitemeditor.argument.ColorArgumentType;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.argument.DurationArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.potion.Potion;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

public class PotionNode implements Node {
    public static final CommandSyntaxException ISNT_POTION_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.cannotedit")).create();
    public static final CommandSyntaxException NO_EFFECTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.noeffects")).create();
    public static final CommandSyntaxException NO_SUCH_EFFECT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.nosucheffect")).create();
    public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.alreadyis")).create();
    public static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.nocolor")).create();
    public static final CommandSyntaxException COLOR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.coloralreadyis")).create();
    public static final CommandSyntaxException NO_TYPE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.notype")).create();
    public static final CommandSyntaxException TYPE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.typealreadyis")).create();
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
    private static final String OUTPUT_COLOR_RESET = "commands.edit.potion.resetcolor";
    private static final String OUTPUT_EFFECT = "commands.edit.potion.effect";
    private static final Map<String, Integer> DURATION_CONSTS = new HashMap<>();

    static {
        DURATION_CONSTS.put("infinity", -1);
    }

    private static Text getTranslation(StatusEffectInstance effect) {
        return Text.translatable(OUTPUT_EFFECT, effect.getEffectType().value().getName(), effect.getAmplifier() + 1, effect.getDuration() < 0 ? "Infinity" : effect.getDuration());
    }

    private static Text getTranslation(ItemStack stack, Potion potion) {
        return Text.translatable(Potion.finishTranslationKey(Optional.of(RegistryEntry.of(potion)), stack.getTranslationKey() + ".effect."));
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
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(component.potion(), component.customColor(), effects));
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
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(componentPotion, component.customColor(), component.customEffects()));
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
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(component.potion(), Optional.of(color), component.customEffects()));
    }

    private static void removeColor(ItemStack stack) {
        PotionContentsComponent component = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        stack.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(component.potion(), Optional.empty(), component.customEffects()));
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

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("potion").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            List<StatusEffectInstance> effects = getEffects(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
            for (StatusEffectInstance instance : effects) {
                context.getSource().sendFeedback(Text.empty().append(Text.literal("- ").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY))).append(getTranslation(instance)));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<StatusEffect>> getEffectNode = ClientCommandManager.argument("effect", RegistryArgumentType.registryEntry(RegistryKeys.STATUS_EFFECT, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
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

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_EFFECT, getTranslation(matching)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<StatusEffect>> setEffectNode = ClientCommandManager.argument("effect", RegistryArgumentType.registryEntry(RegistryKeys.STATUS_EFFECT, registryAccess)).build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setEffectDurationNode = ClientCommandManager.argument("duration", AlternativeArgumentType.argument(DurationArgumentType.duration(), DURATION_CONSTS)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            int duration = DurationArgumentType.getDuration(context, "duration");
            List<StatusEffectInstance> effects = new ArrayList<>(getEffects(stack));
            StatusEffectInstance instance = new StatusEffectInstance(RegistryEntry.of(effect), duration);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setEffectDurationAmplifierNode = ClientCommandManager.argument("amplifier", IntegerArgumentType.integer(StatusEffectInstance.MIN_AMPLIFIER, StatusEffectInstance.MAX_AMPLIFIER)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            int duration = DurationArgumentType.getDuration(context, "duration");
            int amplifier = IntegerArgumentType.getInteger(context, "amplifier");
            List<StatusEffectInstance> effects = new ArrayList<>(getEffects(stack));
            StatusEffectInstance instance = new StatusEffectInstance(RegistryEntry.of(effect), duration, amplifier);
            addEffect(effects, instance);
            setEffects(stack, effects);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> setEffectDurationAmplifierParticlesNode = ClientCommandManager.argument("particles", BoolArgumentType.bool()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
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
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> setEffectDurationAmplifierParticlesIconNode = ClientCommandManager.argument("icon", BoolArgumentType.bool()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
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
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        ArgumentCommandNode<FabricClientCommandSource, Boolean> setEffectDurationAmplifierParticlesIconAmbientNode = ClientCommandManager.argument("ambient", BoolArgumentType.bool()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
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
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(instance)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<StatusEffect>> removeEffectNode = ClientCommandManager.argument("effect", RegistryArgumentType.registryEntry(RegistryKeys.STATUS_EFFECT, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            StatusEffect effect = RegistryArgumentType.getRegistryEntry(context, "effect", RegistryKeys.STATUS_EFFECT);
            List<StatusEffectInstance> effects = new ArrayList<>(getEffects(stack));
            if (hasEffects(stack)) {
                Iterator<StatusEffectInstance> iterator = effects.iterator();
                while (iterator.hasNext()) {
                    StatusEffectInstance instance = iterator.next();
                    if (instance.getEffectType().value() == effect) {
                        iterator.remove();
                    }
                }
            }
            setEffects(stack, effects);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, effect.getName()));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager.literal("clear").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasEffects(stack)) {
                throw NO_EFFECTS_EXCEPTION;
            }
            setEffects(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> colorNode = ClientCommandManager.literal("color").build();

        LiteralCommandNode<FabricClientCommandSource> colorGetNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            int color = getColor(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_COLOR_GET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> colorSetNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> colorSetColorNode = ClientCommandManager.argument("color", ColorArgumentType.color()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            int color = ColorArgumentType.getColor(context, "color");
            if (hasColor(stack) && color == getColor(stack)) {
                throw COLOR_ALREADY_IS_EXCEPTION;
            }
            setColor(stack, color);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_COLOR_SET, TextUtil.copyable(EditorUtil.formatColor(color))));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> colorResetNode = ClientCommandManager.literal("reset").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasColor(stack)) {
                throw NO_COLOR_EXCEPTION;
            }
            removeColor(stack);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_COLOR_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> typeNode = ClientCommandManager.literal("type").build();

        LiteralCommandNode<FabricClientCommandSource> typeGetNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasType(stack)) {
                throw NO_TYPE_EXCEPTION;
            }
            Potion potion = getType(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_TYPE_GET, getTranslation(stack, potion)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> typeSetNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry<Potion>> typeSetTypeNode = ClientCommandManager.argument("type", RegistryArgumentType.registryEntry(RegistryKeys.POTION, registryAccess)).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            Potion potion = RegistryArgumentType.getRegistryEntry(context, "type", RegistryKeys.POTION);
            if (hasType(stack) && potion == getType(stack)) {
                throw TYPE_ALREADY_IS_EXCEPTION;
            }
            setType(stack, potion);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_TYPE_SET, getTranslation(stack, potion)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> typeResetNode = ClientCommandManager.literal("reset").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!hasType(stack)) {
                throw NO_TYPE_EXCEPTION;
            }
            setType(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_TYPE_RESET));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

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
        // ... reset
        colorNode.addChild(colorResetNode);
    }
}
