package me.white.itemeditor.node;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.EnumArgumentType;
import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class FlagsNode {
    private static final String OUTPUT_GET = "commands.edit.flags.get";
    private static final String OUTPUT_ALL_ENABLE = "commands.edit.flags.allenable";
    private static final String OUTPUT_ALL_DISABLE = "commands.edit.flags.alldisable";

    private static enum Flag {
        ENCHANTMENT(0,
            "commands.edit.flags.enchantmentgetenabled",
            "commands.edit.flags.enchantmentsgetdisabled",
            "commands.edit.flags.enchantmentsenable",
            "commands.edit.flags.enchantmentsdisable"
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

        int position;
        String getEnabledTranslationKey;
        String getDisabledTranslationKey;
        String enableTranslationKey;
        String disableTranslationKey;

        private Flag(int position, String getEnabledTranslationKey, String getDisabledTranslationKey, String enableTranslationKey, String disableTranslationKey) {
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

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("flags")
            .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                List<Boolean> flags = EditHelper.getFlags(stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                for (int i = 0; i < EditHelper.FLAGS_AMOUNT; ++i) {
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
                ItemStack stack = Util.getItemStack(context.getSource());
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                Flag flag = EnumArgumentType.getEnum(context, "flag", Flag.class);
                List<Boolean> flags = EditHelper.getFlags(stack);

                context.getSource().sendFeedback(Text.translatable(flags.get(flag.position) ? flag.getDisabledTranslationKey : flag.getEnabledTranslationKey));
                return flags.get(flag.position) ? 0 : 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
            .literal("toggle")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                boolean toggle = EditHelper.getFlags(stack).contains(true);
                if (toggle) {
                    EditHelper.setFlags(stack, null);
                } else {
                    List<Boolean> flags = new ArrayList<>();
                    for (int i = 0; i < EditHelper.FLAGS_AMOUNT; ++i) {
                        flags.add(true);
                    }
                    EditHelper.setFlags(stack, flags);
                }

                Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(toggle ? OUTPUT_ALL_ENABLE : OUTPUT_ALL_DISABLE));
                return toggle ? 0 : 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Flag> toggleFlagNode = ClientCommandManager
            .argument("flag", EnumArgumentType.enumArgument(Flag.class))
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
                if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
                if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
                Flag flag = EnumArgumentType.getEnum(context, "flag", Flag.class);
                List<Boolean> flags = EditHelper.getFlags(stack);
                boolean toggle = flags.get(flag.position);
                flags.set(flag.position, !toggle);
                EditHelper.setFlags(stack, flags);

                Util.setItemStack(context.getSource(), stack);
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
