package weather2.client.entity.particle;

import extendedrenderer.particle.entity.ParticleCrossSection;
import extendedrenderer.particle.entity.ParticleTexExtraRender;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

public class ParticleHail extends ParticleCrossSection {

    public ParticleHail(ClientWorld worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void onHit() {
        super.onHit();
        if (world.rand.nextInt(30) == 0) {
            world.playSound(new BlockPos(posX, posY, posZ), SoundEvents.BLOCK_WOOD_BREAK, SoundCategory.WEATHER, 0.2F, 2F, false);
        }
    }
}
