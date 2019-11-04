package weather2;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import weather2.util.WeatherUtilConfig;

import java.util.function.Supplier;

public class PacketNBTFromClient {
    private final CompoundNBT nbt;

    public PacketNBTFromClient(CompoundNBT nbt) {
        this.nbt = nbt;
    }

    public static void encode(PacketNBTFromClient msg, PacketBuffer buffer) {
        buffer.writeCompoundTag(msg.nbt);
    }

    public static PacketNBTFromClient decode(PacketBuffer buffer) {
        return new PacketNBTFromClient(buffer.readCompoundTag());
    }

    public static class Handler {
        public static void handle(final PacketNBTFromClient msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayerEntity playerEntity = ctx.get().getSender();
            if( playerEntity == null ) {
                ctx.get().setPacketHandled(true);
                return;
            }

            ctx.get().enqueueWork(() -> {
                try {
                    CompoundNBT nbt = msg.nbt;

                    String packetCommand = nbt.getString("packetCommand");
                    String command = nbt.getString("command");

                    Weather.dbg("Weather2 packet command from client: " + packetCommand + " - " + command);

                    if (packetCommand.equals("WeatherData")) {

                        if (command.equals("syncFull")) {
                            ServerTickHandler.playerClientRequestsFullSync(playerEntity);
                        }

                    } else if (packetCommand.equals("EZGuiData")) {


                        Weather.dbg("packet handling command: " + command);

                        if (command.equals("syncRequest")) {
                            Weather.dbg("EZGUI syncRequest");
                            CompoundNBT sendNBT = new CompoundNBT();
                            sendNBT.putString("packetCommand", "EZGuiData");
                            sendNBT.putString("command", "syncUpdate");
                            sendNBT.putBoolean("markUpdated", true);
                            sendNBT.putBoolean("isPlayerOP", ServerLifecycleHooks.getCurrentServer().isSinglePlayer() || ServerLifecycleHooks.getCurrentServer().getPlayerList().canSendCommands(playerEntity.getGameProfile()));
                            sendNBT.put("data", WeatherUtilConfig.nbtServerData);
                            sendNBT.put("dimListing", WeatherUtilConfig.createNBTDimensionListing());

                            //Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(sendNBT, Weather.eventChannelName), playerEntity);
                            WeatherNetworking.HANDLER.sendToServer(new PacketNBTFromClient(sendNBT));
                        } else if (command.equals("applySettings")) {
                            if (ServerLifecycleHooks.getCurrentServer().isSinglePlayer() || ServerLifecycleHooks.getCurrentServer().getPlayerList().canSendCommands(playerEntity.getGameProfile())) {
                                WeatherUtilConfig.nbtReceiveClientData(nbt.getCompound("guiData"));
                            }
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                /*ItemStack heldItem = GadgetCopyPaste.getGadget(playerEntity);
                if (heldItem.isEmpty()) return;

                BlockPos startPos = msg.start;
                BlockPos endPos = msg.end;
                if (startPos.equals(BlockPos.ZERO) && endPos.equals(BlockPos.ZERO)) {
                    GadgetCopyPaste.setSelectedRegion(heldItem, null);
                    playerEntity.sendStatusMessage(MessageTranslation.AREA_RESET.componentTranslation().setStyle(Styles.AQUA), true);
                } else {
                    GadgetCopyPaste.setSelectedRegion(heldItem, new Region(startPos, endPos));
                }*/
            });

            ctx.get().setPacketHandled(true);
        }
    }
}