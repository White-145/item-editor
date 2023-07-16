package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import me.white.itemeditor.Colored;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public class NameNode {
	private static final CommandSyntaxException NO_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.error.noname")).create();
	private static final String OUTPUT_GET = "commands.edit.name.get";
	private static final String OUTPUT_SET = "commands.edit.name.set";
	private static final String OUTPUT_RESET = "commands.edit.name.reset";

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("name")
			.build();

		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());

				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				item.setCustomName(Text.empty());
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, ""));
				return 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> setNameNode = ClientCommandManager
			.argument("name", StringArgumentType.greedyString())
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Text name = Colored.of(StringArgumentType.getString(context, "name"));
				item.setCustomName(name);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, name));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				EditCommand.checkHasItem(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource());
				NbtCompound display = item.getSubNbt("display");
				if (display == null || !display.contains("Name", NbtElement.STRING_TYPE)) throw NO_NAME_EXCEPTION;
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, Text.Serializer.fromJson(display.getString("Name").toString())));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
			.literal("reset")
			.executes(context -> {
				EditCommand.checkCanEdit(context.getSource());
				
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				item.setCustomName(null);
				EditCommand.setItemStack(context.getSource(), item);
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
				return 1;
			})
			.build();

		rootNode.addChild(node);

		// ... get
		node.addChild(getNode);

		// ... set [<name>]
		node.addChild(setNode);
		setNode.addChild(setNameNode);

		// ... reset
		node.addChild(resetNode);
	}
}
