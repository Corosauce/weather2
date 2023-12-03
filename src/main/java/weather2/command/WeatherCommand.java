package weather2.command;

import com.corosus.coroutil.util.CoroUtilBlock;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.RangeArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.InterModComms;
import weather2.ServerTickHandler;
import weather2.config.ConfigMisc;
import weather2.config.ConfigWind;
import weather2.config.WeatherUtilConfig;
import weather2.util.WeatherUtil;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class WeatherCommand {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				literal("weather2")
						.then(literal("kill_all_storms").requires(s -> s.hasPermission(2)).executes(c -> {
							WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
							wm.clearAllStorms();
							c.getSource().sendSuccess(() -> Component.literal("Killed all storms"), true);
							return Command.SINGLE_SUCCESS;
						}))
						.then(literal("debug").requires(s -> s.hasPermission(2))
								.then(literal("print_grab_list").executes(c -> {
									WeatherUtil.testAllBlocks();
									c.getSource().sendSuccess(() -> Component.literal("Tornado grab list printed to debug.log"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("storm_chance").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									float chance = wm.getBiomeBasedStormSpawnChanceInArea(CoroUtilBlock.blockPos(c.getSource().getPosition().x, c.getSource().getPosition().y, c.getSource().getPosition().z));

									c.getSource().sendSuccess(() -> Component.literal("Likelyhood of storms to spawn here within 1024 blocks: " + (chance * 100)), true);
									return Command.SINGLE_SUCCESS;
								}))
						)
						.then(literal("wind_event").requires(s -> s.hasPermission(2))
								.then(literal("clear").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									wm.getWindManager().stopLowWindEvent();
									wm.getWindManager().stopHighWindEvent();
									c.getSource().sendSuccess(() -> Component.literal("Stopped any active high or low wind events"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("high").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									wm.getWindManager().stopLowWindEvent();
									wm.getWindManager().startHighWindEvent();
									c.getSource().sendSuccess(() -> Component.literal("Started high wind event"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("low").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									wm.getWindManager().stopHighWindEvent();
									wm.getWindManager().startLowWindEvent();
									wm.getWindManager().windSpeedGlobal = (float) (ConfigWind.windSpeedMin + 0.2F);
									c.getSource().sendSuccess(() -> Component.literal("Started low wind event"), true);
									return Command.SINGLE_SUCCESS;
								}))
						)
						.then(literal("wind_angle").requires(s -> s.hasPermission(2))
								.then(argument("angle", IntegerArgumentType.integer(0, 359)).executes(c -> {
									int angle = IntegerArgumentType.getInteger(c, "angle");
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									wm.getWindManager().windAngleGlobal = angle;
									c.getSource().sendSuccess(() -> Component.literal("Set wind angle for clouds to " + angle), true);
									return Command.SINGLE_SUCCESS;
								}))
						)
						.then(literal("wind_speed").requires(s -> s.hasPermission(2))
								.then(argument("speed", FloatArgumentType.floatArg(0, 1.5F)).executes(c -> {
									float speed = FloatArgumentType.getFloat(c, "speed");
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									wm.getWindManager().windSpeedGlobal = speed;
									c.getSource().sendSuccess(() -> Component.literal("Set wind speed for clouds to " + speed), true);
									return Command.SINGLE_SUCCESS;
								}))
						)
						.then(literal("server_precipitation").requires(s -> s.hasPermission(2))
								.then(argument("amount", FloatArgumentType.floatArg(0, 1.0F)).executes(c -> {
									float amount = FloatArgumentType.getFloat(c, "amount");
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									wm.vanillaRainAmountOnServer = amount;
									if (ConfigMisc.overcastMode) {
										c.getSource().sendSuccess(() -> Component.literal("Server precipitation amount set to " + amount), true);
									} else {
										c.getSource().sendSuccess(() -> Component.literal("overcastMode not on, this will change nothing"), true);
									}
									return Command.SINGLE_SUCCESS;
								}))
						)
						.then(literal("summon").requires(s -> s.hasPermission(2)).requires(s -> WeatherUtilConfig.listDimensionsWeather.contains(s.getLevel().dimension().location().toString()))
								.then(literal("storm_rain").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_NORMAL);

									c.getSource().sendSuccess(() -> Component.literal("Summoned rain storm"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("storm_lightning").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_THUNDER);

									stormObject.initRealStorm(null, null);
									stormObject.levelCurIntensityStage = StormObject.STATE_THUNDER;
									stormObject.levelStormIntensityMax = StormObject.STATE_THUNDER;

									c.getSource().sendSuccess(() -> Component.literal("Summoned lightning storm"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("storm_highwind").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_HIGHWIND);

									stormObject.initRealStorm(null, null);
									stormObject.levelCurIntensityStage = StormObject.STATE_HIGHWIND;
									stormObject.levelStormIntensityMax = StormObject.STATE_HIGHWIND;

									c.getSource().sendSuccess(() -> Component.literal("Summoned highwind storm"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("storm_hail").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_HAIL);

									stormObject.initRealStorm(null, null);
									stormObject.levelCurIntensityStage = StormObject.STATE_HAIL;
									stormObject.levelStormIntensityMax = StormObject.STATE_HAIL;

									c.getSource().sendSuccess(() -> Component.literal("Summoned hail storm"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_f0").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_FORMING);
									stormObject.levelStormIntensityMax = StormObject.STATE_STAGE1;

									c.getSource().sendSuccess(() -> Component.literal("Summoned forming tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_f1").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									c.getSource().sendSuccess(() -> Component.literal("Summoned f1 tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_f2").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE2);

									c.getSource().sendSuccess(() -> Component.literal("Summoned f2 tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_f3").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE3);

									c.getSource().sendSuccess(() -> Component.literal("Summoned f3 tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_f4").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE4);

									c.getSource().sendSuccess(() -> Component.literal("Summoned f4 tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("sharknado").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);
									stormObject.levelStormIntensityMax = StormObject.STATE_STAGE4;

									stormObject.setSharknado(true);

									c.getSource().sendSuccess(() -> Component.literal("Summoned sharknado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("firenado_f0").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_FORMING);
									stormObject.levelStormIntensityMax = StormObject.STATE_STAGE4;
									stormObject.isFirenado = true;

									c.getSource().sendSuccess(() -> Component.literal("Summoned firenado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("firenado_f1").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);
									stormObject.levelStormIntensityMax = StormObject.STATE_STAGE4;
									stormObject.isFirenado = true;

									c.getSource().sendSuccess(() -> Component.literal("Summoned firenado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								/*.then(literal("tornado_player_baby").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(800);
									stormObject.setBaby(true);

									c.getSource().sendSuccess(() -> Component.literal("Summoned baby player tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_baby").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setBaby(true);

									c.getSource().sendSuccess(() -> Component.literal("Summoned baby tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))*/
								.then(literal("tornado_player").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(600);

									c.getSource().sendSuccess(() -> Component.literal("Summoned player tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))/*
								.then(literal("tornado_pet").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(-1);
									stormObject.setPet(true);
									stormObject.setPetGrabsItems(true);

									c.getSource().sendSuccess(() -> Component.literal("Summoned pet tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_pet_no_item_grab").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(-1);
									stormObject.setPet(true);
									stormObject.setPetGrabsItems(false);

									c.getSource().sendSuccess(() -> Component.literal("Summoned pet tornado with no item grabbing"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornadotestimc").executes(c -> {

									InterModComms.sendTo("weather2", "sharknado", () -> {
										CompoundTag tag = new CompoundTag();
										tag.putString("uuid", c.getSource().getEntity().getUUID().toString());
										tag.putInt("time_ticks", 1200);
										tag.putBoolean("baby", false);
										tag.putBoolean("sharknado", true);
										tag.putString("dimension", c.getSource().getEntity().getLevel().dimension().location().toString());
										return tag;
									});

									c.getSource().sendSuccess(() -> Component.literal("Summoned tornado test"), true);
									return Command.SINGLE_SUCCESS;
								}))*/
								.then(literal("tornado_f0_max").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_FORMING);
									stormObject.levelStormIntensityMax = StormObject.STATE_FORMING;
									stormObject.alwaysProgresses = false;

									c.getSource().sendSuccess(() -> Component.literal("Summoned tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("sandstorm_try").executes(c -> {

									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									boolean sandstormMade = wm.trySpawnParticleStormNearPos(c.getSource().getLevel(), c.getSource().getPosition(), WeatherObjectParticleStorm.StormType.SANDSTORM);
									if (sandstormMade) {
										c.getSource().sendSuccess(() -> Component.literal("Summoned sandstorm"), true);
										wm.getWindManager().stopLowWindEvent();
										wm.getWindManager().startHighWindEvent();
									} else {
										c.getSource().sendSuccess(() -> Component.literal("Couldn't spawn, try being in a large desert"), true);
									}


									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("snowstorm_try").executes(c -> {

									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									boolean sandstormMade = wm.trySpawnParticleStormNearPos(c.getSource().getLevel(), c.getSource().getPosition(), WeatherObjectParticleStorm.StormType.SNOWSTORM);
									if (sandstormMade) {
										c.getSource().sendSuccess(() -> Component.literal("Summoned snowstorm"), true);
										wm.getWindManager().stopLowWindEvent();
										wm.getWindManager().startHighWindEvent();
									} else {
										c.getSource().sendSuccess(() -> Component.literal("Couldn't spawn, try being in a large snowy area"), true);
									}


									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("sandstorm_force").executes(c -> {

									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());

									wm.spawnParticleStorm(CoroUtilBlock.blockPos(c.getSource().getPosition()), WeatherObjectParticleStorm.StormType.SANDSTORM);
									wm.getWindManager().stopLowWindEvent();
									wm.getWindManager().startHighWindEvent();
									c.getSource().sendSuccess(() -> Component.literal("Summoned sandstorm"), true);

									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("snowstorm_force").executes(c -> {

									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());

									wm.spawnParticleStorm(CoroUtilBlock.blockPos(c.getSource().getPosition()), WeatherObjectParticleStorm.StormType.SNOWSTORM);
									wm.getWindManager().stopLowWindEvent();
									wm.getWindManager().startHighWindEvent();
									c.getSource().sendSuccess(() -> Component.literal("Summoned snowstorm"), true);

									return Command.SINGLE_SUCCESS;
								}))

						)
		);

	}

	private static StormObject summonStorm(CommandContext<CommandSourceStack> c, int intensity) {
		WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
		StormObject stormObject = new StormObject(wm);
		
		stormObject.setupStorm(c.getSource().getEntity());
		stormObject.levelCurIntensityStage = intensity;
		stormObject.levelStormIntensityMax = intensity;
		stormObject.initPositions(new Vec3(c.getSource().getPosition().x, StormObject.layers.get(stormObject.layer), c.getSource().getPosition().z));

		wm.addStormObject(stormObject);
		wm.syncStormNew(stormObject);
		return stormObject;
	}
}
