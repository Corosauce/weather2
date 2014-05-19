package weather2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import CoroUtil.packet.PacketHelper;

public class WeatherPacketHelper {

	public static Packet250CustomPayload createPacketForServerToClientSerialization(String channel, NBTTagCompound par) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try {
			PacketHelper.writeNBTTagCompound(par, dos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = channel;
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;
		return pkt;
	}
	
}
