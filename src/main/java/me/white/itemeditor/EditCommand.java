package me.white.itemeditor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.editnodes.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class EditCommand {
	public record Feedback(ItemStack result, int value) {};

	private static final String OUTPUT_EQUIP = "commands.edit.equip";
	private static final String OUTPUT_UNBREABKABLE_ENABLE = "commands.edit.unbreakableenable";
	private static final String OUTPUT_UNBREABKABLE_DISABLE = "commands.edit.unbreakabledisable";
	private static final String UNBREAKABLE_KEY = "Unbreakable";

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> editNode = ClientCommandManager
			.literal("edit")
			.requires(context -> context.getClient().interactionManager.getCurrentGameMode().equals(GameMode.CREATIVE))
			.build();

		LiteralCommandNode<FabricClientCommandSource> equipNode = ClientCommandManager
			.literal("equip")
			.executes(context -> {
				ItemStack item = getItemStack(context.getSource());
				PlayerInventory inventory = context.getSource().getPlayer().getInventory();
				ItemStack headItem = inventory.getArmorStack(3);
				setItemStack(context.getSource(), headItem);
				inventory.setStack(39, item);
				inventory.updateItems();
				context.getSource().getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(5, item));
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_EQUIP));
				return headItem.isEmpty() ? 0 : 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> unbreakableNode = ClientCommandManager
			.literal("unbreakable")
			.executes(context -> {
				ItemStack item = getItemStack(context.getSource()).copy();
				NbtCompound nbt = item.getOrCreateNbt();
				boolean unbreakable = nbt.getBoolean(UNBREAKABLE_KEY);
				nbt.putBoolean(UNBREAKABLE_KEY, !unbreakable);
				item.setNbt(nbt);
				setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(unbreakable ? OUTPUT_UNBREABKABLE_DISABLE : OUTPUT_UNBREABKABLE_ENABLE));
				return 1;
			})
			.build();

		// material
		MaterialNode.register(editNode, registryAccess);
		// name
		NameNode.register(editNode, registryAccess);
		// lore
		LoreNode.register(editNode, registryAccess);
		// count
		CountNode.register(editNode, registryAccess);
		// model
		ModelNode.register(editNode, registryAccess);
		// enchantment
		EnchantmentNode.register(editNode, registryAccess);
		// get
		GetNode.register(editNode, registryAccess);
		// attribute
		AttributeNode.register(editNode, registryAccess);
		// color
		ColorNode.register(editNode, registryAccess);
		// hideflags
		HideFlagsNode.register(editNode, registryAccess);
		// equip
		// unbreakable
		
		dispatcher.getRoot().addChild(editNode);
		// ... whitelist ...
		// TODO
		// ... durability ...
		// TODO
		// ... data ...
		// TODO
		// ... book ...
		// TODO
		// ... firework ...
		// TODO
		// ... head ...
		// TODO
		// ... entity ...
		// TODO
		// ... trim ...
		// TODO
		// ... script ...
		// TODO
	}
	
	public static ItemStack getItemStack(FabricClientCommandSource context) {
		return context.getPlayer().getInventory().getMainHandStack();
	}

	public static void setItemStack(FabricClientCommandSource context, ItemStack item) {
		PlayerInventory inventory = context.getPlayer().getInventory();
		int slot = inventory.selectedSlot;
		inventory.setStack(slot, item);
		inventory.updateItems();
		context.getClient().getNetworkHandler().sendPacket(new CreativeInventoryActionC2SPacket(36 + slot, item));
	}
}
