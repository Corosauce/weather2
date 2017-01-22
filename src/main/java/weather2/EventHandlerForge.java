package weather2;

import java.nio.FloatBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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
		ClientProxy.radarIconSandstorm = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radarIconSandstorm"));
		
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
        	event.setDensity(0F);
        }*/
        
        //event.setCanceled(true);
        //event.setDensity(0.0F);
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void onFogColors(FogColors event) {
		
        if (SceneEnhancer.isFogOverridding()) {
			//backup original fog colors that are actively being adjusted based on time of day
			SceneEnhancer.stormFogRedOrig = event.getRed();
			SceneEnhancer.stormFogGreenOrig = event.getGreen();
			SceneEnhancer.stormFogBlueOrig = event.getBlue();
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

			//TODO: make use of this, density only works with EXP or EXP 2 mode
        	/*GlStateManager.setFog(GlStateManager.FogMode.EXP2);
			GlStateManager.setFogDensity(SceneEnhancer.stormFogDensity);*/
			
			if (event.getFogMode() == -1) {
				GlStateManager.setFogStart(SceneEnhancer.stormFogStartClouds);
	            GlStateManager.setFogEnd(SceneEnhancer.stormFogEndClouds);
			} else {
				GlStateManager.setFogStart(SceneEnhancer.stormFogStart);
	            GlStateManager.setFogEnd(SceneEnhancer.stormFogEnd);
			}

			//GlStateManager.setFogDensity(0.01F);
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onScreenEvent(RenderGameOverlayEvent.Pre event) {
		if (false && event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
			Minecraft mc = Minecraft.getMinecraft();
			//System.out.println(event.getType());
			float lightLevel = 0.4F/* - lightLevel*/;
			lightLevel = MathHelper.clamp_float(lightLevel, 0.0F, 1.0F);
			WorldBorder worldborder = mc.theWorld.getWorldBorder();
			float f = (float)worldborder.getClosestDistance(mc.thePlayer);
			double d0 = Math.min(worldborder.getResizeSpeed() * (double)worldborder.getWarningTime() * 1000.0D, Math.abs(worldborder.getTargetSize() - worldborder.getDiameter()));
			double d1 = Math.max((double)worldborder.getWarningDistance(), d0);

			if ((double)f < d1)
			{
				f = 1.0F - (float)((double)f / d1);
			}
			else
			{
				f = 0.0F;
			}

			float prevVignetteBrightness = lightLevel;//(float)((double)this.prevVignetteBrightness + (double)(lightLevel - this.prevVignetteBrightness) * 0.01D);
			GlStateManager.disableDepth();
			GlStateManager.depthMask(false);
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

			if (f > 0.0F)
			{
				GlStateManager.color(0.0F, f, f, 1.0F);
			}
			else
			{
				GlStateManager.color(prevVignetteBrightness, prevVignetteBrightness, prevVignetteBrightness, 1.0F);
			}

			ScaledResolution scaledRes = new ScaledResolution(mc);

			//added
			GlStateManager.enableBlend();

			//mc.getTextureManager().bindTexture(new ResourceLocation("textures/misc/vignette.png"));
			mc.getTextureManager().bindTexture(new ResourceLocation("weather2:textures/gui/vignette.png"));
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer vertexbuffer = tessellator.getBuffer();
			vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
			vertexbuffer.pos(0.0D, (double)scaledRes.getScaledHeight() * scaledRes.getScaleFactor() * 1D, -90.0D).tex(0.0D, 1.0D).endVertex();
			vertexbuffer.pos((double)scaledRes.getScaledWidth() * scaledRes.getScaleFactor(), (double)scaledRes.getScaledHeight() * scaledRes.getScaleFactor(), -90.0D).tex(1.0D, 1.0D).endVertex();
			vertexbuffer.pos((double)scaledRes.getScaledWidth() * scaledRes.getScaleFactor(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
			vertexbuffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
			tessellator.draw();
			GlStateManager.depthMask(true);
			GlStateManager.enableDepth();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		}

	}
}
