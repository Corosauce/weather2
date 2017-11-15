package weather2.client.entity.particle;

import extendedrenderer.particle.entity.ParticleTexFX;
import extendedrenderer.shader.InstancedMeshParticle;
import extendedrenderer.shader.Matrix4fe;
import extendedrenderer.shader.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ParticleFish extends ParticleTexFX {

    public ParticleFish(World worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item) {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);


    }

    @Override
    public void onUpdate() {
        super.onUpdate();
    }

    @Override
    public void renderParticleForShader(InstancedMeshParticle mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        super.renderParticleForShader(mesh, transformation, viewMatrix, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }
}
