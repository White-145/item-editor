package me.white.itemeditor.editnodes;

import java.util.Collection;
import java.util.List;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class ColorNode {
    public static class HexColorArgumentType implements ArgumentType<Integer> {
        private static final DynamicCommandExceptionType INVALID_HEX_COLOR_EXCEPTION = new DynamicCommandExceptionType((arg) -> Text.translatable("commands.edit.error.color.invalidhexcolor", arg));

		private static final Collection<String> EXAMPLES = List.of(
			"#FF0000",
            "#00bb88",
            "#b8b8b8"
		);

        private HexColorArgumentType() {}

        @Override
        public Integer parse(StringReader reader) throws CommandSyntaxException {
            if (!reader.canRead() || reader.peek() != '#') {
                throw INVALID_HEX_COLOR_EXCEPTION.create(reader.getRemaining());
            }
            reader.skip();
            int rgb = 0;
            if (!reader.canRead(6)) throw INVALID_HEX_COLOR_EXCEPTION.create(reader.getRemaining());
            for (int i = 0; i < 6; ++i) {
                char ch = Character.toLowerCase(reader.read());
                if (!(ch >= '0' || ch <= '9' || ch >= 'a' || ch <= 'f')) {
                    throw INVALID_HEX_COLOR_EXCEPTION.create(reader.getRemaining());
                } else {
                    rgb += Math.pow(16, 5 - i) * ((ch >= '0' && ch <= '9') ? (int)(ch - '0') : (int)(ch - 'a') + 10);
                }
            }
            return rgb;
        }

        public static HexColorArgumentType hexColor() {
            return new HexColorArgumentType();
        }

        @Override
        public Collection<String> getExamples() {
            return EXAMPLES;
        }

        public static Integer getHexColor(CommandContext<FabricClientCommandSource> context, String name) {
            return context.getArgument(name, Integer.class);
        }
    }

    public static final CommandSyntaxException NO_COLOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.error.color.nocolor")).create();
    private static final String OUTPUT_GET = "commands.edit.color.get";
    private static final String OUTPUT_SET = "commands.edit.color.set";
    private static final String OUTPUT_RESET = "commands.edit.color.reset";
    private static final String DISPLAY_KEY = "display";
    private static final String MAP_COLOR_KEY = "MapColor";
    private static final String COLOR_KEY = "color";

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("color")
            .build();
            
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource());
                String colorKey = COLOR_KEY;
                if (item.getItem() == Items.FILLED_MAP) {
                    colorKey = MAP_COLOR_KEY;
                }
                NbtCompound display = item.getSubNbt(DISPLAY_KEY);
                if (display == null) throw NO_COLOR_EXCEPTION;
                if (!display.contains(colorKey)) throw NO_COLOR_EXCEPTION;
                int color = display.getInt(colorKey);
                context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_GET, Integer.toHexString(color)));
                return color;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .build();

        ArgumentCommandNode<FabricClientCommandSource, Integer> setHexColorNode = ClientCommandManager
            .argument(COLOR_KEY, HexColorArgumentType.hexColor())
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
                String colorKey = COLOR_KEY;
                if (item.getItem() == Items.FILLED_MAP) {
                    colorKey = MAP_COLOR_KEY;
                }
                int color = HexColorArgumentType.getHexColor(context, "color");
                NbtCompound display = item.getOrCreateSubNbt(DISPLAY_KEY);
                display.putInt(colorKey, color);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_SET, Integer.toHexString(color)));
                return 1;
            })
            .build();

        
        LiteralCommandNode<FabricClientCommandSource> resetNode = ClientCommandManager
            .literal("reset")
            .executes(context -> {
                ItemStack item = EditCommand.getItemStack(context.getSource()).copy();
                NbtCompound display = item.getSubNbt(DISPLAY_KEY);
                if (display == null) throw NO_COLOR_EXCEPTION;
                if (!display.contains(COLOR_KEY) || !display.contains(MAP_COLOR_KEY)) throw NO_COLOR_EXCEPTION;
                display.remove(COLOR_KEY);
                display.remove(MAP_COLOR_KEY);
                item.setSubNbt(DISPLAY_KEY, display);
                EditCommand.setItemStack(context.getSource(), item);
                context.getSource().getPlayer().sendMessage(Text.translatable(OUTPUT_RESET));
                return 1;
            })
            .build();

        rootNode.addChild(node);

        // ... color get
        node.addChild(getNode);

        // ... color set <color>
        node.addChild(setNode);
        setNode.addChild(setHexColorNode);
        node.addChild(resetNode);
    }
}
