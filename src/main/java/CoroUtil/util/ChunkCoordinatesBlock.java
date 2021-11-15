package CoroUtil.util;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class ChunkCoordinatesBlock extends BlockPos {

	public Block block = null;
	
	public ChunkCoordinatesBlock(int par1, int par2, int par3, Block parBlockID)
	{
        super(par1, par2, par3);
        block = parBlockID;
    }
	
	public ChunkCoordinatesBlock(BlockPos par1BlockCoord, Block parBlockID)
	{
        super(par1BlockCoord);
        block = parBlockID;
    }
}
