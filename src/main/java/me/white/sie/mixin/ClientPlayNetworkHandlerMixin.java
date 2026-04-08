package me.white.sie.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.ClientCommand;
import me.white.sie.SimpleItemEditor;
import me.white.sie.command.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow
    private CommandDispatcher<SharedSuggestionProvider> commands;
    @Final
    @Shadow
    private FeatureFlagSet enabledFeatures;
    @Final
    @Shadow
    private RegistryAccess.Frozen registryAccess;
    @Unique
    private static final ClientCommand[] PROVIDERS = {
            EditCommand.PROVIDER
    };
    @Unique
    private static CommandDispatcher<FabricClientCommandSource> clientCommandDispatcher = new CommandDispatcher<>();

    @SuppressWarnings("unchecked")
    @Inject(method = "handleCommands(Lnet/minecraft/network/protocol/game/ClientboundCommandsPacket;)V", at = @At("RETURN"))
    public void onCommandTree(ClientboundCommandsPacket packet, CallbackInfo ci) {
        clientCommandDispatcher = new CommandDispatcher<>();
        CommandBuildContext access = CommandBuildContext.simple(this.registryAccess, this.enabledFeatures);
        for (ClientCommand provider : PROVIDERS) {
            CommandNode<FabricClientCommandSource> node = provider.register(commands, access);
            clientCommandDispatcher.getRoot().addChild(node);
            commands.getRoot().addChild((CommandNode<SharedSuggestionProvider>)(CommandNode<? extends SharedSuggestionProvider>)node);
        }
    }

    @Inject(method = "sendCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void sendChatCommand(String command, CallbackInfo ci) {
        if (executeCommand(command)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean executeCommand(String command) {
        Minecraft client = Minecraft.getInstance();
        FabricClientCommandSource commandSource = (FabricClientCommandSource)client.getConnection().getSuggestionsProvider();

        try {
            clientCommandDispatcher.execute(command, commandSource);
        } catch (CommandSyntaxException e) {
            CommandExceptionType type = e.getType();
            BuiltInExceptionProvider builtins = CommandSyntaxException.BUILT_IN_EXCEPTIONS;
            if (type == builtins.dispatcherUnknownCommand() || type == builtins.dispatcherParseException()) {
                SimpleItemEditor.LOGGER.debug("Syntax exception for client-sided command '{}'", command, e);
                return false;
            }
            SimpleItemEditor.LOGGER.warn("Syntax exception for client-sided command '{}'", command, e);
            Component message = ComponentUtils.fromMessage(e.getRawMessage());
            String context = e.getContext();
            Component errorMessage = context != null ? Component.translatable("command.context.parse_error", message, e.getCursor(), context) : message;
            commandSource.sendError(errorMessage);
        } catch (Exception e) {
            SimpleItemEditor.LOGGER.warn("Error while executing client-sided command '{}'", command, e);
            commandSource.sendError(Component.literal(e.getMessage()));
        }
        return true;
    }
}
