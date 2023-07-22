package me.white.itemeditor.node;

import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.EnumArgumentType;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public class FlagsNode {
    private static final String OUTPUT_GET = "commands.edit.flags.get";
    private static final String OUTPUT_ALL_ENABLE = "commands.edit.flags.allenable";
    private static final String OUTPUT_ALL_DISABLE = "commands.edit.flags.alldisable";
    private static final String HIDEFLAGS_KEY = "HideFlags";

    private static enum Flag {
        ENCHANTMENT(0b00000001,
            "commands.edit.flags.enchantmentgetenabled",
            "commands.edit.flags.enchantmentsgetdisabled",
            "commands.edit.flags.enchantmentsenable",
            "commands.edit.flags.enchantmentsdisable"
        ),
        ATTRIBUTE(0b00000010,
            "commands.edit.flags.attributegetenabled",
            "commands.edit.flags.attributegetdisabled",
            "commands.edit.flags.attributeenable",
            "commands.edit.flags.attributedisable"
        ),
        UNBREAKABLE(0b00000100,
            "commands.edit.flags.unbreakablegetenabled",
            "commands.edit.flags.unbreakablegetdisabled",
            "commands.edit.flags.unbreakableenable",
            "commands.edit.flags.unbreakabledisable"
        ),
        DESTROY(0b00001000,
            "commands.edit.flags.destroygetenabled",
            "commands.edit.flags.destroygetdisabled",
            "commands.edit.flags.destroyenable",
            "commands.edit.flags.destroydisable"
        ),
        PLACE(0b00010000,
            "commands.edit.flags.placegetenabled",
            "commands.edit.flags.placegetdisabled",
            "commands.edit.flags.placeenable",
            "commands.edit.flags.placedisable"
        ),
        OTHER(0b00100000,
            "commands.edit.flags.othergetenabled",
            "commands.edit.flags.othergetdisabled",
            "commands.edit.flags.otherenable",
            "commands.edit.flags.otherdisable"
        ),
        DYED(0b01000000,
            "commands.edit.flags.dyedgetenabled",
            "commands.edit.flags.dyedgetdisabled",
            "commands.edit.flags.dyedenable",
            "commands.edit.flags.dyeddisable"
        ),
        TRIM(0b10000000,
            "commands.edit.flags.trimgetenabled",
            "commands.edit.flags.trimgetdisabled",
            "commands.edit.flags.trimenable",
            "commands.edit.flags.trimdisable"
        );

        int mask;
        String getEnabledTranslationKey;
        String getDisabledTranslationKey;
        String enableTranslationKey;
        String disableTranslationKey;

        private Flag(int mask, String getEnabledTranslationKey, String getDisabledTranslationKey, String enableTranslationKey, String disableTranslationKey) {
            this.mask = mask;
            this.getEnabledTranslationKey = getEnabledTranslationKey;
            this.getDisabledTranslationKey = getDisabledTranslationKey;
            this.enableTranslationKey = enableTranslationKey;
            this.disableTranslationKey = disableTranslationKey;
        }

        public boolean isPresent(int flags) {
            return (flags & mask) == mask;
        }
    }

    public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
        LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
            .literal("flags")
            .build();

        LiteralCommandNode<FabricClientCommandSource> getNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                Util.checkHasItem(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GET));
                for (Flag flag : Flag.values()) {
                    context.getSource().sendFeedback(Text.translatable(flag.isPresent(hideflags) ? flag.getDisabledTranslationKey : flag.getEnabledTranslationKey));
                }
                return hideflags;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Flag> getFlagNode = ClientCommandManager
            .argument("flag", EnumArgumentType.enumArgument(Flag.class))
            .executes(context -> {
                Util.checkHasItem(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource());
                Flag flag = EnumArgumentType.getEnum(context, "flag", Flag.class);

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);

                boolean result = flag.isPresent(hideflags);
                context.getSource().sendFeedback(Text.translatable(result ? flag.getDisabledTranslationKey : flag.getEnabledTranslationKey));
                return result ? 1 : 0;
            })
            .build();

        LiteralCommandNode<FabricClientCommandSource> toggleNode = ClientCommandManager
            .literal("toggle")
            .executes(context -> {
                Util.checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags == 0 ? -1 : 0);
                context.getSource().sendFeedback(Text.translatable(hideflags == 0 ? OUTPUT_ALL_ENABLE : OUTPUT_ALL_DISABLE));
                Util.setItemStack(context.getSource(), item);
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Flag> toggleFlagNode = ClientCommandManager
            .argument("flag", EnumArgumentType.enumArgument(Flag.class))
            .executes(context -> {
                Util.checkCanEdit(context.getSource());

                ItemStack item = Util.getItemStack(context.getSource()).copy();
                Flag flag = EnumArgumentType.getEnum(context, "flag", Flag.class);

                NbtCompound nbt = item.getNbt();
                int hideflags = 0;
                if (nbt != null) hideflags = nbt.getInt(HIDEFLAGS_KEY);

                boolean result = flag.isPresent(hideflags);
                item.getOrCreateNbt().putInt(HIDEFLAGS_KEY, hideflags ^ flag.mask);

                Util.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(result ? flag.enableTranslationKey : flag.disableTranslationKey));
                return result ? 1 : 0;
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
