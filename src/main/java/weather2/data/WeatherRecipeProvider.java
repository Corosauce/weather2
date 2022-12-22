package weather2.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import weather2.Weather;
import weather2.WeatherBlocks;
import weather2.WeatherItems;

import java.util.function.Consumer;

public class WeatherRecipeProvider extends RecipeProvider {

    public WeatherRecipeProvider(DataGenerator p_125973_) {
        super(p_125973_);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(WeatherItems.WEATHER_ITEM.get(), 1)
                .pattern("X X").pattern("DID").pattern("X X")
                .define('D', Items.REDSTONE)
                .define('I', Items.GOLD_INGOT)
                .define('X', Items.IRON_INGOT)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(WeatherBlocks.BLOCK_DEFLECTOR_ITEM.get(), 1)
                .pattern("XDX").pattern("DID").pattern("XDX")
                .define('D', Items.REDSTONE)
                .define('I', WeatherItems.WEATHER_ITEM.get())
                .define('X', Items.IRON_INGOT)
                .unlockedBy("has_weather_item", has(WeatherItems.WEATHER_ITEM.get()))
                .save(consumer);

        //TODO: change back to orig recipe once we add sensor block
        ShapedRecipeBuilder.shaped(WeatherBlocks.BLOCK_TORNADO_SIREN_ITEM.get(), 1)
                .pattern("X X").pattern("DID").pattern("X X")
                .define('D', Items.REDSTONE)
                .define('I', WeatherItems.WEATHER_ITEM.get())
                .define('X', Items.IRON_INGOT)
                .unlockedBy("has_weather_item", has(WeatherItems.WEATHER_ITEM.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(WeatherBlocks.BLOCK_SAND_LAYER_ITEM.get(), 8)
                .pattern("DDD").pattern("D D").pattern("DDD")
                .define('D', Items.SAND)
                .unlockedBy("has_sand", has(Items.SAND))
                .save(consumer);
    }
}
