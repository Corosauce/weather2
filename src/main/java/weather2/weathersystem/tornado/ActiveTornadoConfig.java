package weather2.weathersystem.tornado;

import net.minecraft.nbt.CompoundTag;

/**
 * Defines the shape and other characteristics of a tornado
 */
public class ActiveTornadoConfig {

    private float radiusOfBase;
    //incremental size of radius per layer
    private float radiusIncreasePerLayer;
    private float height;
    private float spinSpeed;
    private float entityPullDistXZ;
    private float entityPullDistXZForY;

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("radiusOfBase", radiusOfBase);
        tag.putFloat("radiusIncreasePerLayer", radiusIncreasePerLayer);
        tag.putFloat("height", height);
        tag.putFloat("spinSpeed", spinSpeed);
        tag.putFloat("entityPullDistXZ", entityPullDistXZ);
        tag.putFloat("entityPullDistXZForY", entityPullDistXZForY);
        return tag;
    }

    public static ActiveTornadoConfig deserialize(CompoundTag tag) {
        ActiveTornadoConfig config = new ActiveTornadoConfig();
        config.setRadiusOfBase(tag.getFloat("radiusOfBase"));
        config.setRadiusIncreasePerLayer(tag.getFloat("radiusIncreasePerLayer"));
        config.setHeight(tag.getFloat("height"));
        config.setSpinSpeed(tag.getFloat("spinSpeed"));
        config.setEntityPullDistXZ(tag.getFloat("entityPullDistXZ"));
        config.setEntityPullDistXZForY(tag.getFloat("entityPullDistXZForY"));
        return config;
    }

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

    public float getEntityPullDistXZ() {
        return entityPullDistXZ;
    }

    public ActiveTornadoConfig setEntityPullDistXZ(float entityPullDistXZ) {
        this.entityPullDistXZ = entityPullDistXZ;
        return this;
    }

    public float getEntityPullDistXZForY() {
        return entityPullDistXZForY;
    }

    public ActiveTornadoConfig setEntityPullDistXZForY(float entityPullDistXZForY) {
        this.entityPullDistXZForY = entityPullDistXZForY;
        return this;
    }
}
