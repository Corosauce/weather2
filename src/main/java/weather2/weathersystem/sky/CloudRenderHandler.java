package weather2.weathersystem.sky;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import extendedrenderer.particle.ParticleRegistry;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.ICloudRenderHandler;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Random;

public class CloudRenderHandler implements ICloudRenderHandler {

    private boolean cloudsNeedUpdate = true;
    private VertexBuffer cloudsVBO;
    private VertexBuffer cloudVBO;

    @Override
    public void render(int ticks, float partialTicks, MatrixStack matrixStackIn, ClientWorld world, Minecraft mc, double viewEntityX, double viewEntityY, double viewEntityZ) {
        int method = 3;
        if (method == 0) {
            BlockState blockstate = Blocks.ICE.getDefaultState();
            Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            BufferBuilder bufferIn = Tessellator.getInstance().getBuffer();
            //IRenderTypeBuffer.Impl bufferIn = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
            if (blockstate.getRenderType() == BlockRenderType.MODEL) {
                //matrixStackIn.push();
                BlockPos blockpos = new BlockPos(viewEntityX, viewEntityY + 2, viewEntityZ);
                blockpos = new BlockPos(0, 0 + 0, 0);

                matrixStackIn.push();

                matrixStackIn.translate(-0.5D, 0.0D, -0.5D);
                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                for (net.minecraft.client.renderer.RenderType type : net.minecraft.client.renderer.RenderType.getBlockRenderTypes()) {
                    //if (RenderTypeLookup.canRenderInLayer(blockstate, type)) {
                        net.minecraftforge.client.ForgeHooksClient.setRenderLayer(type);
                        bufferIn.begin(7, DefaultVertexFormats.BLOCK);
                        blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(blockstate), blockstate, blockpos, matrixStackIn, bufferIn, false, new Random(), 0, OverlayTexture.NO_OVERLAY);
                        bufferIn.finishDrawing();
                    //}
                }

                matrixStackIn.pop();

                net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
                //matrixStackIn.pop();
            }
        } else if (method == 1) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.enableFog();
            RenderSystem.depthMask(true);
            BufferBuilder bufferIn = Tessellator.getInstance().getBuffer();

            VertexBuffer cloudsVBO;
            cloudsVBO = new VertexBuffer(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

            bufferIn.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

            double l1 = 0;
            double f17 = 0;
            double i2 = 0;
            /*l1 += viewEntityX;
            f17 += viewEntityY;
            i2 += viewEntityZ;*/
            float f3 = 1;
            float f4 = 1;
            float f5 = 1;
            float f6 = 1;
            float f7 = 1;

            bufferIn.pos((double)(l1 + 0), (double)f17, (double)(i2 + 32)).tex((float)(l1 + 0) * 0.00390625F + f3, (float)(i2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
            bufferIn.pos((double)(l1 + 32), (double)f17, (double)(i2 + 32)).tex((float)(l1 + 32) * 0.00390625F + f3, (float)(i2 + 32) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
            bufferIn.pos((double)(l1 + 32), (double)f17, (double)(i2 + 0)).tex((float)(l1 + 32) * 0.00390625F + f3, (float)(i2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();
            bufferIn.pos((double)(l1 + 0), (double)f17, (double)(i2 + 0)).tex((float)(l1 + 0) * 0.00390625F + f3, (float)(i2 + 0) * 0.00390625F + f4).color(f5, f6, f7, 0.8F).normal(0.0F, -1.0F, 0.0F).endVertex();

            bufferIn.finishDrawing();
            cloudsVBO.upload(bufferIn);

            ResourceLocation CLOUDS_TEXTURES = new ResourceLocation("textures/environment/clouds.png");
            Minecraft.getInstance().textureManager.bindTexture(CLOUDS_TEXTURES);
            //Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
            matrixStackIn.push();
            //matrixStackIn.scale(12.0F, 1.0F, 12.0F);
            matrixStackIn.scale(0.01F, 1F, 0.01F);
            matrixStackIn.translate((double)(-f3), (double)f4 - 0.8, (double)(-f5));

            cloudsVBO.bindBuffer();
            DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.setupBufferState(0L);

            RenderSystem.colorMask(true, true, true, true);
            //RenderSystem.colorMask(false, false, false, false);

            cloudsVBO.draw(matrixStackIn.getLast().getMatrix(), 7);
            VertexBuffer.unbindBuffer();
            DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.clearBufferState();

            matrixStackIn.pop();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableFog();
        } else if (method == 2) {

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            //RenderSystem.enableFog();
            RenderSystem.depthMask(true);
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();

            Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
            //GlStateManager.disableTexture();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
            PlayerEntity player = Minecraft.getInstance().player;
            double playerX = player.getPosX();// - ((player.getPosX() - player.prevPosX) * partialTicks);
            //playerX = (MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX()) - player.getPosX());
            double playerY = player.getPosY();
            double playerZ = player.getPosZ();
            //playerZ = (MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ()) - player.getPosZ());

            double x = 5911 - (playerX);
            double y = 250 - viewEntityY + 0.001;
            double z = 6972 - (playerZ);

            Random rand = new Random(3);

            int randRangeY = 40;
            int randRange = 250;

            int randRangeY2 = 16;
            int randRange2 = 20;

            int clusters = 50;
            int clusterDensity = 50;

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
                    renderSphere(buffer, x + xx + xxx, y + 10 + yy + yyy, z + zz + zzz, new Vector3d(r, r, r), scale);
                    index++;
                }
            }

            //System.out.println(index);



            Tessellator tessellator = Tessellator.getInstance();
            tessellator.draw();

            //DefaultVertexFormats.POSITION_COLOR.clearBufferState();
            //DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.clearBufferState();

            //matrixStackIn.pop();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableFog();
            //RenderSystem.enableTexture();
        } else if (method == 3) {

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            //RenderSystem.enableFog();
            RenderSystem.depthMask(true);
            BufferBuilder buffer = Tessellator.getInstance().getBuffer();

            Minecraft.getInstance().getTextureManager().bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
            //GlStateManager.disableTexture();

            //this.cloudsNeedUpdate = true;
            BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

            boolean force = true;

            if (this.cloudVBO == null || force) {
                if (this.cloudVBO != null) {
                    this.cloudVBO.close();
                }
                this.cloudVBO = new VertexBuffer(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                renderSphere(bufferbuilder, 0, 0, 0, new Vector3d(1, 1, 1), 0.1F);
                bufferbuilder.finishDrawing();
                this.cloudVBO.upload(bufferbuilder);
            }

            boolean uhh = true;
            this.cloudsNeedUpdate = true;

            if (uhh && this.cloudsNeedUpdate) {
                this.cloudsNeedUpdate = false;
                if (this.cloudsVBO != null) {
                    this.cloudsVBO.close();
                }

                this.cloudsVBO = new VertexBuffer(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

                //this.drawClouds(bufferbuilder, d2, d3, d4, vector3d);
                PlayerEntity player = Minecraft.getInstance().player;
                double playerX = player.getPosX();// - ((player.getPosX() - player.prevPosX) * partialTicks);
                //playerX = (MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX()) - player.getPosX());
                double playerY = player.getPosY();
                double playerZ = player.getPosZ();
                //playerZ = (MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ()) - player.getPosZ());

                double x = 5911 - (playerX);
                double y = 250 - viewEntityY + 0.001;
                double z = 6972 - (playerZ);

                Random rand = new Random(3);

                int randRangeY = 40;
                int randRange = 250;

                int randRangeY2 = 16;
                int randRange2 = 20;

                int clusters = 50;
                int clusterDensity = 50;

                bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

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
                        renderSphere(bufferbuilder, 0, 10, 0, new Vector3d(r, r, r), 0.1F);
                        index++;
                    }
                }

                bufferbuilder.finishDrawing();
                this.cloudsVBO.upload(bufferbuilder);
            }

            /*matrixStackIn.push();*/
            //matrixStackIn.scale(12.0F, 1.0F, 12.0F);
            //matrixStackIn.translate((double)(-f3), (double)f4, (double)(-f5));
            if (this.cloudsVBO != null && uhh && false) {
                this.cloudsVBO.bindBuffer();
                DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.setupBufferState(0L);

                this.cloudsVBO.draw(matrixStackIn.getLast().getMatrix(), GL11.GL_QUADS);

                VertexBuffer.unbindBuffer();
                DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.clearBufferState();
            }

            /*matrixStackIn.pop();
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableAlphaTest();
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableFog();*/

            boolean vboEachCloud = true;

            if (vboEachCloud) {
                PlayerEntity player = Minecraft.getInstance().player;
                double playerX = player.getPosX();// - ((player.getPosX() - player.prevPosX) * partialTicks);
                //playerX = (MathHelper.lerp(partialTicks, player.prevPosX, player.getPosX()) - player.getPosX());
                double playerY = player.getPosY();
                double playerZ = player.getPosZ();
                //playerZ = (MathHelper.lerp(partialTicks, player.prevPosZ, player.getPosZ()) - player.getPosZ());

                double x = 5911 - (playerX);
                double y = 250 - viewEntityY + 0.001;
                double z = 6972 - (playerZ);

                Random rand = new Random(3);

                int randRangeY = 40;
                int randRange = 250;

                int randRangeY2 = 16;
                int randRange2 = 20;

                int clusters = 50;
                int clusterDensity = 500;

                //bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

                this.cloudsVBO.bindBuffer();
                DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.setupBufferState(0L);

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

                        matrixStackIn.push();
                        matrixStackIn.translate(x + xx + xxx, y + 10 + yy + yyy, z + zz + zzz);
                        matrixStackIn.scale(scale, scale, scale);

                        this.cloudsVBO.draw(matrixStackIn.getLast().getMatrix(), GL11.GL_QUADS);

                        matrixStackIn.pop();

                        index++;
                    }
                }

                VertexBuffer.unbindBuffer();
                DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL.clearBufferState();

                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableAlphaTest();
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                RenderSystem.disableFog();
            }
        }
    }

    private void renderCube(BufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vector3d cloudsColor, float scale) {
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

        float f7 = sprite.getMinU();
        float f8 = sprite.getMaxU();
        float f5 = sprite.getMinV();
        float f6 = sprite.getMaxV();


        float particleRed = (float) cloudsColor.x;
        float particleGreen = (float) cloudsColor.y;
        float particleBlue = (float) cloudsColor.z;
        float particleAlpha = 1;

        bufferIn.pos(avector3f3[0].getX(), avector3f3[0].getY(), avector3f3[0].getZ()).tex(f8, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferIn.pos(avector3f3[1].getX(), avector3f3[1].getY(), avector3f3[1].getZ()).tex(f8, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferIn.pos(avector3f3[2].getX(), avector3f3[2].getY(), avector3f3[2].getZ()).tex(f7, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(0.0F, -1.0F, 0.0F).endVertex();
        bufferIn.pos(avector3f3[3].getX(), avector3f3[3].getY(), avector3f3[3].getZ()).tex(f7, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(0.0F, -1.0F, 0.0F).endVertex();
    }

    private void renderSphere(BufferBuilder bufferIn, double cloudsX, double cloudsY, double cloudsZ, Vector3d cloudsColor, float scale) {
        TextureAtlasSprite sprite = ParticleRegistry.cloud_square;

        float f7 = sprite.getMinU();
        float f8 = sprite.getMaxU();
        float f5 = sprite.getMinV();
        float f6 = sprite.getMaxV();

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

                bufferIn.pos(x1 * zr0 * radius2 + cloudsX, y1 * zr0 * radius2 + cloudsY, z0 * radius2 + cloudsZ).tex(f7, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(n1, n2, n3).endVertex();
                bufferIn.pos(x0 * zr0 * radius2 + cloudsX, y0 * zr0 * radius2 + cloudsY, z0 * radius2 + cloudsZ).tex(f8, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(n1, n2, n3).endVertex();;
                bufferIn.pos(x0 * zr1 * radius2 + cloudsX, y0 * zr1 * radius2 + cloudsY, z1 * radius2 + cloudsZ).tex(f8, f5).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(n1, n2, n3).endVertex();
                bufferIn.pos(x1 * zr1 * radius2 + cloudsX, y1 * zr1 * radius2 + cloudsY, z1 * radius2 + cloudsZ).tex(f7, f6).color(particleRed, particleGreen, particleBlue, particleAlpha).normal(n1, n2, n3).endVertex();

                iter2++;
            }

            /*Tessellator tessellator = Tessellator.getInstance();
            tessellator.draw();*/

            iter++;

            //glEnd();
        }
    }
}
