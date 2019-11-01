package weather2.client.entity.particle;

import extendedrenderer.particle.entity.ParticleTexFX;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;

public class ParticleFish extends ParticleTexFX {

    public ParticleFish(World worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);


    }

    @Override
    public void tick() {
        super.tick();
    }
}
