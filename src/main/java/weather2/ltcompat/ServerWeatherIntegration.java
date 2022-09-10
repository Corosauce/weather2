package weather2.ltcompat;

import com.lovetropics.minigames.common.core.game.weather.StormState;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import net.minecraft.server.level.ServerLevel;

public class ServerWeatherIntegration {

    public static float getWindSpeed(ServerLevel level) {
        WeatherController weatherController = WeatherControllerManager.forWorld(level);
        return weatherController.getWindSpeed();
    }

    public static StormState getSandstormForEverywhere(ServerLevel level) {
        WeatherController controller = WeatherControllerManager.forWorld(level);
        return controller != null ? controller.getSandstorm() : null;
    }

    public static StormState getSnowstormForEverywhere(ServerLevel level) {
        WeatherController controller = WeatherControllerManager.forWorld(level);
        return controller != null ? controller.getSnowstorm() : null;
    }

}
