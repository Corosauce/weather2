package weather2.mixin.client;

import com.lovetropics.minigames.common.core.game.weather.RainType;
import extendedrenderer.ParticleRegistry2ElectricBubbleoo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import weather2.ClientWeather;

@Mixin(WorldRenderer.class)
public class RainSplashOverride {

	@ModifyArg(
			method = "addRainParticles",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/world/ClientWorld;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V", 
					ordinal = 0),
			index = 0)
	public IParticleData getParticle(IParticleData particleData) {
		//System.out.println("wat");
        if (ClientWeather.get().getRainType() == RainType.ACID) {
            return ParticleRegistry2ElectricBubbleoo.ACIDRAIN_SPLASH;
        } else {
            return ParticleTypes.RAIN;
        }
	}
}
