package me.white.itemeditor.node;

import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class FlagsNode {
    private static final String OUTPUT_GET = "commands.edit.flags.get";
    private static final String OUTPUT_ENCHANTMENTS_GET_ENABLED = "commands.edit.flags.enchantmentsgetenabled";
    private static final String OUTPUT_ATTRIBUTES_GET_ENABLED = "commands.edit.flags.attributesgetenabled";
    private static final String OUTPUT_UNBREAKABLE_GET_ENABLED = "commands.edit.flags.unbreakablegetenabled";
    private static final String OUTPUT_CANDESTROY_GET_ENABLED = "commands.edit.flags.candestroygetenabled";
    private static final String OUTPUT_CANPLACEON_GET_ENABLED = "commands.edit.flags.canplaceongetenabled";
    private static final String OUTPUT_OTHERS_GET_ENABLED = "commands.edit.flags.othersgetenabled";
    private static final String OUTPUT_DYED_GET_ENABLED = "commands.edit.flags.dyedgetenabled";
    private static final String OUTPUT_TRIM_GET_ENABLED = "commands.edit.flags.trimgetenabled";
    private static final String OUTPUT_ENCHANTMENTS_GET_DISABLED = "commands.edit.flags.enchantmentsgetdisabled";
    private static final String OUTPUT_ATTRIBUTES_GET_DISABLED = "commands.edit.flags.attributesgetdisabled";
    private static final String OUTPUT_UNBREAKABLE_GET_DISABLED = "commands.edit.flags.unbreakablegetdisabled";
    private static final String OUTPUT_CANDESTROY_GET_DISABLED = "commands.edit.flags.candestroygetdisabled";
    private static final String OUTPUT_CANPLACEON_GET_DISABLED = "commands.edit.flags.canplaceongetdisabled";
    private static final String OUTPUT_OTHERS_GET_DISABLED = "commands.edit.flags.othersgetdisabled";
    private static final String OUTPUT_DYED_GET_DISABLED = "commands.edit.flags.dyedgetdisabled";
    private static final String OUTPUT_TRIM_GET_DISABLED = "commands.edit.flags.trimgetdisabled";
    private static final String OUTPUT_ENCHANTMENTS_ENABLE = "commands.edit.flags.enchantmentsenable";
    private static final String OUTPUT_ATTRIBUTES_ENABLE = "commands.edit.flags.attributesenable";
    private static final String OUTPUT_UNBREAKABLE_ENABLE = "commands.edit.flags.unbreakableenable";
    private static final String OUTPUT_CANDESTROY_ENABLE = "commands.edit.flags.candestroyenable";
    private static final String OUTPUT_CANPLACEON_ENABLE = "commands.edit.flags.canplaceonenable";
    private static final String OUTPUT_OTHERS_ENABLE = "commands.edit.flags.othersenable";
    private static final String OUTPUT_DYED_ENABLE = "commands.edit.flags.dyedenable";
    private static final String OUTPUT_TRIM_ENABLE = "commands.edit.flags.trimenable";
    private static final String OUTPUT_ENCHANTMENTS_DISABLE = "commands.edit.flags.enchantmentsdisable";
    private static final String OUTPUT_ATTRIBUTES_DISABLE = "commands.edit.flags.attributesdisable";
    private static final String OUTPUT_UNBREAKABLE_DISABLE = "commands.edit.flags.unbreakabledisable";
    private static final String OUTPUT_CANDESTROY_DISABLE = "commands.edit.flags.candestroydisable";
    private static final String OUTPUT_CANPLACEON_DISABLE = "commands.edit.flags.canplaceondisable";
    private static final String OUTPUT_OTHERS_DISABLE = "commands.edit.flags.othersdisable";
    private static final String OUTPUT_DYED_DISABLE = "commands.edit.flags.dyeddisable";
    private static final String OUTPUT_TRIM_DISABLE = "commands.edit.flags.trimdisable";
    private static final String OUTPUT_ALL_ENABLE = "commands.edit.flags.allenable";
    private static final String OUTPUT_ALL_DISABLE = "commands.edit.flags.alldisable";
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
                ItemUtil.checkHasItem(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean enchantments = (hideflags & ENCHANTMENTS_MASK) == ENCHANTMENTS_MASK;
                boolean attributes = (hideflags & ATTRIBUTES_MASK) == ATTRIBUTES_MASK;
                boolean unbreakable = (hideflags & UNBREAKABLE_MASK) == UNBREAKABLE_MASK;
                boolean candestroy = (hideflags & CANDESTROY_MASK) == CANDESTROY_MASK;
                boolean canplaceon = (hideflags & CANPLACEON_MASK) == CANPLACEON_MASK;
                boolean others = (hideflags & OTHERS_MASK) == OTHERS_MASK;
                boolean dyed = (hideflags & DYED_MASK) == DYED_MASK;
                boolean trim = (hideflags & TRIM_MASK) == TRIM_MASK;
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                context.getSource().sendFeedback(Text.translatable(enchantments ? OUTPUT_ENCHANTMENTS_GET_ENABLED : OUTPUT_ENCHANTMENTS_GET_DISABLED));
                context.getSource().sendFeedback(Text.translatable(attributes ? OUTPUT_ATTRIBUTES_GET_ENABLED : OUTPUT_ATTRIBUTES_GET_DISABLED));
                context.getSource().sendFeedback(Text.translatable(unbreakable ? OUTPUT_UNBREAKABLE_GET_ENABLED : OUTPUT_UNBREAKABLE_GET_DISABLED));
                context.getSource().sendFeedback(Text.translatable(candestroy ? OUTPUT_CANDESTROY_GET_ENABLED : OUTPUT_CANDESTROY_GET_DISABLED));
                context.getSource().sendFeedback(Text.translatable(canplaceon ? OUTPUT_CANPLACEON_GET_ENABLED : OUTPUT_CANPLACEON_GET_DISABLED));
                context.getSource().sendFeedback(Text.translatable(others ? OUTPUT_OTHERS_GET_ENABLED : OUTPUT_OTHERS_GET_DISABLED));
                context.getSource().sendFeedback(Text.translatable(dyed ? OUTPUT_DYED_GET_ENABLED : OUTPUT_DYED_GET_DISABLED));
                context.getSource().sendFeedback(Text.translatable(trim ? OUTPUT_TRIM_GET_ENABLED : OUTPUT_TRIM_GET_DISABLED));
                return hideflags;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getEnchantmentsNode = ClientCommandManager
            .literal("enchantments")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean enchantments = (hideflags & ENCHANTMENTS_MASK) == ENCHANTMENTS_MASK;
                context.getSource().sendFeedback(Text.translatable(enchantments ? OUTPUT_ENCHANTMENTS_GET_DISABLED : OUTPUT_ENCHANTMENTS_GET_ENABLED));
                return enchantments ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getAttributesNode = ClientCommandManager
            .literal("attributes")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean attributes = (hideflags & ATTRIBUTES_MASK) == ATTRIBUTES_MASK;
                context.getSource().sendFeedback(Text.translatable(attributes ? OUTPUT_ATTRIBUTES_GET_DISABLED : OUTPUT_ATTRIBUTES_GET_ENABLED));
                return attributes ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getUnbreakableNode = ClientCommandManager
            .literal("unbreakable")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean unbreakable = (hideflags & UNBREAKABLE_MASK) == UNBREAKABLE_MASK;
                context.getSource().sendFeedback(Text.translatable(unbreakable ? OUTPUT_UNBREAKABLE_GET_DISABLED : OUTPUT_UNBREAKABLE_GET_ENABLED));
                return unbreakable ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getCandestroyNode = ClientCommandManager
            .literal("candestroy")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean candestroy = (hideflags & CANDESTROY_MASK) == CANDESTROY_MASK;
                context.getSource().sendFeedback(Text.translatable(candestroy ? OUTPUT_CANDESTROY_GET_DISABLED : OUTPUT_CANDESTROY_GET_ENABLED));
                return candestroy ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getCanplaceonNode = ClientCommandManager
            .literal("canplaceon")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean canplaceon = (hideflags & CANPLACEON_MASK) == CANPLACEON_MASK;
                context.getSource().sendFeedback(Text.translatable(canplaceon ? OUTPUT_CANPLACEON_GET_DISABLED : OUTPUT_CANPLACEON_GET_ENABLED));
                return canplaceon ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getOthersNode = ClientCommandManager
            .literal("others")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean others = (hideflags & OTHERS_MASK) == OTHERS_MASK;
                context.getSource().sendFeedback(Text.translatable(others ? OUTPUT_OTHERS_GET_DISABLED : OUTPUT_OTHERS_GET_ENABLED));
                return others ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getDyedNode = ClientCommandManager
            .literal("dyed")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean dyed = (hideflags & DYED_MASK) == DYED_MASK;
                context.getSource().sendFeedback(Text.translatable(dyed ? OUTPUT_DYED_GET_DISABLED : OUTPUT_DYED_GET_ENABLED));
                return dyed ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> getTrimNode = ClientCommandManager
            .literal("trim")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean trim = (hideflags & TRIM_MASK) == TRIM_MASK;
                context.getSource().sendFeedback(Text.translatable(trim ? OUTPUT_TRIM_GET_DISABLED : OUTPUT_TRIM_GET_ENABLED));
                return trim ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
            .literal("toggle")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags == 0 ? -1 : 0);
                context.getSource().sendFeedback(Text.translatable(hideflags == 0 ? OUTPUT_ALL_DISABLE : OUTPUT_ALL_ENABLE));
                ItemUtil.setItemStack(context.getSource(), item);
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> toggleEnchantmentsNode = ClientCommandManager
            .literal("enchantments")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean enchantments = (hideflags & ENCHANTMENTS_MASK) == ENCHANTMENTS_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ ENCHANTMENTS_MASK);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(enchantments ? OUTPUT_ENCHANTMENTS_DISABLE : OUTPUT_ENCHANTMENTS_ENABLE));
                return enchantments ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> toggleAttributesNode = ClientCommandManager
            .literal("attributes")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean attributes = (hideflags & ATTRIBUTES_MASK) == ATTRIBUTES_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ ATTRIBUTES_MASK);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(attributes ? OUTPUT_ATTRIBUTES_DISABLE : OUTPUT_ATTRIBUTES_ENABLE));
                return attributes ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> toggleUnbreakableNode = ClientCommandManager
            .literal("unbreakable")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean unbreakable = (hideflags & UNBREAKABLE_MASK) == UNBREAKABLE_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ UNBREAKABLE_MASK);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(unbreakable ? OUTPUT_UNBREAKABLE_DISABLE : OUTPUT_UNBREAKABLE_ENABLE));
                return unbreakable ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> toggleCandestroyNode = ClientCommandManager
            .literal("candestroy")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean candestroy = (hideflags & CANDESTROY_MASK) == CANDESTROY_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ CANDESTROY_MASK);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(candestroy ? OUTPUT_CANDESTROY_DISABLE : OUTPUT_CANDESTROY_ENABLE));
                return candestroy ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> toggleCanplaceonNode = ClientCommandManager
            .literal("canplaceon")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean canplaceon = (hideflags & CANPLACEON_MASK) == CANPLACEON_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ CANPLACEON_MASK);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(canplaceon ? OUTPUT_CANPLACEON_DISABLE : OUTPUT_CANPLACEON_ENABLE));
                return canplaceon ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> toggleOthersNode = ClientCommandManager
            .literal("others")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean others = (hideflags & OTHERS_MASK) == OTHERS_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ OTHERS_MASK);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(others ? OUTPUT_OTHERS_DISABLE : OUTPUT_OTHERS_ENABLE));
                return others ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> toggleDyedNode = ClientCommandManager
            .literal("dyed")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean dyed = (hideflags & DYED_MASK) == DYED_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ DYED_MASK);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(dyed ? OUTPUT_DYED_DISABLE : OUTPUT_DYED_ENABLE));
                return dyed ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> toggleTrimNode = ClientCommandManager
            .literal("trim")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                boolean trim = (hideflags & TRIM_MASK) == TRIM_MASK;
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ TRIM_MASK);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(trim ? OUTPUT_TRIM_DISABLE : OUTPUT_TRIM_ENABLE));
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

        // ... toggle enchantments|attributes|unbreakable|candestroy|canplaceon|others|dyed|trim
        node.addChild(toggleNode);
        toggleNode.addChild(toggleEnchantmentsNode);
        toggleNode.addChild(toggleAttributesNode);
        toggleNode.addChild(toggleUnbreakableNode);
        toggleNode.addChild(toggleCandestroyNode);
        toggleNode.addChild(toggleCanplaceonNode);
        toggleNode.addChild(toggleOthersNode);
        toggleNode.addChild(toggleDyedNode);
        toggleNode.addChild(toggleTrimNode);
    }
}
