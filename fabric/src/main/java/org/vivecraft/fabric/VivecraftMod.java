package org.vivecraft.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import org.vivecraft.client.gui.settings.VivecraftMainSettings;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.common.network.packet.c2s.VivecraftPayloadC2S;
import org.vivecraft.common.network.packet.s2c.VivecraftPayloadS2C;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerUtil;
import org.vivecraft.server.config.ServerConfig;

public class VivecraftMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // init server config
        ServerConfig.init(null);

        // add server config commands
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> ServerUtil.registerCommands(dispatcher, registryAccess));

        // register packets

        PayloadTypeRegistry.playS2C().register(VivecraftPayloadS2C.TYPE, VivecraftPayloadS2C.CODEC);
        PayloadTypeRegistry.playC2S().register(VivecraftPayloadC2S.TYPE, VivecraftPayloadC2S.CODEC);

        // use channel registers to be compatible with other mod loaders
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(VivecraftPayloadS2C.TYPE,
                (payload, context) -> ClientNetworking.handlePacket(payload));

            ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("vivecraft-client-config").executes(context -> {
                    Minecraft mc = context.getSource().getClient();
                    mc.schedule(() -> mc.setScreen(new VivecraftMainSettings(mc.screen)));
                    return 1;
                })));
        }

        ServerPlayNetworking.registerGlobalReceiver(VivecraftPayloadC2S.TYPE,
            (payload, context) -> ServerNetworking.handlePacket(payload, context.player(),
                context.responseSender()::sendPacket));
    }
}
