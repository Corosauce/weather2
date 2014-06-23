package weather2;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent.Save;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Weather2EventHandler {
	
	@ForgeSubscribe
	public void worldSave(Save event) {
		Weather.writeOutData(false);
	}
	
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
		ClientTickHandler.weatherManager.tickRender(event.partialTicks);
    }
	
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent event) {
		if (event.map.textureType == 1) {
			ClientProxy.radarIconRain = event.map.registerIcon(Weather.modID + ":radar/radarIconRain");
			ClientProxy.radarIconLightning = event.map.registerIcon(Weather.modID + ":radar/radarIconLightning");
			ClientProxy.radarIconWind = event.map.registerIcon(Weather.modID + ":radar/radarIconWind");
			ClientProxy.radarIconHail = event.map.registerIcon(Weather.modID + ":radar/radarIconHail");
			ClientProxy.radarIconTornado = event.map.registerIcon(Weather.modID + ":radar/radarIconTornado");
			ClientProxy.radarIconCyclone = event.map.registerIcon(Weather.modID + ":radar/radarIconCyclone");
		}
	}
}