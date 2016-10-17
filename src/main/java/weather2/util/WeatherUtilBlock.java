package weather2.util;

import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import CoroUtil.util.Vec3;

public class WeatherUtilBlock {

	/**
	 * Fill direction up with a block, as if flowing particles filled the area up
	 * 
	 * - calculate endpoint and try to fill that up first, the propegate back
	 * - 
	 * 
	 */
	public static void floodAreaWithSand(World world, Vec3 posSource, float directionYaw, float fillDistance, float fillWideness) {
		//want to use this variable for how much the fill up spreads out to neighboring blocks
		float thickness = 1F;
		float tickStep = 0.75F;
		int fillPerTick = 2;
		//use snow for now, make sand block after
		
		//snow has 8 layers till its a full solid block (full solid on 8th layer)
		
		BlockPos posSourcei = posSource.toBlockPos();
		int ySource = world.getHeight(posSourcei).getY();
		int y = ySource;
		float startScan = fillDistance;
		
		//scan outwards to find closest wall
		for (float i = 0; i < fillDistance; i += tickStep) {
			double vecX = (-Math.sin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Math.cos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor_double(posSource.xCoord + vecX);
    		int z = MathHelper.floor_double(posSource.zCoord + vecZ);
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		IBlockState state = world.getBlockState(pos);
    		
    		if (state.getMaterial() != Material.AIR) {
    			startScan = i;
    			break;
    		}
		}
		
		//scan inwards from the non air block we found
		for (float i = startScan; i > 0; i -= tickStep) {
			double vecX = (-Math.sin(Math.toRadians(directionYaw)) * (i));
    		double vecZ = (Math.cos(Math.toRadians(directionYaw)) * (i));
    		
    		int x = MathHelper.floor_double(posSource.xCoord + vecX);
    		int z = MathHelper.floor_double(posSource.zCoord + vecZ);
    		
    		//world.getHeight(new BlockPos(x, 0, z)).getY();
    		
    		boolean foundSpotToFill = false;
    		
    		BlockPos pos = new BlockPos(x, y, z);
    		IBlockState state = world.getBlockState(pos);
    		
    		int snowMetaMax = 8;
    		//IBlockState state = world.getBlockState(new BlockPos(xxx + x, setBlockHeight, zzz + z));
    		/*int meta = state.getBlock().getMetaFromState(state);
    		if (meta < snowMetaMax) {
        		meta += 1;
    		}*/
    		
    		if (state.getMaterial() == Material.AIR) {
    			world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, fillPerTick));
    			foundSpotToFill = true;
    			break;
    		} else if (state.getBlock() == Blocks.SNOW_LAYER) {
    			foundSpotToFill = true;
    			//1 - 8
    			int height = ((Integer)state.getValue(BlockSnow.LAYERS)).intValue();
    			int extraFill = 0;
    			if (height <= snowMetaMax-1) {
    				height += fillPerTick;
    				if (height > snowMetaMax) {
    					extraFill = height - snowMetaMax;
    					height = snowMetaMax;
    				}
    				world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, height));
    				System.out.println("extra to fill: " + extraFill);
    				break;
    			} else {
    				//full from the start, treat like a wall
    				//extraFill = fillPerTick;
    			}
    			
    			//world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, fillPerTick));
    			
    		}
		}
	}
	
}
