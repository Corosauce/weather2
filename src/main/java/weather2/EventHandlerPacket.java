package weather2;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
			final CompoundNBT nbt = PacketHelper.readNBTTagCompound(event.getPacket().payload());
			
			final String packetCommand = nbt.getString("packetCommand");
			final String command = nbt.getString("command");
			
			//System.out.println("Weather2 packet command from server: " + packetCommand);
			Minecraft.getInstance().execute(() -> {
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
                        ClientTickHandler.clientConfigData.read(nbt);
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
		final ServerPlayerEntity entP = ((ServerPlayNetHandler)event.getHandler()).player;
		
		try {
			final CompoundNBT nbt = PacketHelper.readNBTTagCompound(event.getPacket().payload());

			final String packetCommand = nbt.getString("packetCommand");
			final String command = nbt.getString("command");
			
			Weather.dbg("Weather2 packet command from client: " + packetCommand + " - " + command);

			entP.server.execute(() -> {
                if (packetCommand.equals("WeatherData")) {

                    if (command.equals("syncFull")) {
                        ServerTickHandler.playerClientRequestsFullSync(entP);
                    }

                } else if (packetCommand.equals("EZGuiData")) {


                    Weather.dbg("packet handling command: " + command);

                    if (command.equals("syncRequest")) {
                        Weather.dbg("EZGUI syncRequest");
                        CompoundNBT sendNBT = new CompoundNBT();
                        sendNBT.putString("packetCommand", "EZGuiData");
                        sendNBT.putString("command", "syncUpdate");
                        sendNBT.putBoolean("markUpdated", true);
                        sendNBT.putBoolean("isPlayerOP", FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() || FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(entP.getGameProfile()));
                        sendNBT.put("data", WeatherUtilConfig.nbtServerData);
                        sendNBT.put("dimListing", WeatherUtilConfig.createNBTDimensionListing());

                        Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(sendNBT, Weather.eventChannelName), entP);
                        //PacketDispatcher.sendPacketToPlayer(WeatherPacketHelper.createPacketForServerToClientSerialization("EZGuiData", sendNBT), player);
                    } else if (command.equals("applySettings")) {
                        if (FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer() || FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(entP.getGameProfile())) {
                            WeatherUtilConfig.nbtReceiveClientData(nbt.getCompound("guiData"));
                        }
                    }
                }
            });
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
	}
    
    @OnlyIn(Dist.CLIENT)
    public String getSelfUsername() {
    	return CoroUtilEntity.getName(Minecraft.getInstance().player);
    }
	
}
