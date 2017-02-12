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
    

    public EntityRendererProxyWeather2Mini(Minecraft var1, IResourceManager resMan)
    {
        super(var1, resMan);
    }
    
    @Override
    public void updateCameraAndRender(float var1)
    {
        super.updateCameraAndRender(var1);
        //ModLoader.OnTick(var1, this.game);
        
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer entP = mc.thePlayer;
		if (entP != null) {
			float curRainStr = SceneEnhancer.getRainStrengthAndControlVisuals(entP, true);
			curRainStr = Math.abs(curRainStr);
			mc.theWorld.setRainStrength(curRainStr);
		}
    }

    @Override
    protected void renderRainSnow(float par1)
    {
    	
    	boolean overrideOn = ConfigMisc.Misc_proxyRenderOverrideEnabled;
    	
    	if (!overrideOn) {
    		super.renderRainSnow(par1);
    		return;
    	} else {
    		
    		//note, the overcast effect change will effect vanilla non particle rain distance too, particle rain for life!
    		if (!ConfigMisc.Particle_RainSnow) {
    			super.renderRainSnow(par1);
    		}
    		
    	}
    }
}
