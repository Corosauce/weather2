package weather2;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import weather2.block.BlockAnemometer;
import weather2.block.BlockSandLayer;
import weather2.block.BlockTSensor;
import weather2.block.BlockTSiren;
import weather2.block.BlockWeatherDeflector;
import weather2.block.BlockWeatherForecast;
import weather2.block.BlockWeatherMachine;
import weather2.block.BlockWindVane;
import weather2.block.TileEntityAnemometer;
import weather2.block.TileEntityTSiren;
import weather2.block.TileEntityWeatherDeflector;
import weather2.block.TileEntityWeatherForecast;
import weather2.block.TileEntityWeatherMachine;
import weather2.block.TileEntityWindVane;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityLightningBolt;
import weather2.entity.EntityLightningBoltCustom;
import weather2.entity.EntityMovingBlock;
import weather2.item.ItemSandLayer;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilConfig;

public class CommonProxy implements IGuiHandler
{

	public static Block blockTSensor;
	public static Block blockTSiren;
	public static Block blockWindVane;
	public static Block blockAnemometer;
	public static Block blockWeatherForecast;
	public static Block blockWeatherMachine;
	public static Block blockWeatherDeflector;
	public static Block blockSandLayer;
	
	public static Item itemSandLayer;
	
	public static CreativeTabWeather tab;
	
    public CommonProxy()
    {
    	
    }

    public void init()
    {
    	tab = new CreativeTabWeather("Weather2");
    	
    	//Weather.dbg("block list processing disabled");
    	WeatherUtil.doBlockList();
    	WeatherUtilConfig.processLists();
    	
    	SoundRegistry.init();
    	
    	addMapping(EntityIceBall.class, "Weather Hail", 0, 128, 5, true);
    	addMapping(EntityMovingBlock.class, "Moving Block", 1, 128, 5, true);
    	addMapping(EntityLightningBolt.class, "Weather2 Lightning Bolt", 2, 512, 5, true);
    	addMapping(EntityLightningBoltCustom.class, "Weather2 Lightning Bolt Custom", 2, 512, 5, true);
    	
    	addBlock(blockTSensor = (new BlockTSensor()), "TornadoSensor", "Tornado Sensor");
    	addBlock(blockTSiren = (new BlockTSiren()), TileEntityTSiren.class, "TornadoSiren", "Tornado Siren");
    	addBlock(blockWindVane = (new BlockWindVane()), TileEntityWindVane.class, "WindVane", "Wind Vane");
    	addBlock(blockWeatherForecast = (new BlockWeatherForecast()), TileEntityWeatherForecast.class, "WeatherForecast", "Weather Forecast");
    	addBlock(blockWeatherMachine = (new BlockWeatherMachine()), TileEntityWeatherMachine.class, "WeatherMachine", "Weather Machine (right click to cycle)");
    	addBlock(blockWeatherDeflector = (new BlockWeatherDeflector()), TileEntityWeatherDeflector.class, "WeatherDeflector", "Weather Deflector");
    	addBlock(blockAnemometer = (new BlockAnemometer()), TileEntityAnemometer.class, "Anemometer", "Anemometer");
    	addBlock(blockSandLayer = (new BlockSandLayer()), "sand_layer", "Sand Layer");
    	
    	registerItem(new ItemSandLayer(blockSandLayer), "Sand Layer");
    	
    	GameRegistry.addRecipe(new ItemStack(blockTSensor, 1), new Object[] {"X X", "DID", "X X", 'D', Items.REDSTONE, 'I', Items.GOLD_INGOT, 'X', Items.IRON_INGOT});
    	GameRegistry.addRecipe(new ItemStack(blockTSiren, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', blockTSensor, 'X', Items.IRON_INGOT});
    	GameRegistry.addRecipe(new ItemStack(blockWindVane, 1), new Object[] {"X X", "DXD", "X X", 'D', Items.REDSTONE, 'X', Items.IRON_INGOT});
    	GameRegistry.addRecipe(new ItemStack(blockWeatherForecast, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', Items.COMPASS, 'X', Items.IRON_INGOT});
    	GameRegistry.addRecipe(new ItemStack(blockWeatherMachine, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', Items.DIAMOND, 'X', Items.IRON_INGOT});
    	GameRegistry.addRecipe(new ItemStack(blockWeatherDeflector, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', blockWeatherMachine, 'X', Items.IRON_INGOT});
    	
    	//LanguageRegistry.instance().addStringLocalization("itemGroup.Weather2", "Weather2 Items");
    }
    
    public void addItem(ItemStack is, String unlocalizedName) {
		addItem(is, unlocalizedName, "");
	}
	
	public void addItem(ItemStack is, String unlocalizedName, String itemNameBase) {
		
		Item item = is.getItem();
		
		//vanilla calls
		item.setUnlocalizedName(Weather.modID + ":" + unlocalizedName);
		//item.setTextureName(Weather.modID + ":" + unlocalizedName);
		item.setCreativeTab(CreativeTabs.MISC);
		//LanguageRegistry.addName(item, itemNameBase); //really not usefull, since its dynamic from nbt
		
		
	}
    
    public void addBlock(Block block, Class tEnt, String unlocalizedName, String blockNameBase) {
		addBlock(block, unlocalizedName, blockNameBase);
		GameRegistry.registerTileEntity(tEnt, unlocalizedName);
	}
	
	public void addBlock(Block parBlock, String unlocalizedName, String blockNameBase) {
		//vanilla calls
		GameRegistry.registerBlock(parBlock, unlocalizedName);
		/*parBlock.setBlockName(Weather.modID + ":" + unlocalizedName);
		parBlock.setBlockTextureName(Weather.modID + ":" + unlocalizedName);*/
		
		//new 1.8 stuff
		parBlock.setUnlocalizedName(getNamePrefixed(unlocalizedName));
		//parBlock.setRegistryName
		
		parBlock.setCreativeTab(tab);
		//LanguageRegistry.addName(parBlock, blockNameBase);
	}
	
	private Item registerItem(Item item, String name) {
		item.setUnlocalizedName(getNamePrefixed(name));
		item.setRegistryName(new ResourceLocation(Weather.modID, name));

		GameRegistry.register(item);
		item.setCreativeTab(tab);
		registerItemVariantModel(item, name, 0);

		return item;
	}
    
    public void addMapping(Class par0Class, String par1Str, int entityId, int distSync, int tickRateSync, boolean syncMotion) {
    	EntityRegistry.registerModEntity(par0Class, par1Str, entityId, Weather.instance, distSync, tickRateSync, syncMotion);
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
    
    public void registerItemVariantModel(Item item, String name, int metadata) {}
	public void registerItemVariantModel(Item item, String registryName, int metadata, String variantName) {}
    
    public ResourceLocation getResource(String name) {
    	return new ResourceLocation(Weather.modID, name);
    }
    
    public String getNamePrefixed(String name) {
    	return Weather.modID + "." + name;
    }
}
