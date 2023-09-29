package me.white.simpleitemeditor;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import me.white.simpleitemeditor.command.RegistryCommand;
import me.white.simpleitemeditor.command.EditCommand;
import net.minecraft.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleItemEditor implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("item-editor");

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(EditCommand::register);
        if (SharedConstants.isDevelopment) {
            ClientCommandRegistrationCallback.EVENT.register(RegistryCommand::register);
        }
        // TODO:
        // color command
        // character command
        // craft command

    }
}
