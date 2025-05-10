package org.vivecraft.mixin.client_vr.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerVRMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void vivecraft$init(CallbackInfo ci) {
        if (ClientNetworking.NEEDS_RESET) {
            ClientNetworking.resetServerSettings();
            ClientNetworking.DISPLAYED_CHAT_MESSAGE = false;
            ClientNetworking.DISPLAYED_CHAT_WARNING = false;
            ClientNetworking.NEEDS_RESET = false;
        }
    }

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void vivecraft$resetOnLogin(CallbackInfo ci) {
        // clear old data
        ClientNetworking.resetServerSettings();

        // request server data
        ClientNetworking.sendVersionInfo();

        if (VRState.VR_INITIALIZED) {
            // set the timer, even if vr is currently not running
            ClientDataHolderVR.getInstance().vrPlayer.chatWarningTimer = 200;
            ClientDataHolderVR.getInstance().vrPlayer.teleportWarning = true;
            ClientDataHolderVR.getInstance().vrPlayer.vrSwitchWarning = false;
        }
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", shift = At.Shift.AFTER))
    private void vivecraft$resetOnDimensionChange(CallbackInfo ci) {
        // clear old data
        ClientNetworking.resetServerSettings();

        // request server data
        ClientNetworking.sendVersionInfo();

        if (VRState.VR_INITIALIZED) {
            // set the timer, even if vr is currently not running
            ClientDataHolderVR.getInstance().vrPlayer.chatWarningTimer = 200;
            ClientDataHolderVR.getInstance().vrPlayer.teleportWarning = true;
            ClientDataHolderVR.getInstance().vrPlayer.vrSwitchWarning = false;
        }
    }

    @Inject(method = "cleanup", at = @At("TAIL"))
    private void vivecraft$cleanup(CallbackInfo ci) {
        ClientNetworking.resetServerSettings();
        ClientNetworking.DISPLAYED_CHAT_MESSAGE = false;
        ClientNetworking.DISPLAYED_CHAT_WARNING = false;
        ClientNetworking.NEEDS_RESET = true;
    }

    @Inject(method = "handlePlayerChat", at = @At("TAIL"))
    private void vivecraft$chatHapticsPlayer(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
        String lastMsg = ((PlayerExtension) this.minecraft.player).vivecraft$getLastMsg();
        ((PlayerExtension) this.minecraft.player).vivecraft$setLastMsg(null);
        if (VRState.VR_RUNNING && (this.minecraft.player == null || lastMsg == null ||
            packet.message().signedHeader().sender() == this.minecraft.player.getUUID()
        ))
        {
            vivecraft$triggerHapticSound();
        }
    }

    @Inject(method = "handleSystemChat", at = @At("TAIL"))
    private void vivecraft$chatHapticsSystem(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (this.minecraft.player != null) {
            String lastMsg = ((PlayerExtension) this.minecraft.player).vivecraft$getLastMsg();
            ((PlayerExtension) this.minecraft.player).vivecraft$setLastMsg(null);
            if (VRState.VR_RUNNING &&
                (this.minecraft.player == null || lastMsg == null || packet.content().getString().contains(lastMsg)))
            {
                vivecraft$triggerHapticSound();
            }
        }
    }

    @Unique
    private void vivecraft$triggerHapticSound() {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        if (dataHolder.vrSettings.chatNotifications != VRSettings.ChatNotifications.NONE) {
            if (!dataHolder.vrSettings.seated &&
                (dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.HAPTIC ||
                    dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH
                ))
            {
                dataHolder.vr.triggerHapticPulse(ControllerType.LEFT, 0.2F, 1000.0F, 1.0F);
            }

            if (dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND ||
                dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH)
            {
                Vec3 controllerPos = dataHolder.vrPlayer.vrdata_world_pre.getController(1).getPosition();
                this.minecraft.level.playLocalSound(
                    controllerPos.x(), controllerPos.y(), controllerPos.z(),
                    Registry.SOUND_EVENT.get(new ResourceLocation(dataHolder.vrSettings.chatNotificationSound)),
                    SoundSource.NEUTRAL, 0.3F, 0.1F, false);
            }
        }
    }

    @Inject(method = "handleOpenScreen", at = @At("HEAD"))
    private void vivecraft$markScreenActive(CallbackInfo ci) {
        GuiHandler.GUI_APPEAR_OVER_BLOCK_ACTIVE = true;
    }
}
