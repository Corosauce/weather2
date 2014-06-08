package weather2;

import weather2.volcano.VolcanoObject;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Vec3;

public class CommandWeather2 extends CommandBase {

	@Override
	public String getCommandName() {
		return "weather2";
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		
		String helpMsgStorm = "Syntax: storm create <rain/thunder/hail/F1>... example: storm create F1";
		
		try {
			if(var1 instanceof EntityPlayerMP)
			{
				EntityPlayer player = getCommandSenderAsPlayer(var1);
				
				if (var2[0].equals("volcano")) {
					if (var2[1].equals("create")) {
						if (player.worldObj.provider.dimensionId == 0) {
							WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(0);
							VolcanoObject vo = new VolcanoObject(wm);
							vo.pos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ);
							vo.initFirstTime();
							wm.addVolcanoObject(vo);
							vo.initPost();
							
							wm.syncVolcanoNew(vo);
							
							var1.sendChatToPlayer(new ChatMessageComponent().addText("volcano created"));
						} else {
							var1.sendChatToPlayer(new ChatMessageComponent().addText("can only make volcanos on main overworld"));
						}
					}
				} else if (var2[0].equals("storm")) {
					if (var2[1].equals("create")) {
						if (var2.length > 2) {
							WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(0);
							StormObject so = new StormObject(wm);
							so.layer = 0;
							so.userSpawnedFor = player.username;
							so.naturallySpawned = false;
							so.pos = Vec3.createVectorHelper(player.posX, StormObject.layers.get(so.layer), player.posZ);
							if (var2[2].equals("rain")) {
								so.levelWater = so.levelWaterStartRaining * 2;
							} else if (var2[2].equals("thunder")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 1.9F;
								so.levelStormIntensityCur = 1F;
							} else if (var2[2].equals("hail")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 3.9F;
								so.levelStormIntensityCur = 3F;
							} else if (var2[2].equals("F1")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 5.9F;
								so.levelStormIntensityCur = 5F;
								so.attrib_precipitation = true;
								so.state = StormObject.STATE_SPINNING;
								
								so.initRealStorm(null, null);
							} else if (var2[2].equals("Full")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 10F;
								so.levelStormIntensityCur = 0F;
								//so.attrib_rain = true;
								so.state = StormObject.STATE_NORMAL;
								
								so.initRealStorm(null, null);
							} else if (var2[2].equals("Test")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 10F;
								so.levelStormIntensityCur = 4F;
								so.attrib_precipitation = true;
								so.state = StormObject.STATE_NORMAL;
								
								so.initRealStorm(null, null);
							}
							so.initFirstTime();
							wm.addStormObject(so);
							wm.syncStormNew(so);
							
							var1.sendChatToPlayer(new ChatMessageComponent().addText("storm created"));
						} else {
							var1.sendChatToPlayer(new ChatMessageComponent().addText(helpMsgStorm));
						}
					} else if (var2[1].equals("help")) {
						var1.sendChatToPlayer(new ChatMessageComponent().addText(helpMsgStorm));
					}
				}
			}
		} catch (Exception ex) {
			System.out.println("Exception handling Weather2 command");
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}

}
