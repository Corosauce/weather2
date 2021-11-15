package weather2.weathersystem.fog;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.util.math.vector.Vector3f;

public class FogProfile {

    private Vector3f rgb;
    private float fogStart;
    private float fogEnd;
    private float fogStartSky;
    private float fogEndSky;

    public FogProfile(FogProfile profile) {
        this.rgb = profile.rgb;
        this.fogStart = profile.fogStart;
        this.fogStartSky = profile.fogStart;
        this.fogEnd = profile.fogEnd;
        this.fogEndSky = profile.fogEnd;
    }

    public FogProfile(Vector3f rgb, float fogStart, float fogEnd) {
        this.rgb = rgb;
        this.fogStart = fogStart;
        this.fogStartSky = fogStart;
        this.fogEnd = fogEnd;
        this.fogEndSky = fogEnd;
    }

    public Vector3f getRgb() {
        return rgb;
    }

    public void setRgb(Vector3f rgb) {
        this.rgb = rgb;
    }

    public float getFogStart() {
        return fogStart;
    }

    public void setFogStart(float fogStart) {
        this.fogStart = fogStart;
    }

    public float getFogEnd() {
        return fogEnd;
    }

    public void setFogEnd(float fogEnd) {
        this.fogEnd = fogEnd;
    }

    public float getFogStartSky() {
        return fogStartSky;
    }

    public void setFogStartSky(float fogStartSky) {
        this.fogStartSky = fogStartSky;
    }

    public float getFogEndSky() {
        return fogEndSky;
    }

    public void setFogEndSky(float fogEndSky) {
        this.fogEndSky = fogEndSky;
    }
}
