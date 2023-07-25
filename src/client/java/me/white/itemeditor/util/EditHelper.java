package me.white.itemeditor.util;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.primitives.Ints;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import oshi.util.tuples.Quintet;

public class EditHelper {
    private static final String ATTRIBUTE_MODIFIERS_KEY = "AttributeModifiers";
    private static final String ATTRIBUTE_MODIFIERS_AMOUNT_KEY = "Amount";
    private static final String ATTRIBUTE_MODIFIERS_ATTRIBUTE_NAME_KEY = "AttributeName";
    private static final String ATTRIBUTE_MODIFIERS_OPERATION_KEY = "Operation";
    private static final String ATTRIBUTE_MODIFIERS_SLOT_KEY = "Slot";
    private static final String ATTRIBUTE_MODIFIERS_UUID_KEY = "UUID";
    private static final String AUTHOR_KEY = "author";
    private static final String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";
    private static final String BLOCK_ENTITY_TAG_NOTE_BLOCK_SOUND = "note_block_sound";
    private static final String BLOCK_ENTITY_TAG_PATTERNS_KEY = "Patterns";
    private static final String BLOCK_ENTITY_TAG_PATTERNS_COLOR_KEY = "Color";
    private static final String BLOCK_ENTITY_TAG_PATTERNS_PATTERN_KEY = "Pattern";
    private static final String CAN_DESTROY_KEY = "CanDestroy";
    private static final String CAN_PLACE_ON_KEY = "CanPlaceOn";
    private static final String CUSTOM_MODEL_DATA_KEY = "CustomModelData";
    private static final String CUSTOM_POTION_EFFECTS_KEY = "CustomPotionEffects";
    private static final String CUSTOM_POTION_EFFECTS_ID_KEY = "Id";
    private static final String CUSTOM_POTION_EFFECTS_AMPLIFIER_KEY = "Amplifier";
    private static final String CUSTOM_POTION_EFFECTS_DURATION_KEY = "Duration";
    private static final String CUSTOM_POTION_EFFECTS_SHOW_PARTICLES_KEY = "ShowParticles";
    private static final String DISPLAY_KEY = "display";
    private static final String DISPLAY_COLOR_KEY = "color";
    private static final String DISPLAY_LORE_KEY = "Lore";
    private static final String DISPLAY_MAP_COLOR_KEY = "MapColor";
    private static final String DISPLAY_NAME_KEY = "Name";
    private static final String ENCHANTMENTS_KEY = "Enchantments";
    private static final String ENCHANTMENTS_ID_KEY = "id";
    private static final String ENCHANTMENTS_LVL_KEY = "lvl";
    private static final String FIREWORKS_KEY = "Fireworks";
    private static final String FIREWORKS_EXPLOSIONS_KEY = "Explosions";
    private static final String FIREWORKS_EXPLOSIONS_COLORS_KEY = "Colors";
    private static final String FIREWORKS_EXPLOSIONS_FADE_COLORS_KEY = "FadeColors";
    private static final String FIREWORKS_EXPLOSIONS_FLICKER_KEY = "Flicker";
    private static final String FIREWORKS_EXPLOSIONS_TRAIL_KEY = "Trail";
    private static final String FIREWORKS_EXPLOSIONS_TYPE_KEY = "Type";
    private static final String FIREWORKS_FLIGHT_KEY = "Flight";
    private static final String GENERATION_KEY = "generation";
    private static final String PAGES_KEY = "pages";
    private static final String SKULL_OWNER_KEY = "SkullOwner";
    private static final String SKULL_OWNER_NAME_KEY = "Name";
    private static final String SKULL_OWNER_PROPERTIES_KEY = "Properties";
    private static final String SKULL_OWNER_PROPERTIES_TEXTURES_KEY = "textures";
    private static final String SKULL_OWNER_PROPERTIES_TEXTURES_VALUE_KEY = "Value";
    private static final String TITLE_KEY = "title";
    private static final String TRIM_KEY = "Trim";
    private static final String TRIM_MATERIAL_KEY = "material";
    private static final String TRIM_PATTERN_KEY = "pattern";
    private static final String UNBREAKABLE_KEY = "Unbreakable";
    private static final String HIDE_FLAGS_KEY = "HideFlags";
    private static final String HEAD_TEXTURE_REGEX = "\\{\"textures\":\\{\"SKIN\":\\{\"url\":\"https?://textures\\.minecraft\\.net/texture/[0-9a-fA-F]+\"}}}";
    private static final String HEAD_TEXTURE_URL_REGEX = "https?://textures\\.minecraft\\.net/texture/[0-9a-fA-F]+";
    private static final String HEAD_TEXTURE_OBJECT = "{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}";
    public static final int FLAGS_AMOUNT = 8;
    public static final int GENERATIONS_AMOUNT = 4;

    private static String getColorKey(Item item) {
        if (item instanceof FilledMapItem) return DISPLAY_MAP_COLOR_KEY;
        return DISPLAY_COLOR_KEY;
    }

    /**
     * Checks if compound have all valid tags to be parsed to enchantment
     *
     * @param nbt NBT compound to check
     * @return Is compound valid for enchantment
     */
    public static boolean isValidEnchantment(@NotNull NbtCompound nbt) {
        if (nbt.isEmpty()) return false;
        if (!nbt.contains(ENCHANTMENTS_ID_KEY, NbtElement.STRING_TYPE)) return false;
        if (!nbt.contains(ENCHANTMENTS_LVL_KEY, NbtElement.INT_TYPE)) return false;
        Identifier id = Identifier.tryParse(nbt.getString(ENCHANTMENTS_ID_KEY));
        if (id == null) return false;
        return Registries.ENCHANTMENT.containsId(id);
    }

    /**
     * Checks if compound have all valid tags to be parsed to attribute
     *
     * @param nbt NBT compound to check
     * @return Is compound valid for attribute
     */
    public static boolean isValidAttribute(@NotNull NbtCompound nbt) {
        if (!nbt.contains(ATTRIBUTE_MODIFIERS_AMOUNT_KEY, NbtElement.DOUBLE_TYPE)) return false;
        if (!nbt.contains(ATTRIBUTE_MODIFIERS_UUID_KEY, NbtElement.INT_ARRAY_TYPE)) return false;
        UUID uuid = nbt.getUuid(ATTRIBUTE_MODIFIERS_UUID_KEY);
        if (uuid.getLeastSignificantBits() == 0 || uuid.getMostSignificantBits() == 0) return false;
        if (!nbt.contains(ATTRIBUTE_MODIFIERS_ATTRIBUTE_NAME_KEY, NbtElement.STRING_TYPE)) return false;
        Identifier attributeName = Identifier.tryParse(nbt.getString(ATTRIBUTE_MODIFIERS_ATTRIBUTE_NAME_KEY));
        if (attributeName == null || !Registries.ATTRIBUTE.containsId(attributeName)) return false;
        int operation = nbt.getInt(ATTRIBUTE_MODIFIERS_OPERATION_KEY);
        return operation >= 0 && operation < EntityAttributeModifier.Operation.values().length;
    }

    /**
     * Checks if compound have all valid tags to be parsed to banner pattern
     *
     * @param nbt NBT compound to check
     * @return Is compound valid for banner pattern
     */
    public static boolean isValidPattern(@NotNull NbtCompound nbt) {
        if (!nbt.contains(BLOCK_ENTITY_TAG_PATTERNS_COLOR_KEY, NbtElement.INT_TYPE)) return false;
        int color = nbt.getInt(BLOCK_ENTITY_TAG_PATTERNS_COLOR_KEY);
        if (color < 0 || color >= 16) return false;
        if (!nbt.contains(BLOCK_ENTITY_TAG_PATTERNS_PATTERN_KEY, NbtElement.STRING_TYPE)) return false;
        String pattern = nbt.getString(BLOCK_ENTITY_TAG_PATTERNS_PATTERN_KEY);
        return BannerPattern.byId(pattern) != null;
    }

    /**
     * Checks if compound have all valid tags to be parsed to armor trim
     *
     * @param nbt NBT compound to check
     * @param registryManager Dynamic registry manager to check in
     * @return Is compound valid for armor trim
     */
    public static boolean isValidTrim(@NotNull NbtCompound nbt, @NotNull DynamicRegistryManager registryManager) {
        if (!nbt.contains(TRIM_PATTERN_KEY, NbtElement.STRING_TYPE)) return false;
        if (!nbt.contains(TRIM_MATERIAL_KEY, NbtElement.STRING_TYPE)) return false;
        Identifier pattern = Identifier.tryParse(nbt.getString(TRIM_PATTERN_KEY));
        if (pattern == null || !registryManager.get(RegistryKeys.TRIM_PATTERN).containsId(pattern)) return false;
        Identifier material = Identifier.tryParse(nbt.getString(TRIM_MATERIAL_KEY));
        return material != null && registryManager.get(RegistryKeys.TRIM_MATERIAL).containsId(material);
    }

    /**
     * Checks if object is valid for head texture
     *
     * @param texture Base64 encoded texture object
     * @return Is object valid for head texture
     */
    public static boolean isValidHeadTexture(@NotNull String texture) {
        String value = new String(Base64.getDecoder().decode(texture));
        System.out.println(value);
        System.out.println(HEAD_TEXTURE_REGEX);
        System.out.println(value.matches(HEAD_TEXTURE_REGEX));
        return value.matches(HEAD_TEXTURE_REGEX);
    }

    /**
     * Checks if url is valid for head texture
     *
     * @param url Texture url
     * @return Is url valid for head texture
     */
    public static boolean isValidHeadTextureUrl(@NotNull String url) {
        return url.matches(HEAD_TEXTURE_URL_REGEX);
    }

    /**
     * Checks if sound id is valid
     *
     * @param sound Sound id
     * @return Is sound valid
     */
    public static boolean isValidSound(@NotNull String sound) {
        Identifier id = Identifier.tryParse(sound);
        if (id == null) return false;
        return Registries.SOUND_EVENT.containsId(id);
    }

    /**
     * Checks if compound have all valid tags to be parsed to potion effect
     *
     * @param nbt NBT compound to check
     * @return Is compound valid for potion effect
     */
    public static boolean isValidPotionEffect(@NotNull NbtCompound nbt) {
        if (!nbt.contains(CUSTOM_POTION_EFFECTS_ID_KEY, NbtElement.INT_TYPE)) return false;
        if (!nbt.contains(CUSTOM_POTION_EFFECTS_AMPLIFIER_KEY, NbtElement.INT_TYPE)) return false;
        if (!nbt.contains(CUSTOM_POTION_EFFECTS_DURATION_KEY, NbtElement.INT_TYPE)) return false;
        StatusEffect effect = StatusEffect.byRawId(nbt.getInt(CUSTOM_POTION_EFFECTS_ID_KEY));
        return effect != null;
    }

    /**
     * Checks if stack has attribute modifiers
     *
     * @param stack Item stack to check
     * @param validate Check for validity of attributes. False to only check for tag
     * @return Does item stack have attribute modifiers
     */
    public static boolean hasAttributes(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
        if (!validate) return !attributes.isEmpty();
        for (NbtElement attribute : attributes) {
            if (isValidAttribute((NbtCompound)attribute)) return true;
        }
        return false;
    }

    /**
     * Redirects to {@link #hasAttributes(ItemStack, boolean) hasAttributes} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid attribute modifiers
     */
    public static boolean hasAttributes(@NotNull ItemStack stack) {
        return hasAttributes(stack, true);
    }

    /**
     * Checks if stack has banner patterns
     *
     * @param stack Item stack to check
     * @param validate Check for validity of banner patterns. False to only check for tag
     * @return Does item stack have banner patterns
     */
    public static boolean hasBannerPatterns(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(BLOCK_ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG_KEY);
        if (!blockEntityTag.contains(BLOCK_ENTITY_TAG_PATTERNS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList patterns = blockEntityTag.getList(BLOCK_ENTITY_TAG_PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
        if (!validate) return !patterns.isEmpty();
        for (NbtElement pattern : patterns) {
            if (isValidPattern((NbtCompound)pattern)) return true;
        }
        return false;
    }

    /**
     * Redirects to {@link #hasBannerPatterns(ItemStack, boolean) hasBannerPatterns} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid banner patterns
     */
    public static boolean hasBannerPatterns(@NotNull ItemStack stack) {
        return hasBannerPatterns(stack, true);
    }

    /**
     * Checks if stack has book author
     *
     * @param stack Item stack to check
     * @return Does item stack have book author
     */
    public static boolean hasBookAuthor(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt.contains(AUTHOR_KEY, NbtElement.STRING_TYPE);
    }

    /**
     * Checks if stack has book title
     *
     * @param stack Item stack to check
     * @return Does item stack have book title
     */
    public static boolean hasBookTitle(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt.contains(TITLE_KEY, NbtElement.STRING_TYPE);
    }

    /**
     * Checks if stack has book pages
     *
     * @param stack Item stack to check
     * @return Does item stack have book pages
     */
    public static boolean hasBookPages(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(PAGES_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList pages = nbt.getList(PAGES_KEY, NbtElement.STRING_TYPE);
        return !pages.isEmpty();
    }

    /**
     * Checks if stack has custom color
     *
     * @param stack Item stack to check
     * @return Does item stack have custom color
     */
    public static boolean hasColor(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound display = nbt.getCompound(DISPLAY_KEY);
        return display.contains(getColorKey(stack.getItem()), NbtElement.INT_TYPE);
    }

    /**
     * Checks if stack has enchantments
     *
     * @param stack Item stack to check
     * @param validate Check for validity of enchantments. False to only check for tag
     * @return Does item stack have enchantments
     */
    public static boolean hasEnchantments(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList enchantments = nbt.getList(ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        if (!validate) return !enchantments.isEmpty();
        for (NbtElement enchantment : enchantments) {
            if (isValidEnchantment((NbtCompound)enchantment)) return true;
        }
        return false;
    }

    /**
     * Redirects to {@link #hasEnchantments(ItemStack, boolean) hasEnchantments} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid enchantments
     */
    public static boolean hasEnchantments(@NotNull ItemStack stack) {
        return hasEnchantments(stack, false);
    }

    /**
     * Checks if stack has firework explosions
     *
     * @param stack Item stack to check
     * @return Does item stack have firework explosions
     */
    public static boolean hasFireworkExplosions(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
        if (!fireworks.contains(FIREWORKS_EXPLOSIONS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList explosions = fireworks.getList(FIREWORKS_EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);
        // even empty explosion works, no need for validation
        return !explosions.isEmpty();
    }

    /**
     * Checks if stack has head owner name
     *
     * @param stack Item stack to check
     * @return Does item stack have head owner name
     */
    public static boolean hasHeadOwner(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(SKULL_OWNER_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound skullOwner = nbt.getCompound(SKULL_OWNER_KEY);
        return skullOwner.contains(SKULL_OWNER_NAME_KEY, NbtElement.STRING_TYPE);
    }

    /**
     * Checks if stack has head texture (not name)
     *
     * @param stack Item stack to check
     * @param validate Check for validity of texture. False to only check for tag
     * @return Does item stack have head texture
     */
    public static boolean hasHeadTexture(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(SKULL_OWNER_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound skullOwner = nbt.getCompound(SKULL_OWNER_KEY);
        if (!skullOwner.contains(SKULL_OWNER_PROPERTIES_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound properties = skullOwner.getCompound(SKULL_OWNER_PROPERTIES_KEY);
        if (!properties.contains(SKULL_OWNER_PROPERTIES_TEXTURES_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList textures = properties.getList(SKULL_OWNER_PROPERTIES_TEXTURES_KEY, NbtElement.COMPOUND_TYPE);
        if (textures.isEmpty()) return false;
        NbtCompound texture = textures.getCompound(0);
        if (!texture.contains(SKULL_OWNER_PROPERTIES_TEXTURES_VALUE_KEY, NbtElement.STRING_TYPE)) return false;
        if (!validate) return true;
        String value = texture.getString(SKULL_OWNER_PROPERTIES_TEXTURES_VALUE_KEY);
        return isValidHeadTexture(value);
    }

    /**
     * Redirects to {@link #hasHeadTexture(ItemStack, boolean) hasHeadTexture} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid head texture
     */
    public static boolean hasHeadTexture(@NotNull ItemStack stack) {
        return hasHeadTexture(stack, true);
    }

    /**
     * Checks if stack has note block sound
     *
     * @param stack Item stack to check
     * @param validate Check for validity of sound. False to only check for tag
     * @return Does item stack have note block sound
     */
    public static boolean hasNoteBlockSound(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(BLOCK_ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG_KEY);
        if (!blockEntityTag.contains(BLOCK_ENTITY_TAG_NOTE_BLOCK_SOUND, NbtElement.STRING_TYPE)) return false;
        if (!validate) return true;
        String sound = blockEntityTag.getString(BLOCK_ENTITY_TAG_NOTE_BLOCK_SOUND);
        return isValidSound(sound);
    }

    /**
     * Redirects to {@link #hasNoteBlockSound(ItemStack, boolean) hasNoteBlockSound} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid note block sound
     */
    public static boolean hasNoteBlockSound(@NotNull ItemStack stack) {
        return hasNoteBlockSound(stack, false);
    }

    /**
     * Checks if stack has lore
     *
     * @param stack Item stack to check
     * @return Does item stack have lore
     */
    public static boolean hasLore(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound display = nbt.getCompound(DISPLAY_KEY);
        if (!display.contains(DISPLAY_LORE_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList lore = display.getList(DISPLAY_LORE_KEY, NbtElement.STRING_TYPE);
        return !lore.isEmpty();
    }

    /**
     * Checks if stack has custom model
     *
     * @param stack Item stack to check
     * @return Does item stack have custom model
     */
    public static boolean hasModel(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(CUSTOM_MODEL_DATA_KEY, NbtElement.INT_TYPE)) return false;
        int model = nbt.getInt(CUSTOM_MODEL_DATA_KEY);
        return model > 0;
    }

    /**
     * Checks if stack has custom name
     *
     * @param stack Item stack to check
     * @return Does item stack have custom name
     */
    public static boolean hasName(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound display = nbt.getCompound(DISPLAY_KEY);
        return display.contains(DISPLAY_NAME_KEY, NbtElement.STRING_TYPE);
    }

    /**
     * Checks if stack has armor trim
     *
     * @param stack Item stack to check
     * @param validate Check for validity of armor trim. False to only check for tag
     * @param registryManager Dynamic registry manager to check in
     * @return Does item stack have armor trim
     */
    public static boolean hasTrim(@NotNull ItemStack stack, boolean validate, @Nullable DynamicRegistryManager registryManager) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(TRIM_KEY, NbtElement.COMPOUND_TYPE)) return false;
        if (!validate || registryManager == null) return true;
        NbtCompound trim = nbt.getCompound(TRIM_KEY);
        return isValidTrim(trim, registryManager);
    }

    /**
     * Checks if stack has placing whitelist
     *
     * @param stack Item stack to check
     * @param validate Check for validity of whitelist. False to only check for tag
     * @return Does item stack have placing whitelist
     */
    public static boolean hasWhitelistPlace(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(CAN_PLACE_ON_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList place = nbt.getList(CAN_PLACE_ON_KEY, NbtElement.STRING_TYPE);
        if (!validate) return !place.isEmpty();
        for (NbtElement element : place) {
            Identifier id = Identifier.tryParse(element.asString());
            if (id == null) continue;
            if (Registries.BLOCK.containsId(id)) return true;
        }
        return false;
    }

    /**
     * Redirects to {@link #hasWhitelistPlace(ItemStack, boolean) hasWhitelistPlace} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid placing whitelist
     */
    public static boolean hasWhitelistPlace(@NotNull ItemStack stack) {
        return hasWhitelistPlace(stack, true);
    }

    /**
     * Checks if stack has destroying whitelist
     *
     * @param stack Item stack to check
     * @param validate Check for validity of whitelist. False to only check for tag
     * @return Does item stack have destroying whitelist
     */
    public static boolean hasWhitelistDestroy(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(CAN_DESTROY_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList destroy = nbt.getList(CAN_DESTROY_KEY, NbtElement.STRING_TYPE);
        if (!validate) return !destroy.isEmpty();
        for (NbtElement element : destroy) {
            Identifier id = Identifier.tryParse(element.asString());
            if (id == null) continue;
            if (Registries.BLOCK.containsId(id)) return true;
        }
        return false;
    }

    /**
     * Redirects to {@link #hasWhitelistDestroy(ItemStack, boolean) hasWhitelistDestroy} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid destroying whitelist
     */
    public static boolean hasWhitelistDestroy(@NotNull ItemStack stack) {
        return hasWhitelistDestroy(stack, false);
    }

    /**
     * Checks if stack has potion effects
     *
     * @param stack Item stack to check
     * @param validate Check for validity of potion effects. False to only check for tag
     * @return Does item stack have potion effects
     */
    public static boolean hasPotionEffects(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(CUSTOM_POTION_EFFECTS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList customPotionEffects = nbt.getList(CUSTOM_POTION_EFFECTS_KEY, NbtElement.COMPOUND_TYPE);
        if (!validate) return !customPotionEffects.isEmpty();
        for (NbtElement customPotionEffect : customPotionEffects) {
            if (isValidPotionEffect((NbtCompound)customPotionEffect)) return true;
        }
        return false;
    }

    /**
     * Redirects to {@link #hasPotionEffects(ItemStack, boolean) hasPotionEffects} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid potion effects
     */
    public static boolean hasPotionEffects(@NotNull ItemStack stack) {
        return hasPotionEffects(stack, false);
    }

    public static @NotNull List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> getAttributes(@NotNull ItemStack stack) {
        if (!hasAttributes(stack, true)) return List.of();
        NbtList nbtAttributeModifiers = stack.getNbt().getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
        List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> attributes = new ArrayList<>();
        for (NbtElement nbtAttributeModifier : nbtAttributeModifiers) {
            NbtCompound attributeModifier = (NbtCompound)nbtAttributeModifier;
            if (!isValidAttribute(attributeModifier)) continue;
            double amount = attributeModifier.getDouble(ATTRIBUTE_MODIFIERS_AMOUNT_KEY);
            EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.fromId(attributeModifier.getInt(ATTRIBUTE_MODIFIERS_OPERATION_KEY));
            UUID uuid = attributeModifier.getUuid(ATTRIBUTE_MODIFIERS_UUID_KEY);
            String name = attributeModifier.getString(ATTRIBUTE_MODIFIERS_ATTRIBUTE_NAME_KEY);
            EntityAttribute attribute = Registries.ATTRIBUTE.get(Identifier.tryParse(name));
            EntityAttributeModifier modifier = new EntityAttributeModifier(uuid, name, amount, operation);
            try {
                EquipmentSlot slot = EquipmentSlot.byName(attributeModifier.getString(ATTRIBUTE_MODIFIERS_SLOT_KEY));
                attributes.add(Triple.of(attribute, modifier, slot));
            } catch (IllegalArgumentException e) {
                attributes.add(Triple.of(attribute, modifier, null));
            }
        }
        return attributes;
    }

    public static @NotNull List<Pair<BannerPattern, Integer>> getBannerPatterns(@NotNull ItemStack stack) {
        if (!hasBannerPatterns(stack, true)) return List.of();
        NbtList nbtPatterns = stack.getNbt().getCompound(BLOCK_ENTITY_TAG_KEY).getList(BLOCK_ENTITY_TAG_PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
        List<Pair<BannerPattern, Integer>> patterns = new ArrayList<>();
        for (NbtElement nbtPattern : nbtPatterns) {
            NbtCompound pattern = (NbtCompound)nbtPattern;
            if (!isValidPattern(pattern)) continue;
            int color = pattern.getInt(BLOCK_ENTITY_TAG_PATTERNS_COLOR_KEY);
            BannerPattern bannerPattern = BannerPattern.byId(pattern.getString(BLOCK_ENTITY_TAG_PATTERNS_PATTERN_KEY)).value();
            patterns.add(Pair.of(bannerPattern, color));
        }
        return patterns;
    }

    public static @Nullable String getBookAuthor(@NotNull ItemStack stack) {
        if (!hasBookAuthor(stack)) return null;
        return stack.getNbt().getString(AUTHOR_KEY);
    }

    public static @Nullable String getBookTitle(@NotNull ItemStack stack) {
        if (!hasBookTitle(stack)) return null;
        return stack.getNbt().getString(TITLE_KEY);
    }

    public static int getBookGeneration(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return 0;
        int generation = stack.getNbt().getInt(GENERATION_KEY);
        return generation >= 0 && generation < 3 ? generation : 0;
    }

    public static @NotNull List<Text> getBookPages(@NotNull ItemStack stack) {
        if (!hasBookPages(stack)) return List.of();
        List<Text> pages = new ArrayList<>();
        for (NbtElement page : stack.getNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE)) {
            pages.add(Text.Serializer.fromJson(page.asString()));
        }
        return pages;
    }

    public static int getColor(@NotNull ItemStack stack) {
        if (!hasColor(stack)) return 0;
        return stack.getNbt().getCompound(DISPLAY_KEY).getInt(getColorKey(stack.getItem()));
    }

    public static @NotNull HashMap<Enchantment, Integer> getEnchantments(@NotNull ItemStack stack) {
        if (!hasEnchantments(stack, true)) return new HashMap<>();
        NbtList nbtEnchantments = stack.getNbt().getList(ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        HashMap<Enchantment, Integer> enchantments = new HashMap<>();
        for (NbtElement nbtEnchantment : nbtEnchantments) {
            NbtCompound enchantment = (NbtCompound)nbtEnchantment;
            if (!isValidEnchantment(enchantment)) continue;
            String id = enchantment.getString(ENCHANTMENTS_ID_KEY);
            int lvl = enchantment.getInt(ENCHANTMENTS_LVL_KEY);
            enchantments.put(Registries.ENCHANTMENT.get(Identifier.tryParse(id)), lvl);
        }
        return enchantments;
    }

    public static @NotNull List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> getFireworkExplosions(@NotNull ItemStack stack) {
        if (!hasFireworkExplosions(stack)) return List.of();
        NbtList nbtExplosions = stack.getNbt().getCompound(FIREWORKS_KEY).getList(FIREWORKS_EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);
        List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> result = new ArrayList<>();
        for (NbtElement nbtExplosion : nbtExplosions) {
            NbtCompound explosion = (NbtCompound)nbtExplosion;
            int type = explosion.getInt(FIREWORKS_EXPLOSIONS_TYPE_KEY);
            int[] colors = explosion.getIntArray(FIREWORKS_EXPLOSIONS_COLORS_KEY);
            boolean flicker = explosion.getBoolean(FIREWORKS_EXPLOSIONS_FLICKER_KEY);
            boolean trail = explosion.getBoolean(FIREWORKS_EXPLOSIONS_TRAIL_KEY);
            int[] fadeColors = explosion.getIntArray(FIREWORKS_EXPLOSIONS_FADE_COLORS_KEY);
            result.add(new Quintet<>(type, Ints.asList(colors), flicker, trail, Ints.asList(fadeColors)));
        }
        return result;
    }

    public static int getFireworkFlight(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return 0;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) return 0;
        NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
        return fireworks.getInt(FIREWORKS_FLIGHT_KEY);
    }

    public static @Nullable String getHeadOwner(@NotNull ItemStack stack) {
        if (!hasHeadOwner(stack)) return null;
        return stack.getNbt().getCompound(SKULL_OWNER_KEY).getString(SKULL_OWNER_NAME_KEY);
    }

    public static @Nullable String getHeadTexture(@NotNull ItemStack stack) {
        if (!hasHeadTexture(stack, true)) return null;
        String texture = stack.getNbt().getCompound(SKULL_OWNER_KEY).getCompound(SKULL_OWNER_PROPERTIES_KEY).getList(SKULL_OWNER_PROPERTIES_TEXTURES_KEY, NbtElement.COMPOUND_TYPE).getCompound(0).getString(SKULL_OWNER_PROPERTIES_TEXTURES_VALUE_KEY);
        String textureObj = new String(Base64.getDecoder().decode(texture));
        return textureObj.substring(28, textureObj.length() - 4);
    }

    public static @Nullable SoundEvent getHeadSound(@NotNull ItemStack stack) {
        if (!hasNoteBlockSound(stack)) return null;
        Identifier id = Identifier.tryParse(stack.getNbt().getCompound(BLOCK_ENTITY_TAG_KEY).getString(BLOCK_ENTITY_TAG_NOTE_BLOCK_SOUND));
        if (id == null) return null;
        return Registries.SOUND_EVENT.get(id);
    }

    public static @NotNull List<Text> getLore(@NotNull ItemStack stack) {
        if (!hasLore(stack)) return List.of();
        NbtList nbtLore = stack.getNbt().getCompound(DISPLAY_KEY).getList(DISPLAY_LORE_KEY, NbtElement.STRING_TYPE);
        List<Text> lore = new ArrayList<>();
        for (NbtElement nbtLine : nbtLore) {
            Text line = Text.Serializer.fromJson(nbtLine.asString());
            lore.add(line);
        }
        return lore;
    }

    public static int getModel(@NotNull ItemStack stack) {
        if (!hasModel(stack)) return 0;
        return stack.getNbt().getInt(CUSTOM_MODEL_DATA_KEY);
    }

    public static @Nullable Text getName(@NotNull ItemStack stack) {
        if (!hasName(stack)) return null;
        return Text.Serializer.fromJson(stack.getNbt().getCompound(DISPLAY_KEY).getString(DISPLAY_NAME_KEY));
    }

    public static @Nullable ArmorTrim getTrim(@NotNull ItemStack stack, @NotNull DynamicRegistryManager registryManager) {
        if (!hasTrim(stack, true, registryManager)) return null;
        NbtCompound nbtTrim = stack.getNbt().getCompound(TRIM_KEY);
        Identifier patternId = Identifier.tryParse(nbtTrim.getString(TRIM_PATTERN_KEY));
        Identifier materialId = Identifier.tryParse(nbtTrim.getString(TRIM_MATERIAL_KEY));
        Registry<ArmorTrimPattern> patternRegistry = registryManager.get(RegistryKeys.TRIM_PATTERN);
        Registry<ArmorTrimMaterial> materialRegistry = registryManager.get(RegistryKeys.TRIM_MATERIAL);
        ArmorTrimPattern pattern = patternRegistry.get(patternId);
        ArmorTrimMaterial material = materialRegistry.get(materialId);
        return new ArmorTrim(materialRegistry.getEntry(material), patternRegistry.getEntry(pattern));
    }

    public static @NotNull List<Block> getWhitelistPlace(@NotNull ItemStack stack) {
        if (!hasWhitelistPlace(stack, true)) return List.of();
        NbtList nbtPlace = stack.getNbt().getList(CAN_PLACE_ON_KEY, NbtElement.STRING_TYPE);
        List<Block> result = new ArrayList<>();
        for (NbtElement nbtBlock : nbtPlace) {
            Identifier id = Identifier.tryParse(nbtBlock.asString());
            if (id == null || !Registries.BLOCK.containsId(id)) continue;
            Block block = Registries.BLOCK.get(id);
            result.add(block);
        }
        return result;
    }

    public static @NotNull List<Block> getWhitelistDestroy(@NotNull ItemStack stack) {
        if (!hasWhitelistDestroy(stack, true)) return List.of();
        NbtList nbtDestroy = stack.getNbt().getList(CAN_DESTROY_KEY, NbtElement.STRING_TYPE);
        List<Block> result = new ArrayList<>();
        for (NbtElement nbtBlock : nbtDestroy) {
            Identifier id = Identifier.tryParse(nbtBlock.asString());
            if (id == null || !Registries.BLOCK.containsId(id)) continue;
            Block block = Registries.BLOCK.get(id);
            result.add(block);
        }
        return result;
    }

    public static boolean getUnbreakable(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        return stack.getNbt().getBoolean(UNBREAKABLE_KEY);
    }

    public static @NotNull List<Boolean> getFlags(@NotNull ItemStack stack) {
        List<Boolean> result = new ArrayList<>();
        if (!stack.hasNbt()) return result;
        int flags = stack.getNbt().getInt(HIDE_FLAGS_KEY);
        for (int i = 0; i < FLAGS_AMOUNT; ++i) {
            int mask = 1 << i;
            result.add((flags & mask) == mask);
        }
        return result;
    }

    public static @NotNull HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> getPotionEffects(@NotNull ItemStack stack) {
        if (!hasPotionEffects(stack, true)) return new HashMap<>();
        HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> result = new HashMap<>();
        NbtList customPotionEffects = stack.getNbt().getList(CUSTOM_POTION_EFFECTS_KEY, NbtElement.COMPOUND_TYPE);
        for (NbtElement customPotionEffect : customPotionEffects) {
            NbtCompound potionEffect = (NbtCompound)customPotionEffect;
            if (!isValidPotionEffect(potionEffect)) continue;
            StatusEffect effect = StatusEffect.byRawId(potionEffect.getInt(CUSTOM_POTION_EFFECTS_ID_KEY));
            int amplifier = potionEffect.getInt(CUSTOM_POTION_EFFECTS_AMPLIFIER_KEY);
            int duration = potionEffect.getInt(CUSTOM_POTION_EFFECTS_DURATION_KEY);
            if (!potionEffect.contains(CUSTOM_POTION_EFFECTS_SHOW_PARTICLES_KEY, NbtElement.BYTE_TYPE)) {
                result.put(effect, Triple.of(amplifier, duration, null));
            } else {
                result.put(effect, Triple.of(amplifier, duration, potionEffect.getBoolean(CUSTOM_POTION_EFFECTS_SHOW_PARTICLES_KEY)));
            }
        }
        return result;
    }
    
    public static void setAttributes(@NotNull ItemStack stack, @Nullable List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            if (!hasAttributes(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(ATTRIBUTE_MODIFIERS_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList nbtAttributeModifiers = new NbtList();
            for (Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot> attribute : attributes) {
                NbtCompound nbtAttributeModifier = new NbtCompound();
                EntityAttributeModifier attributeModifier = attribute.getMiddle();
                EquipmentSlot slot = attribute.getRight();
                nbtAttributeModifier.putDouble(ATTRIBUTE_MODIFIERS_AMOUNT_KEY, attributeModifier.getValue());
                nbtAttributeModifier.putUuid(ATTRIBUTE_MODIFIERS_UUID_KEY, attributeModifier.getId());
                nbtAttributeModifier.putString(ATTRIBUTE_MODIFIERS_ATTRIBUTE_NAME_KEY, attributeModifier.getName());
                nbtAttributeModifier.putInt(ATTRIBUTE_MODIFIERS_OPERATION_KEY, attributeModifier.getOperation().getId());
                if (slot != null) nbtAttributeModifier.putString(ATTRIBUTE_MODIFIERS_SLOT_KEY, slot.getName());
                nbtAttributeModifiers.add(nbtAttributeModifier);
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(ATTRIBUTE_MODIFIERS_KEY, nbtAttributeModifiers);
            stack.setNbt(nbt);
        }
    }
    
    public static void setAttributePlaceholder(@NotNull ItemStack stack, boolean placeholder) {
        if (hasAttributes(stack) == placeholder) return;
        if (!placeholder) {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.remove(ATTRIBUTE_MODIFIERS_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList attributes = new NbtList();
            NbtCompound attribute = new NbtCompound();
            attributes.add(attribute);

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(ATTRIBUTE_MODIFIERS_KEY, attributes);
            stack.setNbt(nbt);
        }
    }

    public static void setBannerPatterns(@NotNull ItemStack stack, @Nullable List<Pair<BannerPattern, Integer>> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            if (!hasBannerPatterns(stack)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG_KEY);
            blockEntityTag.remove(BLOCK_ENTITY_TAG_PATTERNS_KEY);
            nbt.put(BLOCK_ENTITY_TAG_KEY, blockEntityTag);
            stack.setNbt(nbt);
        } else {
            NbtList nbtPatterns = new NbtList();
            for (Pair<BannerPattern, Integer> pattern : patterns) {
                NbtCompound nbtPattern = new NbtCompound();
                BannerPattern bannerPattern = pattern.getLeft();
                int color = pattern.getRight();
                nbtPattern.putString(BLOCK_ENTITY_TAG_PATTERNS_PATTERN_KEY, bannerPattern.getId());
                nbtPattern.putInt(BLOCK_ENTITY_TAG_PATTERNS_COLOR_KEY, color);
                nbtPatterns.add(nbtPattern);
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG_KEY);
            blockEntityTag.put(BLOCK_ENTITY_TAG_PATTERNS_KEY, nbtPatterns);
            nbt.put(BLOCK_ENTITY_TAG_KEY, blockEntityTag);
            stack.setNbt(nbt);
        }
    }

    public static void setBookAuthor(@NotNull ItemStack stack, @Nullable String author) {
        if (author == null) {
            if (!hasBookAuthor(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(AUTHOR_KEY);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putString(AUTHOR_KEY, author);
            stack.setNbt(nbt);
        }
    }

    public static void setBookTitle(@NotNull ItemStack stack, @Nullable String title) {
        if (title == null) {
            if (!hasBookTitle(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(TITLE_KEY);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putString(TITLE_KEY, title);
            stack.setNbt(nbt);
        }
    }

    public static void setBookGeneration(@NotNull ItemStack stack, @Nullable Integer generation) {
        if (generation == null || generation < 0 || generation >= GENERATIONS_AMOUNT) {
            if (!stack.hasNbt()) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(GENERATION_KEY);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putInt(GENERATION_KEY, generation);
            stack.setNbt(nbt);
        }
    }

    public static void setBookPages(@NotNull ItemStack stack, @Nullable List<Text> pages) {
        if (pages == null || pages.isEmpty()) {
            if (!hasBookPages(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(PAGES_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList nbtPages = new NbtList();
            for (Text page : pages) {
                nbtPages.add(NbtString.of(Text.Serializer.toJson(page)));
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(PAGES_KEY, nbtPages);
            stack.setNbt(nbt);
        }
    }

    public static void setColor(@NotNull ItemStack stack, @Nullable Integer color) {
        // TODO: support for more items such as firework stars and potions
        if (color == null) {
            if (!hasColor(stack)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            display.remove(getColorKey(stack.getItem()));
            nbt.put(DISPLAY_KEY, display);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            display.putInt(getColorKey(stack.getItem()), color);
            nbt.put(DISPLAY_KEY, display);
            stack.setNbt(nbt);
        }
    }

    public static void setEnchantments(@NotNull ItemStack stack, @Nullable HashMap<Enchantment, Integer> enchantments) {
        if (enchantments == null || enchantments.isEmpty()) {
            if (!hasEnchantments(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(ENCHANTMENTS_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList nbtEnchantments = new NbtList();
            for (Enchantment enchantment : enchantments.keySet()) {
                NbtCompound nbtEnchantment = new NbtCompound();
                nbtEnchantment.putString(ENCHANTMENTS_ID_KEY, Registries.ENCHANTMENT.getId(enchantment).toString());
                nbtEnchantment.putInt(ENCHANTMENTS_LVL_KEY, enchantments.get(enchantment));
                nbtEnchantments.add(nbtEnchantment);
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(ENCHANTMENTS_KEY, nbtEnchantments);
            stack.setNbt(nbt);
        }
    }

    public static void setEnchantmentGlint(@NotNull ItemStack stack, boolean glint) {
        if (hasEnchantments(stack) == glint) return;
        if (!glint) {
            NbtCompound nbt = stack.getNbt();
            nbt.remove(ENCHANTMENTS_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList enchantments = new NbtList();
            NbtCompound enchantment = new NbtCompound();
            enchantments.add(enchantment);

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(ENCHANTMENTS_KEY, enchantments);
            stack.setNbt(nbt);
        }
    }

    public static void setFireworkExplosions(@NotNull ItemStack stack, @Nullable List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions) {
        if (explosions == null || explosions.isEmpty()) {
            if (!hasFireworkExplosions(stack)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
            fireworks.remove(FIREWORKS_EXPLOSIONS_KEY);
            nbt.put(FIREWORKS_KEY, fireworks);
            stack.setNbt(nbt);
        } else {
            NbtList nbtExplosions = new NbtList();
            for (Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>> explosion : explosions) {
                NbtCompound nbtExplosion = new NbtCompound();
                nbtExplosion.putInt(FIREWORKS_EXPLOSIONS_TYPE_KEY, explosion.getA());
                nbtExplosion.putIntArray(FIREWORKS_EXPLOSIONS_COLORS_KEY, explosion.getB());
                nbtExplosion.putBoolean(FIREWORKS_EXPLOSIONS_FLICKER_KEY, explosion.getC());
                nbtExplosion.putBoolean(FIREWORKS_EXPLOSIONS_TRAIL_KEY, explosion.getD());
                nbtExplosion.putIntArray(FIREWORKS_EXPLOSIONS_FADE_COLORS_KEY, explosion.getE());
                nbtExplosions.add(nbtExplosion);
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
            fireworks.put(FIREWORKS_EXPLOSIONS_KEY, nbtExplosions);
            nbt.put(FIREWORKS_KEY, fireworks);
            stack.setNbt(nbt);
        }
    }

    public static void setFireworkFlight(@NotNull ItemStack stack, @Nullable Integer flight) {
        if (flight == null) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
            fireworks.remove(FIREWORKS_FLIGHT_KEY);
            nbt.put(FIREWORKS_KEY, fireworks);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
            fireworks.putInt(FIREWORKS_FLIGHT_KEY, flight);
            nbt.put(FIREWORKS_KEY, fireworks);
            stack.setNbt(nbt);
        }
    }

    public static void setHeadOwner(@NotNull ItemStack stack, @Nullable String owner) {
        if (owner == null) {
            if (!hasHeadOwner(stack)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound skullOwner = nbt.getCompound(SKULL_OWNER_KEY);
            skullOwner.remove(SKULL_OWNER_NAME_KEY);
            nbt.put(SKULL_OWNER_KEY, skullOwner);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putString(SKULL_OWNER_KEY, owner);
            stack.setNbt(nbt);
        }
    }

    public static void setHeadTexture(@NotNull ItemStack stack, @Nullable String texture) {
        if (texture == null) {
            if (!hasHeadTexture(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(SKULL_OWNER_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList textures = new NbtList();
            NbtCompound nbtTexture = new NbtCompound();
            String textureObj = new String(Base64.getEncoder().encode(String.format(HEAD_TEXTURE_OBJECT, texture).getBytes()));
            nbtTexture.putString(SKULL_OWNER_PROPERTIES_TEXTURES_VALUE_KEY, textureObj);
            textures.add(nbtTexture);

            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound skullOwner = nbt.getCompound(SKULL_OWNER_KEY);
            NbtCompound properties = skullOwner.getCompound(SKULL_OWNER_PROPERTIES_KEY);
            properties.put(SKULL_OWNER_PROPERTIES_TEXTURES_KEY, textures);
            skullOwner.put(SKULL_OWNER_PROPERTIES_KEY, properties);
            nbt.put(SKULL_OWNER_KEY, skullOwner);
            stack.setNbt(nbt);
        }
    }

    public static void setHeadSound(@NotNull ItemStack stack, @Nullable SoundEvent sound) {
        if (sound == null) {
            if (!hasNoteBlockSound(stack)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG_KEY);
            blockEntityTag.remove(BLOCK_ENTITY_TAG_NOTE_BLOCK_SOUND);
            nbt.put(BLOCK_ENTITY_TAG_KEY, blockEntityTag);
            stack.setNbt(nbt);
        } else {
            String id = Registries.SOUND_EVENT.getId(sound).toString();

            NbtCompound nbt = stack.getNbt();
            NbtCompound blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG_KEY);
            blockEntityTag.putString(BLOCK_ENTITY_TAG_NOTE_BLOCK_SOUND, id);
            nbt.put(BLOCK_ENTITY_TAG_KEY, blockEntityTag);
            stack.setNbt(nbt);
        }
    }

    public static void setLore(@NotNull ItemStack stack, @Nullable List<Text> lore) {
        if (lore == null || lore.isEmpty()) {
            if (!hasLore(stack)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            display.remove(DISPLAY_LORE_KEY);
            nbt.put(DISPLAY_KEY, display);
            stack.setNbt(nbt);
        } else {
            NbtList nbtLore = new NbtList();
            for (Text line : lore) {
                nbtLore.add(NbtString.of(Text.Serializer.toJson(line)));
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            display.put(DISPLAY_LORE_KEY, nbtLore);
            nbt.put(DISPLAY_KEY, display);
            stack.setNbt(nbt);
        }
    }

    public static void setModel(@NotNull ItemStack stack, @Nullable Integer model) {
        if (model == null || model <= 0) {
            if (!hasModel(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(CUSTOM_MODEL_DATA_KEY);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putInt(CUSTOM_MODEL_DATA_KEY, model);
            stack.setNbt(nbt);
        }
    }

    public static void setName(@NotNull ItemStack stack, @Nullable Text name) {
        if (name == null) {
            if (!hasName(stack)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            display.remove(DISPLAY_NAME_KEY);
            nbt.put(DISPLAY_KEY, display);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            display.putString(DISPLAY_NAME_KEY, Text.Serializer.toJson(name));
            nbt.put(DISPLAY_KEY, display);
            stack.setNbt(nbt);
        }
    }

    public static void setTrim(@NotNull ItemStack stack, @Nullable ArmorTrim trim) {
        if (trim == null) {
            if (!hasTrim(stack, false, null)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(TRIM_KEY);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbtTrim = new NbtCompound();
            nbtTrim.putString(TRIM_PATTERN_KEY, trim.getPattern().value().assetId().toString());
            nbtTrim.putString(TRIM_MATERIAL_KEY, trim.getMaterial().value().assetName());

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(TRIM_KEY, nbtTrim);
            stack.setNbt(nbt);
        }
    }

    public static void setTrim(@NotNull ItemStack stack, @NotNull ArmorTrimPattern pattern, @NotNull ArmorTrimMaterial material) {
        NbtCompound nbtTrim = new NbtCompound();
        nbtTrim.putString(TRIM_PATTERN_KEY, pattern.assetId().toString());
        nbtTrim.putString(TRIM_MATERIAL_KEY, material.assetName());

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.put(TRIM_KEY, nbtTrim);
        stack.setNbt(nbt);
    }

    public static void setWhitelistPlace(@NotNull ItemStack stack, @Nullable List<Block> place) {
        if (place == null || place.isEmpty()) {
            if (!hasWhitelistPlace(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(CAN_PLACE_ON_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList nbtPlace = new NbtList();
            for (Block block : place) {
                nbtPlace.add(NbtString.of(Registries.BLOCK.getId(block).toString()));
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(CAN_PLACE_ON_KEY, nbtPlace);
            stack.setNbt(nbt);
        }
    }

    public static void setWhitelistDestroy(@NotNull ItemStack stack, @Nullable List<Block> destroy) {
        if (destroy == null || destroy.isEmpty()) {
            if (!hasWhitelistDestroy(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(CAN_DESTROY_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList nbtDestroy = new NbtList();
            for (Block block : destroy) {
                nbtDestroy.add(NbtString.of(Registries.BLOCK.getId(block).toString()));
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(CAN_DESTROY_KEY, nbtDestroy);
            stack.setNbt(nbt);
        }
    }

    public static void setUnbreakable(@NotNull ItemStack stack, @Nullable Boolean unbreakable) {
        if (unbreakable == null) {
            if (!stack.hasNbt()) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(UNBREAKABLE_KEY);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putBoolean(UNBREAKABLE_KEY, unbreakable);
            stack.setNbt(nbt);
        }
    }

    public static void setFlags(@NotNull ItemStack stack, @Nullable List<Boolean> flags) {
        if (flags == null) {
            if (!stack.hasNbt()) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(HIDE_FLAGS_KEY);
            stack.setNbt(nbt);
        } else {
            int result = 0;
            for (int i = 0; i < flags.size(); ++i) {
                if (flags.get(i)) result += 1 << i;
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putInt(HIDE_FLAGS_KEY, result);
            stack.setNbt(nbt);
        }
    }

    public static void setPotionEffects(@NotNull ItemStack stack, @Nullable HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> effects) {
        if (effects == null || effects.isEmpty()) {
            if (!hasPotionEffects(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(CUSTOM_POTION_EFFECTS_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList potionEffects = new NbtList();
            for (StatusEffect effect : effects.keySet()) {
                NbtCompound potionEffect = new NbtCompound();
                Triple<Integer, Integer, Boolean> data = effects.get(effect);
                potionEffect.putInt(CUSTOM_POTION_EFFECTS_ID_KEY, StatusEffect.getRawId(effect));
                potionEffect.putInt(CUSTOM_POTION_EFFECTS_AMPLIFIER_KEY, data.getLeft());
                potionEffect.putInt(CUSTOM_POTION_EFFECTS_DURATION_KEY, data.getMiddle());
                if (data.getRight() != null) potionEffect.putBoolean(CUSTOM_POTION_EFFECTS_SHOW_PARTICLES_KEY, data.getRight());
                potionEffects.add(potionEffect);
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(CUSTOM_POTION_EFFECTS_KEY, potionEffects);
            stack.setNbt(nbt);
        }
    }
}
