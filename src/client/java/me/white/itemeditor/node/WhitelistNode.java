package me.white.itemeditor.node;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.ItemUtil;
import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WhitelistNode implements Node {
    public static final CommandSyntaxException NO_WHITELIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.whitelist.error.nowhitelist")).create();
    public static final CommandSyntaxException ALREADY_EXISTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.whitelist.error.alreadyexists")).create();
    public static final CommandSyntaxException DOESNT_EXIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.whitelist.error.doesntexist")).create();
    public static final CommandSyntaxException NO_SUCH_WHITELIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.whitelist.error.nosuchwhitelist")).create();
    private static final String OUTPUT_GET_PLACE = "commands.edit.whitelist.getplace";
    private static final String OUTPUT_GET_DESTROY = "commands.edit.whitelist.getdestroy";
    private static final String OUTPUT_ADD_PLACE = "commands.edit.whitelist.addplace";
    private static final String OUTPUT_ADD_DESTROY = "commands.edit.whitelist.adddestroy";
    private static final String OUTPUT_REMOVE_PLACE = "commands.edit.whitelist.removeplace";
    private static final String OUTPUT_REMOVE_DESTROY = "commands.edit.whitelist.removedestroy";
    private static final String OUTPUT_CLEAR = "commands.edit.whitelist.clear";
    private static final String OUTPUT_CLEAR_PLACE = "commands.edit.whitelist.clearplace";
    private static final String OUTPUT_CLEAR_DESTROY = "commands.edit.whitelist.cleardestroy";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("whitelist")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasWhitelistPlace(stack) && !ItemUtil.hasWhitelistDestroy(stack)) throw NO_WHITELIST_EXCEPTION;
                    List<Block> place = ItemUtil.getWhitelistPlace(stack);
                    List<Block> destroy = ItemUtil.getWhitelistDestroy(stack);

                    if (!place.isEmpty()) {
                        context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_PLACE));
                        for (Block block : place) {
                            context.getSource().sendFeedback(Text.empty()
                                    .append(Text.literal("- ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
                                    .append(block.getName())
                            );
                        }
                    }
                    if (!destroy.isEmpty()) {
                        context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_DESTROY));
                        for (Block block : destroy) {
                            context.getSource().sendFeedback(Text.empty()
                                    .append(Text.literal("- ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
                                    .append(block.getName())
                            );
                        }
                    }
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> getPlaceNode = ClientCommandManager
                .literal("place")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasWhitelistPlace(stack)) throw NO_SUCH_WHITELIST_EXCEPTION;
                    List<Block> place = ItemUtil.getWhitelistPlace(stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_PLACE));
                    for (Block block : place) {
                        context.getSource().sendFeedback(Text.empty()
                                .append(Text.literal("- ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
                                .append(block.getName())
                        );
                    }
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> getDestroyNode = ClientCommandManager
                .literal("destroy")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasWhitelistDestroy(stack)) throw NO_SUCH_WHITELIST_EXCEPTION;
                    List<Block> destroy = ItemUtil.getWhitelistDestroy(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_DESTROY));
                    for (Block block : destroy) {
                        context.getSource().sendFeedback(Text.empty()
                                .append(Text.literal("- ").setStyle(Style.EMPTY.withFormatting(Formatting.GRAY)))
                                .append(block.getName())
                        );
                    }
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
                .literal("add")
                .build();

        LiteralCommandNode<FabricClientCommandSource> addPlaceNode = ClientCommandManager
                .literal("place")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> addPlaceBlockNode = ClientCommandManager
                .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BLOCK))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    Block block = EditorUtil.getRegistryEntryArgument(context, "block", RegistryKeys.BLOCK);
                    List<Block> place = new ArrayList<>(ItemUtil.getWhitelistPlace(stack));
                    if (place.contains(block)) throw ALREADY_EXISTS_EXCEPTION;
                    place.add(block);
                    ItemUtil.setWhitelistPlace(stack, place);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD_PLACE, block.getName()));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> addDestroyNode = ClientCommandManager
                .literal("destroy")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> addDestroyBlockNode = ClientCommandManager
                .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BLOCK))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    Block block = EditorUtil.getRegistryEntryArgument(context, "block", RegistryKeys.BLOCK);
                    List<Block> destroy = new ArrayList<>(ItemUtil.getWhitelistDestroy(stack));
                    if (destroy.contains(block)) throw ALREADY_EXISTS_EXCEPTION;
                    destroy.add(block);
                    ItemUtil.setWhitelistDestroy(stack, destroy);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD_DESTROY, block.getName()));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .build();

        LiteralCommandNode<FabricClientCommandSource> removePlaceNode = ClientCommandManager
                .literal("place")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> removePlaceBlockNode = ClientCommandManager
                .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BLOCK))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasWhitelistPlace(stack)) throw NO_SUCH_WHITELIST_EXCEPTION;
                    Block block = EditorUtil.getRegistryEntryArgument(context, "block", RegistryKeys.BLOCK);
                    List<Block> place = new ArrayList<>(ItemUtil.getWhitelistPlace(stack));
                    if (!place.contains(block)) throw DOESNT_EXIST_EXCEPTION;
                    place.remove(block);
                    ItemUtil.setWhitelistPlace(stack, place);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE_PLACE, block.getName()));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> removeDestroyNode = ClientCommandManager
                .literal("destroy")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> removeDestroyBlockNode = ClientCommandManager
                .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.BLOCK))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasWhitelistDestroy(stack)) throw NO_SUCH_WHITELIST_EXCEPTION;
                    Block block = EditorUtil.getRegistryEntryArgument(context, "block", RegistryKeys.BLOCK);
                    List<Block> destroy = new ArrayList<>(ItemUtil.getWhitelistDestroy(stack));
                    if (!destroy.contains(block)) throw DOESNT_EXIST_EXCEPTION;
                    destroy.remove(block);
                    ItemUtil.setWhitelistDestroy(stack, destroy);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE_DESTROY, block.getName()));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
                .literal("clear")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasWhitelistPlace(stack, false) && !ItemUtil.hasWhitelistDestroy(stack, false)) throw NO_WHITELIST_EXCEPTION;
                    ItemUtil.setWhitelistPlace(stack, null);
                    ItemUtil.setWhitelistDestroy(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearPlaceNode = ClientCommandManager
                .literal("place")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasWhitelistPlace(stack)) throw NO_SUCH_WHITELIST_EXCEPTION;
                    ItemUtil.setWhitelistPlace(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_PLACE));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearDestroyNode = ClientCommandManager
                .literal("destroy")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasWhitelistDestroy(stack)) throw NO_SUCH_WHITELIST_EXCEPTION;
                    ItemUtil.setWhitelistDestroy(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_DESTROY));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get ...
        node.addChild(getNode);
        // ... place
        getNode.addChild(getPlaceNode);
        // ... destroy
        getNode.addChild(getDestroyNode);

        // ... add ...
        node.addChild(addNode);
        // ... place <block>
        addNode.addChild(addPlaceNode);
        addPlaceNode.addChild(addPlaceBlockNode);
        // ... destroy <block>
        addNode.addChild(addDestroyNode);
        addDestroyNode.addChild(addDestroyBlockNode);

        // ... remove ...
        node.addChild(removeNode);
        // ... place <block>
        removeNode.addChild(removePlaceNode);
        removePlaceNode.addChild(removePlaceBlockNode);
        // ... destroy <block>
        removeNode.addChild(removeDestroyNode);
        removeDestroyNode.addChild(removeDestroyBlockNode);

        // ... clear ...
        node.addChild(clearNode);
        // ... place
        clearNode.addChild(clearPlaceNode);
        // ... destroy
        clearNode.addChild(clearDestroyNode);
    }
}
