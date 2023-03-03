package extendedrenderer.particle.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import weather2.client.SceneEnhancer;
import weather2.util.WeatherUtil;

public class ParticleBird extends EntityRotFX {

    public ParticleBird(ClientLevel par1World, double par2, double par4, double par6, double par8, double par10, double par12) {
        super(par1World, par2, par4, par6, par8, par10, par12);
    }

    @Override
    public void tick() {
        //super.tick();
        if (this.age++ >= this.lifetime) {
            this.remove();
        }

        //https://processing.org/examples/flocking.html

        Vec3 separate = new Vec3(0, 0, 0);
        int collideCount = 0;
        int nearCount = 0;

        float avoidDist = 1F;
        float neighborDist = 2F;

        for (ParticleBird bird : SceneEnhancer.getListBirds()) {
            if (this == bird) continue;
            double dist = WeatherUtil.dist(this.getPosition(), bird.getPosition());
            if (dist < avoidDist) {
                Vec3 vec = this.getPos().subtract(bird.getPos());
                vec = vec.normalize();
                vec = new Vec3(vec.x / dist, vec.y / dist, vec.z / dist);
                separate = separate.add(vec);
                collideCount++;

                //vec = vec.multiply(dist, dist, dist);
            }
        }

        if (collideCount > 0) {
            separate = new Vec3(separate.x / (float)collideCount, separate.y / (float)collideCount, separate.z / (float)collideCount);
        }

        Vec3 sum = new Vec3(0, 0, 0);

    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        //super.render(buffer, renderInfo, partialTicks);
    }
}
