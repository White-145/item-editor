package me.white.itemeditor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.primitives.Ints;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
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
    private static final String BLOCK_ENTITY_TAG_PATTERNS_KEY = "Patterns";
    private static final String BLOCK_ENTITY_TAG_PATTERNS_COLOR_KEY = "Color";
    private static final String BLOCK_ENTITY_TAG_PATTERNS_PATTERN_KEY = "Pattern";
    private static final String CAN_DESTROY_KEY = "CanDestroy";
    private static final String CAN_PLACE_ON_KEY = "CanPlaceOn";
    private static final String CUSTOM_MODEL_DATA_KEY = "CustomModelData";
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
    // private static final String SKULL_OWNER_PROPERTIES_KEY = "Properties";
    // private static final String SKULL_OWNER_PROPERTIES_TEXTURES_KEY = "textures";
    private static final String TITLE_KEY = "title";
    private static final String TRIM_KEY = "Trim";
    private static final String TRIM_MATERIAL_KEY = "material";
    private static final String TRIM_PATTERN_KEY = "pattern";
    private static final String UNBREAKABLE_KEY = "Unbreakable";
    private static final String HIDE_FLAGS_KEY = "HideFlags";
    public static final int FLAGS_AMOUNT = 8;
    public static final int GENERATIONS_AMOUNT = 4;

    private static String getColorKey(Item item) {
        if (item instanceof FilledMapItem) return DISPLAY_MAP_COLOR_KEY;
        return DISPLAY_COLOR_KEY;
    }

    public static boolean isValidEnchantment(NbtCompound nbt) {
        if (nbt.isEmpty()) return false;
        if (!nbt.contains(ENCHANTMENTS_ID_KEY, NbtElement.STRING_TYPE)) return false;
        if (!nbt.contains(ENCHANTMENTS_LVL_KEY, NbtElement.INT_TYPE)) return false;
        Identifier id = Identifier.tryParse(nbt.getString(ENCHANTMENTS_ID_KEY));
        if (id == null) return false;
        return Registries.ENCHANTMENT.containsId(id);
    }

    public static boolean isValidAttribute(NbtCompound nbt) {
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

    public static boolean isValidPattern(NbtCompound nbt) {
        if (!nbt.contains(BLOCK_ENTITY_TAG_PATTERNS_COLOR_KEY, NbtElement.INT_TYPE)) return false;
        int color = nbt.getInt(BLOCK_ENTITY_TAG_PATTERNS_COLOR_KEY);
        if (color < 0 || color >= 16) return false;
        if (!nbt.contains(BLOCK_ENTITY_TAG_PATTERNS_PATTERN_KEY, NbtElement.STRING_TYPE)) return false;
        String pattern = nbt.getString(BLOCK_ENTITY_TAG_PATTERNS_PATTERN_KEY);
        return BannerPattern.byId(pattern) != null;
    }

    // TODO: also check is value valid
    
    public static boolean hasAttributes(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
        for (NbtElement attribute : attributes) {
            if (isValidAttribute((NbtCompound)attribute)) return true;
        }
        return false;
    }
    
    public static boolean hasAttributesPlaceholder(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
        return !attributes.isEmpty();
    }

    public static boolean hasBannerPatterns(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(BLOCK_ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG_KEY);
        if (!blockEntityTag.contains(BLOCK_ENTITY_TAG_PATTERNS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList patterns = blockEntityTag.getList(BLOCK_ENTITY_TAG_PATTERNS_KEY, NbtElement.COMPOUND_TYPE);
        return !patterns.isEmpty();
    }

    public static boolean hasBookAuthor(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt.contains(AUTHOR_KEY, NbtElement.STRING_TYPE);
    }

    public static boolean hasBookTitle(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt.contains(TITLE_KEY, NbtElement.STRING_TYPE);
    }

    public static boolean hasBookPages(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(PAGES_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList pages = nbt.getList(PAGES_KEY, NbtElement.STRING_TYPE);
        return !pages.isEmpty();
    }

    public static boolean hasColor(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound display = nbt.getCompound(DISPLAY_KEY);
        return display.contains(getColorKey(stack.getItem()), NbtElement.INT_TYPE);
    }

    public static boolean hasEnchantments(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList enchantments = nbt.getList(ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        if (enchantments.isEmpty()) return false;
        for (NbtElement enchantment : enchantments) {
            if (isValidEnchantment((NbtCompound)enchantment)) return true;
        }
        return false;
    }

    public static boolean hasEnchantmentGlint(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(ENCHANTMENTS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList enchantments = nbt.getList(ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        return !enchantments.isEmpty();
    }

    public static boolean hasFireworkExplosions(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
        if (!fireworks.contains(FIREWORKS_EXPLOSIONS_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList explosions = fireworks.getList(FIREWORKS_EXPLOSIONS_KEY, NbtElement.COMPOUND_TYPE);
        return !explosions.isEmpty();
    }

    public static boolean hasHeadOwner(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(SKULL_OWNER_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound skullOwner = nbt.getCompound(SKULL_OWNER_KEY);
        return skullOwner.contains(SKULL_OWNER_NAME_KEY, NbtElement.STRING_TYPE);
    }

    // public static boolean hasHeadTexture(ItemStack stack) {
    //     if (!stack.hasNbt()) return false;
    //     NbtCompound nbt = stack.getNbt();
    //     if (!nbt.contains(SKULL_OWNER_KEY, NbtElement.COMPOUND_TYPE)) return false;
    //     NbtCompound skullOwner = nbt.getCompound(SKULL_OWNER_KEY);
    //     if (!skullOwner.contains(SKULL_OWNER_PROPERTIES_KEY, NbtElement.COMPOUND_TYPE)) return false;
    //     NbtCompound properties = skullOwner.getCompound(SKULL_OWNER_PROPERTIES_KEY);
    //     if (!properties.contains(SKULL_OWNER_PROPERTIES_TEXTURES_KEY, NbtElement.LIST_TYPE)) return false;
    //     NbtList textures = properties.getList(SKULL_OWNER_PROPERTIES_TEXTURES_KEY, NbtElement.STRING_TYPE);
    //     return !textures.isEmpty();
    // }

    public static boolean hasLore(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound display = nbt.getCompound(DISPLAY_KEY);
        if (!display.contains(DISPLAY_LORE_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList lore = display.getList(DISPLAY_LORE_KEY, NbtElement.STRING_TYPE);
        return !lore.isEmpty();
    }

    public static boolean hasModel(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(CUSTOM_MODEL_DATA_KEY, NbtElement.INT_TYPE)) return false;
        int model = nbt.getInt(CUSTOM_MODEL_DATA_KEY);
        return model != 0;
    }

    public static boolean hasName(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(DISPLAY_KEY, NbtElement.COMPOUND_TYPE)) return false;
        NbtCompound display = nbt.getCompound(DISPLAY_KEY);
        return display.contains(DISPLAY_NAME_KEY, NbtElement.STRING_TYPE);
    }

    public static boolean hasTrim(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        return nbt.contains(TRIM_KEY, NbtElement.COMPOUND_TYPE);
    }

    public static boolean hasWhitelistPlace(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(CAN_PLACE_ON_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList place = nbt.getList(CAN_PLACE_ON_KEY, NbtElement.STRING_TYPE);
        return !place.isEmpty();
    }

    public static boolean hasWhitelistDestroy(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(CAN_DESTROY_KEY, NbtElement.LIST_TYPE)) return false;
        NbtList destroy = nbt.getList(CAN_DESTROY_KEY, NbtElement.STRING_TYPE);
        return !destroy.isEmpty();
    }

    public static List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> getAttributes(ItemStack stack) {
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

    public static List<Pair<BannerPattern, Integer>> getBannerPatterns(ItemStack stack) {
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

    public static String getBookAuthor(ItemStack stack) {
        if (!hasBookAuthor(stack)) return null;
        return stack.getNbt().getString(AUTHOR_KEY);
    }

    public static String getBookTitle(ItemStack stack) {
        if (!hasBookTitle(stack)) return null;
        return stack.getNbt().getString(TITLE_KEY);
    }

    public static int getBookGeneration(ItemStack stack) {
        if (!stack.hasNbt()) return 0;
        int generation = stack.getNbt().getInt(GENERATION_KEY);
        return generation >= 0 && generation < 3 ? generation : 0;
    }

    public static List<Text> getBookPages(ItemStack stack) {
        if (!hasBookPages(stack)) return List.of();
        List<Text> pages = new ArrayList<>();
        for (NbtElement page : stack.getNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE)) {
            pages.add(Text.Serializer.fromJson(page.asString()));
        }
        return pages;
    }

    public static int getColor(ItemStack stack) {
        if (!hasColor(stack)) return 0;
        return stack.getNbt().getCompound(DISPLAY_KEY).getInt(getColorKey(stack.getItem()));
    }

    public static List<Pair<Enchantment, Integer>> getEnchantments(ItemStack stack) {
        if (!hasEnchantments(stack)) return List.of();
        NbtList nbtEnchantments = stack.getNbt().getList(ENCHANTMENTS_KEY, NbtElement.COMPOUND_TYPE);
        List<Pair<Enchantment, Integer>> enchantments = new ArrayList<>();
        for (NbtElement nbtEnchantment : nbtEnchantments) {
            NbtCompound enchantment = (NbtCompound)nbtEnchantment;
            String id = enchantment.getString(ENCHANTMENTS_ID_KEY);
            int lvl = enchantment.getInt(ENCHANTMENTS_LVL_KEY);
            enchantments.add(Pair.of(Registries.ENCHANTMENT.get(new Identifier(id)), lvl));
        }
        return enchantments;
    }

    public static List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> getFireworkExplosions(ItemStack stack) {
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
            result.add(new Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>(type, Ints.asList(colors), flicker, trail, Ints.asList(fadeColors)));
        }
        return result;
    }

    public static int getFireworkFlight(ItemStack stack) {
        if (!stack.hasNbt()) return 0;
        NbtCompound nbt = stack.getNbt();
        if (!nbt.contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) return 0;
        NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
        return fireworks.getInt(FIREWORKS_FLIGHT_KEY);
    }

    public static String getHeadOwner(ItemStack stack) {
        if (!hasHeadOwner(stack)) return null;
        return stack.getNbt().getCompound(SKULL_OWNER_KEY).getString(SKULL_OWNER_NAME_KEY);
    }

    // public static String getHeadTexture(ItemStack stack) {
    //     if (!hasHeadTexture(stack)) return null;
    //     return stack.getNbt().getCompound(SKULL_OWNER_KEY).getCompound(SKULL_OWNER_PROPERTIES_KEY).getList(SKULL_OWNER_PROPERTIES_TEXTURES_KEY, NbtElement.STRING_TYPE).getString(0);
    // }

    public static List<Text> getLore(ItemStack stack) {
        if (!hasLore(stack)) return List.of();
        NbtList nbtLore = stack.getNbt().getCompound(DISPLAY_KEY).getList(DISPLAY_LORE_KEY, NbtElement.STRING_TYPE);
        List<Text> lore = new ArrayList<>();
        for (NbtElement nbtLine : nbtLore) {
            Text line = Text.Serializer.fromJson(nbtLine.asString());
            lore.add(line);
        }
        return lore;
    }

    public static int getModel(ItemStack stack) {
        if (!hasModel(stack)) return 0;
        return stack.getNbt().getInt(CUSTOM_MODEL_DATA_KEY);
    }

    public static Text getName(ItemStack stack) {
        if (!hasName(stack)) return null;
        return Text.Serializer.fromJson(stack.getNbt().getCompound(DISPLAY_KEY).getString(DISPLAY_NAME_KEY));
    }

    public static ArmorTrim getTrim(ItemStack stack, DynamicRegistryManager registryManager) {
        if (!hasTrim(stack)) return null;
        NbtCompound nbtTrim = stack.getNbt().getCompound(TRIM_KEY);
        Identifier patternId = Identifier.tryParse(nbtTrim.getString(TRIM_PATTERN_KEY));
        Identifier materialId = Identifier.tryParse(nbtTrim.getString(TRIM_MATERIAL_KEY));
        if (patternId == null || materialId == null) return null;
        Registry<ArmorTrimPattern> patternRegistry = registryManager.get(RegistryKeys.TRIM_PATTERN);
        Registry<ArmorTrimMaterial> materialRegistry = registryManager.get(RegistryKeys.TRIM_MATERIAL);
        ArmorTrimPattern pattern = patternRegistry.get(patternId);
        ArmorTrimMaterial material = materialRegistry.get(materialId);
        if (pattern == null || material == null) return null;
        return new ArmorTrim(materialRegistry.getEntry(material), patternRegistry.getEntry(pattern));
    }

    public static List<Block> getWhitelistPlace(ItemStack stack) {
        if (!hasWhitelistPlace(stack)) return List.of();
        NbtList nbtPlace = stack.getNbt().getList(CAN_PLACE_ON_KEY, NbtElement.STRING_TYPE);
        List<Block> result = new ArrayList<>();
        for (NbtElement nbtBlock : nbtPlace) {
            Identifier id = Identifier.tryParse(((NbtString)nbtBlock).asString());
            if (id == null) continue;
            Block block = Registries.BLOCK.get(id);
            if (block == null) continue;
            result.add(block);
        }
        return result;
    }

    public static List<Block> getWhitelistDestroy(ItemStack stack) {
        if (!hasWhitelistDestroy(stack)) return List.of();
        NbtList nbtDestroy = stack.getNbt().getList(CAN_DESTROY_KEY, NbtElement.STRING_TYPE);
        List<Block> result = new ArrayList<>();
        for (NbtElement nbtBlock : nbtDestroy) {
            Identifier id = Identifier.tryParse(((NbtString)nbtBlock).asString());
            if (id == null) continue;
            Block block = Registries.BLOCK.get(id);
            if (block == null) continue;
            result.add(block);
        }
        return result;
    }

    public static boolean getUnbreakable(ItemStack stack) {
        if (!stack.hasNbt()) return false;
        return stack.getNbt().getBoolean(UNBREAKABLE_KEY);
    }

    public static List<Boolean> getFlags(ItemStack stack) {
        List<Boolean> result = new ArrayList<>();
        if (!stack.hasNbt()) return result;
        int flags = stack.getNbt().getInt(HIDE_FLAGS_KEY);
        for (int i = 0; i < FLAGS_AMOUNT; ++i) {
            int mask = 1 << i;
            result.add((flags & mask) == mask);
        }
        return result;
    }
    
    public static void setAttributes(ItemStack stack, List<Triple<EntityAttribute, EntityAttributeModifier, EquipmentSlot>> attributes) {
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
    
    public static void setAttributePlaceholder(ItemStack stack, boolean placeholder) {
        if (hasAttributesPlaceholder(stack) == placeholder) return;
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

    public static void setBannerPatterns(ItemStack stack, List<Pair<BannerPattern, Integer>> patterns) {
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

    public static void setBookAuthor(ItemStack stack, String author) {
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

    public static void setBookTitle(ItemStack stack, String title) {
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

    public static void setBookGeneration(ItemStack stack, Integer generation) {
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

    public static void setBookPages(ItemStack stack, List<Text> pages) {
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

    public static void setColor(ItemStack stack, Integer color) {
        // TODO: support for more items such as firework stars and potions
        if (color == null || color > 0xFFFFFF || color < -0xFFFFFF) {
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

    public static void setEnchantments(ItemStack stack, List<Pair<Enchantment, Integer>> enchantments) {
        if (enchantments == null || enchantments.isEmpty()) {
            if (!hasEnchantments(stack)) return;

            NbtCompound nbt = stack.getNbt();
            nbt.remove(ENCHANTMENTS_KEY);
            stack.setNbt(nbt);
        } else {
            NbtList nbtEnchantments = new NbtList();
            for (Pair<Enchantment, Integer> enchantment : enchantments) {
                NbtCompound nbtEnchantment = new NbtCompound();
                nbtEnchantment.putString(ENCHANTMENTS_ID_KEY, Registries.ENCHANTMENT.getId(enchantment.getLeft()).toString());
                nbtEnchantment.putInt(ENCHANTMENTS_LVL_KEY, enchantment.getRight());
                nbtEnchantments.add(nbtEnchantment);
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.put(ENCHANTMENTS_KEY, nbtEnchantments);
            stack.setNbt(nbt);
        }
    }

    public static void setEnchantmentGlint(ItemStack stack, boolean glint) {
        if (hasEnchantmentGlint(stack) == glint) return;
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

    public static void setFireworkExplosions(ItemStack stack, List<Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>>> explosions) {
        if (explosions == null || explosions.isEmpty()) {
            if (!hasFireworkExplosions(stack)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
            fireworks.remove(FIREWORKS_EXPLOSIONS_KEY);
            nbt.put(FIREWORKS_KEY, fireworks);
            stack.setNbt(nbt);
        } else {
            NbtList nbtExplosions = new NbtList();
            for (Quintet<Integer, List<Integer>, Boolean, Boolean, List<Integer>> enchantment : explosions) {
                NbtCompound nbtExplosion = new NbtCompound();
                nbtExplosion.putInt(FIREWORKS_EXPLOSIONS_TYPE_KEY, enchantment.getA());
                nbtExplosion.putIntArray(FIREWORKS_EXPLOSIONS_COLORS_KEY, enchantment.getB());
                nbtExplosion.putBoolean(FIREWORKS_EXPLOSIONS_FLICKER_KEY, enchantment.getC());
                nbtExplosion.putBoolean(FIREWORKS_EXPLOSIONS_TRAIL_KEY, enchantment.getD());
                nbtExplosion.putIntArray(FIREWORKS_EXPLOSIONS_FADE_COLORS_KEY, enchantment.getE());
                nbtExplosions.add(nbtExplosion);
            }

            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
            fireworks.put(FIREWORKS_EXPLOSIONS_KEY, nbtExplosions);
            nbt.put(FIREWORKS_KEY, fireworks);
            stack.setNbt(nbt);
        }
    }

    public static void setFireworkFlight(ItemStack stack, Integer flight) {
        if (flight == null || flight < -128 || flight > 127) {
            if (!stack.hasNbt()) return;
            NbtCompound nbt = stack.getNbt();
            if (!nbt.contains(FIREWORKS_KEY, NbtElement.COMPOUND_TYPE)) return;
            NbtCompound fireworks = nbt.getCompound(FIREWORKS_KEY);
            if (!fireworks.contains(FIREWORKS_FLIGHT_KEY, NbtElement.INT_TYPE)) return;
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

    public static void setHeadOwner(ItemStack stack, String owner) {
        if (owner == null) {
            if (!hasHeadOwner(stack)) return;

            NbtCompound nbt = stack.getNbt();
            NbtCompound skullOwner = nbt.getCompound(SKULL_OWNER_KEY);
            skullOwner.remove(SKULL_OWNER_NAME_KEY);
            nbt.put(SKULL_OWNER_KEY, skullOwner);
            stack.setNbt(nbt);
        } else {
            NbtCompound nbt = stack.getOrCreateNbt();
            NbtCompound skullOwner = nbt.getCompound(SKULL_OWNER_KEY);
            skullOwner.putString(SKULL_OWNER_NAME_KEY, owner);
            nbt.put(SKULL_OWNER_KEY, skullOwner);
            stack.setNbt(nbt);
        }
    }

    // TODO
    // public static void setHeadTexture(ItemStack stack, String texture) { }

    public static void setLore(ItemStack stack, List<Text> lore) {
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

    public static void setModel(ItemStack stack, Integer model) {
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

    public static void setName(ItemStack stack, Text name) {
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

    public static void setTrim(ItemStack stack, ArmorTrim trim, DynamicRegistryManager registryManager) {
        if (trim == null) {
            if (!hasTrim(stack)) return;

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

    public static void setTrim(ItemStack stack, ArmorTrimPattern pattern, ArmorTrimMaterial material) {
        NbtCompound nbtTrim = new NbtCompound();
        nbtTrim.putString(TRIM_PATTERN_KEY, pattern.assetId().toString());
        nbtTrim.putString(TRIM_MATERIAL_KEY, material.assetName());

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.put(TRIM_KEY, nbtTrim);
        stack.setNbt(nbt);
    }

    public static void setWhitelistPlace(ItemStack stack, List<Block> place) {
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

    public static void setWhitelistDestroy(ItemStack stack, List<Block> destroy) {
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

    public static void setUnbreakable(ItemStack stack, Boolean unbreakable) {
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

    public static void setFlags(ItemStack stack, List<Boolean> flags) {
        if (flags == null || flags.size() >= FLAGS_AMOUNT) {
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
}
