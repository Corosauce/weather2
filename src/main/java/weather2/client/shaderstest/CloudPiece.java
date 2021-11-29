package weather2.client.shaderstest;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.world.entity.Entity;
import weather2.client.shaders.WeatherShaders;

import java.nio.FloatBuffer;
import java.util.Random;

public class CloudPiece {

    public float prevPosX = 0;
    public float prevPosY = 0;
    public float prevPosZ = 0;

    public float posX = 0;
    public float posY = 0;
    public float posZ = 0;

    public float rotX = 0;
    public float prevRotX = 0;

    public Random rand = new Random();

    public CloudPiece() {
        rand = new Random(5);
    }

    public void tick() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
    }

    public void renderParticleForShader(InstancedMeshParticle mesh, Transformation transformation, Matrix4f modelViewMatrix, Entity entityIn,
                                        float partialTicks, float rotationX, float rotationZ,
                                        float rotationYZ, float rotationXY, float rotationXZ) {

        if (mesh.curBufferPos >= mesh.numInstances) return;

        rand = new Random(mesh.curBufferPos);
        //Random rand = new Random();
        int range = 50;
        posX = rand.nextInt(range) - rand.nextInt(range);
        posY = rand.nextInt(range) - rand.nextInt(range);
        posZ = rand.nextInt(range) - rand.nextInt(range);

        //posX = 1;
        //posY = 0;
        //posZ = 0;

        //camera relative positions, for world position, remove the interpPos values
        /*float posX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks);
        float posY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks);
        float posZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks);*/
        //Vector3f pos = new Vector3f((float) (entityIn.posX - particle.posX), (float) (entityIn.posY - particle.posY), (float) (entityIn.posZ - particle.posZ));
        Vector3f pos = new Vector3f(posX, posY, posZ);

        float rotXSmooth = (float) (this.prevRotX + (this.rotX - this.prevRotX) * (double) partialTicks);

        //Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);
        Quaternion rotation = new Quaternion(0, 0, 0, 1);
        //rotation.mul(Vector3f.YP.rotationDegrees(rand.nextInt(360)));
        rotation.mul(Vector3f.YP.rotationDegrees(rotXSmooth));
        rotation.mul(Vector3f.XP.rotationDegrees(rotXSmooth));
        rotation.mul(Vector3f.ZP.rotationDegrees(rotXSmooth));
        //Vector3f scale = new Vector3f(1.0F, 1.0F, 1.0F);
        Vector3f scale = new Vector3f(0.1F, 0.1F, 0.1F);


        //modelViewMatrix.multiplyWithTranslation(posX, posY, posZ);
        //modelViewMatrix.multiplyWithTranslation(15, 0, 0);
        modelViewMatrix.multiplyWithTranslation(posX, posY + 80, posZ);
        modelViewMatrix.multiply(rotation);
        Matrix4fe modelMatrix = new Matrix4fe(modelViewMatrix);

        //adjust to perspective and camera
        //Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
        //upload to buffer
        modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), mesh.instanceDataBuffer);

        //brightness
        float brightness;
        //brightness = CoroUtilBlockLightCache.getBrightnessCached(world, (float)this.posX, (float)this.posY, (float)this.posZ);
        //brightness = brightnessCache;
        //brightness = 15728640;
        brightness = rand.nextInt(range);
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
        int range = 30;
        return rand.nextInt(range) - rand.nextInt(range);
        //return rand.nextFloat();
    }

    private float getGreenColorF() {
        int range = 30;
        return rand.nextInt(range) - rand.nextInt(range);
        //return rand.nextFloat();
    }

    private float getRedColorF() {
        int range = 30;
        return rand.nextInt(range) - rand.nextInt(range);
        //return rand.nextFloat();
    }

}
