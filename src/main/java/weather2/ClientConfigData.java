package weather2;

import net.minecraft.nbt.NBTTagCompound;

public class ClientConfigData {

    public boolean overcastMode = false;
    public boolean Storm_Tornado_grabPlayer = true;
    public boolean Storm_Tornado_grabPlayersOnly = false;
    public boolean Storm_Tornado_grabMobs = true;
    public boolean Storm_Tornado_grabAnimals = true;
    public boolean Storm_Tornado_grabVillagers = true;

    public void readNBT(NBTTagCompound nbt) {
        overcastMode = nbt.getBoolean("overcastMode");
        Storm_Tornado_grabPlayer = nbt.getBoolean("Storm_Tornado_grabPlayer");
        Storm_Tornado_grabPlayersOnly = nbt.getBoolean("Storm_Tornado_grabPlayersOnly");
        Storm_Tornado_grabMobs = nbt.getBoolean("Storm_Tornado_grabMobs");
        Storm_Tornado_grabAnimals = nbt.getBoolean("Storm_Tornado_grabAnimals");
        Storm_Tornado_grabVillagers = nbt.getBoolean("Storm_Tornado_grabVillagers");
    }

}
