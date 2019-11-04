package weather2.util;

import CoroUtil.api.weather.IWindHandler;
import CoroUtil.util.CoroUtilEntOrParticle;
import CoroUtil.util.Vec3;
import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import weather2.api.WeatherUtilData;

public class WeatherUtilEntity {
	
	//old non multiplayer friendly var, needs resdesign where this is used
	public static int playerInAirTime = 0;

    
    public static float getWeight(Object entity1) {
    	return getWeight(entity1, false);
    }
    
    public static float getWeight(Object entity1, boolean forTornado)
    {
    	World world = CoroUtilEntOrParticle.getWorld(entity1);

    	//fixes issue #270
        if (world == null) {
            return 1F;
        }

    	if (entity1 instanceof IWindHandler) {
    		return ((IWindHandler) entity1).getWindWeight();
    	}

        //TODO: 1.14 uncomment
        /*if (entity1 instanceof EntityMovingBlock)
        {
            return 1F + ((float)((EntityMovingBlock) entity1).age / 200);
        }*/

        if (entity1 instanceof PlayerEntity)
        {
        	PlayerEntity player = (PlayerEntity) entity1;
            if (player.onGround || player.handleWaterMovement())
            {
                playerInAirTime = 0;
            }
            else
            {
                //System.out.println(playerInAirTime);
                playerInAirTime++;
            }

            
            if (((PlayerEntity) entity1).abilities.isCreativeMode) return 99999999F;
            
            int extraWeight = 0;
            
            if (((PlayerEntity)entity1).inventory != null && !(((PlayerEntity)entity1).inventory.armorInventory.get(2).isEmpty())
                    && ((PlayerEntity)entity1).inventory.armorInventory.get(2).getItem() == Items.IRON_CHESTPLATE)
            {
            	extraWeight = 2;
            }

            if (((PlayerEntity)entity1).inventory != null && !(((PlayerEntity)entity1).inventory.armorInventory.get(2).isEmpty())
                    && ((PlayerEntity)entity1).inventory.armorInventory.get(2).getItem() == Items.DIAMOND_CHESTPLATE)
            {
            	extraWeight = 4;
            }

            if (forTornado) {
            	return 4.5F + extraWeight + ((float)(playerInAirTime / 400));
            } else {
            	return 5.0F + extraWeight + ((float)(playerInAirTime / 400));
            }
        }

        
        if (isParticleRotServerSafe(world, entity1))
        {
        	
            float var = WeatherUtilParticle.getParticleWeight((EntityRotFX)entity1);

            if (var != -1)
            {
                return var;
            }
        }

        if (entity1 instanceof SquidEntity)
        {
            return 400F;
        }

        /*if (entity1 instanceof EntityPlayerProxy) {
        	return 50F;
        }*/

        if (entity1 instanceof LivingEntity)
        {
        	LivingEntity livingEnt = (LivingEntity) entity1;
            //if (entity1.onGround || entity1.handleWaterMovement())
            //{
                //entity1.onGround = false;
                //c_CoroWeatherUtil.setEntityAge((EntityLivingBase)entity1, -150);
        	int airTime = livingEnt.getPersistentData().getInt("timeInAir");
        	if (livingEnt.onGround || livingEnt.handleWaterMovement())
            {
                airTime = 0;
            }
            else {
            	airTime++;
            }
        	
        	//test
        	//airTime = 0;
        	
        	livingEnt.getPersistentData().putInt("timeInAir", airTime);
            //}

            //System.out.println(((EntityLivingBase)entity1).entityAge+150);
            //int age = ((Integer)entToAge.get(entity1)).intValue();
            //System.out.println(age);
            
        }

        if (entity1 instanceof Entity) {
            Entity ent = (Entity) entity1;
            if (WeatherUtilData.isWindWeightSet(ent) && (forTornado || WeatherUtilData.isWindAffected(ent))) {
                return WeatherUtilData.getWindWeight(ent);
            }
        }

        if (entity1 instanceof LivingEntity) {
            LivingEntity livingEnt = (LivingEntity) entity1;
            int airTime = livingEnt.getPersistentData().getInt("timeInAir");
            if (forTornado) {
                return 0.5F + (((float)airTime) / 800F);
            } else {
                return 500.0F + (livingEnt.onGround ? 2.0F : 0.0F) + ((airTime) / 400);
            }
        }

        if (/*entity1 instanceof EntitySurfboard || */entity1 instanceof BoatEntity || entity1 instanceof ItemEntity/* || entity1 instanceof EntityTropicalFishHook*/ || entity1 instanceof FishingBobberEntity)
        {
            return 4000F;
        }

        if (entity1 instanceof AbstractMinecartEntity)
        {
            return 80F;
        }

        return 1F;
    }
    
    public static boolean isParticleRotServerSafe(World world, Object obj) {
    	if (EffectiveSide.get().equals(LogicalSide.SERVER)) {
    		return false;
    	}
    	if (!world.isRemote) return false;
    	return isParticleRotClientCheck(obj);
    }
    
    public static boolean isParticleRotClientCheck(Object obj) {
    	return obj instanceof EntityRotFX;
    }
    
	/*public static boolean canPushEntity(Entity ent)
    {
    	
    	//weather2: shouldnt be needed since its particles only now, ish
    	//if (!WeatherUtil.canUseWindOn(ent)) return false;
    	
    	WindManager windMan = ClientTickHandler.weatherManager.windMan;
    	
        double speed = 10.0D;
        int startX = (int)(ent.posX - speed * (double)(-MathHelper.sin(windMan.getWindAngleForPriority(null) / 180.0F * (float)Math.PI) * MathHelper.cos(0F*//*weatherMan.wind.yDirection*//* / 180.0F * (float)Math.PI)));
        int startZ = (int)(ent.posZ - speed * (double)(MathHelper.cos(windMan.getWindAngleForPriority(null) / 180.0F * (float)Math.PI) * MathHelper.cos(0F*//*weatherMan.wind.yDirection*//* / 180.0F * (float)Math.PI)));

        if (ent instanceof PlayerEntity)
        {
            boolean bool = true;
        }

        return ent.world.rayTraceBlocks((new Vec3(ent.posX, ent.posY + (double)ent.getEyeHeight(), ent.posZ)).toMCVec(), (new Vec3(startX, ent.posY + (double)ent.getEyeHeight(), startZ)).toMCVec()) == null;
        //return true;
    }*/
	
	public static boolean isEntityOutside(Entity parEnt) {
		return isEntityOutside(parEnt, false);
	}
	
	public static boolean isEntityOutside(Entity parEnt, boolean cheapCheck) {
		return isPosOutside(parEnt.world, new Vec3(parEnt.posX, parEnt.posY, parEnt.posZ), cheapCheck);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos) {
		return isPosOutside(parWorld, parPos, false);
	}
	
	public static boolean isPosOutside(World parWorld, Vec3 parPos, boolean cheapCheck) {
		int rangeCheck = 5;
		int yOffset = 1;
		
		if (WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parPos.xCoord), 0, MathHelper.floor(parPos.zCoord))).getY() < parPos.yCoord+1) return true;
		
		if (cheapCheck) return false;
		
		Vec3 vecTry = new Vec3(parPos.xCoord + Direction.NORTH.getXOffset()*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + Direction.NORTH.getZOffset()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.xCoord + Direction.SOUTH.getXOffset()*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + Direction.SOUTH.getZOffset()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.xCoord + Direction.EAST.getXOffset()*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + Direction.EAST.getZOffset()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		vecTry = new Vec3(parPos.xCoord + Direction.WEST.getXOffset()*rangeCheck, parPos.yCoord+yOffset, parPos.zCoord + Direction.WEST.getZOffset()*rangeCheck);
		if (checkVecOutside(parWorld, parPos, vecTry)) return true;
		
		return false;
	}
	
	public static boolean checkVecOutside(World parWorld, Vec3 parPos, Vec3 parCheckPos) {
		//boolean dirNorth = parWorld.rayTraceBlocks(parPos.toMCVec(), parCheckPos.toMCVec()) == null;
        BlockRayTraceResult blockraytraceresult = WeatherUtil.rayTraceBlocks(parWorld, new RayTraceContextNoEntity(parPos.toMCVec(), parCheckPos.toMCVec(),
                RayTraceContextNoEntity.BlockMode.COLLIDER, RayTraceContextNoEntity.FluidMode.NONE));
		if (blockraytraceresult.getType() == RayTraceResult.Type.MISS) {
			if (WeatherUtilBlock.getPrecipitationHeightSafe(parWorld, new BlockPos(MathHelper.floor(parCheckPos.xCoord), 0, MathHelper.floor(parCheckPos.zCoord))).getY() < parCheckPos.yCoord) return true;
		}
		return false;
	}

    public static double getDistanceSqEntToPos(Entity ent, BlockPos pos) {
        double d0 = ent.posX - pos.getX();
        double d1 = ent.posY - pos.getY();
        double d2 = ent.posZ - pos.getZ();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }
}
