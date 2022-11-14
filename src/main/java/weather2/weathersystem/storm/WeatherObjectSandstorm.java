package weather2.weathersystem.storm;

import java.util.Random;

import com.corosus.coroutil.util.CU;
import com.corosus.coroutil.util.CULog;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.WeatherBlocks;
import weather2.config.ConfigSand;
import weather2.util.CachedNBTTagCompound;
import weather2.util.WeatherUtilBlock;
import weather2.weathersystem.WeatherManager;
import weather2.weathersystem.wind.WindManager;

public class WeatherObjectSandstorm extends WeatherObject {

	public int age = 0;
	public int maxAge = 20*20;

	public Random rand = new Random();
	
	public WeatherObjectSandstorm(WeatherManager parManager) {
		super(parManager);
		
		this.weatherObjectType = EnumWeatherObjectType.SAND;
	}
	
	public void initSandstormSpawn(Vec3 pos) {
		this.pos = pos;
		this.maxAge = 20*60*5;
	}
	
	public static boolean isDesert(Biome biome) {
		return isDesert(biome, false);
	}
	
	/**
	 * prevent rivers from killing sandstorm if its just passing over from desert to more desert
	 * 
	 * @param biome
	 * @param forSpawn
	 * @return
	 */
	public static boolean isDesert(Biome biome, boolean forSpawn) {
		//TODO: make sure new comparison works
		return biome.equals(Biomes.DESERT)/* || biome.equals(Biomes.DESERT_HILLS)*/ || (!forSpawn && biome.equals(Biomes.RIVER)) || biome.getRegistryName().toString().toLowerCase().contains("desert");
		//return biome == Biomes.DESERT || biome == Biomes.DESERT_HILLS || (!forSpawn && biome == Biomes.RIVER) || biome.getCategory().getName().toLowerCase().contains("desert");
	}
	
	/**
	 * 
	 * - size of storm determined by how long it was in desert
	 * - front of storm dies down once it exits desert
	 * - stops moving once fully dies down
	 * 
	 * - storm continues for minutes even after front has exited desert
	 * 
	 * 
	 * 
	 */
	public void tickProgressionAndMovement() {
		
		Level world = manager.getWorld();
		WindManager windMan = manager.getWindManager();
	}

	public int getSize() {
		return 250;
	}
	
	@Override
	public void tick() {
		super.tick();

		if (!manager.getWorld().isClientSide()) {
			this.age++;
			//CULog.dbg("this.age: " + this.age);
			if (this.age > this.maxAge) {
				this.remove();
			}

			if (getIntensity() > 0.2D) {
				tickBlockSandBuildup();
			}
		}

		posGround = pos;
	}

	/**
	 * 0-1F for first half of age, 1-0F for second half of age
	 * @return
	 */
	public float getIntensity() {
		float age = this.age;
		float maxAge = this.maxAge;
		if (age / maxAge <= 0.5F) {
			return age / (maxAge/2);
		} else {
			return 1F - (age / (maxAge/2) - 1F);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void tickClient() {

	}

	public void tickBlockSandBuildup() {

		Level world = manager.getWorld();
		WindManager windMan = manager.getWindManager();

		float angle = windMan.getWindAngleForClouds();

		//keep it set to do a lot of work only occasionally, prevents chunk render tick spam for client which kills fps
		int delay = ConfigSand.Sandstorm_Sand_Buildup_TickRate;
		int loop = (int)((float)ConfigSand.Sandstorm_Sand_Buildup_LoopAmountBase * getIntensity());

		int count = 0;

		//sand block buildup
		if (!world.isClientSide) {
			if (world.getGameTime() % delay == 0) {

				for (int i = 0; i < loop; i++) {

					//rate of placement based on storm intensity
					if (rand.nextDouble() >= getIntensity()) continue;

					Vec3 vecPos = getRandomPosInSandstorm();

					int y = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos(vecPos.x, 0, vecPos.z)).getY();

					BlockPos blockPos = new BlockPos(vecPos.x, y, vecPos.z);

					//avoid unloaded areas
					if (!world.hasChunkAt(blockPos)) continue;

					Biome biomeIn = world.m_204166_(blockPos).m_203334_();

					if (ConfigSand.Sandstorm_Sand_Buildup_AllowOutsideDesert || isDesert(biomeIn)) {
						//CULog.dbg("sand pos: " + blockPos);
						WeatherUtilBlock.fillAgainstWallSmoothly(world, new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()), angle, 15, 2, WeatherBlocks.blockSandLayer, 3);
					}

					count++;


				}

				//System.out.println("count: " + count);
			}
		}
	}

	public Vec3 getRandomPosInSandstorm() {
		Random rand = new Random();
		int x = (int) Math.floor(posGround.x + rand.nextInt(getSize()) - rand.nextInt(getSize()));
		int z = (int) Math.floor(posGround.z + rand.nextInt(getSize()) - rand.nextInt(getSize()));
		int y = WeatherUtilBlock.getPrecipitationHeightSafe(manager.getWorld(), new BlockPos(x, 128, z)).getY();
		Vec3 vec = new Vec3(x, y, z);
		return vec;/*
		if (vec.distanceTo(posGround) < getSize()) {
			return vec;
		}
		return null;*/
	}
	
	@Override
	public int getUpdateRateForNetwork() {
		return 1;
	}
	
	@Override
	public void nbtSyncForClient() {
		super.nbtSyncForClient();
		CachedNBTTagCompound data = this.getNbtCache();
		data.putInt("age", age);
		data.putInt("maxAge", maxAge);
	}
	
	@Override
	public void nbtSyncFromServer() {
		super.nbtSyncFromServer();
		CachedNBTTagCompound parNBT = this.getNbtCache();
		this.age = parNBT.getInt("age");
		this.maxAge = parNBT.getInt("maxAge");
	}

	@Override
	public void read()
	{
		super.read();
		nbtSyncFromServer();
		CachedNBTTagCompound var1 = this.getNbtCache();
		motion = new Vec3(var1.getDouble("vecX"), var1.getDouble("vecY"), var1.getDouble("vecZ"));
	}

	@Override
	public void write()
	{
		super.write();
		nbtSyncForClient();

		CachedNBTTagCompound nbt = this.getNbtCache();

		nbt.putDouble("vecX", motion.x);
		nbt.putDouble("vecY", motion.y);
		nbt.putDouble("vecZ", motion.z);

	}

	@Override
	public void cleanup() {
		super.cleanup();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void cleanupClient() {
		super.cleanupClient();
	}

}
