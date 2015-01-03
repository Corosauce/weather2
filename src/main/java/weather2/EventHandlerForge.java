package weather2;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import CoroUtil.quest.PlayerQuestManager;

public class EventHandlerForge {

	@SubscribeEvent
	public void worldSave(Save event) {
		Weather.writeOutData(false);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
		ClientTickHandler.weatherManager.tickRender(event.partialTicks);
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent event) {
		if (event.map.getTextureType() == 1) {
			ClientProxy.radarIconRain = event.map.registerIcon(Weather.modID + ":radar/radarIconRain");
			ClientProxy.radarIconLightning = event.map.registerIcon(Weather.modID + ":radar/radarIconLightning");
			ClientProxy.radarIconWind = event.map.registerIcon(Weather.modID + ":radar/radarIconWind");
			ClientProxy.radarIconHail = event.map.registerIcon(Weather.modID + ":radar/radarIconHail");
			ClientProxy.radarIconTornado = event.map.registerIcon(Weather.modID + ":radar/radarIconTornado");
			ClientProxy.radarIconCyclone = event.map.registerIcon(Weather.modID + ":radar/radarIconCyclone");
		}
	}
}
