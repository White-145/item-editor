package me.white.itemeditor.node;

import java.util.HashMap;

import org.apache.commons.lang3.tuple.Triple;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class PotionNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.cannotedit")).create();
    public static final CommandSyntaxException NO_POTION_EFFECTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.nopotioneffects")).create();
    public static final CommandSyntaxException ALREADY_EXISTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.alreadyexists")).create();
    public static final CommandSyntaxException DOESNT_EXIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.potion.error.doesntexist")).create();
    private static final String OUTPUT_GET = "commands.edit.potion.get";
    private static final String OUTPUT_GET_EFFECT = "commands.edit.potion.geteffect";
    private static final String OUTPUT_SET = "commands.edit.potion.set";
    private static final String OUTPUT_REMOVE = "commands.edit.potion.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.potion.clear";
    private static final String OUTPUT_EFFECT = "commands.edit.potion.effect";

    private static Text getTranslation(StatusEffect effect, Triple<Integer, Integer, Boolean> potionEffect) {
        return Text.translatable(OUTPUT_EFFECT, effect.getName(), potionEffect.getLeft(), potionEffect.getMiddle() < 0 ? "Infinity" : potionEffect.getMiddle());
    }

    private static boolean canEdit(ItemStack stack) {
        Item item = stack.getItem();
        return
            item instanceof PotionItem ||
            item instanceof TippedArrowItem;
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("potion")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!EditHelper.hasPotionEffects(stack, true)) throw NO_POTION_EFFECTS_EXCEPTION;
                HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> potionEffects = EditHelper.getPotionEffects(stack);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                for (StatusEffect effect : potionEffects.keySet()) {
                    Triple<Integer, Integer, Boolean> potionEffect = potionEffects.get(effect);
                    context.getSource().sendFeedback(Text.empty()
                        .append(Text.literal("- ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
                        .append(getTranslation(effect, potionEffect))
                    );
                }
                return potionEffects.size();
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Reference<StatusEffect>> getIndexNode = ClientCommandManager
            .argument("effect", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!EditHelper.hasPotionEffects(stack, true)) throw NO_POTION_EFFECTS_EXCEPTION;
                StatusEffect effect = Util.getRegistryEntryArgument(context, "effect", RegistryKeys.STATUS_EFFECT);
                HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> potionEffects = EditHelper.getPotionEffects(stack);
                if (!potionEffects.containsKey(effect)) throw DOESNT_EXIST_EXCEPTION;
                Triple<Integer, Integer, Boolean> potionEffect = potionEffects.get(effect);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_EFFECT, getTranslation(effect, potionEffect)));
                return potionEffects.size();
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Reference<StatusEffect>> setEffectNode = ClientCommandManager
            .argument("effect", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                StatusEffect effect = Util.getRegistryEntryArgument(context, "effect", RegistryKeys.STATUS_EFFECT);
                HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> potionEffects = EditHelper.getPotionEffects(stack);
                Triple<Integer, Integer, Boolean> potion = Triple.of(1, -1, true);
                if (potionEffects.containsKey(effect)) {
                    Triple<Integer, Integer, Boolean> potionEffect = potionEffects.get(effect);
                    if (potionEffect.equals(potion)) throw ALREADY_EXISTS_EXCEPTION;
                }
                potionEffects.put(effect, potion);
                EditHelper.setPotionEffects(stack, potionEffects);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(effect, potion)));
                return potionEffects.size();
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> setEffectLevelNode = ClientCommandManager
            .argument("level", IntegerArgumentType.integer(1, 256))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                StatusEffect effect = Util.getRegistryEntryArgument(context, "effect", RegistryKeys.STATUS_EFFECT);
                int level = IntegerArgumentType.getInteger(context, "level") - 1;
                HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> potionEffects = EditHelper.getPotionEffects(stack);
                Triple<Integer, Integer, Boolean> potion = Triple.of(level, -1, true);
                if (potionEffects.containsKey(effect)) {
                    Triple<Integer, Integer, Boolean> potionEffect = potionEffects.get(effect);
                    if (potionEffect.equals(potion)) throw ALREADY_EXISTS_EXCEPTION;
                }
                potionEffects.put(effect, potion);
                EditHelper.setPotionEffects(stack, potionEffects);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(effect, potion)));
                return potionEffects.size();
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Integer> setEffectLevelDurationNode = ClientCommandManager
            .argument("duration", IntegerArgumentType.integer(1))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                StatusEffect effect = Util.getRegistryEntryArgument(context, "effect", RegistryKeys.STATUS_EFFECT);
                int level = IntegerArgumentType.getInteger(context, "level") - 1;
                int duration = IntegerArgumentType.getInteger(context, "duration");
                HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> potionEffects = EditHelper.getPotionEffects(stack);
                Triple<Integer, Integer, Boolean> potion = Triple.of(level, duration, true);
                if (potionEffects.containsKey(effect)) {
                    Triple<Integer, Integer, Boolean> potionEffect = potionEffects.get(effect);
                    if (potionEffect.equals(potion)) throw ALREADY_EXISTS_EXCEPTION;
                }
                potionEffects.put(effect, potion);
                EditHelper.setPotionEffects(stack, potionEffects);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(effect, potion)));
                return potionEffects.size();
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Boolean> setEffectLevelDurationParticlesNode = ClientCommandManager
            .argument("particles", BoolArgumentType.bool())
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                StatusEffect effect = Util.getRegistryEntryArgument(context, "effect", RegistryKeys.STATUS_EFFECT);
                int level = IntegerArgumentType.getInteger(context, "level") - 1;
                int duration = IntegerArgumentType.getInteger(context, "duration");
                boolean particles = BoolArgumentType.getBool(context, "particles");
                HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> potionEffects = EditHelper.getPotionEffects(stack);
                Triple<Integer, Integer, Boolean> potion = Triple.of(level, duration, particles);
                if (potionEffects.containsKey(effect)) {
                    Triple<Integer, Integer, Boolean> potionEffect = potionEffects.get(effect);
                    if (potionEffect.equals(potion)) throw ALREADY_EXISTS_EXCEPTION;
                }
                potionEffects.put(effect, potion);
                EditHelper.setPotionEffects(stack, potionEffects);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(effect, potion)));
                return potionEffects.size();
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setEffectLevelInfinityNode = ClientCommandManager
            .literal("infinity")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                StatusEffect effect = Util.getRegistryEntryArgument(context, "effect", RegistryKeys.STATUS_EFFECT);
                int level = IntegerArgumentType.getInteger(context, "level") - 1;
                HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> potionEffects = EditHelper.getPotionEffects(stack);
                Triple<Integer, Integer, Boolean> potion = Triple.of(level, -1, true);
                if (potionEffects.containsKey(effect)) {
                    Triple<Integer, Integer, Boolean> potionEffect = potionEffects.get(effect);
                    if (potionEffect.equals(potion)) throw ALREADY_EXISTS_EXCEPTION;
                }
                potionEffects.put(effect, potion);
                EditHelper.setPotionEffects(stack, potionEffects);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, getTranslation(effect, potion)));
                return potionEffects.size();
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Boolean> setEffectLevelInfinityParticlesNode = ClientCommandManager
            .argument("particles", BoolArgumentType.bool())
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                StatusEffect effect = Util.getRegistryEntryArgument(context, "effect", RegistryKeys.STATUS_EFFECT);
                int level = IntegerArgumentType.getInteger(context, "level") - 1;
                boolean particles = BoolArgumentType.getBool(context, "particles");
                HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> potionEffects = EditHelper.getPotionEffects(stack);
                Triple<Integer, Integer, Boolean> potion = Triple.of(level, -1, particles);
                if (potionEffects.containsKey(effect)) {
                    Triple<Integer, Integer, Boolean> potionEffect = potionEffects.get(effect);
                    if (potionEffect.equals(potion)) throw ALREADY_EXISTS_EXCEPTION;
                }
                potionEffects.put(effect, potion);
                EditHelper.setPotionEffects(stack, potionEffects);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(effect, potion)));
                return potionEffects.size();
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
            .literal("remove")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Reference<StatusEffect>> removeEffectNode = ClientCommandManager
        .argument("effect", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!EditHelper.hasPotionEffects(stack, true)) throw NO_POTION_EFFECTS_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                StatusEffect effect = Util.getRegistryEntryArgument(context, "effect", RegistryKeys.STATUS_EFFECT);
                HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> potionEffects = EditHelper.getPotionEffects(stack);
                if (!potionEffects.containsKey(effect)) throw DOESNT_EXIST_EXCEPTION;
                potionEffects.remove(effect);
                EditHelper.setPotionEffects(stack, potionEffects);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, effect.getName()));
                return potionEffects.size();
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
            .literal("clear")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                if (!EditHelper.hasPotionEffects(stack)) throw NO_POTION_EFFECTS_EXCEPTION;
                int old = EditHelper.getPotionEffects(stack).size();
                EditHelper.setPotionEffects(stack, null);

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
                return old;
            })
            .build();
        
        rootNode.addChild(node);
        
        // ... get [<index>]
        node.addChild(getNode);
        getNode.addChild(getIndexNode);

        // ... set <effect> [<level>] [<duration>|infinity] [<particles>]
        node.addChild(setNode);
        setNode.addChild(setEffectNode);
        setEffectNode.addChild(setEffectLevelNode);
        setEffectLevelNode.addChild(setEffectLevelDurationNode);
        setEffectLevelDurationNode.addChild(setEffectLevelDurationParticlesNode);
        setEffectLevelNode.addChild(setEffectLevelInfinityNode);
        setEffectLevelInfinityNode.addChild(setEffectLevelInfinityParticlesNode);

        // ... remove <effect>
        node.addChild(removeNode);
        removeNode.addChild(removeEffectNode);

        // ... clear
        node.addChild(clearNode);
    }
}
