package weather2.util;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import weather2.config.ConfigMisc;
import cpw.mods.fml.client.FMLClientHandler;

public class WeatherUtil {

	public static HashMap<Integer, Boolean> blockIDToUseMapping = new HashMap<Integer, Boolean>();
	
    public static boolean isPaused() {
    	if (FMLClientHandler.instance().getClient().getIntegratedServer() != null && FMLClientHandler.instance().getClient().getIntegratedServer().getServerListeningThread() != null && FMLClientHandler.instance().getClient().getIntegratedServer().getServerListeningThread().isGamePaused()) return true;
    	return false;
    }
    
    //Terrain grabbing
    public static boolean shouldGrabBlock(World parWorld, int id)
    {
        try
        {
        	ItemStack itemStr = new ItemStack(Item.axeDiamond);

            Block block = Block.blocksList[id];
            
        	boolean result = true;
            if (ConfigMisc.Storm_Tornado_GrabCond_StrengthGrabbing)
            {
                float strMin = 0.0F;
                float strMax = 0.74F;

                if (block == null)
                {
                	result = false;
                	return result; //force return false to prevent unchecked future code outside scope
                } else {

	                float strVsBlock = block.getBlockHardness(parWorld, 0, 0, 0) - (((itemStr.getStrVsBlock(block) - 1) / 4F));
	
	                //System.out.println(strVsBlock);
	                if (/*block.getHardness() <= 10000.6*/ (strVsBlock <= strMax && strVsBlock >= strMin) || (block.blockMaterial == Material.wood) || block.blockMaterial == Material.cloth || block.blockMaterial == Material.plants || block instanceof BlockTallGrass)
	                {
	                    /*if (block.blockMaterial == Material.water) {
	                    	return false;
	                    }*/
	                    if (!safetyCheck(block.blockID))
	                    {
	                    	result = false;
	                    }
	                } else {
	                	result = false;
	                }
	
	                
                }
            }
            
            if (ConfigMisc.Storm_Tornado_GrabCond_List)
            {
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
            }
            
            if (ConfigMisc.Storm_Tornado_RefinedGrabRules) {
            	if (id == Block.dirt.blockID || id == Block.grass.blockID || id == Block.sand.blockID || block instanceof BlockLog/* || block.blockMaterial == Material.wood*/) {
            		result = false;
            	}
            }
            
            return result;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }
    
    public static boolean safetyCheck(int id)
    {
        if (id != Block.bedrock.blockID && id != Block.wood.blockID && id != Block.chest.blockID && id != Block.jukebox.blockID/* && id != Block.waterMoving.blockID && id != Block.waterStill.blockID */)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public static boolean shouldRemoveBlock(int blockID)
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
        if (blockID == Block.waterMoving.blockID || blockID == Block.waterStill.blockID)
        {
            return false;
        }

        return true;
    }
    
    public static boolean isOceanBlock(int blockID)
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
    
    public static boolean isSolidBlock(int id)
    {
        return (id == Block.stone.blockID ||
                id == Block.cobblestone.blockID ||
                id == Block.sandStone.blockID);
    }
	
    public static void doBlockList()
    {
        blockIDToUseMapping.clear();
        //System.out.println("Blacklist: ");
        String[] splEnts = ConfigMisc.Storm_Tornado_GrabList.split(",");
        int[] blocks = new int[splEnts.length];

        if (splEnts.length > 1) {
	        for (int i = 0; i < splEnts.length; i++)
	        {
	            splEnts[i] = splEnts[i].trim();
	            blocks[i] = Integer.valueOf(splEnts[i]);
	            //System.out.println(splEnts[i]);
	        }
        }

        //HashMap hashmap = null;
        //System.out.println("?!?!" + Block.blocksList.length);
        blockIDToUseMapping.put(0, false);

        for (int i = 1; i < Block.blocksList.length; i++)
        {
            //System.out.println(i);
            //Object o = i$.next();
            //String s = (String)o;

            /*Class class1 = (Class)hashmap.get(o);
            try
            {
              class1.getDeclaredConstructor(new Class[] { EntityList.class }); } catch (Throwable throwable1) {
              	blockIDToUseMapping.put(class1, false);//continue;
            }*/

            //if ((!Modifier.isAbstract(class1.getModifiers())))
            //{
            //SettingBoolean settingboolean = new SettingBoolean("mobarrow_" + s, Boolean.valueOf(true));
            //mod_Arrows303.Settings.append(settingboolean);
            //widgetclassictwocolumn.add(new WidgetBoolean(settingboolean, s));
            //mobSettings.put(s, settingboolean);
            //if ((IMob.class.isAssignableFrom(class1))) {
            if (Block.blocksList[i] != null)
            {
                boolean foundEnt = false;

                for (int j = 0; j < blocks.length; j++)
                {
                    int uh = blocks[j];

                    if (uh == i)
                    {
                        foundEnt = true;
                        //blackList.append(s + " ");
                        //System.out.println("adding to list: " + blocks[j]);
                        break;
                    }
                }

                //entList.append(s + " ");
                blockIDToUseMapping.put(Block.blocksList[i].blockID, foundEnt);
            }
            else
            {
                blockIDToUseMapping.put(i, false);
            }

            /*} else {
              //non mobs
              blockIDToUseMapping.put(class1, false);
            }*/
            //System.out.println("hmmmm? " + s);
            //}
        }

        //System.out.println(entList.toString());
        //System.out.println(blackList.toString());
    }
    
    
}
