package weather2.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import weather2.Weather;
import weather2.WeatherBlocks;
import weather2.WeatherItems;

import java.util.function.Consumer;

public class WeatherRecipeProvider extends RecipeProvider {

    public WeatherRecipeProvider(PackOutput p_125973_) {
        super(p_125973_);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        //TODO: 1.20
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WeatherItems.WEATHER_ITEM.get(), 1)
                .pattern("X X").pattern("DID").pattern("X X")
                .define('D', Items.REDSTONE)
                .define('I', Items.GOLD_INGOT)
                .define('X', Items.IRON_INGOT)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WeatherItems.BLOCK_DEFLECTOR_ITEM.get(), 1)
                .pattern("XDX").pattern("DID").pattern("XDX")
                .define('D', Items.REDSTONE)
                .define('I', WeatherItems.WEATHER_ITEM.get())
                .define('X', Items.IRON_INGOT)
                .unlockedBy("has_weather_item", has(WeatherItems.WEATHER_ITEM.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WeatherItems.BLOCK_FORECAST_ITEM.get(), 1)
                .pattern("XDX").pattern("DID").pattern("XDX")
                .define('D', Items.REDSTONE)
                .define('I', Items.COMPASS)
                .define('X', WeatherItems.WEATHER_ITEM.get())
                .unlockedBy("has_weather_item", has(WeatherItems.WEATHER_ITEM.get()))
                .save(consumer);

        //if (!ConfigMisc.Block_WeatherForecastNoRecipe) GameRegistry.addShapedRecipe(new ResourceLocation(Weather.modID, weather_forecast), group,
        //				new ItemStack(blockWeatherForecast, 1), new Object[] {"XDX", "DID", "XDX", 'D', Items.REDSTONE, 'I', Items.COMPASS, 'X', itemWeatherRecipe});

        //TODO: change back to orig recipe once we add sensor block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WeatherItems.BLOCK_TORNADO_SIREN_ITEM.get(), 1)
                .pattern("X X").pattern("DID").pattern("X X")
                .define('D', Items.REDSTONE)
                .define('I', WeatherItems.WEATHER_ITEM.get())
                .define('X', Items.IRON_INGOT)
                .unlockedBy("has_weather_item", has(WeatherItems.WEATHER_ITEM.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WeatherItems.BLOCK_SAND_LAYER_ITEM.get(), 8)
                .pattern("DDD").pattern("D D").pattern("DDD")
                .define('D', Items.SAND)
                .unlockedBy("has_sand", has(Items.SAND))
                .save(consumer);
    }
}
