package me.white.simpleitemeditor.node;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.argument.IdentifierArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class CustomNode implements Node {
    public static final CommandSyntaxException NO_TAGS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.custom.error.notags")).create();
    public static final CommandSyntaxException NO_SUCH_TAG_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.custom.error.nosuchtag")).create();
    public static final CommandSyntaxException ALREADY_HAS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.custom.error.alreadyhas")).create();
    private static final String OUTPUT_GET_ALL = "commands.edit.custom.getall";
    private static final String OUTPUT_GET = "commands.edit.custom.get";
    private static final String OUTPUT_SET = "commands.edit.custom.set";
    private static final String OUTPUT_REMOVE = "commands.edit.custom.remove";
    private static final String OUTPUT_CLEAR = "commands.edit.custom.clear";

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("custom")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!stack.hasNbt()) throw NO_TAGS_EXCEPTION;
                    if (!ItemUtil.hasCustomTags(stack)) throw NO_TAGS_EXCEPTION;

                    HashMap<Identifier, NbtElement> tags = ItemUtil.getCustomTags(stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET_ALL));
                    for (Identifier tagName : tags.keySet()) {
                        NbtElement value = tags.get(tagName);
                        context.getSource().sendFeedback(Text.empty()
                                .append(Text.literal("- ").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                                .append(TextUtil.copyable(tagName).setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
                                .append(Text.literal(": ").setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)))
                                .append(TextUtil.copyable(value))
                        );
                    }
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Identifier> getIdentifierNode = ClientCommandManager
                .argument("identifier", IdentifierArgumentType.identifier())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!ItemUtil.hasCustomTags(stack)) throw NO_TAGS_EXCEPTION;
                    HashMap<Identifier, NbtElement> tags = ItemUtil.getCustomTags(stack);
                    if (!tags.containsKey(identifier)) throw NO_SUCH_TAG_EXCEPTION;

                    NbtElement value = tags.get(identifier);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, TextUtil.copyable(value)));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Identifier> setIdentifierNode = ClientCommandManager
                .argument("identifier", IdentifierArgumentType.identifier())
                .build();

        ArgumentCommandNode<FabricClientCommandSource, NbtElement> setIdentifierValueNode = ClientCommandManager
                .argument("value", NbtElementArgumentType.nbtElement())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");
                    NbtElement value = NbtElementArgumentType.getNbtElement(context, "value");
                    HashMap<Identifier, NbtElement> tags = ItemUtil.getCustomTags(stack);
                    if (tags.containsKey(identifier) && tags.get(identifier).equals(value)) throw ALREADY_HAS_EXCEPTION;

                    tags.put(identifier, value);
                    ItemUtil.setCustomTags(stack, tags);
                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, TextUtil.copyable(value)));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager
                .literal("remove")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Identifier> removeIdentifierNode = ClientCommandManager
                .argument("identifier", IdentifierArgumentType.identifier())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    Identifier identifier = IdentifierArgumentType.getIdentifier(context, "identifier");
                    HashMap<Identifier, NbtElement> tags = ItemUtil.getCustomTags(stack);
                    if (!tags.containsKey(identifier)) throw NO_SUCH_TAG_EXCEPTION;

                    tags.remove(identifier);
                    ItemUtil.setCustomTags(stack, tags);
                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_REMOVE));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> clearNode = ClientCommandManager
                .literal("clear")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!ItemUtil.hasCustomTags(stack, false)) throw NO_TAGS_EXCEPTION;

                    ItemUtil.setCustomTags(stack, null);
                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_CLEAR));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get [<identifier>]
        node.addChild(getNode);
        getNode.addChild(getIdentifierNode);

        // ... set <identifier> <value>
        node.addChild(setNode);
        setNode.addChild(setIdentifierNode);
        setIdentifierNode.addChild(setIdentifierValueNode);

        // ... remove <identifier>
        node.addChild(removeNode);
        removeNode.addChild(removeIdentifierNode);

        // ... clear
        node.addChild(clearNode);
    }
}
