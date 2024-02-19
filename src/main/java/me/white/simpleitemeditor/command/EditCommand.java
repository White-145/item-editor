package me.white.simpleitemeditor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.node.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class EditCommand {
	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
				.literal("edit")
				.build();

		LiteralCommandNode<FabricClientCommandSource> nodeNamespaced = ClientCommandManager
				.literal("itemeditor:edit")
				.redirect(node)
				.build();

		for (Node editNode : new Node[] {
				new AttributeNode(),
				new BannerNode(),
				new BookNode(),
				new ColorNode(),
				new CountNode(),
				new CustomNode(),
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
		}) {
			try {
				editNode.register(node, registryAccess);
			} catch (IllegalStateException e) {
				SimpleItemEditor.LOGGER.error("Failed to register " + editNode.getClass().getName() + ": " + e);
			}
		}

		// TODO:
		// ... command ... - edit command blocks. Possibly in block node
		// ... sign ... - edit signs. Possibly in block node
		// ... block ... - edit block data (lectern book, items, metadata)
		// ... script ... - store/execute list of actions to perform. Possibly parameters
		// ... optimize ... - remove unnecessary nbt tags and optimize them as much as possible

		dispatcher.getRoot().addChild(node);
		dispatcher.getRoot().addChild(nodeNamespaced);
	}
}
