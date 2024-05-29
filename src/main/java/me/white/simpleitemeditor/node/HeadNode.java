package me.white.simpleitemeditor.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class HeadNode implements Node {
    public static final CommandSyntaxException ISNT_HEAD_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.isnthead")).create();
    public static final CommandSyntaxException NO_TEXTURE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.notexture")).create();
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
    private static final URL MINESKIN_API;

    static {
        try {
            MINESKIN_API = new URL("https://api.mineskin.org/generate/url");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isHead(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.PLAYER_HEAD;
    }

    private static boolean hasProfile(ItemStack stack) {
        return stack.contains(DataComponentTypes.PROFILE);
    }

    private static GameProfile getProfile(ItemStack stack) {
        if (!hasProfile(stack)) {
            return null;
        }
        return stack.get(DataComponentTypes.PROFILE).gameProfile();
    }

    private static void setProfile(ItemStack stack, String texture, String signature) {
        PropertyMap properties = new PropertyMap();
        if (signature == null) {
            properties.put("textures", new Property("textures", texture));
        } else {
            properties.put("textures", new Property("textures", texture, signature));
        }
        stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.empty(), properties));
    }

    private static void setProfile(ItemStack stack, String owner) {
        stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of(owner), Optional.empty(), new PropertyMap()));
    }

    private static void removeProfile(ItemStack stack) {
        stack.remove(DataComponentTypes.PROFILE);
    }

    private static String getTexture(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        List<Property> textures = profile.getProperties().get("textures").stream().toList();
        if (textures.isEmpty()) {
            return null;
        }
        return textures.get(0).value();
    }

    public static void setFromUrl(String url, FabricClientCommandSource source) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("url", url);
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
            switch (code) {
                case (HttpURLConnection.HTTP_OK) -> {
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

                    ItemStack stack = EditorUtil.getStack(source).copy();
                    if (!EditorUtil.hasItem(stack)) {
                        throw EditorUtil.NO_ITEM_EXCEPTION;
                    }
                    if (!EditorUtil.hasCreative(source)) {
                        throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    }
                    if (!isHead(stack)) {
                        throw ISNT_HEAD_EXCEPTION;
                    }
                    setProfile(stack, value, signature);

                    EditorUtil.setStack(source, stack);
                    source.sendFeedback(Text.translatable(OUTPUT_TEXTURE_CUSTOM_OK));
                }
                case (HttpURLConnection.HTTP_BAD_REQUEST) -> {
                    throw BAD_CUSTOM_TEXTURE_EXCEPTION;
                }
                case (429) -> {
                    throw TOO_FAST_EXCEPTION;
                }
                default -> {
                    throw SERVER_ERROR_EXCEPTION;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CommandSyntaxException e) {
            source.sendError(Text.literal(e.getMessage()));
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("head").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            if (!hasProfile(stack)) {
                throw NO_TEXTURE_EXCEPTION;
            }
            GameProfile profile = getProfile(stack);
            String texture = getTexture(profile);
            String json = new String(Base64.getDecoder().decode(texture));
            JsonObject object = new Gson().fromJson(json, JsonObject.class);
            String url = object.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
            context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_GET, TextUtil.url(url)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        LiteralCommandNode<FabricClientCommandSource> setOwnerNode = ClientCommandManager.literal("owner").build();

        ArgumentCommandNode<FabricClientCommandSource, String> setOwnerOwnerNode = ClientCommandManager.argument("owner", StringArgumentType.word()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            String owner = StringArgumentType.getString(context, "owner");
            if (hasProfile(stack)) {
                GameProfile profile = getProfile(stack);
                if (owner.equals(profile.getName())) {
                    throw OWNER_ALREADY_IS_EXCEPTION;
                }
            }
            setProfile(stack, owner);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_OWNER_SET, owner));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setTextureNode = ClientCommandManager.literal("texture").build();

        ArgumentCommandNode<FabricClientCommandSource, String> setTextureTextureNode = ClientCommandManager.argument("texture", StringArgumentType.greedyString()).executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            String oldTexture = getTexture(getProfile(stack));
            String texture = StringArgumentType.getString(context, "texture");
            try {
                URL url = new URL(texture);
                if (url.toURI().getHost().equals("textures.minecraft.net")) {
                    if (texture.equals(oldTexture)) {
                        throw TEXTURE_ALREADY_IS_EXCEPTION;
                    }
                    String value = new String(Base64.getEncoder().encode(("{\"textures\":{\"SKIN\":{\"url\":\"" + texture + "\"}}}").getBytes()));
                    setProfile(stack, value, null);
                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_SET, TextUtil.url(texture)));
                } else {
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_CUSTOM_SET, TextUtil.url(texture)));
                    CompletableFuture.runAsync(() -> setFromUrl(texture, context.getSource()));
                }
            } catch (MalformedURLException | URISyntaxException ignored) {
                throw INVALID_URL_EXCEPTION;
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> removeNode = ClientCommandManager.literal("remove").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            if (!hasProfile(stack)) {
                throw NO_TEXTURE_EXCEPTION;
            }
            removeProfile(stack);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

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

        // ... remove
        node.addChild(removeNode);
    }
}
