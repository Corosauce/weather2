package weather2;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import weather2.block.BlockTSensor;
import weather2.block.BlockTSiren;
import weather2.block.BlockWeatherForecast;
import weather2.block.BlockWeatherMachine;
import weather2.block.BlockWindVane;
import weather2.block.TileEntityTSiren;
import weather2.block.TileEntityWeatherForecast;
import weather2.block.TileEntityWeatherMachine;
import weather2.block.TileEntityWindVane;
import weather2.config.ConfigMisc;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityLightningBolt;
import weather2.entity.EntityMovingBlock;
import weather2.util.WeatherUtil;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy implements IGuiHandler
{

	public static Block blockTSensor;
	public static Block blockTSiren;
	public static Block blockWindVane;
	public static Block blockWeatherForecast;
	public static Block blockWeatherMachine;
	public static CreativeTabWeather tab;
	
    public CommonProxy()
    {
    	
    }

    public void init()
    {
    	tab = new CreativeTabWeather("Weather2");
    	
    	TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
    	
    	WeatherUtil.doBlockList();
    	
    	addMapping(EntityIceBall.class, "Weather Hail", 0, 128, 5, true);
    	addMapping(EntityMovingBlock.class, "Moving Block", 1, 128, 5, true);
    	addMapping(EntityLightningBolt.class, "Weather2 Lightning Bolt", 1, 512, 5, true);
    	
    	addBlock(blockTSensor = (new BlockTSensor(ConfigMisc.Block_sensorID)), "TornadoSensor", "Tornado Sensor");
    	addBlock(blockTSiren = (new BlockTSiren(ConfigMisc.Block_sirenID)), TileEntityTSiren.class, "TornadoSiren", "Tornado Siren");
    	addBlock(blockWindVane = (new BlockWindVane(ConfigMisc.Block_windVaneID)), TileEntityWindVane.class, "WindVane", "Wind Vane");
    	addBlock(blockWeatherForecast = (new BlockWeatherForecast(ConfigMisc.Block_weatherForecastID)), TileEntityWeatherForecast.class, "WeatherForecast", "Weather Forecast");
    	addBlock(blockWeatherMachine = (new BlockWeatherMachine(ConfigMisc.Block_weatherMachineID)), TileEntityWeatherMachine.class, "WeatherMachine", "Weather Machine");
        
    	GameRegistry.addRecipe(new ItemStack(blockTSensor, 1), new Object[] {"X X", "DID", "X X", 'D', Item.redstone, 'I', Item.ingotGold, 'X', Item.ingotIron});
    	GameRegistry.addRecipe(new ItemStack(blockTSiren, 1), new Object[] {"XDX", "DID", "XDX", 'D', Item.redstone, 'I', blockTSensor, 'X', Item.ingotIron});
    	GameRegistry.addRecipe(new ItemStack(blockWindVane, 1), new Object[] {"X X", "DXD", "X X", 'D', Item.redstone, 'X', Item.ingotIron});
    	GameRegistry.addRecipe(new ItemStack(blockWeatherForecast, 1), new Object[] {"XDX", "DID", "XDX", 'D', Item.redstone, 'I', Item.compass, 'X', Item.ingotIron});
    	GameRegistry.addRecipe(new ItemStack(blockWeatherMachine, 1), new Object[] {"XDX", "DID", "XDX", 'D', Item.redstone, 'I', Item.diamond, 'X', Item.ingotIron});
    	
    	LanguageRegistry.instance().addStringLocalization("itemGroup.Weather2", "Weather2 Items");
    }
    
    public static void addItem(ItemStack is, String unlocalizedName) {
		addItem(is, unlocalizedName, "");
	}
	
	public static void addItem(ItemStack is, String unlocalizedName, String itemNameBase) {
		
		Item item = is.getItem();
		
		//vanilla calls
		item.setUnlocalizedName(Weather.modID + ":" + unlocalizedName);
		item.setTextureName(Weather.modID + ":" + unlocalizedName);
		item.setCreativeTab(CreativeTabs.tabMisc);
		LanguageRegistry.addName(item, itemNameBase); //really not usefull, since its dynamic from nbt
		
		
	}
    
    public static void addBlock(Block block, Class tEnt, String unlocalizedName, String blockNameBase) {
		addBlock(block, unlocalizedName, blockNameBase);
		GameRegistry.registerTileEntity(tEnt, unlocalizedName);
	}
	
	public static void addBlock(Block parBlock, String unlocalizedName, String blockNameBase) {
		//vanilla calls
		GameRegistry.registerBlock(parBlock, unlocalizedName);
		parBlock.setUnlocalizedName(Weather.modID + ":" + unlocalizedName);
		parBlock.setTextureName(Weather.modID + ":" + unlocalizedName);
		parBlock.setCreativeTab(tab);
		LanguageRegistry.addName(parBlock, blockNameBase);
	}
    
    public static void addMapping(Class par0Class, String par1Str, int entityId, int distSync, int tickRateSync, boolean syncMotion) {
    	EntityRegistry.registerModEntity(par0Class, par1Str, entityId, Weather.instance, distSync, tickRateSync, syncMotion);
        EntityList.addMapping(par0Class, par1Str, entityId);
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
}
