package me.white.itemeditor.node;

import java.net.MalformedURLException;
import java.net.URL;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.ItemUtil;
import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HeadNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.cannotedit")).create();
    public static final CommandSyntaxException NO_TEXTURE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.notexture")).create();
    public static final CommandSyntaxException NO_SOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.nosound")).create();
    public static final CommandSyntaxException TEXTURE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.texturealreadyis")).create();
    public static final CommandSyntaxException OWNER_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.owneralreadyis")).create();
    private static final CommandSyntaxException INVALID_URL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.invalidtexture")).create();
    private static final String OUTPUT_OWNER_GET = "commands.edit.head.ownerget";
    private static final String OUTPUT_OWNER_SET = "commands.edit.head.ownerset";
    private static final String OUTPUT_TEXTURE_GET = "commands.edit.head.textureget";
    private static final String OUTPUT_TEXTURE_REMOVE = "commands.edit.head.textureremove";
    private static final String OUTPUT_TEXTURE_SET = "commands.edit.head.textureset";
    private static final String OUTPUT_SOUND_GET = "commands.edit.head.soundget";
    private static final String OUTPUT_SOUND_RESET = "commands.edit.head.soundreset";
    private static final String OUTPUT_SOUND_SET = "commands.edit.head.soundset";

    private static boolean canEdit(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.PLAYER_HEAD;
    }

    private static Text stylizeUrl(URL url) {
        return Text.empty()
                .append(url.toString())
                .setStyle(Style.EMPTY
                        .withUnderline(true)
                        .withColor(Formatting.BLUE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.toString()))
                        .withInsertion(url.toString())
                );
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("head")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    if (ItemUtil.hasHeadOwner(stack)) {
                        String owner = ItemUtil.getHeadOwner(stack);

                        context.getSource().sendFeedback(Text.translatable(OUTPUT_OWNER_GET, owner));
                    } else {
                        if (!ItemUtil.hasHeadTexture(stack)) throw NO_TEXTURE_EXCEPTION;
                        String texture = ItemUtil.getHeadTexture(stack);

                        try {
                            context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_GET, stylizeUrl(new URL(texture))));
                        } catch (MalformedURLException e) {
                            throw INVALID_URL_EXCEPTION;
                        }
                    }
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
                .literal("set")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasHeadOwner(stack) && !ItemUtil.hasHeadTexture(stack, true)) throw NO_TEXTURE_EXCEPTION;
                    ItemUtil.setHeadTexture(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_REMOVE));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setOwnerNode = ClientCommandManager
                .literal("owner")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, String> setOwnerOwnerNode = ClientCommandManager
                .argument("owner", StringArgumentType.word())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    String oldOwner = ItemUtil.getHeadOwner(stack);
                    String owner = StringArgumentType.getString(context, "owner");
                    if (oldOwner != null && oldOwner.equals(owner)) throw OWNER_ALREADY_IS_EXCEPTION;
                    ItemUtil.setHeadOwner(stack, owner);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_OWNER_SET, owner));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> setTextureNode = ClientCommandManager
                .literal("texture")
                .build();

        ArgumentCommandNode<FabricClientCommandSource, String> setTextureTextureNode = ClientCommandManager
                .argument("texture", StringArgumentType.greedyString())
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    String oldTexture = ItemUtil.getHeadTexture(stack);
                    String texture = StringArgumentType.getString(context, "texture");
                    if (!ItemUtil.isValidHeadTextureUrl(texture)) throw INVALID_URL_EXCEPTION;
                    if (oldTexture != null && oldTexture.equals(texture)) throw TEXTURE_ALREADY_IS_EXCEPTION;
                    ItemUtil.setHeadTexture(stack, texture);

                    EditorUtil.setStack(context.getSource(), stack);
                    try {
                        context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_SET, stylizeUrl(new URL(texture))));
                    } catch (MalformedURLException e) {
                        throw INVALID_URL_EXCEPTION;
                    }
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> soundNode = ClientCommandManager
                .literal("sound")
                .build();

        LiteralCommandNode<FabricClientCommandSource> soundGetNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasNoteBlockSound(stack, true)) throw NO_SOUND_EXCEPTION;
                    SoundEvent sound = ItemUtil.getNoteBlockSound(stack);

                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SOUND_GET, sound.toString()));
                    return 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> soundSetNode = ClientCommandManager
                .literal("set")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    if (!ItemUtil.hasNoteBlockSound(stack)) throw NO_SOUND_EXCEPTION;
                    ItemUtil.setNoteBlockSound(stack, null);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SOUND_RESET));
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Reference<SoundEvent>> soundSetSoundNode = ClientCommandManager
                .argument("sound", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.SOUND_EVENT))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    SoundEvent sound = EditorUtil.getRegistryEntryArgument(context, "sound", RegistryKeys.SOUND_EVENT);
                    ItemUtil.setNoteBlockSound(stack, sound);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_SOUND_SET, sound.toString()));
                    return 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [...]
        node.addChild(setNode);
        // ... owner <owner>
        setNode.addChild(setOwnerNode);
        setOwnerNode.addChild(setOwnerOwnerNode);
        // ... texture <texture>
        setNode.addChild(setTextureNode);
        setTextureNode.addChild(setTextureTextureNode);

        // ... sound
        node.addChild(soundNode);
        // ... get
        soundNode.addChild(soundGetNode);
        // ... set [<sound>]
        soundNode.addChild(soundSetNode);
        soundSetNode.addChild(soundSetSoundNode);
    }
}
