package me.white.simpleitemeditor.mixin;

import com.mojang.brigadier.CommandDispatcher;
import me.white.simpleitemeditor.SimpleItemEditor;
import me.white.simpleitemeditor.command.EditCommand;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Shadow
    private CommandDispatcher<?> commandDispatcher;
    @Final
    @Shadow
    private FeatureSet enabledFeatures;
    @Final
    @Shadow
    private DynamicRegistryManager.Immutable combinedDynamicRegistries;

    @SuppressWarnings("unchecked")
    @Inject(method = "onCommandTree(Lnet/minecraft/network/packet/s2c/play/CommandTreeS2CPacket;)V", at = @At("RETURN"))
    public void onCommandTree(CommandTreeS2CPacket packet, CallbackInfo ci) {
        SimpleItemEditor.clientCommandDispatcher = new CommandDispatcher<>();
        EditCommand.registerClient((CommandDispatcher<FabricClientCommandSource>)commandDispatcher, SimpleItemEditor.clientCommandDispatcher, CommandRegistryAccess.of(this.combinedDynamicRegistries, this.enabledFeatures));

    }

    @Inject(method = "sendCommand(Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
    public void sendCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        if (SimpleItemEditor.executeCommand(command)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "sendChatCommand(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void sendChatCommand(String command, CallbackInfo ci) {
        if (SimpleItemEditor.executeCommand(command)) {
            ci.cancel();
        }
    }
}
