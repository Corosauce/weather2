package weather2.weathersystem.wind;

import java.util.Random;

import weather2.config.ConfigMisc;
import weather2.weathersystem.WeatherManagerBase;

public class WindManager {

	//2 wind layers:
	//1: high level wind that clouds use
	//2: event wind:
	//2a: storm event, pulling wind into tornado
	//2b: wind gusts
	
	//particles use in priority order: storm event, if no event, gust, if no gust, global wind
	
	//global wind wont have gusts, but slowly changes intensity and angle
	
	//weather effect wind will have gusts and overrides from weather events
	
	public WeatherManagerBase manager;
	
	//global
	public float windAngleGlobal = 0;
	//public float windAngleGlobalSmooth = 0;
	public float windSpeed = 0;
	//public float windSpeedSmooth = 0;
	public float windSpeedChangeRate = 0.1F;
	public int windSpeedRandChangeTimer = 0;
	public int windSpeedRandChangeDelay = 10;
	
	public float windSpeedMin = 0F;
	public float windSpeedMax = 1F;
	
	//weather event effects
	public float windAngleEvent = 0;
	
	public WindManager(WeatherManagerBase parManager) {
		manager = parManager;
	}
	
	public void tick() {
		
		Random rand = new Random();
		
		//debug
		//windSpeedMin = 0.2F;
		//windAngleGlobal = 180;
		
		if (!ConfigMisc.windOn) {
			windSpeed = 0;
			//windSpeedSmooth = 0;
		} else {
			
			//WIND SPEED\\
			
			//global random wind speed change
			if (windSpeedRandChangeTimer-- <= 0)
            {
				windSpeed += (rand.nextDouble() * windSpeedChangeRate) - (windSpeedChangeRate / 2);
				windSpeedRandChangeTimer = windSpeedRandChangeDelay;
            }
			
			//enforce mins and maxs of wind speed
			if (windSpeed < windSpeedMin)
            {
				windSpeed = windSpeedMin;
            }

            if (windSpeed > windSpeedMax)
            {
            	windSpeed = windSpeedMax;
            }
			
            //smooth use
			/*if (windSpeed > windSpeedSmooth)
            {
				windSpeedSmooth += 0.01F;
            }
            else if (windSpeed < windSpeedSmooth)
            {
            	windSpeedSmooth -= 0.01F;
            }

            if (windSpeedSmooth < 0)
            {
            	windSpeedSmooth = 0F;
            }*/
            
            //WIND SPEED //
            
            //WIND ANGLE\\
			
			//global wind angle
			windAngleGlobal += ((new Random()).nextInt(5) - 2) * 0.5;
			
            if (windAngleGlobal < -180)
            {
            	windAngleGlobal += 360;
            }

            if (windAngleGlobal > 180)
            {
            	windAngleGlobal -= 360;
            }
            
            //WIND ANGLE //
		}
		
	}
}
