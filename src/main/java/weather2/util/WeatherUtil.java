package weather2.util;

import java.util.HashMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public class WeatherUtil {
	
    public static boolean isPaused() {
    	if (Minecraft.getInstance().isGamePaused()) return true;
    	return false;
    }
    
    public static boolean isPausedSideSafe(World world) {
    	//return false if server side because it cant be paused legit
    	if (!world.isRemote) return false;
    	return isPausedForClient();
    }
    
    public static boolean isPausedForClient() {
    	if (Minecraft.getInstance().isGamePaused()) return true;
    	return false;
    }
    
    
}
