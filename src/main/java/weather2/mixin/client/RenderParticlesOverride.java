package weather2.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import weather2.ClientTickHandler;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class RenderParticlesOverride {

    @Redirect(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"))
    public void render(ParticleEngine particleManager, PoseStack matrixStackIn, MultiBufferSource.BufferSource bufferIn, LightTexture lightTextureIn, Camera activeRenderInfoIn, float partialTicks, @Nullable net.minecraft.client.renderer.culling.Frustum clippingHelper) {
        //System.out.println("hooked render!");
        ClientTickHandler.particleManagerExtended().render(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, clippingHelper);
        particleManager.render(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, clippingHelper);

    }

    @Redirect(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    public void renderSnowAndRain(LevelRenderer worldRenderer, LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn) {
        //stopping vanilla from running renderRainSnow
    }
}