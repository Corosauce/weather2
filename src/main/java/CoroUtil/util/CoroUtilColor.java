package CoroUtil.util;

import CoroUtil.repack.de.androidpit.colorthief.ColorThief;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.awt.image.BufferedImage;

public class CoroUtilColor {
    
    @SuppressWarnings("null")
    public static int[] getColors(BlockState state) {
        IBakedModel model;

        //used when foliage shader is on
//        if (FoliageData.backupBakedModelStore.containsKey(state)) {
//            model = FoliageData.backupBakedModelStore.get(state);
//        } else {
            model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state);
//        }

        if (model != null && !model.isBuiltInRenderer()) {
            TextureAtlasSprite sprite = model.getParticleTexture(EmptyModelData.INSTANCE);
            if (sprite != null && !sprite.getName().equals(MissingTextureSprite.getLocation())) {
                return getColors(sprite);
            }
        }
        return IntArrays.EMPTY_ARRAY;
    }

    public static int[] getColors(TextureAtlasSprite sprite) {
        int width = sprite.getWidth();
        int height = sprite.getHeight();
        int frames = sprite.getFrameCount();
        
        BufferedImage img = new BufferedImage(width, height * frames, BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < frames; i++) {
        	for (int x = 0; x < width; x++) {
        		for (int y = 0; y < height; y++) {
                    int abgr = sprite.getPixelRGBA(i, x, y);
                    int red = abgr & 0xFF;
                    int green = (abgr >> 8) & 0xFF;
                    int blue = (abgr >> 16) & 0xFF;
                    int alpha = (abgr >> 24) & 0xFF;
                    img.setRGB(x, y + (i * height), (alpha << 24) | (red << 16) | (green << 8) | blue);
        		}
        	}
        }
        
        int[][] colorData = ColorThief.getPalette(img, 6, 5, true);
        if (colorData != null) {
            int[] ret = new int[colorData.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = getColor(colorData[i]);
            }
            return ret;
        }
        return IntArrays.EMPTY_ARRAY;
    }
    
    private static int getColor(int[] colorData) {
        float mr = 1F;//((multiplier >>> 16) & 0xFF) / 255f;
        float mg = 1F;//((multiplier >>> 8) & 0xFF) / 255f;
        float mb = 1F;//(multiplier & 0xFF) / 255f;

        return 0xFF000000 | (((int) (colorData[0] * mr)) << 16) | (((int) (colorData[1] * mg)) << 8) | (int) (colorData[2] * mb);
    }

}
