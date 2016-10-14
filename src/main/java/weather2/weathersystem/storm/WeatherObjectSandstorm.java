package weather2.weathersystem.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.weathersystem.WeatherManagerBase;
import CoroUtil.util.Vec3;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorFogGround;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleTexFX;

/**
 * spawns in sandy biomes
 * needs high wind event
 * starts small size grows up to something like 80 height
 * needs to sorda stay near sand, to be fed
 * where should position be? stay in sand biome? travel outside it?
 * - main position is moving, where the front of the storm is
 * - store original spawn position, spawn particles of increasing height from spawn to current pos
 * 
 * build up sand like snow?
 * usual crazy storm sounds
 * hurt plantlife leafyness
 * 
 * 
 * 
 * 
 * @author Corosus
 *
 */
public class WeatherObjectSandstorm extends WeatherObject {

	public int height = 0;
	
	public Vec3 posSpawn = new Vec3(0, 0, 0);
	
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesCloud;
	
	public ParticleBehaviorFogGround particleBehaviorFog;
	
	public int age = 0;
	public int maxAge = 20*20;
	
	public WeatherObjectSandstorm(WeatherManagerBase parManager) {
		super(parManager);
		
		this.stormType = EnumStormType.SAND;
		
		if (parManager.getWorld().isRemote) {
			listParticlesCloud = new ArrayList<EntityRotFX>();
			
		}
	}
	
	public void initSandstormSpawn(Vec3 pos) {
		this.pos = new Vec3(pos);
		this.posSpawn = new Vec3(pos);
		
		/*height = 0;
		size = 0;
		
		maxSize = 300;
		maxHeight = 100;*/
	}
	
	@Override
	public void tick() {
		super.tick();
		
		boolean testGrowth = true;
	
		//assume its a high wind event, until that feature is coded, buff wind speed this depends on
		
		if (testGrowth) {
			/*if (size < maxSize) {
				size++;
			}
			
			if (height < maxHeight) {
				height++;
			}*/
			
			if (!manager.getWorld().isRemote) {
				age++;
				System.out.println("sandstorm age: " + age);
				if (age >= maxAge) {
					this.setDead();
					return;
				}
			}
		}
		
		float angle = manager.getWindManager().getWindAngleForClouds();
		
		double vecX = -Math.sin(Math.toRadians(angle));
		double vecZ = Math.cos(Math.toRadians(angle));
		double speed = 5D;
		
		//this.pos.xCoord += vecX * speed;
		//this.pos.zCoord += vecZ * speed;
		
		if (manager.getWorld().isRemote) {
			tickClient();
		}
		
		
		
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient() {
		Minecraft mc = Minecraft.getMinecraft();
		
		if (particleBehaviorFog == null) {
			particleBehaviorFog = new ParticleBehaviorFogGround(pos);
		}
		
		double size = 150;
    	//double height = 50;
    	double distanceToCenter = pos.distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
    	//how close to renderable particle wall
    	double distanceToFront = distanceToCenter - size;
    	boolean isInside = distanceToFront < 0;
    	
    	double circ = Math.PI * size;
    	
    	//double scale = 10;
    	double distBetweenParticles = 3;
    	
    	//double circScale = circ / distBetweenParticles;
    	
    	/**
    	 * if circ is 10, 10 / 3 size = 3 particles
    	 * if 30 circ / 3 size = 10 particles
    	 * if 200 circ / 3 size = 66 particles
    	 * 
    	 * how many degrees do we need to jump, 
    	 * 360 / 3 part = 120
    	 * 360 / 10 part = 36
    	 * 360 / 66 part = 5.4
    	 * 
    	 */
    	
    	//need steady dist between particles
    	
    	double degRate = 360D / (circ / distBetweenParticles);
    	
    	if (mc.theWorld.getTotalWorldTime() % 40 == 0) {
    		//System.out.println("circ: " + circ);
    		//System.out.println("degRate: " + degRate);
    	}
    	
    	Random rand = mc.theWorld.rand;
    	
	    for (double i = 0; i < 360; i += degRate) {
	    	if ((mc.theWorld.getTotalWorldTime()) % 10 == 0) {
	    		double sizeRand = rand.nextDouble() * 30D - rand.nextDouble() * 30D;
	    		double x = pos.xCoord + (Math.sin(Math.toRadians(i)) * (size + sizeRand));
	    		double z = pos.zCoord + (Math.cos(Math.toRadians(i)) * (size + sizeRand));
	    		double y = pos.yCoord;
	    		
	    		TextureAtlasSprite sprite = ParticleRegistry.chicken;
	    		if (mc.theWorld.rand.nextInt(30) == 0) {
	    			//sprite = ParticleRegistry.smokeTest;
	    		}
	    		
	    		ParticleTexFX part = new ParticleTexFX(mc.theWorld, x, y, z
	    				, 0, 0, 0, sprite);
	    		particleBehaviorFog.initParticle(part);
	    		part.setFacePlayer(false);
	    		part.isTransparent = false;
	    		part.rotationYaw = (float) -i + rand.nextInt(20) - 10;//Math.toDegrees(Math.cos(Math.toRadians(i)) * 2D);
	    		part.rotationPitch = 0;
	    		part.setMaxAge(300);
	    		part.setGravity(0.09F);
	    		part.setAlphaF(1F);
	    		float brightnessMulti = 1F - (rand.nextFloat() * 0.3F);
	    		part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
	    		part.setScale(100);
	    		//particleBehaviorFog.particles.add(part);
	    		part.spawnAsWeatherEffect();
	    		//mc.effectRenderer.addEffect(part);
	    		
	    		
	    	}
    	}
	    
	    System.out.println("spawn particles at: " + pos);
	}
	
	@Override
	public NBTTagCompound nbtSyncForClient(NBTTagCompound nbt) {
		NBTTagCompound data = super.nbtSyncForClient(nbt);
		
		/*data.setInteger("posX", (int)pos.xCoord);
		data.setInteger("posY", (int)pos.yCoord);
		data.setInteger("posZ", (int)pos.zCoord);
		
		data.setLong("ID", ID);
		data.setInteger("size", size);
		data.setInteger("maxSize", maxSize);*/
		return data;
	}

}
