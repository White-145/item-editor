package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.argument.EnumArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.Node;
import me.white.sie.util.EditorUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class RarityNode implements Node {
    private static final CommandSyntaxException ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.rarity.error.alreadyis")).create();
    private static final String OUTPUT_GET = "commands.edit.rarity.get";
    private static final String OUTPUT_SET = "commands.edit.rarity.set";
    private static final String RARITY_COMMON = "rarity.minecraft.common";
    private static final String RARITY_UNCOMMON = "rarity.minecraft.uncommon";
    private static final String RARITY_RARE = "rarity.minecraft.rare";
    private static final String RARITY_EPIC = "rarity.minecraft.epic";

    private static Component getTranslation(Rarity rarity) {
        return switch(rarity) {
            case COMMON -> Component.translatable(RARITY_COMMON);
            case UNCOMMON -> Component.translatable(RARITY_UNCOMMON);
            case RARE -> Component.translatable(RARITY_RARE);
            case EPIC -> Component.translatable(RARITY_EPIC);
        };
    }

    private static Rarity getRarity(ItemStack stack) {
        return stack.getOrDefault(DataComponents.RARITY, Rarity.COMMON);
    }

    private static void setRarity(ItemStack stack, Rarity rarity) {
        stack.set(DataComponents.RARITY, rarity);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("rarity").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            Rarity rarity = getRarity(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_GET, getTranslation(rarity)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> setNode = commandManager.literal("set").build();

        CommandNode<S> setRarityNode = commandManager.argument("rarity", EnumArgumentType.enums(Rarity.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            Rarity rarity = context.getArgument("rarity", Rarity.class);
            if (getRarity(stack) == rarity) {
                throw ALREADY_IS_EXCEPTION;
            }
            setRarity(stack, rarity);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_SET, getTranslation(rarity)));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... get
        node.addChild(getNode);

        // ... set <rarity>
        node.addChild(setNode);
        setNode.addChild(setRarityNode);

        return node;
    }
}
