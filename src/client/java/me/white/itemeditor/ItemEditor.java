package me.white.itemeditor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.white.itemeditor.command.EditCommand;

public class ItemEditor implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("itemeditor");

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(EditCommand::register);
        // TODO /char command
        // TODO /color command
        // TODO /craft command
        
        // TODO make translations
        // TODO test everything
        // TODO text argument type
    }
}
