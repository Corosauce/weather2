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

    @Redirect(method = "updateCameraAndRender",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleManager;renderParticles(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/renderer/ActiveRenderInfo;FLnet/minecraft/client/renderer/culling/ClippingHelper;)V"))
    public void renderParticles(ParticleEngine particleManager, PoseStack matrixStackIn, MultiBufferSource.BufferSource bufferIn, LightTexture lightTextureIn, Camera activeRenderInfoIn, float partialTicks, @Nullable net.minecraft.client.renderer.culling.Frustum clippingHelper) {
        //System.out.println("hooked render!");
        ClientTickHandler.particleManagerExtended().renderParticles(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, clippingHelper);
        particleManager.render(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, clippingHelper);

    }

    @Redirect(method = "updateCameraAndRender",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/WorldRenderer;renderRainSnow(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    public void renderRainSnow(LevelRenderer worldRenderer, LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn) {
        //stopping vanilla from running renderRainSnow
    }
}