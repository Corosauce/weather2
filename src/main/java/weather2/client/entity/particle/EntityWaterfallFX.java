package weather2.client.entity.particle;

import java.awt.Color;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.api.weather.IWindHandler;
import extendedrenderer.particle.entity.EntityRotFX;
@SideOnly(Side.CLIENT)
public class EntityWaterfallFX extends EntityRotFX implements IWindHandler
{
    public int age;
    public float brightness;

    public EntityWaterfallFX(World var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, int var16)
    {
        super(var1, var2, var4, var6, var8, var10, var12);
        this.motionX = var8 + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.05F);
        this.motionY = var10 + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.05F);
        this.motionZ = var12 + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.05F);
        Color var17 = null;

        if (var16 == 0)
        {
            this.particleRed = this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.3F;
        }
        else if (var16 == 1)
        {
            var17 = new Color(0xFF5000);
        }
        else if (var16 == 2)
        {
            var17 = new Color(0x0000FF);
            //var17 = new Color(0xFFFFFF);
        }
        else if (var16 == 3)
        {
        	var17 = new Color(0x6666FF);
        }
        else if (var16 == 4)
        {
            var17 = new Color(0xFFFFFF);
        }
        else if (var16 == 5)
        {
            var17 = new Color(7951674);
        }

        this.brightness = 1.0F;

        if (var17 != null && var16 != 0)
        {
            this.particleRed = (float)var17.getRed() / 255.0F;
            this.particleGreen = (float)var17.getGreen() / 255.0F;
            this.particleBlue = (float)var17.getBlue() / 255.0F;
        }

        //this.particleScale = this.rand.nextFloat() * this.rand.nextFloat() * 6.0F + 1.0F;
        this.particleMaxAge = 18;
        this.particleMaxAge = (int)((double)((float)this.particleMaxAge) * var14);
        
        this.particleGravity = 0.2F;
        this.particleScale = 0.5F;
        
        setParticleTextureIndex(0);
    }
    
    @Override
    public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn, float var2, float var3, float var4, float var5, float var6, float var7) {
    	float var8 = (float)(this.getParticleTextureIndex() % 16) / 16.0F;
        float var9 = var8 + 0.0624375F;
        float var10 = (float)(this.getParticleTextureIndex() / 16) / 16.0F;
        float var11 = var10 + 0.0624375F;
        float var12 = 0.1F * this.particleScale;
        float var13 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)var2 - interpPosX);
        float var14 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)var2 - interpPosY);
        float var15 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)var2 - interpPosZ);
        float var16 = this.getBrightnessForRender(var2) * this.brightness;
        var16 = (1F + FMLClientHandler.instance().getClient().gameSettings.gammaSetting) - (this.world.calculateSkylightSubtracted(var2) * 0.13F);
        
        
        
        /*var1.setColorOpaque_F(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16);
        var1.addVertexWithUV((double)(var13 - var3 * var12 - var6 * var12), (double)(var14 - var4 * var12), (double)(var15 - var5 * var12 - var7 * var12), (double)var9, (double)var11);
        var1.addVertexWithUV((double)(var13 - var3 * var12 + var6 * var12), (double)(var14 + var4 * var12), (double)(var15 - var5 * var12 + var7 * var12), (double)var9, (double)var10);
        var1.addVertexWithUV((double)(var13 + var3 * var12 + var6 * var12), (double)(var14 + var4 * var12), (double)(var15 + var5 * var12 + var7 * var12), (double)var8, (double)var10);
        var1.addVertexWithUV((double)(var13 + var3 * var12 - var6 * var12), (double)(var14 - var4 * var12), (double)(var15 + var5 * var12 - var7 * var12), (double)var8, (double)var11);*/
        
        int i = 65535;//this.getBrightnessForRender(partialTicks);
        int j = i >> 16 & 65535;
        int k = i & 65535;
        
        worldRendererIn.pos((double)(var13 - var3 * var12 - var6 * var12), (double)(var14 - var4 * var12), (double)(var15 - var5 * var12 - var7 * var12)).tex((double)var9, (double)var11)
        .color(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16, this.particleAlpha).lightmap(j, k).endVertex();
        
        worldRendererIn.pos((double)(var13 - var3 * var12 + var6 * var12), (double)(var14 + var4 * var12), (double)(var15 - var5 * var12 + var7 * var12)).tex((double)var9, (double)var10)
        .color(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16, this.particleAlpha).lightmap(j, k).endVertex();
        
        worldRendererIn.pos((double)(var13 + var3 * var12 + var6 * var12), (double)(var14 + var4 * var12), (double)(var15 + var5 * var12 + var7 * var12)).tex((double)var8, (double)var10)
        .color(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16, this.particleAlpha).lightmap(j, k).endVertex();
        
        worldRendererIn.pos((double)(var13 + var3 * var12 - var6 * var12), (double)(var14 - var4 * var12), (double)(var15 + var5 * var12 - var7 * var12)).tex((double)var8, (double)var11)
        .color(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16, this.particleAlpha).lightmap(j, k).endVertex();
    }

    public void renderParticle(Tessellator var1, float var2, float var3, float var4, float var5, float var6, float var7)
    {
    	//GL11.glBindTexture(GL11.GL_TEXTURE_2D, RenderManager.instance.renderEngine.getTexture("/particles.png"));
    	
        
    }
    
    @Override
    public int getFXLayer()
    {
        return 0;
    }

    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        
        float adj = 0.08F * rand.nextFloat();
        //this.motionX += adj * Math.sin(world.getWorldTime());
        //this.motionZ += adj * Math.sin(world.getWorldTime());
        //this.motionY += adj * Math.cos(world.getWorldTime());

        if (particleRed < 255) particleRed += 0.01F;
        if (particleGreen < 255) particleGreen += 0.01F;
        if (particleBlue < 255) particleBlue += 0.01F;
        
        
        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setExpired();
        }
        
        this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        //this.setParticleTextureIndex(7 - this.particleAge * 8 / this.particleMaxAge);
        
        Block id = this.world.getBlockState(new BlockPos((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ))).getBlock();
        //int id2 = this.world.getBlockId((int)Math.floor(posX), (int)Math.floor(posY-1), (int)Math.floor(posZ));
        
        
        
        int meta = 0;
        
        if (id.getMaterial(id.getDefaultState()) == Material.WATER/*id == 9 || id == 8*/) {
        	
        	BlockPos pos = new BlockPos((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ));
        	
        	//patch for missing getFlowDirection, based on its code, could just strait up use this method and test new speed
        	Vec3d vec3 = Blocks.FLOWING_WATER.modifyAcceleration(world, pos, null, new Vec3d(0, 0, 0));
        	double dir = -1000;
        	if (vec3.x != 0 && vec3.z != 0) {
        		dir = Math.atan2(vec3.z, vec3.x) - (Math.PI / 2D);
        	}
        	
        	
        	//double dir = BlockLiquid.getFlowDirection(world, pos, Material.WATER);
        	
        	if (dir != -1000) {
            	//System.out.println("uhhhh: " + dir);
        		
        		float speed = 0.005F;
        		
        		this.motionX -= Math.sin(dir) * speed;
        		this.motionZ += Math.cos(dir) * speed;
            }
        	
        	float range = 0.03F;
    		
    		this.motionX += (rand.nextFloat() * range) - (range/2);
    		//this.motionY += (rand.nextFloat() * range/2) - (range/4);
    		this.motionZ += (rand.nextFloat() * range) - (range/2);
        	
    		IBlockState state = this.world.getBlockState(pos);
    		
    		meta = state.getBlock().getMetaFromState(state);
    		
        	//meta = this.world.getBlockMetadata((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ));
        	
        	if ((meta & 8) != 0/* && (id2 == 8 || id2 == 9)*/) {
        		this.motionY -= 0.05000000074505806D * this.particleGravity;
        		
        		
        		
        	} else {
        		//double remain = ((this.boundingBox.minY) - ((int)Math.floor(this.boundingBox.minY)));
        		//System.out.println("remain: " + remain);
        		//if (remain < 0.3D) {
        		//this.handleWaterMovement();
        		//this.handleWaterMovement();
        		//if (remain <= 0.5D) {
        			this.motionY += (0.05F * this.particleGravity * 0.2F);/* * (remain / 4);*/
        			
        			//meta >= 4 && 
        		//}
        		//}
        	}
        	
        } else {
        	//setDead();
        	
        	this.motionY -= 0.05000000074505806D * this.particleGravity * 1.5F;
        	//if (this.onGround) this.setDead();
        }
        
        if (this.motionY > 0.03F) this.motionY = 0.03F;
        
        float var1 = 0.98F;
        this.motionX *= (double)var1;
        this.motionY *= (double)var1;
        this.motionZ *= (double)var1;
        this.move(this.motionX, this.motionY, this.motionZ);
        
        int meta2 = meta;
        
        if (meta2 > 9) meta2 = 9;
        
        float height = ((10-meta2) * 0.1F);
        
        //System.out.println("adjusted height: " + height);
        
        if ((id.getMaterial(id.getDefaultState()) == Material.WATER) && motionY > 0F && this.posY > ((int)Math.floor(this.posY)) + height) {
        	//System.out.println("meta: " + meta);
        	//this.posY = ((int)Math.floor(this.posY)) + height;
        	//this.setPosition(posX, posY, posZ);
        	this.motionY = -0.05F;
        }
    }

	@Override
	public float getWindWeight() {
		return 60F;
	}

	@Override
	public int getParticleDecayExtra() {
		return 0;
	}
}
