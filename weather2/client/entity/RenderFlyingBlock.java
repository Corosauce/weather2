package weather2.client.entity;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import weather2.entity.EntityMovingBlock;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFlyingBlock extends Render
{
	Block renderBlock;
	
    public RenderFlyingBlock(Block parBlock)
    {
    	renderBlock = parBlock;
    }
    
    @Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TextureMap.locationBlocksTexture;
	}

    @Override
    public void doRender(Entity var1, double var2, double var4, double var6, float var8, float var9)
    {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_FOG);
        
        int age = var1.ticksExisted * 5;
        
        float size = 0.7F;// - (age * 0.03F);
        
        if (size < 0) size = 0;
        
        if (var1 instanceof EntityMovingBlock) {
        	size = 1;
        }
        
        GL11.glTranslatef((float)var2, (float)var4, (float)var6);
        this.bindEntityTexture(var1);
        //this.loadTexture("/terrain.png");
        World var11 = var1.worldObj;
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glRotatef((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
        GL11.glRotatef((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
        RenderBlocks rb = new RenderBlocks(var1.worldObj);
        GL11.glScalef(size, size, size);
        //Tessellator tess = Tessellator.instance;
        //tess.setBrightness(255);
        //tess.setColorOpaque_F(255, 255, 255);
        //renderBlock = Block.netherrack;
        if (var1 instanceof EntityMovingBlock) {
        	Block dynamicRenderBlock = Block.blocksList[((EntityMovingBlock) var1).tile];
        	rb.setRenderBoundsFromBlock(dynamicRenderBlock);
	        rb.renderBlockAsItem(dynamicRenderBlock, 0, 0.8F);
        } else {
	        rb.setRenderBoundsFromBlock(renderBlock);
	        rb.renderBlockAsItem(renderBlock, 0, 0.8F);
        }
        
        GL11.glEnable(GL11.GL_FOG);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }
}
