package weather2.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import weather2.CommonProxy;
import weather2.config.ConfigMisc;

public class WeatherUtil {

	public static HashMap<Block, Boolean> blockIDToUseMapping = new HashMap<Block, Boolean>();
	
    public static boolean isPaused() {
    	if (FMLClientHandler.instance().getClient().isGamePaused()) return true;
    	return false;
    }
    
    //Terrain grabbing
    public static boolean shouldGrabBlock(World parWorld, Block id)
    {
        try
        {
        	ItemStack itemStr = new ItemStack(Items.diamond_axe);

            Block block = id;
            
        	boolean result = true;
            
            if (ConfigMisc.Storm_Tornado_GrabCond_List)
            {
            	try {

                    if (!ConfigMisc.Storm_Tornado_GrabListBlacklistMode)
                    {
                        if (!((Boolean)blockIDToUseMapping.get(id)).booleanValue()) {
                        	result = false;
                        }
                    }
                    else
                    {
                        if (((Boolean)blockIDToUseMapping.get(id)).booleanValue()) {
                        	result = false;
                        }
                    }
				} catch (Exception e) {
					//sometimes NPEs, just assume false if so
					result = false;
				}
            } else {

                if (ConfigMisc.Storm_Tornado_GrabCond_StrengthGrabbing)
                {
                    float strMin = 0.0F;
                    float strMax = 0.74F;

                    if (block == null)
                    {
                    	result = false;
                    	return result; //force return false to prevent unchecked future code outside scope
                    } else {

    	                float strVsBlock = block.getBlockHardness(parWorld, new BlockPos(0, 0, 0)) - (((itemStr.getStrVsBlock(block) - 1) / 4F));
    	
    	                //System.out.println(strVsBlock);
    	                if (/*block.getHardness() <= 10000.6*/ (strVsBlock <= strMax && strVsBlock >= strMin) || (block.getMaterial() == Material.wood) || block.getMaterial() == Material.cloth || block.getMaterial() == Material.plants || block instanceof BlockTallGrass)
    	                {
    	                    /*if (block.blockMaterial == Material.water) {
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
                
                if (ConfigMisc.Storm_Tornado_RefinedGrabRules) {
                	if (id == Blocks.dirt || id == Blocks.grass || id == Blocks.sand || block instanceof BlockLog/* || block.blockMaterial == Material.wood*/) {
                		result = false;
                	}
                }
            }
            
            if (id == CommonProxy.blockWeatherMachine) {
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
        if (id != Blocks.bedrock && id != Blocks.log && id != Blocks.chest && id != Blocks.jukebox/* && id != Block.waterMoving.blockID && id != Block.waterStill.blockID */)
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
        if (blockID.getMaterial() == Material.water)
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
        return (id == Blocks.stone ||
                id == Blocks.cobblestone ||
                id == Blocks.sandstone);
    }
	
    public static void doBlockList()
    {
        blockIDToUseMapping.clear();
        //System.out.println("Blacklist: ");
        String[] splEnts = ConfigMisc.Storm_Tornado_GrabList.split(",");
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
        blockIDToUseMapping.put(Blocks.air, false);

        Set set = Block.blockRegistry.getKeys();
        Iterator it = set.iterator();
        while (it.hasNext()) {
        	String tagName = (String) it.next();
        	
        	Block block = (Block) Block.blockRegistry.getObject(tagName);
        	if (dbgShow) System.out.println("??? " + Block.blockRegistry.getNameForObject(block));
        	
        	if (block != null)
            {
                boolean foundEnt = false;

                for (int j = 0; j < splEnts.length; j++)
                {
                	if (ConfigMisc.Storm_Tornado_GrabCond_List_PartialMatches) {
                		if (tagName.contains(splEnts[j])) {
                			dbg += Block.blockRegistry.getNameForObject(block) + ", ";
                			foundEnt = true;
                			break;
                		}
                	} else {
	                    Block blockEntry = (Block)Block.blockRegistry.getObject(splEnts[j]);
	
	                    if (blockEntry != null && block == blockEntry)
	                    {
	                        foundEnt = true;
	                        dbg += Block.blockRegistry.getNameForObject(block) + ", ";
	                        //blackList.append(s + " ");
	                        //System.out.println("adding to list: " + blocks[j]);
	                        break;
	                    }
                	}
                }

                blockIDToUseMapping.put(block, foundEnt);
                
                //entList.append(s + " ");
                /*if (foundEnt) {
                	blockIDToUseMapping.put(block, foundEnt);
                } else {
                	blockIDToUseMapping.put(block, false);
                }*/
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
    
    
}
