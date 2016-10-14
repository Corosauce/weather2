package weather2.weathersystem.storm;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumStormType {
	
	CLOUD, SAND;
	
	private static final Map<Integer, EnumStormType> lookup = new HashMap<Integer, EnumStormType>();
    static { for(EnumStormType e : EnumSet.allOf(EnumStormType.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumStormType get(int intValue) { return lookup.get(intValue); }
}
