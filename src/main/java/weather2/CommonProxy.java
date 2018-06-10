package weather2;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import weather2.block.*;
import weather2.config.ConfigMisc;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityLightningBolt;
import weather2.entity.EntityLightningBoltCustom;
import weather2.entity.EntityMovingBlock;
import weather2.item.ItemPocketSand;
import weather2.item.ItemSandLayer;
import weather2.item.ItemWeatherRecipe;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;

@Mod.EventBusSubscriber
public class CommonProxy implements IGuiHandler
{

	public static final String tornado_sensor = "tornado_sensor";
	public static final String tornado_siren = "tornado_siren";
	public static final String tornado_siren_manual = "tornado_siren_manual";
	public static final String wind_vane = "wind_vane";
	public static final String weather_forecast = "weather_forecast";
	public static final String weather_machine = "weather_machine";
	public static final String weather_deflector = "weather_deflector";
	public static final String anemometer = "anemometer";
	public static final String sand_layer = "sand_layer";

	public static final String sand_layer_placeable = "sand_layer_placeable";
	public static final String weather_item = "weather_item";
	public static final String pocket_sand = "pocket_sand";

	@GameRegistry.ObjectHolder(Weather.modID + ":" + tornado_sensor)
	public static Block blockTSensor;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + tornado_siren_manual)
	public static Block blockTSirenManual;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + tornado_siren)
	public static Block blockTSiren;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + wind_vane)
	public static Block blockWindVane;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + anemometer)
	public static Block blockAnemometer;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + weather_forecast)
	public static Block blockWeatherForecast;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + weather_machine)
	public static Block blockWeatherMachine;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + weather_deflector)
	public static Block blockWeatherDeflector;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + sand_layer)
	public static Block blockSandLayer;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + sand_layer_placeable)
	public static Item itemSandLayer;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + weather_item)
	public static Item itemWeatherRecipe;

	@GameRegistry.ObjectHolder(Weather.modID + ":" + pocket_sand)
	public static Item itemPocketSand;
	
	public static CreativeTabWeather tab;
	
    public CommonProxy()
    {
    	
    }

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		Weather.proxy.addBlock(event, blockTSensor = (new BlockTSensor()), tornado_sensor);
		Weather.proxy.addBlock(event, blockTSiren = (new BlockTSiren()), TileEntityTSiren.class, tornado_siren);
		Weather.proxy.addBlock(event, blockTSirenManual = (new BlockTSirenManual()), TileEntityTSirenManual.class, tornado_siren_manual);
		Weather.proxy.addBlock(event, blockWindVane = (new BlockWindVane()), TileEntityWindVane.class, wind_vane);
		Weather.proxy.addBlock(event, blockWeatherForecast = (new BlockWeatherForecast()), TileEntityWeatherForecast.class, weather_forecast);
		Weather.proxy.addBlock(event, blockWeatherMachine = (new BlockWeatherMachine()), TileEntityWeatherMachine.class, weather_machine);
		Weather.proxy.addBlock(event, blockWeatherDeflector = (new BlockWeatherDeflector()), TileEntityWeatherDeflector.class, weather_deflector);
		Weather.proxy.addBlock(event, blockAnemometer = (new BlockAnemometer()), TileEntityAnemometer.class, anemometer);
		Weather.proxy.addBlock(event, blockSandLayer = (new BlockSandLayer()), sand_layer, false);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		Weather.proxy.addItem(event, itemSandLayer = new ItemSandLayer(blockSandLayer), sand_layer_placeable);
		Weather.proxy.addItem(event, itemWeatherRecipe = new ItemWeatherRecipe(), weather_item);
		Weather.proxy.addItem(event, itemPocketSand = new ItemPocketSand(), pocket_sand);

		Weather.proxy.addItemBlock(event, new ItemBlock(blockTSensor).setRegistryName(blockTSensor.getRegistryName()));
		Weather.proxy.addItemBlock(event, new ItemBlock(blockTSiren).setRegistryName(blockTSiren.getRegistryName()));
		Weather.proxy.addItemBlock(event, new ItemBlock(blockTSirenManual).setRegistryName(blockTSirenManual.getRegistryName()));
		Weather.proxy.addItemBlock(event, new ItemBlock(blockWindVane).setRegistryName(blockWindVane.getRegistryName()));
		Weather.proxy.addItemBlock(event, new ItemBlock(blockWeatherForecast).setRegistryName(blockWeatherForecast.getRegistryName()));
		Weather.proxy.addItemBlock(event, new ItemBlock(blockWeatherMachine).setRegistryName(blockWeatherMachine.getRegistryName()));
		Weather.proxy.addItemBlock(event, new ItemBlock(blockWeatherDeflector).setRegistryName(blockWeatherDeflector.getRegistryName()));
		Weather.proxy.addItemBlock(event, new ItemBlock(blockAnemometer).setRegistryName(blockAnemometer.getRegistryName()));
	}

    public void init()
    {

    	//Weather.dbg("block list processing disabled");
    	WeatherUtil.doBlockList();
    	WeatherUtilConfig.processLists();

    	SoundRegistry.init();

    	addMapping(EntityIceBall.class, "weather_hail", 0, 128, 5, true);
    	addMapping(EntityMovingBlock.class, "moving_block", 1, 128, 5, true);
    	addMapping(EntityLightningBolt.class, "weather2_lightning_bolt", 2, 512, 5, true);
    	addMapping(EntityLightningBoltCustom.class, "weather2_lightning_bolt_custom", 2, 512, 5, true);

		/*registerBlocks(null);
		registerItems(null);*/
    }

    public void preInit() {
		tab = new CreativeTabWeather("Weather2");
	}

	public void postInit() {
		ResourceLocation group = new ResourceLocation(Weather.modID, "weather2_misc");

		if (!ConfigMisc.Item_WeatherItemNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, weather_item), group,
				new ItemStack(itemWeatherRecipe, 1), new Object[] {"X X", "DID", "X X", 'D', Items.REDSTONE, 'I', Items.GOLD_INGOT, 'X', Items.IRON_INGOT});

		if (!ConfigMisc.Block_SensorNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, tornado_sensor), group,
				new ItemStack(blockTSensor, 1), new Object[] {"X X", "DID", "X X", 'D', Items.REDSTONE, 'I', itemWeatherRecipe, 'X', Items.IRON_INGOT});
		if (!ConfigMisc.Block_SirenNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, tornado_siren), group,
				new ItemStack(blockTSiren, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', blockTSensor, 'X', Items.IRON_INGOT});

		if (!ConfigMisc.Block_SirenManualNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, tornado_siren_manual), group,
				new ItemStack(blockTSirenManual, 1), new Object[] {"XLX", "DID", "XLX", 'D', Items.REDSTONE, 'I', blockTSensor, 'X', Items.IRON_INGOT, 'L', Blocks.LEVER});

		if (!ConfigMisc.Block_WindVaneNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, wind_vane), group,
				new ItemStack(blockWindVane, 1), new Object[] {"X X", "DXD", "X X", 'D', Items.REDSTONE, 'X', itemWeatherRecipe});
		if (!ConfigMisc.Block_AnemometerNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, anemometer), group,
				new ItemStack(blockAnemometer, 1), new Object[] {"X X", "XDX", "X X", 'D', Items.REDSTONE, 'X', itemWeatherRecipe});

		if (!ConfigMisc.Block_WeatherForecastNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, weather_forecast), group,
				new ItemStack(blockWeatherForecast, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', Items.COMPASS, 'X', itemWeatherRecipe});
		if (!ConfigMisc.Block_WeatherMachineNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, weather_machine), group,
				new ItemStack(blockWeatherMachine, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', Items.DIAMOND, 'X', itemWeatherRecipe});
		if (!ConfigMisc.Block_WeatherDeflectorNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, weather_deflector), group,
				new ItemStack(blockWeatherDeflector, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', itemWeatherRecipe, 'X', Items.IRON_INGOT});

		if (!ConfigMisc.Block_SandLayerNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, sand_layer), group,
				new ItemStack(itemSandLayer, 64), new Object[] {"DDD", "DID", "DDD", 'D', Blocks.SAND, 'I', itemWeatherRecipe});
		if (!ConfigMisc.Block_SandNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, "sand"), group,
				new ItemStack(Blocks.SAND, 1), new Object[] {"DDD", "D D", "DDD", 'D', itemSandLayer});

		if (!ConfigMisc.Item_PocketSandNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, pocket_sand), group,
				new ItemStack(itemPocketSand, 8), new Object[] {"DDD", "DID", "DDD", 'D', itemSandLayer, 'I', itemWeatherRecipe});
	}
    
    /*public void addItem(Item is, String unlocalizedName) {
		addItem(is, unlocalizedName, "");
	}
	
	public void addItem(Item is, String unlocalizedName, String itemNameBase) {
		
		Item item = is;//.getItem();
		
		//vanilla calls
		item.setUnlocalizedName(Weather.modID + ":" + unlocalizedName);
		//item.setTextureName(Weather.modID + ":" + unlocalizedName);
		item.setCreativeTab(tab);
		//LanguageRegistry.addName(item, itemNameBase); //really not usefull, since its dynamic from nbt
		
		
	}*/
    
	public void addBlock(RegistryEvent.Register<Block> event, Block block, Class tEnt, String unlocalizedName) {
		addBlock(event, block, tEnt, unlocalizedName, true);
	}
	
    public void addBlock(RegistryEvent.Register<Block> event, Block block, Class tEnt, String unlocalizedName, boolean creativeTab) {
		addBlock(event, block, unlocalizedName, creativeTab);
		GameRegistry.registerTileEntity(tEnt, unlocalizedName);
	}
	
    public void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String unlocalizedName) {
    	addBlock(event, parBlock, unlocalizedName, true);
    }
    
	public void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String unlocalizedName, boolean creativeTab) {
		//vanilla calls
		//GameRegistry.registerBlock(parBlock, unlocalizedName);
		
		parBlock.setUnlocalizedName(Weather.modID + "." + unlocalizedName);
		parBlock.setRegistryName(/*Weather.modID + ":" + */unlocalizedName);
		
		if (creativeTab) {
			parBlock.setCreativeTab(tab);
		} else {
			parBlock.setCreativeTab(null);
		}
		if (event != null) {
			event.getRegistry().register(parBlock);
		} else {
			//GameRegistry.register(parBlock);
		}

		//ForgeRegistries.BLOCKS.register(parBlock);
		//LanguageRegistry.addName(parBlock, blockNameBase);
	}

	public void addItemBlock(RegistryEvent.Register<Item> event, Item item) {
		event.getRegistry().register(item);
	}
	
	public void addItem(RegistryEvent.Register<Item> event, Item item, String name) {
		item.setUnlocalizedName(Weather.modID + "." + name);
		//item.setRegistryName(new ResourceLocation(Weather.modID, name));
		item.setRegistryName(name);

		//GameRegistry.register(item);
		item.setCreativeTab(tab);

		if (event != null) {
			event.getRegistry().register(item);
		} else {
			//GameRegistry.register(item);
		}

		//registerItemVariantModel(item, name, 0);

		//return item;
	}
    
    public void addMapping(Class par0Class, String par1Str, int entityId, int distSync, int tickRateSync, boolean syncMotion) {
    	EntityRegistry.registerModEntity(new ResourceLocation(Weather.modID, par1Str), par0Class, par1Str, entityId, Weather.instance, distSync, tickRateSync, syncMotion);
        //EntityList.addMapping(par0Class, par1Str, entityId);
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z)
    {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z)
    {
        return null;
    }

	/*public void registerItemVariantModel(Item item, String name, int metadata) {}
	public void registerItemVariantModel(Item item, String registryName, int metadata, String variantName) {}

	public ResourceLocation getResource(String name) {
		return new ResourceLocation(Weather.modID, name);
	}*/

}
