package weather2.entity;

import io.netty.buffer.ByteBuf;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import weather2.config.ConfigStorm;
import weather2.config.ConfigTornado;
import weather2.util.WeatherUtil;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.Vec3;

public class EntityMovingBlock extends Entity implements IEntityAdditionalSpawnData
{
    public Block tile;
    public static final int falling = 0;
    public static final int grabbed = 1;
    //mode 0 = use gravity
    public int mode;
    public static final float slowdown = 0.98F;
    public static final float curvature = 0.05F;
    public int metadata;
    public TileEntity tileentity;
    public Material material;
    public int age;
    //i think type was used to change behavior between tornado based ones and hostile worlds ones?
    //currently nothing in weather2 sets it to 1, always 0
    public int type;
    public boolean noCollision;
    public boolean collideFalling = false;
    public double vecX;
    public double vecY;
    public double vecZ;
    public double lastPosX;
    public double lastPosZ;
    //public Entity controller;
    public StormObject owner;
    public int gravityDelay;

    public boolean killNextTick = false;

    public IBlockState stateCached = null;

    public EntityMovingBlock(World var1)
    {
        super(var1);
        this.mode = 1;
        this.age = 0;
        this.tile = Blocks.STONE;
        this.noCollision = true;
        this.gravityDelay = 60;
    }

    public EntityMovingBlock(World var1, int var2, int var3, int var4, IBlockState state, StormObject parOwner)
    {
        super(var1);
        this.mode = 1;
        this.age = 0;
        this.type = 0;
        this.noCollision = false;
        this.gravityDelay = 60;
        this.noCollision = true;
        this.setSize(0.9F, 0.9F);
        //this.yOffset = this.height / 2.0F;
        this.setPosition(var2 + 0.5D, var3 + 0.5D, var4 + 0.5D);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.prevPosX = (var2 + 0.5F);
        this.prevPosY = (var3 + 0.5F);
        this.prevPosZ = (double)(var4 + 0.5F);

        this.tile = state.getBlock();
        this.metadata = state.getBlock().getMetaFromState(state);
        this.material = tile.getMaterial(tile.getDefaultState());
        this.stateCached = state;
        //this.tileentity = var1.getTileEntity(new BlockPos(var2, var3, var4));

        owner = parOwner;
        
        if (this.tileentity != null)
        {
            //var1.setBlockStateTileEntity(var2, var3, var4, ((BlockContainer)Block.blocksList[this.tile]).createNewTileEntity(var1));
            //var1.setBlockState(new BlockPos(var2, var3, var4), Blocks.AIR.getDefaultState(), 2);
        }
    }

    @Override
    public boolean isInRangeToRenderDist(double var1)
    {
        //return super.isInRangeToRenderDist(var1);
        return var1 < 256D * 256D;
    }

    @Override
    public boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public void entityInit() {}

    @Override
    public boolean canBePushed()
    {
        return !this.isDead;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return !this.isDead && !this.noCollision;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    	//new kill off when distant method
    	if (!world.isRemote) {
    	    if (killNextTick) {
    	        setDead();
            }
	    	if (this.world.getClosestPlayer(this.posX, 50, this.posZ, 512, false) == null) {
				setDead();
				//return;
			}
    	}
    	
        if (CoroUtilBlock.isAir(this.tile))
        {
            this.setDead();
            //return;
        }
        else
        {
            ++this.age;

            if (this.age > this.gravityDelay && this.type == 0)
            {
                this.mode = 0;

                if (this.tileentity == null && ConfigTornado.Storm_Tornado_rarityOfDisintegrate != -1 && this.rand.nextInt((ConfigTornado.Storm_Tornado_rarityOfDisintegrate + 1 + (owner != null && owner.isFirenado ? 100 : 0)) * 20) == 0)
                {
                    this.setDead();
                    //return;
                }

                /*if (this.tileentity == null && ConfigTornado.Storm_Tornado_rarityOfFirenado != -1 && this.rand.nextInt((ConfigTornado.Storm_Tornado_rarityOfFirenado + 1) * 20) == 0)
                {
                    this.tile = Blocks.FIRE;
                }*/
            }

            if (this.type == 0)
            {
            	/*if (this.controller != null) {
	                this.vecX = this.controller.posX - this.posX;
	                this.vecY = this.controller.boundingBox.minY + (double)(this.controller.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
	                this.vecZ = this.controller.posZ - this.posZ;
            	} else {*/
            		this.vecX++;
	                this.vecY++;
	                this.vecZ++;
            	//}
            }

            if (this.mode == 1)
            {
                this.fallDistance = 0.0F;
                this.isCollidedHorizontally = false;
            }

            /*Field fire = null;
            int fireInt = 0;
            try {
             fire = Entity.class.getDeclaredField("c");
             fire.setAccessible(true);
             fireInt = (int)fire.get(ent);
            } catch (Exception ex) {
             try {
              fire = Entity.class.getDeclaredField("fire");
                 fire.setAccessible(true);
                 fireInt = (int)fire.get(ent);
             } catch (Exception ex2) {
             }
            }*/
            /*if(this.fire > 0) {
               --this.fire;
            }*/
            Vec3d var1 = new Vec3d(this.posX, this.posY, this.posZ);
            Vec3d var2 = new Vec3d(this.posX + this.motionX * 1.3D, this.posY + this.motionY * 1.3D, this.posZ + this.motionZ * 1.3D);
            RayTraceResult var3 = this.world.rayTraceBlocks(var1, var2);
            var2 = new Vec3d(this.posX + this.motionX * 1.3D, this.posY + this.motionY * 1.3D, this.posZ + this.motionZ * 1.3D);

            if (var3 != null)
            {
                var2 = new Vec3d(var3.hitVec.x, var3.hitVec.y, var3.hitVec.z);
            }

            Entity var4 = null;
            List var5 = null;

            if (this.age > this.gravityDelay / 4)
            {
                var5 = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(this.motionX, this.motionY, this.motionZ));
            }

            double var6 = 0.0D;
            int var8;
            int var9;
            int var11;

            for (var8 = 0; var5 != null && var8 < var5.size() && var8 < 5; ++var8)
            {
                Entity var10 = (Entity)var5.get(var8);

                if (!(var10 instanceof EntityMovingBlock) && var10.canBeCollidedWith() && this.canEntityBeSeen(var10))
                {
                	if (!(var10 instanceof EntityPlayer) || !((EntityPlayer)var10).capabilities.isCreativeMode) {
	                    var10.motionX = this.motionX / 1.5D;
	                    var10.motionY = this.motionY / 1.5D;
	                    var10.motionZ = this.motionZ / 1.5D;
                	}
                    
                    if (ConfigStorm.Storm_FlyingBlocksHurt && Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) > 0.4F) {
                    	//System.out.println("damaging with block: " + var10);
                    	
                    	DamageSource ds = DamageSource.causeThrownDamage(this, this);
                		ds.damageType = "wm.movingblock";
                		var10.attackEntityFrom(ds, 4);
                    }
                }

                if (var10.canBeCollidedWith() && !this.noCollision)
                {
                    if (var10.canBePushed())
                    {
                        var10.getDistanceSqToEntity(this);

                        if (this.isBurning())
                        {
                            var10.setFire(15);
                        }

                        if (this.tile == Blocks.CACTUS)
                        {
                            var10.attackEntityFrom(DamageSource.causeThrownDamage(this, this), 1);
                        }
                        else if (this.material == Material.LAVA)
                        {
                        	var10.setFire(15);
                        }
                        else
                        {
                            var9 = MathHelper.floor(this.posX);
                            var11 = MathHelper.floor(this.posY);
                            int var12 = MathHelper.floor(this.posZ);
                            BlockPos pos = new BlockPos(var9, var11, var12);
                            IBlockState state = world.getBlockState(pos);
                            tile.onEntityCollidedWithBlock(this.world, pos, state, var10);
                        }
                    }

                    float var16 = 0.3F;
                    AxisAlignedBB var19 = var10.getEntityBoundingBox().grow((double)var16, (double)var16, (double)var16);
                    RayTraceResult var13 = var19.calculateIntercept(var1, var2);

                    if (var13 != null)
                    {
                        double var14 = var1.distanceTo(var13.hitVec);

                        if (var14 < var6 || var6 == 0.0D)
                        {
                            var4 = var10;
                            var6 = var14;
                        }
                    }
                }
            }

            if (var4 != null)
            {
                var3 = new RayTraceResult(var4);
            }

            if (var3 != null && var3.entityHit == null && this.mode == 0)
            {
                var8 = var3.getBlockPos().getX();
                int var17 = var3.getBlockPos().getY();
                var9 = var3.getBlockPos().getZ();

                //0
                if (var3.sideHit == EnumFacing.DOWN)
                {
                    --var17;
                }

                //1
                if (var3.sideHit == EnumFacing.UP)
                {
                    ++var17;
                }

                //2
                if (var3.sideHit == EnumFacing.SOUTH)
                {
                    --var9;
                }

                //3
                if (var3.sideHit == EnumFacing.NORTH)
                {
                    ++var9;
                }

                //4
                if (var3.sideHit == EnumFacing.WEST)
                {
                    --var8;
                }

                //5
                if (var3.sideHit == EnumFacing.EAST)
                {
                    ++var8;
                }

                if (this.type == 0)
                {
                    if (var3.sideHit != EnumFacing.DOWN && !this.collideFalling)
                    {
                        if (!this.collideFalling)
                        {
                            this.collideFalling = true;
                            this.posX = MathHelper.floor(posX);
                            this.posZ = MathHelper.floor(posZ);
                            //this.posZ = (double)((int)(this.posZ + 0.0D));
                            this.setPosition(this.posX, this.posY, this.posZ);
                            this.motionX = 0.0D;
                            this.motionZ = 0.0D;
                        }
                    }
                    else
                    {
                        this.blockify(var8, var17, var9, var3.sideHit);
                    }

                    this.lastPosX = this.posX;
                    this.lastPosZ = this.posZ;
                }
                else
                {
                    this.blockify(var8, var17, var9, var3.sideHit);
                }

                return;
            }

            float var18 = 0.98F;

            if (this.type == 1)
            {
                var18 = (float)((double)var18 * 0.92D);

                if (this.mode == 0)
                {
                    this.motionY -= 0.05000000074505806D;
                }
            }
            else
            {
                this.motionY -= 0.05000000074505806D;
            }

            this.motionX *= (double)var18;
            this.motionY *= (double)var18;
            this.motionZ *= (double)var18;
            var11 = (int)(this.posX + this.motionX * 5.0D);
            byte var20 = 50;
            int var21 = (int)(this.posZ + this.motionZ * 5.0D);

            if (!this.world.isBlockLoaded(new BlockPos(var11, var20, var21)))
            {
                this.setDead();
                //return;
            }

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.mode == 1)
            {
                //this.moveEntity(this.motionX, this.motionY, this.motionZ);
                this.posX += this.motionX;
                this.posY += this.motionY;
                this.posZ += this.motionZ;
            }
            else if (this.mode == 0)
            {
                this.posX += this.motionX;
                this.posY += this.motionY;
                this.posZ += this.motionZ;
            }

            this.setPosition(this.posX, this.posY, this.posZ);
        }
    }

    public boolean canEntityBeSeen(Entity par1Entity)
    {
        return this.world.rayTraceBlocks(new Vec3d(this.posX, this.posY + (double)this.getEyeHeight(), this.posZ), new Vec3d(par1Entity.posX, par1Entity.posY + (double)par1Entity.getEyeHeight(), par1Entity.posZ)) == null;
    }

    private void blockify(int var1, int var2, int var3, EnumFacing var4)
    {
        //TODO: this was the only thing killing off moving blocks on client side, syncing is broken server to client?

        //if (true) return;
        if (this.world.isRemote) return;
        this.setDead();

        Block var5 = this.world.getBlockState(new BlockPos(var1, var2, var3)).getBlock();

        if (this.tileentity != null || this.type != 0 || ConfigTornado.Storm_Tornado_rarityOfBreakOnFall > 0 && this.rand.nextInt(ConfigTornado.Storm_Tornado_rarityOfBreakOnFall + 1) != 0)
        {
            if (!WeatherUtil.shouldRemoveBlock(var5) && !WeatherUtil.isOceanBlock(var5) && var2 < 255)
            {
                this.world.setBlockState(new BlockPos(var1, var2 + 1, var3), this.tile.getStateFromMeta(this.metadata), 3);
            }

            boolean var6 = false;

            if (!WeatherUtil.isOceanBlock(var5))
            {
                if (this.world.setBlockState(new BlockPos(var1, var2, var3), this.tile.getStateFromMeta(this.metadata), 3))
                {
                    var6 = true;
                }
            }/*
            else
            {
                this.world.setBlockState(new BlockPos(var1, var2, var3), WeatherMod.finiteWaterId.getDefaultState(), this.metadata, 3);

                if (var2 < 255)
                {
                    this.world.setBlockState(new BlockPos(var1, var2 + 1, var3), WeatherMod.finiteWaterId.getDefaultState(), this.metadata, 3);
                }
            }*/

            if (var6)
            {
                //Block.blocksList[this.tile].onBlockPlacedBy(this.world, var1, var2, var3, var4, this);
                /*if (this.tileentity != null)
                {
                    this.world.setTileEntity(new BlockPos(var1, var2, var3), this.tileentity);
                }*/
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1)
    {
        var1.setString("Tile", Block.REGISTRY.getNameForObject(tile).toString());
        var1.setByte("Metadata", (byte)this.metadata);
        var1.setInteger("blocktype", type);
        NBTTagCompound var2 = new NBTTagCompound();

        if (this.tileentity != null)
        {
            this.tileentity.writeToNBT(var2);
        }

        var1.setTag("TileEntity", var2);
        
        
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound var1)
    {
        this.tile = (Block)Block.REGISTRY.getObject(new ResourceLocation(var1.getString("Tile")));
        this.metadata = var1.getByte("Metadata") & 15;
        this.type = var1.getInteger("blocktype");
        this.tileentity = null;

        if (this.tile instanceof BlockContainer)
        {
            this.tileentity = ((BlockContainer)this.tile).createNewTileEntity(world, metadata);
            NBTTagCompound var2 = var1.getCompoundTag("TileEntity");
            this.tileentity.readFromNBT(var2);
        }
        
        if (type == 0) {
            //setDead(); //kill flying block on reload for tornado spazing fix
            killNextTick = true;
        }
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return super.isInRangeToRender3d(x, y, z);
    }

    @Override
    public void setDead()
    {
    	/*if (!world.isRemote) {
    		if (owner != null && owner.tornadoHelper != null) {
    			--owner.tornadoHelper.blockCount;
    			
    	        if (owner.tornadoHelper.blockCount < 0)
    	        {
    	        	owner.tornadoHelper.blockCount = 0;
    	        }
    		}
	        
    	}*/
    	
    	owner = null;
        super.setDead();
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        String str = "blank";
        if (tile != null && Block.REGISTRY.getNameForObject(tile) != null) {
            str = Block.REGISTRY.getNameForObject(tile).toString();
        }
    	ByteBufUtils.writeUTF8String(data, str);
        data.writeInt(metadata);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        String str = ByteBufUtils.readUTF8String(data);
        if (!str.equals("blank")) {
            tile = Block.REGISTRY.getObject(new ResourceLocation(str));
            metadata = data.readInt();
        } else {
            tile = Blocks.STONE;
            metadata = 0;
        }

        stateCached = tile.getStateFromMeta(metadata);
    }
}
