package weather2;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import weather2.util.WeatherUtilConfig;
import CoroUtil.packet.INBTPacketHandler;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.CoroUtilEntity;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandlerPacket {
	
	//if im going to load nbt, i probably should package it at the VERY end of the packet so it loads properly
	//does .payload continue from where i last read or is it whole thing?
	//maybe i should just do nbt only
	
	//changes from 1.6.4 to 1.7.2:
	//all nbt now:
	//- inv writes stack to nbt, dont use buffer
	//- any sending code needs a full reverification that it matches up with how its received in this class
	//- READ ABOVE ^
	//- CoroAI_Inv could be factored out and replaced with CoroAI_Ent, epoch entities use it this way
	
	@SubscribeEvent
	public void onPacketFromServer(FMLNetworkEvent.ClientCustomPacketEvent event) {
		
		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.packet.payload());
			
			String packetCommand = nbt.getString("packetCommand");
			
			//System.out.println("Weather2 packet command from server: " + packetCommand);
			
			if (packetCommand.equals("WeatherData")) {
				ClientTickHandler.checkClientWeather();
	        	
	        	ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
			} else if (packetCommand.equals("EZGuiData")) {
				String command = nbt.getString("command");
				Weather.dbg("receiving GUI data for client, command: " + command);
				if (command.equals("syncUpdate")) {
					
	        		WeatherUtilConfig.nbtReceiveServerDataForCache(nbt);
	        	}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	@SubscribeEvent
	public void onPacketFromClient(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayerMP entP = ((NetHandlerPlayServer)event.handler).playerEntity;
		
		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.packet.payload());
			
			String packetCommand = nbt.getString("packetCommand");
			
			Weather.dbg("Weather2 packet command from client: " + packetCommand);
			
			if (packetCommand.equals("EZGuiData")) {
				String command = nbt.getString("command");
	        	
	        	Weather.dbg("packet handling command: " + command);
	        	
	        	if (command.equals("syncRequest")) {
	        		Weather.dbg("EZGUI syncRequest");
	        		NBTTagCompound sendNBT = new NBTTagCompound();
	        		sendNBT.setString("packetCommand", "EZGuiData");
	        		sendNBT.setString("command", "syncUpdate");
	        		sendNBT.setBoolean("markUpdated", true);
	        		sendNBT.setBoolean("isPlayerOP", MinecraftServer.getServer().isSinglePlayer() || MinecraftServer.getServer().getConfigurationManager().func_152596_g(entP.getGameProfile()));
	        		sendNBT.setTag("data", WeatherUtilConfig.nbtServerData);
	        		sendNBT.setTag("dimListing", WeatherUtilConfig.createNBTDimensionListing());
	        		
	        		Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(sendNBT, Weather.eventChannelName), entP);
	        		//PacketDispatcher.sendPacketToPlayer(WeatherPacketHelper.createPacketForServerToClientSerialization("EZGuiData", sendNBT), player);
	        	} else if (command.equals("applySettings")) {
	        		if (MinecraftServer.getServer().isSinglePlayer() || MinecraftServer.getServer().getConfigurationManager().func_152596_g(entP.getGameProfile())) {
	        			WeatherUtilConfig.nbtReceiveClientData(nbt.getCompoundTag("guiData"));
	        		}
	        	}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
    
    @SideOnly(Side.CLIENT)
    public String getSelfUsername() {
    	return CoroUtilEntity.getName(Minecraft.getMinecraft().thePlayer);
    }
	
}
