package weather2;

import CoroUtil.render.RenderNull;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import weather2.block.TileEntityTSiren;
import weather2.block.TileEntityWeatherForecast;
import weather2.block.TileEntityWindVane;
import weather2.client.block.TileEntityTSirenRenderer;
import weather2.client.block.TileEntityWeatherForecastRenderer;
import weather2.client.block.TileEntityWindVaneRenderer;
import weather2.client.entity.RenderFlyingBlock;
import weather2.client.entity.RenderLightningBolt;
import weather2.client.entity.particle.EntityFallingRainFX;
import weather2.client.entity.particle.EntityFallingSnowFX;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityLightningBolt;
import weather2.entity.EntityMovingBlock;
import weather2.util.WeatherUtilSound;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

	
    public ClientProxy()
    {
        
    }

    @Override
    public void init()
    {
    	super.init();
    	
    	WeatherUtilSound.init();
    	
    	MinecraftForge.EVENT_BUS.register(new SoundLoader());
    	
    	TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
        
        addMapping(EntityIceBall.class, new RenderFlyingBlock(Block.ice));
        addMapping(EntityMovingBlock.class, new RenderFlyingBlock(null));
        addMapping(EntityLightningBolt.class, new RenderLightningBolt());
        addMapping(EntityFallingRainFX.class, new RenderNull());
        addMapping(EntityFallingSnowFX.class, new RenderNull());
        
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTSiren.class, new TileEntityTSirenRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindVane.class, new TileEntityWindVaneRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWeatherForecast.class, new TileEntityWeatherForecastRenderer());
    }
    
    private static void addMapping(Class<? extends Entity> entityClass, Render render) {
		RenderingRegistry.registerEntityRenderingHandler(entityClass, render);
	}
}
