package me.white.itemeditor.editnodes;

import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class UnbreakableNode {
	private static final String OUTPUT_UNBREABKABLE_ENABLE = "commands.edit.unbreakableenable";
	private static final String OUTPUT_UNBREABKABLE_DISABLE = "commands.edit.unbreakabledisable";
	private static final String UNBREAKABLE_KEY = "Unbreakable";

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("unbreakable")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getOrCreateNbt();
				boolean unbreakable = nbt.getBoolean(UNBREAKABLE_KEY);
				nbt.putBoolean(UNBREAKABLE_KEY, !unbreakable);
				item.setNbt(nbt);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(unbreakable ? OUTPUT_UNBREABKABLE_DISABLE : OUTPUT_UNBREABKABLE_ENABLE));
				return 1;
			})
            .build();
        
        rootNode.addChild(node);
    }
}
