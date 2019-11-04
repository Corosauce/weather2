package weather2;

import CoroUtil.util.Vec3;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

import java.util.List;
import java.util.Random;

import static net.minecraft.command.Commands.literal;

public class CommandWeather2 {

	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		//a history of not what to do
		/*dispatcher.register(
				literal("weather2")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("arg", StringArgumentType.word().greedyString())
							.then(Commands.argument("arg2", StringArgumentType.word())
									.then(Commands.argument("arg3", StringArgumentType.word())
											.executes(c -> perform(c))
									).executes(c -> perform(c))
							).executes(c -> perform(c))
					)
		);*/

		//greedy strings for backwards compat
		dispatcher.register(
				literal("weather2")
						.requires(s -> s.hasPermissionLevel(2))
						.then(Commands.argument("arg", StringArgumentType.greedyString())
								.executes(c -> perform(c))
						).executes(c -> perform(c))
		);
	}

	private static int perform(final CommandContext<CommandSource> cc) {

		String[] vars = cc.getInput().split(" ");
		String[] var2 = new String[vars.length -1];
		//skip first argument being 'weather2'
		for (int i = 0; i < var2.length; i++) {
			var2[i] = vars[i+1];
		}
		CommandSource source = cc.getSource();
		
		if (source.getEntity() != null) {
			
		}
		
		if (source.getEntity().getType() != EntityType.PLAYER) {
			source.sendErrorMessage(new StringTextComponent("Cannot teleport non-players!"));
		}

		String helpMsgStorm = "Syntax: storm create <rain/thunder/wind/spout/hail/F0/F1/F2/F3/F4/F5/C0/C1/C2/C3/C4/C5/hurricane> <Optional: alwaysProgress>... example: storm create F1 alwaysProgress ... eg2: storm killall";

		PlayerEntity player = null;
		if (source.getEntity() instanceof PlayerEntity) {
			player = (PlayerEntity) source.getEntity();
		}
		World world = source.getWorld();
		int dimension = world.getDimension().getType().getId();
		BlockPos posBlock = new BlockPos(source.getPos());
		Vec3d posVec = source.getPos();


		try {
			/*if (var2[0].equals("testLightning")) {
				Random rand = new Random();
				EntityLightningBolt ent = new EntityLightningBolt(world, posBlock.getX() + rand.nextInt(2) -  + rand.nextInt(2)
						, posBlock.getY(), posBlock.getZ() + rand.nextInt(2) -  + rand.nextInt(2));
				WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
				wm.getWorld().weatherEffects.add(ent);
				wm.syncLightningNew(ent, false);
				sendCommandSenderMsg(source, "spawned lightning");
			} else */if (var2[0].equals("storm")) {
				if (var2[1].equalsIgnoreCase("killAll")) {
					WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
					sendCommandSenderMsg(source, "killing all storms");
					List<WeatherObject> listStorms = wm.getStormObjects();
					for (int i = 0; i < listStorms.size(); i++) {
						WeatherObject wo = listStorms.get(i);
						if (wo instanceof WeatherObject) {
							WeatherObject so = wo;
							Weather.dbg("force killing storm ID: " + so.ID);
							so.remove();
						}
					}
				} else if (var2[1].equalsIgnoreCase("killDeadly")) {
					WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
					sendCommandSenderMsg(source, "killing all deadly storms");
					List<WeatherObject> listStorms = wm.getStormObjects();
					for (int i = 0; i < listStorms.size(); i++) {
						WeatherObject wo = listStorms.get(i);
						if (wo instanceof StormObject) {
							StormObject so = (StormObject)wo;
							if (so.levelCurIntensityStage >= StormObject.STATE_THUNDER) {
								Weather.dbg("force killing storm ID: " + so.ID);
								so.remove();
							}
						}
					}
				} else if (var2[1].equalsIgnoreCase("killRain") || var2[1].equalsIgnoreCase("killStorm")) {
					WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(dimension);
					sendCommandSenderMsg(source, "killing all raining or deadly storms");
					List<WeatherObject> listStorms = wm.getStormObjects();
					for (int i = 0; i < listStorms.size(); i++) {
						WeatherObject wo = listStorms.get(i);
						if (wo instanceof StormObject) {
							StormObject so = (StormObject)wo;
							if (so.levelCurIntensityStage >= StormObject.STATE_THUNDER || so.attrib_precipitation) {
								Weather.dbg("force killing storm ID: " + so.ID);
								so.remove();
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
						so.spawnerUUID = player.getCachedUniqueIdString();
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

							Vec3 pos = new Vec3(posVec.x, world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(posVec.x, 0, posVec.z)).getY() + 1, posVec.z);


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
								sendCommandSenderMsg(source, "couldnt find spot to spawn");
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

						sendCommandSenderMsg(source, "storm " + var2[2] + " created" + (so.alwaysProgresses ? ", flags: alwaysProgresses" : ""));
					} else {
						sendCommandSenderMsg(source, helpMsgStorm);
					}
				} else if (var2[1].equals("help")) {
					sendCommandSenderMsg(source, helpMsgStorm);

				} else {
					sendCommandSenderMsg(source, helpMsgStorm);
				}
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
						sendCommandSenderMsg(source, "started high wind event");
					} else if (doHighOff) {
						wm.windMan.stopHighWindEvent();
						sendCommandSenderMsg(source, "stopped high wind event");
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
						sendCommandSenderMsg(source, "started low wind event");
					} else if (doLowOff) {
						wm.windMan.stopLowWindEvent();
						sendCommandSenderMsg(source, "stopped low wind event");
					}
				}
			} else {
				sendCommandSenderMsg(source, helpMsgStorm);
			}
			/*}*/
		} catch (Exception ex) {
			System.out.println("Exception handling Weather2 command");
			sendCommandSenderMsg(source, helpMsgStorm);
			ex.printStackTrace();
		}

		return 1;
	}

	public static void sendCommandSenderMsg(CommandSource entP, String msg) {
		entP.sendErrorMessage(new StringTextComponent(msg));
	}
}
