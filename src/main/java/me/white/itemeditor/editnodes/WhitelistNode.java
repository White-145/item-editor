package me.white.itemeditor.editnodes;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.block.Block;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WhitelistNode {
	public static final CommandSyntaxException NO_WHITELIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.whitelist.nowhitelist")).create();
	public static final CommandSyntaxException ENTRY_EXISTS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.whitelist.entryexists")).create();
	public static final CommandSyntaxException DOESNT_EXIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.whitelist.doesntexist")).create();
	public static final CommandSyntaxException NO_SUCH_WHITELIST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.whitelist.nosuchwhitelist")).create();
    private static final String OUTPUT_GET_PLACE = "commands.edit.whitelist.getplace";
    private static final String OUTPUT_GET_DESTROY = "commands.edit.whitelist.getdestroy";
    private static final String OUTPUT_CLEAR = "commands.edit.whitelist.clear";
    private static final String OUTPUT_CLEAR_PLACE = "commands.edit.whitelist.clearplace";
    private static final String OUTPUT_CLEAR_DESTROY = "commands.edit.whitelist.cleardestroy";
    private static final String OUTPUT_ADD = "commands.edit.whitelist.add";
    private static final String OUTPUT_ADD_PLACE = "commands.edit.whitelist.addplace";
    private static final String OUTPUT_ADD_DESTROY = "commands.edit.whitelist.adddestroy";
    private static final String OUTPUT_REMOVE = "commands.edit.whitelist.remove";
    private static final String OUTPUT_REMOVE_PLACE = "commands.edit.whitelist.removeplace";
    private static final String OUTPUT_REMOVE_DESTROY = "commands.edit.whitelist.removedestroy";
    private static final String PLACE_KEY = "CanPlaceOn";
    private static final String DESTROY_KEY = "CanDestroy";

    private static Block getBlockArgument(CommandContext<FabricClientCommandSource> context, String key) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.isOf(RegistryKeys.BLOCK)) {
            throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), RegistryKeys.BLOCK.getValue());
        }
		return (Block)reference.value();
	}

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("whitelist")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                if (!item.hasNbt()) throw NO_WHITELIST_EXCEPTION;
                NbtCompound nbt = item.getNbt();
                NbtList place = nbt.getList(PLACE_KEY, NbtElement.STRING_TYPE);
                NbtList destroy = nbt.getList(DESTROY_KEY, NbtElement.STRING_TYPE);
                if ((place == null || place.isEmpty()) && (destroy == null || place.isEmpty())) throw NO_WHITELIST_EXCEPTION;
                if (place != null) {
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_PLACE));
                    for (NbtElement entry : place) {
                        Identifier id = Identifier.tryParse(((NbtString)entry).toString());
                        if (id == null) continue;
                        Block block = Registries.BLOCK.get(id);
                        if (block == null) continue;
                        context.getSource().sendFeedback(block.getName());
                    }
                }
                if (destroy != null) {
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_DESTROY));
                    for (NbtElement entry : destroy) {
                        Identifier id = Identifier.tryParse(((NbtString)entry).toString());
                        if (id == null) continue;
                        Block block = Registries.BLOCK.get(id);
                        if (block == null) continue;
                        context.getSource().sendFeedback(block.getName());
                    }
                }
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getPlaceNode = ClientCommandManager
            .literal("place")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                if (!item.hasNbt()) throw NO_WHITELIST_EXCEPTION;
                NbtCompound nbt = item.getNbt();
                NbtList place = nbt.getList(PLACE_KEY, NbtElement.STRING_TYPE);
                if (place == null || place.isEmpty()) throw NO_SUCH_WHITELIST_EXCEPTION;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_PLACE));
                for (NbtElement entry : place) {
                    Identifier id = Identifier.tryParse(((NbtString)entry).toString());
                    if (id == null) continue;
                    Block block = Registries.BLOCK.get(id);
                    if (block == null) continue;
                    context.getSource().sendFeedback(block.getName());
                }
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getDestroyNode = ClientCommandManager
            .literal("destroy")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                if (!item.hasNbt()) throw NO_WHITELIST_EXCEPTION;
                NbtCompound nbt = item.getNbt();
                NbtList destroy = nbt.getList(DESTROY_KEY, NbtElement.STRING_TYPE);
                if (destroy == null || destroy.isEmpty()) throw NO_SUCH_WHITELIST_EXCEPTION;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_DESTROY));
                for (NbtElement entry : destroy) {
                    Identifier id = Identifier.tryParse(((NbtString)entry).toString());
                    if (id == null) continue;
                    Block block = Registries.BLOCK.get(id);
                    if (block == null) continue;
                    context.getSource().sendFeedback(block.getName());
                }
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> addNode = ClientCommandManager
            .literal("add")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> addBlockNode = ClientCommandManager
            .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, Registries.BLOCK.getKey()))
            .executes(context -> {
                Block block = getBlockArgument(context, "block");
                NbtString id = NbtString.of(Registries.BLOCK.getId(block).toString());
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                if (nbt == null) nbt = new NbtCompound();
                NbtList place = nbt.getList(PLACE_KEY, NbtElement.STRING_TYPE);
                NbtList destroy = nbt.getList(DESTROY_KEY, NbtElement.STRING_TYPE);
                if (place == null) place = new NbtList();
                if (destroy == null) destroy = new NbtList();
                if (place.contains(id) && destroy.contains(id)) throw ENTRY_EXISTS_EXCEPTION;
                if (!place.contains(id)) place.add(id);
                if (!destroy.contains(id)) destroy.add(id);
                nbt.put(PLACE_KEY, place);
                nbt.put(DESTROY_KEY, destroy);
                item.setNbt(nbt);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD, block.getName()));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> addPlaceNode = ClientCommandManager
            .literal("place")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> addPlaceBlockNode = ClientCommandManager
            .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, Registries.BLOCK.getKey()))
            .executes(context -> {
                Block block = getBlockArgument(context, "block");
                NbtString id = NbtString.of(Registries.BLOCK.getId(block).toString());
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                if (nbt == null) nbt = new NbtCompound();
                NbtList place = nbt.getList(PLACE_KEY, NbtElement.STRING_TYPE);
                if (place == null) place = new NbtList();
                if (place.contains(id)) throw ENTRY_EXISTS_EXCEPTION;
                place.add(id);
                nbt.put(PLACE_KEY, place);
                item.setNbt(nbt);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD_PLACE, block.getName()));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> addDestroyNode = ClientCommandManager
            .literal("destroy")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> addDestroyBlockNode = ClientCommandManager
            .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, Registries.BLOCK.getKey()))
            .executes(context -> {
                Block block = getBlockArgument(context, "block");
                NbtString id = NbtString.of(Registries.BLOCK.getId(block).toString());
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                if (nbt == null) nbt = new NbtCompound();
                NbtList destroy = nbt.getList(DESTROY_KEY, NbtElement.STRING_TYPE);
                if (destroy == null) destroy = new NbtList();
                if (destroy.contains(id)) throw ENTRY_EXISTS_EXCEPTION;
                destroy.add(id);
                nbt.put(DESTROY_KEY, destroy);
                item.setNbt(nbt);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ADD_DESTROY, block.getName()));
                return 1;
            })
            .build();
    
        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
            .literal("remove")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> removeBlockNode = ClientCommandManager
            .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, Registries.BLOCK.getKey()))
            .executes(context -> {
                Block block = getBlockArgument(context, "block");
                NbtString id = NbtString.of(Registries.BLOCK.getId(block).toString());
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                if (nbt == null) throw NO_WHITELIST_EXCEPTION;
                NbtList place = nbt.getList(PLACE_KEY, NbtElement.STRING_TYPE);
                NbtList destroy = nbt.getList(DESTROY_KEY, NbtElement.STRING_TYPE);
                if ((place == null || place.isEmpty()) && (destroy == null || place.isEmpty())) throw NO_WHITELIST_EXCEPTION;
                if ((place == null || !place.contains(id)) && (destroy == null || !destroy.contains(id))) throw DOESNT_EXIST_EXCEPTION;
                if (place != null) {
                    place.remove(id);
                    nbt.put(PLACE_KEY, place);
                }
                if (destroy != null) {
                    destroy.remove(id);
                    nbt.put(DESTROY_KEY, destroy);
                }
                item.setNbt(nbt);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE, block.getName()));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> removePlaceNode = ClientCommandManager
            .literal("place")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> removePlaceBlockNode = ClientCommandManager
            .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, Registries.BLOCK.getKey()))
            .executes(context -> {
                Block block = getBlockArgument(context, "block");
                NbtString id = NbtString.of(Registries.BLOCK.getId(block).toString());
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                if (nbt == null) throw NO_WHITELIST_EXCEPTION;
                NbtList place = nbt.getList(PLACE_KEY, NbtElement.STRING_TYPE);
                if (place == null || place.isEmpty()) throw NO_SUCH_WHITELIST_EXCEPTION;
                if (!place.contains(id)) throw DOESNT_EXIST_EXCEPTION;
                place.remove(id);
                nbt.put(PLACE_KEY, place);
                item.setNbt(nbt);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE_PLACE, block.getName()));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> removeDestroyNode = ClientCommandManager
            .literal("destroy")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<Block>> removeDestroyBlockNode = ClientCommandManager
            .argument("block", RegistryEntryArgumentType.registryEntry(registryAccess, Registries.BLOCK.getKey()))
            .executes(context -> {
                Block block = getBlockArgument(context, "block");
                NbtString id = NbtString.of(Registries.BLOCK.getId(block).toString());
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                if (nbt == null) throw NO_WHITELIST_EXCEPTION;
                NbtList destroy = nbt.getList(DESTROY_KEY, NbtElement.STRING_TYPE);
                if (destroy == null || destroy.isEmpty()) throw NO_SUCH_WHITELIST_EXCEPTION;
                if (!destroy.contains(id)) throw DOESNT_EXIST_EXCEPTION;
                destroy.remove(id);
                nbt.put(PLACE_KEY, destroy);
                item.setNbt(nbt);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE_DESTROY, block.getName()));
                return 1;
            })
            .build();
    
        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
            .literal("clear")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                if (!item.hasNbt()) throw NO_WHITELIST_EXCEPTION;
                NbtCompound nbt = item.getNbt();
                NbtList destroy = nbt.getList(DESTROY_KEY, NbtElement.STRING_TYPE);
                NbtList place = nbt.getList(PLACE_KEY, NbtElement.STRING_TYPE);
                if ((place == null || place.isEmpty()) && (destroy == null || destroy.isEmpty())) throw NO_WHITELIST_EXCEPTION;
                nbt.remove(DESTROY_KEY);
                nbt.remove(PLACE_KEY);
                item.setNbt(nbt);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> clearPlaceNode = ClientCommandManager
            .literal("place")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                if (!item.hasNbt()) throw NO_WHITELIST_EXCEPTION;
                NbtCompound nbt = item.getNbt();
                NbtList place = nbt.getList(PLACE_KEY, NbtElement.STRING_TYPE);
                if (place == null || place.isEmpty()) throw NO_SUCH_WHITELIST_EXCEPTION;
                nbt.remove(PLACE_KEY);
                item.setNbt(nbt);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_PLACE));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> clearDestroyNode = ClientCommandManager
            .literal("destroy")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                if (!item.hasNbt()) throw NO_WHITELIST_EXCEPTION;
                NbtCompound nbt = item.getNbt();
                NbtList destroy = nbt.getList(DESTROY_KEY, NbtElement.STRING_TYPE);
                if (destroy == null || destroy.isEmpty()) throw NO_SUCH_WHITELIST_EXCEPTION;
                nbt.remove(DESTROY_KEY);
                item.setNbt(nbt);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR_DESTROY));
                return 1;
            })
            .build();
        
        rootNode.addChild(node);

        // ... get ...
        node.addChild(getNode);
        getNode.addChild(getPlaceNode);
        getNode.addChild(getDestroyNode);

        // ... add ...
        node.addChild(addNode);
        addNode.addChild(addBlockNode);
        addNode.addChild(addPlaceNode);
        addPlaceNode.addChild(addPlaceBlockNode);
        addNode.addChild(addDestroyNode);
        addDestroyNode.addChild(addDestroyBlockNode);

        // ... remove ...
        node.addChild(removeNode);
        removeNode.addChild(removeBlockNode);
        removeNode.addChild(removePlaceNode);
        removePlaceNode.addChild(removePlaceBlockNode);
        removeNode.addChild(removeDestroyNode);
        removeDestroyNode.addChild(removeDestroyBlockNode);

        // ... clear ...
        node.addChild(clearNode);
        clearNode.addChild(clearPlaceNode);
        clearNode.addChild(clearDestroyNode);
    }
}
