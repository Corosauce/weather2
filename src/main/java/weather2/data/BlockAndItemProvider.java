package weather2.data;

import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;
import weather2.Weather;

import java.util.Optional;

public class BlockAndItemProvider extends SpriteSourceProvider {

	public BlockAndItemProvider(PackOutput output, ExistingFileHelper fileHelper)
	{
		super(output, fileHelper, Weather.MODID);
	}

	@Override
	protected void addSources()
	{
		addSpriteBlock("tornado_siren");
		addSpriteBlock("tornado_siren_manual");
		addSpriteBlock("tornado_siren_manual_on");
		addSpriteBlock("tornado_sensor");
		addSpriteBlock("weather_deflector");
		addSpriteBlock("weather_forecast");
		addSpriteBlock("weather_machine");
		addSpriteBlock("anemometer");
		addSpriteBlock("wind_vane");
		addSpriteBlock("wind_turbine");
		addSpriteItem("weather_item");
		addSpriteItem("sand_layer");
		addSpriteItem("sand_layer_placeable");
	}

	public void addSpriteBlock(String textureName) {
		atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(Weather.MODID + ":blocks/" + textureName), Optional.empty()));
	}

	public void addSpriteItem(String textureName) {
		atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(Weather.MODID + ":items/" + textureName), Optional.empty()));
	}
}
