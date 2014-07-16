package weather2.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import weather2.ClientTickHandler;
import weather2.entity.EntityMovingBlock;
import weather2.weathersystem.wind.WindManager;
import CoroUtil.api.weather.IWindHandler;
import CoroUtil.api.weather.WindHandler;
import CoroUtil.entity.EntityTropicalFishHook;
import extendedrenderer.particle.entity.EntityRotFX;

public class WeatherUtilEntity {
	
	//old non multiplayer friendly var, needs resdesign where this is used
	public static int playerInAirTime = 0;

    
    public static float getWeight(Entity entity1) {
    	return getWeight(entity1, false);
    }
    
    public static float getWeight(Entity entity1, boolean forTornado)
    {
    	
    	if (entity1 instanceof IWindHandler) {
    		return ((IWindHandler) entity1).getWindWeight();
    	}
    	
    	if (entity1 instanceof WindHandler) {
    		return ((WindHandler) entity1).getWindWeight();
    	}
    	
    	//commented out for weather2 copy
        if (entity1 instanceof EntityMovingBlock)
        {
            return 1F + ((float)((EntityMovingBlock) entity1).age / 200);
        }

        if (entity1 instanceof EntityPlayer)
        {
            if (entity1.onGround || entity1.handleWaterMovement())
            {
                playerInAirTime = 0;
            }
            else
            {
                //System.out.println(playerInAirTime);
                playerInAirTime++;
            }

            
            if (((EntityPlayer) entity1).capabilities.isCreativeMode) return 99999999F;
            
            int extraWeight = 0;
            
            if (((EntityPlayer)entity1).inventory != null && (((EntityPlayer)entity1).inventory.armorInventory[2] != null) && ((EntityPlayer)entity1).inventory.armorInventory[2].getItem() == Items.iron_chestplate)
            {
            	extraWeight = 2;
            }

            if (((EntityPlayer)entity1).inventory != null && (((EntityPlayer)entity1).inventory.armorInventory[2] != null) && ((EntityPlayer)entity1).inventory.armorInventory[2].getItem() == Items.diamond_chestplate)
            {
            	extraWeight = 4;
            }

            if (forTornado) {
            	return 4.5F + extraWeight + ((float)(playerInAirTime / 400));
            } else {
            	return 5.0F + extraWeight + ((float)(playerInAirTime / 400));
            }
        }

        if (entity1.worldObj.isRemote && entity1 instanceof EntityRotFX)
        {
            float var = WeatherUtilParticle.getParticleWeight((EntityRotFX)entity1);

            if (var != -1)
            {
                return var;
            }
        }

        if (entity1 instanceof EntitySquid)
        {
            return 400F;
        }

        /*if (entity1 instanceof EntityPlayerProxy) {
        	return 50F;
        }*/

        if (entity1 instanceof EntityLivingBase)
        {
            //if (entity1.onGround || entity1.handleWaterMovement())
            //{
                //entity1.onGround = false;
                //c_CoroWeatherUtil.setEntityAge((EntityLivingBase)entity1, -150);
        	int airTime = entity1.getEntityData().getInteger("timeInAir");
        	if (entity1.onGround || entity1.handleWaterMovement())
            {
                airTime = 0;
            }
            else {
            	airTime++;
            }
        	
        	//test
        	//airTime = 0;
        	
        	entity1.getEntityData().setInteger("timeInAir", airTime);
            //}

            //System.out.println(((EntityLivingBase)entity1).entityAge+150);
            //int age = ((Integer)entToAge.get(entity1)).intValue();
            //System.out.println(age);
            if (forTornado) {
            	//System.out.println(1.0F + ((c_CoroWeatherUtil.getEntityAge((EntityLivingBase)entity1) + 150) / 50));
            	//Weather.dbg("airTime: " + airTime);
            	return 0.5F + (((float)airTime) / 800F);
            } else {
            	return 500.0F + (entity1.onGround ? 2.0F : 0.0F) + ((airTime) / 400);
            }
            
        }

        if (/*entity1 instanceof EntitySurfboard || */entity1 instanceof EntityBoat || entity1 instanceof EntityItem || entity1 instanceof EntityTropicalFishHook || entity1 instanceof EntityFishHook)
        {
            return 4000F;
        }

        if (entity1 instanceof EntityMinecart)
        {
            return 80F;
        }

        return 1F;
    }
	public static boolean canPushEntity(Entity ent)
    {
    	
    	//weather2: shouldnt be needed since its particles only now, ish
    	//if (!WeatherUtil.canUseWindOn(ent)) return false;
    	
    	WindManager windMan = ClientTickHandler.weatherManager.windMan;
    	
        double speed = 10.0D;
        int startX = (int)(ent.posX - speed * (double)(-MathHelper.sin(windMan.getWindAngleForPriority() / 180.0F * (float)Math.PI) * MathHelper.cos(0F/*weatherMan.wind.yDirection*/ / 180.0F * (float)Math.PI)));
        int startZ = (int)(ent.posZ - speed * (double)(MathHelper.cos(windMan.getWindAngleForPriority() / 180.0F * (float)Math.PI) * MathHelper.cos(0F/*weatherMan.wind.yDirection*/ / 180.0F * (float)Math.PI)));

        if (ent instanceof EntityPlayer)
        {
            boolean bool = true;
        }

        return ent.worldObj.rayTraceBlocks(Vec3.createVectorHelper(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ), Vec3.createVectorHelper(startX, ent.posY + (double)ent.getEyeHeight(), startZ)) == null;
        //return true;
    }
	
	public static boolean isEntityOutside(Entity parEnt) {
		return isEntityOutside(parEnt, false);
	}
	
	public static boolean isEntityOutside(Entity parEnt, boolean cheapCheck) {
		return isPosOutside(parEnt.worldObj, Vec3.createVectorHelper(parEnt.posX, parEnt.posY, parEnt.posZ), cheapCheck);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos) {
		return isPosOutside(parWorld, parPos, false);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos, boolean cheapCheck) {
		int rangeCheck = 5;
		int yOffset = 1;
		
		if (parWorld.getHeightValue(MathHelper.floor_double(parPos.xCoord), MathHelper.floor_double(parPos.zCoord)) < parPos.yCoord+1) return true;
		
		if (cheapCheck) return false;
		
		Vec3 vecTry = Vec3.createVectorHelper(parPos.xCoord + ForgeDirection.NORTH.offsetX*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + ForgeDirection.NORTH.offsetZ*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = Vec3.createVectorHelper(parPos.xCoord + ForgeDirection.SOUTH.offsetX*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + ForgeDirection.SOUTH.offsetZ*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = Vec3.createVectorHelper(parPos.xCoord + ForgeDirection.EAST.offsetX*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + ForgeDirection.EAST.offsetZ*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = Vec3.createVectorHelper(parPos.xCoord + ForgeDirection.WEST.offsetX*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + ForgeDirection.WEST.offsetZ*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		return false;
	}
	
	public static boolean checkVecOutside(World parWorld, Vec3 parPos, Vec3 parCheckPos) {
		boolean dirNorth = parWorld.rayTraceBlocks(parPos, parCheckPos) == null;
		if (dirNorth) {
			if (parWorld.getHeightValue(MathHelper.floor_double(parCheckPos.xCoord), MathHelper.floor_double(parCheckPos.zCoord)) < parCheckPos.yCoord) return true;
		}
		return false;
	}
}
