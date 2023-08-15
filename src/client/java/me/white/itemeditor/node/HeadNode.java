package me.white.itemeditor.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.util.ItemUtil;
import me.white.itemeditor.util.EditorUtil;
import me.white.itemeditor.util.TextUtil;
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
import net.minecraft.text.Text;

public class HeadNode implements Node {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.cannotedit")).create();
    public static final CommandSyntaxException NO_TEXTURE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.notexture")).create();
    public static final CommandSyntaxException NO_SOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.nosound")).create();
    public static final CommandSyntaxException TEXTURE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.texturealreadyis")).create();
    public static final CommandSyntaxException OWNER_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.owneralreadyis")).create();
    public static final CommandSyntaxException INVALID_URL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.invalidtexture")).create();
    public static final CommandSyntaxException BAD_CUSTOM_TEXTURE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.texturecustombad")).create();
    public static final CommandSyntaxException TOO_FAST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.texturecustomtoofast")).create();
    public static final CommandSyntaxException SERVER_ERROR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.texturecustomservererror")).create();
    private static final String OUTPUT_OWNER_GET = "commands.edit.head.ownerget";
    private static final String OUTPUT_OWNER_SET = "commands.edit.head.ownerset";
    private static final String OUTPUT_TEXTURE_GET = "commands.edit.head.textureget";
    private static final String OUTPUT_TEXTURE_REMOVE = "commands.edit.head.textureremove";
    private static final String OUTPUT_TEXTURE_SET = "commands.edit.head.textureset";
    private static final String OUTPUT_TEXTURE_CUSTOM_SET = "commands.edit.head.texturecustomset";
    private static final String OUTPUT_TEXTURE_CUSTOM_OK = "commands.edit.head.texturecustomok";
    private static final String OUTPUT_SOUND_GET = "commands.edit.head.soundget";
    private static final String OUTPUT_SOUND_RESET = "commands.edit.head.soundreset";
    private static final String OUTPUT_SOUND_SET = "commands.edit.head.soundset";
    private static final URL MINESKIN_API;

    static {
        try {
            MINESKIN_API = new URL("https://api.mineskin.org/generate/url");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean canEdit(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.PLAYER_HEAD;
    }

    public static void setFromUrl(URL url, FabricClientCommandSource source) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("url", url.toString());
            body.addProperty("visibility", 1);

            HttpURLConnection connection = (HttpURLConnection) MINESKIN_API.openConnection();
            connection.addRequestProperty("User-Agent", "ItemEditor-HeadGenerator");
            connection.setConnectTimeout(30000);
            connection.addRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (OutputStream output = connection.getOutputStream()) {
                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                output.write(input, 0, input.length);
            }

            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = buffer.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                }
                JsonObject object = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonObject texture = object.getAsJsonObject("data").getAsJsonObject("texture");

                String value = texture.get("value").getAsString();
                String signature = texture.get("signature").getAsString();

                ItemStack stack = EditorUtil.getStack(source);
                if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                if (!EditorUtil.hasCreative(source)) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                ItemUtil.setHeadTexture(stack, value, signature);

                EditorUtil.setStack(source, stack);
                source.sendFeedback(Text.translatable(OUTPUT_TEXTURE_CUSTOM_OK));
            } else if (code == HttpURLConnection.HTTP_BAD_REQUEST) {
                throw BAD_CUSTOM_TEXTURE_EXCEPTION;
            } else if (code == 429) {
                throw TOO_FAST_EXCEPTION;
            } else {
                throw SERVER_ERROR_EXCEPTION;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CommandSyntaxException e) {
            source.sendError(Text.literal(e.getMessage()));
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("head")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                    if (ItemUtil.hasHeadTexture(stack)) {
                        URL texture = ItemUtil.getHeadTexture(stack);
                        context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_GET, TextUtil.clickable(texture)));
                    } else {
                        if (!ItemUtil.hasHeadOwner(stack)) throw NO_TEXTURE_EXCEPTION;
                        String owner = ItemUtil.getHeadOwner(stack);
                        context.getSource().sendFeedback(Text.translatable(OUTPUT_OWNER_GET, owner));
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
                    if (!ItemUtil.hasHeadOwner(stack) && !ItemUtil.hasHeadTexture(stack, false)) throw NO_TEXTURE_EXCEPTION;
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
                    URL oldTexture = ItemUtil.getHeadTexture(stack);
                    URL texture;
                    try {
                        texture = new URL(StringArgumentType.getString(context, "texture"));
                    } catch (MalformedURLException e) {
                        throw INVALID_URL_EXCEPTION;
                    }
                    if (ItemUtil.isValidHeadTextureUrl(texture)) {
                        if (oldTexture != null && texture.getPath().equals(oldTexture.getPath())) throw TEXTURE_ALREADY_IS_EXCEPTION;
                        ItemUtil.setHeadTexture(stack, texture);
                        EditorUtil.setStack(context.getSource(), stack);
                        context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_SET, TextUtil.clickable(texture)));
                    } else {
                        context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_CUSTOM_SET, TextUtil.clickable(texture)));
                        CompletableFuture.runAsync(() -> setFromUrl(texture, context.getSource()));
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

        // ... set
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
