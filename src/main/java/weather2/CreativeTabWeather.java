package weather2;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

public class CreativeTabWeather extends ItemGroup {
	
	public CreativeTabWeather(String label) {
		super(label);
	}
	
	@OnlyIn(Dist.CLIENT)
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
