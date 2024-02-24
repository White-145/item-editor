package me.white.simpleitemeditor.node;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.util.ItemUtil;
import me.white.simpleitemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class FlagNode implements Node {
    private static final String OUTPUT_GET = "commands.edit.flags.get";
    private static final String OUTPUT_ALL_ENABLE = "commands.edit.flags.allenable";
    private static final String OUTPUT_ALL_DISABLE = "commands.edit.flags.alldisable";

    private enum Flag {
        ENCHANTMENT(1,
                "commands.edit.flags.enchantmentgetenabled",
                "commands.edit.flags.enchantmentgetdisabled",
                "commands.edit.flags.enchantmentenable",
                "commands.edit.flags.enchantmentdisable"
        ),
        ATTRIBUTE(2,
                "commands.edit.flags.attributegetenabled",
                "commands.edit.flags.attributegetdisabled",
                "commands.edit.flags.attributeenable",
                "commands.edit.flags.attributedisable"
        ),
        UNBREAKABLE(4,
                "commands.edit.flags.unbreakablegetenabled",
                "commands.edit.flags.unbreakablegetdisabled",
                "commands.edit.flags.unbreakableenable",
                "commands.edit.flags.unbreakabledisable"
        ),
        DESTROY(8,
                "commands.edit.flags.destroygetenabled",
                "commands.edit.flags.destroygetdisabled",
                "commands.edit.flags.destroyenable",
                "commands.edit.flags.destroydisable"
        ),
        PLACE(16,
                "commands.edit.flags.placegetenabled",
                "commands.edit.flags.placegetdisabled",
                "commands.edit.flags.placeenable",
                "commands.edit.flags.placedisable"
        ),
        OTHER(32,
                "commands.edit.flags.othergetenabled",
                "commands.edit.flags.othergetdisabled",
                "commands.edit.flags.otherenable",
                "commands.edit.flags.otherdisable"
        ),
        DYED(64,
                "commands.edit.flags.dyedgetenabled",
                "commands.edit.flags.dyedgetdisabled",
                "commands.edit.flags.dyedenable",
                "commands.edit.flags.dyeddisable"
        ),
        TRIM(128,
                "commands.edit.flags.trimgetenabled",
                "commands.edit.flags.trimgetdisabled",
                "commands.edit.flags.trimenable",
                "commands.edit.flags.trimdisable"
        );

        final int mask;
        final String getEnabledTranslationKey;
        final String getDisabledTranslationKey;
        final String enableTranslationKey;
        final String disableTranslationKey;

        Flag(int mask, String getEnabledTranslationKey, String getDisabledTranslationKey, String enableTranslationKey, String disableTranslationKey) {
            this.mask = mask;
            this.getEnabledTranslationKey = getEnabledTranslationKey;
            this.getDisabledTranslationKey = getDisabledTranslationKey;
            this.enableTranslationKey = enableTranslationKey;
            this.disableTranslationKey = disableTranslationKey;
        }

        public boolean isEnabled(int flags) {
            return (flags & mask) == mask;
        }
    }

    public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("flag")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    int flags = ItemUtil.getFlags(stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                    for (int i = 0; i < ItemUtil.FLAGS_AMOUNT; ++i) {
                        Flag flag = Flag.values()[i];
                        context.getSource().sendFeedback(Text.translatable(flag.isEnabled(flags) ? flag.getDisabledTranslationKey : flag.getEnabledTranslationKey));
                    }
                    return flags;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Flag> getFlagNode = ClientCommandManager
                .argument("flag", EnumArgumentType.enumArgument(Flag.class))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    Flag flag = EnumArgumentType.getEnum(context, "flag", Flag.class);
                    int flags = ItemUtil.getFlags(stack);

                    context.getSource().sendFeedback(Text.translatable(flag.isEnabled(flags) ? flag.getDisabledTranslationKey : flag.getEnabledTranslationKey));
                    return flag.isEnabled(flags) ? 1 : 0;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    boolean toggle = ItemUtil.getFlags(stack) != 0;
                    if (toggle) {
                        ItemUtil.setFlags(stack, null);
                    } else {
                        int flags = 0;
                        for (int i = 0; i < ItemUtil.FLAGS_AMOUNT; ++i) {
                            flags += Flag.values()[i].mask;
                        }
                        ItemUtil.setFlags(stack, flags);
                    }

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(toggle ? OUTPUT_ALL_ENABLE : OUTPUT_ALL_DISABLE));
                    return toggle ? 0 : 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Flag> toggleFlagNode = ClientCommandManager
                .argument("flag", EnumArgumentType.enumArgument(Flag.class))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    Flag flag = EnumArgumentType.getEnum(context, "flag", Flag.class);
                    int flags = ItemUtil.getFlags(stack);
                    boolean toggle = flag.isEnabled(flags);
                    if (toggle) {
                        flags -= flag.mask;
                    } else {
                        flags += flag.mask;
                    }
                    ItemUtil.setFlags(stack, flags);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(toggle ? flag.enableTranslationKey : flag.disableTranslationKey));
                    return toggle ? 0 : 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> toggleAllNode = ClientCommandManager
                .literal("all")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    boolean toggle = ItemUtil.getFlags(stack) != 0;
                    if (toggle) {
                        ItemUtil.setFlags(stack, null);
                    } else {
                        int flags = 0;
                        for (int i = 0; i < ItemUtil.FLAGS_AMOUNT; ++i) {
                            flags += Flag.values()[i].mask;
                        }
                        ItemUtil.setFlags(stack, flags);
                    }

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(toggle ? OUTPUT_ALL_ENABLE : OUTPUT_ALL_DISABLE));
                    return toggle ? 0 : 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get [<flag>]
        node.addChild(getNode);
        getNode.addChild(getFlagNode);

        // ... toggle
        node.addChild(toggleNode);
        // ... <flag>
        toggleNode.addChild(toggleFlagNode);
        // ... all
        toggleNode.addChild(toggleAllNode);
    }
}
