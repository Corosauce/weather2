package weather2.client.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import CoroUtil.util.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.ClientTickHandler;
import weather2.entity.EntityLightningBoltCustom;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

@SideOnly(Side.CLIENT)
public class RenderLightningBoltCustom extends Render<EntityLightningBoltCustom>
{
    public RenderLightningBoltCustom(RenderManager renderManagerIn)
    {
        super(renderManagerIn);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity>) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doe
     */
    @Override
    public void doRender(EntityLightningBoltCustom entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 1);
        GlStateManager.disableCull();
        
        //Random random = new Random(entity.boltVertex);
        
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        
        double xx = x;
        double yy = y;
        double zz = z;
        
        float r = 1F;
        float g = 1F;
        float b = 1F;
        float alpha = 0.4F;
        
        
        
        
        
        
        
        //listVec.add(new Vec3d(0, 0, 0));
        //listVec.add(new Vec3d(13, 13, 13));
        /*worldrenderer.pos(xx+0, yy+0, zz+0).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
        worldrenderer.pos(xx+1, yy+1, zz+1).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
        worldrenderer.pos(xx+2, yy+2, zz+2).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
        worldrenderer.pos(xx+3, yy+3, zz+3).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();*/
        
        //worldrenderer.pos(xx+0, yy+0, zz+0).color(0.45F, 0.45F, 0.5F, 1F).endVertex();
        
        //worldrenderer.pos(xx+1, yy+0, zz+1).color(0.45F, 0.45F, 0.5F, 1F).endVertex();
        //worldrenderer.pos(xx+2, yy+0, zz+2).color(0.45F, 0.45F, 0.5F, 1F).endVertex();
        //worldrenderer.pos(xx+3, yy+0, zz+3).color(0.45F, 0.45F, 0.5F, 1F).endVertex();
        
        double sizeRadius = 0.3D;

        //temp - visualize sandstorm
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        World world = mc.world;
        Vec3 posPlayer = new Vec3(mc.player.posX, 0/*mc.player.posY*/, mc.player.posZ);
        WeatherObjectSandstorm sandstorm = ClientTickHandler.weatherManager.getClosestSandstormByIntensity(posPlayer);
        if (sandstorm != null) {
            List<Vec3> wat = sandstorm.getSandstormAsShape();
            entity.listVec.clear();
            for (Vec3 wat2 : wat) {
                Vec3d wat3 = new Vec3d(wat2.xCoord - player.posX, -10, wat2.zCoord - player.posZ);
                entity.listVec.add(wat3);

                /*if (world.getTotalWorldTime() % 20 == 0) {
                    System.out.println("wat: " + wat3);
                }*/
            }


        }

        if (world.getTotalWorldTime() % 20 == 0) {
            //System.out.println("----------");
        }

        for (int i = 0; i < entity.listVec.size() - 1; i++) {
        	Vec3d vec = entity.listVec.get(i);
        	Vec3d vec2 = entity.listVec.get(i+1);

            if (world.getTotalWorldTime() % 20 == 0) {
                //System.out.println("wat: " + vec + " --- " + vec2);
            }

        	/*worldrenderer.pos(vec.x - sizeRadius + x, vec.y + y, vec.z - sizeRadius + z).color(r, g, b, alpha).endVertex();
            worldrenderer.pos(vec.x + sizeRadius + x, vec.y + y, vec.z - sizeRadius + z).color(r, g, b, alpha).endVertex();
            worldrenderer.pos(vec2.x + sizeRadius + x, vec2.y + y, vec2.z + sizeRadius + z).color(r, g, b, alpha).endVertex();
            worldrenderer.pos(vec2.x - sizeRadius + x, vec2.y + y, vec2.z + sizeRadius + z).color(r, g, b, alpha).endVertex();*/

        	//temp - visualize sandstorm
            worldrenderer.pos(vec.x - sizeRadius, vec.y, vec.z - sizeRadius).color(r, g, b, alpha).endVertex();
            worldrenderer.pos(vec.x + sizeRadius, vec.y, vec.z - sizeRadius).color(r, g, b, alpha).endVertex();
            worldrenderer.pos(vec2.x + sizeRadius, vec2.y, vec2.z + sizeRadius).color(r, g, b, alpha).endVertex();
            worldrenderer.pos(vec2.x - sizeRadius, vec2.y, vec2.z + sizeRadius).color(r, g, b, alpha).endVertex();
        }

        //temp - visualize sandstorm
        Vec3d vec = entity.listVec.get(0);
        Vec3d vec2 = entity.listVec.get(entity.listVec.size()-1);
        worldrenderer.pos(vec.x - sizeRadius, vec.y, vec.z - sizeRadius).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(vec.x + sizeRadius, vec.y, vec.z - sizeRadius).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(vec2.x + sizeRadius, vec2.y, vec2.z + sizeRadius).color(r, g, b, alpha).endVertex();
        worldrenderer.pos(vec2.x - sizeRadius, vec2.y, vec2.z + sizeRadius).color(r, g, b, alpha).endVertex();
        
        //worldrenderer.pos(-1, 0, -1).color(0.45F, 0.45F, 0.5F, 1F).endVertex();
        
        tessellator.draw();
        
        /*double d6 = 0;
        double d7 = 0;
        double d2 = 0;
        double d4 = 0;
        double i1 = 0;
        double d3 = 0;
        double d5 = 0;
        
        for (int j1 = 0; j1 < 5; ++j1)
        {
            double d8 = x + 0.5D - d6;
            double d9 = z + 0.5D - d6;

            if (j1 == 1 || j1 == 2)
            {
                d8 += d6 * 2.0D;
            }

            if (j1 == 2 || j1 == 3)
            {
                d9 += d6 * 2.0D;
            }

            double d10 = x + 0.5D - d7;
            double d11 = z + 0.5D - d7;

            if (j1 == 1 || j1 == 2)
            {
                d10 += d7 * 2.0D;
            }

            if (j1 == 2 || j1 == 3)
            {
                d11 += d7 * 2.0D;
            }

            worldrenderer.pos(d10 + d2, y + (double)(i1 * 16), d11 + d3).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
            worldrenderer.pos(d8 + d4, y + (double)((i1 + 1) * 16), d9 + d5).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
        }*/
        
        //tessellator.draw();
        
        /*double[] adouble = new double[8];
        double[] adouble1 = new double[8];
        double d0 = 0.0D;
        double d1 = 0.0D;
        Random random = new Random(entity.boltVertex);

        for (int i = 7; i >= 0; --i)
        {
            adouble[i] = d0;
            adouble1[i] = d1;
            d0 += (double)(random.nextInt(11) - 5);
            d1 += (double)(random.nextInt(11) - 5);
        }

        for (int k1 = 0; k1 < 4; ++k1)
        {
            Random random1 = new Random(entity.boltVertex);

            for (int j = 0; j < 3; ++j)
            {
                int k = 7;
                int l = 0;

                if (j > 0)
                {
                    k = 7 - j;
                }

                if (j > 0)
                {
                    l = k - 2;
                }

                double d2 = adouble[k] - d0;
                double d3 = adouble1[k] - d1;

                for (int i1 = k; i1 >= l; --i1)
                {
                    double d4 = d2;
                    double d5 = d3;

                    if (j == 0)
                    {
                        d2 += (double)(random1.nextInt(11) - 5);
                        d3 += (double)(random1.nextInt(11) - 5);
                    }
                    else
                    {
                        d2 += (double)(random1.nextInt(31) - 15);
                        d3 += (double)(random1.nextInt(31) - 15);
                    }

                    worldrenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);
                    float f = 0.5F;
                    float f1 = 0.45F;
                    float f2 = 0.45F;
                    float f3 = 0.5F;
                    double d6 = 0.1D + (double)k1 * 0.2D;

                    if (j == 0)
                    {
                        d6 *= (double)i1 * 0.1D + 1.0D;
                    }

                    double d7 = 0.1D + (double)k1 * 0.2D;

                    if (j == 0)
                    {
                        d7 *= (double)(i1 - 1) * 0.1D + 1.0D;
                    }

                    for (int j1 = 0; j1 < 5; ++j1)
                    {
                        double d8 = x + 0.5D - d6;
                        double d9 = z + 0.5D - d6;

                        if (j1 == 1 || j1 == 2)
                        {
                            d8 += d6 * 2.0D;
                        }

                        if (j1 == 2 || j1 == 3)
                        {
                            d9 += d6 * 2.0D;
                        }

                        double d10 = x + 0.5D - d7;
                        double d11 = z + 0.5D - d7;

                        if (j1 == 1 || j1 == 2)
                        {
                            d10 += d7 * 2.0D;
                        }

                        if (j1 == 2 || j1 == 3)
                        {
                            d11 += d7 * 2.0D;
                        }

                        worldrenderer.pos(d10 + d2, y + (double)(i1 * 16), d11 + d3).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
                        worldrenderer.pos(d8 + d4, y + (double)((i1 + 1) * 16), d9 + d5).color(0.45F, 0.45F, 0.5F, 0.3F).endVertex();
                    }

                    tessellator.draw();
                }
            }
        }*/

        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(EntityLightningBoltCustom entity)
    {
        return null;
    }
}