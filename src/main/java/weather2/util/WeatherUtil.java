package weather2.util;

import com.corosus.coroutil.util.CULog;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.server.ServerLifecycleHooks;
import weather2.config.ConfigTornado;

import java.util.*;

public class WeatherUtil {

    public static HashMap<ResourceLocation, Boolean> listGrabBlockCache = new HashMap<>();
    public static List<String> listGrabBlocks = new ArrayList<>();
    public static List<String> listGrabBlockTags = new ArrayList<>();

    public static String lastConfigChecked = "";

    public static void updateGrabBlockList(String grabListStr) {
        CULog.dbg("Updating weather2 tornado grab list");

        listGrabBlocks.clear();
        listGrabBlockTags.clear();
        listGrabBlockCache.clear();

        String[] splEnts = grabListStr.split(",");

        for (String str : splEnts) {
            str = str.trim();
            if (str.contains("#")) {
                listGrabBlockTags.add(addNamespaceIfMissing(str.substring(1)));
            } else {
                listGrabBlocks.add(addNamespaceIfMissing(str));
            }
        }
    }

    public static void testAllBlocks() {
        //Blocks.GLASS
        //if (!ConfigTornado.Storm_Tornado_GrabList.equals(lastConfigChecked)) {
            lastConfigChecked = ConfigTornado.Storm_Tornado_GrabList;
            CULog.log("PRINTING OUT ALL WEATHER2 TORNADO GRABBABLE BLOCKS WITH CURRENT CONFIG: ");
            Registry.BLOCK.forEach(block -> {
                List<BlockState> list = block.getStateDefinition().getPossibleStates();
                for (BlockState state : list) {
                    boolean result = canGrabViaLists(state);
                    if (result) {
                        CULog.log(state + " -> " + result);
                    }
                }
            });

            boolean wat = canGrabViaLists(Blocks.TORCH.defaultBlockState());
            System.out.println("wat: " + wat);
        //}
    }

    public static String addNamespaceIfMissing(String str) {
        if (!str.contains(":")) {
            str = "minecraft:" + str;
        }
        return str;
    }

    public static boolean isStateInListOfTags(BlockState state) {
        for (String str : listGrabBlockTags) {
            TagKey<Block> key = getTagKeyFor(str);
            if (key != null) {
                if (state.m_204336_(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static TagKey<Block> getTagKeyFor(String str) {
        return TagKey.m_203882_(Registry.BLOCK_REGISTRY, new ResourceLocation(str));
    }

    public static boolean canGrabViaLists(BlockState state) {
        boolean returnVal = !ConfigTornado.Storm_Tornado_GrabListBlacklistMode;
        ResourceLocation registeredName = state.getBlock().getRegistryName();
        if (listGrabBlockCache.containsKey(registeredName)) {
            return listGrabBlockCache.get(registeredName);
        }

        if (listGrabBlocks.contains(registeredName.toString())) {
            listGrabBlockCache.put(registeredName, returnVal);
            return returnVal;
        }

        if (isStateInListOfTags(state)) {
            listGrabBlockCache.put(registeredName, returnVal);
            return returnVal;
        }

        listGrabBlockCache.put(registeredName, !returnVal);
        return !returnVal;
    }
	
    public static boolean isPaused() {
    	if (Minecraft.getInstance().isPaused()) return true;
    	return false;
    }
    
    public static boolean isPausedSideSafe(Level world) {
    	//return false if server side because it cant be paused legit
    	if (!world.isClientSide) return false;
    	return isPausedForClient();
    }
    
    public static boolean isPausedForClient() {
    	if (Minecraft.getInstance().isPaused()) return true;
    	return false;
    }

    public static boolean isAprilFoolsDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        //test
        //return calendar.get(Calendar.MONTH) == Calendar.MARCH && calendar.get(Calendar.DAY_OF_MONTH) == 25;

        return calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    public static boolean shouldRemoveBlock(BlockState blockID)
    {
        //water no
        if (blockID.getMaterial() == Material.WATER)
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

    public static boolean shouldGrabBlock(Level parWorld, BlockState state)
    {
        try
        {
            ItemStack itemStr = new ItemStack(Items.DIAMOND_AXE);

            Block block = state.getBlock();

            boolean result = true;

            if (ConfigTornado.Storm_Tornado_GrabCond_List)
            {
                try {
                    result = canGrabViaLists(state);
                } catch (Exception e) {
                    //sometimes NPEs (pre 1.18), just assume false if so
                    e.printStackTrace();
                    result = false;
                }
            } else {

                if (ConfigTornado.Storm_Tornado_GrabCond_StrengthGrabbing)
                {
                    float strMin = 0.0F;
                    float strMax = 0.74F;

                    if (block == null) {
                        result = false;
                        return result; //force return false to prevent unchecked future code outside scope
                    } else {

                        //float strVsBlock = block.getBlockHardness(block.defaultBlockState(), parWorld, new BlockPos(0, 0, 0)) - (((itemStr.getStrVsBlock(block.defaultBlockState()) - 1) / 4F));
                        float strVsBlock = state.getDestroySpeed(parWorld, new BlockPos(0, 0, 0)) - (((itemStr.getDestroySpeed(block.defaultBlockState()) - 1) / 4F));

                        //System.out.println(strVsBlock);
                        if (/*block.getHardness() <= 10000.6*/ (strVsBlock <= strMax && strVsBlock >= strMin) ||
                                (state.getMaterial() == Material.WOOD) ||
                                state.getMaterial() == Material.WOOL ||
                                state.getMaterial() == Material.PLANT ||/*
                                state.getMaterial() == Material.VINE ||*/
                                block instanceof TallGrassBlock) {
                            if (!safetyCheck(state)) {
                                result = false;
                            }
                        } else {
                            result = false;
                        }

                    }
                }

                if (ConfigTornado.Storm_Tornado_RefinedGrabRules) {
                    if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.DIRT_PATH || block == Blocks.SAND || block == Blocks.RED_SAND || (block instanceof RotatedPillarBlock && state.getMaterial() == Material.WOOD)) {
                        result = false;
                    }
                    if (!canTornadoGrabBlockRefinedRules(state)) {
                        result = false;
                    }
                }
            }

            //TODO: 1.18
            /*if (block == CommonProxy.blockWeatherMachine) {
                result = false;
            }*/

            return result;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean safetyCheck(BlockState state)
    {
        Block id = state.getBlock();
        if (id != Blocks.BEDROCK && id != Blocks.ACACIA_LOG && id != Blocks.CHEST && id != Blocks.JUKEBOX/* && id != Block.waterMoving.blockID && id != Block.waterStill.blockID */)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public static ServerLevel getWorld(ResourceKey<Level> levelResourceKey) {
        return ServerLifecycleHooks.getCurrentServer().getLevel(levelResourceKey);
    }

    public static boolean canTornadoGrabBlockRefinedRules(BlockState state) {
        ResourceLocation registeredName = state.getBlock().getRegistryName();
        if (registeredName.getNamespace().equals("dynamictrees")) {
            if (registeredName.getPath().contains("rooty") || registeredName.getPath().contains("branch")) {
                return false;
            }
        }
        return true;
    }

    public static float dist(Vector3f vec1, Vector3f vec2) {
        double d0 = vec2.x() - vec1.x();
        double d1 = vec2.y() - vec1.y();
        double d2 = vec2.z() - vec1.z();
        return (float) Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static double dist(Vector3d vec1, Vector3d vec2) {
        double d0 = vec2.x - vec1.x;
        double d1 = vec2.y - vec1.y;
        double d2 = vec2.z - vec1.z;
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }
    
}
