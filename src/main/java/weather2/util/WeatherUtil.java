package weather2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class WeatherUtil {
	
    public static boolean isPaused() {
    	if (Minecraft.getInstance().isPaused()) return true;
    	return false;
    }
    
    public static boolean isPausedSideSafe(Level world) {
    	//return false if server side because it cant be paused legit
    	if (!world.isClientSide) return false;
    	return isPausedForClient();
    }
    
    public static boolean isPausedForClient() {
    	if (Minecraft.getInstance().isPaused()) return true;
    	return false;
    }
    
    
}
