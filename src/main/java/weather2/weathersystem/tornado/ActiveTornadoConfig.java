package weather2.weathersystem.tornado;

/**
 * Defines the shape and other characteristics of a tornado
 */
public class ActiveTornadoConfig {

    private float radiusOfBase;
    //incremental size of radius per layer
    private float radiusIncreasePerLayer;
    private float height;
    private float spinSpeed;

    public float getRadiusOfBase() {
        return radiusOfBase;
    }

    public ActiveTornadoConfig setRadiusOfBase(float radiusOfBase) {
        this.radiusOfBase = radiusOfBase;
        return this;
    }

    public float getRadiusIncreasePerLayer() {
        return radiusIncreasePerLayer;
    }

    public ActiveTornadoConfig setRadiusIncreasePerLayer(float radiusIncreasePerLayer) {
        this.radiusIncreasePerLayer = radiusIncreasePerLayer;
        return this;
    }

    public float getHeight() {
        return height;
    }

    public ActiveTornadoConfig setHeight(float height) {
        this.height = height;
        return this;
    }

    public float getSpinSpeed() {
        return spinSpeed;
    }

    public ActiveTornadoConfig setSpinSpeed(float spinSpeed) {
        this.spinSpeed = spinSpeed;
        return this;
    }
}
