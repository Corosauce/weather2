package weather2;

import net.minecraft.server.level.ServerLevel;
import weather2.datatypes.StormState;
import weather2.ltcompat.ServerWeatherIntegration;

public class ServerWeatherProxy {

    public static float getWindSpeed(ServerLevel level) {
        if (isWeatherEffectsServerSideControlled()) {
            return ServerWeatherIntegration.getWindSpeed(level);
        } else {
            return -1;
        }
    }

    public static StormState getSandstormForEverywhere(ServerLevel level) {
        if (isWeatherEffectsServerSideControlled()) {
            return ServerWeatherIntegration.getSandstormForEverywhere(level);
        } else { return null; }
    }

    public static StormState getSnowstormForEverywhere(ServerLevel level) {
        if (isWeatherEffectsServerSideControlled()) {
            return ServerWeatherIntegration.getSnowstormForEverywhere(level);
        } else { return null; }
    }

    public static boolean isWeatherEffectsServerSideControlled() {
        return Weather.isLoveTropicsInstalled();
    }

}
