package me.white.simpleitemeditor.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
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
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import oshi.util.tuples.Quintet;

public class ItemUtil {
    public static final String ATTRIBUTE_MODIFIERS_KEY = "AttributeModifiers";
    public static final String ATTRIBUTE_MODIFIERS_AMOUNT_KEY = "Amount";
    public static final String ATTRIBUTE_MODIFIERS_ATTRIBUTE_NAME_KEY = "AttributeName";
    public static final String ATTRIBUTE_MODIFIERS_OPERATION_KEY = "Operation";
    public static final String ATTRIBUTE_MODIFIERS_SLOT_KEY = "Slot";
    public static final String ATTRIBUTE_MODIFIERS_UUID_KEY = "UUID";
    public static final String AUTHOR_KEY = "author";
    public static final String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";
    public static final String BLOCK_ENTITY_TAG_NOTE_BLOCK_SOUND = "note_block_sound";
    public static final String BLOCK_ENTITY_TAG_PATTERNS_KEY = "Patterns";
    public static final String BLOCK_ENTITY_TAG_PATTERNS_COLOR_KEY = "Color";
    public static final String BLOCK_ENTITY_TAG_PATTERNS_PATTERN_KEY = "Pattern";
    public static final String CAN_DESTROY_KEY = "CanDestroy";
    public static final String CAN_PLACE_ON_KEY = "CanPlaceOn";
    public static final String CUSTOM_MODEL_DATA_KEY = "CustomModelData";
    public static final String CUSTOM_POTION_COLOR_KEY = "CustomPotionColor";
    public static final String CUSTOM_POTION_EFFECTS_KEY = "custom_potion_effects";
    public static final String CUSTOM_POTION_EFFECTS_ID_KEY = "id";
    public static final String CUSTOM_POTION_EFFECTS_AMPLIFIER_KEY = "amplifier";
    public static final String CUSTOM_POTION_EFFECTS_DURATION_KEY = "duration";
    public static final String CUSTOM_POTION_EFFECTS_SHOW_PARTICLES_KEY = "show_particles";
    public static final String DISPLAY_KEY = "display";
    public static final String DISPLAY_COLOR_KEY = "color";
    public static final String DISPLAY_LORE_KEY = "Lore";
    public static final String DISPLAY_MAP_COLOR_KEY = "MapColor";
    public static final String DISPLAY_NAME_KEY = "Name";
    public static final String ENCHANTMENTS_KEY = "Enchantments";
    public static final String ENCHANTMENTS_ID_KEY = "id";
    public static final String ENCHANTMENTS_LVL_KEY = "lvl";
    public static final String ENTITY_TAG_KEY = "EntityTag";
    public static final String ENTITY_TAG_ABSORPTION_AMOUNT_KEY = "AbsorptionAmount";
    public static final String ENTITY_TAG_AIR_KEY = "Air";
    public static final String ENTITY_TAG_CAN_PICK_UP_LOOT_KEY = "CanPickUpLoot";
    public static final String ENTITY_TAG_GLOWING_KEY = "Glowing";
    public static final String ENTITY_TAG_HEALTH_KEY = "Health";
    public static final String ENTITY_TAG_ID_KEY = "id";
    public static final String ENTITY_TAG_INVISIBLE_KEY = "Invisible";
    public static final String ENTITY_TAG_INVULNERABLE_KEY = "Invulnerable";
    public static final String ENTITY_TAG_MOTION_KEY = "Motion";
    public static final String ENTITY_TAG_NO_AI_KEY = "NoAI";
    public static final String ENTITY_TAG_NO_GRAVITY_KEY = "NoGravity";
    public static final String ENTITY_TAG_PERSISTANCE_REQUIRED_KEY = "PersistanceRequired";
    public static final String ENTITY_TAG_POS_KEY = "Pos";
    public static final String ENTITY_TAG_ROTATION_KEY = "Rotation";
    public static final String ENTITY_TAG_SILENT_KEY = "Silent";
    public static final String EXPLOSION_KEY = "Explosion";
    public static final String EXPLOSION_COLORS_KEY = "Colors";
    public static final String FIREWORKS_KEY = "Fireworks";
    public static final String FIREWORKS_EXPLOSIONS_KEY = "Explosions";
    public static final String FIREWORKS_EXPLOSIONS_COLORS_KEY = "Colors";
    public static final String FIREWORKS_EXPLOSIONS_FADE_COLORS_KEY = "FadeColors";
    public static final String FIREWORKS_EXPLOSIONS_FLICKER_KEY = "Flicker";
    public static final String FIREWORKS_EXPLOSIONS_TRAIL_KEY = "Trail";
    public static final String FIREWORKS_EXPLOSIONS_TYPE_KEY = "Type";
    public static final String FIREWORKS_FLIGHT_KEY = "Flight";
    public static final String GENERATION_KEY = "generation";
    public static final String PAGES_KEY = "pages";
    public static final String SKULL_OWNER_KEY = "SkullOwner";
    public static final String SKULL_OWNER_ID_KEY = "Id";
    public static final String SKULL_OWNER_NAME_KEY = "Name";
    public static final String SKULL_OWNER_PROPERTIES_KEY = "Properties";
    public static final String SKULL_OWNER_PROPERTIES_TEXTURES_KEY = "textures";
    public static final String SKULL_OWNER_PROPERTIES_TEXTURES_VALUE_KEY = "Value";
    public static final String SKULL_OWNER_PROPERTIES_TEXTURES_SIGNATURE_KEY = "Signature";
    public static final String TITLE_KEY = "title";
    public static final String TRIM_KEY = "Trim";
    public static final String TRIM_MATERIAL_KEY = "material";
    public static final String TRIM_PATTERN_KEY = "pattern";
    public static final String UNBREAKABLE_KEY = "Unbreakable";
    public static final String HIDE_FLAGS_KEY = "HideFlags";
    private static final String HEAD_TEXTURE_OBJECT = "{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}";
    public static final int FLAGS_AMOUNT = 8;
    public static final int GENERATIONS_AMOUNT = 4;

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
     * Gets attribute modifiers from stack
     *
     * @param stack Item stack to get from
     * @return Attribute modifiers from item stack
     */
    public static @NotNull List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> getAttributes(@NotNull ItemStack stack) {
        if (!hasAttributes(stack)) return List.of();
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

    /**
     * Sets attribute modifiers to the stack
     *
     * @param stack Item stack to modify
     * @param attributes Attribute modifiers to set
     */
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

    /**
     * Sets attribute modifiers placeholder to remove default attribute modifiers
     *
     * @param stack Item stack to modify
     * @param placeholder Do apply placeholder
     */
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
     * Gets banner patterns from stack
     *
     * @param stack Item stack to get from
     * @return Banner patterns from item stack
     */
    public static @NotNull List<Pair<BannerPattern, Integer>> getBannerPatterns(@NotNull ItemStack stack) {
        if (!hasBannerPatterns(stack)) return List.of();
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

    /**
     * Sets banner patterns to the stack
     *
     * @param stack Item stack to modify
     * @param patterns Banner patterns to set. Removes tag if null or empty
     */
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
     * Gets book author from stack
     *
     * @param stack Item stack to get from
     * @return Book author from item stack, or null if none is present
     */
    public static @Nullable String getBookAuthor(@NotNull ItemStack stack) {
        if (!hasBookAuthor(stack)) return null;
        return stack.getNbt().getString(AUTHOR_KEY);
    }

    /**
     * Sets book author to the stack
     *
     * @param stack Item stack to modify
     * @param author Book author to set. Removes tag if null
     */
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
     * Gets book title from stack
     *
     * @param stack Item stack to get from
     * @return Book title from item stack, or null if none is present
     */
    public static @Nullable String getBookTitle(@NotNull ItemStack stack) {
        if (!hasBookTitle(stack)) return null;
        return stack.getNbt().getString(TITLE_KEY);
    }

    /**
     * Sets book title to the stack
     *
     * @param stack Item stack to modify
     * @param title Book title to set. Removes tag if null
     */
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

    /**
     * Gets book generation from stack
     *
     * @param stack Item stack to get from
     * @return Book generation id from item stack
     */
    public static int getBookGeneration(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return 0;
        int generation = stack.getNbt().getInt(GENERATION_KEY);
        return generation >= 0 && generation < GENERATIONS_AMOUNT ? generation : 0;
    }

    /**
     * Sets book generation to the stack
     *
     * @param stack Item stack to modify
     * @param generation Book generation to set. Removes tag if null or invalid
     */
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
     * Gets book pages from stack
     *
     * @param stack Item stack to get from
     * @return Book pages from item stack
     */
    public static @NotNull List<Text> getBookPages(@NotNull ItemStack stack) {
        if (!hasBookPages(stack)) return List.of();
        List<Text> pages = new ArrayList<>();
        for (NbtElement page : stack.getNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE)) {
            pages.add(Text.Serializer.fromJson(page.asString()));
        }
        return pages;
    }

    /**
     * Sets book pages to the stack
     *
     * @param stack Item stack to modify
     * @param pages Book pages to set. Removes tag if null or empty
     */
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

    /**
     * Checks if stack has custom color
     *
     * @param stack Item stack to check
     * @return Does item stack have custom color
     */
    public static boolean hasColor(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        Item item = stack.getItem();
        if (item instanceof ArmorItem || item instanceof HorseArmorItem) {
            if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return false;
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            return display.contains(DISPLAY_COLOR_KEY, NbtElement.INT_TYPE);
        } else if (item instanceof FilledMapItem) {
            if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return false;
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            return display.contains(DISPLAY_MAP_COLOR_KEY, NbtElement.INT_TYPE);
        } else if (item instanceof PotionItem || item instanceof TippedArrowItem) {
            return nbt.contains(CUSTOM_POTION_COLOR_KEY, NbtElement.INT_TYPE);
        } else if (item instanceof FireworkStarItem) {
            if (!nbt.contains(EXPLOSION_KEY, NbtElement.COMPOUND_TYPE)) return false;
            NbtCompound explosion = nbt.getCompound(EXPLOSION_KEY);
            return explosion.contains(EXPLOSION_COLORS_KEY, NbtElement.INT_ARRAY_TYPE);
        }
        return false;
    }

    /**
     * Gets custom color from stack
     *
     * @param stack Item stack to get from
     * @return Custom color from item stack, or null if none is present
     */
    public static @Nullable Integer getColor(@NotNull ItemStack stack) {
        if (!hasColor(stack)) return null;
        NbtCompound nbt = stack.getNbt();
        Item item = stack.getItem();
        if (item instanceof ArmorItem || item instanceof HorseArmorItem) {
            if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return null;
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            if (!display.contains(DISPLAY_COLOR_KEY, NbtElement.INT_TYPE)) return null;
            return display.getInt(DISPLAY_COLOR_KEY);
        } else if (item instanceof FilledMapItem) {
            if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return null;
            NbtCompound display = nbt.getCompound(DISPLAY_KEY);
            if (!display.contains(DISPLAY_MAP_COLOR_KEY, NbtElement.INT_TYPE)) return null;
            return display.getInt(DISPLAY_MAP_COLOR_KEY);
        } else if (item instanceof PotionItem || item instanceof TippedArrowItem) {
            if (!nbt.contains(CUSTOM_POTION_COLOR_KEY, NbtElement.INT_TYPE)) return null;
            return nbt.getInt(CUSTOM_POTION_COLOR_KEY);
        } else if (item instanceof FireworkStarItem) {
            if (!nbt.contains(EXPLOSION_KEY, NbtElement.COMPOUND_TYPE)) return null;
            NbtCompound explosion = nbt.getCompound(EXPLOSION_KEY);
            if (!explosion.contains(EXPLOSION_COLORS_KEY, NbtElement.INT_ARRAY_TYPE)) return null;
            return EditorUtil.meanColor(explosion.getIntArray(EXPLOSION_COLORS_KEY));
        }
        return null;
    }

    /**
     * Sets custom color to the stack
     *
     * @param stack Item stack to modify
     * @param color Custom color to set. Removes tag if null
     */
    public static void setColor(@NotNull ItemStack stack, @Nullable Integer color) {
        Item item = stack.getItem();
        if (color == null) {
            if (!hasColor(stack)) return;
            NbtCompound nbt = stack.getNbt();
            if (item instanceof ArmorItem || item instanceof HorseArmorItem) {
                NbtCompound display = nbt.getCompound(DISPLAY_KEY);
                display.remove(DISPLAY_COLOR_KEY);
                nbt.put(DISPLAY_KEY, display);
            } else if (item instanceof FilledMapItem) {
                NbtCompound display = nbt.getCompound(DISPLAY_KEY);
                display.remove(DISPLAY_MAP_COLOR_KEY);
                nbt.put(DISPLAY_KEY, display);
            } else if (item instanceof PotionItem || item instanceof TippedArrowItem) {
                nbt.remove(CUSTOM_POTION_COLOR_KEY);
            } else if (item instanceof FireworkStarItem) {
                NbtCompound explosion = nbt.getCompound(EXPLOSION_KEY);
                explosion.remove(EXPLOSION_COLORS_KEY);
                nbt.put(EXPLOSION_KEY, explosion);
            }
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            if (item instanceof ArmorItem || item instanceof HorseArmorItem) {
                NbtCompound display = nbt.getCompound(DISPLAY_KEY);
                display.putInt(DISPLAY_COLOR_KEY, color);
                nbt.put(DISPLAY_KEY, display);
            } else if (item instanceof FilledMapItem) {
                NbtCompound display = nbt.getCompound(DISPLAY_KEY);
                display.putInt(DISPLAY_MAP_COLOR_KEY, color);
                nbt.put(DISPLAY_KEY, display);
            } else if (item instanceof PotionItem || item instanceof TippedArrowItem) {
                nbt.putInt(CUSTOM_POTION_COLOR_KEY, color);
            } else if (item instanceof FireworkStarItem) {
                NbtCompound explosion = nbt.getCompound(EXPLOSION_KEY);
                explosion.putIntArray(EXPLOSION_COLORS_KEY, new int[] { color });
                nbt.put(EXPLOSION_KEY, explosion);
            }
            stack.setNbt(nbt);
        }
    }

    /**
     * Checks if compound have all valid tags to be parsed to enchantment
     *
     * @param nbt NBT compound to check
     * @return Is compound valid for enchantment
     */
    public static boolean isValidEnchantment(@NotNull NbtCompound nbt) {
        if (nbt.isEmpty()) return false;
        if (!nbt.contains(ENCHANTMENTS_LVL_KEY, NbtElement.INT_TYPE)) return false;
        if (!nbt.contains(ENCHANTMENTS_ID_KEY, NbtElement.STRING_TYPE)) {
            if (!nbt.contains(ENCHANTMENTS_ID_KEY, NbtElement.SHORT_TYPE)) return false;
            int id = nbt.getShort(ENCHANTMENTS_ID_KEY);
            return id >= 0 && id < Registries.ENCHANTMENT.size();
        }
        Identifier id = Identifier.tryParse(nbt.getString(ENCHANTMENTS_ID_KEY));
        if (id == null) return false;
        return Registries.ENCHANTMENT.containsId(id);
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
        return hasEnchantments(stack, true);
    }

    /**
     * Gets enchantments from stack
     *
     * @param stack Item stack to get from
     * @return Enchantments from item stack
     */
    public static @NotNull HashMap<Enchantment, Integer> getEnchantments(@NotNull ItemStack stack) {
        if (!hasEnchantments(stack)) return new HashMap<>();
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

    /**
     * Sets enchantments to the stack
     *
     * @param stack Item stack to modify
     * @param enchantments Enchantments to set. Removes tag if null or empty
     */
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

    /**
     * Sets enchantment glint to the stack
     *
     * @param stack Item stack to modify
     * @param glint Do apply glint
     */
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
     * Gets firework explosions from stack
     *
     * @param stack Item stack to get from
     * @return Firework explosions from item stack
     */
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

    /**
     * Sets firework explosions to the stack
     *
     * @param stack Item stack to modify
     * @param explosions Firework explosions to set. Removes tag if null or empty
     */
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

    /**
     * Gets firework flight from stack
     *
     * @param stack Item stack to get from
     * @return Firework flight from item stack
     */
    public static int getFireworkFlight(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return 0;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) return 0;
        NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
        return fireworks.getInt(FIREWORKS_FLIGHT_KEY);
    }

    /**
     * Sets firework flight to the stack
     *
     * @param stack Item stack to modify
     * @param flight Firework flight to set. Removes tag if null
     */
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
     * Gets head owner name from stack
     *
     * @param stack Item stack to get from
     * @return Head owner name from item stack, or null if none is present
     */
    public static @Nullable String getHeadOwner(@NotNull ItemStack stack) {
        if (!hasHeadOwner(stack)) return null;
        return stack.getNbt().getCompound(SKULL_OWNER_KEY).getString(SKULL_OWNER_NAME_KEY);
    }

    /**
     * Sets head owner name to the stack
     *
     * @param stack Item stack to modify
     * @param owner Head owner name to set. Removes tag if null
     */
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

    /**
     * Checks if object is valid for head texture
     *
     * @param texture Base64 encoded texture object
     * @return Is object valid for head texture
     */
    public static boolean isValidHeadTexture(@NotNull String texture) {
        try {
            String value = new String(Base64.getDecoder().decode(texture));
            JsonObject textureObj = JsonParser.parseString(value).getAsJsonObject();
            System.out.println(textureObj.toString());
            if (!textureObj.has("textures")) return false;
            JsonObject textures = textureObj.getAsJsonObject("textures");
            System.out.println(textures.toString());
            if (!textures.has("SKIN")) return false;
            JsonObject skin = textures.getAsJsonObject("SKIN");
            System.out.println(skin.toString());
            if (!skin.has("url")) return false;
            URL url = new URL(skin.get("url").getAsString());
            return isValidHeadTextureUrl(url);
        } catch (JsonParseException | IllegalStateException | ClassCastException | UnsupportedOperationException | IllegalArgumentException | MalformedURLException e) {
            System.out.println("failed");
            return false;
        }
    }

    /**
     * Checks if url is valid for head texture
     *
     * @param url Texture url
     * @return Is url valid for head texture
     */
    public static boolean isValidHeadTextureUrl(@NotNull URL url) {
        String path = url.getPath();
        return url.getHost().equals("textures.minecraft.net") && path.startsWith("/texture/") && path.substring(9).matches("[a-fA-F0-9]+");
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
     * Gets head texture from stack
     *
     * @param stack Item stack to get from
     * @return Head texture from item stack, or null if none is present
     */
    public static @Nullable URL getHeadTexture(@NotNull ItemStack stack) {
        if (!hasHeadTexture(stack)) return null;
        String texture = stack.getNbt().getCompound(SKULL_OWNER_KEY).getCompound(SKULL_OWNER_PROPERTIES_KEY).getList(SKULL_OWNER_PROPERTIES_TEXTURES_KEY, NbtElement.COMPOUND_TYPE).getCompound(0).getString(SKULL_OWNER_PROPERTIES_TEXTURES_VALUE_KEY);
        try {
            String value = new String(Base64.getDecoder().decode(texture));
            JsonObject textures = JsonParser.parseString(value).getAsJsonObject().getAsJsonObject("textures");
            return new URL(textures.getAsJsonObject("SKIN").get("url").getAsString());
        } catch (JsonParseException | IllegalStateException | ClassCastException | UnsupportedOperationException | IllegalArgumentException | MalformedURLException e) {
            return null;
        }
    }

    /**
     * Sets head texture to the stack
     *
     * @param stack Item stack to modify
     * @param texture URL to head texture to set. Removes tag if null
     */
    public static void setHeadTexture(@NotNull ItemStack stack, @Nullable URL texture) {
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
            skullOwner.putUuid(SKULL_OWNER_ID_KEY, UUID.randomUUID());
            nbt.put(SKULL_OWNER_KEY, skullOwner);
            stack.setNbt(nbt);
        }
    }

    /**
     * Sets head texture to the stack
     *
     * @param stack Item stack to modify
     * @param value value of head texture to set
     * @param signature signature of head texture to set
     */
    public static void setHeadTexture(@NotNull ItemStack stack, @NotNull String value, @Nullable String signature) {
        NbtList textures = new NbtList();
        NbtCompound nbtTexture = new NbtCompound();
        nbtTexture.putString(SKULL_OWNER_PROPERTIES_TEXTURES_VALUE_KEY, value);
        if (signature != null) nbtTexture.putString(SKULL_OWNER_PROPERTIES_TEXTURES_SIGNATURE_KEY, signature);
        textures.add(nbtTexture);

        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound skullOwner = nbt.getCompound(SKULL_OWNER_KEY);
        NbtCompound properties = skullOwner.getCompound(SKULL_OWNER_PROPERTIES_KEY);
        properties.put(SKULL_OWNER_PROPERTIES_TEXTURES_KEY, textures);
        skullOwner.put(SKULL_OWNER_PROPERTIES_KEY, properties);
        skullOwner.putUuid(SKULL_OWNER_ID_KEY, UUID.randomUUID());
        nbt.put(SKULL_OWNER_KEY, skullOwner);
        stack.setNbt(nbt);
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
        return hasNoteBlockSound(stack, true);
    }

    /**
     * Gets note block sound from stack
     *
     * @param stack Item stack to get from
     * @return Note block sound from item stack, or null if none is present
     */
    public static @Nullable SoundEvent getNoteBlockSound(@NotNull ItemStack stack) {
        if (!hasNoteBlockSound(stack)) return null;
        Identifier id = Identifier.tryParse(stack.getNbt().getCompound(BLOCK_ENTITY_TAG_KEY).getString(BLOCK_ENTITY_TAG_NOTE_BLOCK_SOUND));
        if (id == null) return null;
        return Registries.SOUND_EVENT.get(id);
    }

    /**
     * Sets note block sound to the stack
     *
     * @param stack Item stack to modify
     * @param sound Note block sound to set. Removes tag if null
     */
    public static void setNoteBlockSound(@NotNull ItemStack stack, @Nullable SoundEvent sound) {
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
     * Gets lore from stack
     *
     * @param stack Item stack to get from
     * @return Lore from item stack
     */
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

    /**
     * Sets lore to the stack
     *
     * @param stack Item stack to modify
     * @param lore Lore to set. Removes tag if null or empty
     */
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

    /**
     * Gets custom model from stack
     *
     * @param stack Item stack to get from
     * @return Custom model from item stack
     */
    public static int getModel(@NotNull ItemStack stack) {
        if (!hasModel(stack)) return 0;
        return stack.getNbt().getInt(CUSTOM_MODEL_DATA_KEY);
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
     * Gets custom name from stack
     *
     * @param stack Item stack to get from
     * @return Custom name from item stack
     */
    public static @Nullable Text getName(@NotNull ItemStack stack) {
        if (!hasName(stack)) return null;
        return Text.Serializer.fromJson(stack.getNbt().getCompound(DISPLAY_KEY).getString(DISPLAY_NAME_KEY));
    }

    /**
     * Sets custom name to the stack
     *
     * @param stack Item stack to modify
     * @param name Custom name to set. Removes tag if null
     */
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

    /**
     * Sets custom model to the stack
     *
     * @param stack Item stack to modify
     * @param model Custom model to set. Removes tag if null or less than 1
     */
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
     * Checks if stack has armor trim
     *
     * @param stack Item stack to check
     * @param registryManager Dynamic registry manager to check in. Doesn't check for validity if null
     * @return Does item stack have armor trim
     */
    public static boolean hasTrim(@NotNull ItemStack stack, @Nullable DynamicRegistryManager registryManager) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(TRIM_KEY, NbtElement.COMPOUND_TYPE)) return false;
        if (registryManager == null) return true;
        NbtCompound trim = nbt.getCompound(TRIM_KEY);
        return isValidTrim(trim, registryManager);
    }

    /**
     * Gets armor trim from stack
     *
     * @param stack Item stack to get from
     * @param registryManager Dynamic registry manager to get from
     * @return Armor trim from item stack, or null if none is present
     */
    public static @Nullable ArmorTrim getTrim(@NotNull ItemStack stack, @NotNull DynamicRegistryManager registryManager) {
        if (!hasTrim(stack, registryManager)) return null;
        NbtCompound nbtTrim = stack.getNbt().getCompound(TRIM_KEY);
        Identifier patternId = Identifier.tryParse(nbtTrim.getString(TRIM_PATTERN_KEY));
        Identifier materialId = Identifier.tryParse(nbtTrim.getString(TRIM_MATERIAL_KEY));
        Registry<ArmorTrimPattern> patternRegistry = registryManager.get(RegistryKeys.TRIM_PATTERN);
        Registry<ArmorTrimMaterial> materialRegistry = registryManager.get(RegistryKeys.TRIM_MATERIAL);
        ArmorTrimPattern pattern = patternRegistry.get(patternId);
        ArmorTrimMaterial material = materialRegistry.get(materialId);
        return new ArmorTrim(materialRegistry.getEntry(material), patternRegistry.getEntry(pattern));
    }

    /**
     * Sets armor trim to the stack
     *
     * @param stack Item stack to modify
     * @param trim Armor trim to set. Removes tag if null
     */
    public static void setTrim(@NotNull ItemStack stack, @Nullable ArmorTrim trim) {
        if (trim == null) {
            if (!hasTrim(stack, null)) return;

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

    /**
     * Sets armor trim to the stack
     *
     * @param stack Item stack to modify
     * @param pattern Trim pattern to set
     * @param material Trim material to set
     */
    public static void setTrim(@NotNull ItemStack stack, @NotNull ArmorTrimPattern pattern, @NotNull ArmorTrimMaterial material) {
        NbtCompound nbtTrim = new NbtCompound();
        nbtTrim.putString(TRIM_PATTERN_KEY, pattern.assetId().toString());
        nbtTrim.putString(TRIM_MATERIAL_KEY, material.assetName());

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.put(TRIM_KEY, nbtTrim);
        stack.setNbt(nbt);
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
     * Gets placing whitelist from stack
     *
     * @param stack Item stack to get from
     * @return Placing whitelist from item stack
     */
    public static @NotNull List<Block> getWhitelistPlace(@NotNull ItemStack stack) {
        if (!hasWhitelistPlace(stack)) return List.of();
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

    /**
     * Sets placing whitelist to the stack
     *
     * @param stack Item stack to modify
     * @param place Placing whitelist to set. Removes tag if null or empty
     */
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
        return hasWhitelistDestroy(stack, true);
    }

    /**
     * Gets destroying whitelist from stack
     *
     * @param stack Item stack to get from
     * @return Destroying whitelist from item stack
     */
    public static @NotNull List<Block> getWhitelistDestroy(@NotNull ItemStack stack) {
        if (!hasWhitelistDestroy(stack)) return List.of();
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

    /**
     * Sets destroying whitelist to the stack
     *
     * @param stack Item stack to modify
     * @param destroy Destroying whitelist to set. Removes tag if null or empty
     */
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

    /**
     * Gets stack unbreakability
     *
     * @param stack Item stack to get from
     * @return Is stack unbreakable
     */
    public static boolean getUnbreakable(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        return stack.getNbt().getBoolean(UNBREAKABLE_KEY);
    }

    /**
     * Sets unbreakability to the stack
     *
     * @param stack Item stack to modify
     * @param unbreakable Unbreakability to set. Removes tag if null
     */
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

    /**
     * Gets display flags from stack
     *
     * @param stack Item stack to get from
     * @return Display flags from item stack
     */
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

    /**
     * Sets display flags to the stack
     *
     * @param stack Item stack to modify
     * @param flags Display flags to set. Removes tag if null or empty
     */
    public static void setFlags(@NotNull ItemStack stack, @Nullable List<Boolean> flags) {
        if (flags == null || flags.isEmpty()) {
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

    /**
     * Checks if compound have all valid tags to be parsed to potion effect
     *
     * @param nbt NBT compound to check
     * @return Is compound valid for potion effect
     */
    public static boolean isValidPotionEffect(@NotNull NbtCompound nbt) {
        if (!nbt.contains(CUSTOM_POTION_EFFECTS_ID_KEY, NbtElement.STRING_TYPE)) return false;
        if (!nbt.contains(CUSTOM_POTION_EFFECTS_AMPLIFIER_KEY, NbtElement.INT_TYPE)) return false;
        if (!nbt.contains(CUSTOM_POTION_EFFECTS_DURATION_KEY, NbtElement.INT_TYPE)) return false;
        Identifier id = Identifier.tryParse(nbt.getString(CUSTOM_POTION_EFFECTS_ID_KEY));
        if (id == null) return false;
        return Registries.STATUS_EFFECT.containsId(id);
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
     * @return Does item stack have potion effects tag
     */
    public static boolean hasPotionEffects(@NotNull ItemStack stack) {
        return hasPotionEffects(stack, true);
    }

    /**
     * Gets potion effects from stack
     *
     * @param stack Item stack to get from
     * @return Potion effects from item stack
     */
    public static @NotNull HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> getPotionEffects(@NotNull ItemStack stack) {
        if (!hasPotionEffects(stack)) return new HashMap<>();
        HashMap<StatusEffect, Triple<Integer, Integer, Boolean>> result = new HashMap<>();
        NbtList customPotionEffects = stack.getNbt().getList(CUSTOM_POTION_EFFECTS_KEY, NbtElement.COMPOUND_TYPE);
        for (NbtElement customPotionEffect : customPotionEffects) {
            NbtCompound potionEffect = (NbtCompound)customPotionEffect;
            if (!isValidPotionEffect(potionEffect)) continue;
            Identifier id = Identifier.tryParse(potionEffect.getString(CUSTOM_POTION_EFFECTS_ID_KEY));
            StatusEffect effect = Registries.STATUS_EFFECT.get(id);
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

    /**
     * Sets potion effects to the stack
     *
     * @param stack Item stack to modify
     * @param effects Potion effects to set. Removes tag if null or empty
     */
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
                Identifier id = Registries.STATUS_EFFECT.getId(effect);
                potionEffect.putString(CUSTOM_POTION_EFFECTS_ID_KEY, id.toString());
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

    // cringe ↓. Possible solutions: Move in another file (e.g. EntityUtil), remove entity editing. ← preferable

    /**
     * Checks if stack has entity type
     *
     * @param stack Item stack to check
     * @param validate Check for validity of entity type. False to only check for tag
     * @return Does item stack have entity type
     */
    public static boolean hasEntityType(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
        if (!entityTag.contains(ENTITY_TAG_ID_KEY, NbtElement.STRING_TYPE)) return false;
        if (!validate) return true;
        Identifier id = Identifier.tryParse(entityTag.getString(ENTITY_TAG_ID_KEY));
        if (id == null) return false;
        return Registries.ENTITY_TYPE.containsId(id);
    }

    /**
     * Redirects to {@link #hasEntityType(ItemStack, boolean) hasEntityType} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid entity type
     */
    public static boolean hasEntityType(@NotNull ItemStack stack) {
        return hasEntityType(stack, true);
    }

    /**
     * Gets entity type from stack
     *
     * @param stack Item stack to get from
     * @return Entity type from item stack, or null if none
     */
    public static @Nullable EntityType<?> getEntityType(@NotNull ItemStack stack) {
        if (!hasEntityType(stack)) return null;
        return Registries.ENTITY_TYPE.get(Identifier.tryParse(stack.getNbt().getCompound(ENTITY_TAG_KEY).getString(ENTITY_TAG_ID_KEY)));
    }

    /**
     * Sets entity type to the stack
     *
     * @param stack Item stack to modify
     * @param type Entity type to set. Removes tag if null
     */
    public static void setEntityType(@NotNull ItemStack stack, @Nullable EntityType<?> type) {
        if (type == null) {
            if (!hasEntityType(stack, false)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_ID_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            String id = Registries.ENTITY_TYPE.getId(type).toString();

            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putString(ENTITY_TAG_ID_KEY, id);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Checks if stack has entity position
     *
     * @param stack Item stack to check
     * @param validate Check for validity of entity position. False to only check for tag
     * @return Does item stack have entity position
     */
    public static boolean hasEntityPosition(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
        if (!entityTag.contains(ENTITY_TAG_POS_KEY, NbtElement.LIST_TYPE)) return false;
        if (!validate) return true;
        NbtList pos = entityTag.getList(ENTITY_TAG_POS_KEY, NbtElement.DOUBLE_TYPE);
        return pos.size() == 3;
    }

    /**
     * Redirects to {@link #hasEntityPosition(ItemStack, boolean) hasEntityPosition} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid entity position
     */
    public static boolean hasEntityPosition(@NotNull ItemStack stack) {
        return hasEntityPosition(stack, true);
    }

    /**
     * Gets entity position from stack
     *
     * @param stack Item stack to get from
     * @return Entity position from item stack, or null if none
     */
    public static @Nullable Vec3d getEntityPosition(@NotNull ItemStack stack) {
        if (!hasEntityPosition(stack)) return null;
        NbtList pos = stack.getNbt().getCompound(ENTITY_TAG_KEY).getList(ENTITY_TAG_POS_KEY, NbtElement.DOUBLE_TYPE);
        return new Vec3d(pos.getDouble(0), pos.getDouble(1), pos.getDouble(2));
    }

    /**
     * Sets entity position to the stack
     *
     * @param stack Item stack to modify
     * @param position Entity position to set. Removes tag if null
     */
    public static void setEntityPosition(@NotNull ItemStack stack, @Nullable Vec3d position) {
        if (position == null) {
            if (!hasEntityPosition(stack, false)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_POS_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtList nbtPosition = new NbtList();
            nbtPosition.add(NbtDouble.of(position.x));
            nbtPosition.add(NbtDouble.of(position.y));
            nbtPosition.add(NbtDouble.of(position.z));

            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.put(ENTITY_TAG_POS_KEY, nbtPosition);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Checks if stack has entity motion
     *
     * @param stack Item stack to check
     * @param validate Check for validity of entity motion. False to only check for tag
     * @return Does item stack have entity motion
     */
    public static boolean hasEntityMotion(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
        if (!entityTag.contains(ENTITY_TAG_MOTION_KEY, NbtElement.LIST_TYPE)) return false;
        if (!validate) return true;
        NbtList pos = entityTag.getList(ENTITY_TAG_MOTION_KEY, NbtElement.DOUBLE_TYPE);
        return pos.size() == 3;
    }

    /**
     * Redirects to {@link #hasEntityMotion(ItemStack, boolean) hasEntityMotion} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid entity motion
     */
    public static boolean hasEntityMotion(@NotNull ItemStack stack) {
        return hasEntityMotion(stack, true);
    }

    /**
     * Gets entity motion from stack
     *
     * @param stack Item stack to get from
     * @return Entity motion from item stack, or null if none
     */
    public static @Nullable Vec3d getEntityMotion(@NotNull ItemStack stack) {
        if (!hasEntityMotion(stack)) return null;
        NbtList motion = stack.getNbt().getCompound(ENTITY_TAG_KEY).getList(ENTITY_TAG_MOTION_KEY, NbtElement.DOUBLE_TYPE);
        return new Vec3d(motion.getDouble(0), motion.getDouble(1), motion.getDouble(2));
    }

    /**
     * Sets entity motion to the stack
     *
     * @param stack Item stack to modify
     * @param motion Entity motion to set. Removes tag if null
     */
    public static void setEntityMotion(@NotNull ItemStack stack, @Nullable Vec3d motion) {
        if (motion == null) {
            if (!hasEntityMotion(stack, false)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_MOTION_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtList nbtMotion = new NbtList();
            nbtMotion.add(NbtDouble.of(motion.x));
            nbtMotion.add(NbtDouble.of(motion.y));
            nbtMotion.add(NbtDouble.of(motion.z));

            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.put(ENTITY_TAG_MOTION_KEY, nbtMotion);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Checks if stack has entity rotation
     *
     * @param stack Item stack to check
     * @param validate Check for validity of entity rotation. False to only check for tag
     * @return Does item stack have entity rotation
     */
    public static boolean hasEntityRotation(@NotNull ItemStack stack, boolean validate) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
        if (!entityTag.contains(ENTITY_TAG_ROTATION_KEY, NbtElement.LIST_TYPE)) return false;
        if (!validate) return true;
        NbtList rotation = entityTag.getList(ENTITY_TAG_ROTATION_KEY, NbtElement.FLOAT_TYPE);
        return rotation.size() == 2;
    }

    /**
     * Redirects to {@link #hasEntityRotation(ItemStack, boolean) hasEntityRotation} with validate param equals to true
     *
     * @param stack Item stack to check
     * @return Does item stack have valid entity rotation
     */
    public static boolean hasEntityRotation(@NotNull ItemStack stack) {
        return hasEntityRotation(stack, true);
    }

    /**
     * Gets entity rotation from stack
     *
     * @param stack Item stack to get from
     * @return Entity rotation from item stack, or null if none
     */
    public static @Nullable Vec2f getEntityRotation(@NotNull ItemStack stack) {
        if (!hasEntityRotation(stack)) return null;
        NbtList rotation = stack.getNbt().getCompound(ENTITY_TAG_KEY).getList(ENTITY_TAG_ROTATION_KEY, NbtElement.FLOAT_TYPE);
        return new Vec2f(rotation.getFloat(0), rotation.getFloat(1));
    }

    /**
     * Sets entity rotation to the stack
     *
     * @param stack Item stack to modify
     * @param rotation Entity rotation to set. Removes tag if null
     */
    public static void setEntityRotation(@NotNull ItemStack stack, @Nullable Vec2f rotation) {
        if (rotation == null) {
            if (!hasEntityRotation(stack, false)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_ROTATION_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtList nbtRotation = new NbtList();
            nbtRotation.add(NbtFloat.of(rotation.x));
            nbtRotation.add(NbtFloat.of(rotation.y));

            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.put(ENTITY_TAG_ROTATION_KEY, nbtRotation);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Gets entity gravity from stack
     *
     * @param stack Item stack to get from
     * @return Entity gravity from item stack
     */
    public static boolean getEntityGravity(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return true;
        return !stack.getNbt().getCompound(ENTITY_TAG_KEY).getBoolean(ENTITY_TAG_NO_GRAVITY_KEY);
    }

    /**
     * Sets entity gravity to the stack
     *
     * @param stack Item stack to modify
     * @param gravity Entity gravity to set. Removes tag if null
     */
    public static void setEntityGravity(@NotNull ItemStack stack, @Nullable Boolean gravity) {
        if (gravity == null) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_NO_GRAVITY_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putBoolean(ENTITY_TAG_NO_GRAVITY_KEY, !gravity);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Gets entity silence from stack
     *
     * @param stack Item stack to get from
     * @return Entity silence from item stack
     */
    public static boolean getEntitySilence(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        return stack.getNbt().getCompound(ENTITY_TAG_KEY).getBoolean(ENTITY_TAG_SILENT_KEY);
    }

    /**
     * Sets entity silence to the stack
     *
     * @param stack Item stack to modify
     * @param silence Entity silence to set. Removes tag if null
     */
    public static void setEntitySilence(@NotNull ItemStack stack, @Nullable Boolean silence) {
        if (silence == null) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_SILENT_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putBoolean(ENTITY_TAG_SILENT_KEY, silence);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Gets entity invulnerability from stack
     *
     * @param stack Item stack to get from
     * @return Entity invulnerability from item stack
     */
    public static boolean getEntityInvulnerability(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        return stack.getNbt().getCompound(ENTITY_TAG_KEY).getBoolean(ENTITY_TAG_INVULNERABLE_KEY);
    }

    /**
     * Sets entity invulnerability to the stack
     *
     * @param stack Item stack to modify
     * @param invulnerability Entity invulnerability to set. Removes tag if null
     */
    public static void setEntityInvulnerability(@NotNull ItemStack stack, @Nullable Boolean invulnerability) {
        if (invulnerability == null) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_INVULNERABLE_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putBoolean(ENTITY_TAG_INVULNERABLE_KEY, invulnerability);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Gets entity picking up from stack
     *
     * @param stack Item stack to get from
     * @return Entity picking up from item stack
     */
    public static boolean getEntityPickingUp(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        return stack.getNbt().getCompound(ENTITY_TAG_KEY).getBoolean(ENTITY_TAG_CAN_PICK_UP_LOOT_KEY);
    }

    /**
     * Sets entity picking up to the stack
     *
     * @param stack Item stack to modify
     * @param pickingUp Entity picking up to set. Removes tag if null
     */
    public static void setEntityPickingUp(@NotNull ItemStack stack, @Nullable Boolean pickingUp) {
        if (pickingUp == null) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_CAN_PICK_UP_LOOT_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putBoolean(ENTITY_TAG_CAN_PICK_UP_LOOT_KEY, pickingUp);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Gets entity persistance from stack
     *
     * @param stack Item stack to get from
     * @return Entity persistance from item stack
     */
    public static boolean getEntityPersistance(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        return stack.getNbt().getCompound(ENTITY_TAG_KEY).getBoolean(ENTITY_TAG_PERSISTANCE_REQUIRED_KEY);
    }

    /**
     * Sets entity picking up to the stack
     *
     * @param stack Item stack to modify
     * @param persistance Entity persistance to set. Removes tag if null
     */
    public static void setEntityPersistance(@NotNull ItemStack stack, @Nullable Boolean persistance) {
        if (persistance == null) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_PERSISTANCE_REQUIRED_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putBoolean(ENTITY_TAG_PERSISTANCE_REQUIRED_KEY, persistance);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Gets entity intellect from stack
     *
     * @param stack Item stack to get from
     * @return Entity intellect from item stack
     */
    public static boolean getEntityIntellect(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return true;
        return !stack.getNbt().getCompound(ENTITY_TAG_KEY).getBoolean(ENTITY_TAG_NO_AI_KEY);
    }

    /**
     * Sets entity picking up to the stack
     *
     * @param stack Item stack to modify
     * @param intellect Entity intellect to set. Removes tag if null
     */
    public static void setEntityIntellect(@NotNull ItemStack stack, @Nullable Boolean intellect) {
        if (intellect == null) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_NO_AI_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putBoolean(ENTITY_TAG_NO_AI_KEY, !intellect);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Gets entity glow from stack
     *
     * @param stack Item stack to get from
     * @return Entity glow from item stack
     */
    public static boolean getEntityGlow(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        return stack.getNbt().getCompound(ENTITY_TAG_KEY).getBoolean(ENTITY_TAG_GLOWING_KEY);
    }

    /**
     * Sets entity glow to the stack
     *
     * @param stack Item stack to modify
     * @param glow Entity glow to set. Removes tag if null
     */
    public static void setEntityGlow(@NotNull ItemStack stack, @Nullable Boolean glow) {
        if (glow == null) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_GLOWING_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putBoolean(ENTITY_TAG_GLOWING_KEY, glow);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Checks if stack has entity health
     *
     * @param stack Item stack to check
     * @return Does item stack have entity health
     */
    public static boolean hasEntityHealth(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
        return entityTag.contains(ENTITY_TAG_HEALTH_KEY, NbtElement.FLOAT_TYPE);
    }

    /**
     * Gets entity health from stack
     *
     * @param stack Item stack to get from
     * @return Entity health from item stack, or null if none
     */
    public static @Nullable Float getEntityHealth(@NotNull ItemStack stack) {
        if (!hasEntityHealth(stack)) return null;
        return stack.getNbt().getCompound(ENTITY_TAG_KEY).getFloat(ENTITY_TAG_HEALTH_KEY);
    }

    /**
     * Sets entity health to the stack
     *
     * @param stack Item stack to modify
     * @param health Entity health to set. Removes tag if null
     */
    public static void setEntityHealth(@NotNull ItemStack stack, @Nullable Float health) {
        if (health == null) {
            if (!hasEntityHealth(stack)) return;
            NbtCompound nbt = stack.getNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_HEALTH_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putFloat(ENTITY_TAG_HEALTH_KEY, health);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Checks if stack has entity absorption
     *
     * @param stack Item stack to check
     * @return Does item stack have entity absorption
     */
    public static boolean hasEntityAbsorption(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
        return entityTag.contains(ENTITY_TAG_ABSORPTION_AMOUNT_KEY, NbtElement.FLOAT_TYPE);
    }

    /**
     * Gets entity abosrption from stack
     *
     * @param stack Item stack to get from
     * @return Entity absorption from item stack, or null if none
     */
    public static @Nullable Float getEntityAbsorption(@NotNull ItemStack stack) {
        if (!hasEntityAbsorption(stack)) return null;
        return stack.getNbt().getCompound(ENTITY_TAG_KEY).getFloat(ENTITY_TAG_ABSORPTION_AMOUNT_KEY);
    }

    /**
     * Sets entity absorption to the stack
     *
     * @param stack Item stack to modify
     * @param absorption Entity absorption to set. Removes tag if null
     */
    public static void setEntityAbsorption(@NotNull ItemStack stack, @Nullable Float absorption) {
        if (absorption == null) {
            if (!hasEntityAbsorption(stack)) return;
            NbtCompound nbt = stack.getNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_ABSORPTION_AMOUNT_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putFloat(ENTITY_TAG_ABSORPTION_AMOUNT_KEY, absorption);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Gets entity invisibility from stack
     *
     * @param stack Item stack to get from
     * @return Entity invisibility from item stack
     */
    public static boolean getEntityInvisibility(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        return stack.getNbt().getCompound(ENTITY_TAG_KEY).getBoolean(ENTITY_TAG_INVISIBLE_KEY);
    }

    /**
     * Sets entity invisibility to the stack
     *
     * @param stack Item stack to modify
     * @param invisibility Entity invisibility to set. Removes tag if null
     */
    public static void setEntityInvisibility(@NotNull ItemStack stack, @Nullable Boolean invisibility) {
        if (invisibility == null) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_INVISIBLE_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putBoolean(ENTITY_TAG_INVISIBLE_KEY, invisibility);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }

    /**
     * Checks if stack has entity air
     *
     * @param stack Item stack to check
     * @return Does item stack have entity air
     */
    public static boolean hasEntityAir(@NotNull ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
        return entityTag.contains(ENTITY_TAG_AIR_KEY, NbtElement.INT_TYPE);
    }

    /**
     * Gets entity air from stack
     *
     * @param stack Item stack to get from
     * @return Entity air from item stack, or null if none
     */
    public static @Nullable Integer getEntityAir(@NotNull ItemStack stack) {
        if (!hasEntityHealth(stack)) return null;
        return stack.getNbt().getCompound(ENTITY_TAG_KEY).getInt(ENTITY_TAG_AIR_KEY);
    }

    /**
     * Sets entity air to the stack
     *
     * @param stack Item stack to modify
     * @param air Entity air to set. Removes tag if null
     */
    public static void setEntityAir(@NotNull ItemStack stack, @Nullable Integer air) {
        if (air == null) {
            if (!hasEntityAir(stack)) return;
            NbtCompound nbt = stack.getNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.remove(ENTITY_TAG_AIR_KEY);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound entityTag = nbt.getCompound(ENTITY_TAG_KEY);
            entityTag.putInt(ENTITY_TAG_AIR_KEY, air);
            nbt.put(ENTITY_TAG_KEY, entityTag);
            stack.setNbt(nbt);
        }
    }
}
