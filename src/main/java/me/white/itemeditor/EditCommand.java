package me.white.itemeditor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.editnodes.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class EditCommand {
	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> editNode = ClientCommandManager
			.literal("edit")
			.requires(context -> context.hasPermissionLevel(1))
			.build();

		LiteralCommandNode<FabricClientCommandSource> materialNode = ClientCommandManager
			.literal("material")
			.requires(MaterialNode::requirement)
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> nameNode = ClientCommandManager
			.literal("name")
			.requires(NameNode::requirement)
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> loreNode = ClientCommandManager
			.literal("lore")
			.requires(LoreNode::requirement)
			.build();
	
		LiteralCommandNode<FabricClientCommandSource> countNode = ClientCommandManager
			.literal("count")
			.requires(CountNode::requirement)
			.build();

		LiteralCommandNode<FabricClientCommandSource> modelNode = ClientCommandManager
			.literal("model")
			.requires(ModelNode::requirement)
			.build();

		MaterialNode.register(materialNode, registryAccess);
		NameNode.register(nameNode, registryAccess);
		LoreNode.register(loreNode, registryAccess);
		CountNode.register(countNode, registryAccess);
		ModelNode.register(modelNode, registryAccess);
		
		dispatcher.getRoot().addChild(editNode);
		/// ... material ...
		editNode.addChild(materialNode);
		/// ... name ...
		editNode.addChild(nameNode);
		/// ... lore ...
		editNode.addChild(loreNode);
		/// ... count ...
		editNode.addChild(countNode);
		/// ... model ...
		editNode.addChild(modelNode);
	}
}
