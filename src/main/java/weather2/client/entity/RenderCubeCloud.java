package weather2.client.entity;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import weather2.weathersystem.storm.StormObject;

@SideOnly(Side.CLIENT)
public class RenderCubeCloud extends Render
{
	
    public RenderCubeCloud()
    {
    	
    }
    
    @Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TextureMap.locationBlocksTexture;
	}

    public void doRenderClouds(StormObject parStorm, double var2, double var4, double var6, float var8, float var9)
    {
    	
    	EntityPlayer entP = FMLClientHandler.instance().getClient().thePlayer;
    	if (entP == null) return;
    	
        GL11.glPushMatrix();
        //GL11.glDisable(GL11.GL_FOG);
        //GL11.glEnable(GL11.GL_LIGHTING);
        
        int age = 0;//var1.ticksExisted * 5;
        
        float size = 80F;// - (age * 0.03F);
        
        if (size < 0) size = 0;
        
        
        
        GL11.glTranslatef((float)(var2 - entP.posX), (float)(var4 - entP.posY), (float)(var6 - entP.posZ));
        
        RenderManager.instance.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        //this.loadTexture("/terrain.png");
        World var11 = parStorm.manager.getWorld();
        //GL11.glDisable(GL11.GL_LIGHTING);
        
        RenderBlocks rb = new RenderBlocks(var11);
        GL11.glScalef(size, size, size);
        //Tessellator tess = Tessellator.instance;
        //tess.setBrightness(255);
        //tess.setColorOpaque_F(255, 255, 255);
        //renderBlock = Block.netherrack;
        
        Block renderBlock = Blocks.ice;
        
    	//the real one
        rb.setRenderBoundsFromBlock(renderBlock);
        rb.renderBlockAsItem(renderBlock, 0, 0.8F);
        
        //the fake ones
    	/*for (int i = 0; i < Math.min(4, WeatherUtilParticle.maxRainDrops); i++) {
    		GL11.glPushMatrix();
    		GL11.glTranslatef((float)WeatherUtilParticle.rainPositions[i].xCoord * 3F, (float)WeatherUtilParticle.rainPositions[i].yCoord * 3F, (float)WeatherUtilParticle.rainPositions[i].zCoord * 3F);
    		GL11.glRotatef((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
            GL11.glRotatef((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
    		rb.setRenderBoundsFromBlock(renderBlock);
	        rb.renderBlockAsItem(renderBlock, 0, 0.8F);
    		//GL11.glTranslatef((float)-WeatherUtilParticle.rainPositions[i].xCoord, (float)-WeatherUtilParticle.rainPositions[i].yCoord, (float)-WeatherUtilParticle.rainPositions[i].zCoord);
    		GL11.glPopMatrix();
    	}*/
        	
        
        
        //GL11.glEnable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

	@Override
	public void doRender(Entity entity, double d0, double d1, double d2,
			float f, float f1) {
		//blank
	}
}
