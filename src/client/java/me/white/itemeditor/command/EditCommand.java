package me.white.itemeditor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.ItemEditor;
import me.white.itemeditor.node.*;
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

		// ... material ...
		MaterialNode.register(node, registryAccess);
		// ... name ...
		NameNode.register(node);
		// ... lore ...
		LoreNode.register(node);
		// ... count ...
		CountNode.register(node);
		// ... model ...
		ModelNode.register(node);
		// ... enchantment ...
		EnchantmentNode.register(node, registryAccess);
		// ... get ...
		GetNode.register(node, registryAccess);
		// ... attribute ...
		AttributeNode.register(node, registryAccess);
		// ... color ...
		ColorNode.register(node);
		// ... hideflags ...
		FlagNode.register(node);
		// ... equip
		EquipNode.register(node);
		// ... unbreakable
		UnbreakableNode.register(node);
		// ... whitelist ...
		WhitelistNode.register(node, registryAccess);
		// ... durability ...
		DurabilityNode.register(node);
		// ... data ...
		DataNode.register(node);
		// ... book ...
		BookNode.register(node);
		// ... head ...
		HeadNode.register(node, registryAccess);
		// ... trim ...
		try {
			TrimNode.register(node, registryAccess);
		} catch (IllegalStateException e) {
			ItemEditor.LOGGER.error("Failed to load trim node");
		}
		// ... firework ...
		FireworkNode.register(node);
		// ... banner ...
		BannerNode.register(node, registryAccess);
		// ... potion ...
		PotionNode.register(node, registryAccess);

		// BETA RELEASE

		// ... script ...
		// ... items ...
		// ... entity ...
		// ... optimize ...

		dispatcher.getRoot().addChild(node);
		dispatcher.getRoot().addChild(nodeNamespaced);
	}
}
