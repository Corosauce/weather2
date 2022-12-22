package extendedrenderer.particle.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;

public class ParticleEmitter extends EntityRotFX {

    public ParticleEmitter(ClientLevel par1World, double par2, double par4, double par6, double par8, double par10, double par12) {
        super(par1World, par2, par4, par6, par8, par10, par12);
    }

    @Override
    public void tick() {
        //super.tick();
        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        //super.render(buffer, renderInfo, partialTicks);
    }
}
