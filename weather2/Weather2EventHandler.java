package weather2;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent.Save;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class Weather2EventHandler {
	
	@ForgeSubscribe
	public void worldSave(Save event) {
		Weather.writeOutData(false);
	}
	
}