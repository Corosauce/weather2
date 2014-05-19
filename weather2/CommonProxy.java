package weather2;

import weather2.entity.EntityIceBall;
import weather2.entity.EntityMovingBlock;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class CommonProxy implements IGuiHandler
{
	
    public CommonProxy()
    {
    	
    }

    public void init()
    {
    	TickRegistry.registerTickHandler(new ServerTickHandler(), Side.SERVER);
    	
    	WeatherUtil.doBlockList();
    	
    	addMapping(EntityIceBall.class, "Weather Hail", 0, 128, 5, true);
    	addMapping(EntityMovingBlock.class, "Moving Block", 1, 128, 5, true);
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
		parBlock.setCreativeTab(CreativeTabs.tabMisc);
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
