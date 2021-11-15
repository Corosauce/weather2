package weather2;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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