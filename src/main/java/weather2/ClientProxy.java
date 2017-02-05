package weather2;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.block.TileEntityAnemometer;
import weather2.block.TileEntityTSiren;
import weather2.block.TileEntityWeatherDeflector;
import weather2.block.TileEntityWeatherForecast;
import weather2.block.TileEntityWeatherMachine;
import weather2.block.TileEntityWindVane;
import weather2.client.block.TileEntityAnemometerRenderer;
import weather2.client.block.TileEntityTSirenRenderer;
import weather2.client.block.TileEntityWeatherDeflectorRenderer;
import weather2.client.block.TileEntityWeatherForecastRenderer;
import weather2.client.block.TileEntityWeatherMachineRenderer;
import weather2.client.block.TileEntityWindVaneRenderer;
import weather2.client.entity.RenderFlyingBlock;
import weather2.client.entity.RenderLightningBolt;
import weather2.client.entity.RenderLightningBoltCustom;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityLightningBolt;
import weather2.entity.EntityLightningBoltCustom;
import weather2.entity.EntityMovingBlock;
import weather2.util.WeatherUtilSound;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

	public static TextureAtlasSprite radarIconRain;
	public static TextureAtlasSprite radarIconLightning;
	public static TextureAtlasSprite radarIconWind;
	public static TextureAtlasSprite radarIconHail;
	public static TextureAtlasSprite radarIconTornado;
	public static TextureAtlasSprite radarIconCyclone;
    public static TextureAtlasSprite radarIconSandstorm;
	
	public static ClientTickHandler clientTickHandler;
	
    public ClientProxy()
    {
        clientTickHandler = new ClientTickHandler();
    }

    @Override
    public void init()
    {
    	super.init();
    	
    	WeatherUtilSound.init();
    	
    	//MinecraftForge.EVENT_BUS.register(new SoundLoader());
    	
    	//TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
        
        addMapping(EntityIceBall.class, new RenderFlyingBlock(Minecraft.getMinecraft().getRenderManager(), Blocks.ICE));
        addMapping(EntityMovingBlock.class, new RenderFlyingBlock(Minecraft.getMinecraft().getRenderManager(), null));
        addMapping(EntityLightningBolt.class, new RenderLightningBolt(Minecraft.getMinecraft().getRenderManager()));
        addMapping(EntityLightningBoltCustom.class, new RenderLightningBoltCustom(Minecraft.getMinecraft().getRenderManager()));
        /*addMapping(EntityFallingRainFX.class, new RenderNull(Minecraft.getMinecraft().getRenderManager()));
        addMapping(EntityFallingSnowFX.class, new RenderNull(Minecraft.getMinecraft().getRenderManager()));*/
        
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTSiren.class, new TileEntityTSirenRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindVane.class, new TileEntityWindVaneRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWeatherForecast.class, new TileEntityWeatherForecastRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWeatherMachine.class, new TileEntityWeatherMachineRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWeatherDeflector.class, new TileEntityWeatherDeflectorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAnemometer.class, new TileEntityAnemometerRenderer());
    }
    
    private static void addMapping(Class<? extends Entity> entityClass, Render render) {
		RenderingRegistry.registerEntityRenderingHandler(entityClass, render);
	}
    
    @Override
    public void addBlock(Block parBlock, String unlocalizedName, boolean creativeTab) {
    	super.addBlock(parBlock, unlocalizedName, creativeTab);
    	
    	registerItem(Item.getItemFromBlock(parBlock), 0, new ModelResourceLocation(Weather.modID + ":" + unlocalizedName, "inventory"));
    }
    
    public void registerItem(Item item, int meta, ModelResourceLocation location) {
    	Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, location);
    }
    
    @Override
	public void registerItemVariantModel(Item item, String name, int metadata) {
		if (item != null) {
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, metadata, new ModelResourceLocation(Weather.modID + ":" + name, "inventory"));
		}
	}

	@Override
	public void registerItemVariantModel(Item item, String registryName, int metadata, String variantName) {
		if (item != null) {
            //old preinit only way
			// ModelLoader.setCustomModelResourceLocation(item, metadata, mrl);
            //this method works via init, ModelLoader does not, requires being in preinit
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, metadata, new ModelResourceLocation(Weather.modID + ":" + variantName, null));
		}
	}
}
