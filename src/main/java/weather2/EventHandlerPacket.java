package weather2;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.item.ItemPocketSand;
import weather2.util.WeatherUtilConfig;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.CoroUtilEntity;

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
			final NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.getPacket().payload());
			
			final String packetCommand = nbt.getString("packetCommand");
			final String command = nbt.getString("command");
			
			//System.out.println("Weather2 packet command from server: " + packetCommand);
			Minecraft.getMinecraft().addScheduledTask(() -> {
                if (packetCommand.equals("WeatherData")) {
                    ClientTickHandler.checkClientWeather();

                    //this line still gets NPE's despite it checking if its null right before it, wtf
                    ClientTickHandler.weatherManager.nbtSyncFromServer(nbt);
                } else if (packetCommand.equals("EZGuiData")) {

                    Weather.dbg("receiving GUI data for client, command: " + command);
                    if (command.equals("syncUpdate")) {

                        WeatherUtilConfig.nbtReceiveServerDataForCache(nbt);
                    }
                } else if (packetCommand.equals("PocketSandData")) {
                    if (command.equals("create")) {
                        ItemPocketSand.particulateFromServer(nbt.getString("playerName"));
                    }
                } else if (packetCommand.equals("ClientConfigData")) {
                    if (command.equals("syncUpdate")) {
                        ClientTickHandler.clientConfigData.readNBT(nbt);
                        //ItemPocketSand.particulateFromServer(nbt.getString("playerName"));
                    }
                }
            });
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	@SubscribeEvent
	public void onPacketFromClient(FMLNetworkEvent.ServerCustomPacketEvent event) {
		final EntityPlayerMP entP = ((NetHandlerPlayServer)event.getHandler()).player;
		
		try {
			final NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.getPacket().payload());

			final String packetCommand = nbt.getString("packetCommand");
			final String command = nbt.getString("command");
			
			Weather.dbg("Weather2 packet command from client: " + packetCommand + " - " + command);

			entP.mcServer.addScheduledTask(() -> {
                if (packetCommand.equals("WeatherData")) {

                    if (command.equals("syncFull")) {
                        ServerTickHandler.playerClientRequestsFullSync(entP);
                    }

                } else if (packetCommand.equals("EZGuiData")) {


                    Weather.dbg("packet handling command: " + command);

                    if (command.equals("syncRequest")) {
                        Weather.dbg("EZGUI syncRequest");
                        NBTTagCompound sendNBT = new NBTTagCompound();
                        sendNBT.setString("packetCommand", "EZGuiData");
                        sendNBT.setString("command", "syncUpdate");
                        sendNBT.setBoolean("markUpdated", true);
                        sendNBT.setBoolean("isPlayerOP", FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() || FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(entP.getGameProfile()));
                        sendNBT.setTag("data", WeatherUtilConfig.nbtServerData);
                        sendNBT.setTag("dimListing", WeatherUtilConfig.createNBTDimensionListing());

                        Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(sendNBT, Weather.eventChannelName), entP);
                        //PacketDispatcher.sendPacketToPlayer(WeatherPacketHelper.createPacketForServerToClientSerialization("EZGuiData", sendNBT), player);
                    } else if (command.equals("applySettings")) {
                        if (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() || FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(entP.getGameProfile())) {
                            WeatherUtilConfig.nbtReceiveClientData(nbt.getCompoundTag("guiData"));
                        }
                    }
                }
            });
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
    
    @SideOnly(Side.CLIENT)
    public String getSelfUsername() {
    	return CoroUtilEntity.getName(Minecraft.getMinecraft().player);
    }
	
}
