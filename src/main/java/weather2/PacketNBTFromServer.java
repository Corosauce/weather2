package weather2;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import weather2.util.WeatherUtilConfig;

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
                    } else if (packetCommand.equals("EZGuiData")) {

                        Weather.dbg("receiving GUI data for client, command: " + command);
                        if (command.equals("syncUpdate")) {

                            WeatherUtilConfig.nbtReceiveServerDataForCache(nbt);
                        }
                    //TODO: 1.14 uncomment
                    /*} else if (packetCommand.equals("PocketSandData")) {
                        if (command.equals("create")) {
                            ItemPocketSand.particulateFromServer(nbt.getString("uuid"));
                        }*/
                    } else if (packetCommand.equals("ClientConfigData")) {
                        if (command.equals("syncUpdate")) {
                            ClientTickHandler.clientConfigData.read(nbt);
                            //ItemPocketSand.particulateFromServer(nbt.getString("playerName"));
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