package weather2.client.entity.particle;

import com.corosus.coroutil.util.CoroUtilBlock;
import com.corosus.coroutil.util.CoroUtilMisc;
import extendedrenderer.particle.entity.ParticleCrossSection;
import extendedrenderer.particle.entity.ParticleTexExtraRender;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;

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
        if (CoroUtilMisc.random.nextInt(30) == 0) {
            level.playLocalSound(CoroUtilBlock.blockPos(x, y, z), SoundEvents.WOOD_BREAK, SoundSource.WEATHER, 0.2F, 2F, false);
        }
    }
}
