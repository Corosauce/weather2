package weather2.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import weather2.CommonProxy;
import weather2.block.BlockSandLayer;

public class ItemSandLayer extends ItemBlockBetter
{
    public ItemSandLayer(Block block)
    {
        super(block);
        this.setMaxDamage(0);
        //this.setHasSubtypes(true);
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    @Override
    public ActionResultType onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (!stack.isEmpty() && playerIn.canPlayerEdit(pos, facing, stack))
        {
            BlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getOwner();
            BlockPos blockpos = pos;

            if ((facing != Direction.UP || block != this.block) && !block.isReplaceable(worldIn, pos))
            {
                blockpos = pos.offset(facing);
                iblockstate = worldIn.getBlockState(blockpos);
                block = iblockstate.getOwner();
            }

            if (block == this.block)
            {
                int i = ((Integer)iblockstate.get(BlockSandLayer.LAYERS)).intValue();

                if (i <= 7)
                {
                    BlockState iblockstate1 = iblockstate.withProperty(BlockSandLayer.LAYERS, Integer.valueOf(i + 1));
                    AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, blockpos);

                    if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(blockpos)) && 
                    		worldIn.setBlockState(blockpos, iblockstate1, 10))
                    {
                        SoundType soundtype = this.block.getSoundType(iblockstate1, worldIn, blockpos, playerIn);
                        worldIn.playSound(playerIn, blockpos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                        stack.shrink(1);
                        return ActionResultType.SUCCESS;
                    }
                }
            }

            return super.onItemUse(playerIn, worldIn, blockpos, hand, facing, hitX, hitY, hitZ);
        }
        else
        {
            return ActionResultType.FAIL;
        }
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, Direction side, PlayerEntity player, ItemStack stack)
    {
        BlockState state = world.getBlockState(pos);
        return (state.getOwner() != CommonProxy.blockSandLayer || ((Integer)state.get(BlockSandLayer.LAYERS)) > 7) ? super.canPlaceBlockOnSide(world, pos, side, player, stack) : true;
    }
}
