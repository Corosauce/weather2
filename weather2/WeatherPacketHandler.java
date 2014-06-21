package weather2;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import weather2.util.WeatherUtilConfig;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
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
	        	
	        	//race condition fix
	        	if (side == Side.CLIENT) {
	        		checkClientWeather();
	        	}
	        	
	        	ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
	        } else if (packet.channel.equals("EZGuiData")) {
	        	NBTTagCompound nbt = Packet.readNBTTagCompound(dis);
	        	
	        	String command = nbt.getString("command");
	        	
	        	Weather.dbg("packet handling command: " + command);
	        	
	        	if (side == Side.SERVER) {
		        	if (command.equals("syncRequest")) {
		        		Weather.dbg("EZGUI syncRequest");
		        		NBTTagCompound sendNBT = new NBTTagCompound();
		        		sendNBT.setString("command", "syncUpdate");
		        		sendNBT.setBoolean("markUpdated", true);
		        		sendNBT.setBoolean("isPlayerOP", MinecraftServer.getServer().isSinglePlayer() || MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(((EntityPlayer)player).username));
		        		sendNBT.setCompoundTag("data", WeatherUtilConfig.nbtServerData);
		        		PacketDispatcher.sendPacketToPlayer(WeatherPacketHelper.createPacketForServerToClientSerialization("EZGuiData", sendNBT), player);
		        	} else if (command.equals("applySettings")) {
		        		if (MinecraftServer.getServer().isSinglePlayer() || MinecraftServer.getServer().getConfigurationManager().isPlayerOpped(((EntityPlayer)player).username)) {
		        			WeatherUtilConfig.nbtReceiveClientData(nbt.getCompoundTag("guiData"));
		        		}
		        	}
	        	} else {
	        		if (command.equals("syncUpdate")) {
		        		WeatherUtilConfig.nbtReceiveServerDataForCache(nbt);
		        	}
	        	}
	        }
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void checkClientWeather() {
    	try {
    		if (ClientTickHandler.weatherManager == null) {
    			ClientTickHandler.init(FMLClientHandler.instance().getClient().theWorld);
        	}
    	} catch (Exception ex) {
    		Weather.dbg("Warning, Weather2 client received packet before it was ready to use, and failed to init client weather due to null world");
    	}
    }
    
    @SideOnly(Side.CLIENT)
    public String getSelfUsername() {
    	return Minecraft.getMinecraft().thePlayer.username;
    }
    
}
