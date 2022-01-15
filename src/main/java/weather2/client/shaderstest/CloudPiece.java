package weather2.client.shaderstest;

import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.phys.AABB;
import weather2.ClientTickHandler;

import java.util.Random;

public class CloudPiece {

    public int index = 0;
    public int indexMax = 0;

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
    public float scale = 1F;

    public Random rand = new Random();

    public AABB aabb = new AABB(0, 0, 0, 1, 1, 1);

    public boolean render = true;
    public boolean fadingIn = false;
    public boolean fadingOut = false;
    public int fadeIn = 0;
    public int fadeInMax = 10;
    public int fadeOut = 0;
    public int fadeOutMax = 10;

    public CloudPiece() {
        rand = new Random(5);
    }

    public void tick() {
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        fadeInMax = 15;
        fadeOutMax = 15;

        rand = new Random(index);
        long time = Minecraft.getInstance().level.getGameTime();

        int testmodes = 4;

        if (testmodes == 0) {

            //Random rand = new Random();
            int range = 150;
            posY = 2 + rand.nextInt(range);

            int yRange = (int) posY;

            int wat = Math.max(1, yRange / 2);
            int wat2 = Math.max(1, yRange / 4);
            /*posX = rand.nextInt(yRange) - rand.nextInt(yRange);
            posZ = rand.nextInt(yRange) - rand.nextInt(yRange);*/
            posX = rand.nextInt(wat) + 15 + yRange / 4 - wat2;
            posZ = rand.nextInt(wat) + 15 + yRange / 4 - wat2;

            posX += rand.nextFloat();
            posY += rand.nextFloat();
            posZ += rand.nextFloat();

            posY += 60;

            colorR = 0.7F;
            colorG = 0.7F;
            colorB = 0.7F;
        } else if (testmodes == 1) {
            int yLayers = 1;
            int xWide = 200;
            int xzCount = indexMax / yLayers;
            int indexX = index % xWide;
            int indexZ = index / xWide;
            posX = indexX;
            posZ = indexZ;
            posY = 80;
            posY -= Math.sin(((time-(indexX / 2)) / 10F) % 360) * 15;
        } else if (testmodes == 2) {
            int yLayers = 1;
            int xWide = 200;
            int xzCount = indexMax / yLayers;
            int indexX = index % xWide;
            int indexZ = index / xWide;
            SimplexNoise noise = ClientTickHandler.weatherManager.cloudManager.getSimplexNoise();
            PerlinNoise perlinNoise = ClientTickHandler.weatherManager.cloudManager.getPerlinNoise();
            NormalNoise normalNoise = ClientTickHandler.weatherManager.cloudManager.getNormalNoise();
            double posYAdj = 0;//Math.sin(((time - (indexX / 2)) / 10F)) * 15;
            double noiseVal = 0;
            double scale = 30 * Math.sin(((time) / 40F) % 360);
            scale = 2;
            double scaleSimple = 0.1;
            //time = 0;
            double timeD = time * 0.01;
            //noiseVal = noise.getValue(((indexX) * scaleSimple) + 0, ((indexZ) * scaleSimple) + 0);
            //noiseVal = noise.getValue(((indexX) * scaleSimple) + timeD, ((indexZ) * scaleSimple) + timeD);
            //noiseVal *= noise.getValue(((indexX) * scaleSimple) + timeD * 2, ((indexZ) * scaleSimple) + timeD * 2);
            //noiseVal = perlinNoise.getValue(indexX + time, indexZ + time, posYAdj);
            noiseVal = perlinNoise.getValue(((indexX) * scale) + time, ((indexZ) * scale) + time, posYAdj);
            //noiseVal *= perlinNoise.getValue(((indexX) * scale) + time * 2, ((indexZ) * scale) + time * 2, posYAdj);
            //noiseVal = normalNoise.getValue(((indexX) * scale) + time, ((indexZ) * scale) + time, posYAdj);

            posX = 0;
            posY = 0;
            posZ = 0;
            posX = indexX;
            posZ = indexZ;
            posY = 1000;
            //prevPosY = posY;
            posY = 160;
            //posY -= posYAdj;
            alpha = 1F;
            float adjval = (float) ((noiseVal + 1F) / 2F);
            //alpha = (float) (noiseVal + 0.5F);
            //alpha = adjval;
            colorR = adjval;
            colorG = adjval;
            colorB = adjval;
            /*colorG = rand.nextFloat();
            colorB = rand.nextFloat();*/
            double noiseThreshold = Math.sin(((time) / 100F)) * 0.5D;
            //noiseThreshold = 0.0D;
            if (noiseVal > noiseThreshold) {
                //alpha = (float) (noiseVal + 1 / 2);
                //alpha = (float) noiseVal;
                alpha = 0F;
                //prevPosY = posY;
                //System.out.println(noiseVal);
            }
        } else if (testmodes == 3) {
            // Input: k in N(ABC)
            // Output: (x, y, z) in N(A) x N(B) x N(C)
            int B = 80;
            int C = 80;

            // N(ABC) -> N(A) x N(BC)
            int y = index / (B * C);   // x in N(A)
            int w = index % (B * C);   // w in N(BC)

            // N(BC) -> N(B) x N(C)
            int x = w / C;         // y in N(B)
            int z = w % C;         // z in N(C)

            posX = x + 0.5F;
            posY = y + 0.5F;
            posZ = z + 0.5F;

            posY += 25;

            //alpha = 1;
            //scale = 1;

            SimplexNoise noise = ClientTickHandler.weatherManager.cloudManager.getSimplexNoise();
            PerlinNoise perlinNoise = ClientTickHandler.weatherManager.cloudManager.getPerlinNoise();
            double scale = 10;
            double scale2 = 30;
            double scalescale = scale2 / scale;
            double scaleSimple = 0.05;
            double timeD = time * 0.01;
            double timeD2 = time * 0.05;
            timeD2 = time * 0.3;
            //timeD2 = 0;
            //time = 0;
            /*double noiseVal = (perlinNoise.getValue(((x) * scale) + timeD2, ((y) * scale) + 0, (z * scale) + 0) * 0.8F);
            noiseVal += (perlinNoise.getValue(((x) * scale2) + timeD2*scalescale, ((y) * scale2) + 0, (z * scale2) + 0) * 0.2F);*/

            double noiseVal = (perlinNoise.getValue(((x) * scale) + timeD2, ((0) * scale) + 0, (z * scale) + timeD2) * 1F);
            //noiseVal += (perlinNoise.getValue(((x) * scale2) + timeD2*scalescale, ((y) * scale2) + 0, (z * scale2) + 0) * 0.2F);


            //noiseVal = noise.getValue(((x) * scaleSimple) + timeD, ((z) * scaleSimple) + timeD);
            //noiseVal *= noise.getValue(((y) * scaleSimple) + timeD, ((z) * scaleSimple) + timeD) / 2D;
            float adjval = (float) ((noiseVal + 1F) / 1F);
            //alpha = (float) (noiseVal + 0.5F);
            //alpha = adjval;

            //0 - 1
            double noiseVal2 = (noiseVal + 1D) / 2D;
            //noiseVal2 = (noiseVal + 0.5D) / 1D;

            double range = 0.33F;

            /*int number = (int) (255F*255F*255F * noiseVal2);
            int r = (number & 0xff0000) >> 16;
            int g = (number & 0x00ff00) >> 8;
            int b = (number & 0x0000ff);

            colorR = (float)r / 255F;
            colorG = (float)g / 255F;
            colorB = (float)b / 255F;*/

            colorR = (float) (noiseVal2 * 1.4F);
            colorG = colorR;
            colorB = colorR;

            /*if (noiseVal2 < range) {
                colorR = (float) (noiseVal2 * 3F);
            } else if (noiseVal2 < range*2 && noiseVal2 >= range) {
                colorG = (float) (noiseVal2 * 3F);
            } else {
                colorB = (float) (noiseVal2 * 3F);
            }*/

            double noiseThreshold = Math.sin(((time) / 150F)) * 0.35D;
            //noiseThreshold = 0.0D;
            double yDist = Math.abs(y - 3);
            //for perlin
            noiseThreshold += yDist * 0.06F;
            //for simple noise
            //noiseThreshold += yDist * 0.3F;
            //alpha = (float) noiseVal2 - 0.5F;
            if (noiseVal < noiseThreshold/* || yDist >= 4*/) {
                //alpha = 0F;
                if (!fadingOut && alpha >= 0.5F) {
                    fadingOut = true;
                    fadeOut = 0;
                }
            } else {
                if (!fadingIn && alpha <= 0.5F) {
                    fadingIn = true;
                    fadeIn = 0;
                }
            }

            /*fadingIn = false;
            fadingOut = false;
            alpha = 1;
            scale = 1;*/
        } else if (testmodes == 4) {
            alpha = 1F;
            //scale = 1F;
        }

        if (fadingIn) {
            alpha = (float)fadeIn / (float)fadeInMax;
            scale = (float)fadeIn / (float)fadeInMax;
            rotX = ((float)fadeIn / (float)fadeInMax) * 90;
            fadeIn++;
            if (fadeIn > fadeInMax) {
                fadingIn = false;
            }
        } else if (fadingOut) {
            alpha = 1F - (float)fadeOut / (float)fadeOutMax;
            scale = 1F - (float)fadeOut / (float)fadeOutMax;
            rotX = (1F - (float)fadeOut / (float)fadeInMax) * 90;
            fadeOut++;
            if (fadeOut > fadeOutMax) {
                fadingOut = false;
            }
        }

        /*alpha = 1;
        scale = 1;
        rotX = 0;*/

        /*colorR = rand.nextFloat();
        colorG = rand.nextFloat();
        colorB = rand.nextFloat();*/

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

        /*double d0 = viewEntityX - posX;
        double d1 = viewEntityY - posY;
        double d2 = viewEntityZ - posZ;
        double dist = d0 * d0 + d1 * d1 + d2 * d2;

        if (dist > 300 * 300) return;

        if (!cullingFrustum.isVisible(aabb)) return;*/

        //index = mesh.curBufferPos;

        float rotXSmooth = (float) (this.prevRotX + (this.rotX - this.prevRotX) * (double) partialTicks);

        Quaternion rotation = new Quaternion(0, 0, 0, 1);
        //Quaternion rotation2 = new Quaternion(0, 0, 0, 1);

        rotation.mul(Vector3f.YP.rotationDegrees(rotXSmooth));
        /*rotation2.mul(Vector3f.YP.rotationDegrees(rotXSmooth));
        rotation2.mul(Vector3f.XP.rotationDegrees(rotXSmooth));
        rotation2.mul(Vector3f.ZP.rotationDegrees(rotXSmooth));*/

        Vector3f scaleF = new Vector3f(scale, scale, scale);

        Matrix4f matrix4f = new Matrix4f();
        matrix4f.setIdentity();
        matrix4f.multiply(scale);
        matrix4f.multiplyWithTranslation((float) -viewEntityX, (float) -viewEntityY, (float) -viewEntityZ);
        //matrix4f.multiply(rotation);
        matrix4f.multiplyWithTranslation(posX, posY, posZ);
        matrix4f.multiply(rotation);
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
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS, 1);

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
        return 1;
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
