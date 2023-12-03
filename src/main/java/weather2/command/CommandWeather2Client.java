package weather2.command;

import com.corosus.coroutil.util.CULog;
import com.corosus.modconfig.ConfigMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import weather2.ClientTickHandler;
import weather2.config.ConfigDebug;
import weather2.config.ConfigParticle;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Queue;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class CommandWeather2Client {
	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal(getCommandName())
						.then(literal("client")
								.then(literal("particle_rate")
										.then(argument("value", FloatArgumentType.floatArg(0, 1F)).executes(c -> {
											float value = FloatArgumentType.getFloat(c, "value");
											ConfigParticle.Particle_effect_rate = value;
											c.getSource().sendSuccess(() -> Component.literal("Set weather2 particle effect rate to " + value), true);
											ConfigMod.forceSaveAllFilesFromRuntimeSettings();
											return Command.SINGLE_SUCCESS;
										}))
								)
								/*.then(literal("particle_reset_frequency")
										.then(argument("seconds", IntegerArgumentType.integer(0, 20*60*24)).executes(c -> {
											int value = IntegerArgumentType.getInteger(c, "seconds");
											ConfigDebug.Particle_Reset_Frequency = value * 20;
											c.getSource().sendSuccess(() -> Component.literal("Set weather2 particle reset frequency " + value), true);
											ConfigMod.forceSaveAllFilesFromRuntimeSettings();
											ConfigMod.forceSaveAllFilesFromRuntimeSettings();
											return Command.SINGLE_SUCCESS;
										}))
								)*/
								.then(literal("particle_vanilla_precipitation")
										.then(argument("value", BoolArgumentType.bool()).executes(c -> {
											boolean value = BoolArgumentType.getBool(c, "value");
											ConfigParticle.Particle_vanilla_precipitation = value;
											c.getSource().sendSuccess(() -> Component.literal("Set weather2 to use vanilla particles?: " + value), true);
											ConfigMod.forceSaveAllFilesFromRuntimeSettings();
											return Command.SINGLE_SUCCESS;
										}))
								)
								.then(literal("particle_engine")
										.then(literal("weather2").executes(c -> {
											ConfigParticle.Particle_engine_weather2 = true;
											c.getSource().sendSuccess(() -> Component.literal("Set particle engine to weather2"), true);
											ConfigMod.forceSaveAllFilesFromRuntimeSettings();
											return Command.SINGLE_SUCCESS;
										}))
										.then(literal("vanilla").executes(c -> {
											ConfigParticle.Particle_engine_weather2 = false;
											ClientTickHandler.particleManagerExtended().clearParticles();
											c.getSource().sendSuccess(() -> Component.literal("Set particle engine to vanilla"), true);
											ConfigMod.forceSaveAllFilesFromRuntimeSettings();
											return Command.SINGLE_SUCCESS;
										}))
								)
								.then(literal("debug")
									.then(literal("particles_weather2").executes(c -> {
										msg(c, "total particle count: " + ClientTickHandler.particleManagerExtended().countParticles());
										Map<ParticleRenderType, Queue<Particle>> particles = ClientTickHandler.particleManagerExtended().getParticles();
										if (particles != null) {
											msg(c, "particle type count: " + particles.size());
											msg(c, "detailed particle info output to log file");
											//Map<ParticleRenderType, Queue<Particle>> particles = particleEngine.particles;
											int maxCount = 200;
											int count = 0;
											CULog.log("outputting particle data:");
											for (Map.Entry<ParticleRenderType, Queue<Particle>> type : particles.entrySet()) {
												CULog.log("type: " + type.getKey() + " -> " + type.getValue().size() + " - classpath: " + type.getKey().getClass().getName());
												if (count > maxCount) {
													CULog.log("aborted due to large particle type list");
													break;
												}
												count++;
											}
										} else {
											msg(c, "failed to get particles list");
										}

										return Command.SINGLE_SUCCESS;

									}))
									.then(literal("particles_vanilla").executes(c -> {
										ParticleEngine particleEngine = Minecraft.getInstance().particleEngine;
										//Map<ParticleRenderType, Queue<Particle>> particles = ObfuscationReflectionHelper.getPrivateValue(ParticleEngine.class, particleEngine, "particles");
										msg(c, "total particle count: " + particleEngine.countParticles());
										msg(c, "emitter count: " + particleEngine.trackingEmitters.size());
										Map<ParticleRenderType, Queue<Particle>> particles = getParticles();
										if (particles != null) {
											msg(c, "particle type count: " + particles.size());
											msg(c, "detailed particle info output to log file");
											//Map<ParticleRenderType, Queue<Particle>> particles = particleEngine.particles;
											int maxCount = 200;
											int count = 0;
											CULog.log("outputting particle data:");
											for (Map.Entry<ParticleRenderType, Queue<Particle>> type : particles.entrySet()) {
												CULog.log("type: " + type.getKey() + " -> " + type.getValue().size() + " - classpath: " + type.getKey().getClass().getName());
												if (count > maxCount) {
													CULog.log("aborted due to large particle type list");
													break;
												}
												count++;
											}
										} else {
											msg(c, "failed to get particles list");
										}
										return Command.SINGLE_SUCCESS;

									}))
									.then(literal("particle_engine_render")
											.then(argument("value", BoolArgumentType.bool()).executes(c -> {
												boolean value = BoolArgumentType.getBool(c, "value");
												ConfigDebug.Particle_engine_render = value;
												c.getSource().sendSuccess(() -> Component.literal("ConfigParticle.Particle_engine_render: " + value), true);
												ConfigMod.forceSaveAllFilesFromRuntimeSettings();
												return Command.SINGLE_SUCCESS;
											}))
									)
									.then(literal("particle_engine_tick")
											.then(argument("value", BoolArgumentType.bool()).executes(c -> {
												boolean value = BoolArgumentType.getBool(c, "value");
												ConfigDebug.Particle_engine_tick = value;
												c.getSource().sendSuccess(() -> Component.literal("ConfigParticle.Particle_engine_tick: " + value), true);
												ConfigMod.forceSaveAllFilesFromRuntimeSettings();
												return Command.SINGLE_SUCCESS;
											}))
									)
									.then(literal("reset_vanilla_particles")
										.executes(c -> {
											Minecraft.getInstance().particleEngine.clearParticles();
											c.getSource().sendSuccess(() -> Component.literal("cleared particles"), true);
											return Command.SINGLE_SUCCESS;
										})
									)
								)
						)
		);
	}

	public static Map<ParticleRenderType, Queue<Particle>> getParticles() {
		try {
			Field[] fields = ParticleEngine.class.getDeclaredFields();
			fields[6].setAccessible(true);
			return (Map<ParticleRenderType, Queue<Particle>>) fields[6].get(Minecraft.getInstance().particleEngine);
		} catch (IllegalAccessException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void msg(CommandContext<CommandSourceStack> c, String msg) {
		c.getSource().sendSuccess(() -> Component.literal(msg), true);
	}

	public static String getCommandName() {
		return "weather2";
	}
}
