package weather2;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketNBTFromServer {
    private final CompoundNBT nbt;

    public PacketNBTFromServer(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    public static void encode(PacketNBTFromServer msg, PacketBuffer buffer) {
        buffer.writeCompoundTag(msg.nbt);
    }

    public static PacketNBTFromServer decode(PacketBuffer buffer) {
        return new PacketNBTFromServer(buffer.readCompoundTag());
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
                    CompoundNBT nbt = msg.nbt;

                    String packetCommand = nbt.getString("packetCommand");
                    String command = nbt.getString("command");

                    //System.out.println("Weather2 packet command from server: " + packetCommand);
                    if (packetCommand.equals("WeatherData")) {
                        ClientTickHandler.checkClientWeather();

                        //this line still gets NPE's despite it checking if its null right before it, wtf
                        ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            ctx.get().setPacketHandled(true);
        }
    }
}
