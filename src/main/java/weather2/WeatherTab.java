package weather2;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.WoodType;

public class WeatherTab extends CreativeModeTab {
	private ItemStack tabIcon;

	public WeatherTab(Builder builder, ItemStack tabIcon) {
		super(builder);
		this.tabIcon = tabIcon;
	}

	/*WeatherTab() {
		super(Weather.MODID);
	}

	@Override
	public ItemStack makeIcon() {
		if (tabIcon == null) {
			tabIcon = new ItemStack(WeatherItems.WEATHER_ITEM.get());
		}
		return tabIcon;
	}*/
}
