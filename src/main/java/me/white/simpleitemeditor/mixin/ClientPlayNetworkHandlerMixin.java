package me.white.simpleitemeditor.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.command.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow
    private CommandDispatcher<CommandSource> commandDispatcher;
    @Final
    @Shadow
    private FeatureSet enabledFeatures;
    @Final
    @Shadow
    private DynamicRegistryManager.Immutable combinedDynamicRegistries;
    @Unique
    private static CommandDispatcher<FabricClientCommandSource> clientCommandDispatcher = new CommandDispatcher<>();

    @SuppressWarnings("unchecked")
    @Inject(method = "onCommandTree(Lnet/minecraft/network/packet/s2c/play/CommandTreeS2CPacket;)V", at = @At("RETURN"))
    public void onCommandTree(CommandTreeS2CPacket packet, CallbackInfo ci) {
        clientCommandDispatcher = new CommandDispatcher<>();
        CommandNode<FabricClientCommandSource> node = EditCommand.PROVIDER.register(commandDispatcher, CommandRegistryAccess.of(this.combinedDynamicRegistries, this.enabledFeatures));

        clientCommandDispatcher.getRoot().addChild(node);
        commandDispatcher.getRoot().addChild((CommandNode<CommandSource>)(CommandNode<? extends CommandSource>)node);
    }

    @Inject(method = "sendChatCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void sendChatCommand(String command, CallbackInfo ci) {
        if (executeCommand(command)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean executeCommand(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        FabricClientCommandSource commandSource = (FabricClientCommandSource)client.getNetworkHandler().getCommandSource();

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
            Text message = Texts.toText(e.getRawMessage());
            String context = e.getContext();
            Text errorMessage = context != null ? Text.translatable("command.context.parse_error", message, e.getCursor(), context) : message;
            commandSource.sendError(errorMessage);
        } catch (Exception e) {
            SimpleItemEditor.LOGGER.warn("Error while executing client-sided command '{}'", command, e);
            commandSource.sendError(Text.of(e.getMessage()));
        }
        return true;
    }
}
