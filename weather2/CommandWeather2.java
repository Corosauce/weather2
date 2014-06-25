package weather2;

import java.util.List;

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
		
		String helpMsgStorm = "Syntax: storm create <rain/thunder/wind/spout/hail/F0/F1/F2/F3/F4/F5/C0/C1/C2/C3/C4/C5/hurricane> <Optional: alwaysProgress>... example: storm create F1 alwaysProgress ... eg2: storm killall";
		
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
					if (var2[1].equalsIgnoreCase("killAll")) {
						WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(player.worldObj.provider.dimensionId);
						var1.sendChatToPlayer(new ChatMessageComponent().addText("killing all storms"));
						List<StormObject> listStorms = wm.getStormObjects();
						for (int i = 0; i < listStorms.size(); i++) {
							StormObject so = listStorms.get(i);
							Weather.dbg("force killing storm ID: " + so.ID);
							so.setDead();
							/*wm.syncStormRemove(so);
							wm.removeStormObject(so.ID);
							*/
						}
					} else if (var2[1].equals("create") || var2[1].equals("spawn")) {
						if (var2.length > 2) {
							WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(player.worldObj.provider.dimensionId);
							StormObject so = new StormObject(wm);
							so.layer = 0;
							so.userSpawnedFor = player.username;
							so.naturallySpawned = false;
							so.levelTemperature = 0.1F;
							so.pos = Vec3.createVectorHelper(player.posX, StormObject.layers.get(so.layer), player.posZ);

							so.levelWater = so.levelWaterStartRaining * 2;
							so.attrib_precipitation = true;
							
							if (!var2[2].equals("rain")) {
								so.initRealStorm(null, null);
							}
							
							if (var2[2].equals("rain")) {
								
							} else if (var2[2].equalsIgnoreCase("thunder") || var2[2].equalsIgnoreCase("lightning")) {
								so.levelCurIntensityStage = StormObject.STATE_THUNDER;
							} else if (var2[2].equalsIgnoreCase("wind")) {
								so.levelCurIntensityStage = StormObject.STATE_HIGHWIND;
							} else if (var2[2].equalsIgnoreCase("spout")) {
								so.levelCurIntensityStage = StormObject.STATE_HIGHWIND;
								so.attrib_waterSpout = true;
							} else if (var2[2].equalsIgnoreCase("hail")) {
								so.levelCurIntensityStage = StormObject.STATE_HAIL;
							} else if (var2[2].equalsIgnoreCase("F5")) {
								so.levelCurIntensityStage = StormObject.STATE_STAGE5;
							} else if (var2[2].equalsIgnoreCase("F4")) {
								so.levelCurIntensityStage = StormObject.STATE_STAGE4;
							} else if (var2[2].equalsIgnoreCase("F3")) {
								so.levelCurIntensityStage = StormObject.STATE_STAGE3;
							} else if (var2[2].equalsIgnoreCase("F2")) {
								so.levelCurIntensityStage = StormObject.STATE_STAGE2;
							} else if (var2[2].equalsIgnoreCase("F1")) {
								so.levelCurIntensityStage = StormObject.STATE_STAGE1;
							} else if (var2[2].equalsIgnoreCase("F0")) {
								so.levelCurIntensityStage = StormObject.STATE_FORMING;
							} else if (var2[2].equalsIgnoreCase("C0")) {
								so.stormType = StormObject.TYPE_WATER;
								so.levelCurIntensityStage = StormObject.STATE_FORMING;
							} else if (var2[2].equalsIgnoreCase("C1")) {
								so.stormType = StormObject.TYPE_WATER;
								so.levelCurIntensityStage = StormObject.STATE_STAGE1;
							} else if (var2[2].equalsIgnoreCase("C2")) {
								so.stormType = StormObject.TYPE_WATER;
								so.levelCurIntensityStage = StormObject.STATE_STAGE2;
							} else if (var2[2].equalsIgnoreCase("C3")) {
								so.stormType = StormObject.TYPE_WATER;
								so.levelCurIntensityStage = StormObject.STATE_STAGE3;
							} else if (var2[2].equalsIgnoreCase("C4")) {
								so.stormType = StormObject.TYPE_WATER;
								so.levelCurIntensityStage = StormObject.STATE_STAGE4;
							} else if (var2[2].equalsIgnoreCase("C5") || var2[2].equalsIgnoreCase("hurricane")) {
								so.stormType = StormObject.TYPE_WATER;
								so.levelCurIntensityStage = StormObject.STATE_STAGE5;
							} else if (var2[2].equalsIgnoreCase("hurricane")) {
								so.stormType = StormObject.TYPE_WATER;
								so.levelCurIntensityStage = StormObject.STATE_STAGE5;
							} else if (var2[2].equalsIgnoreCase("full")) {
								//needs code to somehow guarantee it will build to max stage
								so.levelCurIntensityStage = StormObject.STATE_THUNDER;
								so.alwaysProgresses = true;
							} else if (var2[2].equalsIgnoreCase("test")) {
								so.levelCurIntensityStage = StormObject.STATE_THUNDER;
							}
							
							if (var2.length > 3) {
								if (var2[3].contains("Progress") || var2[3].contains("progress")) {
									so.alwaysProgresses = true;
								}
							}
							
							so.initFirstTime();
							wm.addStormObject(so);
							wm.syncStormNew(so);
							
							var1.sendChatToPlayer(new ChatMessageComponent().addText("storm " + var2[2] + " created" + (so.alwaysProgresses ? ", flags: alwaysProgresses" : "")));
						} else {
							var1.sendChatToPlayer(new ChatMessageComponent().addText(helpMsgStorm));
						}
					} else if (var2[1].equals("help")) {
						var1.sendChatToPlayer(new ChatMessageComponent().addText(helpMsgStorm));
					} else {
						var1.sendChatToPlayer(new ChatMessageComponent().addText(helpMsgStorm));
					}
				} else {
					var1.sendChatToPlayer(new ChatMessageComponent().addText(helpMsgStorm));
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
