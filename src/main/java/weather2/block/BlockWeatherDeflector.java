package weather2.block;

import net.minecraft.block.*;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockWeatherDeflector extends ContainerBlock
{
    public BlockWeatherDeflector()
    {
        super(Material.CLAY);
        setHardness(0.6F);
        setResistance(10.0F);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileEntityWeatherDeflector();
    }
    
    @Override
    public boolean isOpaqueCube(BlockState state)
    {
        return false;
    }
    
    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {

        if (!worldIn.isRemote && hand == Hand.MAIN_HAND) {
            TileEntity tEnt = worldIn.getTileEntity(pos);

            if (tEnt instanceof TileEntityWeatherDeflector) {
                ((TileEntityWeatherDeflector) tEnt).rightClicked(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
            }
        }

        return true;
    }
}

