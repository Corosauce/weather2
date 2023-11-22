package extendedrenderer.particle;

import extendedrenderer.ExtendedRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import weather2.Weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParticleRegistry extends SpriteSourceProvider {

	public static TextureAtlasSprite squareGrey;
	public static TextureAtlasSprite smoke;
	//public static TextureAtlasSprite smokeTest;
	public static TextureAtlasSprite cloud;
	public static TextureAtlasSprite cloud256;
	public static TextureAtlasSprite cloud256_fire;
	public static TextureAtlasSprite cloud256_test;
	//public static TextureAtlasSprite cloud256_2;
	public static TextureAtlasSprite groundSplash;
	//public static TextureAtlasSprite downfall2;
	public static TextureAtlasSprite downfall3;
	//public static TextureAtlasSprite downfall4;
	//public static TextureAtlasSprite cloud256_7;
	public static TextureAtlasSprite chicken;
	public static TextureAtlasSprite potato;
	public static TextureAtlasSprite leaf;
	public static TextureAtlasSprite rain;
	public static TextureAtlasSprite rain_white;
	//public static TextureAtlasSprite rain_white_trans;
	//public static TextureAtlasSprite rain_white_2;
	//public static TextureAtlasSprite rain_10;
	//public static TextureAtlasSprite rain_vanilla;
	//public static TextureAtlasSprite snow_vanilla;
	public static TextureAtlasSprite snow;
	public static TextureAtlasSprite snow2;
	//public static TextureAtlasSprite test;
	//public static TextureAtlasSprite cloud256dark;
	//public static TextureAtlasSprite cloudDownfall;
	public static TextureAtlasSprite tumbleweed;
	public static TextureAtlasSprite debris_1;
	public static TextureAtlasSprite debris_2;
	public static TextureAtlasSprite debris_3;
	public static TextureAtlasSprite test_texture;
	public static TextureAtlasSprite white_square;
	public static List<TextureAtlasSprite> listFish = new ArrayList<>();
	//public static List<TextureAtlasSprite> listSeaweed = new ArrayList<>();
	public static TextureAtlasSprite grass;
	public static TextureAtlasSprite hail;
	public static TextureAtlasSprite cloudNew;
	public static TextureAtlasSprite cloud_square;
	public static TextureAtlasSprite square16;
	public static TextureAtlasSprite square64;

	public ParticleRegistry(PackOutput output, ExistingFileHelper fileHelper)
	{
		super(output, fileHelper, ExtendedRenderer.modid);
	}

	@Override
	protected void addSources()
	{
		//atlas(SpriteSourceProvider.PARTICLES_ATLAS).addSource(new SingleFile(new ResourceLocation(Weather.MODID + ":white"), Optional.empty()));

		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white"));
		//addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke_00"));
		//smokeTest = event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke_2"));
		//cloud = event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud64"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_fire"));
		//addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_test"));
		//cloud256_2 = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_5"));
		//ground splash
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_6"));
		//cloud256_7 = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_7"));
		//downfall2 = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall2"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall3"));
		//downfall4 = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall4"));
		if (!Weather.isLoveTropicsInstalled()) {
			addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/chicken"));
			addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/potato"));
		}
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/leaf"));
		//rain = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain"));
		//addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/test_texture"));
		//addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white_square"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white"));
		//rain_white_trans = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white_trans"));
		//rain_white_2 = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white_2"));
		//rain_10 = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_10"));
		//rain_vanilla = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/vanilla/rain"));
		//snow_vanilla = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/vanilla/snow"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow2"));
		//cloud256dark = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256dark"));
		//cloudDownfall = addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/tumbleweed"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_1"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_2"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_3"));
		/*for (int i = 1; i <= 9; i++) {
			listFish.add(addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/fish_" + i)));
		}
		for (int i = 1; i <= 7; i++) {
			listSeaweed.add(addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/seaweed_section_" + i)));
		}*/
		//used indirectly not via reference
		//addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/grass"));
		addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/hail"));
		//addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud"));
		//addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud_square"));

		//addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white16"));
		//addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white64"));

		//TODO: 1.14 uncomment
		/*MeshBufferManagerParticle.cleanup();
		MeshBufferManagerFoliage.cleanup();*/
	}

	public void addSprite(ResourceLocation res) {
		atlas(SpriteSourceProvider.PARTICLES_ATLAS).addSource(new SingleFile(res, Optional.empty()));
	}

	@SubscribeEvent
	public static void getRegisteredParticles(TextureStitchEvent.Post event) {

		if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_PARTICLES)) {
			return;
		}

		squareGrey = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white"));
		//smoke = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke_00"));
		//smokeTest = event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/smoke_2"));
		//cloud = event.addSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud64"));
		cloud256 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256"));
		cloud256_fire = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_fire"));
		//cloud256_test = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_test"));
		//cloud256_2 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_5"));
		//ground splash
		groundSplash = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_6"));
		//cloud256_7 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256_7"));
		//downfall2 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall2"));
		downfall3 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall3"));
		//downfall4 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall4"));
		if (!Weather.isLoveTropicsInstalled()) {
			chicken = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/chicken"));
			potato = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/potato"));
		}
		leaf = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/leaf"));
		//rain = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain"));
		//test_texture = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/test_texture"));
		//white_square = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white_square"));
		rain_white = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white"));
		//rain_white_trans = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white_trans"));
		//rain_white_2 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_white_2"));
		//rain_10 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/rain_10"));
		//rain_vanilla = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/vanilla/rain"));
		//snow_vanilla = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/vanilla/snow"));
		snow = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow"));
		snow2 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/snow2"));
		//cloud256dark = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud256dark"));
		//cloudDownfall = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/downfall"));
		tumbleweed = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/tumbleweed"));
		debris_1 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_1"));
		debris_2 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_2"));
		debris_3 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/debris_3"));
		/*for (int i = 1; i <= 9; i++) {
			listFish.add(event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/fish_" + i)));
		}
		for (int i = 1; i <= 7; i++) {
			listSeaweed.add(event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/seaweed_section_" + i)));
		}*/
		//used indirectly not via reference
		//grass = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/grass"));
		hail = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/hail"));
		//cloudNew = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud"));
		//cloud_square = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/cloud_square"));

		//square16 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white16"));
		//square64 = event.getAtlas().getSprite(new ResourceLocation(ExtendedRenderer.modid + ":particles/white64"));

		//TODO: 1.14 uncomment
		/*if (RotatingParticleManager.useShaders) {
			RotatingParticleManager.forceShaderReset = true;
		}*/

	}

	/*public static TextureAtlasSprite addSprite(TextureStitchEvent.Pre event, ResourceLocation resourceLocation) {
		event.addSprite(resourceLocation);
		return event.getAtlas().getSprite(resourceLocation);
	}*/
}
