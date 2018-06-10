package weather2.client.foliage;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
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

    public int expectedHeight = 1;

    public Material baseMaterial = Material.GRASS;
    public boolean biomeColorize = true;
    public boolean randomizeCoord = true;

    public boolean stateSensitive = false;
    public HashMap<IProperty, Comparable> lookupPropertiesToComparable = new HashMap<>();

    public int animationID;
    public float looseness = 1F;

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

    public FoliageReplacerBase setBaseMaterial(Material material) {
        this.baseMaterial = material;
        return this;
    }

    public FoliageReplacerBase setBiomeColorize(boolean val) {
        this.biomeColorize = val;
        return this;
    }

    public FoliageReplacerBase setRandomizeCoord(boolean val) {
        this.randomizeCoord = val;
        return this;
    }

    public FoliageReplacerBase setStateSensitive(boolean val) {
        this.stateSensitive = val;
        return this;
    }

    public FoliageReplacerBase addComparable(IProperty property, Comparable comparable) {
        lookupPropertiesToComparable.put(property, comparable);
        return this;
    }

    public abstract boolean validFoliageSpot(World world, BlockPos pos);

    public abstract void addForPos(World world, BlockPos pos);

    public void markMeshesDirty() {
        for (TextureAtlasSprite sprite : sprites) {
            FoliageEnhancerShader.markMeshDirty(sprite, true);
        }
    }

    public FoliageReplacerBase setLooseness(float val) {
        this.looseness = val;
        return this;
    }

    public boolean isActive() {
        return true;
    }

}
