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
import weather2.ClientTickHandler;
import weather2.client.entity.particle.ParticleSandstorm;
import weather2.util.WeatherUtil;
import weather2.weathersystem.WeatherManagerBase;
import CoroUtil.util.Vec3;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorFogGround;
import extendedrenderer.particle.behavior.ParticleBehaviorSandstorm;
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
 * take sand and relocate it forward in direction storm is pushing, near center of where stormfront is
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
	
	public ParticleBehaviorSandstorm particleBehavior;
	
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
		
		size = 15;
		maxSize = 100;
		
		maxAge = 20*60;
		
		float angle = manager.getWindManager().getWindAngleForClouds();
		
		double vecX = -Math.sin(Math.toRadians(angle));
		double vecZ = Math.cos(Math.toRadians(angle));
		double speed = 150D;
		
		this.pos.xCoord -= vecX * speed;
		this.pos.zCoord -= vecZ * speed;
		
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
				//System.out.println("sandstorm age: " + age);
				if (age >= maxAge) {
					this.setDead();
					return;
				}
				
				if (manager.getWorld().getTotalWorldTime() % 10 == 0) {
					if (size < maxSize) {
						size++;
					}
				}
				
			}
		}
		
		float angle = manager.getWindManager().getWindAngleForClouds();
		
		double vecX = -Math.sin(Math.toRadians(angle));
		double vecZ = Math.cos(Math.toRadians(angle));
		double speed = 0.2D;
		
		this.pos.xCoord += vecX * speed;
		this.pos.zCoord += vecZ * speed;
		
		if (manager.getWorld().isRemote) {
			tickClient();
		}
		
		
		
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient() {
		
		if (WeatherUtil.isPaused()) return;
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if (particleBehavior == null) {
			particleBehavior = new ParticleBehaviorSandstorm(pos);
		}
		
		
		//double size = 15;
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
    	
    	this.height = this.size / 2;
    	int heightLayers = Math.max(1, this.height / (int) distBetweenParticles);
    	
    	if ((mc.theWorld.getTotalWorldTime()) % 10 == 0) {
    		//System.out.println(heightLayers);
    	}
    	
    	for (int heightLayer = 0; heightLayer < heightLayers; heightLayer++) {
		    for (double i = 0; i < 360; i += degRate) {
		    	if ((mc.theWorld.getTotalWorldTime()) % 40 == 0) {
		    		
		    		//double sizeSub = 0.1D * size;
		    		
		    		double sizeRand = (size + /*rand.nextDouble() * 30D*/ - rand.nextDouble() * size/*30D*/)/* / (double)heightLayer*/;
		    		double x = pos.xCoord + (Math.sin(Math.toRadians(i)) * (sizeRand));
		    		double z = pos.zCoord + (Math.cos(Math.toRadians(i)) * (sizeRand));
		    		double y = pos.yCoord + (heightLayer * distBetweenParticles);
		    		
		    		TextureAtlasSprite sprite = ParticleRegistry.cloud256;
		    		
		    		ParticleSandstorm part = new ParticleSandstorm(mc.theWorld, x, y, z
		    				, 0, 0, 0, sprite);
		    		particleBehavior.initParticle(part);
		    		
		    		part.angleToStorm = i;
		    		part.distAdj = sizeRand;
		    		part.heightLayer = heightLayer;
		    		
		    		part.setFacePlayer(false);
		    		part.isTransparent = true;
		    		part.rotationYaw = (float) -i + rand.nextInt(20) - 10;//Math.toDegrees(Math.cos(Math.toRadians(i)) * 2D);
		    		part.rotationPitch = 0;
		    		part.setMaxAge(300);
		    		part.setGravity(0.09F);
		    		part.setAlphaF(1F);
		    		float brightnessMulti = 1F - (rand.nextFloat() * 0.3F);
		    		part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
		    		part.setScale(100);
		    		particleBehavior.particles.add(part);
		    		part.spawnAsWeatherEffect();
		    		
		    		//only need for non managed particles
		    		//ClientTickHandler.weatherManager.addWeatheredParticle(part);
		    		
		    		//mc.effectRenderer.addEffect(part);
		    		
		    		
		    	}
	    	}
    	}

	    float angle = manager.getWindManager().getWindAngleForClouds();
		
		double vecX = -Math.sin(Math.toRadians(angle));
		double vecZ = Math.cos(Math.toRadians(angle));
		//double speed = 0.2D;
		
		
	    
		particleBehavior.coordSource = pos;
	    particleBehavior.tickUpdateList();
	    
	    //System.out.println("client side size: " + size);
	    
	    //weather specific updates
	    for (int i = 0; i < particleBehavior.particles.size(); i++) {
	    	ParticleSandstorm particle = (ParticleSandstorm) particleBehavior.particles.get(i);
	    	
	    	//particle.setMotionX(particle.getMotionX() + (vecX * speed));
	    	//particle.setMotionZ(particle.getMotionZ() + (vecZ * speed));
	    	
	    	double x = pos.xCoord + (Math.sin(Math.toRadians(particle.angleToStorm)) * (particle.distAdj));
    		double z = pos.zCoord + (Math.cos(Math.toRadians(particle.angleToStorm)) * (particle.distAdj));
    		double y = pos.yCoord + (particle.heightLayer * distBetweenParticles);
    		
    		moveToPosition(particle, x, y, z, 0.01D);
	    	//particle.setPosition(x, y, z);
	    }
	    
	    //System.out.println("spawn particles at: " + pos);
	}
	
	public void moveToPosition(ParticleSandstorm particle, double x, double y, double z, double maxSpeed) {
		if (particle.getPosX() > x) {
			particle.setMotionX(particle.getMotionX() + -maxSpeed);
		} else {
			particle.setMotionX(particle.getMotionX() + maxSpeed);
		}
		
		/*if (particle.getPosY() > y) {
			particle.setMotionY(particle.getMotionY() + -maxSpeed);
		} else {
			particle.setMotionY(particle.getMotionY() + maxSpeed);
		}*/
		
		if (particle.getPosZ() > z) {
			particle.setMotionZ(particle.getMotionZ() + -maxSpeed);
		} else {
			particle.setMotionZ(particle.getMotionZ() + maxSpeed);
		}
		
		
		double distXZ = Math.sqrt((particle.getPosX() - x) * 2 + (particle.getPosZ() - z) * 2);
		if (distXZ < 5D) {
			particle.setMotionX(particle.getMotionX() * 0.8D);
			particle.setMotionZ(particle.getMotionZ() * 0.8D);
		}
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
