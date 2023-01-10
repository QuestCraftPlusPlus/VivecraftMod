package org.vivecraft.gui.settings;

import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.gui.framework.VROptionEntry;
import org.vivecraft.settings.VRSettings;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiVRControls extends GuiVROptionsBase
{
    private static VROptionEntry[] controlsSettings = new VROptionEntry[] {
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.DUMMY, true),
            new VROptionEntry(VRSettings.VrOptions.THIRDPERSON_ITEMTRANSFORMS)
    };

    public GuiVRControls(Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.controls";
        super.init(controlsSettings, true);
        super.addDefaultButtons();
    }
}
