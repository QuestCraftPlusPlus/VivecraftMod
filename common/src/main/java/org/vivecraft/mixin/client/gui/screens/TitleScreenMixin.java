package org.vivecraft.mixin.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client.gui.screens.UpdateScreen;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin(Component component) {
        super(component);
    }

    //TODO Add config file
//    private final Properties vrConfig = new Properties();
//    private final Path vrConfigPath = Xplat.getConfigPath("vivecraft-config.properties");
    private boolean showError = false;
    private String getIcon() {
        return (showError ? "§c\u26A0§r " : "");
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PanoramaRenderer;render(FF)V"), method = "render")
    public void maybeNoPanorama(PanoramaRenderer instance, float f, float g){
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().menuWorldRenderer.isReady()){
            return;
        }
        instance.render(f, g);
    }
}
