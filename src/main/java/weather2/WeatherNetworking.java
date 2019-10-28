package weather2;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class WeatherNetworking {

    private static final String PROTOCOL_VERSION = Integer.toString(3);
    private static short lastID = 0;
    public static final ResourceLocation NETWORK_CHANNEL_ID_MAIN = new ResourceLocation(Weather.MODID, "main");

    public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(NETWORK_CHANNEL_ID_MAIN)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        registerMessage(PacketNBTFromServer.class, PacketNBTFromServer::encode, PacketNBTFromServer::decode, PacketNBTFromServer.Handler::handle);
        registerMessage(PacketNBTFromClient.class, PacketNBTFromClient::encode, PacketNBTFromClient::decode, PacketNBTFromClient.Handler::handle);
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<Context>> messageConsumer) {
        HANDLER.registerMessage(lastID, messageType, encoder, decoder, messageConsumer);
        lastID++;
        if (lastID > 0xFF)
            throw new RuntimeException("Too many messages!");
    }

}

