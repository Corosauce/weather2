package weather2;

import java.util.List;
import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import weather2.entity.EntityLightningBolt;
import weather2.util.WeatherUtilBlock;
import weather2.volcano.VolcanoObject;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;
import weather2.weathersystem.storm.WeatherObject;
import CoroUtil.util.CoroUtilMisc;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.Vec3;

public class CommandWeather2 extends CommandBase {
	
	@Override
	public String getName() {
		return "weather2";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender var1, String[] var2) {
		
		String helpMsgStorm = "Syntax: storm create <rain/thunder/wind/spout/hail/F0/F1/F2/F3/F4/F5/C0/C1/C2/C3/C4/C5/hurricane> <Optional: alwaysProgress>... example: storm create F1 alwaysProgress ... eg2: storm killall";
		
		EntityPlayer player = null;
		if (var1 instanceof EntityPlayer) {
			player = (EntityPlayer) var1;
		}
		World world = var1.getEntityWorld();
		int dimension = world.provider.getDimension();
		BlockPos posBlock = var1.getPosition();
		Vec3d posVec = var1.getPositionVector();
		
		try {
			/*if(var1 instanceof EntityPlayerMP)
			{*/
				//EntityPlayer player = getCommandSenderAsPlayer(var1);
				
				if (var2[0].equals("volcano")) {
					if (var2[1].equals("create") && posVec != Vec3d.ZERO) {
						if (dimension == 0) {
							WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(0);
							VolcanoObject vo = new VolcanoObject(wm);
							vo.pos = new Vec3(posVec);
							vo.initFirstTime();
							wm.addVolcanoObject(vo);
							vo.initPost();
							
							wm.syncVolcanoNew(vo);
							
							sendCommandSenderMsg(var1, "volcano created");
						} else {
							sendCommandSenderMsg(var1, "can only make volcanos on main overworld");
						}
					}
				} else if (var2[0].equals("testLightning")) {
					Random rand = new Random();
					EntityLightningBolt ent = new EntityLightningBolt(world, posBlock.getX() + rand.nextInt(2) -  + rand.nextInt(2)
							, posBlock.getY(), posBlock.getZ() + rand.nextInt(2) -  + rand.nextInt(2));
					WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
					wm.getWorld().weatherEffects.add(ent);
					wm.syncLightningNew(ent, false);
					sendCommandSenderMsg(var1, "spawned lightning");
				} else if (var2[0].equals("storm")) {
					if (var2[1].equalsIgnoreCase("killAll")) {
						WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
						sendCommandSenderMsg(var1, "killing all storms");
						List<WeatherObject> listStorms = wm.getStormObjects();
						for (int i = 0; i < listStorms.size(); i++) {
							WeatherObject wo = listStorms.get(i);
							if (wo instanceof WeatherObject) {
								WeatherObject so = wo;
								Weather.dbg("force killing storm ID: " + so.ID);
								so.setDead();
							}
						}
					} else if (var2[1].equalsIgnoreCase("killDeadly")) {
						WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
						sendCommandSenderMsg(var1, "killing all deadly storms");
						List<WeatherObject> listStorms = wm.getStormObjects();
						for (int i = 0; i < listStorms.size(); i++) {
							WeatherObject wo = listStorms.get(i);
							if (wo instanceof StormObject) {
								StormObject so = (StormObject)wo;
								if (so.levelCurIntensityStage >= StormObject.STATE_THUNDER) {
									Weather.dbg("force killing storm ID: " + so.ID);
									so.setDead();
								}
							}
						}
					} else if (var2[1].equalsIgnoreCase("killRain") || var2[1].equalsIgnoreCase("killStorm")) {
						WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
						sendCommandSenderMsg(var1, "killing all raining or deadly storms");
						List<WeatherObject> listStorms = wm.getStormObjects();
						for (int i = 0; i < listStorms.size(); i++) {
							WeatherObject wo = listStorms.get(i);
							if (wo instanceof StormObject) {
								StormObject so = (StormObject)wo;
								if (so.levelCurIntensityStage >= StormObject.STATE_THUNDER || so.attrib_precipitation) {
									Weather.dbg("force killing storm ID: " + so.ID);
									so.setDead();
								}
							}
						}
					} else if (var2[1].equals("create") || var2[1].equals("spawn")) {
						if (var2.length > 2 && posVec != Vec3d.ZERO) {
							//TODO: make this handle non StormObject types better, currently makes instance and doesnt use that type if its a sandstorm
							boolean spawnCloudStorm = true;
							WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
							StormObject so = new StormObject(wm);
							so.layer = 0;
							so.userSpawnedFor = CoroUtilEntity.getName(player);
							so.naturallySpawned = false;
							so.levelTemperature = 0.1F;
							so.pos = new Vec3(posVec.x, StormObject.layers.get(so.layer), posVec.z);

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
							} else if (var2[2].equalsIgnoreCase("firenado")) {
								so.levelCurIntensityStage = StormObject.STATE_STAGE1;
								so.isFirenado = true;
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
							} else if (var2[2].equalsIgnoreCase("sandstormUpwind")) {
								
								WeatherObjectSandstorm sandstorm = new WeatherObjectSandstorm(wm);
								
								//sandstorm.pos = new Vec3(player.posX, player.world.getHeight(new BlockPos(player.posX, 0, player.posZ)).getY() + 1, player.posZ);

								Vec3 pos = new Vec3(posVec.x, world.getHeight(new BlockPos(posVec.x, 0, posVec.z)).getY() + 1, posVec.z);

								
								/**
								 * adjust position upwind 150 blocks
								 */
								float angle = wm.getWindManager().getWindAngleForClouds();
								double vecX = -Math.sin(Math.toRadians(angle));
								double vecZ = Math.cos(Math.toRadians(angle));
								double speed = 150D;
								pos.xCoord -= vecX * speed;
								pos.zCoord -= vecZ * speed;
								
								sandstorm.initFirstTime();
								sandstorm.initSandstormSpawn(pos);

								
								wm.addStormObject(sandstorm);
								wm.syncStormNew(sandstorm);
								spawnCloudStorm = false;

								wm.windMan.startHighWindEvent();
								wm.windMan.lowWindTimer = 0;
								
							} else if (var2[2].equalsIgnoreCase("sandstorm")) {
								boolean spawned = wm.trySpawnSandstormNearPos(world, new Vec3(posVec));
								spawnCloudStorm = false;
								if (!spawned) {
									sendCommandSenderMsg(var1, "couldnt find spot to spawn");
									return;
								} else {
									wm.windMan.startHighWindEvent();
									wm.windMan.lowWindTimer = 0;
								}
							}
							
							if (var2.length > 3) {
								if (var2[3].contains("Progress") || var2[3].contains("progress")) {
									so.alwaysProgresses = true;
								}
							}
							
							if (spawnCloudStorm) {
								so.initFirstTime();

								//lock it to current stage or less
								so.levelStormIntensityMax = so.levelCurIntensityStage;

								wm.addStormObject(so);
								wm.syncStormNew(so);
							}

							sendCommandSenderMsg(var1, "storm " + var2[2] + " created" + (so.alwaysProgresses ? ", flags: alwaysProgresses" : ""));
						} else {
							sendCommandSenderMsg(var1, helpMsgStorm);
						}
					} else if (var2[1].equals("help")) {
						sendCommandSenderMsg(var1, helpMsgStorm);
						
					} else {
						sendCommandSenderMsg(var1, helpMsgStorm);
					}
				} else if (var2[0].equals("testderp") && player != null) {
					//EntityPlayerMP player = var1;
					WeatherUtilBlock.floodAreaWithLayerableBlock(player.world, new Vec3(player.posX, player.posY, player.posZ), player.rotationYawHead, 1, 1, CommonProxy.blockSandLayer, 30);
				} else if (var2[0].equals("wind")) {
					if (var2[1].equals("high")) {
						boolean doHighOn = false;
						boolean doHighOff = false;
						if (var2.length > 2) {
							 if (var2[2].equals("start")) {
								 doHighOn = true;
							 } else if (var2[2].equals("stop")) {
								 doHighOff = true;
							 }
						} else {
							doHighOn = true;
						}
						WeatherManagerServer wm = ServerTickHandler.getWeatherSystemForDim(dimension);
						if (doHighOn) {
							wm.windMan.startHighWindEvent();
							//cancel any low wind state if there is one
							wm.windMan.lowWindTimer = 0;
							sendCommandSenderMsg(var1, "started high wind event");
						} else if (doHighOff) {
							wm.windMan.stopHighWindEvent();
							sendCommandSenderMsg(var1, "stopped high wind event");
						}
					} else if (var2[1].equals("low")) {
						boolean doLowOn = false;
						boolean doLowOff = false;
						if (var2.length > 2) {
							 if (var2[2].equals("start")) {
								 doLowOn = true;
							 } else if (var2[2].equals("stop")) {
								 doLowOff = true;
							 }
						} else {
							doLowOn = true;
						}
						WeatherManagerServer wm = ServerTickHandler.getWeatherSystemForDim(dimension);
						if (doLowOn) {
							wm.windMan.startLowWindEvent();
							//cancel any high wind state if there is one
							wm.windMan.highWindTimer = 0;
							sendCommandSenderMsg(var1, "started low wind event");
						} else if (doLowOff) {
							wm.windMan.stopLowWindEvent();
							sendCommandSenderMsg(var1, "stopped low wind event");
						}
					}
				} else {
					sendCommandSenderMsg(var1, helpMsgStorm);
				}
			/*}*/
		} catch (Exception ex) {
			System.out.println("Exception handling Weather2 command");
			sendCommandSenderMsg(var1, helpMsgStorm);
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
    }

	@Override
	public String getUsage(ICommandSender icommandsender) {
		return "Magic dev method!";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	public static void sendCommandSenderMsg(ICommandSender entP, String msg) {
		entP.sendMessage(new TextComponentString(msg));
	}

}
