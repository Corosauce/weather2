package weather2.api;

import java.util.List;

import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.Vec3;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.ClientTickHandler;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.EnumWeatherObjectType;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.wind.WindManager;

public class WeatherInterface {
	
	private String modName;
	private int dimension;
	
	public WeatherInterface(int dim, String modName){
		this.dimension = dim;
		this.modName = modName;
	}
	
    public boolean isPrecipitating(int worldID, Vec3 pos){
    	
    	WeatherManagerBase wm;
    	
    	if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT){
    		wm = ClientTickHandler.weatherManager;
    	} else {
    		wm = ServerTickHandler.getWeatherSystemForDim(worldID);
    	}
    	
    	return wm.isPrecipitatingAt(pos);
    }
    
    public List<WeatherObject> getWeatherObjects(){
    	return ServerTickHandler.getWeatherSystemForDim(dimension).getStormObjects();
    }
    
    public WindManager getWindManager(){
    	return ServerTickHandler.getWeatherSystemForDim(dimension).windMan;
    }
    
    public void createStorm(Vec3 pos, int Stage, boolean rain, int StormType){
    	StormObject so = new StormObject(ServerTickHandler.getWeatherSystemForDim(dimension));
		so.layer = 0;
		so.naturallySpawned = true;
		so.levelTemperature = 0.1F;
		so.pos = pos;

		so.levelWater = so.levelWaterStartRaining * 2;
		so.attrib_precipitation = true;
		
		if (!rain) {
			so.initRealStorm(null, null);
		}
		
		so.levelCurIntensityStage = Stage;
		so.stormType = StormType;
    }
    
    public void	setEnvironmentTemperature(float temp){
    	EnvVars.environmentTemperature = temp;
    }
    public void setEnvironmentHumidity(float humid){
    	EnvVars.environmentHumidity = humid;
    }
}
