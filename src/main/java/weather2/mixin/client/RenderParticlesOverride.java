package weather2.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import weather2.ClientTickHandler;
import weather2.config.ConfigParticle;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class RenderParticlesOverride {

    //replaced by RenderLevelStageEvent.Stage.AFTER_PARTICLES
    /*@Redirect(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"))
    public void render(ParticleEngine particleManager, PoseStack matrixStackIn, MultiBufferSource.BufferSource bufferIn, LightTexture lightTextureIn, Camera activeRenderInfoIn, float partialTicks, @Nullable net.minecraft.client.renderer.culling.Frustum clippingHelper) {
        ClientTickHandler.particleManagerExtended().render(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, clippingHelper);
        particleManager.render(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, clippingHelper);

    }*/

    @Redirect(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    public void renderSnowAndRain(LevelRenderer worldRenderer, LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn) {
        //CULog.dbg("renderSnowAndRain hook");
        //stopping vanilla from running renderRainSnow
        if (ConfigParticle.Particle_vanilla_precipitation) {
            worldRenderer.renderSnowAndRain(lightmapIn, partialTicks, xIn, yIn, zIn);
        }
    }

    /*@Redirect(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderClouds(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/math/Matrix4f;FDDD)V"))
    public void renderClouds(LevelRenderer instance, PoseStack poseStack, Matrix4f l, float i1, double f1, double f2, double d0) {
        //CULog.dbg("renderClouds hook");
        //workaround for missing projection matrix info
        ICloudRenderHandler cloudRenderHandler = Minecraft.getInstance().level().effects().getCloudRenderHandler();
        if (cloudRenderHandler instanceof CloudRenderHandler) {
            ((CloudRenderHandler)cloudRenderHandler).render(poseStack, l, i1, f1, f2, d0);
        } else {
            instance.renderClouds(poseStack, l, i1, f1, f2, d0);
        }
    }*/
}