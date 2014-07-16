package weather2.weathersystem;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import weather2.client.SceneEnhancer;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtil;
import cpw.mods.fml.client.FMLClientHandler;

public class EntityRendererProxyWeather2Mini extends EntityRenderer
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

    public EntityRendererProxyWeather2Mini(Minecraft var1, IResourceManager resMan)
    {
        super(var1, resMan);
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
    	return WeatherUtil.isPaused();
    }

    @Override
    protected void renderRainSnow(float par1)
    {
    	
    	boolean overrideOn = ConfigMisc.Misc_proxyRenderOverrideEnabled/* && ConfigMisc.Misc_takeControlOfGlobalRain*/;
    	
    	if (!overrideOn) {
    		super.renderRainSnow(par1);
    		return;
    	} else {
    		
    		Minecraft mc = FMLClientHandler.instance().getClient();
    		EntityPlayer entP = mc.thePlayer;
    		if (entP != null) {
    			//convert to absolute (positive) value for old effects
    			float curRainStr = Math.abs(SceneEnhancer.getRainStrengthAndControlVisuals(entP, true));

    			//convert to abs for snow being rain
    			curRainStr = Math.abs(curRainStr);
    			
    			//Weather.dbg("curRainStr: " + curRainStr);
    			
    			//if (!ConfigMisc.overcastMode) {
    				mc.theWorld.setRainStrength(curRainStr);
    			//}
		    	
		    	//TEMP
		    	//mc.theWorld.setRainStrength(0.5F);
    		}
    		
    		//note, the overcast effect change will effect vanilla non particle rain distance too, particle rain for life!
    		if (!ConfigMisc.Particle_RainSnow) {
    			super.renderRainSnow(par1);
    		}
    		
    	}
    }
}
