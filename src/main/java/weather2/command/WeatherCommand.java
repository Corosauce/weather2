package weather2.command;

import com.corosus.coroutil.util.CULog;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec3;
import weather2.ServerTickHandler;
import weather2.weathersystem.WeatherManager;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;

import static net.minecraft.commands.Commands.literal;

public class WeatherCommand {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				literal("weather2")
						.then(literal("summon").requires(s -> s.hasPermission(2))
								.then(literal("tornado").executes(c -> {
									WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(c.getSource().getLevel().dimension());
									StormObject stormObject = new StormObject(wm);
									stormObject.layer = 0;
									if (c.getSource().getEntity() != null) {
										stormObject.spawnerUUID = c.getSource().getEntity().getUUID().toString();
										stormObject.naturallySpawned = false;
										stormObject.levelTemperature = 0.1F;
										stormObject.pos = c.getSource().getEntity().position();
										stormObject.levelWater = stormObject.levelWaterStartRaining * 2;
										stormObject.attrib_precipitation = true;
										stormObject.levelCurIntensityStage = StormObject.STATE_STAGE1;
										stormObject.alwaysProgresses = true;

										stormObject.initFirstTime();

										//lock it to current stage or less
										stormObject.levelStormIntensityMax = stormObject.levelCurIntensityStage;

										wm.addStormObject(stormObject);
										wm.syncStormNew(stormObject);
									}

									c.getSource().sendSuccess(new TextComponent("Summoned Tornado"), true);
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("sandstorm").executes(c -> {
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
