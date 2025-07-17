package me.white.simpleitemeditor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleItemEditor implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("item-editor");
    public static CommandDispatcher<FabricClientCommandSource> clientCommandDispatcher = new CommandDispatcher<>();

    public static boolean executeCommand(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        FabricClientCommandSource commandSource = (FabricClientCommandSource)client.getNetworkHandler().getCommandSource();

        try {
            clientCommandDispatcher.execute(command, commandSource);
        } catch (CommandSyntaxException e) {
            CommandExceptionType type = e.getType();
            BuiltInExceptionProvider builtins = CommandSyntaxException.BUILT_IN_EXCEPTIONS;
            if (type == builtins.dispatcherUnknownCommand() || type == builtins.dispatcherParseException()) {
                LOGGER.debug("Syntax exception for client-sided command '{}'", command, e);
                return false;
            }
            LOGGER.warn("Syntax exception for client-sided command '{}'", command, e);
            Text message = Texts.toText(e.getRawMessage());
            String context = e.getContext();
            Text errorMessage = context != null ? Text.translatable("command.context.parse_error", message, e.getCursor(), context) : message;
            commandSource.sendError(errorMessage);
        } catch (Exception e) {
            LOGGER.warn("Error while executing client-sided command '{}'", command, e);
            commandSource.sendError(Text.of(e.getMessage()));
        }
        return true;
    }

    @Override
    public void onInitializeClient() { }
}
