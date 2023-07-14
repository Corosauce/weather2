package weather2.client.entity.particle;

import extendedrenderer.particle.entity.ParticleCrossSection;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class ParticleHail extends ParticleCrossSection {

    public ParticleHail(ClientLevel worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void onHit() {
        super.onHit();
        if (RandomSource.create().nextInt(30) == 0) {
            level.playLocalSound(new BlockPos(x, y, z), SoundEvents.WOOD_BREAK, SoundSource.WEATHER, 0.2F, 2F, false);
        }
    }
}
