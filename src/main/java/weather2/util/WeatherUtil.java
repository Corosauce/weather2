package weather2.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class WeatherUtil {

	public static HashMap<Block, Boolean> blockIDToUseMapping = new HashMap<Block, Boolean>();
	
    public static boolean isPaused() {
    	if (Minecraft.getInstance().isGamePaused()) return true;
    	return false;
    }
    
    public static boolean isPausedSideSafe(World world) {
    	//return false if server side because it cant be paused legit
    	if (!world.isRemote) return false;
    	return isPausedForClient();
    }
    
    public static boolean isPausedForClient() {
    	if (Minecraft.getInstance().isGamePaused()) return true;
    	return false;
    }
    
    //Terrain grabbing
    public static boolean shouldGrabBlock(World parWorld, BlockState state)
    {
        //TODO: 1.14 unbork tornado grabbing
        return false;
        //TODO: 1.14 uncomment
        /*try
        {
        	ItemStack itemStr = new ItemStack(Items.DIAMOND_AXE);

            Block block = state.getBlock();
            
        	boolean result = true;
            
            if (ConfigTornado.Storm_Tornado_GrabCond_List)
            {
            	try {

                    if (!ConfigTornado.Storm_Tornado_GrabListBlacklistMode)
                    {
                        if (!((Boolean)blockIDToUseMapping.get(block)).booleanValue()) {
                        	result = false;
                        }
                    }
                    else
                    {
                        if (((Boolean)blockIDToUseMapping.get(block)).booleanValue()) {
                        	result = false;
                        }
                    }
				} catch (Exception e) {
					//sometimes NPEs, just assume false if so
					result = false;
				}
            } else {

                if (ConfigTornado.Storm_Tornado_GrabCond_StrengthGrabbing)
                {
                    float strMin = 0.0F;
                    float strMax = 0.74F;

                    if (block == null)
                    {
                    	result = false;
                    	return result; //force return false to prevent unchecked future code outside scope
                    } else {

    	                float strVsBlock = block.getBlockHardness(block.getDefaultState(), parWorld, new BlockPos(0, 0, 0)) - (((itemStr.getStrVsBlock(block.getDefaultState()) - 1) / 4F));
    	
    	                //System.out.println(strVsBlock);
    	                if (*//*block.getHardness() <= 10000.6*//* (strVsBlock <= strMax && strVsBlock >= strMin) ||
                                (block.getMaterial(block.getDefaultState()) == Material.WOOD) ||
                                block.getMaterial(block.getDefaultState()) == Material.WOOL ||
                                block.getMaterial(block.getDefaultState()) == Material.PLANTS ||
                                block.getMaterial(block.getDefaultState()) == Material.TALL_PLANTS ||
                                block instanceof TallGrassBlock)
    	                {
    	                    *//*if (block.material == Material.water) {
    	                    	return false;
    	                    }*//*
    	                    if (!safetyCheck(block))
    	                    {
    	                    	result = false;
    	                    }
    	                } else {
    	                	result = false;
    	                }
    	
    	                
                    }
                }
                
                if (ConfigTornado.Storm_Tornado_RefinedGrabRules) {
                	if (block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.SAND || block instanceof LogBlock*//* || block.material == Material.wood*//*) {
                		result = false;
                	}
                	if (!CoroUtilCompatibility.canTornadoGrabBlockRefinedRules(state)) {
                	    result = false;
                    }
                }
            }
            
            if (block == CommonProxy.blockWeatherMachine) {
            	result = false;
            }
            
            return result;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }*/
    }

    //TODO: 1.14 uncomment
    /*public static boolean safetyCheck(Block id)
    {
        if (id != Blocks.BEDROCK && id != Blocks.LOG && id != Blocks.CHEST && id != Blocks.JUKEBOX*//* && id != Block.waterMoving.blockID && id != Block.waterStill.blockID *//*)
        {
            return true;
        }
        else
        {
            return false;
        }
    }*/
    
    public static boolean shouldRemoveBlock(Block blockID)
    {
        //water no
        if (blockID.getMaterial(blockID.getDefaultState()) == Material.WATER)
        {
            return false;
        }

        return true;
    }
    
    public static boolean isOceanBlock(Block blockID)
    {
        return false;
    }
    
    public static boolean isSolidBlock(Block id)
    {
        return (id == Blocks.STONE ||
                id == Blocks.COBBLESTONE ||
                id == Blocks.SANDSTONE);	
    }

    //TODO: 1.14 uncomment
    /*public static void doBlockList()
    {
    	
    	//System.out.println("1.8 TODO: verify block list lookup matching for exact comparions");
    	
        blockIDToUseMapping.clear();
        //System.out.println("Blacklist: ");
        String[] splEnts = ConfigTornado.Storm_Tornado_GrabList.split(",");
        //int[] blocks = new int[splEnts.length];

        if (splEnts.length > 0) {
	        for (int i = 0; i < splEnts.length; i++)
	        {
	        	splEnts[i] = splEnts[i].trim();
	            //blocks[i] = Integer.valueOf(splEnts[i]);
	            //System.out.println(splEnts[i]);
	        }
        }
        
        boolean dbgShow = false;
        String dbg = "block list: ";

        //HashMap hashmap = null;
        //System.out.println("?!?!" + Block.blocksList.length);
        blockIDToUseMapping.put(Blocks.AIR, false);

        Set set = Block.REGISTRY.keySet();
        Iterator it = set.iterator();
        while (it.hasNext()) {
        	Object obj = it.next();
        	//String tagName = (String) ((ResourceLocation)obj).toString();
        	ResourceLocation tagName = ((ResourceLocation)obj);
        	
        	
        	Block block = (Block) Block.REGISTRY.getOrDefault(tagName);
        	//if (dbgShow) System.out.println("??? " + Block.REGISTRY.getKey(block));
        	
        	if (block != null)
            {
                boolean foundEnt = false;

                for (int j = 0; j < splEnts.length; j++)
                {
                	if (ConfigTornado.Storm_Tornado_GrabCond_List_PartialMatches) {
                		if (tagName.toString().contains(splEnts[j])) {
                			dbg += Block.REGISTRY.getKey(block) + ", ";
                			foundEnt = true;
                			break;
                		}
                	} else {
	                    Block blockEntry = (Block)Block.REGISTRY.getOrDefault(new ResourceLocation(splEnts[j]));
	
	                    if (blockEntry != null && block == blockEntry)
	                    {
	                        foundEnt = true;
	                        dbg += Block.REGISTRY.getKey(block) + ", ";
	                        //blackList.append(s + " ");
	                        //System.out.println("adding to list: " + blocks[j]);
	                        break;
	                    }
                	}
                }

                blockIDToUseMapping.put(block, foundEnt);
                
                //entList.append(s + " ");
                //if (foundEnt) {
                	//blockIDToUseMapping.put(block, foundEnt);
                //} else {
                	//blockIDToUseMapping.put(block, false);
                //}
            }
            else
            {
                //blockIDToUseMapping.put(block, false);
            }
        	
        	
        }
        
        if (dbgShow) {
        	System.out.println(dbg);
        }
    }*/

    public static boolean isAprilFoolsDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        //test
        //return calendar.get(Calendar.MONTH) == Calendar.MARCH && calendar.get(Calendar.DAY_OF_MONTH) == 25;

        return calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public static ServerWorld getWorld(int dimID) {
        return DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), DimensionType.getById(dimID), true, true);
    }

    public static Iterable<ServerWorld> getWorlds() {
        return ServerLifecycleHooks.getCurrentServer().getWorlds();
    }

    public static boolean areAllPlayersAsleep(ServerWorld world) {
        //TODO: 1.14 fix
        /*return world.allPlayersSleeping && world.getPlayers().stream().noneMatch((p_217449_0_) -> {
            return !p_217449_0_.isSpectator() && !p_217449_0_.isPlayerFullyAsleep();
        });*/
        return false;
    }

    public static BlockRayTraceResult rayTraceBlocks(World world, RayTraceContextNoEntity context) {
        return func_217300_a(context, (p_217297_1_, p_217297_2_) -> {
            BlockState blockstate = world.getBlockState(p_217297_2_);
            IFluidState ifluidstate = world.getFluidState(p_217297_2_);
            Vec3d vec3d = p_217297_1_.func_222253_b();
            Vec3d vec3d1 = p_217297_1_.func_222250_a();
            VoxelShape voxelshape = p_217297_1_.getBlockShape(blockstate, world, p_217297_2_);
            BlockRayTraceResult blockraytraceresult = world.func_217296_a(vec3d, vec3d1, p_217297_2_, voxelshape, blockstate);
            VoxelShape voxelshape1 = p_217297_1_.getFluidShape(ifluidstate, world, p_217297_2_);
            BlockRayTraceResult blockraytraceresult1 = voxelshape1.rayTrace(vec3d, vec3d1, p_217297_2_);
            double d0 = blockraytraceresult == null ? Double.MAX_VALUE : p_217297_1_.func_222253_b().squareDistanceTo(blockraytraceresult.getHitVec());
            double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : p_217297_1_.func_222253_b().squareDistanceTo(blockraytraceresult1.getHitVec());
            return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
        }, (p_217302_0_) -> {
            Vec3d vec3d = p_217302_0_.func_222253_b().subtract(p_217302_0_.func_222250_a());
            return BlockRayTraceResult.createMiss(p_217302_0_.func_222250_a(), Direction.getFacingFromVector(vec3d.x, vec3d.y, vec3d.z), new BlockPos(p_217302_0_.func_222250_a()));
        });
    }

    public static <T> T func_217300_a(RayTraceContextNoEntity p_217300_0_, BiFunction<RayTraceContextNoEntity, BlockPos, T> p_217300_1_, Function<RayTraceContextNoEntity, T> p_217300_2_) {
        Vec3d vec3d = p_217300_0_.func_222253_b();
        Vec3d vec3d1 = p_217300_0_.func_222250_a();
        if (vec3d.equals(vec3d1)) {
            return p_217300_2_.apply(p_217300_0_);
        } else {
            double d0 = MathHelper.lerp(-1.0E-7D, vec3d1.x, vec3d.x);
            double d1 = MathHelper.lerp(-1.0E-7D, vec3d1.y, vec3d.y);
            double d2 = MathHelper.lerp(-1.0E-7D, vec3d1.z, vec3d.z);
            double d3 = MathHelper.lerp(-1.0E-7D, vec3d.x, vec3d1.x);
            double d4 = MathHelper.lerp(-1.0E-7D, vec3d.y, vec3d1.y);
            double d5 = MathHelper.lerp(-1.0E-7D, vec3d.z, vec3d1.z);
            int i = MathHelper.floor(d3);
            int j = MathHelper.floor(d4);
            int k = MathHelper.floor(d5);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(i, j, k);
            T t = p_217300_1_.apply(p_217300_0_, blockpos$mutableblockpos);
            if (t != null) {
                return t;
            } else {
                double d6 = d0 - d3;
                double d7 = d1 - d4;
                double d8 = d2 - d5;
                int l = MathHelper.signum(d6);
                int i1 = MathHelper.signum(d7);
                int j1 = MathHelper.signum(d8);
                double d9 = l == 0 ? Double.MAX_VALUE : (double)l / d6;
                double d10 = i1 == 0 ? Double.MAX_VALUE : (double)i1 / d7;
                double d11 = j1 == 0 ? Double.MAX_VALUE : (double)j1 / d8;
                double d12 = d9 * (l > 0 ? 1.0D - MathHelper.frac(d3) : MathHelper.frac(d3));
                double d13 = d10 * (i1 > 0 ? 1.0D - MathHelper.frac(d4) : MathHelper.frac(d4));
                double d14 = d11 * (j1 > 0 ? 1.0D - MathHelper.frac(d5) : MathHelper.frac(d5));

                while(d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
                    if (d12 < d13) {
                        if (d12 < d14) {
                            i += l;
                            d12 += d9;
                        } else {
                            k += j1;
                            d14 += d11;
                        }
                    } else if (d13 < d14) {
                        j += i1;
                        d13 += d10;
                    } else {
                        k += j1;
                        d14 += d11;
                    }

                    T t1 = p_217300_1_.apply(p_217300_0_, blockpos$mutableblockpos.setPos(i, j, k));
                    if (t1 != null) {
                        return t1;
                    }
                }

                return p_217300_2_.apply(p_217300_0_);
            }
        }
    }
    
    
}
