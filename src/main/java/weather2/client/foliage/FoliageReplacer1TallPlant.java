package weather2.client.foliage;

import extendedrenderer.ExtendedRenderer;
import extendedrenderer.foliage.FoliageReplacerBase;
import extendedrenderer.render.FoliageRenderer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FoliageReplacer1TallPlant extends FoliageReplacerBase {

    //TODO: variants on the plant type
    public IBlockState state;
    public TextureAtlasSprite sprite;

    public FoliageReplacer1TallPlant(IBlockState state, TextureAtlasSprite sprite) {
        this.state = state;
        this.sprite = sprite;
    }

    @Override
    public boolean validFoliageSpot(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.GRASS && world.getBlockState(pos.up()).getBlock() == state.getBlock();
    }

    @Override
    public void addForPos(World world, BlockPos pos) {
        ExtendedRenderer.foliageRenderer.addForPos(this, sprite, pos);
    }
}
