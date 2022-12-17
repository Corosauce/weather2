package weather2.weathersystem.sky;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.client.IWeatherParticleRenderHandler;

/**
 * Dummy class, we override what this would do elsewhere already, just stop vanillas code so we can tweak it
 */
public class WeatherParticleRenderHandler implements IWeatherParticleRenderHandler {

    @Override
    public void render(int ticks, ClientLevel level, Minecraft minecraft, Camera camera) {

    }
}
