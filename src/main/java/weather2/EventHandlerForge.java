package weather2;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import weather2.client.SceneEnhancer;
import weather2.util.WeatherUtilEntity;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.wind.WindManager;

@Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHandlerForge {

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
    public void worldRender(RenderLevelLastEvent event)
    {
		ClientTickHandler.getClientWeather();
    }

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
    public void onFogColors(FogColors event) {
        SceneEnhancer.getFogAdjuster().onFogColors(event);
		
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onFogRender(RenderFogEvent event) {
		SceneEnhancer.getFogAdjuster().onFogRender(event);
	}
	
	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onRenderTick(TickEvent.RenderTickEvent event) {
		SceneEnhancer.renderTick(event);
	}

	@SubscribeEvent
	public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		Entity ent = event.getEntity();
		if (ent.level.isClientSide && (ent instanceof Player && ((Player) ent).isLocalPlayer())) {
			onClientPlayerUpdate(event);
		}
	}

	@SubscribeEvent
	@OnlyIn(Dist.CLIENT)
	public void onClientPlayerUpdate(LivingEvent.LivingUpdateEvent event) {

		Entity ent = event.getEntity();
		WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
		if (weatherMan == null) return;
		WindManager windMan = weatherMan.getWindManager();
		if (windMan == null) return;

		ClientWeatherProxy weather = ClientWeatherProxy.get();
		if (weather.isSnowstorm() || weather.isSandstorm()) {
			if (ent.isOnGround() && !ent.isSpectator() && !WeatherUtilEntity.isPlayerSheltered(ent)/* && ent.world.getGameTime() % 20 == 0*/) {

				float playerSpeed = (float) Math.sqrt(ent.getDeltaMovement().x * ent.getDeltaMovement().x + ent.getDeltaMovement().z * ent.getDeltaMovement().z);

				if (playerSpeed > 0.02F && playerSpeed < 0.3F) {

					//System.out.println("playerSpeed: " + playerSpeed);

					/**
					 * Calculate the players angle from motion, compare it against wind
					 * under 90 means theyre moving with the wind, above 90 means against the wind, 90 means perpendicular to it
					 * scale wind assistance / resistance to wind based on dist from 0 to 90 or 90 to 180
					 */

					float playerAngle = -(float) (Math.toDegrees(Math.atan2(ent.getDeltaMovement().x, ent.getDeltaMovement().z)));
					int phi = (int) (Math.abs(windMan.getWindAngle(ent.position()) - playerAngle) % 360);
					float diffAngle = phi > 180 ? 360 - phi : phi;
					//System.out.println("diffAngle: " + diffAngle);
					if (diffAngle < 90) {
						float assistRate = 1F - (diffAngle / 90F);
						float assist = 1F + (0.12F * assistRate);
						//System.out.println("assist: " + assist);
						ent.setDeltaMovement(ent.getDeltaMovement().x * assist, ent.getDeltaMovement().y, ent.getDeltaMovement().z * assist);
					} else if (diffAngle >= 90) {
						float dampenRate = ((diffAngle - 90F) / 90F);
						float dampen = 1F - (0.12F * dampenRate);
						//System.out.println("dampen: " + dampen);
						if (dampen != 0) {
							ent.setDeltaMovement(ent.getDeltaMovement().x * dampen, ent.getDeltaMovement().y, ent.getDeltaMovement().z * dampen);
						}
					}
				}
			}
		}

	}
}
