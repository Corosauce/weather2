package weather2;

import net.minecraft.creativetab.CreativeTabs;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreativeTabWeather extends CreativeTabs {
	
	public CreativeTabWeather(String label) {
		super(label);
	}
	
	@SideOnly(Side.CLIENT)
	/**
	 * the itemID for the item to be displayed on the tab
	 */
	public int getTabIconItemIndex()
	{
		return CommonProxy.blockTSensor.blockID;
	}

}
