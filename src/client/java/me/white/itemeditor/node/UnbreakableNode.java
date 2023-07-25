package me.white.itemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class UnbreakableNode {
	public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.unbreakable.error.cannotedit")).create();
	private static final String OUTPUT_GET_ENABLED = "commands.edit.unbreakable.getenabled";
	private static final String OUTPUT_GET_DISABLED = "commands.edit.unbreakable.getdisabled";
	private static final String OUTPUT_ENABLE = "commands.edit.unbreakable.enable";
	private static final String OUTPUT_DISABLE = "commands.edit.unbreakable.disable";

	private static boolean canEdit(ItemStack stack) {
		return stack.getMaxDamage() != 0;
	}

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("unbreakable")
            .build();
		
		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
                ItemStack stack = Util.getStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				boolean isUnbreakable = EditHelper.getUnbreakable(stack);

				context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
				return isUnbreakable ? 1 : 0;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
			.literal("toggle")
			.executes(context -> {
                ItemStack stack = Util.getStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				boolean isUnbreakable = EditHelper.getUnbreakable(stack);
				EditHelper.setUnbreakable(stack, !isUnbreakable);

				Util.setStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_DISABLE : OUTPUT_ENABLE));
				return isUnbreakable ? 1 : 0;
			})
			.build();
        
        rootNode.addChild(node);

		// ... get
		node.addChild(getNode);

		// ... toggle
		node.addChild(toggleNode);
    }
}
