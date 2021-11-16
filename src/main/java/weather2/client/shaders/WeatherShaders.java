package weather2.client.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import weather2.Weather;

import java.io.IOException;

public class WeatherShaders
{

    public static ShaderInstanceExtended getShaderExtended() {
        ShaderInstance shaderinstance = RenderSystem.getShader();
        if (shaderinstance instanceof ShaderInstanceExtended) {
            return (ShaderInstanceExtended) shaderinstance;
        }
        return null;
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModClientEvents
    {
        @SubscribeEvent
        public static void shaderRegistry(RegisterShadersEvent event) throws IOException
        {
            // Adds a shader to the list, the callback runs when loading is complete.
            event.registerShader(new ShaderInstanceExtended(event.getResourceManager(),
                            new ResourceLocation(Weather.MODID + ":rendertype_clouds"), DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL),
                    shaderInstance -> {
                CustomRenderTypes.cloudShader = shaderInstance;
            });
        }
    }

    // Accessor function, ensures that you don't use the raw methods below unintentionally.
    /*public static RenderType clouds(ResourceLocation texture)
    {
        return CustomRenderTypes.CLOUDS.apply(texture);
    }*/

    // Keep private because this stuff isn't meant to be public
    public static class CustomRenderTypes extends RenderType
    {
        // Holds the object loaded via RegisterShadersEvent
        public static ShaderInstance cloudShader;

        // Shader state for use in the render type, the supplier ensures it updates automatically with resource reloads
        //private static final ShaderStateShard RENDERTYPE_CLOUD = new ShaderStateShard(() -> cloudShader);

        // Dummy constructor needed to make java happy
        private CustomRenderTypes(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b2, Runnable r, Runnable r2)
        {
            super(s, v, m, i, b, b2, r, r2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }

        // The memoize caches the output value for each input, meaning the expensive registration process doesn't have to rerun
        //public static Function<ResourceLocation, RenderType> CLOUDS = Util.memoize(CustomRenderTypes::clouds);
        
        // Defines the RenderType. Make sure the name is unique by including your MODID in the name.
        /*private static RenderType clouds(ResourceLocation locationIn)
        {
            RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_CLOUD)
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .createCompositeState(true);
            return create("rendertype_clouds", DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,
                    VertexFormat.Mode.QUADS, 256, true, false, rendertype$state);
        }*/

        public static ShaderInstance getCloudShader() {
            return cloudShader;
        }
    }
}