package weather2.ltcompat;

import com.lovetropics.weather.TypeBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import weather2.datatypes.StormState;

public class ServerWeatherIntegration {

    public static float getWindSpeed(ServerLevel level) {
        return TypeBridge.getWindSpeed(level);
    }

    public static StormState getSandstormForEverywhere(ServerLevel level) {
        Tuple<Integer, Integer> data = TypeBridge.getSandstormData(level);
        return new StormState(data.getA(), data.getB());
    }

    public static StormState getSnowstormForEverywhere(ServerLevel level) {
        Tuple<Integer, Integer> data = TypeBridge.getSnowstormData(level);
        return new StormState(data.getA(), data.getB());
    }

}
