package weather2.ltcompat;

import weather2.datatypes.PrecipitationType;
import weather2.datatypes.StormState;
import weather2.datatypes.WeatherEventType;

public class TypeConverter {

    public static PrecipitationType from(com.lovetropics.minigames.common.core.game.weather.PrecipitationType type) {
        return PrecipitationType.VALUES[type.ordinal()];
    }

    public static StormState from(com.lovetropics.minigames.common.core.game.weather.StormState state) {
        return new StormState(state.getBuildupTickRate(), state.getMaxStackable());
    }

    public static WeatherEventType from(com.lovetropics.minigames.common.core.game.weather.WeatherEventType type) {
        return WeatherEventType.VALUES[type.ordinal()];
    }

}
