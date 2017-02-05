package weather2.weathersystem.storm;

import CoroUtil.util.Vec3;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.weathersystem.WeatherManagerBase;

public class WeatherObject {

	public static long lastUsedStormID = 0; //ID starts from 0 for each game start, no storm nbt disk reload for now
	public long ID; //loosely accurate ID for tracking, but we wanted to persist between world reloads..... need proper UUID??? I guess add in UUID later and dont persist, start from 0 per game run
	public boolean isDead = false;
	
	public WeatherManagerBase manager;
	
	public Vec3 pos = new Vec3(0, 0, 0);
	public Vec3 posGround = new Vec3(0, 0, 0);
	public Vec3 motion = new Vec3(0, 0, 0);
	
	public int size = 50;
	public int maxSize = 0;
	
	public EnumWeatherObjectType weatherObjectType = EnumWeatherObjectType.CLOUD;
	
	public WeatherObject(WeatherManagerBase parManager) {
		manager = parManager;
	}
	
	public void initFirstTime() {
		ID = lastUsedStormID++;
	}
	
	public void tick() {
		
	}
	
	@SideOnly(Side.CLIENT)
	public void tickRender(float partialTick) {
		
	}
	
	public void reset() {
		setDead();
	}
	
	public void setDead() {
		//Weather.dbg("storm killed, ID: " + ID);
		
		isDead = true;
		
		//cleanup memory
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT/*manager.getWorld().isRemote*/) {
			cleanupClient();
		}
		
		cleanup();
	}
	
	public void cleanup() {
		manager = null;
	}
	
	@SideOnly(Side.CLIENT)
	public void cleanupClient() {
		
	}
	
	public int getUpdateRateForNetwork() {
		return 40;
	}
	
	public void readFromNBT(NBTTagCompound var1) {
		
    }
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		return nbt;
    }
	
	public void nbtSyncFromServer(NBTTagCompound parNBT) {
		ID = parNBT.getLong("ID");
		//Weather.dbg("StormObject " + ID + " receiving sync");
		
		pos = new Vec3(parNBT.getDouble("posX"), parNBT.getDouble("posY"), parNBT.getDouble("posZ"));
		size = parNBT.getInteger("size");
		maxSize = parNBT.getInteger("maxSize");
		this.weatherObjectType = EnumWeatherObjectType.get(parNBT.getInteger("weatherObjectType"));
	}
	
	public NBTTagCompound nbtSyncForClient(NBTTagCompound nbt) {
		nbt.setDouble("posX", pos.xCoord);
		nbt.setDouble("posY", pos.yCoord);
		nbt.setDouble("posZ", pos.zCoord);
		
		nbt.setLong("ID", ID);
		nbt.setInteger("size", size);
		nbt.setInteger("maxSize", maxSize);
		nbt.setInteger("weatherObjectType", this.weatherObjectType.ordinal());
		return nbt;
	}
	
}
