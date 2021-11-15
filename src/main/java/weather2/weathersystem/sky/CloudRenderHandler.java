package weather2.weathersystem.sky;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Vector3f;
import extendedrenderer.particle.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ICloudRenderHandler;

import java.util.Random;

public class CloudRenderHandler implements ICloudRenderHandler {

    private boolean cloudsNeedUpdate = true;
    private VertexBuffer cloudsVBO;
    private VertexBuffer cloudVBO;

    @Override
    public void render(int ticks, float partialTicks, PoseStack matrixStackIn, ClientLevel world, Minecraft mc, double viewEntityX, double viewEntityY, double viewEntityZ) {

    }

    private void renderCube(BufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vec3 cloudsColor, float scale) {
        Vector3f[] avector3f3 = new Vector3f[]{
                new Vector3f(-1.0F, 0.0F, -1.0F),
                new Vector3f(-1.0F, 0.0F, 1.0F),
                new Vector3f(1.0F, 0.0F, 1.0F),
                new Vector3f(1.0F, 0.0F, -1.0F)};

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f3[i];
            vector3f.mul(0.5F * scale);
            vector3f.add((float) cloudsX + 0.5F, (float) cloudsY, (float) cloudsZ + 0.5F);
        }

        TextureAtlasSprite sprite = ParticleRegistry.cloudNew;

        float f7 = sprite.getU0();
        float f8 = sprite.getU1();
        float f5 = sprite.getV0();
        float f6 = sprite.getV1();


        float particleRed = (float) cloudsColor.x;
        float particleGreen = (float) cloudsColor.y;
        float particleBlue = (float) cloudsColor.z;
        float particleAlpha = 1;

        bufferIn.vertex(avector3f3[0].x(), avector3f3[0].y(), avector3f3[0].z()).uv(f8, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferIn.vertex(avector3f3[1].x(), avector3f3[1].y(), avector3f3[1].z()).uv(f8, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferIn.vertex(avector3f3[2].x(), avector3f3[2].y(), avector3f3[2].z()).uv(f7, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferIn.vertex(avector3f3[3].x(), avector3f3[3].y(), avector3f3[3].z()).uv(f7, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(0.0F, -1.0F, 0.0F).endVertex();
    }

    private void renderSphere(BufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vec3 cloudsColor, float scale) {
        TextureAtlasSprite sprite = ParticleRegistry.cloud_square;

        float f7 = sprite.getU0();
        float f8 = sprite.getU1();
        float f5 = sprite.getV0();
        float f6 = sprite.getV1();

        float uRange = f8 - f7;
        float yRange = f6 - f5;

        float particleRed = (float) cloudsColor.x;
        float particleGreen = (float) cloudsColor.y;
        float particleBlue = (float) cloudsColor.z;
        float particleAlpha = 1;

        boolean alt = false;
        Random rand = new Random(555);

        int nSegments = 5;
        int nSlices = 5;
        float radius = 20;

        int iter = 0;
        int iter2 = 0;

        rand = new Random(555);

        for (double slice = 1.0; slice <= nSlices && iter <= 100; slice += 1.0) {
            double lat0 = Math.PI * (((slice - 1) / nSlices) - 0.5);
            double z0 = Math.sin(lat0);
            double zr0 = Math.cos(lat0);

            double lat1 = Math.PI * ((slice / nSlices) - 0.5);
            double z1 = Math.sin(lat1);
            double zr1 = Math.cos(lat1);

            //glBegin(GL_QUADS);
            //bufferIn.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

            for (double segment = 0.0; segment < nSegments && iter2 <= 408; segment += 1.0) {
                double long0 = 2 * Math.PI * ((segment -1 ) / nSegments);
                double x0 = Math.cos(long0);
                double y0 = Math.sin(long0);

                double long1 = 2 * Math.PI * (segment / nSegments);
                double x1 = Math.cos(long1);
                double y1 = Math.sin(long1);

                /*glVertex3f(x0 * zr0, y0 * zr0, z0);
                glVertex3f(x1 * zr1, y1 * zr1, z0);
                glVertex3f(x0 * zr0, y0 * zr0, z1);
                glVertex3f(x1 * zr1, y1 * zr1, z1);*/

                /*bufferIn.pos(x0 * zr0 * radius + cloudsX, y0 * zr0 * radius + cloudsY, z0 * radius + cloudsZ).tex(f8, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(1, 1, 1).endVertex();;
                bufferIn.pos(x1 * zr1 * radius + cloudsX, y1 * zr1 * radius + cloudsY, z0 * radius + cloudsZ).tex(f8, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(1, 1, 1).endVertex();
                bufferIn.pos(x0 * zr0 * radius + cloudsX, y0 * zr0 * radius + cloudsY, z1 * radius + cloudsZ).tex(f7, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(1, 1, 1).endVertex();
                bufferIn.pos(x1 * zr1 * radius + cloudsX, y1 * zr1 * radius + cloudsY, z1 * radius + cloudsZ).tex(f7, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(1, 1, 1).endVertex();*/

                /*particleRed = rand.nextFloat();
                particleGreen = rand.nextFloat();
                particleBlue = rand.nextFloat();*/
                particleAlpha = 1F;
                float radius2 = radius;
                if (scale > 0.5F) {
                    float scale2 = (1F - scale) * 2F;
                    radius2 = radius * scale2;//(1F - scale2);
                    //particleAlpha = scale2;
                } else {
                    radius2 = radius * scale * 2F;
                }

                //particleAlpha = 0.9F;

                float n1 = rand.nextFloat();
                float n2 = rand.nextFloat();
                float n3 = rand.nextFloat();

                n1 = 0;
                n2 = 0;
                n3 = 0;

                bufferIn.vertex(x1 * zr0 * radius2 + cloudsX, y1 * zr0 * radius2 + cloudsY, z0 * radius2 + cloudsZ).uv(f7, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(n1, n2, n3).endVertex();
                bufferIn.vertex(x0 * zr0 * radius2 + cloudsX, y0 * zr0 * radius2 + cloudsY, z0 * radius2 + cloudsZ).uv(f8, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(n1, n2, n3).endVertex();;
                bufferIn.vertex(x0 * zr1 * radius2 + cloudsX, y0 * zr1 * radius2 + cloudsY, z1 * radius2 + cloudsZ).uv(f8, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(n1, n2, n3).endVertex();
                bufferIn.vertex(x1 * zr1 * radius2 + cloudsX, y1 * zr1 * radius2 + cloudsY, z1 * radius2 + cloudsZ).uv(f7, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(n1, n2, n3).endVertex();

                iter2++;
            }

            /*Tessellator tessellator = Tessellator.getInstance();
            tessellator.draw();*/

            iter++;

            //glEnd();
        }
    }
}
