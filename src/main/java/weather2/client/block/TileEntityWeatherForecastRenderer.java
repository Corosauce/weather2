package weather2.client.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import weather2.ClientProxy;
import weather2.ClientTickHandler;
import weather2.util.WindReader;
import weather2.block.TileEntityWeatherForecast;
import weather2.client.SceneEnhancer;
import weather2.config.ConfigMisc;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;
import extendedrenderer.ExtendedRenderer;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

public class TileEntityWeatherForecastRenderer extends TileEntitySpecialRenderer
{
    @Override
    public void render(TileEntity var1, double x, double y, double z, float var8, int destroyStage, float alpha) {
    	
    	//renderTileEntityAtTest(var1, x, y, z, var8, destroyStage);
    	
    	//renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconLightning);
    	
    	//if (true) return;
    	
    	TileEntityWeatherForecast tEnt = (TileEntityWeatherForecast) var1;
    	
    	String particleCount = ExtendedRenderer.rotEffRenderer.getStatistics();
    	
    	//GL11.glColor4f(1F, 1F, 1F, 1F);
    	
    	StormObject so = tEnt.lastTickStormObject;
    	
    	Vec3 pos = new Vec3(tEnt.getPos().getX(), tEnt.getPos().getY(), tEnt.getPos().getZ());
    	
    	String descSeverity = "";
    	String descDist = "";
    	String descWindAngleCloud = "Wind Angle Clouds: " + (int)WindReader.getWindAngle(var1.getWorld(), pos, WindReader.WindType.CLOUD);
    	String descWindAngle = "Wind Angle Effect: " + (int)WindReader.getWindAngle(var1.getWorld(), pos, WindReader.WindType.PRIORITY);
    	String descWindSpeed = "Wind Speed Effect: " + (((int)(WindReader.getWindSpeed(var1.getWorld(), pos, WindReader.WindType.PRIORITY) * 100F)) / 100F);
    	String descStage = "";
    	
    	String progression = "";

    	float levelWater = 0;
    	
    	if (so != null) {

            descStage = "" + so.levelCurIntensityStage;
    		
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

    		levelWater = so.levelWater;
    	}
    	
    	int index = 1;

    	boolean oldMode = false;
    	
    	float yOffset = 2.5F;
    		
		float sizeSimBoxDiameter = 2048;
		float sizeRenderBoxDiameter = 3;
		
		GlStateManager.pushMatrix();
        GlStateManager.translate((float)x + 0.5F, (float)y+1.1F, (float)z + 0.5F);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        //GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.disableLighting();
        //GlStateManager.depthMask(false);
        //GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.depthMask(false);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        GlStateManager.disableTexture2D();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)-(sizeRenderBoxDiameter/2), 0, -(double)(sizeRenderBoxDiameter/2)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)-(sizeRenderBoxDiameter/2), 0, (double)(sizeRenderBoxDiameter/2)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(sizeRenderBoxDiameter/2), 0, (double)(sizeRenderBoxDiameter/2)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(sizeRenderBoxDiameter/2), 0, -(double)(sizeRenderBoxDiameter/2)).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        //GlStateManager.enableDepth();
        //GlStateManager.depthMask(true);
        
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();

        //if (true) return;

        float playerViewY = Minecraft.getMinecraft().getRenderManager().playerViewY;
		
        renderLivingLabel("\u00A7" + '6' + "|", x, y + 1.2F, z, 1, 10, 10, playerViewY);

        if (ConfigMisc.radarCloudDebug) {
            EntityPlayer entP = Minecraft.getMinecraft().player;
            if (entP != null) {
                WeatherManagerClient wm = ClientTickHandler.weatherManager;
                float precipStr = Math.abs(SceneEnhancer.getRainStrengthAndControlVisuals(entP, true));
                boolean clientWeather2Rain = precipStr > 0;

                String rainThunder = entP.world.rainingStrength + " / " + entP.world.thunderingStrength;
                renderLivingLabel("\u00A7" + " rain/clear time: " + wm.vanillaRainTimeOnServer
                        , x, y + 1.9F, z, 1, 10, 10, playerViewY);
                renderLivingLabel("\u00A7" + " client weather: " +
                                (entP.world.isRaining() ? "raining, " : (clientWeather2Rain ? "light rain" : "")) + (entP.world.isThundering() ? "thundering" : "")
                        , x, y + 2.0F, z, 1, 10, 10, playerViewY);
                renderLivingLabel("\u00A7" + " server weather: " +
                                (wm.isVanillaRainActiveOnServer ? "raining, " : "") + (wm.isVanillaThunderActiveOnServer ? "thundering" : "")
                        , x, y + 2.1F, z, 1, 10, 10, playerViewY);
                renderLivingLabel("\u00A7" + " precip str: " + SceneEnhancer.getRainStrengthAndControlVisuals(entP), x, y + 2.2F, z, 1, 10, 10, playerViewY);
                renderLivingLabel("\u00A7" + " levelWater: " + levelWater, x, y + 2.3F, z, 1, 10, 10, playerViewY);
                renderLivingLabel("\u00A7" + " precip dist: " + descDist + ", stage: " + descStage, x, y + 2.4F, z, 1, 10, 10, playerViewY);
                renderLivingLabel("\u00A7" + " rainThunder: " + rainThunder, x, y + 2.5F, z, 1, 10, 10, playerViewY);

            }
        }
        
		for (int i = 0; i < tEnt.storms.size(); i++) {

            WeatherObject wo = tEnt.storms.get(i);

            GlStateManager.pushMatrix();
			
            Vec3 posRenderOffset = new Vec3(wo.pos.xCoord - tEnt.getPos().getX(), 0, wo.pos.zCoord - tEnt.getPos().getZ());
            posRenderOffset.xCoord /= sizeSimBoxDiameter;
            posRenderOffset.zCoord /= sizeSimBoxDiameter;
            
            posRenderOffset.xCoord *= sizeRenderBoxDiameter;
            posRenderOffset.zCoord *= sizeRenderBoxDiameter;
            
            //Icon particleIcon = CommonProxy.blockWeatherDeflector.getBlockTextureFromSide(0);

            GlStateManager.translate(posRenderOffset.xCoord, 0, posRenderOffset.zCoord);

            if (wo instanceof StormObject) {
                StormObject storm = (StormObject)wo;

                if (storm.levelCurIntensityStage >= StormObject.STATE_FORMING) {
                    if (storm.stormType == StormObject.TYPE_WATER) {
                        renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconCyclone);
                        renderLivingLabel("C" + (int)(storm.levelCurIntensityStage - StormObject.levelStormIntensityFormingStartVal), x, y + 1.5F, z, 1, 15, 5, playerViewY);
                    } else {
                        renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconTornado);
                        renderLivingLabel("F" + (int)(storm.levelCurIntensityStage - StormObject.levelStormIntensityFormingStartVal), x, y + 1.5F, z, 1, 12, 5, playerViewY);
                    }
                } else if (storm.levelCurIntensityStage >= StormObject.STATE_HAIL) {
                    renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconHail);
                    renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconWind);
                } else if (storm.levelCurIntensityStage >= StormObject.STATE_HIGHWIND) {
                    renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconLightning);
                    renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconWind);
                } else if (storm.levelCurIntensityStage >= StormObject.STATE_THUNDER) {
                    renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconLightning);
                } else if (storm.isPrecipitating()) {
                    renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconRain);
                }

                String charCode = "|";
                if (ConfigMisc.radarCloudDebug) {
                    if (storm.levelTemperature > 0) {
                        charCode = TextFormatting.DARK_RED.toString();
                    } else {
                        charCode = TextFormatting.BLUE.toString();
                    }
                }

                if (storm.levelCurIntensityStage > storm.STATE_NORMAL) {
                    if (storm.hasStormPeaked) {
                        renderLivingLabel("\u00A7" + '4' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
                    } else {
                        renderLivingLabel("\u00A7" + '2' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
                    }
                } else {
                    if (ConfigMisc.radarCloudDebug) {
                        if (storm.isCloudlessStorm()) {
                            renderLivingLabel(TextFormatting.BLACK + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
                        } else {
                            renderLivingLabel(charCode + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
                            //renderLivingLabel("\u00A7" + 'f' + charCode, x, y + 1.1F, z, 1, 5, 5, playerViewY);
                        }
                    } else {
                        renderLivingLabel(TextFormatting.WHITE + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
                    }
                }
            } else if (wo instanceof WeatherObjectSandstorm) {
                renderIconNew(x, y + 1.4F, z, 16, 16, playerViewY, ClientProxy.radarIconSandstorm);
                if (((WeatherObjectSandstorm)wo).isFrontGrowing) {
                    renderLivingLabel("\u00A7" + '2' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
                } else {
                    renderLivingLabel("\u00A7" + '4' + "|", x, y + 1.2F, z, 1, 5, 5, playerViewY);
                }
            }
            
        	//renderLivingLabel("r", x, y + 1.4F, z, 1, 10, 10, playerViewY);

            GlStateManager.translate(-posRenderOffset.xCoord, 0, -posRenderOffset.zCoord);

            GlStateManager.popMatrix();
		}
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, float angle)
    {
    	renderLivingLabel(par2Str, par3, par5, par7, par9, 200, 80, angle);
    }
    
    protected void renderLivingLabel(String par2Str, double par3, double par5, double par7, int par9, int width, int height, float angle)
    {

        int borderSize = 2;

        GlStateManager.disableCull();
        GlStateManager.disableTexture2D();
    	
        FontRenderer var11 = Minecraft.getMinecraft().getRenderManager().getFontRenderer();
        float var12 = 0.6F;
        float var13 = 0.016666668F * var12;
        GlStateManager.pushMatrix();
        //GL11.glPushMatrix();
        GlStateManager.translate(par3 + 0.5F, par5, par7 + 0.5F);
        //GL11.glTranslatef((float)par3 + 0.5F, (float)par5, (float)par7 + 0.5F);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        //GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-angle, 0.0F, 1.0F, 0.0F);
        //GL11.glRotatef(-angle, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-var13, -var13, var13);
        //GL11.glScalef(-var13, -var13, var13);
        GlStateManager.disableLighting();
        //GL11.glDisable(GL11.GL_LIGHTING);
        
        if (par9 == 0) {
            //GL11.glDepthMask(false);
            //GL11.glDisable(GL11.GL_DEPTH_TEST);
            GlStateManager.enableBlend();
            //GL11.glEnable(GL11.GL_BLEND);
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            //GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            Tessellator var14 = Tessellator.getInstance();
            BufferBuilder worldrenderer = var14.getBuffer();
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
        GlStateManager.enableTexture2D();
        //GL11.glEnable(GL11.GL_TEXTURE_2D);
        //GL11.glEnable(GL11.GL_DEPTH_TEST);
        //GL11.glDepthMask(true);
        var11.drawString(par2Str, -width/2+borderSize, 0, 0xFFFFFF);
        GlStateManager.enableLighting();
        //GL11.glEnable(GL11.GL_LIGHTING);
        GlStateManager.enableBlend();
        //GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        //GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
        //GL11.glPopMatrix();

        GlStateManager.enableCull();
        //GL11.glEnable(GL11.GL_CULL_FACE);
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
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        //GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-angle, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(-var13, -var13, var13);
        
        int borderSize = 2;
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldrenderer = tessellator.getBuffer();
        
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

        GlStateManager.popMatrix();
        //GL11.glPopMatrix();
    }
}
