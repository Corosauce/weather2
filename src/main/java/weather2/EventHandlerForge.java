package weather2;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent.Pre event) {
		
		ResourceLocation res;
		
		res = new ResourceLocation(Weather.modID + ":radar/radarIconRain");
		event.getMap().setTextureEntry(res.toString(), ClientProxy.radarIconRain = new TextureAtlasSpriteImpl(res.toString()));
		
		res = new ResourceLocation(Weather.modID + ":radar/radarIconLightning");
		event.getMap().setTextureEntry(res.toString(), ClientProxy.radarIconLightning = new TextureAtlasSpriteImpl(res.toString()));
		
		res = new ResourceLocation(Weather.modID + ":radar/radarIconWind");
		event.getMap().setTextureEntry(res.toString(), ClientProxy.radarIconWind = new TextureAtlasSpriteImpl(res.toString()));
		
		res = new ResourceLocation(Weather.modID + ":radar/radarIconHail");
		event.getMap().setTextureEntry(res.toString(), ClientProxy.radarIconHail = new TextureAtlasSpriteImpl(res.toString()));
		
		res = new ResourceLocation(Weather.modID + ":radar/radarIconTornado");
		event.getMap().setTextureEntry(res.toString(), ClientProxy.radarIconTornado = new TextureAtlasSpriteImpl(res.toString()));
		
		res = new ResourceLocation(Weather.modID + ":radar/radarIconCyclone");
		event.getMap().setTextureEntry(res.toString(), ClientProxy.radarIconCyclone = new TextureAtlasSpriteImpl(res.toString()));
		
		/*if (event.map.getTextureType() == 1) {
			ClientProxy.radarIconRain = event.map.registerIcon(Weather.modID + ":radar/radarIconRain");
			ClientProxy.radarIconLightning = event.map.registerIcon(Weather.modID + ":radar/radarIconLightning");
			ClientProxy.radarIconWind = event.map.registerIcon(Weather.modID + ":radar/radarIconWind");
			ClientProxy.radarIconHail = event.map.registerIcon(Weather.modID + ":radar/radarIconHail");
			ClientProxy.radarIconTornado = event.map.registerIcon(Weather.modID + ":radar/radarIconTornado");
			ClientProxy.radarIconCyclone = event.map.registerIcon(Weather.modID + ":radar/radarIconCyclone");
		}*/
	}
}
