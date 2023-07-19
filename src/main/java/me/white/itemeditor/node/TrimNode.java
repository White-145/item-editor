package me.white.itemeditor.node;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.ItemManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TrimNode {
    public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.trim.error.cannotedit")).create();
    public static final CommandSyntaxException NO_TRIM_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.trim.error.notrim")).create();
    private static final String OUTPUT_GET = "commands.edit.trim.get";
    private static final String OUTPUT_RESET = "commands.edit.trim.reset";
    private static final String OUTPUT_SET = "commands.edit.trim.set";
    private static final String TRIM_KEY = "Trim";
    private static final String PATTERN_KEY = "pattern";
    private static final String MATERIAL_KEY = "material";

    private static void checkCanEdit(FabricClientCommandSource context) throws CommandSyntaxException {
        ItemStack item = ItemManager.getItemStack(context);
        if (!(item.getItem() instanceof ArmorItem)) throw CANNOT_EDIT_EXCEPTION;
    }

    private static void checkHasTrim(FabricClientCommandSource context) throws CommandSyntaxException {
        ItemStack item = ItemManager.getItemStack(context);
        if (!item.hasNbt()) throw NO_TRIM_EXCEPTION;
        NbtCompound trim = item.getSubNbt(TRIM_KEY);
        if (trim == null || !trim.contains(PATTERN_KEY, NbtElement.STRING_TYPE) || !trim.contains(MATERIAL_KEY, NbtElement.STRING_TYPE)) throw NO_TRIM_EXCEPTION;
        String pattern = trim.getString(PATTERN_KEY);
        String material = trim.getString(MATERIAL_KEY);
        MinecraftClient client = MinecraftClient.getInstance();
        DynamicRegistryManager registryManager = client.world.getRegistryManager();
        Identifier patternId = Identifier.tryParse(pattern);
        Identifier materialId = Identifier.tryParse(material);
        if (patternId == null || materialId == null) throw NO_TRIM_EXCEPTION;
        if (registryManager.get(RegistryKeys.TRIM_PATTERN).get(patternId) == null) throw NO_TRIM_EXCEPTION;
        if (registryManager.get(RegistryKeys.TRIM_MATERIAL).get(materialId) == null) throw NO_TRIM_EXCEPTION;
    }

    private static ArmorTrimPattern getArmorTrimPatternArgument(CommandContext<FabricClientCommandSource> context, String key) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.isOf(RegistryKeys.TRIM_PATTERN)) {
            throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), RegistryKeys.TRIM_PATTERN.getValue());
        }
		return (ArmorTrimPattern)reference.value();
	}

    private static ArmorTrimMaterial getArmorTrimMaterialArgument(CommandContext<FabricClientCommandSource> context, String key) throws CommandSyntaxException {
		RegistryEntry.Reference<?> reference = context.getArgument(key, RegistryEntry.Reference.class);
        RegistryKey<?> registryKey = reference.registryKey();
        if (!registryKey.isOf(RegistryKeys.TRIM_MATERIAL)) {
            throw RegistryEntryArgumentType.INVALID_TYPE_EXCEPTION.create(registryKey.getValue(), registryKey.getRegistry(), RegistryKeys.TRIM_MATERIAL.getValue());
        }
		return (ArmorTrimMaterial)reference.value();
	}

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("trim")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemManager.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());
                checkHasTrim(context.getSource());

                ItemStack item = ItemManager.getItemStack(context.getSource());

                NbtCompound trim = item.getSubNbt(TRIM_KEY);
                String pattern = trim.getString(PATTERN_KEY);
                String material = trim.getString(MATERIAL_KEY);
                MinecraftClient client = MinecraftClient.getInstance();
                DynamicRegistryManager registryManager = client.world.getRegistryManager();
                ArmorTrimPattern trimPattern = registryManager.get(RegistryKeys.TRIM_PATTERN).get(Identifier.tryParse(pattern));
                ArmorTrimMaterial trimMaterial = registryManager.get(RegistryKeys.TRIM_MATERIAL).get(Identifier.tryParse(material));

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, trimPattern.description(), trimMaterial.description()));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemManager.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());
                checkHasTrim(context.getSource());

                ItemStack item = ItemManager.getItemStack(context.getSource());
                item.removeSubNbt(TRIM_KEY);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_RESET));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Reference<ArmorTrimPattern>> setPatternNode = ClientCommandManager
            .argument("pattern", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.TRIM_PATTERN))
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Reference<ArmorTrimMaterial>> setPatternMaterialNode = ClientCommandManager
            .argument("material", RegistryEntryArgumentType.registryEntry(registryAccess, RegistryKeys.TRIM_MATERIAL))
            .executes(context -> {
                ItemManager.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemManager.getItemStack(context.getSource());
                ArmorTrimPattern pattern = getArmorTrimPatternArgument(context, "pattern");
                ArmorTrimMaterial material = getArmorTrimMaterialArgument(context, "material");

                NbtCompound trim = new NbtCompound();
                trim.putString(PATTERN_KEY, pattern.assetId().toString());
                trim.putString(MATERIAL_KEY, material.assetName());
                
                item.setSubNbt(TRIM_KEY, trim);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, pattern.description(), material.description()));
                return 1;
            })
            .build();
        
        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set [<pattern>] <material>
        node.addChild(setNode);
        setNode.addChild(setPatternNode);
        setPatternNode.addChild(setPatternMaterialNode);
    }
}
