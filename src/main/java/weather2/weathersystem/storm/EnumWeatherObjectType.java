package weather2.weathersystem.storm;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumWeatherObjectType {
	
	CLOUD, SAND;
	
	private static final Map<Integer, EnumWeatherObjectType> lookup = new HashMap<Integer, EnumWeatherObjectType>();
    static { for(EnumWeatherObjectType e : EnumSet.allOf(EnumWeatherObjectType.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumWeatherObjectType get(int intValue) { return lookup.get(intValue); }
}
