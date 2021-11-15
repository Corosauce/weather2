package weather2.weathersystem;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.weather.StormState;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import weather2.PacketNBTFromServer;
import weather2.WeatherBlocks;
import weather2.WeatherNetworking;
import weather2.util.WeatherUtilBlock;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.wind.WindManager;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class WeatherManagerServer extends WeatherManager {
	private final ServerLevel world;

	public WeatherManagerServer(ServerLevel world) {
		super(world.dimension());
		this.world = world;
	}

	@Override
	public Level getWorld() {
		return world;
	}

	@Override
	public void tick() {
		super.tick();

		WeatherController controller = WeatherControllerManager.forWorld(world);
		if (controller != null) {
			StormState snowstorm = controller.getSnowstorm();
			StormState sandstorm = controller.getSandstorm();
			if (snowstorm != null) {
				tickStormBlockBuildup(snowstorm, Blocks.SNOW);
			} else if (sandstorm != null) {
				tickStormBlockBuildup(sandstorm, WeatherBlocks.blockSandLayer);
			}
		}

		if (world != null) {
			WindManager windMan = getWindManager();

			//sync wind
			if (world.getGameTime() % 60 == 0) {
				syncWindUpdate(windMan);
			}
		}
	}

	public void tickStormBlockBuildup(StormState stormState, Block block) {
		Level world = getWorld();
		WindManager windMan = getWindManager();
		Random rand = world.random;

		float angle = windMan.getWindAngle();

		if (world.getGameTime() % stormState.getBuildupTickRate() == 0) {
			List<ChunkHolder> list = Lists.newArrayList(((ServerLevel)world).getChunkSource().chunkMap.getChunks());
			Collections.shuffle(list);
			list.forEach((p_241099_7_) -> {
				Optional<LevelChunk> optional = p_241099_7_.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
				if (optional.isPresent()) {
					for (int i = 0; i < 10; i++) {
						BlockPos blockPos = new BlockPos((optional.get().getPos().x * 16) + rand.nextInt(16), 0, (optional.get().getPos().z * 16) + rand.nextInt(16));
						int y = WeatherUtilBlock.getPrecipitationHeightSafe(world, blockPos).getY();
						Vec3 pos = new Vec3(blockPos.getX(), y, blockPos.getZ());
						WeatherUtilBlock.fillAgainstWallSmoothly(world, pos, angle, 15, 2, block, stormState.getMaxStackable());
					}
				}
			});
		}
	}

	public void syncStormRemove(WeatherObject parStorm) {
		//packets
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormRemove");
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());
		//data.put("data", parStorm.nbtSyncForClient(new NBTTagCompound()));
		//fix for client having broken states
		data.getCompound("data").putBoolean("removed", true);
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}

	public void syncWindUpdate(WindManager parManager) {
		//packets
		CompoundTag data = new CompoundTag();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncWindUpdate");
		data.put("data", parManager.nbtSyncForClient());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().dimension()), new PacketNBTFromServer(data));
	}
}
