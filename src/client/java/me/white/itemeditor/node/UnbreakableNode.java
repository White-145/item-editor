package me.white.itemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class UnbreakableNode {
	public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.unbreakable.error.cannotedit")).create();
	private static final String OUTPUT_GET_ENABLED = "commands.edit.unbreakable.getenabled";
	private static final String OUTPUT_GET_DISABLED = "commands.edit.unbreakable.getdisabled";
	private static final String OUTPUT_ENABLE = "commands.edit.unbreakable.enable";
	private static final String OUTPUT_DISABLE = "commands.edit.unbreakable.disable";
	private static final String UNBREAKABLE_KEY = "Unbreakable";

	private static void checkCanEdit(FabricClientCommandSource source) throws CommandSyntaxException {
		ItemStack item = Util.getItemStack(source);
		if (item.getMaxDamage() == 0) throw CANNOT_EDIT_EXCEPTION;
	}

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("unbreakable")
            .build();
		
		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				Util.checkHasItem(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource());
				boolean isUnbreakable = false;
				if (item.hasNbt()) isUnbreakable = item.getNbt().getBoolean(UNBREAKABLE_KEY);

				context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_GET_ENABLED : OUTPUT_GET_DISABLED));
				return isUnbreakable ? 1 : 0;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
			.literal("toggle")
			.executes(context -> {
                Util.checkCanEdit(context.getSource());
				checkCanEdit(context.getSource());

				ItemStack item = Util.getItemStack(context.getSource()).copy();

				NbtCompound nbt = item.getOrCreateNbt();
				boolean isUnbreakable = nbt.getBoolean(UNBREAKABLE_KEY);
				nbt.putBoolean(UNBREAKABLE_KEY, !isUnbreakable);
				item.setNbt(nbt);

				Util.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(isUnbreakable ? OUTPUT_DISABLE : OUTPUT_ENABLE));
				return isUnbreakable ? 1 : 0;
			})
			.build();
        
        rootNode.addChild(node);

		// ... get|toggle
		node.addChild(getNode);
		node.addChild(toggleNode);
    }
}
