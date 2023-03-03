package weather2.mixin.client;

import com.corosus.coroutil.util.CULog;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.client.ICloudRenderHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import weather2.ClientTickHandler;
import weather2.weathersystem.sky.CloudRenderHandler;

import javax.annotation.Nullable;

@Mixin(GameRenderer.class)
public abstract class GameRendererOverride {

    /**
     * @author Corosus
     * @reason render particle clouds further
     */
    @Overwrite
    public float getDepthFar() {
        //CULog.dbg("getDepthFar override");
        return Minecraft.getInstance().gameRenderer.getRenderDistance() * 32.0F;
    }
}