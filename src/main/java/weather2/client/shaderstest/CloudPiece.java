package weather2.client.shaderstest;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import weather2.client.shaders.WeatherShaders;

import java.nio.FloatBuffer;
import java.util.Random;

public class CloudPiece {

    public int index = 0;

    public float prevPosX = 0;
    public float prevPosY = 0;
    public float prevPosZ = 0;

    public float posX = 0;
    public float posY = 0;
    public float posZ = 0;

    public float rotX = 0;
    public float prevRotX = 0;

    public float colorR = 1F;
    public float colorG = 1F;
    public float colorB = 1F;
    public float alpha = 1F;

    public Random rand = new Random();

    public AABB aabb = new AABB(0, 0, 0, 1, 1, 1);

    public CloudPiece() {
        rand = new Random(5);
    }

    public void tick() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        rand = new Random(index);

        //Random rand = new Random();
        int range = 150;
        posY = 2+rand.nextInt(range);

        int yRange = (int) posY;

        int wat = Math.max(1, yRange/2);
        int wat2 = Math.max(1, yRange/4);
        /*posX = rand.nextInt(yRange) - rand.nextInt(yRange);
        posZ = rand.nextInt(yRange) - rand.nextInt(yRange);*/
        posX = rand.nextInt(wat) + 15 + yRange/4 - wat2;
        posZ = rand.nextInt(wat) + 15 + yRange/4 - wat2;

        posX += rand.nextFloat();
        posY += rand.nextFloat();
        posZ += rand.nextFloat();

        posY += 60;

        colorR = 0.7F;
        colorG = 0.7F;
        colorB = 0.7F;

        colorR = rand.nextFloat();
        colorG = rand.nextFloat();
        colorB = rand.nextFloat();

        float size = 1.5F;
        aabb = new AABB(posX, posY, posZ, posX, posY, posZ);
        aabb = aabb.inflate(size);
    }

    public void renderParticleForShader(InstancedMeshParticle mesh, Frustum cullingFrustum, Matrix4f modelViewMatrix, Entity entityIn,
                                        float partialTicks, double viewEntityX, double viewEntityY, double viewEntityZ) {

        if (mesh.curBufferPos >= mesh.numInstances) return;

        int range = 50;

        float posX = (float) ((this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks) - 0);
        float posY = (float) ((this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks) - 0);
        float posZ = (float) ((this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks) - 0);

        if (!cullingFrustum.isVisible(aabb)) return;

        //index = mesh.curBufferPos;

        float rotXSmooth = (float) (this.prevRotX + (this.rotX - this.prevRotX) * (double) partialTicks);

        Quaternion rotation = new Quaternion(0, 0, 0, 1);
        Quaternion rotation2 = new Quaternion(0, 0, 0, 1);

        rotation.mul(Vector3f.YP.rotationDegrees(rotXSmooth));
        rotation2.mul(Vector3f.YP.rotationDegrees(rotXSmooth));
        rotation2.mul(Vector3f.XP.rotationDegrees(rotXSmooth));
        rotation2.mul(Vector3f.ZP.rotationDegrees(rotXSmooth));

        Vector3f scale = new Vector3f(0.1F, 0.1F, 0.1F);

        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.multiplyWithTranslation((float) -viewEntityX, (float) -viewEntityY, (float) -viewEntityZ);
        //matrix4f.multiply(rotation);
        matrix4f.multiplyWithTranslation(posX, posY, posZ);
        matrix4f.multiply(rotation2);
        //matrix4f.multiply(rotation2);
        //matrix4f.multiply(5F);
        //matrix4f.multiply(rotation2);
        //matrix4f.multiplyWithTranslation((float) -viewEntityX, (float) -viewEntityY + 80, (float) -viewEntityZ);
        //modelViewMatrix.multiplyWithTranslation(posX, posY - 10, posZ);
        //modelViewMatrix.multiply(rotation);
        //Matrix4fe modelMatrix = new Matrix4fe(modelViewMatrix);
        Matrix4fe modelMatrix = new Matrix4fe(matrix4f);

        //upload to buffer
        modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), mesh.instanceDataBuffer);

        //brightness
        float brightness;
        //brightness = CoroUtilBlockLightCache.getBrightnessCached(world, (float)this.posX, (float)this.posY, (float)this.posZ);
        //brightness = brightnessCache;

        brightness = rand.nextInt(15728640);
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
        return alpha;
    }

    private float getBlueColorF() {
        return colorB;
    }

    private float getGreenColorF() {
        return colorG;
    }

    private float getRedColorF() {
        return colorR;
    }

}
