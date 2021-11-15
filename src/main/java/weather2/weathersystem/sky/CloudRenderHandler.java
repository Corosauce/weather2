package weather2.weathersystem.sky;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import extendedrenderer.particle.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ICloudRenderHandler;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class CloudRenderHandler implements ICloudRenderHandler {

    private boolean cloudsNeedUpdate = true;
    private VertexBuffer cloudsVBO;
    //private VertexBuffer cloudVBO;

    @Override
    public void render(int ticks, float partialTicks, PoseStack matrixStackIn, ClientLevel world, Minecraft mc, double viewEntityX, double viewEntityY, double viewEntityZ) {
        //dont use until interface is fixed to add projection matrix
        //this.render(ticks, partialTicks, matrixStackIn, null, world, mc, viewEntityX, viewEntityY, viewEntityZ);
    }
    //PoseStack matrixStackIn, Matrix4f projectionMatrix, float p_172957_, double p_172958_, double viewEntityY, double p_172960_

    //public void render(int ticks, float partialTicks, PoseStack matrixStackIn, Matrix4f projectionMatrix, ClientLevel world, Minecraft mc, double viewEntityX, double viewEntityY, double viewEntityZ) {
    public void render(PoseStack matrixStackIn, Matrix4f projectionMatrix, float p_172957_, double p_172958_, double viewEntityY, double p_172960_) {

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        int ticks = (int) level.getGameTime() % Integer.MAX_VALUE;

        int method = 3;
        if (method == 3) {

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(true);
            //RenderSystem.enableFog();
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();

            //Minecraft.getInstance().getTextureManager().bindForSetup(TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            FogRenderer.levelFogColor();
            //GlStateManager.disableTexture();

            //this.cloudsNeedUpdate = true;
            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

            boolean force = true;

            /*if (this.cloudVBO == null || force) {
                if (this.cloudVBO != null) {
                    this.cloudVBO.close();
                }
                this.cloudVBO = new VertexBuffer();
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
                renderSphere(bufferbuilder, 0, 0, 0, new Vec3(1, 1, 1), 0.1F);
                bufferbuilder.end();
                this.cloudVBO.upload(bufferbuilder);
            }*/

            boolean uhh = true;
            this.cloudsNeedUpdate = true;

            if (uhh && this.cloudsNeedUpdate) {
                this.cloudsNeedUpdate = false;
                if (this.cloudsVBO != null) {
                    this.cloudsVBO.close();
                }

                this.cloudsVBO = new VertexBuffer();

                //this.drawClouds(bufferbuilder, d2, d3, d4, vector3d);
                Player player = Minecraft.getInstance().player;
                double playerX = player.getX();// - ((player.getPosX() - player.prevPosX) * partialTicks);
                //playerX = (MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX()) - player.getPosX());
                double playerY = player.getY();
                double playerZ = player.getZ();
                //playerZ = (MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ()) - player.getPosZ());

                double x = 5911 - (playerX);
                double y = 128 - viewEntityY + 0.001;
                double z = 6972 - (playerZ);

                Random rand = new Random(3);

                int randRangeY = 40;
                int randRange = 250;

                int randRangeY2 = 16;
                int randRange2 = 20;

                int clusters = 50;
                int clusterDensity = 50;

                RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
                bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

                for (int i = 0; i < 1; i++) {
                    double xx = rand.nextInt(randRange) - rand.nextInt(randRange);
                    double yy = rand.nextInt(randRangeY) - rand.nextInt(randRangeY);
                    double zz = rand.nextInt(randRange) - rand.nextInt(randRange);

                    float index = 0;
                    float indexMax = clusters;
                    float tickShift = (((float)ticks * 0.05F)) % indexMax;
                    for (int ii = 0; ii < 1; ii++) {
                        double xxx = rand.nextInt(randRange2) - rand.nextInt(randRange2);
                        double yyy = rand.nextInt(randRangeY2) - rand.nextInt(randRangeY2);
                        double zzz = rand.nextInt(randRange2) - rand.nextInt(randRange2);
                        double r = 0.8 + rand.nextDouble() * 0.2;
                        r = 0.7 + (yyy / (randRangeY2 * 2)) * 0.3;
                        //double g = 0.8 + rand.nextDouble() * 0.2;
                        //double b = 0.8 + rand.nextDouble() * 0.2;
                        float scale = (float)((index*1F + tickShift) % indexMax) / (float)indexMax;
                        //renderSphere(bufferbuilder, x + xx + xxx, y + 10 + yy + yyy, z + zz + zzz, new Vector3d(r, r, r), scale);
                        renderSphere(bufferbuilder, 0, 10, 0, new Vec3(r, r, r), 0.1F);
                        index++;
                    }
                }

                bufferbuilder.end();
                this.cloudsVBO.upload(bufferbuilder);
            }

            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            FogRenderer.levelFogColor();

            /*matrixStackIn.push();*/
            //matrixStackIn.scale(12.0F, 1.0F, 12.0F);
            //matrixStackIn.translate((double)(-f3), (double)f4, (double)(-f5));
            if (this.cloudsVBO != null && uhh && false) {
                /*this.cloudsVBO.bind();
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.setupBufferState(0L);

                this.cloudsVBO.draw(matrixStackIn.last().pose(), GL11.GL_QUADS);

                VertexBuffer.unbind();
                DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.clearBufferState();*/

                RenderSystem.colorMask(true, true, true, true);

                ShaderInstance shaderinstance = RenderSystem.getShader();
                this.cloudsVBO.drawWithShader(matrixStackIn.last().pose(), projectionMatrix, shaderinstance);
            }

            /*matrixStackIn.pop();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableFog();*/

            boolean vboEachCloud = true;

            if (vboEachCloud) {
                Player player = Minecraft.getInstance().player;
                double playerX = player.getX();// - ((player.getPosX() - player.prevPosX) * partialTicks);
                //playerX = (MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX()) - player.getPosX());
                double playerY = player.getY();
                double playerZ = player.getZ();
                //playerZ = (MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ()) - player.getPosZ());

                double x = 5911 - (playerX);
                double y = 250 - viewEntityY + 0.001;
                double z = 6972 - (playerZ);

                x = 336 - (playerX);
                y = 128 - viewEntityY + 0.001;
                z = -50 - (playerZ);

                Random rand = new Random(3);

                int randRangeY = 40;
                int randRange = 250;

                int randRangeY2 = 16;
                int randRange2 = 20;

                int clusters = 50;
                int clusterDensity = 50;

                //bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

                //this.cloudsVBO.bind();
                //DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.setupBufferState();

                RenderSystem.colorMask(true, true, true, true);

                for (int i = 0; i < clusters; i++) {
                    double xx = rand.nextInt(randRange) - rand.nextInt(randRange);
                    double yy = rand.nextInt(randRangeY) - rand.nextInt(randRangeY);
                    double zz = rand.nextInt(randRange) - rand.nextInt(randRange);

                    float index = 0;
                    float indexMax = clusters;
                    float tickShift = (((float)ticks * 0.05F)) % indexMax;
                    for (int ii = 0; ii < clusterDensity; ii++) {
                        double xxx = rand.nextInt(randRange2) - rand.nextInt(randRange2);
                        double yyy = rand.nextInt(randRangeY2) - rand.nextInt(randRangeY2);
                        double zzz = rand.nextInt(randRange2) - rand.nextInt(randRange2);
                        double r = 0.8 + rand.nextDouble() * 0.2;
                        r = 0.7 + (yyy / (randRangeY2 * 2)) * 0.3;
                        //double g = 0.8 + rand.nextDouble() * 0.2;
                        //double b = 0.8 + rand.nextDouble() * 0.2;
                        float scale = (float)((index*1F + tickShift) % indexMax) / (float)indexMax;
                        //renderSphere(bufferbuilder, x + xx + xxx, y + 10 + yy + yyy, z + zz + zzz, new Vector3d(r, r, r), scale);

                        matrixStackIn.pushPose();
                        matrixStackIn.translate(x + xx + xxx, y + 10 + yy + yyy, z + zz + zzz);
                        matrixStackIn.scale(scale, scale, scale);

                        //this.cloudsVBO.draw(matrixStackIn.last().pose(), GL11.GL_QUADS);
                        ShaderInstance shaderinstance = RenderSystem.getShader();
                        this.cloudsVBO.drawWithShader(matrixStackIn.last().pose(), projectionMatrix, shaderinstance);

                        matrixStackIn.popPose();

                        index++;
                    }
                }

                //VertexBuffer.unbind();
                //DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL.clearBufferState();

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                //RenderSystem.disableAlphaTest();
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                //RenderSystem.disableFog();
            }
        }
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
