package me.white.itemeditor.node;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.white.itemeditor.argument.EnumArgumentType;
import me.white.itemeditor.util.ItemUtil;
import me.white.itemeditor.util.EditorUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class FlagNode {
    private static final String OUTPUT_GET = "commands.edit.flags.get";
    private static final String OUTPUT_ALL_ENABLE = "commands.edit.flags.allenable";
    private static final String OUTPUT_ALL_DISABLE = "commands.edit.flags.alldisable";

    private enum Flag {
        ENCHANTMENT(0,
                "commands.edit.flags.enchantmentgetenabled",
                "commands.edit.flags.enchantmentgetdisabled",
                "commands.edit.flags.enchantmentenable",
                "commands.edit.flags.enchantmentdisable"
        ),
        ATTRIBUTE(1,
                "commands.edit.flags.attributegetenabled",
                "commands.edit.flags.attributegetdisabled",
                "commands.edit.flags.attributeenable",
                "commands.edit.flags.attributedisable"
        ),
        UNBREAKABLE(2,
                "commands.edit.flags.unbreakablegetenabled",
                "commands.edit.flags.unbreakablegetdisabled",
                "commands.edit.flags.unbreakableenable",
                "commands.edit.flags.unbreakabledisable"
        ),
        DESTROY(3,
                "commands.edit.flags.destroygetenabled",
                "commands.edit.flags.destroygetdisabled",
                "commands.edit.flags.destroyenable",
                "commands.edit.flags.destroydisable"
        ),
        PLACE(4,
                "commands.edit.flags.placegetenabled",
                "commands.edit.flags.placegetdisabled",
                "commands.edit.flags.placeenable",
                "commands.edit.flags.placedisable"
        ),
        OTHER(5,
                "commands.edit.flags.othergetenabled",
                "commands.edit.flags.othergetdisabled",
                "commands.edit.flags.otherenable",
                "commands.edit.flags.otherdisable"
        ),
        DYED(6,
                "commands.edit.flags.dyedgetenabled",
                "commands.edit.flags.dyedgetdisabled",
                "commands.edit.flags.dyedenable",
                "commands.edit.flags.dyeddisable"
        ),
        TRIM(7,
                "commands.edit.flags.trimgetenabled",
                "commands.edit.flags.trimgetdisabled",
                "commands.edit.flags.trimenable",
                "commands.edit.flags.trimdisable"
        );

        final int position;
        final String getEnabledTranslationKey;
        final String getDisabledTranslationKey;
        final String enableTranslationKey;
        final String disableTranslationKey;

        Flag(int position, String getEnabledTranslationKey, String getDisabledTranslationKey, String enableTranslationKey, String disableTranslationKey) {
            this.position = position;
            this.getEnabledTranslationKey = getEnabledTranslationKey;
            this.getDisabledTranslationKey = getDisabledTranslationKey;
            this.enableTranslationKey = enableTranslationKey;
            this.disableTranslationKey = disableTranslationKey;
        }

        public static Flag byPosition(int position) {
            for (Flag flag : Flag.values()) {
                if (flag.position == position) return flag;
            }
            return null;
        }
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
                .literal("flag")
                .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
                .literal("get")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    List<Boolean> flags = ItemUtil.getFlags(stack);
                    context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                    for (int i = 0; i < ItemUtil.FLAGS_AMOUNT; ++i) {
                        Flag flag = Flag.byPosition(i);
                        if (flag == null) continue;
                        context.getSource().sendFeedback(Text.translatable(flags.get(i) ? flag.getDisabledTranslationKey : flag.getEnabledTranslationKey));
                    }
                    return 1;
                })
                .build();

        ArgumentCommandNode<FabricClientCommandSource, Flag> getFlagNode = ClientCommandManager
                .argument("flag", EnumArgumentType.enumArgument(Flag.class))
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource());
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    Flag flag = EnumArgumentType.getEnum(context, "flag", Flag.class);
                    List<Boolean> flags = ItemUtil.getFlags(stack);

                    context.getSource().sendFeedback(Text.translatable(flags.get(flag.position) ? flag.getDisabledTranslationKey : flag.getEnabledTranslationKey));
                    return flags.get(flag.position) ? 0 : 1;
                })
                .build();

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
                .literal("toggle")
                .executes(context -> {
                    ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
                    if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
                    if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
                    boolean toggle = ItemUtil.getFlags(stack).contains(true);
                    if (toggle) {
                        ItemUtil.setFlags(stack, null);
                    } else {
                        List<Boolean> flags = new ArrayList<>();
                        for (int i = 0; i < ItemUtil.FLAGS_AMOUNT; ++i) {
                            flags.add(true);
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
                    List<Boolean> flags = ItemUtil.getFlags(stack);
                    boolean toggle = flags.get(flag.position);
                    flags.set(flag.position, !toggle);
                    ItemUtil.setFlags(stack, flags);

                    EditorUtil.setStack(context.getSource(), stack);
                    context.getSource().sendFeedback(Text.translatable(toggle ? flag.enableTranslationKey : flag.disableTranslationKey));
                    return toggle ? 0 : 1;
                })
                .build();

        rootNode.addChild(node);

        // ... get [<flag>]
        node.addChild(getNode);
        getNode.addChild(getFlagNode);

        // ... toggle <flag>
        node.addChild(toggleNode);
        toggleNode.addChild(toggleFlagNode);
    }
}
