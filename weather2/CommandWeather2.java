package weather2;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Vec3;
import weather2.volcano.VolcanoObject;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

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
							so.levelTemperature = 0.1F;
							so.pos = Vec3.createVectorHelper(player.posX, StormObject.layers.get(so.layer), player.posZ);
							if (var2[2].equals("rain")) {
								so.levelWater = so.levelWaterStartRaining * 2;
							} else if (var2[2].equalsIgnoreCase("thunder")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 1.9F;
								so.levelStormIntensityCur = 1F;
							} else if (var2[2].equalsIgnoreCase("hail")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 3.9F;
								so.levelStormIntensityCur = 3F;
								so.attrib_precipitation = true;
								so.state = StormObject.STATE_HAIL;
							} else if (var2[2].equalsIgnoreCase("F5")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 9.9F;
								so.levelStormIntensityCur = 5F;
								so.attrib_precipitation = true;
								so.state = StormObject.STATE_SPINNING;
								
								so.initRealStorm(null, null);
							} else if (var2[2].equalsIgnoreCase("F4")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 8.9F;
								so.levelStormIntensityCur = 5F;
								so.attrib_precipitation = true;
								so.state = StormObject.STATE_SPINNING;
								
								so.initRealStorm(null, null);
							} else if (var2[2].equalsIgnoreCase("F3")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 7.9F;
								so.levelStormIntensityCur = 5F;
								so.attrib_precipitation = true;
								so.state = StormObject.STATE_SPINNING;
								
								so.initRealStorm(null, null);
							} else if (var2[2].equalsIgnoreCase("F2")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 6.9F;
								so.levelStormIntensityCur = 5F;
								so.attrib_precipitation = true;
								so.state = StormObject.STATE_SPINNING;
								
								so.initRealStorm(null, null);
							} else if (var2[2].equalsIgnoreCase("F1")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 5.9F;
								so.levelStormIntensityCur = 5F;
								so.attrib_precipitation = true;
								so.state = StormObject.STATE_SPINNING;
								
								so.initRealStorm(null, null);
							} else if (var2[2].equalsIgnoreCase("full")) {
								so.levelWater = so.levelWaterStartRaining * 2;
								so.isRealStorm = true;
								so.levelStormIntensityMax = 10F;
								so.levelStormIntensityCur = 0F;
								//so.attrib_rain = true;
								so.state = StormObject.STATE_NORMAL;
								
								so.initRealStorm(null, null);
							} else if (var2[2].equalsIgnoreCase("test")) {
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
			var1.sendChatToPlayer(new ChatMessageComponent().addText(helpMsgStorm));
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
