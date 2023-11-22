package weather2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketNBTFromServer {
    private final CompoundTag nbt;

    public PacketNBTFromServer(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public static void encode(PacketNBTFromServer msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(msg.nbt);
    }

    public static PacketNBTFromServer decode(FriendlyByteBuf buffer) {
        return new PacketNBTFromServer(buffer.readNbt());
    }

    public static class Handler {
        public static void handle(final PacketNBTFromServer msg, Supplier<NetworkEvent.Context> ctx) {
            /*ServerPlayerEntity playerEntity = ctx.get().getSender();
            if( playerEntity == null ) {
                ctx.get().setPacketHandled(true);
                return;
            }*/

            ctx.get().enqueueWork(() -> {
                try {
                    CompoundTag nbt = msg.nbt;

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
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
