package weather2;

import java.nio.FloatBuffer;
import java.util.List;

import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.render.FoliageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFogCoord;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import weather2.api.WeatherUtilData;
import weather2.client.SceneEnhancer;
import weather2.client.foliage.FoliageEnhancerShader;
import weather2.config.ConfigFoliage;
import weather2.config.ConfigMisc;
import weather2.entity.AI.EntityAIMoveIndoorsStorm;
import weather2.util.UtilEntityBuffsMini;
import weather2.weathersystem.storm.TornadoHelper;
import weather2.weathersystem.wind.WindManager;

public class EventHandlerForge {

	@SubscribeEvent
	public void worldSave(Save event) {
		Weather.writeOutData(false);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {

		if (ConfigMisc.Client_PotatoPC_Mode) return;

		ClientTickHandler.checkClientWeather();
		ClientTickHandler.weatherManager.tickRender(event.getPartialTicks());
		SceneEnhancer.renderWorldLast(event);

		FoliageRenderer.radialRange = ConfigFoliage.foliageShaderRange;
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent.Pre event) {
		
		//optifine breaks (removes) forge added method setTextureEntry, dont use it
		
		ClientProxy.radarIconRain = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radar_icon_rain"));
		ClientProxy.radarIconLightning = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radar_icon_lightning"));
		ClientProxy.radarIconWind = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radar_icon_wind"));
		ClientProxy.radarIconHail = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radar_icon_hail"));
		ClientProxy.radarIconTornado = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radar_icon_tornado"));
		ClientProxy.radarIconCyclone = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radar_icon_cyclone"));
		ClientProxy.radarIconSandstorm = event.getMap().registerSprite(new ResourceLocation(Weather.modID + ":radar/radar_icon_sandstorm"));
		
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void onFogDensity(FogDensity event) {

		if (ConfigMisc.Client_PotatoPC_Mode) return;

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

        boolean test2 = false;
        //test for underwater shaders that need LINEAR
        if (test2) {
			GlStateManager.setFogStart(0F);
			GlStateManager.setFogEnd(7F);
			GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
			event.setDensity(1F);
			event.setCanceled(true);
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

		if (ConfigMisc.Client_PotatoPC_Mode) return;
		
        if (SceneEnhancer.isFogOverridding()) {
			//backup original fog colors that are actively being adjusted based on time of day
			SceneEnhancer.stormFogRedOrig = event.getRed();
			SceneEnhancer.stormFogGreenOrig = event.getGreen();
			SceneEnhancer.stormFogBlueOrig = event.getBlue();
        	event.setRed(SceneEnhancer.stormFogRed);
        	event.setGreen(SceneEnhancer.stormFogGreen);
        	event.setBlue(SceneEnhancer.stormFogBlue);
			GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
        }
		
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onFogRender(RenderFogEvent event) {

		if (ConfigMisc.Client_PotatoPC_Mode) return;

		if (SceneEnhancer.isFogOverridding()) {
        	//event.setCanceled(true);
        	//event.setDensity(SceneEnhancer.stormFogDensity);

			//TODO: make use of this, density only works with EXP or EXP 2 mode
			GlStateManager.setFog(GlStateManager.FogMode.LINEAR);
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
			lightLevel = MathHelper.clamp(lightLevel, 0.0F, 1.0F);
			WorldBorder worldborder = mc.world.getWorldBorder();
			float f = (float)worldborder.getClosestDistance(mc.player);
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
			BufferBuilder vertexbuffer = tessellator.getBuffer();
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

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		SceneEnhancer.renderTick(event);
	}

	@SubscribeEvent
	public void onEntityCreatedOrLoaded(EntityJoinWorldEvent event) {
		if (event.getEntity().world.isRemote) return;

		if (ConfigMisc.Villager_MoveInsideForStorms) {
			if (event.getEntity() instanceof EntityVillager) {
				EntityVillager ent = (EntityVillager) event.getEntity();

				//Weather.dbg("applying villager storm AI");
				UtilEntityBuffsMini.replaceTaskIfMissing(ent, EntityAIMoveIndoors.class, EntityAIMoveIndoorsStorm.class, 2);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent.Post event) {
		FoliageEnhancerShader.setupReplacers();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void modelBake(ModelBakeEvent event) {
		FoliageEnhancerShader.modelBakeEvent(event);
	}

	@SubscribeEvent
	public void onBlockBreakTry(BlockEvent.BreakEvent event) {
		boolean testBreakCancel = false;
		if (testBreakCancel) {
			if (event.getPlayer().getName().equals(TornadoHelper.fakePlayerProfile.getName())) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event) {

		Entity ent = event.getEntity();
		if (!ent.world.isRemote) {
			if (WeatherUtilData.isWindAffected(ent)) {
				WindManager windMan = ServerTickHandler.getWeatherSystemForDim(ent.world.provider.getDimension()).windMan;
				windMan.applyWindForceNew(ent, 1F / 20F, 0.5F);
			}

			//test, clean example of spin entity around other entity
			/*if (ent instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) ent;
				List<EntityLivingBase> ents = ent.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(ent.getPosition()).grow(10, 10, 10));
				for (Entity entToSpin : ents) {
					//get the vector of x and z
					double vecX = player.posX - entToSpin.posX;
					double vecZ = player.posZ - entToSpin.posZ;
					//atan2 will give you the angle for yaw, for how minecraft works this will be 90 degrees off from an angle that will aim it directly at center entity
					float yawDegrees = (float)(Math.toDegrees(Math.atan2(vecZ, vecX)));
					//make the spin a bit tighter around the center entity, subtracting a full 90 degrees pulls it right towards you
					yawDegrees -= 10;
					float speed = 0.2F;
					entToSpin.motionX += -Math.sin(Math.toRadians(yawDegrees)) * speed;
					entToSpin.motionZ += Math.cos(Math.toRadians(yawDegrees)) * speed;
				}

			}*/
		}

	}
}
