package weather2.weathersystem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import weather2.WeatherPacketHelper;
import weather2.config.ConfigMisc;
import weather2.weathersystem.storm.StormObject;
import cpw.mods.fml.common.network.PacketDispatcher;

public class WeatherManagerServer extends WeatherManagerBase {

	//storm logic, syncing to client
	
	public int syncRange = 256;

	public WeatherManagerServer(int parDim) {
		super(parDim);
	}
	
	@Override
	public World getWorld() {
		return DimensionManager.getWorld(dim);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		World world = getWorld();
		
		if (world != null) {
			
			//move to config
			boolean forceWeatherOverride = ConfigMisc.takeControlOfGlobalRain;
			
			if (forceWeatherOverride) {
				world.getWorldInfo().setRaining(false);
		    	world.getWorldInfo().setThundering(false);
			}
			
			if (world.getTotalWorldTime() % 40 == 0) {
				for (int i = 0; i < getStormObjects().size(); i++) {
					syncStormUpdate(getStormObjects().get(i));
				}
			}
			
			//tick volcanos
			for (int i = 0; i < getVolcanoObjects().size(); i++) {
				getVolcanoObjects().get(i).tick();
			}
			
			//getVolcanoObjects().clear();
			
			//tick the storms
			for (int i = 0; i < getStormObjects().size(); i++) {
				getStormObjects().get(i).tick();
			}
			
			//tick the server wind
			windMan.tick();
			
			//test code
			if (world.getTotalWorldTime() % 100 == 0) {
				if (world.playerEntities.size() > 0) {
					/*if (getStormObjects().size() == 0) {
						StormObject so = new StormObject(this);
						EntityPlayer entP = (EntityPlayer) world.playerEntities.get(0);
						so.pos = Vec3.createVectorHelper(entP.posX, 200, entP.posZ);
						getStormObjects().add(so);
						syncStormNew(so);
					}*/
				}
			}
		}
	}

	public void syncStormNew(StormObject parStorm) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncStormNew");
		data.setCompoundTag("storm", parStorm.nbtSyncForClient());
		PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
		//PacketDispatcher.sendPacketToAllAround(parStorm.pos.xCoord, parStorm.pos.yCoord, parStorm.pos.zCoord, syncRange, getWorld().provider.dimensionId, WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data));
	}
	
	public void syncStormUpdate(StormObject parStorm) {
		//packets
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "syncStormUpdate");
		data.setCompoundTag("storm", parStorm.nbtSyncForClient());
		PacketDispatcher.sendPacketToAllInDimension(WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data), getWorld().provider.dimensionId);
	}
	
	public void syncStormRemove() {
		
	}
	
	
	
}
