package weather2.client.block;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;

import extendedrenderer.ExtendedRenderer;
import extendedrenderer.render.RotatingEffectRenderer;

import weather2.ClientTickHandler;
import weather2.api.WindReader;
import weather2.block.TileEntityWeatherForecast;
import weather2.block.TileEntityWindVane;
import weather2.weathersystem.storm.StormObject;

public class TileEntityWeatherForecastRenderer extends TileEntitySpecialRenderer
{
    public void renderTileEntityAt(TileEntity var1, double var2, double var4, double var6, float var8) {
    	
    	
    	TileEntityWeatherForecast tEnt = (TileEntityWeatherForecast) var1;
    	
    	String particleCount = ExtendedRenderer.rotEffRenderer.getStatistics();
    	
    	//GL11.glColor4f(1F, 1F, 1F, 1F);
    	renderLivingLabel("particles: " + particleCount, var2, var4 + 2, var6, 0, RenderManager.instance.playerViewY);
    	
    	StormObject so = tEnt.lastTickStormObject;
    	
    	Vec3 pos = Vec3.createVectorHelper(tEnt.xCoord, tEnt.yCoord, tEnt.zCoord);
    	
    	String descSeverity = "";
    	String descDist = "";
    	String descWindAngleCloud = "Wind Angle Clouds: " + (int)WindReader.getWindAngle(var1.worldObj, pos, WindReader.WindType.CLOUD);
    	String descWindAngle = "Wind Angle Effect: " + (int)WindReader.getWindAngle(var1.worldObj, pos, WindReader.WindType.DOMINANT);
    	String descWindSpeed = "Wind Speed Effect: " + (((int)(WindReader.getWindSpeed(var1.worldObj, pos, WindReader.WindType.DOMINANT) * 100F)) / 100F);
    	
    	String progression = "";
    	
    	if (so != null) {
    		
    		progression = "Growing ";
    		if (so.hasStormPeaked) {
    			progression = "Dying ";
    		}
    		
    		if (so.levelCurIntensityStage >= StormObject.STATE_STAGE5 + 1) {
    			descSeverity = "????";
    		} else if (so.levelCurIntensityStage >= StormObject.STATE_STAGE5) {
    			descSeverity = "F5 Tornado";
    			if (so.stormType == StormObject.TYPE_WATER) descSeverity = "Hurricane";
    		} else if (so.levelCurIntensityStage >= StormObject.STATE_STAGE4) {
    			descSeverity = "F4 Tornado";
    			if (so.stormType == StormObject.TYPE_WATER) descSeverity = "Tropical Cyclone Stage 4";
    		} else if (so.levelCurIntensityStage >= StormObject.STATE_STAGE3) {
    			descSeverity = "F3 Tornado";
    			if (so.stormType == StormObject.TYPE_WATER) descSeverity = "Tropical Cyclone Stage 3";
    		} else if (so.levelCurIntensityStage >= StormObject.STATE_STAGE2) {
    			descSeverity = "F2 Tornado";
    			if (so.stormType == StormObject.TYPE_WATER) descSeverity = "Tropical Cyclone Stage 2";
    		} else if (so.levelCurIntensityStage >= StormObject.STATE_STAGE1) {
    			descSeverity = "F1 Tornado";
    			if (so.stormType == StormObject.TYPE_WATER) descSeverity = "Tropical Cyclone Stage 1";
    		} else if (so.levelCurIntensityStage >= StormObject.STATE_FORMING) {
    			descSeverity = "Sign of Tornado";
    			if (so.stormType == StormObject.TYPE_WATER) descSeverity = "Sign of Tropical Cyclone";
    		} else if (so.levelCurIntensityStage >= StormObject.STATE_HAIL) {
    			descSeverity = "Hailstorm";
    		} else if (so.levelCurIntensityStage >= StormObject.STATE_HIGHWIND) {
    			descSeverity = "High wind";
    		} else if (so.levelCurIntensityStage >= StormObject.STATE_THUNDER) {
    			descSeverity = "Thunderstorm";
    		} else if (so.attrib_precipitation) {
    			descSeverity = "Rainstorm";
    			progression = "";
    		}
    		
    		Vec3 posXZ = Vec3.createVectorHelper(tEnt.xCoord, so.pos.yCoord, tEnt.zCoord);
    		
    		descDist = "" + (int)posXZ.distanceTo(so.pos);
    	}
    	
    	int index = 1;
    	
    	renderLivingLabel("closest storm type: " + progression + descSeverity, var2, var4 + 2F - 0.1F * index++, var6, 1, RenderManager.instance.playerViewY);
    	renderLivingLabel("closest storm dist: " + descDist, var2, var4 + 2F - 0.1F * index++, var6, 1, RenderManager.instance.playerViewY);
    	if (so != null) renderLivingLabel("closest storm intensity: " + ((int)(so.levelCurStagesIntensity * 100F) / 100F), var2, var4 + 2F - 0.1F * index++, var6, 1, RenderManager.instance.playerViewY);
    	if (so != null) renderLivingLabel("closest storm water level: " + (so.attrib_precipitation ? so.levelWater : 0), var2, var4 + 2F - 0.1F * index++, var6, 1, RenderManager.instance.playerViewY);
    	renderLivingLabel(descWindAngleCloud, var2, var4 + 2F - 0.1F * index++, var6, 1, RenderManager.instance.playerViewY);
    	renderLivingLabel(descWindAngle, var2, var4 + 2F - 0.1F * index++, var6, 1, RenderManager.instance.playerViewY);
    	renderLivingLabel(descWindSpeed, var2, var4 + 2F - 0.1F * index++, var6, 1, RenderManager.instance.playerViewY);
    	
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, float angle)
    {
    	renderLivingLabel(par2Str, par3, par5, par7, par9, 200, 80, angle);
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, int width, int height, float angle)
    {
        //float var10 = par1EntityLivingBase.getDistanceToEntity(this.renderManager.livingPlayer);

        int borderSize = 2;
        
        GL11.glDisable(GL11.GL_CULL_FACE);
    	
        //if (var10 <= (float)par9)
        //{
            FontRenderer var11 = RenderManager.instance.getFontRenderer();
            float var12 = 0.6F;
            float var13 = 0.016666668F * var12;
            GL11.glPushMatrix();
            GL11.glTranslatef((float)par3 + 0.5F, (float)par5, (float)par7 + 0.5F);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
            //GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
            GL11.glScalef(-var13, -var13, var13);
            GL11.glDisable(GL11.GL_LIGHTING);
            
            if (par9 == 0) {
	            GL11.glDepthMask(false);
	            //GL11.glDisable(GL11.GL_DEPTH_TEST);
	            GL11.glEnable(GL11.GL_BLEND);
	            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	            Tessellator var14 = Tessellator.instance;
	            byte var15 = 0;
	            
	            GL11.glDisable(GL11.GL_TEXTURE_2D);
	            var14.startDrawingQuads();
	            //int width = var11.getStringWidth(par2Str) / 2;
            
            
	            var14.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
	            var14.addVertex((double)(-width / 2 - borderSize), (double)(-borderSize + var15), 0.0D);
	            var14.addVertex((double)(-width / 2 - borderSize), (double)(height + var15), 0.0D);
	            var14.addVertex((double)(width / 2 + borderSize), (double)(height + var15), 0.0D);
	            var14.addVertex((double)(width / 2 + borderSize), (double)(-borderSize + var15), 0.0D);
	            var14.draw();
            }
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            //var11.drawString(par2Str, -var11.getStringWidth(par2Str) / 2, var15, 553648127);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            var11.drawString(par2Str, -width/2+borderSize/*-var11.getStringWidth(par2Str) / 2*/, 0, 0xFFFFFF);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        //}
            
            GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
