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
    // ~ fixed `/edit model remove`
    // - removed `/edit color` for potions and maps
    // ~ removed ambiguous syntax for removing a component (e.g. `/edit trim set`)
    // ~ fixed various bugs with argument types
    // ~ heavily revised and re-tested code
    // - removed `/edit whitelist` for now
    // ~ reworked `/edit attribute`
    // ~ renamed `/edit durability percent` to `... progress`
    // ~ replaced `... toggle` commands with `... set <bool>`
    // ~ moved visbility flags to their respective nodes
}
