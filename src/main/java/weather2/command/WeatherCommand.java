package weather2.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.InterModComms;
import weather2.ServerTickHandler;
import weather2.config.ConfigWind;
import weather2.util.WeatherUtil;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;

import static net.minecraft.commands.Commands.literal;

public class WeatherCommand {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				literal("weather2")
						.then(literal("kill_all_storms").requires(s -> s.hasPermission(2)).executes(c -> {
							WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
							wm.clearAllStorms();
							c.getSource().sendSuccess(new TextComponent("Killed all storms"), true);
							return Command.SINGLE_SUCCESS;
						}))
						.then(literal("debug").requires(s -> s.hasPermission(2))
								.then(literal("print_grab_list").executes(c -> {
									WeatherUtil.testAllBlocks();
									c.getSource().sendSuccess(new TextComponent("Tornado grab list printed to debug.log"), true);
									return Command.SINGLE_SUCCESS;
								}))
						)
						.then(literal("wind_event").requires(s -> s.hasPermission(2))
								.then(literal("clear").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									wm.getWindManager().stopLowWindEvent();
									wm.getWindManager().stopHighWindEvent();
									c.getSource().sendSuccess(new TextComponent("Stopped any active high or low wind events"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("high").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									wm.getWindManager().stopLowWindEvent();
									wm.getWindManager().startHighWindEvent();
									c.getSource().sendSuccess(new TextComponent("Started high wind event"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("low").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									wm.getWindManager().stopHighWindEvent();
									wm.getWindManager().startLowWindEvent();
									wm.getWindManager().windSpeedGlobal = (float) (ConfigWind.windSpeedMin + 0.2F);
									c.getSource().sendSuccess(new TextComponent("Started low wind event"), true);
									return Command.SINGLE_SUCCESS;
								}))
						)
						.then(literal("summon").requires(s -> s.hasPermission(2))
								.then(literal("storm_rain").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_NORMAL);

									c.getSource().sendSuccess(new TextComponent("Summoned rain storm"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("storm_lightning").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_THUNDER);

									stormObject.initRealStorm(null, null);

									c.getSource().sendSuccess(new TextComponent("Summoned lightning storm"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_FORMING);

									c.getSource().sendSuccess(new TextComponent("Summoned tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("sharknado").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setSharknado(true);

									c.getSource().sendSuccess(new TextComponent("Summoned sharknado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_player_baby").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(800);
									stormObject.setBaby(true);

									c.getSource().sendSuccess(new TextComponent("Summoned baby player tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_baby").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setBaby(true);

									c.getSource().sendSuccess(new TextComponent("Summoned baby tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_player").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(800);

									c.getSource().sendSuccess(new TextComponent("Summoned player tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_pet").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(-1);
									stormObject.setPet(true);
									stormObject.setPetGrabsItems(true);

									c.getSource().sendSuccess(new TextComponent("Summoned pet tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_pet_no_item_grab").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_STAGE1);

									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(-1);
									stormObject.setPet(true);
									stormObject.setPetGrabsItems(false);

									c.getSource().sendSuccess(new TextComponent("Summoned pet tornado with no item grabbing"), true);
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

									c.getSource().sendSuccess(new TextComponent("Summoned tornado test"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornado_f0_max").executes(c -> {
									StormObject stormObject = summonStorm(c, StormObject.STATE_FORMING);
									stormObject.levelStormIntensityMax = StormObject.STATE_FORMING;
									stormObject.alwaysProgresses = false;

									c.getSource().sendSuccess(new TextComponent("Summoned tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("sandstorm_try").executes(c -> {

									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									boolean sandstormMade = wm.trySpawnParticleStormNearPos(c.getSource().getLevel(), c.getSource().getPosition(), WeatherObjectParticleStorm.StormType.SANDSTORM);
									if (sandstormMade) {
										c.getSource().sendSuccess(new TextComponent("Summoned sandstorm"), true);
										wm.getWindManager().stopLowWindEvent();
										wm.getWindManager().startHighWindEvent();
									} else {
										c.getSource().sendSuccess(new TextComponent("Couldn't spawn, try being in a large desert"), true);
									}


									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("snowstorm_try").executes(c -> {

									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									boolean sandstormMade = wm.trySpawnParticleStormNearPos(c.getSource().getLevel(), c.getSource().getPosition(), WeatherObjectParticleStorm.StormType.SNOWSTORM);
									if (sandstormMade) {
										c.getSource().sendSuccess(new TextComponent("Summoned snowstorm"), true);
										wm.getWindManager().stopLowWindEvent();
										wm.getWindManager().startHighWindEvent();
									} else {
										c.getSource().sendSuccess(new TextComponent("Couldn't spawn, try being in a large snowy area"), true);
									}


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
