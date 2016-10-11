package weather2.weathersystem.storm;

import weather2.weathersystem.WeatherManagerBase;

public class StormObjectSandstorm extends StormObject {

	public int height = 0;
	
	
	public StormObjectSandstorm(WeatherManagerBase parManager) {
		super(parManager);
	}
	
	public void initSandstorm() {
		height = 0;
		size = 0;
		
		maxSize = 300;
		maxHeight = 100;
	}
	
	@Override
	public void tick() {
		//super.tick();
		
		boolean testGrowth = true;
		
		if (testGrowth) {
			if (size < maxSize) {
				size++;
			}
			
			if (height < maxHeight) {
				height++;
			}
			
			
		}
		
	}
	
	@Override
	public void tickClient() {
		//super.tickClient();
	}

}
