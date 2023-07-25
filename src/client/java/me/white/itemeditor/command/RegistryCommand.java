package me.white.itemeditor.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.itemeditor.ItemEditor;
import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

public class RegistryCommand {
    private static final String OUTPUT_REGISTRY = "commands.testing.registry";

    private static <T> void registerRegistry(LiteralCommandNode<FabricClientCommandSource> node, CommandRegistryAccess registryAccess, RegistryKey<Registry<T>> key) {
        try {
            LiteralCommandNode<FabricClientCommandSource> registryNameNode = ClientCommandManager
                    .literal(key.getValue().toString())
                    .build();

            ArgumentCommandNode<FabricClientCommandSource, RegistryEntry.Reference<T>> registryNameRegistryNode = ClientCommandManager
                    .argument("registry", RegistryEntryArgumentType.registryEntry(registryAccess, key))
                    .executes(context -> {
                        T value = EditorUtil.getRegistryEntryArgument(context, "registry", key);
                        context.getSource().sendFeedback(Text.translatable(OUTPUT_REGISTRY, value.toString()));
                        return 1;
                    })
                    .build();

            node.addChild(registryNameNode);
            registryNameNode.addChild(registryNameRegistryNode);
        } catch (IllegalStateException e) {
            ItemEditor.LOGGER.error(String.format("Failed to load registry %s", key.getValue()));
        }
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("registry")
                .build();

        LiteralCommandNode<FabricClientCommandSource> nodeNamespaced = ClientCommandManager
                .literal("itemeditor:registry")
                .redirect(node)
                .build();

        registerRegistry(node, registryAccess, RegistryKeys.ACTIVITY);
        registerRegistry(node, registryAccess, RegistryKeys.ATTRIBUTE);
        registerRegistry(node, registryAccess, RegistryKeys.BANNER_PATTERN);
        registerRegistry(node, registryAccess, RegistryKeys.BIOME);
        registerRegistry(node, registryAccess, RegistryKeys.BIOME_SOURCE);
        registerRegistry(node, registryAccess, RegistryKeys.BLOCK);
        registerRegistry(node, registryAccess, RegistryKeys.BLOCK_ENTITY_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.BLOCK_PREDICATE_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.BLOCK_STATE_PROVIDER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.CARVER);
        registerRegistry(node, registryAccess, RegistryKeys.CAT_VARIANT);
        registerRegistry(node, registryAccess, RegistryKeys.CHUNK_GENERATOR);
        registerRegistry(node, registryAccess, RegistryKeys.CHUNK_STATUS);
        registerRegistry(node, registryAccess, RegistryKeys.COMMAND_ARGUMENT_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.CUSTOM_STAT);
        registerRegistry(node, registryAccess, RegistryKeys.DAMAGE_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.DECORATED_POT_PATTERN);
        registerRegistry(node, registryAccess, RegistryKeys.DENSITY_FUNCTION_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.DIMENSION_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.ENCHANTMENT);
        registerRegistry(node, registryAccess, RegistryKeys.ENTITY_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.FEATURE);
        registerRegistry(node, registryAccess, RegistryKeys.FEATURE_SIZE_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.FLUID);
        registerRegistry(node, registryAccess, RegistryKeys.FLOAT_PROVIDER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.FROG_VARIANT);
        registerRegistry(node, registryAccess, RegistryKeys.FOLIAGE_PLACER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.GAME_EVENT);
        registerRegistry(node, registryAccess, RegistryKeys.HEIGHT_PROVIDER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.INSTRUMENT);
        registerRegistry(node, registryAccess, RegistryKeys.INT_PROVIDER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.ITEM);
        registerRegistry(node, registryAccess, RegistryKeys.ITEM_GROUP);
        registerRegistry(node, registryAccess, RegistryKeys.LOOT_CONDITION_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.LOOT_FUNCTION_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.LOOT_NBT_PROVIDER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.LOOT_NUMBER_PROVIDER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.LOOT_POOL_ENTRY_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.LOOT_SCORE_PROVIDER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.MATERIAL_CONDITION);
        registerRegistry(node, registryAccess, RegistryKeys.MATERIAL_RULE);
        registerRegistry(node, registryAccess, RegistryKeys.MESSAGE_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.MEMORY_MODULE_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.PAINTING_VARIANT);
        registerRegistry(node, registryAccess, RegistryKeys.PARTICLE_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.PLACEMENT_MODIFIER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.POTION);
        registerRegistry(node, registryAccess, RegistryKeys.POINT_OF_INTEREST_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.POSITION_SOURCE_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.POS_RULE_TEST);
        registerRegistry(node, registryAccess, RegistryKeys.RECIPE_SERIALIZER);
        registerRegistry(node, registryAccess, RegistryKeys.RECIPE_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.ROOT_PLACER_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.RULE_BLOCK_ENTITY_MODIFIER);
        registerRegistry(node, registryAccess, RegistryKeys.RULE_TEST);
        registerRegistry(node, registryAccess, RegistryKeys.SCHEDULE);
        registerRegistry(node, registryAccess, RegistryKeys.SCREEN_HANDLER);
        registerRegistry(node, registryAccess, RegistryKeys.SENSOR_TYPE);
        registerRegistry(node, registryAccess, RegistryKeys.STATUS_EFFECT);
        registerRegistry(node, registryAccess, RegistryKeys.TRIM_MATERIAL);
        registerRegistry(node, registryAccess, RegistryKeys.TRIM_PATTERN);
        registerRegistry(node, registryAccess, RegistryKeys.VILLAGER_PROFESSION);
        registerRegistry(node, registryAccess, RegistryKeys.VILLAGER_TYPE);

        dispatcher.getRoot().addChild(node);
        dispatcher.getRoot().addChild(nodeNamespaced);
    }
}
