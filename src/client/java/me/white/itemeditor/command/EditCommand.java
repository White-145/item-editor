package me.white.itemeditor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.ItemEditor;
import me.white.itemeditor.node.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class EditCommand {
	private static final Node[] NODES = new Node[] {
			new AttributeNode(),
			new BannerNode(),
			new BookNode(),
			new ColorNode(),
			new CountNode(),
			new DataNode(),
			new DurabilityNode(),
			new EnchantmentNode(),
			new EntityNode(),
			new EquipNode(),
			new FireworkNode(),
			new FlagNode(),
			new GetNode(),
			new HeadNode(),
			new LoreNode(),
			new MaterialNode(),
			new ModelNode(),
			new NameNode(),
			new PotionNode(),
			new TrimNode(),
			new UnbreakableNode(),
			new WhitelistNode()
	};

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
				.literal("edit")
				.build();

		LiteralCommandNode<FabricClientCommandSource> nodeNamespaced = ClientCommandManager
				.literal("itemeditor:edit")
				.redirect(node)
				.build();

		for (Node editNode : NODES) {
			try {
				editNode.register(node, registryAccess);
			} catch (IllegalStateException e) {
				ItemEditor.LOGGER.error("Failed to register " + editNode.getClass().getName() + ": " + e);
			}
		}

		dispatcher.getRoot().addChild(node);
		dispatcher.getRoot().addChild(nodeNamespaced);
	}
}
