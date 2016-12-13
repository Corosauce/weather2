package weather2;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
	public static Item itemWeatherRecipe;
	public static Item itemPocketSand;
	
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
    	
    	addBlock(blockTSensor = (new BlockTSensor()), "TornadoSensor");
    	addBlock(blockTSiren = (new BlockTSiren()), TileEntityTSiren.class, "TornadoSiren");
    	addBlock(blockWindVane = (new BlockWindVane()), TileEntityWindVane.class, "WindVane");
    	addBlock(blockWeatherForecast = (new BlockWeatherForecast()), TileEntityWeatherForecast.class, "WeatherForecast");
    	addBlock(blockWeatherMachine = (new BlockWeatherMachine()), TileEntityWeatherMachine.class, "WeatherMachine");
    	addBlock(blockWeatherDeflector = (new BlockWeatherDeflector()), TileEntityWeatherDeflector.class, "WeatherDeflector");
    	addBlock(blockAnemometer = (new BlockAnemometer()), TileEntityAnemometer.class, "Anemometer");
    	addBlock(blockSandLayer = (new BlockSandLayer()), "sand_layer", false);

		registerItem(itemSandLayer = new ItemSandLayer(blockSandLayer), "sand_layer_placeable");
		registerItem(itemWeatherRecipe = new ItemWeatherRecipe(), "weather_item");
		registerItem(itemPocketSand = new ItemPocketSand(), "pocket_sand");
    	
    	GameRegistry.addRecipe(new ItemStack(itemWeatherRecipe, 1), new Object[] {"X X", "DID", "X X", 'D', Items.REDSTONE, 'I', Items.GOLD_INGOT, 'X', Items.IRON_INGOT});
    	
    	GameRegistry.addRecipe(new ItemStack(blockTSensor, 1), new Object[] {"X X", "DID", "X X", 'D', Items.REDSTONE, 'I', itemWeatherRecipe, 'X', Items.IRON_INGOT});
    	GameRegistry.addRecipe(new ItemStack(blockTSiren, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', blockTSensor, 'X', Items.IRON_INGOT});
    	
    	GameRegistry.addRecipe(new ItemStack(blockWindVane, 1), new Object[] {"X X", "DXD", "X X", 'D', Items.REDSTONE, 'X', itemWeatherRecipe});
    	GameRegistry.addRecipe(new ItemStack(blockAnemometer, 1), new Object[] {"X X", "XDX", "X X", 'D', Items.REDSTONE, 'X', itemWeatherRecipe});
    	
    	GameRegistry.addRecipe(new ItemStack(blockWeatherForecast, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', Items.COMPASS, 'X', itemWeatherRecipe});
    	if (!ConfigMisc.Block_WeatherMachineNoRecipe)  {
    		GameRegistry.addRecipe(new ItemStack(blockWeatherMachine, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', Items.DIAMOND, 'X', itemWeatherRecipe});
    	}
    	GameRegistry.addRecipe(new ItemStack(blockWeatherDeflector, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', itemWeatherRecipe, 'X', Items.IRON_INGOT});

		GameRegistry.addRecipe(new ItemStack(itemSandLayer, 8), new Object[] {"DDD", "DID", "DDD", 'D', Blocks.SAND, 'I', itemWeatherRecipe});
		GameRegistry.addRecipe(new ItemStack(itemPocketSand, 8), new Object[] {"DDD", "DID", "DDD", 'D', itemSandLayer, 'I', itemWeatherRecipe});
    }

    public void preInit() {

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
    
	public void addBlock(Block block, Class tEnt, String unlocalizedName) {
		addBlock(block, tEnt, unlocalizedName, true);
	}
	
    public void addBlock(Block block, Class tEnt, String unlocalizedName, boolean creativeTab) {
		addBlock(block, unlocalizedName, creativeTab);
		GameRegistry.registerTileEntity(tEnt, unlocalizedName);
	}
	
    public void addBlock(Block parBlock, String unlocalizedName) {
    	addBlock(parBlock, unlocalizedName, true);
    }
    
	public void addBlock(Block parBlock, String unlocalizedName, boolean creativeTab) {
		//vanilla calls
		
		/*parBlock.setRegistryName(new ResourceLocation(Weather.modID, unlocalizedName));*/
		//GameRegistry.register(parBlock);
		GameRegistry.registerBlock(parBlock, unlocalizedName);
		/*parBlock.setBlockName(Weather.modID + ":" + unlocalizedName);
		parBlock.setBlockTextureName(Weather.modID + ":" + unlocalizedName);*/
		
		parBlock.setUnlocalizedName(getNamePrefixed(unlocalizedName));
		
		if (creativeTab) {
			parBlock.setCreativeTab(tab);
		} else {
			parBlock.setCreativeTab(null);
		}
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
