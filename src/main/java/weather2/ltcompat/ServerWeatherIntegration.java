package weather2.ltcompat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import weather2.datatypes.StormState;

public class ServerWeatherIntegration {

    public static float getWindSpeed(ServerLevel level) {
        return 0;
    }

    public static StormState getSandstormForEverywhere(ServerLevel level) {
        return null;
    }

    public static StormState getSnowstormForEverywhere(ServerLevel level) {
        return null;
    }

    /**
     * TODO: for LT, turn back on when LT is needed, activates dependency on LTWeather
     */
    /*public static float getWindSpeed(ServerLevel level) {
        return TypeBridge.getWindSpeed(level);
    }

    public static StormState getSandstormForEverywhere(ServerLevel level) {
        Tuple<Integer, Integer> data = TypeBridge.getSandstormData(level);
        return data != null ? new StormState(data.getA(), data.getB()) : null;
    }

    public static StormState getSnowstormForEverywhere(ServerLevel level) {
        Tuple<Integer, Integer> data = TypeBridge.getSnowstormData(level);
        return data != null ? new StormState(data.getA(), data.getB()) : null;
    }*/

}
