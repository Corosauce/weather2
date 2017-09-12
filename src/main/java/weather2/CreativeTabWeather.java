package weather2;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabWeather extends CreativeTabs {
	
	public CreativeTabWeather(String label) {
		super(label);
	}
	
	@SideOnly(Side.CLIENT)
	/**
	 * the itemID for the item to be displayed on the tab
	 */
	@Override
	public ItemStack getIconItemStack()
	{
		return new ItemStack(CommonProxy.blockTSensor);
	}

	@Override
	public ItemStack getTabIconItem() {
		return getIconItemStack();
	}
	
	

}
