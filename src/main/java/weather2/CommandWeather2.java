package weather2;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import weather2.volcano.VolcanoObject;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;
import weather2.weathersystem.storm.WeatherObject;
import CoroUtil.util.CoroUtil;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.Vec3;

public class CommandWeather2 extends CommandBase {

	@Override
	public String getCommandName() {
		return "weather2";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender var1, String[] var2) {
		
		String helpMsgStorm = "Syntax: storm create <rain/thunder/wind/spout/hail/F0/F1/F2/F3/F4/F5/C0/C1/C2/C3/C4/C5/hurricane> <Optional: alwaysProgress>... example: storm create F1 alwaysProgress ... eg2: storm killall";
		
		try {
			if(var1 instanceof EntityPlayerMP)
			{
				EntityPlayer player = getCommandSenderAsPlayer(var1);
				
				if (var2[0].equals("volcano")) {
					if (var2[1].equals("create")) {
						if (player.worldObj.provider.getDimension() == 0) {
							WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(0);
							VolcanoObject vo = new VolcanoObject(wm);
							vo.pos = new Vec3(player.posX, player.posY, player.posZ);
							vo.initFirstTime();
							wm.addVolcanoObject(vo);
							vo.initPost();
							
							wm.syncVolcanoNew(vo);
							
							CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, "volcano created");
						} else {
							CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, "can only make volcanos on main overworld");
						}
					}
				} else if (var2[0].equals("storm")) {
					if (var2[1].equalsIgnoreCase("killAll")) {
						WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(player.worldObj.provider.getDimension());
						CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, "killing all storms");
						List<WeatherObject> listStorms = wm.getStormObjects();
						for (int i = 0; i < listStorms.size(); i++) {
							WeatherObject wo = listStorms.get(i);
							if (wo instanceof StormObject) {
								StormObject so = (StormObject) wo;
								Weather.dbg("force killing storm ID: " + so.ID);
								so.setDead();
								/*wm.syncStormRemove(so);
								wm.removeStormObject(so.ID);
								*/
							}
						}
					} else if (var2[1].equals("create") || var2[1].equals("spawn")) {
						if (var2.length > 2) {
							WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(player.worldObj.provider.getDimension());
							StormObject so = new StormObject(wm);
							so.layer = 0;
							so.userSpawnedFor = CoroUtilEntity.getName(player);
							so.naturallySpawned = false;
							so.levelTemperature = 0.1F;
							so.pos = new Vec3(player.posX, StormObject.layers.get(so.layer), player.posZ);

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
							}/* else if (var2[2].equalsIgnoreCase("sandstorm")) {
								so = new WeatherObjectSandstorm(wm);
								so.layer = 0;
								so.userSpawnedFor = CoroUtilEntity.getName(player);
								so.naturallySpawned = false;
								so.levelTemperature = 0F;
								
								so.pos = new Vec3(player.posX, player.worldObj.getHeight(new BlockPos(player.posX, 0, player.posZ)).getY() + 1, player.posZ);

								so.levelWater = 0;
								so.attrib_precipitation = false;
							}*/
							
							if (var2.length > 3) {
								if (var2[3].contains("Progress") || var2[3].contains("progress")) {
									so.alwaysProgresses = true;
								}
							}
							
							so.initFirstTime();
							wm.addStormObject(so);
							wm.syncStormNew(so);
							
							CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, "storm " + var2[2] + " created" + (so.alwaysProgresses ? ", flags: alwaysProgresses" : ""));
						} else {
							CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, helpMsgStorm);
						}
					} else if (var2[1].equals("help")) {
						CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, helpMsgStorm);
					} else {
						CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, helpMsgStorm);
					}
				} else {
					CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, helpMsgStorm);
				}
			}
		} catch (Exception ex) {
			System.out.println("Exception handling Weather2 command");
			CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, helpMsgStorm);
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "Magic dev method!";
	}

}
