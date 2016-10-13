package weather2;

import java.nio.FloatBuffer;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFogCoord;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import weather2.client.SceneEnhancer;

public class EventHandlerForge {

	@SubscribeEvent
	public void worldSave(Save event) {
		Weather.writeOutData(false);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
		ClientTickHandler.checkClientWeather();
		ClientTickHandler.weatherManager.tickRender(event.getPartialTicks());
		SceneEnhancer.renderWorldLast(event);
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent.Pre event) {
		
		//optifine breaks (removes) forge added method setTextureEntry, dont use it
		
		ClientProxy.radarIconRain = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radarIconRain"));
		ClientProxy.radarIconLightning = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radarIconLightning"));
		ClientProxy.radarIconWind = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radarIconWind"));
		ClientProxy.radarIconHail = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radarIconHail"));
		ClientProxy.radarIconTornado = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radarIconTornado"));
		ClientProxy.radarIconCyclone = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radarIconCyclone"));
		
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void onFogDensity(FogDensity event) {
		float fogDensity = 0;
		int delay = 5000;
		long time = System.currentTimeMillis() % delay;
		fogDensity = (float)time / (float)delay;
		boolean test = false;
        if (test) {
            event.setCanceled(true);
            
            GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
            GlStateManager.setFogStart(0F);
            GlStateManager.setFogEnd(400F);
            
            //GlStateManager.glFog(2918, this.setFogColorBuffer(0.7F, 0.6F, 0.3F, 1.0F));
            
            /*GlStateManager.glFog(2918, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
            GlStateManager.glNormal3f(0.0F, -1.0F, 0.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);*/
            
            event.setDensity(fogDensity);
            event.setDensity(0.5F);
        }
        
        /*if (SceneEnhancer.isFogOverridding()) {
        	event.setCanceled(true);
        	event.setDensity(SceneEnhancer.stormFogDensity);
        }*/
        
        //event.setCanceled(true);
        //event.setDensity(0.0F);
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void onFogColors(FogColors event) {
		
        if (SceneEnhancer.isFogOverridding()) {
        	event.setRed(SceneEnhancer.stormFogRed);
        	event.setGreen(SceneEnhancer.stormFogGreen);
        	event.setBlue(SceneEnhancer.stormFogBlue);
        }
		
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onFogRender(RenderFogEvent event) {
		if (SceneEnhancer.isFogOverridding()) {
        	//event.setCanceled(true);
        	//event.setDensity(SceneEnhancer.stormFogDensity);
        	
        	//GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
			
			if (event.getFogMode() == -1) {
				GlStateManager.setFogStart(SceneEnhancer.stormFogStartClouds);
	            GlStateManager.setFogEnd(SceneEnhancer.stormFogEndClouds);
			} else {
				GlStateManager.setFogStart(SceneEnhancer.stormFogStart);
	            GlStateManager.setFogEnd(SceneEnhancer.stormFogEnd);
			}
            
            
            /*GlStateManager.setFogStart(0);
            GlStateManager.setFogEnd(192);*/
            
            /*if (GLContext.getCapabilities().GL_NV_fog_distance)
            {
                GlStateManager.glFogi(34138, 34139);
            }*/
        }
	}
	
	private FloatBuffer setFogColorBuffer(float red, float green, float blue, float alpha)
    {
		FloatBuffer buff = GLAllocation.createDirectFloatBuffer(16);
		buff.clear();
		buff.put(red).put(green).put(blue).put(alpha);
		buff.flip();
        return buff;
    }
}
