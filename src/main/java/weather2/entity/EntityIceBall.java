package weather2.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.api.weather.IWindHandler;
import CoroUtil.entity.EntityThrowableUsefull;
import CoroUtil.util.Vec3;

public class EntityIceBall extends EntityThrowableUsefull implements IWindHandler
{
	public int ticksInAir;
	
	@SideOnly(Side.CLIENT)
	public boolean hasDeathTicked;

	public EntityIceBall(World world)
	{
		super(world);
	}

	public EntityIceBall(World world, EntityLivingBase entityliving)
	{
		super(world, entityliving);
		
		float speed = 0.7F;
		float f = 0.4F;
        this.motionX = (double)(-MathHelper.sin(-this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(-this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionZ = (double)(MathHelper.cos(-this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(-this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionY = (double)(-MathHelper.sin((-this.rotationPitch + this.func_70183_g()) / 180.0F * (float)Math.PI) * f);
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, speed, 1.0F);
	}

	public EntityIceBall(World world, double d, double d1, double d2)
	{
		super(world, d, d1, d2);
	}
	
	@Override
	public void onUpdate()
    {
		super.onUpdate();
		
		//gravity
		this.motionY -= 0.1F;
		
		if (this.motionY <= -3) {
			this.motionY = -3;
		}
		
		if (!this.world.isRemote)
        {
			
			ticksInAir++;
			
			if (this.isCollided) {
				setDead();
			}
			
			if (ticksInAir > 120) {
				setDead();
			}
			
			if (this.world.getClosestPlayer(this.posX, 50, this.posZ, 80, false) == null) {
				setDead();
			}
			
			if (isInWater()) {
				setDead();
			}
        } else {
        	tickAnimate();
        }
    }
	
	@Override
	protected float getGravityVelocity() {
		return 0F;
	}
	
	@Override
	public RayTraceResult tickEntityCollision(Vec3 vec3, Vec3 vec31) {
		RayTraceResult movingobjectposition = null;
		
        Entity entity = null;
        List list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(this.motionX, this.motionY, this.motionZ).grow(0.5D, 1D, 0.5D));
        double d0 = 0.0D;
        EntityLivingBase entityliving = this.getThrower();

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);

            if (entity1.canBeCollidedWith() && (entity1 != entityliving && this.ticksInAir >= 4))
            {
                entity = entity1;
                break;
            }
        }

        if (entity != null)
        {
            movingobjectposition = new RayTraceResult(entity);
            /*if (movingobjectposition != null) {
            	this.onImpact(movingobjectposition);
            	setDead();
            }*/
        }
        return movingobjectposition;
	}

	@Override
	protected void onImpact(RayTraceResult movingobjectposition)
	{
		
		if (movingobjectposition.entityHit != null)
		{
			if (!world.isRemote)
			{
				
				byte damage = 5;
				
				movingobjectposition.entityHit.attackEntityFrom(DamageSource.FALLING_BLOCK, damage);

				if (!world.isRemote) {
					setDead();
				}

			}
		}
		
		
		
		if (!world.isRemote) {
			world.playSound(null, new BlockPos(posX, posY, posZ), SoundEvents.BLOCK_STONE_STEP, SoundCategory.AMBIENT, 3F, 5F);//0.2F + world.rand.nextFloat() * 0.1F);
			setDead();
			//System.out.println("server: " + posX);
		} else {
			tickDeath();
		}
		
	}
	
	@Override
	public void setDead() {
		if (world.isRemote) tickDeath();
		super.setDead();
	}
	
	@SideOnly(Side.CLIENT)
	public void tickAnimate() {
		
	}
	
	@SideOnly(Side.CLIENT)
	public void tickDeath() {
		if (!hasDeathTicked) {
			//System.out.println("client: " + posX);
			hasDeathTicked = true;
		}
	}

	@Override
	public float getWindWeight() {
		return 4;
	}

	@Override
	public int getParticleDecayExtra() {
		return 0;
	}
}
