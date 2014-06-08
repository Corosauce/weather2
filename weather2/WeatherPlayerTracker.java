package weather2;

import net.minecraft.entity.player.EntityPlayer;
import zombiecraft.Core.GameLogic.ZCGame;
import cpw.mods.fml.common.IPlayerTracker;

public class WeatherPlayerTracker implements IPlayerTracker {

	@Override
	public void onPlayerLogin(EntityPlayer player) {

		ServerTickHandler.playerJoinedServerSyncFull(player);
		
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {

	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		
	}

}
