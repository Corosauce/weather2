package weather2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketNBTFromClient {
    private final CompoundTag nbt;

    public PacketNBTFromClient(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public static void encode(PacketNBTFromClient msg, FriendlyByteBuf buffer) {
        buffer.writeNbt(msg.nbt);
    }

    public static PacketNBTFromClient decode(FriendlyByteBuf buffer) {
        return new PacketNBTFromClient(buffer.readNbt());
    }

    public static class Handler {
        public static void handle(final PacketNBTFromClient msg, Supplier<NetworkEvent.Context> ctx) {
            ServerPlayer playerEntity = ctx.get().getSender();
            if( playerEntity == null ) {
                ctx.get().setPacketHandled(true);
                return;
            }

            ctx.get().enqueueWork(() -> {
                try {
                    CompoundTag nbt = msg.nbt;

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