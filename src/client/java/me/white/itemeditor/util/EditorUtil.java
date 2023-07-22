// package me.white.itemeditor.util;

// import net.minecraft.item.ItemStack;
// import net.minecraft.nbt.NbtCompound;
// import net.minecraft.nbt.NbtElement;
// import net.minecraft.nbt.NbtList;

// public class EditorUtil {
//     private static final String ATTRIBUTE_MODIFIERS_KEY = "AttributeModifiers";
//     private static final String BLOCK_ENTITY_TAG_KEY = "BlockEntityTag";

//     public static boolean hasAttributes(ItemStack stack) {
//         if (!stack.hasNbt()) return false;
//         NbtCompound nbt = stack.getNbt();
//         if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY, NbtElement.LIST_TYPE)) return false;
//         NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
//         return !attributes.isEmpty();
//     }

//     public static boolean hasBannerPatterns(ItemStack stack) {
//         if (!stack.hasNbt()) return false;
//         NbtCompound nbt = stack.getNbt();
//         if (!nbt.contains(ATTRIBUTE_MODIFIERS_KEY, NbtElement.LIST_TYPE)) return false;
//         NbtList attributes = nbt.getList(ATTRIBUTE_MODIFIERS_KEY, NbtElement.COMPOUND_TYPE);
//         return !attributes.isEmpty();
//     }

//     public static boolean hasBookAuthor(ItemStack stack) { }

//     public static boolean hasBookTitle(ItemStack stack) { }

//     public static boolean hasBookGeneration(ItemStack stack) { }

//     public static boolean hasBookPages(ItemStack stack) { }

//     public static boolean hasColor(ItemStack stack) { }

//     public static boolean hasEnchantments(ItemStack stack) { }

//     public static boolean hasFireworkExplosions(ItemStack stack) { }

//     public static boolean hasHeadOwner(ItemStack stack) { }

//     public static boolean hasHeadTexture(ItemStack stack) { }

//     public static boolean hasLore(ItemStack stack) { }

//     public static boolean hasModel(ItemStack stack) { }

//     public static boolean hasName(ItemStack stack) { }

//     public static boolean hasTrim(ItemStack stack) { }

//     public static boolean hasWhitelistPlace(ItemStack stack) { }

//     public static boolean hasWhitelistDestroy(ItemStack stack) { }
// }
