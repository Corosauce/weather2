package weather2.weathersystem;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.EntityRendererProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import weather2.ClientTickHandler;
import weather2.config.ConfigMisc;
import weather2.weathersystem.storm.StormObject;
import cpw.mods.fml.client.FMLClientHandler;

public class EntityRendererProxyWeather2Mini extends EntityRendererProxy
{
    private Minecraft mc;
    private Random random = new Random();
    public int rendererUpdateCount;
    public long lastWorldTime = 0;

    private int rainSoundCounter = 0;

    public boolean basicRain = false;
    public int rainRate = 50;
    
    /** Rain X coords */
    public float[] rainXCoords;

    /** Rain Y coords */
    public float[] rainYCoords;
    
    private static final ResourceLocation resRain = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation resSnow = new ResourceLocation("textures/environment/snow.png");
    
    public float curRainStr = 0F;
    public float curRainStrTarget = 0F;

    public EntityRendererProxyWeather2Mini(Minecraft var1)
    {
        super(var1);
        this.mc = var1;
        rendererUpdateCount = 0;
    }

    @Override
    public void updateCameraAndRender(float var1)
    {
        super.updateCameraAndRender(var1);
        //ModLoader.OnTick(var1, this.game);
    }

    public void disableLightMap2(double var1)
    {
        GL13.glClientActiveTexture(GL13.GL_TEXTURE1);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
    }/*
    
    @Override
    public void setupFog(int par1, float par2)
    {
    	super.setupFog(par1, par2);
    	System.out.println("woooooo");
    }*/
    
    public boolean isPaused() {
    	if (FMLClientHandler.instance().getClient().getIntegratedServer() != null && FMLClientHandler.instance().getClient().getIntegratedServer().getServerListeningThread() != null && FMLClientHandler.instance().getClient().getIntegratedServer().getServerListeningThread().isGamePaused()) return true;
    	return false;
    }

    @Override
    protected void renderRainSnow(float par1)
    {
    	
    	boolean overrideOn = ConfigMisc.proxyRenderOverrideEnabled && ConfigMisc.takeControlOfGlobalRain;
    	
    	if (!overrideOn) {
    		super.renderRainSnow(par1);
    		return;
    	} else {
    		
    		Minecraft mc = FMLClientHandler.instance().getClient();
    		EntityPlayer entP = mc.thePlayer;
    		if (entP != null) {
		    	double maxStormDist = 512 / 4 * 3;
    			Vec3 plPos = Vec3.createVectorHelper(entP.posX, StormObject.staticYPos, entP.posZ);
    		    StormObject storm = ClientTickHandler.weatherManager.getClosestStorm(plPos, maxStormDist); 
    		    //System.out.println("storm found? " + storm != null);
    		    
    		    boolean closeEnough = false;
    		    double stormDist = 9999;
    		    
    		    //evaluate if storms size is big enough to over over player
    		    if (storm != null) {
    		    	stormDist = storm.pos.distanceTo(plPos);
    		    	//System.out.println("storm dist: " + stormDist);
    		    	if (storm.size > stormDist) {
    		    		closeEnough = true;
    		    	}
    		    }
    		    
    		    if (closeEnough) {
        		    
        		    double stormIntensity = (storm.size - stormDist) / storm.size;
        		    //System.out.println("intensity: " + stormIntensity);
    		    	mc.theWorld.getWorldInfo().setRaining(true);
    		    	mc.theWorld.getWorldInfo().setThundering(true);
    		    	curRainStrTarget = (float) stormIntensity;
    		    	//mc.theWorld.thunderingStrength = (float) stormIntensity;
    		    } else {
    		    	mc.theWorld.getWorldInfo().setRaining(false);
    		    	mc.theWorld.getWorldInfo().setThundering(false);
    		    	curRainStrTarget = 0;
    		    	//mc.theWorld.setRainStrength(0);
    		    	//mc.theWorld.thunderingStrength = 0;
    		    }
    		    
    		    if (curRainStr > curRainStrTarget) {
    		    	curRainStr -= 0.001F;
    		    } else if (curRainStr < curRainStrTarget) {
    		    	curRainStr += 0.001F;
    		    }

		    	mc.theWorld.setRainStrength(curRainStr);
    		}
    		
    		super.renderRainSnow(par1);
    		
    	}
    }
}
