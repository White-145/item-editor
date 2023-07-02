package me.white.itemeditor.editnodes;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import me.white.itemeditor.EditCommand.Feedback;
import me.white.itemeditor.Colored;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public class NameNode {
	private static final CommandSyntaxException NO_NAME_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.name.noname")).create();
	private static final String OUTPUT_GET = "commands.edit.name.get";
	private static final String OUTPUT_SET = "commands.edit.name.set";
	private static final String OUTPUT_RESET = "commands.edit.name.reset";

	private static Feedback set(ItemStack item, Text name) {
		item.setCustomName(name);
		return new Feedback(item, 1);
	}

	public static void register(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
			.literal("set")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Feedback result = set(item, Text.empty());
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, ""));
				return result.value();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource());
				NbtCompound display = item.getSubNbt("display");
				if (display == null || !display.contains("Name", NbtElement.STRING_TYPE)) {
					throw NO_NAME_EXCEPTION;
				}
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, Text.Serializer.fromJson(display.getString("Name").toString())));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
			.literal("reset")
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Feedback result = set(item, null);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
				return result.value();
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> setNameNode = ClientCommandManager
			.argument("name", StringArgumentType.greedyString())
			.executes(context -> {
				ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
				Text name = Colored.of(StringArgumentType.getString(context, "name"));
				Feedback result = set(item, name);
				EditCommand.setItemStack(context.getSource(), result.result());
				context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, name));
				return result.value();
			})
			.build();

		// ... get
		node.addChild(getNode);

		// ... set [<name>]
		node.addChild(setNode);
		setNode.addChild(setNameNode);

		// ... reset
		node.addChild(resetNode);
	}
}
