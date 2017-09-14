package weather2.client.block;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import weather2.Weather;
import weather2.block.TileEntityWindVane;

public class TileEntityWindVaneRenderer extends TileEntitySpecialRenderer
{
	public ModelWindVane model;
	public ResourceLocation texture = new ResourceLocation(Weather.modID + ":textures/blocks/windvane_custom.png");
	
	public TileEntityWindVaneRenderer() {
		model = new ModelWindVane();
	}

	@Override
	public void render(TileEntity var1, double x, double y, double z, float var8, int what, float alpha) {
    	
    	
    	
    	//TEMP
    	//model = new ModelWindVane();
    	
    	float renderAngle = ((TileEntityWindVane)var1).smoothAngle - 90;
    	
    	float scale = 1F;
    	
    	model.scaleX = scale;
    	model.scaleY = scale;
    	model.scaleZ = scale;
    	
    	GL11.glPushMatrix();
    	
    	GL11.glTranslatef((float) x + 0.5f, (float) y + 0F, (float) z + 0.5f);
    	
    	//renderModel(tile.type, tile.rotation, false);
    	
    	boolean isTransparent = false;
    	boolean isInv = false;
    	
    	this.bindTexture(texture);
    	float rotation = renderAngle/* * 360 / 16.0F*/;
    	if (isTransparent) {
	    	GL11.glEnable(GL11.GL_NORMALIZE);
	    	GL11.glEnable(GL11.GL_BLEND);
	    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    	}
    	if (isInv) {
	    	GL11.glTranslatef(0, 1f * model.scaleY * model.scaleItem, 0);
	    	GL11.glScalef(model.scaleItem, model.scaleItem, model.scaleItem);
	    	GL11.glRotatef(180, 0, 1, 0);
    	} else {
    		GL11.glTranslatef(0, 1.5f * model.scaleY, 0);
    	}
    	GL11.glRotatef(180, 0, 0, 1);
    	
    	//make note of this...
    	//GL11.glRotatef(renderAngle, 0, 1, 0);
    	//GL11.glRotatef(rotation, 0, 1, 0);
    	
    	if (!isInv) {
    		GL11.glTranslatef(model.offsetX, model.offsetY, model.offsetZ);
    	} else {
    		GL11.glTranslatef(model.offsetInvX, model.offsetInvY, 0);
    	}
    	
    	GL11.glScalef(model.scaleX, model.scaleY, model.scaleZ); 
    	
    	//GL11.glDisable(GL11.GL_LIGHTING);
    	
    	//GL11.glColor4f(1F, 1F, 1F, 1F);
    	Tessellator tessellator = Tessellator.getInstance();
    	//tessellator.setBrightness(9999999);

    	//tessellator.setBrightness(var1.getBlockType().getMixedBrightnessForBlock(var1.world, MathHelper.floor(var2), MathHelper.floor(var4), MathHelper.floor(var6)));
    	
    	float br = 0.9F;
    	//GL11.glColor4f(br, br, br, 1F);

    	model.render(0.0625F, renderAngle);
    	
    	//GL11.glEnable(GL11.GL_LIGHTING);

    	GL11.glPopMatrix();
    	
    }
}
