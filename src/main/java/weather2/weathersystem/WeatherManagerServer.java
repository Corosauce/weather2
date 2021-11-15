package weather2.weathersystem;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.weather.StormState;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.network.PacketDistributor;
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
	private final ServerWorld world;

	public WeatherManagerServer(ServerWorld world) {
		super(world.getDimensionKey());
		this.world = world;
	}

	@Override
	public World getWorld() {
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
		World world = getWorld();
		WindManager windMan = getWindManager();
		Random rand = world.rand;

		float angle = windMan.getWindAngle();

		if (world.getGameTime() % stormState.getBuildupTickRate() == 0) {
			List<ChunkHolder> list = Lists.newArrayList(((ServerWorld)world).getChunkProvider().chunkManager.getLoadedChunksIterable());
			Collections.shuffle(list);
			list.forEach((p_241099_7_) -> {
				Optional<Chunk> optional = p_241099_7_.getTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
				if (optional.isPresent()) {
					for (int i = 0; i < 10; i++) {
						BlockPos blockPos = new BlockPos((optional.get().getPos().x * 16) + rand.nextInt(16), 0, (optional.get().getPos().z * 16) + rand.nextInt(16));
						int y = WeatherUtilBlock.getPrecipitationHeightSafe(world, blockPos).getY();
						Vector3d pos = new Vector3d(blockPos.getX(), y, blockPos.getZ());
						WeatherUtilBlock.fillAgainstWallSmoothly(world, pos, angle, 15, 2, block, stormState.getMaxStackable());
					}
				}
			});
		}
	}

	public void syncStormRemove(WeatherObject parStorm) {
		//packets
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormRemove");
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());
		//data.put("data", parStorm.nbtSyncForClient(new NBTTagCompound()));
		//fix for client having broken states
		data.getCompound("data").putBoolean("removed", true);
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimensionKey()), new PacketNBTFromServer(data));
	}

	public void syncWindUpdate(WindManager parManager) {
		//packets
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncWindUpdate");
		data.put("data", parManager.nbtSyncForClient());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimensionKey()), new PacketNBTFromServer(data));
	}
}
