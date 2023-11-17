package weather2.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class EnergyManager extends EnergyStorage {
	private boolean canExtract = true;

	public EnergyManager(int maxTransfer, int capacity) {
		super(capacity, maxTransfer, maxTransfer);
	}

	public int getMaxExtract() {
		return maxExtract;
	}

	public void setReceiveOnly() {
		canExtract = false;
	}/*

	@Override
	public void read(CompoundTag nbt) {
		setEnergyStored(nbt.getInt("Energy"));
	}

	@Override
	public CompoundTag write(CompoundTag nbt) {
		nbt.putInt("Energy", energy);
		return nbt;
	}*/

	public int getMaxEnergyReceived() {
		return this.maxReceive;
	}

	/**
	 * Drains an amount of energy, due to decay from lack of work or other factors
	 */
	public void drainEnergy(int amount) {
		setEnergyStored(energy - amount);
	}

	public void addEnergy(int amount) {
		setEnergyStored(energy + amount);
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergyStored(int energyStored) {
		this.energy = energyStored;
		if (this.energy > capacity) {
			this.energy = capacity;
		} else if (this.energy < 0) {
			this.energy = 0;
		}
	}

	public <T> LazyOptional<T> getCapability(Capability<T> capability) {
		if (capability == ForgeCapabilities.ENERGY) {
            //IEnergyStorage energyStorage = new EnergyStorageWrapper(this, canExtract);
            return LazyOptional.of(() -> this).cast();
        }

		return LazyOptional.empty();
	}
}
