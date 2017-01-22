package weather2.client.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import weather2.ClientProxy;
import weather2.api.WindReader;
import weather2.block.TileEntityWeatherForecast;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;
import extendedrenderer.ExtendedRenderer;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

public class TileEntityWeatherForecastRenderer extends TileEntitySpecialRenderer
{
    public void renderTileEntityAt(TileEntity var1, double x, double y, double z, float var8, int destroyStage) {
    	
    	//renderTileEntityAtTest(var1, x, y, z, var8, destroyStage);
    	
    	//renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconLightning);
    	
    	//if (true) return;
    	
    	TileEntityWeatherForecast tEnt = (TileEntityWeatherForecast) var1;
    	
    	String particleCount = ExtendedRenderer.rotEffRenderer.getStatistics();
    	
    	//GL11.glColor4f(1F, 1F, 1F, 1F);
    	
    	StormObject so = tEnt.lastTickStormObject;
    	
    	Vec3 pos = new Vec3(tEnt.getPos().getX(), tEnt.getPos().getY(), tEnt.getPos().getZ());
    	
    	String descSeverity = "";
    	String descDist = "";
    	String descWindAngleCloud = "Wind Angle Clouds: " + (int)WindReader.getWindAngle(var1.getWorld(), pos, WindReader.WindType.CLOUD);
    	String descWindAngle = "Wind Angle Effect: " + (int)WindReader.getWindAngle(var1.getWorld(), pos, WindReader.WindType.DOMINANT);
    	String descWindSpeed = "Wind Speed Effect: " + (((int)(WindReader.getWindSpeed(var1.getWorld(), pos, WindReader.WindType.DOMINANT) * 100F)) / 100F);
    	
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
    		
    		Vec3 posXZ = new Vec3(tEnt.getPos().getX(), so.pos.yCoord, tEnt.getPos().getZ());
    		
    		descDist = "" + (int)posXZ.distanceTo(so.pos);
    	}
    	
    	int index = 1;

    	boolean oldMode = false;
    	
    	float yOffset = 2.5F;
    		
		float sizeSimBoxDiameter = 2048;
		float sizeRenderBoxDiameter = 3;
		
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x + 0.5F, (float)y+1.1F, (float)z + 0.5F);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.disableLighting();
        //GlStateManager.depthMask(false);
        //GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)-(sizeRenderBoxDiameter/2), 0, -(double)(sizeRenderBoxDiameter/2)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)-(sizeRenderBoxDiameter/2), 0, (double)(sizeRenderBoxDiameter/2)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(sizeRenderBoxDiameter/2), 0, (double)(sizeRenderBoxDiameter/2)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(sizeRenderBoxDiameter/2), 0, -(double)(sizeRenderBoxDiameter/2)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        //GlStateManager.enableDepth();
        //GlStateManager.depthMask(true);
        
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
		
        renderLivingLabel("\u00A7" + '6' + "|", x, y + 1.2F, z, 1, 10, 10, Minecraft.getMinecraft().getRenderManager().playerViewY);
        
		for (int i = 0; i < tEnt.storms.size(); i++) {

            WeatherObject wo = tEnt.storms.get(i);
			
			GL11.glPushMatrix();
			
            Vec3 posRenderOffset = new Vec3(wo.pos.xCoord - tEnt.getPos().getX(), 0, wo.pos.zCoord - tEnt.getPos().getZ());
            posRenderOffset.xCoord /= sizeSimBoxDiameter;
            posRenderOffset.zCoord /= sizeSimBoxDiameter;
            
            posRenderOffset.xCoord *= sizeRenderBoxDiameter;
            posRenderOffset.zCoord *= sizeRenderBoxDiameter;
            
            //Icon particleIcon = CommonProxy.blockWeatherDeflector.getBlockTextureFromSide(0);
            
            GL11.glTranslated(posRenderOffset.xCoord, 0, posRenderOffset.zCoord);

            if (wo instanceof StormObject) {
                StormObject storm = (StormObject)wo;

                if (storm.levelCurIntensityStage >= StormObject.STATE_FORMING) {
                    if (storm.stormType == StormObject.TYPE_WATER) {
                        renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconCyclone);
                        renderLivingLabel("C" + (int)(storm.levelCurIntensityStage - StormObject.levelStormIntensityFormingStartVal), x, y + 1.5F, z, 1, 15, 5, Minecraft.getMinecraft().getRenderManager().playerViewY);
                    } else {
                        renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconTornado);
                        renderLivingLabel("EF" + (int)(storm.levelCurIntensityStage - StormObject.levelStormIntensityFormingStartVal), x, y + 1.5F, z, 1, 20, 5, Minecraft.getMinecraft().getRenderManager().playerViewY);
                    }
                } else if (storm.levelCurIntensityStage >= StormObject.STATE_HAIL) {
                    renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconHail);
                    renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconWind);
                } else if (storm.levelCurIntensityStage >= StormObject.STATE_HIGHWIND) {
                    renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconLightning);
                    renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconWind);
                } else if (storm.levelCurIntensityStage >= StormObject.STATE_THUNDER) {
                    renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconLightning);
                } else {
                    renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconRain);
                }

                if (storm.hasStormPeaked && (storm.levelCurIntensityStage > storm.STATE_NORMAL)) {
                    renderLivingLabel("\u00A7" + '4' + "|", x, y + 1.2F, z, 1, 5, 5, Minecraft.getMinecraft().getRenderManager().playerViewY);
                } else {
                    renderLivingLabel("\u00A7" + '2' + "|", x, y + 1.2F, z, 1, 5, 5, Minecraft.getMinecraft().getRenderManager().playerViewY);
                }
            } else if (wo instanceof WeatherObjectSandstorm) {
                renderIconNew(x, y + 1.4F, z, 16, 16, Minecraft.getMinecraft().getRenderManager().playerViewY, ClientProxy.radarIconSandstorm);
                if (((WeatherObjectSandstorm)wo).isFrontGrowing) {
                    renderLivingLabel("\u00A7" + '2' + "|", x, y + 1.2F, z, 1, 5, 5, Minecraft.getMinecraft().getRenderManager().playerViewY);
                } else {
                    renderLivingLabel("\u00A7" + '4' + "|", x, y + 1.2F, z, 1, 5, 5, Minecraft.getMinecraft().getRenderManager().playerViewY);
                }
            }
            
        	//renderLivingLabel("r", x, y + 1.4F, z, 1, 10, 10, Minecraft.getMinecraft().getRenderManager().playerViewY);
            
            GL11.glTranslated(-posRenderOffset.xCoord, 0, -posRenderOffset.zCoord);
            
            GL11.glPopMatrix();
		}
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, float angle)
    {
    	renderLivingLabel(par2Str, par3, par5, par7, par9, 200, 80, angle);
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, int width, int height, float angle)
    {
        int borderSize = 2;
        
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    	
        FontRenderer var11 = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
        float var12 = 0.6F;
        float var13 = 0.016666668F * var12;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)par3 + 0.5F, (float)par5, (float)par7 + 0.5F);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
        GL11.glScalef(-var13, -var13, var13);
        GL11.glDisable(GL11.GL_LIGHTING);
        
        if (par9 == 0) {
            GL11.glDepthMask(false);
            //GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Tessellator var14 = Tessellator.getInstance();
            VertexBuffer worldrenderer = var14.getBuffer();
            byte var15 = 0;
            
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            
            worldrenderer
    		.color(0.0F, 0.0F, 0.0F, 0.25F)
    		.pos((double)(-width / 2 - borderSize), (double)(-borderSize + var15), 0.0D)
    		
    		.endVertex();
            
            worldrenderer
    		.color(0.0F, 0.0F, 0.0F, 0.25F)
    		.pos((double)(-width / 2 - borderSize), (double)(height + var15), 0.0D)
    		
    		.endVertex();
            
            worldrenderer
    		.color(0.0F, 0.0F, 0.0F, 0.25F)
    		.pos((double)(width / 2 + borderSize), (double)(height + var15), 0.0D)
    		
    		.endVertex();
            
            worldrenderer
    		.color(0.0F, 0.0F, 0.0F, 0.25F)
    		.pos((double)(width / 2 + borderSize), (double)(-borderSize + var15), 0.0D)
    		
    		.endVertex();
            
            var14.draw();
        }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        var11.drawString(par2Str, -width/2+borderSize, 0, 0xFFFFFF);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
        
        GL11.glEnable(GL11.GL_CULL_FACE);
    }
    
    public void renderIconNew(double x, double y, double z, int width, int height, float angle, TextureAtlasSprite parIcon) {
    	float f6 = parIcon.getMinU();
        float f7 = parIcon.getMaxU();
        float f9 = parIcon.getMinV();
        float f8 = parIcon.getMaxV();
        
        float var12 = 0.6F;
        float var13 = 0.016666668F * var12;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x + 0.5F, (float)y, (float)z + 0.5F);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-angle, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-var13, -var13, var13);
        
        int borderSize = 2;
        
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        
        GlStateManager.disableFog();
        
        this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        
        float r = 1F;
        float g = 1F;
        float b = 1F;
        
        worldrenderer
        .pos((double)(-width / 2 - borderSize), (double)(-borderSize), 0.0D)
        .tex(f6, f9)
        .color(r, g, b, 1.0F).endVertex();
        
        worldrenderer
        .pos((double)(-width / 2 - borderSize), (double)(height), 0.0D)
        .tex(f6, f8)
        .color(r, g, b, 1.0F).endVertex();
        
        worldrenderer
        .pos((double)(width / 2 + borderSize), (double)(height), 0.0D)
        .tex(f7, f8)
        .color(r, g, b, 1.0F).endVertex();
        
        worldrenderer
        .pos((double)(width / 2 + borderSize), (double)(-borderSize), 0.0D)
        .tex(f7, f9)
        .color(r, g, b, 1.0F).endVertex();
        
        tessellator.draw();
        
        GL11.glPopMatrix();
    }
}
