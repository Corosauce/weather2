package weather2;

import extendedrenderer.ExtendedRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BlockProvider extends SpriteSourceProvider {

	public BlockProvider(PackOutput output, ExistingFileHelper fileHelper)
	{
		super(output, fileHelper, Weather.MODID);
	}

	@Override
	protected void addSources()
	{
		addSprite("tornado_siren");
		addSprite("tornado_siren_manual");
		addSprite("tornado_siren_manual_on");
		addSprite("tornado_sensor");
		addSprite("weather_deflector");
		addSprite("weather_forecast");
		addSprite("weather_machine");
		addSprite("anemometer");
	}

	public void addSprite(String textureName) {
		atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation(Weather.MODID + ":blocks/" + textureName), Optional.empty()));
	}
}
