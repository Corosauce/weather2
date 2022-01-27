package weather2.util;

import com.corosus.coroutil.util.CoroUtilCompatibility;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.server.ServerLifecycleHooks;
import weather2.config.ConfigTornado;

import java.util.Calendar;

public class WeatherUtil {
	
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
        //TODO: 1.14 unbork tornado grabbing
        //return false;
        //TODO: 1.14 uncomment

        //TODO: block tags for logs, also im gonna go particles instead

        try
        {
            ItemStack itemStr = new ItemStack(Items.DIAMOND_AXE);

            Block block = state.getBlock();

            boolean result = true;

            if (ConfigTornado.Storm_Tornado_GrabCond_List)
            {
                try {

                    //TODO: 1.18
                    /*if (!ConfigTornado.Storm_Tornado_GrabListBlacklistMode)
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
                    }*/
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

                        //float strVsBlock = block.getBlockHardness(block.defaultBlockState(), parWorld, new BlockPos(0, 0, 0)) - (((itemStr.getStrVsBlock(block.defaultBlockState()) - 1) / 4F));
                        float strVsBlock = state.getDestroySpeed(parWorld, new BlockPos(0, 0, 0)) - (((itemStr.getDestroySpeed(block.defaultBlockState()) - 1) / 4F));

                        //System.out.println(strVsBlock);
                        if (/*block.getHardness() <= 10000.6*/ (strVsBlock <= strMax && strVsBlock >= strMin) ||
                                (state.getMaterial() == Material.WOOD) ||
                                state.getMaterial() == Material.WOOL ||
                                state.getMaterial() == Material.PLANT ||/*
                                state.getMaterial() == Material.VINE ||*/
                                block instanceof TallGrassBlock)
                        {
    	                    /*if (block.blockMaterial == Material.water) {
    	                    	return false;
    	                    }*/
                            if (!safetyCheck(state))
                            {
                                result = false;
                            }
                        } else {
                            result = false;
                        }


                    }
                }

                if (ConfigTornado.Storm_Tornado_RefinedGrabRules) {
                    if (block == Blocks.DIRT || block == Blocks.GRASS || block == Blocks.SAND || (block instanceof RotatedPillarBlock && state.getMaterial() == Material.WOOD)) {
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
    
}
