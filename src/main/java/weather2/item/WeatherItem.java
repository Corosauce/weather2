package weather2.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WeatherItem extends Item {
    public WeatherItem(Item.Properties properties, CreativeModeTab itemGroup) {
        //TODO: 1.20
        super(properties/*.tab(itemGroup)*/);
    }

    /*public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);
    }*/
}
