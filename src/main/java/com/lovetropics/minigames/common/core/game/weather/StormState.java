package com.lovetropics.minigames.common.core.game.weather;

import net.minecraft.network.FriendlyByteBuf;

public record StormState(int buildupTickRate, int maxStackable) {
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
