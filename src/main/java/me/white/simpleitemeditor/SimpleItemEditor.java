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
    // - moved `/edit color` for potions and maps to their respective nodes
    // ~ removed ambiguous syntax for removing a component (e.g. `/edit trim set`)
    // ~ fixed various bugs with argument types
    // ~ heavily revised and re-tested code
    // - removed `/edit whitelist`, `/edit book`, and `/edit firework` for now
    // ~ reworked `/edit attribute`
    // ~ renamed `/edit durability percent` to `... progress`
    // ~ replaced `... toggle` commands with `... set <bool>`
    // ~ moved visbility flags to their respective nodes
    // + added ability to set potion type for potions and change icon visibility and ambient
    // ~ reworked `/edit data` for new nbt components
}
