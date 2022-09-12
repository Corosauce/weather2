package com.lovetropics.weather;

import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;

public class TypeBridge {

    public static int getPrecipitationTypeOrdinal(ClientWeather weather) {
        return weather.getPrecipitationType().ordinal();
    }

    public static float getWindSpeed(ServerLevel level) {
        WeatherController weatherController = WeatherControllerManager.forWorld(level);
        return weatherController.getWindSpeed();
    }

    public static Tuple<Integer, Integer> getSandstormData(ServerLevel level) {
        WeatherController controller = WeatherControllerManager.forWorld(level);
        return controller != null ? new Tuple<>(controller.getSandstorm().buildupTickRate(), controller.getSandstorm().maxStackable()) : null;
    }

    public static Tuple<Integer, Integer> getSnowstormData(ServerLevel level) {
        WeatherController controller = WeatherControllerManager.forWorld(level);
        return controller != null ? new Tuple<>(controller.getSnowstorm().buildupTickRate(), controller.getSnowstorm().maxStackable()) : null;
    }

}
