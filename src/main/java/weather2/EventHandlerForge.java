package weather2;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import weather2.api.WeatherUtilData;
import weather2.client.SceneEnhancer;
import weather2.config.ConfigFoliage;
import weather2.config.ConfigMisc;
import weather2.weathersystem.storm.TornadoHelper;
import weather2.weathersystem.wind.WindManager;

import java.nio.FloatBuffer;

@Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandlerForge {

	@SubscribeEvent
	public void worldSave(Save event) {
		Weather.writeOutData(false);
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {

		if (ConfigMisc.Client_PotatoPC_Mode) return;

		ClientTickHandler.checkClientWeather();
		ClientTickHandler.weatherManager.tickRender(event.getPartialTicks());
		SceneEnhancer.renderWorldLast(event);

		//TODO: 1.14 uncomment
		/*FoliageRenderer.radialRange = ConfigFoliage.foliageShaderRange;*/
    }
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerIcons(TextureStitchEvent.Pre event) {

		//TODO: 1.14 relocate to its own mod hooks
		//extendedrenderer.particle.ParticleRegistry.init(event);

		if (!event.getMap().getBasePath().equals("textures")) {
			return;
		}
		
		event.addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_rain"));
		event.addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_lightning"));
		event.addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_wind"));
		event.addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_hail"));
		event.addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_tornado"));
		event.addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_cyclone"));
		event.addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_sandstorm"));

		/*ClientProxy.radarIconRain = event.addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_rain"));
		ClientProxy.radarIconLightning = addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_lightning"));
		ClientProxy.radarIconWind = addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_wind"));
		ClientProxy.radarIconHail = addSprite(new ResourceLocation(Weather.MODID + ":radar/radar_icon_hail"));
		ClientProxy.radarIconTornado = addSprite(event, new ResourceLocation(Weather.MODID + ":radar/radar_icon_tornado"));
		ClientProxy.radarIconCyclone = addSprite(event, new ResourceLocation(Weather.MODID + ":radar/radar_icon_cyclone"));
		ClientProxy.radarIconSandstorm = addSprite(event, new ResourceLocation(Weather.MODID + ":radar/radar_icon_sandstorm"));*/
		
	}

	//TODO: 1.14 get sprites with post after pre registers them
	/*@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public static void registerIcons(TextureStitchEvent.Post event) {
		extendedrenderer.particle.ParticleRegistry.init(event);
	}*/

	/*public static TextureAtlasSprite addSprite(TextureStitchEvent.Pre event, ResourceLocation resourceLocation) {
		event.addSprite(resourceLocation);
		return event.getMap().getSprite(resourceLocation);
	}*/
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
    public void onFogDensity(FogDensity event) {

		if (ConfigMisc.Client_PotatoPC_Mode) return;

		float fogDensity = 0;
		int delay = 5000;
		long time = System.currentTimeMillis() % delay;
		fogDensity = (float)time / (float)delay;
		boolean test = false;
        if (test) {
            event.setCanceled(true);
            
            GlStateManager.fogMode(GlStateManager.FogMode.LINEAR);
            GlStateManager.fogStart(0F);
            GlStateManager.fogEnd(400F);
            
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
			GlStateManager.fogStart(0F);
			GlStateManager.fogEnd(7F);
			GlStateManager.fogMode(GlStateManager.FogMode.LINEAR);
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
	@OnlyIn(Dist.CLIENT)
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
			GlStateManager.fogMode(GlStateManager.FogMode.LINEAR);
        }
		
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onFogRender(RenderFogEvent event) {

		if (ConfigMisc.Client_PotatoPC_Mode) return;

		if (SceneEnhancer.isFogOverridding()) {
        	//event.setCanceled(true);
        	//event.setDensity(SceneEnhancer.stormFogDensity);

			//TODO: make use of this, density only works with EXP or EXP 2 mode
			GlStateManager.fogMode(GlStateManager.FogMode.LINEAR);
        	/*GlStateManager.setFog(GlStateManager.FogMode.EXP2);
			GlStateManager.setFogDensity(SceneEnhancer.stormFogDensity);*/
			
			if (event.getFogMode() == -1) {
				GlStateManager.fogStart(SceneEnhancer.stormFogStartClouds);
	            GlStateManager.fogEnd(SceneEnhancer.stormFogEndClouds);
			} else {
				GlStateManager.fogStart(SceneEnhancer.stormFogStart);
	            GlStateManager.fogEnd(SceneEnhancer.stormFogEnd);
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
	@OnlyIn(Dist.CLIENT)
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		SceneEnhancer.renderTick(event);
	}

	@SubscribeEvent
	public void onEntityCreatedOrLoaded(EntityJoinWorldEvent event) {
		if (event.getEntity().world.isRemote) return;

		if (ConfigMisc.Villager_MoveInsideForStorms) {
			if (event.getEntity() instanceof VillagerEntity) {
				VillagerEntity ent = (VillagerEntity) event.getEntity();

				//Weather.dbg("applying villager storm AI");
				//TODO: 1.14 redesign
				//UtilEntityBuffsMini.replaceTaskIfMissing(ent, EntityAIMoveIndoors.class, EntityAIMoveIndoorsStorm.class, 2);
			}
		}
	}

	//TODO: 1.14 uncomment
	/*@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void registerIcons(TextureStitchEvent.Post event) {
		FoliageEnhancerShader.setupReplacers();
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void modelBake(ModelBakeEvent event) {
		FoliageEnhancerShader.modelBakeEvent(event);
	}*/

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
				WindManager windMan = ServerTickHandler.getWeatherSystemForDim(ent.world.getDimension().getType().getId()).windMan;
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
