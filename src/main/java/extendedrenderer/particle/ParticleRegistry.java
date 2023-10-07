package extendedrenderer.particle;

import extendedrenderer.ExtendedRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import weather2.Weather;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Weather.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRegistry {

	public static TextureAtlasSprite squareGrey;
	public static TextureAtlasSprite smoke;
	//public static TextureAtlasSprite smokeTest;
	public static TextureAtlasSprite cloud;
	public static TextureAtlasSprite cloud256;
	public static TextureAtlasSprite cloud256_fire;
	public static TextureAtlasSprite cloud256_test;
	//public static TextureAtlasSprite cloud256_2;
	public static TextureAtlasSprite cloud256_6;
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
}
