package weather2.client.block;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import weather2.Weather;
import weather2.block.TileEntityAnemometer;

public class TileEntityAnemometerRenderer extends TileEntitySpecialRenderer
{
	public ModelAnemometer model;
	public ResourceLocation texture = new ResourceLocation(Weather.modID + ":textures/blocks/anemometer_custom.png");
	
	public TileEntityAnemometerRenderer() {
		model = new ModelAnemometer();
	}

	@Override
    public void render(TileEntity var1, double var2, double var4, double var6, float var8, int what, float alpha) {
    	
    	
    	
    	//TEMP
    	//model = new ModelWindVane();
    	
    	//SAVE FOR FUTURE ISSUES
    	//custom forcing max brightness
    	//val i: Int = 15728880
    	//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (i % 65536).asInstanceOf[Float] / 1F, (i / 65536).asInstanceOf[Float] / 1F)
		
		//if (((TileEntityAnemometer)var1).smoothAngle > 180) ((TileEntityAnemometer)var1).smoothAngle -= 360;
		//if (((TileEntityAnemometer)var1).smoothAnglePrev > 180) ((TileEntityAnemometer)var1).smoothAnglePrev -= 360;
    	
    	float renderAngle = ((TileEntityAnemometer)var1).smoothAnglePrev + (((TileEntityAnemometer)var1).smoothAngle - ((TileEntityAnemometer)var1).smoothAnglePrev) * var8;
    	
    	//Weather.dbg("a: " + (((TileEntityAnemometer)var1).smoothAngle + "b: " + ((TileEntityAnemometer)var1).smoothAnglePrev));
    	
    	float scale = 1F;
    	
    	model.scaleX = scale;
    	model.scaleY = scale;
    	model.scaleZ = scale;
    	
    	GL11.glPushMatrix();
    	
    	GL11.glTranslatef((float) var2 + 0.5f, (float) var4 + 0F, (float) var6 + 0.5f);
    	
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
