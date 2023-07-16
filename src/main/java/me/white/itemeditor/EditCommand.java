package me.white.itemeditor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.node.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;

public class EditCommand {
	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> editNode = ClientCommandManager
			.literal("edit")
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> namespacedEditNode = ClientCommandManager
			.literal("itemeditor:edit")
			.redirect(editNode, null)
			.build();

		// ... material ...
		MaterialNode.register(editNode, registryAccess);
		// ... name ...
		NameNode.register(editNode, registryAccess);
		// ... lore ...
		LoreNode.register(editNode, registryAccess);
		// ... count ...
		CountNode.register(editNode, registryAccess);
		// ... model ...
		ModelNode.register(editNode, registryAccess);
		// ... enchantment ...
		EnchantmentNode.register(editNode, registryAccess);
		// ... get ...
		GetNode.register(editNode, registryAccess);
		// ... attribute ...
		AttributeNode.register(editNode, registryAccess);
		// ... color ...
		ColorNode.register(editNode, registryAccess);
		// ... hideflags ...
		FlagsNode.register(editNode, registryAccess);
		// ... equip
		EquipNode.register(editNode, registryAccess);
		// ... unbreakable
		UnbreakableNode.register(editNode, registryAccess);
		// ... whitelist ...
		WhitelistNode.register(editNode, registryAccess);
		// ... durability ...
		DurabilityNode.register(editNode, registryAccess);
		// ... data ...
		DataNode.register(editNode, registryAccess);
		// ... book ...
		BookNode.register(editNode, registryAccess);
		// ... firework ...
		// TODO
		// ... head ...
		// TODO
		// ... trim ...
		// TODO
		// ... sound ...
		// TODO
		// ... banner ...
		// TODO

		// BETA RELEASE

		// ... script ...
		// ... items ...
		// ... entity ...
		
		dispatcher.getRoot().addChild(editNode);
		dispatcher.getRoot().addChild(namespacedEditNode);
	}
}
