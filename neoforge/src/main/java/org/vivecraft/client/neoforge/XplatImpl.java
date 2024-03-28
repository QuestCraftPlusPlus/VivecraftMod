package org.vivecraft.client.neoforge;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.vivecraft.client.Xplat;

import java.nio.file.Path;

public class XplatImpl implements Xplat {

    public static boolean isModLoaded(String name) {
        return LoadingModList.get().getModFileById(name) != null;
    }

    public static Path getConfigPath(String fileName) {
        return FMLPaths.CONFIGDIR.get().resolve(fileName);
    }

    public static boolean isDedicatedServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }

    public static String getModloader() {
        return "neoforge";
    }

    public static String getModVersion() {
        if (isModLoadedSuccess()) {
            return LoadingModList.get().getModFileById("vivecraft").versionString();
        }
        return "no version";
    }

    public static boolean isModLoadedSuccess() {
        return LoadingModList.get().getModFileById("vivecraft") != null;
    }

    public static boolean enableRenderTargetStencil(RenderTarget renderTarget) {
        renderTarget.enableStencil();
        return true;
    }

    public static Path getJarPath() {
        return LoadingModList.get().getModFileById("vivecraft").getFile().getSecureJar().getPath("/");
    }

    public static String getUseMethodName() {
        return "use";
    }

    public static TextureAtlasSprite[] getFluidTextures(BlockAndTintGetter level, BlockPos pos, FluidState fluidStateIn) {
        return FluidSpriteCache.getFluidSprites(level, pos, fluidStateIn);
    }

    public static Biome.ClimateSettings getBiomeClimateSettings(Biome biome) {
        return biome.getModifiedClimateSettings();
    }

    public static BiomeSpecialEffects getBiomeEffects(Biome biome) {
        return biome.getModifiedSpecialEffects();
    }

    public static double getItemEntityReach(double baseRange, ItemStack itemStack, EquipmentSlot slot) {
        var attributes = itemStack.getAttributeModifiers(slot).get(NeoForgeMod.ENTITY_REACH.value());
        for (var a : attributes) {
            if (a.getOperation() == AttributeModifier.Operation.ADDITION) {
                baseRange += a.getAmount();
            }
        }
        double totalRange = baseRange;
        for (var a : attributes) {
            if (a.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
                totalRange += baseRange * a.getAmount();
            }
        }
        for (var a : attributes) {
            if (a.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL) {
                totalRange *= 1.0 + a.getAmount();
            }
        }
        return totalRange;
    }

    public static void addNetworkChannel(ClientPacketListener listener, ResourceLocation resourceLocation) {
        // neoforge does that automatically, since we use their networking system
        // at least I have been told this
    }
}
