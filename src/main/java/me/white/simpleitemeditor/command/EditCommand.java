package me.white.simpleitemeditor.command;

import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.ClientCommand;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.node.*;
import me.white.simpleitemeditor.node.TooltipNode;
import me.white.simpleitemeditor.util.CommonCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.util.Identifier;

public class EditCommand {
    // Components as of 1.21.4:

    // attribute_modifiers          — attribute
    // banner_patterns              — banner
    // base_color                   — banner base
    // bees                         —
    // block_entity_data            — data block
    // block_state                  —
    // bucket_entity_data           — data bucket
    // bundle_contents              —
    // can_break                    —
    // can_place_on                 —
    // charged_projectiles          —
    // consumable                   —
    // container                    —
    // container_loot               —
    // custom_data                  — data custom
    // custom_model_data            —
    // custom_name                  — name custom
    // damage                       — durability
    // damage_resistant             —
    // debug_stick_state            —
    // death_protection             —
    // dyed_color                   — color
    // enchantable                  —
    // enchantment_glint_override   — enchantment glint
    // enchantments                 — enchantment
    // entity_data                  — data entity
    // equippable                   —
    // firework_explosion           —
    // fireworks                    —
    // food                         —
    // glider                       —
    // hide_additional_tooltip      — tooltip additional
    // hide_tooltip                 — tooltip all
    // instrument                   —
    // intangible_projectile        —
    // item_model                   —
    // item_name                    — name item
    // jukebox_playable             —
    // lock                         —
    // lodestone_tracker            —
    // lore                         — lore
    // map_color                    —
    // map_decorations              —
    // map_id                       —
    // max_damage                   — durability max
    // max_stack_size               —
    // note_block_sound             — head sound
    // ominous_bottle_amplifier     —
    // pot_decorations              —
    // potion_contents              — potion
    // profile                      — head
    // rarity                       — rarity
    // recipes                      —
    // repairable                   —
    // repair_cost                  —
    // stored_enchantments          — enchantment stored
    // suspicious_stew_effects      —
    // tool                         —
    // tooltip_style                —
    // trim                         — trim
    // unbreakable                  — durability
    // use_cooldown                 —
    // use_remainder                —
    // writable_book_content        —
    // written_book_content         —

    private static Node[] NODES = new Node[]{
            new AttributeNode(),
            new BannerNode(),
            new ComponentNode(),
            new ColorNode(),
            new CountNode(),
            new DataNode(),
            new DurabilityNode(),
            new EnchantmentNode(),
            new EquipNode(),
            new GetNode(),
            new HeadNode(),
            new LoreNode(),
            new MaterialNode(),
            new NameNode(),
            new PotionNode(),
            new RarityNode(),
            new TrimNode(),
            new TooltipNode()
    };

    public static final ClientCommand PROVIDER = new ClientCommand(Identifier.of("sie", "edit"), (name, registryAccess) -> {
        CommonCommandManager<FabricClientCommandSource> commandManager = new CommonCommandManager<>();
        CommandNode<FabricClientCommandSource> node = commandManager.literal(name).build();
        for (Node childNode : NODES) {
            try {
                node.addChild(childNode.register(commandManager, registryAccess));
            } catch (IllegalStateException e) {
                SimpleItemEditor.LOGGER.error("Failed to register {}", childNode.getClass().getName(), e);
            }
        }
        return node;
    });
}
