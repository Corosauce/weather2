package weather2.entity;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import weather2.config.ConfigMisc;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityLightningBolt extends EntityWeatherEffect
{
    /**
     * Declares which state the lightning bolt is in. Whether it's in the air, hit the ground, etc.
     */
    private int lightningState;

    /**
     * A random long that is used to change the vertex of the lightning rendered in RenderLightningBolt
     */
    public long boltVertex;

    /**
     * Determines the time before the EntityLightningBolt is destroyed. It is a random integer decremented over time.
     */
    private int boltLivingTime;

    public int fireLifeTime = ConfigMisc.Lightning_lifetimeOfFire;
    public int fireChance = ConfigMisc.Lightning_OddsTo1OfFire;
    
    public EntityLightningBolt(World par1World, double par2, double par4, double par6)
    {
        super(par1World);
        this.setLocationAndAngles(par2, par4, par6, 0.0F, 0.0F);
        this.lightningState = 2;
        this.boltVertex = this.rand.nextLong();
        this.boltLivingTime = this.rand.nextInt(3) + 1;

        Random rand = new Random();
        
        
        
        if (!par1World.isRemote && (fireChance == 0 || rand.nextInt(fireChance) == 0) && par1World.getGameRules().getGameRuleBooleanValue("doFireTick") && par1World.difficultySetting >= 2 && par1World.doChunksNearChunkExist(MathHelper.floor_double(par2), MathHelper.floor_double(par4), MathHelper.floor_double(par6), 10))
        {
            int i = MathHelper.floor_double(par2);
            int j = MathHelper.floor_double(par4);
            int k = MathHelper.floor_double(par6);

            if (par1World.getBlockId(i, j, k) == 0 && Block.fire.canPlaceBlockAt(par1World, i, j, k))
            {
                par1World.setBlock(i, j, k, Block.fire.blockID, fireLifeTime, 3);
            }

            for (i = 0; i < 4; ++i)
            {
                j = MathHelper.floor_double(par2) + this.rand.nextInt(3) - 1;
                k = MathHelper.floor_double(par4) + this.rand.nextInt(3) - 1;
                int l = MathHelper.floor_double(par6) + this.rand.nextInt(3) - 1;

                if (par1World.getBlockId(j, k, l) == 0 && Block.fire.canPlaceBlockAt(par1World, j, k, l))
                {
                    par1World.setBlock(j, k, l, Block.fire.blockID, fireLifeTime, 3);
                }
            }
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();
        
        //System.out.println("remote: " + worldObj.isRemote);

        //making client side only to fix cauldron issue
        if (worldObj.isRemote) {
	        if (this.lightningState == 2)
	        {
	            this.worldObj.playSound(this.posX, this.posY, this.posZ, "ambient.weather.thunder", 64.0F, 0.8F + this.rand.nextFloat() * 0.2F, false);
	            this.worldObj.playSound(this.posX, this.posY, this.posZ, "random.explode", 2.0F, 0.5F + this.rand.nextFloat() * 0.2F, false);
	        }
        }

        --this.lightningState;

        if (this.lightningState < 0)
        {
            if (this.boltLivingTime == 0)
            {
                this.setDead();
            }
            else if (this.lightningState < -this.rand.nextInt(10))
            {
                --this.boltLivingTime;
                this.lightningState = 1;
                this.boltVertex = this.rand.nextLong();

                if (!this.worldObj.isRemote && rand.nextInt(fireChance) == 0 && this.worldObj.getGameRules().getGameRuleBooleanValue("doFireTick") && this.worldObj.doChunksNearChunkExist(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ), 10))
                {
                    int i = MathHelper.floor_double(this.posX);
                    int j = MathHelper.floor_double(this.posY);
                    int k = MathHelper.floor_double(this.posZ);

                    if (this.worldObj.getBlockId(i, j, k) == 0 && Block.fire.canPlaceBlockAt(this.worldObj, i, j, k))
                    {
                        this.worldObj.setBlock(i, j, k, Block.fire.blockID, fireLifeTime, 3);
                    }
                }
            }
        }

        if (this.lightningState >= 0)
        {
            if (this.worldObj.isRemote)
            {
            	updateFlashEffect();
            }
            else
            {
                double d0 = 3.0D;
                List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, AxisAlignedBB.getAABBPool().getAABB(this.posX - d0, this.posY - d0, this.posZ - d0, this.posX + d0, this.posY + 6.0D + d0, this.posZ + d0));

                for (int l = 0; l < list.size(); ++l)
                {
                    Entity entity = (Entity)list.get(l);
                    //entity.onStruckByLightning(this);
                }
            }
        }
    }
    
    @SideOnly(Side.CLIENT)
    public void updateFlashEffect() {
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	//only flash sky if player is within 256 blocks of lightning
    	if (mc.thePlayer != null && mc.thePlayer.getDistanceToEntity(this) < 256) {
    		this.worldObj.lastLightningBolt = 2;
    	}
    }

    protected void entityInit() {}

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {}

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {}

    @SideOnly(Side.CLIENT)

    /**
     * Checks using a Vec3d to determine if this entity is within range of that vector to be rendered. Args: vec3D
     */
    public boolean isInRangeToRenderVec3D(Vec3 par1Vec3)
    {
        return this.lightningState >= 0;
    }
}
