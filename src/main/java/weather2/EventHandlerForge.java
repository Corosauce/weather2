package weather2;

import weather2.client.SceneEnhancer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry.TextureAtlasSpriteImpl;

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
}
