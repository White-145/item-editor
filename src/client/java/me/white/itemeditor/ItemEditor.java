package me.white.itemeditor;

import me.white.itemeditor.command.RegistryCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import me.white.itemeditor.command.EditCommand;
import net.minecraft.SharedConstants;
import org.mineskin.MineskinClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemEditor implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("item-editor");
    private static MineskinClient mineskin = null;

    public static MineskinClient getMineskinInstance() {
        if (mineskin == null) mineskin = new MineskinClient("ItemEditorHeadGenerator-Generator");
        return mineskin;
    }

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
