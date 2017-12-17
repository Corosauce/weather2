package weather2;

import extendedrenderer.shader.IShaderListener;
import extendedrenderer.shader.ShaderListenerRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
import weather2.client.foliage.FoliageEnhancerShader;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityLightningBolt;
import weather2.entity.EntityLightningBoltCustom;
import weather2.entity.EntityMovingBlock;
import weather2.util.WeatherUtilSound;
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
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

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {

    }
    
    private static void addMapping(Class<? extends Entity> entityClass, Render render) {
		RenderingRegistry.registerEntityRenderingHandler(entityClass, render);
	}
    
    @Override
    public void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String name, boolean creativeTab) {
    	super.addBlock(event, parBlock, name, creativeTab);

        //addItemModel(Item.getItemFromBlock(parBlock), 0, new ModelResourceLocation(Weather.modID + ":" + name, "inventory"));
    }

    @Override
    public void addItemBlock(RegistryEvent.Register<Item> event, Item item) {
        super.addItemBlock(event, item);

        addItemModel(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @Override
    public void addItem(RegistryEvent.Register<Item> event, Item item, String name) {
        super.addItem(event, item, name);

        addItemModel(item, 0, new ModelResourceLocation(Weather.modID + ":" + name, "inventory"));
    }

    public void addItemModel(Item item, int meta, ModelResourceLocation location) {

        //1.11: doesnt work currently for our method of loading, try it again in 1.12
        ModelLoader.setCustomModelResourceLocation(item, meta, location);

        //using this for now
        //Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, location);
    }
    
    /*@Override
	public void registerItemVariantModel(Item item, String name, int metadata) {
		if (item != null) {
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, metadata, new ModelResourceLocation(Weather.modID + ":" + name, "inventory"));
		}
	}

	@Override
	public void registerItemVariantModel(Item item, String registryName, int metadata, String variantName) {
		if (item != null) {
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, metadata, new ModelResourceLocation(Weather.modID + ":" + variantName, null));
		}
	}*/

    @Override
    public void postInit() {
        super.postInit();
    }

    @Override
    public void preInit() {
        super.preInit();
        ShaderListenerRegistry.addListener(new IShaderListener() {
            @Override
            public void init() {
                FoliageEnhancerShader.shadersInit();
            }

            @Override
            public void reset() {
                FoliageEnhancerShader.shadersReset();
            }
        });

        //IReload
    }
}
