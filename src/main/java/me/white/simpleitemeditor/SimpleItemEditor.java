package me.white.simpleitemeditor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import me.white.simpleitemeditor.command.EditCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleItemEditor implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("item-editor");

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(EditCommand::register);
    }
    // + added ability to set potion type for potions and change icon visibility and ambient
    // + `/edit component` for manual component control
    // + data sources for `/edit data`
    // + ability to completely remove tooltip
    // + `/edit rarity`
    // + more duration control in `/edit potion`
    // + negative infinity option in attributes
    // + `/edit count max`
    // + `/edit durability max`
    // ~ heavily revised and re-tested code
    // ~ fixed various bugs with argument types
    // ~ removed ambiguous syntax for removing a component (e.g. `/edit trim set`)
    // ~ reworked `/edit attribute`
    // ~ split `/edit name` and `... name custom`
    // ~ renamed `/edit durability percent` to `... progress`
    // ~ replaced `... toggle` commands with `... set <bool>`
    // ~ renamed `/edit flag` to `... tooltip`
    // ~ reworked `/edit data` for new nbt components
    // ~ moved `/edit color` for potions to its respective node
    // - removed few problematic nodes for a while
}
