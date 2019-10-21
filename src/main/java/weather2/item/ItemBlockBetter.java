package weather2.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockBetter extends Item
{
    public final Block block;

    public ItemBlockBetter(Block block)
    {
        this.block = block;
    }

    /**
     * Sets the unlocalized name of this item to the string passed as the parameter, prefixed by "item."
     */
    @Override
    public ItemBlockBetter setUnlocalizedName(String translationKey)
    {
        super.setUnlocalizedName(translationKey);
        return this;
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    @Override
    public ActionResultType onItemUse(PlayerEntity playerIn, World worldIn, BlockPos pos, Hand hand, Direction facing, float hitX, float hitY, float hitZ)
    {
        BlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getOwner();

        ItemStack stack = playerIn.getHeldItem(hand);

        if (!block.isReplaceable(worldIn, pos))
        {
            pos = pos.offset(facing);
        }

        if (stack.getCount() != 0 && playerIn.canPlayerEdit(pos, facing, stack) && worldIn.mayPlace(this.block, pos, false, facing, (Entity)null))
        {
            int i = this.getMetadata(stack.getMetadata());
            BlockState iblockstate1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, playerIn);

            if (placeBlockAt(stack, playerIn, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1))
            {
                SoundType soundtype = worldIn.getBlockState(pos).getOwner().getSoundType(worldIn.getBlockState(pos), worldIn, pos, playerIn);
                worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
            }

            return ActionResultType.SUCCESS;
        }
        else
        {
            return ActionResultType.FAIL;
        }
    }

    public static boolean setTileEntityNBT(World worldIn, @Nullable PlayerEntity player, BlockPos pos, ItemStack stackIn)
    {
        MinecraftServer minecraftserver = worldIn.getServer();

        if (minecraftserver == null)
        {
            return false;
        }
        else
        {
            if (stackIn.hasTag() && stackIn.getTag().contains("BlockEntityTag", 10))
            {
                TileEntity tileentity = worldIn.getTileEntity(pos);

                if (tileentity != null)
                {
                    if (!worldIn.isRemote && tileentity.onlyOpsCanSetNbt() && (player == null || !player.canUseCommandBlock()))
                    {
                        return false;
                    }

                    CompoundNBT nbttagcompound = tileentity.write(new CompoundNBT());
                    CompoundNBT nbttagcompound1 = nbttagcompound.copy();
                    CompoundNBT nbttagcompound2 = (CompoundNBT)stackIn.getTag().get("BlockEntityTag");
                    nbttagcompound.merge(nbttagcompound2);
                    nbttagcompound.putInt("x", pos.getX());
                    nbttagcompound.putInt("y", pos.getY());
                    nbttagcompound.putInt("z", pos.getZ());

                    if (!nbttagcompound.equals(nbttagcompound1))
                    {
                        tileentity.read(nbttagcompound);
                        tileentity.markDirty();
                        return true;
                    }
                }
            }

            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, Direction side, PlayerEntity player, ItemStack stack)
    {
        Block block = worldIn.getBlockState(pos).getOwner();

        if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos))
        {
            side = Direction.UP;
        }
        else if (!block.isReplaceable(worldIn, pos))
        {
            pos = pos.offset(side);
        }

        return worldIn.mayPlace(this.block, pos, false, side, (Entity)null);
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    /*public String getTranslationKey(ItemStack stack)
    {
        return this.block.getTranslationKey();
    }*/

    /**
     * Returns the unlocalized name of this item.
     */
    /*public String getTranslationKey()
    {
        return this.block.getTranslationKey();
    }*/

    /**
     * gets the CreativeTab this item is displayed on
     */
    /*@SideOnly(Side.CLIENT)
    public CreativeTabs getGroup()
    {
        return this.block.getCreativeTabToDisplayOn();
    }*/

    /**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    /*@SideOnly(Side.CLIENT)
    @Override
    public void fillItemGroup(CreativeTabs tab, NonNullList<ItemStack> subItems)
    {
        this.block.fillItemGroup(tab, subItems);
    }*/

    public Block getOwner()
    {
        return this.block;
    }

    /**
     * Called to actually place the block, after the location is determined
     * and all permission checks have been made.
     *
     * @param stack The item stack that was used to place the block. This can be changed inside the method.
     * @param player The player who is placing the block. Can be null if the block is not being placed by a player.
     * @param side The side the player (or machine) right-clicked on.
     */
    public boolean placeBlockAt(ItemStack stack, PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, BlockState newState)
    {
        if (!world.setBlockState(pos, newState, 3)) return false;

        BlockState state = world.getBlockState(pos);
        if (state.getOwner() == this.block)
        {
            setTileEntityNBT(world, player, pos, stack);
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
        }

        return true;
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
        block.addInformation(stack, worldIn, tooltip, flagIn);
    }
}