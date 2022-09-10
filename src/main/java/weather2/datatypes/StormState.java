package weather2.datatypes;

import net.minecraft.network.FriendlyByteBuf;

public final class StormState {
	private final int buildupTickRate;
	private final int maxStackable;

	public StormState(int buildupTickRate, int maxStackable) {
		this.buildupTickRate = buildupTickRate;
		this.maxStackable = maxStackable;
	}

	public int getBuildupTickRate() {
		return buildupTickRate;
	}

	public int getMaxStackable() {
		return maxStackable;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeVarInt(this.buildupTickRate);
		buffer.writeVarInt(this.maxStackable);
	}

	public static StormState decode(FriendlyByteBuf buffer) {
		int buildupTickRate = buffer.readVarInt();
		int maxStackable = buffer.readVarInt();
		return new StormState(buildupTickRate, maxStackable);
	}
}
