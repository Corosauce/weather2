package weather2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.ServerTickHandler;
import weather2.WeatherBlocks;
import weather2.block.SensorBlock;
import weather2.config.ConfigMisc;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

public class SensorBlockEntity extends BlockEntity {

	public SensorBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
		super(WeatherBlocks.BLOCK_ENTITY_TORNADO_SENSOR.get(), p_155229_, p_155230_);
	}

	@Override
	public void setLevel(final Level level) {
		super.setLevel(level);
	}

	public static void tick(Level level, BlockPos pos2, BlockState state, SensorBlockEntity entity) {
		if (!level.isClientSide && level.getGameTime() % 100 == 0) {
			WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(level);
			if (wm != null) {
				Vec3 pos = new Vec3(pos2.getX(), pos2.getY(), pos2.getZ());
				StormObject so = wm.getClosestStorm(pos, ConfigMisc.sirenActivateDistance, StormObject.STATE_FORMING);
				if (so != null) {
					entity.setPoweredState(true);
				} else {
					entity.setPoweredState(false);
				}
			}
		}
	}

	public void setPoweredState(boolean state) {
		BlockState blockState = level.getBlockState(this.getBlockPos());
		if (blockState.getBlock() instanceof SensorBlock) {
			((SensorBlock) blockState.getBlock()).setPoweredState(this.getBlockState(), level, this.getBlockPos(), state);
		}
	}

	@Override
	public void load(final CompoundTag tag) {
		super.load(tag);
	}

	@Override
	protected void saveAdditional(final CompoundTag tag) {
		super.saveAdditional(tag);
	}
}
