package me.white.itemeditor.node;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.QuotableStringArgumentType;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

// TODO OUTDATED /!\
public class HeadNode {
	public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.cannotedit")).create();
	public static final CommandSyntaxException NO_OWNER_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.noowner")).create();
	public static final CommandSyntaxException NO_TEXTURE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.notexture")).create();
	public static final CommandSyntaxException NO_SOUND_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.nosound")).create();
	public static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.head.error.alreadyis")).create();
    private static final DynamicCommandExceptionType INVALID_URL_EXCEPTION = new DynamicCommandExceptionType((arg) -> Text.translatable("commands.edit.head.error.invalidtexture", arg));
    private static final String OUTPUT_OWNER_GET = "commands.edit.head.ownerget";
    private static final String OUTPUT_OWNER_SET = "commands.edit.head.ownerset";
    private static final String OUTPUT_TEXTURE_GET = "commands.edit.head.textureget";
    private static final String OUTPUT_TEXTURE_REMOVE = "commands.edit.head.textureremove";
    private static final String OUTPUT_TEXTURE_SET = "commands.edit.head.textureset";
    private static final String OUTPUT_SOUND_GET = "commands.edit.head.soundget";
    private static final String OUTPUT_SOUND_RESET = "commands.edit.head.soundreset";
    private static final String OUTPUT_SOUND_SET = "commands.edit.head.soundset";
    private static final String SKULL_OWNER_KEY = "SkullOwner";
    private static final String NAME_KEY = "Name";
    private static final String ID_KEY = "Id";
    private static final String PROPERTIES_KEY = "Properties";
    private static final String TEXTURES_KEY = "textures";
    private static final String VALUE_KEY = "Value";
    private static final String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";
    private static final String NOTE_BLOCK_SOUND_KEY = "note_block_sound";
    private static final String TEXTURE_REGEX = "\\{textures:\\{SKIN:\\{url:\"http(?:s)?:\\/\\/textures\\.minecraft\\.net\\/texture\\/[0-9a-f]+\"\\}\\}\\}";
    private static final String TEXTURE_URL_REGEX = "http(?:s)?:\\/\\/textures\\.minecraft\\.net\\/texture\\/[0-9a-f]+";
    private static final String TEXTURE_FORMAT = "{textures:{SKIN:{url:\"%s\"}}}";

    private static void checkCanEdit(FabricClientCommandSource source) throws CommandSyntaxException {
        Item item = Util.getItemStack(source).getItem();
        if (item != Items.PLAYER_HEAD) throw CANNOT_EDIT_EXCEPTION;
    }

    private static void checkHasTexture(FabricClientCommandSource source) throws CommandSyntaxException {
        ItemStack item = Util.getItemStack(source);
        if (!item.hasNbt()) throw NO_TEXTURE_EXCEPTION;
        NbtCompound nbt = item.getNbt();
        if (!nbt.contains(SKULL_OWNER_KEY, NbtElement.COMPOUND_TYPE)) throw NO_TEXTURE_EXCEPTION;
        NbtCompound owner = nbt.getCompound(SKULL_OWNER_KEY);
        if (!owner.contains(PROPERTIES_KEY, NbtElement.COMPOUND_TYPE)) throw NO_TEXTURE_EXCEPTION;
        NbtCompound properties = owner.getCompound(PROPERTIES_KEY);
        if (!properties.contains(TEXTURES_KEY, NbtElement.LIST_TYPE)) throw NO_TEXTURE_EXCEPTION;
        NbtList textures = properties.getList(TEXTURES_KEY, NbtElement.COMPOUND_TYPE);
        if (textures.isEmpty()) throw NO_TEXTURE_EXCEPTION;
        String textureObject = new String(Base64.getDecoder().decode(((NbtCompound)textures.get(0)).getString(VALUE_KEY)));
        if (textureObject.isEmpty() || !textureObject.matches(TEXTURE_REGEX)) throw NO_TEXTURE_EXCEPTION;
    }

    private static void checkHasName(FabricClientCommandSource source) throws CommandSyntaxException {
        ItemStack item = Util.getItemStack(source);
        if (!item.hasNbt()) throw NO_OWNER_EXCEPTION;
        NbtCompound nbt = item.getNbt();
        if (!nbt.contains(SKULL_OWNER_KEY, NbtElement.COMPOUND_TYPE)) throw NO_OWNER_EXCEPTION;
        NbtCompound owner = nbt.getCompound(SKULL_OWNER_KEY);
        if (!owner.contains(NAME_KEY, NbtElement.STRING_TYPE)) throw NO_OWNER_EXCEPTION;
    }

    private static void checkHasSound(FabricClientCommandSource source) throws CommandSyntaxException {
        ItemStack item = Util.getItemStack(source);
        if (!item.hasNbt()) throw NO_SOUND_EXCEPTION;
        NbtCompound nbt = item.getNbt();
        if (!nbt.contains(BLOCK_ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) throw NO_SOUND_EXCEPTION;
        NbtCompound blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG_KEY);
        if (!blockEntityTag.contains(NOTE_BLOCK_SOUND_KEY, NbtElement.STRING_TYPE)) throw NO_SOUND_EXCEPTION;
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
                Util.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());
                try {
                    checkHasTexture(context.getSource());

                    ItemStack item = Util.getItemStack(context.getSource());
                    String textureObject = new String(Base64.getDecoder().decode(
                        ((NbtCompound)item
                            .getSubNbt(SKULL_OWNER_KEY)
                            .getCompound(PROPERTIES_KEY)
                            .getList(TEXTURES_KEY, NbtElement.COMPOUND_TYPE)
                            .get(0)
                        ).getString(VALUE_KEY)
                    ));
                    String texture = textureObject.substring(22, textureObject.length() - 4);

                    try {
                        context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_GET, stylizeUrl(new URL(texture))));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    return 1;
                } catch (CommandSyntaxException e) {
                    checkHasName(context.getSource());

                    ItemStack item = Util.getItemStack(context.getSource());
                    String owner = item.getNbt().getCompound(SKULL_OWNER_KEY).getString(NAME_KEY);
    
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_OWNER_GET, owner));
                }
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                if (!item.hasNbt() || !item.getNbt().contains(SKULL_OWNER_KEY)) throw NO_TEXTURE_EXCEPTION;
                item.removeSubNbt(SKULL_OWNER_KEY);

                Util.setItemStack(context.getSource(), item);
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
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                String owner = StringArgumentType.getString(context, "owner");

                item.setSubNbt(SKULL_OWNER_KEY, NbtString.of(owner));

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_OWNER_SET, owner));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setTextureNode = ClientCommandManager
            .literal("texture")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, String> setTextureTextureNode = ClientCommandManager
            .argument("texture", QuotableStringArgumentType.quotableString())
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                String texture = QuotableStringArgumentType.getQuotableString(context, "texture");

                URL textureUrl;
                try {
                    textureUrl = new URL(texture);
                } catch (MalformedURLException e) {
                    throw INVALID_URL_EXCEPTION.create(texture);
                }
                if (!textureUrl.toString().matches(TEXTURE_URL_REGEX)) throw INVALID_URL_EXCEPTION.create(texture);
                NbtCompound encoded = new NbtCompound();
                encoded.put(VALUE_KEY, NbtString.of(new String(Base64.getEncoder().encode(String.format(TEXTURE_FORMAT, textureUrl).getBytes()))));
                NbtCompound owner = item.getOrCreateSubNbt(SKULL_OWNER_KEY);
                NbtCompound properties = owner.getCompound(PROPERTIES_KEY);
                NbtList textures = properties.getList(TEXTURES_KEY, NbtList.COMPOUND_TYPE);
                if (textures.size() != 0 && textures.get(0).equals(encoded)) throw ALREADY_IS_EXCEPTION;
                textures.clear();
                textures.add(encoded);
                properties.put(TEXTURES_KEY, textures);
                owner.put(PROPERTIES_KEY, properties);
                owner.put(ID_KEY, NbtHelper.fromUuid(UUID.randomUUID()));
                owner.remove(NAME_KEY);
                item.setSubNbt(SKULL_OWNER_KEY, owner);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_TEXTURE_SET, textureUrl.toString()));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> soundNode = ClientCommandManager
            .literal("sound")
            .build();

        LiteralCommandNode<FabricClientCommandSource> soundGetNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                Util.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());
                checkHasSound(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());

                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                String sound = blockEntityTag.getString(NOTE_BLOCK_SOUND_KEY);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_SOUND_GET, sound));
                return 1;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> soundSetNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());
                checkHasSound(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();

                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                blockEntityTag.remove(NOTE_BLOCK_SOUND_KEY);
                item.setSubNbt(BLOCK_ENTITY_TAG_KEY, blockEntityTag);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SOUND_RESET));
                return 1;
            })
            .build();

        ArgumentCommandNode<FabricClientCommandSource, Reference<SoundEvent>> soundSetSoundNode = ClientCommandManager
            .argument("sound", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.SOUND_EVENT))
            .executes(context -> {
                Util.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                SoundEvent sound = Util.getRegistryEntryArgument(context, "sound", RegistryKeys.SOUND_EVENT);

                NbtCompound blockEntityTag = item.getOrCreateSubNbt(BLOCK_ENTITY_TAG_KEY);
                blockEntityTag.putString(NOTE_BLOCK_SOUND_KEY, sound.getId().toString());
                item.setSubNbt(BLOCK_ENTITY_TAG_KEY, blockEntityTag);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_SOUND_SET, sound.getId().toString()));
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
