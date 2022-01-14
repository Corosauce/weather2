package weather2.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.Calendar;

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

    public static boolean isAprilFoolsDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        //test
        //return calendar.get(Calendar.MONTH) == Calendar.MARCH && calendar.get(Calendar.DAY_OF_MONTH) == 25;

        return calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }
    
    
}
