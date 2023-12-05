package weather2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import weather2.WeatherBlocks;
import weather2.block.AnemometerBlock;
import weather2.util.WeatherUtilEntity;
import weather2.util.WindReader;

public class AnemometerBlockEntity extends BlockEntity {

	public float smoothAngle = 0;
	public float smoothAnglePrev = 0;

	public float smoothAngleRotationalVel = 0;

	public boolean isOutsideCached = false;

	public AnemometerBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
		super(WeatherBlocks.BLOCK_ENTITY_ANEMOMETER.get(), p_155229_, p_155230_);
	}

	@Override
	public void setLevel(final Level level) {
		super.setLevel(level);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, AnemometerBlockEntity entity) {
		entity.tick(level, pos, state);
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (!level.isClientSide) {

		} else {
			if (level.getGameTime() % 40 == 0) {
				isOutsideCached = WeatherUtilEntity.isPosOutside(level, new Vec3(getBlockPos().getX()+0.5F, getBlockPos().getY()+0.5F, getBlockPos().getZ()+0.5F), false, true);
			}

			if (isOutsideCached) {
				//do not cache for this, the amp value used will mess with the turbines amp, design oversight
				float windSpeed = WindReader.getWindSpeed(level, pos, 1);
				float rotMax = 50F;
				float maxSpeed = (windSpeed / 1.2F) * rotMax;
				if (smoothAngleRotationalVel < maxSpeed) {
					smoothAngleRotationalVel += windSpeed * 0.3F;
				}
				if (smoothAngleRotationalVel > rotMax) smoothAngleRotationalVel = rotMax;
				if (smoothAngle >= 180) smoothAngle -= 360;
			}

			smoothAnglePrev = smoothAngle;
			smoothAngle += smoothAngleRotationalVel;
			smoothAngleRotationalVel -= 0.01F;

			smoothAngleRotationalVel *= 0.99F;

			if (smoothAngleRotationalVel <= 0) smoothAngleRotationalVel = 0;
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
