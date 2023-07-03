package me.white.itemeditor.editnodes;

import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class HideFlagsNode {
    private static final String OUTPUT_ENCHANTMENTS_GET = "commands.edit.hideflags.enchantmentsget";
    private static final String OUTPUT_ATTRIBUTES_GET = "commands.edit.hideflags.attributesget";
    private static final String OUTPUT_UNBREAKABLE_GET = "commands.edit.hideflags.unbreakableget";
    private static final String OUTPUT_CANDESTROY_GET = "commands.edit.hideflags.candestroyget";
    private static final String OUTPUT_CANPLACEON_GET = "commands.edit.hideflags.canplaceonget";
    private static final String OUTPUT_OTHERS_GET = "commands.edit.hideflags.othersget";
    private static final String OUTPUT_DYED_GET = "commands.edit.hideflags.dyedget";
    private static final String OUTPUT_TRIM_GET = "commands.edit.hideflags.trimget";
    private static final String HIDEFLAGS_KEY = "HideFlags";
    private static final int ENCHANTMENTS_MASK = 0b00000001;
    private static final int ATTRIBUTES_MASK   = 0b00000010;
    private static final int UNBREAKABLE_MASK  = 0b00000100;
    private static final int CANDESTROY_MASK   = 0b00001000;
    private static final int CANPLACEON_MASK   = 0b00010000;
    private static final int OTHERS_MASK       = 0b00100000;
    private static final int DYED_MASK         = 0b01000000;
    private static final int TRIM_MASK         = 0b10000000;

    public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
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
                ClientPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.translatable(OUTPUT_ENCHANTMENTS_GET, enchantments));
                player.sendMessage(Text.translatable(OUTPUT_ATTRIBUTES_GET, attributes));
                player.sendMessage(Text.translatable(OUTPUT_UNBREAKABLE_GET, unbreakable));
                player.sendMessage(Text.translatable(OUTPUT_CANDESTROY_GET, candestroy));
                player.sendMessage(Text.translatable(OUTPUT_CANPLACEON_GET, canplaceon));
                player.sendMessage(Text.translatable(OUTPUT_OTHERS_GET, others));
                player.sendMessage(Text.translatable(OUTPUT_DYED_GET, dyed));
                player.sendMessage(Text.translatable(OUTPUT_TRIM_GET, trim));
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
                ClientPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.translatable(OUTPUT_ENCHANTMENTS_GET, enchantments));
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
                ClientPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.translatable(OUTPUT_ATTRIBUTES_GET, attributes));
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
                ClientPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.translatable(OUTPUT_UNBREAKABLE_GET, unbreakable));
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
                ClientPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.translatable(OUTPUT_CANDESTROY_GET, candestroy));
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
                ClientPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.translatable(OUTPUT_CANPLACEON_GET, canplaceon));
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
                ClientPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.translatable(OUTPUT_OTHERS_GET, others));
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
                ClientPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.translatable(OUTPUT_DYED_GET, dyed));
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
                ClientPlayerEntity player = context.getSource().getPlayer();
                player.sendMessage(Text.translatable(OUTPUT_TRIM_GET, trim));
                return trim ? 1 : 0;
            })
            .build();

        node.addChild(getNode);
        getNode.addChild(getEnchantmentsNode);
        getNode.addChild(getAttributesNode);
        getNode.addChild(getUnbreakableNode);
        getNode.addChild(getCandestroyNode);
        getNode.addChild(getCanplaceonNode);
        getNode.addChild(getOthersNode);
        getNode.addChild(getDyedNode);
        getNode.addChild(getTrimNode);
    }
}
