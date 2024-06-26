package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

public class RarityNode implements Node {
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.rarity.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.rarity.get";
    private static final String OUTPUT_SET = "commands.edit.rarity.set";
    private static final String RARITY_COMMON = "rarity.minecraft.common";
    private static final String RARITY_UNCOMMON = "rarity.minecraft.uncommon";
    private static final String RARITY_RARE = "rarity.minecraft.rare";
    private static final String RARITY_EPIC = "rarity.minecraft.epic";

    private static Text getTranslation(Rarity rarity) {
        return switch(rarity) {
            case COMMON -> Text.translatable(RARITY_COMMON);
            case UNCOMMON -> Text.translatable(RARITY_UNCOMMON);
            case RARE -> Text.translatable(RARITY_RARE);
            case EPIC -> Text.translatable(RARITY_EPIC);
        };
    }

    private static Rarity getRarity(ItemStack stack) {
        return stack.getOrDefault(DataComponentTypes.RARITY, Rarity.COMMON);
    }

    private static void setRarity(ItemStack stack, Rarity rarity) {
        stack.set(DataComponentTypes.RARITY, rarity);
    }

    @Override
    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager.literal("rarity").build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getStack(context.getSource());
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            Rarity rarity = getRarity(stack);

            context.getSource().sendFeedback(Text.translatable(OUTPUT_GET, getTranslation(rarity)));
            return Command.SINGLE_SUCCESS;
        }).build();

        LiteralCommandNode<FabricClientCommandSource> setNode = ClientCommandManager.literal("set").build();

        ArgumentCommandNode<FabricClientCommandSource, Rarity> setRarityNode = ClientCommandManager.argument("rarity", EnumArgumentType.enumArgument(Rarity.class)).executes(context -> {
            if (!EditorUtil.hasCreative(context.getSource())) {
                throw EditorUtil.NOT_CREATIVE_EXCEPTION;
            }
            ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
            if (!EditorUtil.hasItem(stack)) {
                throw EditorUtil.NO_ITEM_EXCEPTION;
            }
            Rarity rarity = EnumArgumentType.getEnum(context, "rarity", Rarity.class);
            if (getRarity(stack) == rarity) {
                throw ALREADY_IS_EXCEPTION;
            }
            setRarity(stack, rarity);

            EditorUtil.setStack(context.getSource(), stack);
            context.getSource().sendFeedback(Text.translatable(OUTPUT_SET, getTranslation(rarity)));
            return Command.SINGLE_SUCCESS;
        }).build();

        rootNode.addChild(node);

        // ... get
        node.addChild(getNode);

        // ... set <rarity>
        node.addChild(setNode);
        setNode.addChild(setRarityNode);
    }
}
