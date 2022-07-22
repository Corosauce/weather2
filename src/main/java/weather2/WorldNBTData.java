package weather2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class WorldNBTData extends SavedData {

    private CompoundTag data;
    private IWorldData dataHandler;

    public WorldNBTData() {
        this.data = new CompoundTag();
    }

    public WorldNBTData(CompoundTag data) {
        this.data = data;
    }

    public void setDataHandler(IWorldData dataHandler) {
        this.dataHandler = dataHandler;
    }

    public static WorldNBTData load(CompoundTag p_151484_) {
        return new WorldNBTData(p_151484_);
    }

    @Override
    public CompoundTag save(CompoundTag p_77763_) {
        dataHandler.save(p_77763_);
        return p_77763_;
    }

    public CompoundTag getData() {
        return data;
    }

    @Override
    //backwards compat, the data is always changing
    public boolean isDirty() {
        return true;
    }
}
