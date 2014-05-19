package weather2;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WeatherPacketHandler implements IPacketHandler
{
    public WeatherPacketHandler()
    {
    	
    }

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
    {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
        Side side = FMLCommonHandler.instance().getEffectiveSide();
        try {
    		if (packet.channel.equals("WeatherData")) {
	        	
	        	//String username = dis.readUTF();
	        	NBTTagCompound nbt = Packet.readNBTTagCompound(dis);
	        	
	        	ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
	        }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    @SideOnly(Side.CLIENT)
    public String getSelfUsername() {
    	return Minecraft.getMinecraft().thePlayer.username;
    }
    
}
