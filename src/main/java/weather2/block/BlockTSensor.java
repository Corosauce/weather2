package weather2.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import weather2.ServerTickHandler;
import weather2.config.ConfigMisc;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;

public class BlockTSensor extends Block
{
	
	public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
	
    public BlockTSensor()
    {
        super(Material.CLAY);
        this.setDefaultState(this.stateContainer.getBaseState().with(POWER, Integer.valueOf(0)));
        this.setTickRandomly(true);
        setHardness(0.6F);
        setResistance(10.0F);
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, Random rand)
    {
    	
    	if (world.isRemote) return;
    	
    	boolean enable = false;
    	
    	WeatherManagerServer wms = ServerTickHandler.lookupDimToWeatherMan.get(world.provider.getDimension());
    	if (wms != null) {
    		StormObject so = wms.getClosestStorm(new Vec3(pos.getX(), pos.getY(), pos.getZ()), ConfigMisc.sensorActivateDistance, StormObject.STATE_FORMING);
    		if (so != null/* && so.attrib_tornado_severity > 0*/) {
    			enable = true;
    		}
    	}

        if (enable)
        {
        	world.setBlockState(pos, state.with(POWER, 15), 3);
        }
        else
        {
        	world.setBlockState(pos, state.with(POWER, 0), 3);
        }
        
        world.scheduleBlockUpdate(pos, this, 100, 1);
    }
    
    @Override
    public BlockState getStateForPlacement(World worldIn, BlockPos pos,
                                           Direction facing, float hitX, float hitY, float hitZ, int meta,
                                           LivingEntity placer) {
    	worldIn.scheduleBlockUpdate(pos, this, 10, 1);
    	return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public int getWeakPower(BlockState stateContainer, IBlockAccess blockAccess, BlockPos pos, Direction side)
    {
        return ((Integer)stateContainer.get(POWER)).intValue();
    }
    
    @Override
    public boolean canProvidePower(BlockState state)
    {
        return true;
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public BlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().with(POWER, Integer.valueOf(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(BlockState state)
    {
        return ((Integer)state.get(POWER)).intValue();
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {POWER});
    }
}
