package me.white.simpleitemeditor.node;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.RegistryArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class HeadNode implements Node {
    private static final CommandSyntaxException ISNT_HEAD_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.isnthead")).create();
    private static final CommandSyntaxException NO_TEXTURE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.notexture")).create();
    private static final CommandSyntaxException TEXTURE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.texturealreadyis")).create();
    private static final CommandSyntaxException OWNER_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.owneralreadyis")).create();
    private static final CommandSyntaxException INVALID_URL_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.invalidtexture")).create();
    private static final CommandSyntaxException BAD_CUSTOM_TEXTURE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.texturecustombad")).create();
    private static final CommandSyntaxException TOO_FAST_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.texturecustomtoofast")).create();
    private static final DynamicCommandExceptionType SERVER_ERROR_EXCEPTION = new DynamicCommandExceptionType(code -> Text.translatable("commands.edit.head.error.texturecustomservererror", code));
    private static final CommandSyntaxException NO_SOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.nosound")).create();
    private static final CommandSyntaxException SOUND_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.soundalreadyis")).create();
    private static final String OUTPUT_OWNER_GET = "commands.edit.head.ownerget";
    private static final String OUTPUT_OWNER_SET = "commands.edit.head.ownerset";
    private static final String OUTPUT_TEXTURE_GET = "commands.edit.head.textureget";
    private static final String OUTPUT_TEXTURE_REMOVE = "commands.edit.head.textureremove";
    private static final String OUTPUT_TEXTURE_SET = "commands.edit.head.textureset";
    private static final String OUTPUT_TEXTURE_CUSTOM_SET = "commands.edit.head.texturecustomset";
    private static final String OUTPUT_TEXTURE_CUSTOM_OK = "commands.edit.head.texturecustomok";
    private static final String OUTPUT_SOUND_GET = "commands.edit.head.soundget";
    private static final String OUTPUT_SOUND_SET = "commands.edit.head.soundset";
    private static final String OUTPUT_SOUND_REMOVE = "commands.edit.head.soundremove";
    private static final String MINESKIN_API_URL = "https://api.mineskin.org/v2/queue/";

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

    private static void setProfile(ItemStack stack, Property profile) {
        PropertyMap properties = new PropertyMap();
        properties.put("textures", profile);
        stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.empty(), Optional.empty(), properties));
    }

    private static void setProfile(ItemStack stack, String owner) {
        stack.set(DataComponentTypes.PROFILE, new ProfileComponent(Optional.of(owner), Optional.empty(), new PropertyMap()));
    }

    private static void removeProfile(ItemStack stack) {
        stack.remove(DataComponentTypes.PROFILE);
    }

    private static boolean hasSound(ItemStack stack) {
        return stack.contains(DataComponentTypes.NOTE_BLOCK_SOUND);
    }

    private static Identifier getSound(ItemStack stack) {
        if (!hasSound(stack)) {
            return null;
        }
        return stack.get(DataComponentTypes.NOTE_BLOCK_SOUND);
    }

    private static void setSound(ItemStack stack, Identifier sound) {
        if (sound == null) {
            stack.remove(DataComponentTypes.NOTE_BLOCK_SOUND);
        } else {
            stack.set(DataComponentTypes.NOTE_BLOCK_SOUND, sound);
        }
    }

    private static String getTexture(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        List<Property> textures = profile.getProperties().get("textures").stream().toList();
        if (textures.isEmpty()) {
            return null;
        }
        //? if >=1.21.6 {
        String texture = textures.getFirst().value();
        //?} else {
        /*String texture = textures.get(0).value();
        *///?}
        String json = new String(Base64.getDecoder().decode(texture));
        JsonObject object = new Gson().fromJson(json, JsonObject.class);
        return object.get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();
    }

    private static JsonObject getResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            while (true) {
                String responseLine = buffer.readLine();
                if (responseLine == null) {
                    break;
                }
                response.append(responseLine.trim());
            }
        }
        return JsonParser.parseString(response.toString()).getAsJsonObject();
    }

    public static Property getProfile(String url) throws CommandSyntaxException, IOException {
        HttpURLConnection connection = null;
        String id;
        try {
            connection = (HttpURLConnection)URI.create(MINESKIN_API_URL).toURL().openConnection();
            connection.addRequestProperty("User-Agent", "SimpleItemEditor-HeadGenerator");
            connection.addRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(30000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            JsonObject body = new JsonObject();
            body.addProperty("url", url);
            body.addProperty("visibility", "unlisted");
            try (OutputStream output = connection.getOutputStream()) {
                byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
                output.write(input, 0, input.length);
            }

            int code = connection.getResponseCode();
            JsonObject response = getResponse(connection);
            if (code == HttpURLConnection.HTTP_OK) {
                JsonObject data = response.getAsJsonObject("skin").getAsJsonObject("texture").getAsJsonObject("data");
                return new Property("textures", data.get("value").getAsString(), data.get("signature").getAsString());
            } else if (code == HttpURLConnection.HTTP_BAD_REQUEST) {
                throw BAD_CUSTOM_TEXTURE_EXCEPTION;
            } else if (code == 429) {
                throw TOO_FAST_EXCEPTION;
            } else if (code != HttpURLConnection.HTTP_ACCEPTED) {
                throw SERVER_ERROR_EXCEPTION.create(code);
            }

            id = response.getAsJsonObject("job").get("id").getAsString();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        while (true) {
            try {
                connection = (HttpURLConnection)URI.create(MINESKIN_API_URL + id).toURL().openConnection();
                connection.addRequestProperty("User-Agent", "SimpleItemEditor-HeadGenerator");
                connection.addRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(30000);
                connection.setRequestMethod("GET");

                int code = connection.getResponseCode();
                if (code != 200) {
                    throw SERVER_ERROR_EXCEPTION.create(code);
                }
                JsonObject response = getResponse(connection);
                if (response.has("skin")) {
                    JsonObject data = response.getAsJsonObject("skin").getAsJsonObject("texture").getAsJsonObject("data");
                    return new Property("textures", data.get("value").getAsString(), data.get("signature").getAsString());
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                connection.disconnect();
            }
        }
    }

    public static void setFromUrl(String url, CommandSource source) {
        try {
            Property profile = getProfile(url);
            ItemStack stack = EditorUtil.getCheckedStack(source).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            if (!EditorUtil.canEdit(source)) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            setProfile(stack, profile);

            EditorUtil.setStack(source, stack);
            EditorUtil.sendFeedback(source, Text.translatable(OUTPUT_TEXTURE_CUSTOM_OK));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CommandSyntaxException e) {
            EditorUtil.sendError(source, Text.literal(e.getMessage()));
        }
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("head").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            if (!hasProfile(stack)) {
                throw NO_TEXTURE_EXCEPTION;
            }
            GameProfile profile = getProfile(stack);
            String texture = getTexture(profile);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_TEXTURE_GET, TextUtil.url(texture)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setOwnerNode = commandManager.literal("owner").build();

        CommandNode<S> setOwnerOwnerNode = commandManager.argument("owner", StringArgumentType.word()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_OWNER_SET, owner));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setTextureNode = commandManager.literal("texture").build();

        CommandNode<S> setTextureTextureNode = commandManager.argument("texture", StringArgumentType.greedyString()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            String oldTexture = getTexture(getProfile(stack));
            String texture = StringArgumentType.getString(context, "texture");
            URI uri;
            try {
                uri = URI.create(texture);
            } catch (IllegalArgumentException ignored) {
                throw INVALID_URL_EXCEPTION;
            }
            if (uri.getHost().equals("textures.minecraft.net")) {
                if (texture.equals(oldTexture)) {
                    throw TEXTURE_ALREADY_IS_EXCEPTION;
                }
                String value = new String(Base64.getEncoder().encode(("{\"textures\":{\"SKIN\":{\"url\":\"" + texture + "\"}}}").getBytes()));
                setProfile(stack, new Property("textures", value, null));
                EditorUtil.setStack(context.getSource(), stack);
                EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_TEXTURE_SET, TextUtil.url(texture)));
            } else {
                EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_TEXTURE_CUSTOM_SET, TextUtil.url(texture)));
                CompletableFuture.runAsync(() -> setFromUrl(texture, context.getSource()));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            if (!hasProfile(stack)) {
                throw NO_TEXTURE_EXCEPTION;
            }
            removeProfile(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_TEXTURE_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> soundNode = commandManager.literal("sound").build();

        CommandNode<S> soundGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            if (!hasSound(stack)) {
                throw NO_SOUND_EXCEPTION;
            }

            Identifier sound = getSound(stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SOUND_GET, sound));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> soundSetNode = commandManager.literal("set").build();

        CommandNode<S> soundSetSoundNode = commandManager.argument("sound", RegistryArgumentType.registryEntry(RegistryKeys.SOUND_EVENT, registryAccess)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            SoundEvent sound = RegistryArgumentType.getRegistryEntry(context, "sound", RegistryKeys.SOUND_EVENT);
            //? if >=1.21.2 {
            Identifier id = sound.id();
            //?} else {
            /*Identifier id = sound.getId();
            *///?}
            setSound(stack, id);
            if (hasSound(stack) && id.equals(getSound(stack))) {
                throw SOUND_ALREADY_IS_EXCEPTION;
            }

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SOUND_SET, id));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> soundRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isHead(stack)) {
                throw ISNT_HEAD_EXCEPTION;
            }
            if (!hasSound(stack)) {
                throw NO_SOUND_EXCEPTION;
            }
            setSound(stack, null);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SOUND_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get
        node.addChild(getNode);

        // ... set ...
        node.addChild(setNode);
        // ... owner <owner>
        setNode.addChild(setOwnerNode);
        setOwnerNode.addChild(setOwnerOwnerNode);
        // ... texture <texture>
        setNode.addChild(setTextureNode);
        setTextureNode.addChild(setTextureTextureNode);

        // ... remove
        node.addChild(removeNode);

        // ... sound ...
        node.addChild(soundNode);
        // ... get
        soundNode.addChild(soundGetNode);
        // ... set <sound>
        soundNode.addChild(soundSetNode);
        soundSetNode.addChild(soundSetSoundNode);
        // ... remove
        soundNode.addChild(soundRemoveNode);

        return node;
    }
}
