package me.white.itemeditor.editnodes;

import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class FlagsNode {
    private static final String OUTPUT_GET = "commands.edit.flags.get";
    private static final String OUTPUT_ENCHANTMENTS_GET = "commands.edit.flags.enchantmentsget";
    private static final String OUTPUT_ATTRIBUTES_GET = "commands.edit.flags.attributesget";
    private static final String OUTPUT_UNBREAKABLE_GET = "commands.edit.flags.unbreakableget";
    private static final String OUTPUT_CANDESTROY_GET = "commands.edit.flags.candestroyget";
    private static final String OUTPUT_CANPLACEON_GET = "commands.edit.flags.canplaceonget";
    private static final String OUTPUT_OTHERS_GET = "commands.edit.flags.othersget";
    private static final String OUTPUT_DYED_GET = "commands.edit.flags.dyedget";
    private static final String OUTPUT_TRIM_GET = "commands.edit.flags.trimget";
    private static final String OUTPUT_ENCHANTMENTS_SET = "commands.edit.flags.enchantmentsset";
    private static final String OUTPUT_ATTRIBUTES_SET = "commands.edit.flags.attributesset";
    private static final String OUTPUT_UNBREAKABLE_SET = "commands.edit.flags.unbreakableset";
    private static final String OUTPUT_CANDESTROY_SET = "commands.edit.flags.candestroyset";
    private static final String OUTPUT_CANPLACEON_SET = "commands.edit.flags.canplaceonset";
    private static final String OUTPUT_OTHERS_SET = "commands.edit.flags.othersset";
    private static final String OUTPUT_DYED_SET = "commands.edit.flags.dyedset";
    private static final String OUTPUT_TRIM_SET = "commands.edit.flags.trimset";
    private static final String HIDEFLAGS_KEY = "HideFlags";
    private static final int ENCHANTMENTS_MASK = 0b00000001;
    private static final int ATTRIBUTES_MASK   = 0b00000010;
    private static final int UNBREAKABLE_MASK  = 0b00000100;
    private static final int CANDESTROY_MASK   = 0b00001000;
    private static final int CANPLACEON_MASK   = 0b00010000;
    private static final int OTHERS_MASK       = 0b00100000;
    private static final int DYED_MASK         = 0b01000000;
    private static final int TRIM_MASK         = 0b10000000;

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("flags")
            .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean enchantments = (hideflags & ENCHANTMENTS_MASK) == ENCHANTMENTS_MASK;
                boolean attributes = (hideflags & ATTRIBUTES_MASK) == ATTRIBUTES_MASK;
                boolean unbreakable = (hideflags & UNBREAKABLE_MASK) == UNBREAKABLE_MASK;
                boolean candestroy = (hideflags & CANDESTROY_MASK) == CANDESTROY_MASK;
                boolean canplaceon = (hideflags & CANPLACEON_MASK) == CANPLACEON_MASK;
                boolean others = (hideflags & OTHERS_MASK) == OTHERS_MASK;
                boolean dyed = (hideflags & DYED_MASK) == DYED_MASK;
                boolean trim = (hideflags & TRIM_MASK) == TRIM_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ENCHANTMENTS_GET, enchantments));
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ATTRIBUTES_GET, attributes));
                context.getSource().sendFeedback(Text.translatable(OUTPUT_UNBREAKABLE_GET, unbreakable));
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CANDESTROY_GET, candestroy));
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CANPLACEON_GET, canplaceon));
                context.getSource().sendFeedback(Text.translatable(OUTPUT_OTHERS_GET, others));
                context.getSource().sendFeedback(Text.translatable(OUTPUT_DYED_GET, dyed));
                context.getSource().sendFeedback(Text.translatable(OUTPUT_TRIM_GET, trim));
                return hideflags;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getEnchantmentsNode = ClientCommandManager
            .literal("enchantments")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean enchantments = (hideflags & ENCHANTMENTS_MASK) == ENCHANTMENTS_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ENCHANTMENTS_GET, enchantments));
                return enchantments ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getAttributesNode = ClientCommandManager
            .literal("attributes")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean attributes = (hideflags & ATTRIBUTES_MASK) == ATTRIBUTES_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ATTRIBUTES_GET, attributes));
                return attributes ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getUnbreakableNode = ClientCommandManager
            .literal("unbreakable")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean unbreakable = (hideflags & UNBREAKABLE_MASK) == UNBREAKABLE_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_UNBREAKABLE_GET, unbreakable));
                return unbreakable ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getCandestroyNode = ClientCommandManager
            .literal("candestroy")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean candestroy = (hideflags & CANDESTROY_MASK) == CANDESTROY_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CANDESTROY_GET, candestroy));
                return candestroy ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getCanplaceonNode = ClientCommandManager
            .literal("canplaceon")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean canplaceon = (hideflags & CANPLACEON_MASK) == CANPLACEON_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CANPLACEON_GET, canplaceon));
                return canplaceon ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getOthersNode = ClientCommandManager
            .literal("others")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean others = (hideflags & OTHERS_MASK) == OTHERS_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_OTHERS_GET, others));
                return others ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getDyedNode = ClientCommandManager
            .literal("dyed")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean dyed = (hideflags & DYED_MASK) == DYED_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_DYED_GET, dyed));
                return dyed ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getTrimNode = ClientCommandManager
            .literal("trim")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean trim = (hideflags & TRIM_MASK) == TRIM_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_TRIM_GET, trim));
                return trim ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setEnchantmentsNode = ClientCommandManager
            .literal("enchantments")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean enchantments = (hideflags & ENCHANTMENTS_MASK) == ENCHANTMENTS_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ ENCHANTMENTS_MASK);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ENCHANTMENTS_SET, !enchantments));
                return enchantments ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setAttributesNode = ClientCommandManager
            .literal("attributes")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean attributes = (hideflags & ATTRIBUTES_MASK) == ATTRIBUTES_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ ATTRIBUTES_MASK);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_ATTRIBUTES_SET, !attributes));
                return attributes ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setUnbreakableNode = ClientCommandManager
            .literal("unbreakable")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean unbreakable = (hideflags & UNBREAKABLE_MASK) == UNBREAKABLE_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ UNBREAKABLE_MASK);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_UNBREAKABLE_SET, !unbreakable));
                return unbreakable ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setCandestroyNode = ClientCommandManager
            .literal("candestroy")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean candestroy = (hideflags & CANDESTROY_MASK) == CANDESTROY_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ CANDESTROY_MASK);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CANDESTROY_SET, !candestroy));
                return candestroy ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setCanplaceonNode = ClientCommandManager
            .literal("canplaceon")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean canplaceon = (hideflags & CANPLACEON_MASK) == CANPLACEON_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ CANPLACEON_MASK);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_CANPLACEON_SET, !canplaceon));
                return canplaceon ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setOthersNode = ClientCommandManager
            .literal("others")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean others = (hideflags & OTHERS_MASK) == OTHERS_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ OTHERS_MASK);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_OTHERS_SET, !others));
                return others ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setDyedNode = ClientCommandManager
            .literal("dyed")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean dyed = (hideflags & DYED_MASK) == DYED_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ DYED_MASK);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_DYED_SET, !dyed));
                return dyed ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setTrimNode = ClientCommandManager
            .literal("trim")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) {
                    hideflags = nbt.getInt(HIDEFLAGS_KEY);
                }
                boolean trim = (hideflags & TRIM_MASK) == TRIM_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ TRIM_MASK);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_TRIM_SET, !trim));
                return trim ? 1 : 0;
            })
            .build();
        
        rootNode.addChild(node);

        // ... get [enchantments|attributes|unbreakable|candestroy|canplaceon|others|dyed|trim]
        node.addChild(getNode);
        getNode.addChild(getEnchantmentsNode);
        getNode.addChild(getAttributesNode);
        getNode.addChild(getUnbreakableNode);
        getNode.addChild(getCandestroyNode);
        getNode.addChild(getCanplaceonNode);
        getNode.addChild(getOthersNode);
        getNode.addChild(getDyedNode);
        getNode.addChild(getTrimNode);

        // ... set enchantments|attributes|unbreakable|candestroy|canplaceon|others|dyed|trim
        node.addChild(setNode);
        setNode.addChild(setEnchantmentsNode);
        setNode.addChild(setAttributesNode);
        setNode.addChild(setUnbreakableNode);
        setNode.addChild(setCandestroyNode);
        setNode.addChild(setCanplaceonNode);
        setNode.addChild(setOthersNode);
        setNode.addChild(setDyedNode);
        setNode.addChild(setTrimNode);
    }
}
