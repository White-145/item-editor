package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
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
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("rarity").build();

        CommandNode<S> getNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            Rarity rarity = getRarity(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET, getTranslation(rarity)));
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
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET, getTranslation(rarity)));
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
