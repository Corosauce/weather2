package weather2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public record PacketNBTFromServer(CompoundTag nbt) implements PacketBase
{
    public static final CustomPacketPayload.Type<PacketNBTFromServer> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Weather.MODID, "nbt_client"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketNBTFromServer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, PacketNBTFromServer::nbt,
            PacketNBTFromServer::new);

    public PacketNBTFromServer(FriendlyByteBuf buf)
    {
        this(buf.readNbt());
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeNbt(nbt);
    }

    public void handle(Player player)
    {

        try {
            String packetCommand = nbt.getString("packetCommand");
            String command = nbt.getString("command");

            //System.out.println("Weather2 packet command from server: " + packetCommand);
            if (packetCommand.equals("WeatherData")) {
                ClientTickHandler.getClientWeather();

                //this line still gets NPE's despite it checking if its null right before it, wtf
                ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
            } else if (packetCommand.equals("ClientConfigData")) {
                if (command.equals("syncUpdate")) {
                    ClientTickHandler.clientConfigData.readNBT(nbt);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

	/*@Override
	public ResourceLocation id() {
		return WatutMod.PACKET_ID_NBT_FROM_SERVER;
	}*/

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
