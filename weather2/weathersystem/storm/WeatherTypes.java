package weather2.weathersystem.storm;

import java.util.ArrayList;
import java.util.List;

public class WeatherTypes {

	public static List<WeatherEntityConfig> weatherEntTypes;
	
	static {
		initWeatherTypes();
	}
	
	public static void initWeatherTypes() {
    	weatherEntTypes = new ArrayList();
        WeatherEntityConfig sConf = new WeatherEntityConfig();
        //0 = spout
        //1 = F1
        //2 = F3
        //3 = F5
        //4 = F6
        //5 = Hurricane C1
        //water spout
        sConf.tornadoInitialSpeed = 0.2F;
        sConf.tornadoPullRate = 0.04F;
        sConf.tornadoLiftRate = 0.05F;
        sConf.relTornadoSize = 0;
        sConf.tornadoBaseSize = 3;
        sConf.tornadoWidthScale = 1.0F;
        sConf.grabDist = 40D;
        sConf.tornadoTime = 4500;
        sConf.type = 0;
        sConf.grabsBlocks = false;
        weatherEntTypes.add(sConf);
        //F1 tornado
        sConf = new WeatherEntityConfig();
        sConf.tornadoInitialSpeed = 0.2F;
        sConf.tornadoPullRate = 0.04F;
        sConf.tornadoLiftRate = 0.05F;
        sConf.relTornadoSize = -20;
        sConf.tornadoBaseSize = 3;
        sConf.tornadoWidthScale = 1.5F;
        sConf.grabDist = 100D;
        weatherEntTypes.add(sConf);
        //F3 tornado
        sConf = new WeatherEntityConfig();
        sConf.tornadoInitialSpeed = 0.2F;
        sConf.tornadoPullRate = 0.04F;
        sConf.tornadoLiftRate = 0.05F;
        sConf.relTornadoSize = -50;
        sConf.tornadoBaseSize = 10;
        sConf.tornadoWidthScale = 1.9F;
        weatherEntTypes.add(sConf);
        //F5 tornado
        sConf = new WeatherEntityConfig();
        sConf.tornadoInitialSpeed = 0.15F;
        sConf.tornadoPullRate = 0.04F;
        sConf.tornadoLiftRate = 0.04F;
        sConf.relTornadoSize = 95;
        sConf.tornadoBaseSize = 25;
        sConf.tornadoWidthScale = 2.5F;
        weatherEntTypes.add(sConf);
        //F6
        sConf = new WeatherEntityConfig();
        sConf.tornadoInitialSpeed = 0.15F;
        sConf.tornadoPullRate = 0.15F;
        sConf.tornadoLiftRate = 0.04F;
        sConf.relTornadoSize = 95;
        sConf.tornadoBaseSize = 95;
        sConf.tornadoWidthScale = 3.5F;
        weatherEntTypes.add(sConf);
        //Hurricane
        sConf = new WeatherEntityConfig();
        sConf.tornadoInitialSpeed = 0.15F;
        sConf.tornadoPullRate = 0.15F;
        sConf.tornadoLiftRate = 0.04F;
        sConf.relTornadoSize = 95;
        sConf.tornadoBaseSize = 155;
        sConf.tornadoWidthScale = 3.5F;
        sConf.tornadoTime = 4500;
        sConf.type = 2;
        weatherEntTypes.add(sConf);
	}
}
