package weather2.weathersystem.fog;

import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.player.Player;
import com.mojang.math.Vector3f;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import weather2.ClientTickHandler;
import weather2.ClientWeather;
import weather2.client.SceneEnhancer;
import weather2.util.WeatherUtilEntity;

import java.util.Random;

public class FogAdjuster {

    private FogProfile fogHeatwave;
    private FogProfile fogSandstorm;
    private FogProfile fogSnowstorm;

    //initial values arent really used for this one, just used to store dynamically updated values for smooth transitions
    private FogProfile fogVanilla;

    private FogProfile targetProfile;
    private FogProfile activeProfile;
    private FogProfile activeProfileLerps;

    private int lerpTicksCur = 0;
    private int lerpTicksMax = 100;

    //reinit fog values when changes
    private boolean useFarFog = false;

    public static WeatherEventType lastWeatherType = null;

    public int randDelay = 0;

    /**
     *
     * new fog adjust way:
     * when theres a new request to change the state
     * - set old state to prev state
     * - for each thing we have to fade (each color, density)
     * -- calculate a lerp rate so they all take the same amount of time?
     * - actually, how do we want to decide on how long itll take? we might want that dynamic
     * - maybe average it based on the distance between each color, so like, white to black and far dist fog change = long
     * - but white to grey with not much fog dist change = short
     *
     * - we could let color and dist change at diff rates
     * - important the rgbs transition the same, for obvious reasons
     *
     *  important note: activeProfile will now be updated with current actual vals
     *  - so when we get interrupted, we actually have last state we were at
     *
     *  - never change active profile, just set a new target and rates
     *
     *  - monitor vanilla color/fog changes and i guess push a new target? should be fine, will be slow jank with my static 100 tick update for now
     */


    public FogAdjuster() {
        initProfiles(false);
        activeProfile = fogVanilla;
        targetProfile = fogVanilla;
        activeProfileLerps = new FogProfile(new Vector3f(0F, 0F, 0F), 0, 0);
    }

    public void initProfiles(boolean spectator) {

        float distAmp = 1F;
        if (spectator) {
            distAmp = 4F;
        }
        fogHeatwave = new FogProfile(new Vector3f(0.5F, 0.2F, 0.1F), 0, 75);
        fogSandstorm = new FogProfile(new Vector3f(0.7F, 0.5F, 0.2F), 0, 18 * distAmp);
        fogSnowstorm = new FogProfile(new Vector3f(0.7F, 0.7F, 0.7F), 0, 20 * distAmp);
        fogVanilla = new FogProfile(new Vector3f(-1F, -1F, -1F), -1, -1);
    }

    public void tickGame(ClientWeather weather) {
        updateWeatherState();

        if (lastWeatherType != null) {
            if (randDelay <= 0) {
                Random rand = new Random();
                randDelay = 20 + rand.nextInt(5);
                startRandom();
            }
        }

        randDelay--;

        //System.out.println("lerpAmount: " + lerpAmount);
        //System.out.println("isFogOverriding(): " + isFogOverriding());

        if ((SceneEnhancer.getWeatherState() == WeatherEventType.SANDSTORM || SceneEnhancer.getWeatherState() == WeatherEventType.SNOWSTORM)) {
            Player player = Minecraft.getInstance().player;
            //use non cached version of isPlayerOutside to fix data mismatch that is timing crucial here
            boolean isPlayerOutside = WeatherUtilEntity.isEntityOutside(player);
            boolean playerOutside = isPlayerOutside || player.isInWater();
            boolean setFogFar = !playerOutside || player.isSpectator();
            /*System.out.println("set to far mode?: " + setFogFar);
            System.out.println("playerOutside: " + SceneEnhancer.isPlayerOutside);
            System.out.println("isInWater: " + player.isInWater());
            System.out.println("setFogFar: " + setFogFar);*/
            if (player != null) {
                if ((setFogFar && !useFarFog) || !setFogFar && useFarFog) {
                    initProfiles(setFogFar);
                    if (SceneEnhancer.getWeatherState() == WeatherEventType.SANDSTORM) {
                        startSandstorm();
                    } else if (SceneEnhancer.getWeatherState() == WeatherEventType.SNOWSTORM) {
                        startSnowstorm();
                    }
                }
                useFarFog = setFogFar;
            }
        }

        if (lerpTicksCur < lerpTicksMax) {

            float newLerpX = activeProfile.getRgb().x() + activeProfileLerps.getRgb().x();
            float newLerpY = activeProfile.getRgb().y() + activeProfileLerps.getRgb().y();
            float newLerpZ = activeProfile.getRgb().z() + activeProfileLerps.getRgb().z();
            activeProfile.getRgb().set(newLerpX, newLerpY, newLerpZ);

            activeProfile.setFogStart(activeProfile.getFogStart() + activeProfileLerps.getFogStart());
            activeProfile.setFogEnd(activeProfile.getFogEnd() + activeProfileLerps.getFogEnd());

            lerpTicksCur++;
        }

        //lerpAmount = CoroUtilMisc.adjVal(lerpAmount, 1F, 0.01F);

        //targetProfile.getRgb().set();

        //Weather.dbg("activeIntensity: " + activeIntensity);
        //Weather.dbg("lerpAmount: " + lerpAmount);
    }

    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        updateWeatherState();

        if (SceneEnhancer.isFogOverridding()) {
            //float intensity = SceneEnhancer.heatwaveIntensity;

            //keep semi dynamic vanilla settings up to date
            fogVanilla.getRgb().set(event.getRed(), event.getGreen(), event.getBlue());

            /*float red = MathHelper.lerp(lerpAmount, activeProfile.getRgb().getX(), targetProfile.getRgb().getX());
            float green = MathHelper.lerp(lerpAmount, activeProfile.getRgb().getY(), targetProfile.getRgb().getY());
            float blue = MathHelper.lerp(lerpAmount, activeProfile.getRgb().getZ(), targetProfile.getRgb().getZ());*/

            event.setRed(activeProfile.getRgb().x());
            event.setGreen(activeProfile.getRgb().y());
            event.setBlue(activeProfile.getRgb().z());
            //RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
        }
    }

    public void onFogRender(EntityViewRenderEvent.RenderFogEvent event) {
        updateWeatherState();

        if (SceneEnhancer.isFogOverridding()) {
            //TODO: make use of this, density only works with EXP or EXP 2 mode
            //RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);

            if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
                //TODO: note, this value can be different depending on other contexts, we should try to grab GlStateManager.FOG.start, if we dont, itll glitch with bosses that cause fog and blindness effect, maybe more
                //value from FogRenderer.setupFog method
                fogVanilla.setFogStartSky(0);
                fogVanilla.setFogEndSky(event.getFarPlaneDistance());
                /*RenderSystem.fogStart(MathHelper.lerp(lerpAmount, activeProfile.getFogStartSky(), targetProfile.getFogStartSky()));
                RenderSystem.fogEnd(MathHelper.lerp(lerpAmount, activeProfile.getFogEndSky(), targetProfile.getFogEndSky()));*/

                //TODO: fix for new
                RenderSystem.setShaderFogStart(activeProfile.getFogStart());
                RenderSystem.setShaderFogEnd(activeProfile.getFogEnd());

                RenderSystem.setShaderFogStart(0);
                RenderSystem.setShaderFogEnd(event.getFarPlaneDistance());

            } else {
                //value from FogRenderer.setupFog method
                fogVanilla.setFogStart(event.getFarPlaneDistance() * 0.75F);
                fogVanilla.setFogEnd(event.getFarPlaneDistance());
                //Weather.dbg("getFarPlaneDistance: " + event.getFarPlaneDistance());
                /*RenderSystem.fogStart(MathHelper.lerp(lerpAmount, activeProfile.getFogStart(), targetProfile.getFogStart()));
                RenderSystem.fogEnd(MathHelper.lerp(lerpAmount, activeProfile.getFogEnd(), targetProfile.getFogEnd()));*/

                RenderSystem.setShaderFogStart(activeProfile.getFogStart());
                RenderSystem.setShaderFogEnd(activeProfile.getFogEnd());
            }
        }
    }

    public void startRandom() {
        //activeProfile = targetProfile;
        Random rand = new Random();
        int randFog = 0;
        if (activeProfile.getFogEnd() < 50) {
            randFog = 50 + rand.nextInt(50);
        } else {
            randFog = rand.nextInt(50);
        }
        randFog = rand.nextInt(100);
        targetProfile = new FogProfile(new Vector3f(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()), 0, randFog);
        lerpTicksMax = 20 + rand.nextInt(50);
        setupNewLerpRates();
        System.out.println("startRandom: " + randFog);
    }

    public void startHeatwave() {
        System.out.println("startHeatwave");
        //activeProfile = targetProfile;
        targetProfile = new FogProfile(fogHeatwave);
        setupNewLerpRates();
    }

    public void startSandstorm() {
        System.out.println("startSandstorm");
        //activeProfile = targetProfile;
        targetProfile = new FogProfile(fogSandstorm);
        setupNewLerpRates();
    }

    public void startSnowstorm() {
        System.out.println("startSnowstorm");
        //activeProfile = targetProfile;
        targetProfile = new FogProfile(fogSnowstorm);
        setupNewLerpRates();
    }

    public void restoreVanilla() {
        System.out.println("restoreVanilla");
        //activeProfile = targetProfile;
        targetProfile = new FogProfile(fogVanilla);
        setupNewLerpRates();
    }

    public void setupNewLerpRates() {
        lerpTicksCur = 0;
        //lerpTicksMax = 100;
        //for now we do 100 ticks to full lerp each time
        float partialLerpX = getLerpRate(activeProfile.getRgb().x(), targetProfile.getRgb().x(), lerpTicksMax);
        float partialLerpY = getLerpRate(activeProfile.getRgb().y(), targetProfile.getRgb().y(), lerpTicksMax);
        float partialLerpZ = getLerpRate(activeProfile.getRgb().z(), targetProfile.getRgb().z(), lerpTicksMax);
        activeProfileLerps.getRgb().set(partialLerpX, partialLerpY, partialLerpZ);

        activeProfileLerps.setFogStart(getLerpRate(activeProfile.getFogStart(), targetProfile.getFogStart(), lerpTicksMax));
        activeProfileLerps.setFogEnd(getLerpRate(activeProfile.getFogEnd(), targetProfile.getFogEnd(), lerpTicksMax));
    }

    public float getLerpRate(float curVal, float endVal, float fullLerpTicks) {
        return (endVal - curVal) / fullLerpTicks;
    }

    public boolean isFogOverriding() {
        ClientTickHandler.checkClientWeather();
        ClientWeather weather = ClientWeather.get();
        return (weather.isHeatwave() || weather.isSandstorm() || weather.isSnowstorm()) || lerpTicksCur < lerpTicksMax;
    }

    /**
     * In its own method so quick render update calls can force an update check to prevent old data use which causes flickers
     */
    public void updateWeatherState() {
        WeatherEventType curWeather = SceneEnhancer.getWeatherState();
        if (curWeather != lastWeatherType) {
            if (curWeather == WeatherEventType.SANDSTORM) {
                startSandstorm();
            } else if (curWeather == WeatherEventType.SNOWSTORM) {
                startSnowstorm();
            } else if (curWeather == WeatherEventType.HEATWAVE) {
                startHeatwave();
            } else if (curWeather == null) {
                restoreVanilla();
            }
        }
        lastWeatherType = curWeather;
    }
}
