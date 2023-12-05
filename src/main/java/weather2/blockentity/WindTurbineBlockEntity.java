package weather2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import weather2.WeatherBlocks;
import weather2.config.ConfigWind;
import weather2.energy.EnergyManager;
import weather2.util.WeatherUtilEntity;
import weather2.util.WindReader;
import weather2.weathersystem.WeatherManager;

public class WindTurbineBlockEntity extends BlockEntity {

	public float smoothAngle = 0;
	public float smoothAnglePrev = 0;

	public float smoothAngleRotationalVel = 0;

	public boolean isOutsideCached = false;

	private boolean needsInit = true;

	//private final EnergyManager energyManager;
	private LazyOptional<EnergyManager> energy;
	private EnergyManager energyManager;

	//amount generated at windspeed of 1, theoretical max windspeed is 2 when tornado right on top of it
	private int maxNormalGenerated = ConfigWind.Wind_Turbine_FE_Generated_Per_Tick;
	private int capacity = maxNormalGenerated * 2;
	private int maxTransfer = capacity;

	private float lastWindSpeed = 0;

	public WindTurbineBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
		super(WeatherBlocks.BLOCK_ENTITY_WIND_TURBINE.get(), p_155229_, p_155230_);

		this.energyManager = new EnergyManager(maxTransfer, capacity);
		this.energy = LazyOptional.of(() -> this.energyManager);
	}

	@Override
	public void setLevel(final Level level) {
		super.setLevel(level);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, WindTurbineBlockEntity entity) {
		entity.tick(level, pos, state);
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (needsInit) {
			needsInit = false;
			updateIsOutside();
		}
		if (level.getGameTime() % 100 == 0) {
			updateIsOutside();
		}
		if (isOutsideCached) {
			if (level.getGameTime() % 20 == 0) {
				WeatherManager weatherManager = WindReader.getWeatherManagerFor(level);
				if (weatherManager != null) {
					lastWindSpeed = weatherManager.getWindManager().getWindSpeedPositional(getBlockPos(), 2, false);
				}
			}
		}
		if (!level.isClientSide) {
			if (isOutsideCached) {
				this.energyManager.addEnergy((int) (maxNormalGenerated * lastWindSpeed));
				outputEnergy();
			}
		} else {
			if (isOutsideCached) {
				float windSpeed = lastWindSpeed;//WindReader.getWindSpeed(level);
				float rotMax = 100F;
				float maxSpeed = (windSpeed / 2F) * rotMax;
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

	public void updateIsOutside() {
		isOutsideCached = WeatherUtilEntity.isPosOutside(level, new Vec3(getBlockPos().getX()+0.5F, getBlockPos().getY()+0.5F, getBlockPos().getZ()+0.5F), false, true);
	}

	public void outputEnergy() {
		//System.out.println(this.energyManager.getEnergyStored());
		if (this.energyManager.getEnergyStored() >= this.energyManager.getMaxExtract() && this.energyManager.canExtract()) {
			for (final var direction : Direction.values()) {
				final BlockEntity be = this.level.getBlockEntity(this.worldPosition.relative(direction));
				if (be == null) {
					continue;
				}

				be.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(storage -> {
					if (be != this && storage.getEnergyStored() < storage.getMaxEnergyStored()) {
						this.energyManager.drainEnergy(this.energyManager.getMaxExtract());
						//Weather.LOGGER.info("Send: {}", this.energyManager.getMaxExtract());
						final int received = storage.receiveEnergy(this.energyManager.getMaxExtract(), false);
						//Weather.LOGGER.info("Final Received: {}", received);
					}
				});
			}
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

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		LazyOptional<T> energyCapability = energyManager.getCapability(cap);
		if (energyCapability.isPresent()) {
			return energyCapability;
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		this.energy.invalidate();
	}
}
