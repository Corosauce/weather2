package weather2.weathersystem;

import CoroUtil.config.ConfigCoroUtil;
import extendedrenderer.EventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.IResourceManager;
import weather2.config.ConfigMisc;
import weather2.config.ConfigParticle;

public class EntityRendererProxyWeather2Mini extends GameRenderer
{

    public EntityRendererProxyWeather2Mini(Minecraft var1, IResourceManager resMan)
    {
        super(var1, resMan);
    }

    @Override
    protected void renderRainSnow(float par1)
    {
    	
    	boolean overrideOn = ConfigMisc.Misc_proxyRenderOverrideEnabled;

		/**
		 * why render here? because renderRainSnow provides better context, solves issues:
		 * - translucent blocks rendered after
		 * -- shaders are color adjusted when rendering on other side of
		 * --- water
		 * --- stained glass, etc
		 */
		//TODO: 1.14 uncomment
		/*if (ConfigCoroUtil.useEntityRenderHookForShaders) {
			EventHandler.hookRenderShaders(par1);
		}*/
    	
    	if (!overrideOn) {
    		super.renderRainSnow(par1);
    		return;
    	} else {
    		
    		//note, the overcast effect change will effect vanilla non particle rain distance too, particle rain for life!
    		if (!ConfigParticle.Particle_RainSnow) {
    			super.renderRainSnow(par1);
    		}
    		
    	}
    }
}
