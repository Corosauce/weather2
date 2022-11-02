package weather2.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.fml.InterModComms;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

import static net.minecraft.commands.Commands.literal;

public class WeatherCommand {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				literal("weather2")
						.then(literal("killAll").requires(s -> s.hasPermission(2)).executes(c -> {
							WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
							wm.clearAllStorms();
							c.getSource().sendSuccess(new TextComponent("Killed all storms"), true);
							return Command.SINGLE_SUCCESS;
						}))
						.then(literal("summon").requires(s -> s.hasPermission(2))
								.then(literal("tornado").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									StormObject stormObject = new StormObject(wm);

									stormObject.setupForcedTornado(c.getSource().getEntity());

									wm.addStormObject(stormObject);
									wm.syncStormNew(stormObject);

									c.getSource().sendSuccess(new TextComponent("Summoned Tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornadoPlayerBaby").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									StormObject stormObject = new StormObject(wm);

									stormObject.setupForcedTornado(c.getSource().getEntity());
									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(800);
									stormObject.setBaby(true);

									wm.addStormObject(stormObject);
									wm.syncStormNew(stormObject);

									c.getSource().sendSuccess(new TextComponent("Summoned Baby Player Tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornadoBaby").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									StormObject stormObject = new StormObject(wm);

									stormObject.setupForcedTornado(c.getSource().getEntity());
									stormObject.setBaby(true);

									wm.addStormObject(stormObject);
									wm.syncStormNew(stormObject);

									c.getSource().sendSuccess(new TextComponent("Summoned Baby Tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("tornadoPlayer").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									StormObject stormObject = new StormObject(wm);

									stormObject.setupForcedTornado(c.getSource().getEntity());
									stormObject.setupPlayerControlledTornado(c.getSource().getEntity());
									stormObject.setPlayerControlledTimeLeft(800);

									wm.addStormObject(stormObject);
									wm.syncStormNew(stormObject);

									c.getSource().sendSuccess(new TextComponent("Summoned Player Tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("sandstorm").executes(c -> {

									InterModComms.sendTo("weather2", "player_tornado", () -> {
										CompoundTag tag = new CompoundTag();
										tag.putString("uuid", c.getSource().getEntity().getUUID().toString());
										tag.putInt("time_ticks", 800);
										tag.putString("dimension", c.getSource().getEntity().getLevel().dimension().location().toString());
										return tag;
									});

									c.getSource().sendSuccess(new TextComponent("Summoned Sandstorm"), true);
									return Command.SINGLE_SUCCESS;
								}))

						)
		);

		/*dispatcher.register(
				literal("weather2")
						.then(literal("summon").requires(s -> s.hasPermission(2))
								.then(literal("sandstorm"))
									.executes(c -> {
										CULog.dbg("summon sandstorm at executor");

										return Command.SINGLE_SUCCESS;
									})
						)
		);*/
	}
}
