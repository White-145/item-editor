package me.white.simpleitemeditor.node;

import java.util.HashMap;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EnchantmentNode implements Node {
    public static final CommandSyntaxException ALREADY_EXISTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.alreadyexists")).create();
    public static final CommandSyntaxException DOESNT_EXIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.doesntexist")).create();
    public static final CommandSyntaxException NO_ENCHANTMENTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.noenchantments")).create();
    public static final CommandSyntaxException HAS_GLINT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.enchantment.error.hasglint")).create();
    private static final String OUTPUT_GET = "commands.edit.enchantment.get";
    private static final String OUTPUT_GET_ENCHANTMENT = "commands.edit.enchantment.getenchantment";
    private static final String OUTPUT_SET = "commands.edit.enchantment.set";
    private static final String OUTPUT_REMOVE = "commands.edit.enchantment.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.enchantment.clear";
    private static final String OUTPUT_GLINT_ENABLE = "commands.edit.enchantment.glintenable";
    private static final String OUTPUT_GLINT_DISABLE = "commands.edit.enchantment.glintdisable";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("enchantment")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasEnchantments(stack)) throw NO_ENCHANTMENTS_EXCEPTION;

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                    HashMap<Enchantment, Integer> enchantments = ItemUtil.getEnchantments(stack);
                    for (Enchantment enchantment : enchantments.keySet()) {
                        context.getSource().sendFeedback(Text.empty()
                                .append(Text.literal("- ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                .append(enchantment.getName(enchantments.get(enchantment)))
                        );
                    }
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> getEnchantmentNode = ClientCommandManager
                .argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasEnchantments(stack)) throw NO_ENCHANTMENTS_EXCEPTION;
                    Enchantment enchantment = EditorUtil.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
                    HashMap<Enchantment, Integer> enchantments = ItemUtil.getEnchantments(stack);
                    if (!enchantments.containsKey(enchantment)) throw DOESNT_EXIST_EXCEPTION;

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_ENCHANTMENT, enchantment.getName(enchantments.get(enchantment))));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> setEnchantmentNode = ClientCommandManager
                .argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    Enchantment enchantment = EditorUtil.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
                    HashMap<Enchantment, Integer> enchantments = ItemUtil.getEnchantments(stack);
                    int old = enchantments.getOrDefault(enchantment, -1);
                    if (old == 1) throw ALREADY_EXISTS_EXCEPTION;
                    enchantments.put(enchantment, 1);
                    ItemUtil.setEnchantments(stack, enchantments);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, enchantment.getName(1)));
                    return old;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setEnchantmentLevelNode = ClientCommandManager
                .argument("level", IntegerArgumentType.integer(0, 255))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    Enchantment enchantment = EditorUtil.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
                    HashMap<Enchantment, Integer> enchantments = ItemUtil.getEnchantments(stack);
                    int level = IntegerArgumentType.getInteger(context, "level");
                    int old = enchantments.getOrDefault(enchantment, -1);
                    if (old == level) throw ALREADY_EXISTS_EXCEPTION;
                    enchantments.put(enchantment, level);
                    ItemUtil.setEnchantments(stack, enchantments);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, enchantment.getName(level)));
                    return old;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Enchantment>> removeEnchantmentNode = ClientCommandManager
                .argument("enchantment", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.ENCHANTMENT))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!ItemUtil.hasEnchantments(stack)) throw NO_ENCHANTMENTS_EXCEPTION;
                    Enchantment enchantment = EditorUtil.getRegistryEntryArgument(context, "enchantment", RegistryKeys.ENCHANTMENT);
                    HashMap<Enchantment, Integer> enchantments = ItemUtil.getEnchantments(stack);
                    if (!enchantments.containsKey(enchantment)) throw DOESNT_EXIST_EXCEPTION;
                    int old = enchantments.get(enchantment);
                    enchantments.remove(enchantment);
                    ItemUtil.setEnchantments(stack, enchantments);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, Text.translatable(enchantment.getTranslationKey())));
                    return old;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
                .literal("clear")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!ItemUtil.hasEnchantments(stack, false)) throw NO_ENCHANTMENTS_EXCEPTION;
                    int old = ItemUtil.getEnchantments(stack).size();
                    ItemUtil.setEnchantments(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
                    return old;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> glintNode = ClientCommandManager
                .literal("glint")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (ItemUtil.hasEnchantments(stack)) throw HAS_GLINT_EXCEPTION;
                    boolean hasGlint = ItemUtil.hasEnchantments(stack, false);
                    ItemUtil.setEnchantmentGlint(stack, !hasGlint);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(hasGlint ? OUTPUT_GLINT_DISABLE : OUTPUT_GLINT_ENABLE));
                    return 1;
                })
                .build();

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

        // ... clear
        node.addChild(clearNode);

        // ... glint
        node.addChild(glintNode);
    }
}
