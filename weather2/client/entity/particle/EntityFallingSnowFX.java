package weather2.client.entity.particle;

import java.awt.Color;

import weather2.config.ConfigMisc;
import weather2.util.WeatherUtilParticle;

import CoroUtil.api.weather.WindHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.src.ModLoader;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extendedrenderer.particle.entity.EntityRotFX;
@SideOnly(Side.CLIENT)
public class EntityFallingSnowFX extends EntityRotFX implements WindHandler
{
    public int age;
    public float brightness;
    
    public int severityOfRainRate = 2;
    
    public EntityFallingSnowFX(World var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, int colorIndex)
    {
        super(var1, var2, var4, var6, var8, var10, var12);
        this.motionX = var8 + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.05F);
        this.motionY = var10 + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.05F);
        this.motionZ = var12 + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.05F);
        //Color IDS
        //0 = black/regular/default
        //1 = dirt
        //2 = sand
        //3 = water
        //4 = snow
        //5 = stone
        Color color = null;
        
        if (colorIndex == 0)
        {
            this.particleRed = this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.3F/* + 0.7F*/;
        }
        else if (colorIndex == 1)
        {
            color = new Color(0x79553a);
        }
        else if (colorIndex == 2)
        {
            color = new Color(0xd6cf98);
        }
        else if (colorIndex == 3)
        {
            color = new Color(0x002aDD);
        }
        else if (colorIndex == 4)
        {
            color = new Color(0xeeffff);
        }
        else if (colorIndex == 5)
        {
            color = new Color(0x79553a);
        }
        else if (colorIndex == 6)
        {
            color = new Color(0xFFFFFF);
        }

        brightness = 2F;

        if (colorIndex != 0)
        {
            this.particleRed = color.getRed() / 255F;
            this.particleGreen = color.getGreen() / 255F;
            this.particleBlue = color.getBlue() / 255F;
        }

        this.particleScale = this.rand.nextFloat() * this.rand.nextFloat() * 6.0F;
        this.particleMaxAge = (int)(16.0D/* / ((double)this.rand.nextFloat() * 0.8D + 0.2D)*/) + 2;
        this.particleMaxAge = (int)((float)this.particleMaxAge * var14);
        this.particleGravity = 1.0F;
        //this.particleScale = 1F;
        this.setParticleTextureIndex(WeatherUtilParticle.effSnowID);
        
        noClip = true;
    }

    public void renderParticle(Tessellator var1, float var2, float var3, float var4, float var5, float var6, float var7)
    {
    	float framesX = 5;
    	float framesY = 1;
    	
    	float index = this.getParticleTextureIndex();
    	
    	//test
    	//index = 1;
    	
    	float var8 = (float)index / framesX;
        float var9 = var8 + (1F / framesX);
        float var10 = (float)index / framesY;
        float var11 = var10 + (1F / framesY);
    	
        /*float var8 = (float)(this.getParticleTextureIndex() % 16) / 16.0F;
        float var9 = var8 + 0.0624375F;
        float var10 = (float)(this.getParticleTextureIndex() / 16) / 16.0F;
        float var11 = var10 + 0.0624375F;*/
        float var12 = 0.1F * this.particleScale;
        /*if (RenderManager.instance.playerViewX < 0) {
        	var12 = var12 / Math.abs(RenderManager.instance.playerViewX);
        }*/
        //System.out.println(var12);
        
        //float var16 = this.getBrightness(var2) * brightness;
        //var16 = (1.3F + ModLoader.getMinecraftInstance().gameSettings.gammaSetting) - (this.worldObj.calculateSkylightSubtracted(var2) * 0.13F);
        
        Minecraft mc = Minecraft.getMinecraft();
        float br = ((0.9F + (mc.gameSettings.gammaSetting * 0.1F)) - (mc.theWorld.calculateSkylightSubtracted(var2) * 0.03F)) * mc.theWorld.getSunBrightness(1F);
        br = 0.55F * Math.max(0.3F, br) * (2F);
        
        var1.setColorRGBA_F(this.particleRed * br, this.particleGreen * br, this.particleBlue * br, particleAge * 0.1F);
        
        //TEEEEEEMMMMMPPPPPPP
        /*float range = 20F;
        for (int i = 0; i < WeatherUtilParticle.maxRainDrops; i++) {
        	WeatherUtilParticle.rainPositions[i] = Vec3.createVectorHelper((rand.nextFloat() * range) - (range/2), (rand.nextFloat() * range/16) - (range/32), (rand.nextFloat() * range) - (range/2));
        }*/
        
        int rainDrops = 5 + ((Math.max(0, severityOfRainRate-1)) * 5);
        
        //rainDrops *= ConfigMisc.Particle_Precipitation_effect_rate;
        
        for (int i = 0; i < Math.min(rainDrops, WeatherUtilParticle.maxRainDrops); i++) {
	        float var13 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)var2 - interpPosX);
	        float var14 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)var2 - interpPosY);
	        float var15 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)var2 - interpPosZ);
        
	        if (i != 0) {
		        var13 += WeatherUtilParticle.rainPositions[i].xCoord;
		        var14 += WeatherUtilParticle.rainPositions[i].yCoord;
		        var15 += WeatherUtilParticle.rainPositions[i].zCoord;
	        }
	        
	        var1.addVertexWithUV((double)(var13 - var3 * var12 - var6 * var12), (double)(var14 - var4 * var12), (double)(var15 - var5 * var12 - var7 * var12), (double)var9, (double)var11);
	        var1.addVertexWithUV((double)(var13 - var3 * var12 + var6 * var12), (double)(var14 + var4 * var12), (double)(var15 - var5 * var12 + var7 * var12), (double)var9, (double)var10);
	        var1.addVertexWithUV((double)(var13 + var3 * var12 + var6 * var12), (double)(var14 + var4 * var12), (double)(var15 + var5 * var12 + var7 * var12), (double)var8, (double)var10);
	        var1.addVertexWithUV((double)(var13 + var3 * var12 - var6 * var12), (double)(var14 - var4 * var12), (double)(var15 + var5 * var12 - var7 * var12), (double)var8, (double)var11);
	        
	        /*var1.addVertexWithUV((double)(var13 - var3 * var12 - var6 * var12), (double)(var14 - var4 * var12), (double)(var15 - var5 * var12 - var7 * var12), (double)0, (double)0);
	        var1.addVertexWithUV((double)(var13 - var3 * var12 + var6 * var12), (double)(var14 + var4 * var12), (double)(var15 - var5 * var12 + var7 * var12), (double)0, (double)0.2);
	        var1.addVertexWithUV((double)(var13 + var3 * var12 + var6 * var12), (double)(var14 + var4 * var12), (double)(var15 + var5 * var12 + var7 * var12), (double)0.2, (double)0.2);
	        var1.addVertexWithUV((double)(var13 + var3 * var12 - var6 * var12), (double)(var14 - var4 * var12), (double)(var15 + var5 * var12 - var7 * var12), (double)0.2, (double)0);*/
        }
        //var13 += i;//rand.nextInt(6)-3;
        //var14 += j;
        //var15 += k;
        
        //var1.setColorOpaque_F(this.particleRed * var16, this.particleGreen * var16, this.particleBlue * var16);
        
        /*}
        }
        }*/
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;

        //System.out.println("this.worldObj.getHeightValue((int)Math.floor(posX), (int)Math.floor(posZ)): " + this.worldObj.getHeightValue((int)Math.floor(posX), (int)Math.floor(posZ)));
        
        if (this.particleAge++ >= this.particleMaxAge || this.onGround || this.isInWater() || posY+this.motionY < this.worldObj.getHeightValue((int)Math.floor(posX), (int)Math.floor(posZ)))
        {
            this.setDead();
        }

        //this.particleTextureIndex = 7 - this.particleAge * 8 / this.particleMaxAge;
        //this.particleTextureIndex = 7 - this.particleAge * 8 / this.particleMaxAge;
        this.setParticleTextureIndex(WeatherUtilParticle.effSnowID);
        //this.motionY += 0.0040D;
        this.motionY -= 0.001D * (double)this.particleGravity;
        //this.motionY -= 0.05000000074505806D;
        float var20 = 0.98F;
        this.motionX *= (double)var20;
        //this.motionY *= (double)var20;
        this.motionZ *= (double)var20;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        /*this.motionX *= 0.8999999761581421D;
        this.motionY *= 0.8999999761581421D;
        this.motionZ *= 0.8999999761581421D;
        if(this.onGround) {
           this.motionX *= 0.699999988079071D;
           this.motionZ *= 0.699999988079071D;
        }*/
        
        //float snow
        if (motionY < -0.2) {
        	motionY = -0.2F;
        }
    }

    public int getFXLayer()
    {
        return 5;
    }

	@Override
	public float getWindWeight() {
		// TODO Auto-generated method stub
		return 10;
	}

	@Override
	public int getParticleDecayExtra() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public float maxRenderRange() {
    	return 40F;
    }
}
