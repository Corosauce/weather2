package extendedrenderer.particle.entity;

import extendedrenderer.particle.ParticleRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import weather2.client.SceneEnhancer;

public class DustEmitter extends ParticleEmitter {
    public DustEmitter(ClientLevel par1World, double par2, double par4, double par6, double par8, double par10, double par12) {
        super(par1World, par2, par4, par6, par8, par10, par12);
    }

    @Override
    public void tick() {
        super.tick();

        ParticleTexExtraRender dust = new ParticleTexExtraRender(level,
                x,
                y,
                z,
                0D, 0D, 0D, ParticleRegistry.squareGrey);
        SceneEnhancer.particleBehavior.initParticleDustAir(dust);

        dust.spawnAsWeatherEffect();
    }
}
