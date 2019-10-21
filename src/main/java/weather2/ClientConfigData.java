package weather2;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;
import weather2.config.ConfigMisc;
import weather2.config.ConfigTornado;

/**
 * Used for anything that needs to be used on both client and server side, to avoid config mismatch between dedicated server and clients
 */
public class ClientConfigData {

    public boolean overcastMode = false;
    public boolean Storm_Tornado_grabPlayer = true;
    public boolean Storm_Tornado_grabPlayersOnly = false;
    public boolean Storm_Tornado_grabMobs = true;
    public boolean Storm_Tornado_grabAnimals = true;
    public boolean Storm_Tornado_grabItems = false;
    public boolean Storm_Tornado_grabVillagers = true;
    public boolean Aesthetic_Only_Mode = false;

    /**
     * For client side
     *
     * @param nbt
     */
    public void read(CompoundNBT nbt) {
        overcastMode = nbt.getBoolean("overcastMode");
        Storm_Tornado_grabPlayer = nbt.getBoolean("Storm_Tornado_grabPlayer");
        Storm_Tornado_grabPlayersOnly = nbt.getBoolean("Storm_Tornado_grabPlayersOnly");
        Storm_Tornado_grabMobs = nbt.getBoolean("Storm_Tornado_grabMobs");
        Storm_Tornado_grabAnimals = nbt.getBoolean("Storm_Tornado_grabAnimals");
        Storm_Tornado_grabVillagers = nbt.getBoolean("Storm_Tornado_grabVillagers");
        Storm_Tornado_grabItems = nbt.getBoolean("Storm_Tornado_grabItems");
        Aesthetic_Only_Mode = nbt.getBoolean("Aesthetic_Only_Mode");
    }

    /**
     * For server side
     *
     * @param data
     */
    public static void write(CompoundNBT data) {

        data.putBoolean("overcastMode", ConfigMisc.overcastMode);
        data.putBoolean("Storm_Tornado_grabPlayer", ConfigTornado.Storm_Tornado_grabPlayer);
        data.putBoolean("Storm_Tornado_grabPlayersOnly", ConfigTornado.Storm_Tornado_grabPlayersOnly);
        data.putBoolean("Storm_Tornado_grabMobs", ConfigTornado.Storm_Tornado_grabMobs);
        data.putBoolean("Storm_Tornado_grabAnimals", ConfigTornado.Storm_Tornado_grabAnimals);
        data.putBoolean("Storm_Tornado_grabVillagers", ConfigTornado.Storm_Tornado_grabVillagers);
        data.putBoolean("Storm_Tornado_grabItems", ConfigTornado.Storm_Tornado_grabItems);
        data.putBoolean("Aesthetic_Only_Mode", ConfigMisc.Aesthetic_Only_Mode);


    }

}
