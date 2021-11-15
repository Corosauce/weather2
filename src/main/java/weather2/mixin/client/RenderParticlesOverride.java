package weather2.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import weather2.ClientTickHandler;

import javax.annotation.Nullable;

@Mixin(WorldRenderer.class)
public abstract class RenderParticlesOverride {

    @Redirect(method = "updateCameraAndRender",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleManager;renderParticles(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer$Impl;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/renderer/ActiveRenderInfo;FLnet/minecraft/client/renderer/culling/ClippingHelper;)V"))
    public void renderParticles(ParticleManager particleManager, MatrixStack matrixStackIn, IRenderTypeBuffer.Impl bufferIn, LightTexture lightTextureIn, ActiveRenderInfo activeRenderInfoIn, float partialTicks, @Nullable net.minecraft.client.renderer.culling.ClippingHelper clippingHelper) {
        //System.out.println("hooked render!");
        ClientTickHandler.particleManagerExtended().renderParticles(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, clippingHelper);
        particleManager.renderParticles(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, clippingHelper);

    }

    @Redirect(method = "updateCameraAndRender",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/WorldRenderer;renderRainSnow(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    public void renderRainSnow(WorldRenderer worldRenderer, LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn) {
        //stopping vanilla from running renderRainSnow
    }
}