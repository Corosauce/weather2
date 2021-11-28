package weather2.client.shaderstest;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.world.entity.Entity;

import java.nio.FloatBuffer;
import java.util.Random;

public class CloudPiece {

    float prevPosX = 0;
    float prevPosY = 0;
    float prevPosZ = 0;

    float posX = 0;
    float posY = 0;
    float posZ = 0;

    public void renderParticleForShader(InstancedMeshParticle mesh, Transformation transformation, Matrix4f viewMatrix, Entity entityIn,
                                        float partialTicks, float rotationX, float rotationZ,
                                        float rotationYZ, float rotationXY, float rotationXZ) {

        if (mesh.curBufferPos >= mesh.numInstances) return;

        Random rand = new Random(50);
        posX = rand.nextInt(50) - rand.nextInt(50);
        posY = rand.nextInt(50) - rand.nextInt(50);
        posZ = rand.nextInt(50) - rand.nextInt(50);

        posX = 10;
        posY = 0;
        posZ = 0;

        //camera relative positions, for world position, remove the interpPos values
        float posX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks);
        float posY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks);
        float posZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks);
        //Vector3f pos = new Vector3f((float) (entityIn.posX - particle.posX), (float) (entityIn.posY - particle.posY), (float) (entityIn.posZ - particle.posZ));
        Vector3f pos = new Vector3f(posX, posY, posZ);

        //Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);
        Quaternion rotation = new Quaternion(0, 0, 0, 1);
        //Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);
        Vector3f scale = new Vector3f(0.1F, 0.1F, 0.1F);
        Matrix4f modelMatrix1 = new Transformation(pos, rotation, scale, null).getMatrix();
        Matrix4fe modelMatrix = new Matrix4fe(modelMatrix1);

        //adjust to perspective and camera
        //Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
        //upload to buffer
        modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), mesh.instanceDataBuffer);

        //brightness
        float brightness;
        //brightness = CoroUtilBlockLightCache.getBrightnessCached(world, (float)this.posX, (float)this.posY, (float)this.posZ);
        //brightness = brightnessCache;
        brightness = 15728640;
        //brightness = -1F;
        //brightness = CoroUtilBlockLightCache.brightnessPlayer;
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS, brightness);

        int rgbaIndex = 0;
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getRedColorF());
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getGreenColorF());
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getBlueColorF());
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getAlphaF());

        mesh.curBufferPos++;

    }

    private float getAlphaF() {
        return 1F;
    }

    private float getBlueColorF() {
        Random rand = new Random();
        return rand.nextFloat();
    }

    private float getGreenColorF() {
        Random rand = new Random();
        return rand.nextFloat();
    }

    private float getRedColorF() {
        Random rand = new Random();
        return rand.nextFloat();
    }

}
