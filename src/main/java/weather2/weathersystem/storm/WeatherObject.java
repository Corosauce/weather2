package weather2.weathersystem.storm;

import CoroUtil.util.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import weather2.util.CachedNBTTagCompound;
import weather2.weathersystem.WeatherManagerBase;

public class WeatherObject {

	public static long lastUsedStormID = 0; //ID starts from 0 for each game start, no storm nbt disk reload for now
	public long ID; //loosely accurate ID for tracking, but we wanted to persist between world reloads..... need proper UUID??? I guess add in UUID later and dont persist, start from 0 per game run
	public boolean isDead = false;

	/**
	 * used to count up to a threshold to finally remove weather objects,
	 * solves issue of simbox cutoff removing storms for first few ticks as player is joining in singleplayer
	 * helps with multiplayer, requiring 30 seconds of no players near before removal
	 */
	public int ticksSinceNoNearPlayer = 0;
	
	public WeatherManagerBase manager;
	
	public Vec3 pos = new Vec3(0, 0, 0);
	public Vec3 posGround = new Vec3(0, 0, 0);
	public Vec3 motion = new Vec3(0, 0, 0);

	//used as radius
	public int size = 50;
	public int maxSize = 0;
	
	public EnumWeatherObjectType weatherObjectType = EnumWeatherObjectType.CLOUD;

	private CachedNBTTagCompound nbtCache;

	//private NBTTagCompound cachedClientNBTState;

	public WeatherObject(WeatherManagerBase parManager) {
		manager = parManager;
		nbtCache = new CachedNBTTagCompound();
	}
	
	public void initFirstTime() {
		ID = lastUsedStormID++;
	}
	
	public void tick() {
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public void tickRender(float partialTick) {
		
	}
	
	public void reset() {
		remove();
	}
	
	public void remove() {
		//Weather.dbg("storm killed, ID: " + ID);
		
		isDead = true;
		
		//cleanup memory
		//if (FMLCommonHandler.instance().getEffectiveSide() == Dist.CLIENT/*manager.getWorld().isRemote*/) {
		if (EffectiveSide.get().equals(LogicalSide.CLIENT)) {
			cleanupClient();
		}
		
		cleanup();
	}
	
	public void cleanup() {
		manager = null;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void cleanupClient() {
		
	}
	
	public int getUpdateRateForNetwork() {
		return 40;
	}
	
	public void read() {
		
    }
	
	public void write() {

    }
	
	public void nbtSyncFromServer() {
		CachedNBTTagCompound parNBT = this.getNbtCache();
		ID = parNBT.getLong("ID");
		//Weather.dbg("StormObject " + ID + " receiving sync");
		
		pos = new Vec3(parNBT.getDouble("posX"), parNBT.getDouble("posY"), parNBT.getDouble("posZ"));
		//motion = new Vec3(parNBT.getDouble("motionX"), parNBT.getDouble("motionY"), parNBT.getDouble("motionZ"));
		motion = new Vec3(parNBT.getDouble("vecX"), parNBT.getDouble("vecY"), parNBT.getDouble("vecZ"));
		size = parNBT.getInt("size");
		maxSize = parNBT.getInt("maxSize");
		this.weatherObjectType = EnumWeatherObjectType.get(parNBT.getInt("weatherObjectType"));
	}
	
	public void nbtSyncForClient() {
		CachedNBTTagCompound nbt = this.getNbtCache();
		nbt.putDouble("posX", pos.xCoord);
		nbt.putDouble("posY", pos.yCoord);
		nbt.putDouble("posZ", pos.zCoord);

		/*nbt.putDouble("motionX", motion.xCoord);
		nbt.putDouble("motionY", motion.yCoord);
		nbt.putDouble("motionZ", motion.zCoord);*/
		nbt.putDouble("vecX", motion.xCoord);
		nbt.putDouble("vecY", motion.yCoord);
		nbt.putDouble("vecZ", motion.zCoord);

		nbt.putLong("ID", ID);
		//just blind set ID into non cached data so client always has it, no need to check for forced state and restore orig state
		nbt.getNewNBT().putLong("ID", ID);

		nbt.putInt("size", size);
		nbt.putInt("maxSize", maxSize);
		nbt.putInt("weatherObjectType", this.weatherObjectType.ordinal());
	}

	public CachedNBTTagCompound getNbtCache() {
		return nbtCache;
	}

	public void setNbtCache(CachedNBTTagCompound nbtCache) {
		this.nbtCache = nbtCache;
	}
	
}
