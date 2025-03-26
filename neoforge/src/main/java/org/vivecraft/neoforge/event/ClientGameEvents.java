package org.vivecraft.neoforge.event;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;
import org.vivecraft.client_vr.ReloadListener;
import org.vivecraft.neoforge.Vivecraft;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME, modid = Vivecraft.MODID)
public class ClientGameEvents {

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent registerClientCommandsEvent) {
        registerClientCommandsEvent.getDispatcher()
            .register(Commands.literal("vivecraft-client-config").executes(context -> {
                Minecraft mc = Minecraft.getInstance();
                mc.schedule(() -> mc.setScreen(new VivecraftMainSettings(mc.screen)));
                return 1;
            }));
    }
}
