package weather2.util;

import java.util.*;

import CoroUtil.util.CoroUtilCompatibility;
import net.minecraft.block.*;
import net.minecraft.block.LogBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import weather2.CommonProxy;
import weather2.config.ConfigTornado;

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
        try
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
    	                if (/*block.getHardness() <= 10000.6*/ (strVsBlock <= strMax && strVsBlock >= strMin) ||
                                (block.getMaterial(block.getDefaultState()) == Material.WOOD) ||
                                block.getMaterial(block.getDefaultState()) == Material.WOOL ||
                                block.getMaterial(block.getDefaultState()) == Material.PLANTS ||
                                block.getMaterial(block.getDefaultState()) == Material.TALL_PLANTS ||
                                block instanceof TallGrassBlock)
    	                {
    	                    /*if (block.material == Material.water) {
    	                    	return false;
    	                    }*/
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
                	if (block == Blocks.DIRT || block == Blocks.ORGANIC || block == Blocks.SAND || block instanceof LogBlock/* || block.material == Material.wood*/) {
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
        }
    }
    
    public static boolean safetyCheck(Block id)
    {
        if (id != Blocks.BEDROCK && id != Blocks.LOG && id != Blocks.CHEST && id != Blocks.JUKEBOX/* && id != Block.waterMoving.blockID && id != Block.waterStill.blockID */)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static boolean shouldRemoveBlock(Block blockID)
    {
        /*if (tryFinite)
        {
            try
            {
                if (Class.forName("BlockNWater").isInstance(Block.blocksList[blockID]))
                {
                    return false;
                }

                if (Class.forName("BlockNOcean").isInstance(Block.blocksList[blockID]))
                {
                    return false;
                }

                if (Class.forName("BlockNWater_Pressure").isInstance(Block.blocksList[blockID])) {
                    return false;
                }

                if (Class.forName("BlockNWater_Still").isInstance(Block.blocksList[blockID]))
                {
                    return true;
                }
            }
            catch (Exception exception)
            {
                tryFinite = false;
                return false;
            }
        }*/

        //water no
        if (blockID.getMaterial(blockID.getDefaultState()) == Material.WATER)
        {
            return false;
        }

        return true;
    }
    
    public static boolean isOceanBlock(Block blockID)
    {
        /*if (tryFinite)
        {
            try
            {
                if (Class.forName("BlockNOcean").isInstance(Block.blocksList[blockID]))
                {
                    return true;
                }
            }
            catch (Exception exception)
            {
                tryFinite = false;
                return false;
            }
        }*/

        return false;
    }
    
    public static boolean isSolidBlock(Block id)
    {
        return (id == Blocks.STONE ||
                id == Blocks.COBBLESTONE ||
                id == Blocks.SANDSTONE);	
    }
	
    public static void doBlockList()
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
    }

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
        return world.allPlayersSleeping && world.getPlayers().stream().noneMatch((p_217449_0_) -> {
            return !p_217449_0_.isSpectator() && !p_217449_0_.isPlayerFullyAsleep();
        });
    }
    
    
}
