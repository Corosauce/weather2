package weather2.client.foliage;

import com.sun.xml.internal.bind.v2.TODO;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class FoliageReplacerBase {

    /**
     * TODO: consider 2 separate class bases, shape + height
     * vanilla scenarios:
     * - 1 tall crosses
     * - 2 tall crosses
     * - 1 tall crops (4 meshes)
     * - ? tall vines (sided)
     * - ? tall reeds (crosses)
     */

    /**
     * hardcode variant support for now?
     */

    /**
     * material types:
     * - vine
     * - plants
     */

    //TODO: variants on the plant type
    public IBlockState state;
    //public TextureAtlasSprite sprite;
    public List<TextureAtlasSprite> sprites = new ArrayList<>();

    public FoliageReplacerBase(IBlockState state) {
        this.state = state;
    }

    public FoliageReplacerBase setSprites(List<TextureAtlasSprite> sprites) {
        this.sprites = sprites;
        return this;
    }

    public FoliageReplacerBase setSprite(TextureAtlasSprite sprite) {
        this.sprites.add(sprite);
        return this;
    }

    public abstract boolean validFoliageSpot(World world, BlockPos pos);

    public abstract void addForPos(World world, BlockPos pos);

    public void markMeshesDirty() {
        for (TextureAtlasSprite sprite : sprites) {
            FoliageEnhancerShader.markMeshDirty(sprite, true);
        }
    }

}
