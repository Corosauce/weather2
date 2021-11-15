package extendedrenderer.particle.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleTexFX;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleBehaviors {

	public List<EntityRotFX> particles = new ArrayList<EntityRotFX>();
	public Vector3d coordSource;
	public Entity sourceEntity = null;
	public Random rand = new Random();
	
	//Visual tweaks
	public float rateDarken = 0.025F;
	public float rateBrighten = 0.010F;
	public float rateBrightenSlower = 0.003F;
	public float rateAlpha = 0.002F;
	public float rateScale = 0.1F;
	public int tickSmokifyTrigger = 40;
	
	public ParticleBehaviors(Vector3d source) {
		coordSource = source;
	}
	
	public void tickUpdateList() { //shouldnt be used, particles tick their own method, who removes it though?
		for (int i = 0; i < particles.size(); i++) {
			EntityRotFX particle = particles.get(i);
			
			if (!particle.isAlive()) {
				particles.remove(particle);
			} else {
				tickUpdate(particle);
			}
		}
	}
	
	public void tickUpdate(EntityRotFX particle) {
		
		if (sourceEntity != null) {
			coordSource = sourceEntity.getPositionVec();
		}
		
		tickUpdateAct(particle);
	}
	
	//default is smoke effect, override for custom
	public void tickUpdateAct(EntityRotFX particle) {
		
			
		double centerX = particle.getPosX();
		//double centerY = particle.posY;
		double centerZ = particle.getPosZ();
		
		if (coordSource != null) {
			centerX = coordSource.x/* + 0.5D*/;
			//centerY = coordSource.yCoord/* + 0.5D*/;
			centerZ = coordSource.z/* + 0.5D*/;
		}
		
		double vecX = centerX - particle.getPosX();
		double vecZ = centerZ - particle.getPosZ();
		double distToCenter = Math.sqrt(vecX * vecX + vecZ * vecZ);
		double rotYaw = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
		double adjYaw = Math.min(360, 45+particle.getAge());
		
		rotYaw -= adjYaw;
		//rotYaw -= 90D;
		//rotYaw += 20D;
		double speed = 0.1D;
		if (particle.getAge() < 25 && distToCenter > 0.05D) {
			particle.setMotionX(Math.cos(rotYaw * 0.017453D) * speed);
			particle.setMotionZ(Math.sin(rotYaw * 0.017453D) * speed);
		} else {
			double speed2 = 0.008D;
			
			double pSpeed = Math.sqrt(particle.getMotionX() * particle.getMotionX() + particle.getMotionZ() * particle.getMotionZ());
			
			//cheap air search code
			if (pSpeed < 0.2 && particle.getMotionY() < 0.01) {
				speed2 = 0.08D;
			}
			
			if (pSpeed < 0.002 && Math.abs(particle.getMotionY()) < 0.02) {
				particle.setMotionY(particle.getMotionY() - 0.15D);
			}
			
			particle.setMotionX(particle.getMotionX() + (rand.nextDouble() - rand.nextDouble()) * speed2);
			particle.setMotionZ(particle.getMotionZ() + (rand.nextDouble() - rand.nextDouble()) * speed2);
			
		}
		
		float brightnessShiftRate = rateDarken;
		
		int stateChangeTick = tickSmokifyTrigger;
		
		if (particle.getAge() < stateChangeTick) {
			particle.setGravity(-0.2F);
			particle.setColor(particle.particleRed - brightnessShiftRate, particle.particleGreen - brightnessShiftRate, particle.particleBlue - brightnessShiftRate);
		} else if (particle.getAge() == stateChangeTick) {
			particle.setColor(0,0,0);
		} else {
			brightnessShiftRate = rateBrighten;
			particle.setGravity(-0.05F);
			//particle.motionY *= 0.99F;
			if (particle.particleRed < 0.3F) {
				
			} else {
				brightnessShiftRate = rateBrightenSlower;
			}
			
			particle.setColor(particle.particleRed + brightnessShiftRate, particle.particleGreen + brightnessShiftRate, particle.particleBlue + brightnessShiftRate);
			
			if (particle.getAlphaF() > 0) {
				particle.setAlphaF(particle.getAlphaF() - rateAlpha);
			} else {
				particle.setExpired();
			}
		}
		
		if (particle.getScale() < 8F) particle.setScale(particle.getScale() + rateScale);
		
		/*if (particle.getAge() % cycle < cycle/2) {
			particle.setGravity(-0.02F);
		} else {*/
			
		//}
			
		
	}
	
	public void tickUpdateCloud(EntityRotFX particle) {
		particle.rotationYaw -= 0.1;
		
		int ticksFadeInMax = 100;
		
		if (particle.getAge() < ticksFadeInMax) {
			//System.out.println("particle.getAge(): " + particle.getAge());
			particle.setAlphaF(particle.getAge() * 0.01F);
		} else {
			if (particle.getAlphaF() > 0) {
				particle.setAlphaF(particle.getAlphaF() - rateAlpha*1.3F);
			} else {
				particle.setExpired();
			}
		}
	}
	
	/*public EntityRotFX spawnNewParticleIconFX(World world, TextureAtlasSprite icon, double x, double y, double z, double vecX, double vecY, double vecZ) {
		return spawnNewParticleIconFX(world, icon, x, y, z, vecX, vecY, vecZ, 0);
	}
	
	public EntityRotFX spawnNewParticleIconFX(World world, TextureAtlasSprite icon, double x, double y, double z, double vecX, double vecY, double vecZ, int renderOrder) {
		EntityRotFX entityfx = new ParticleTexFX(world, x, y, z, vecX, vecY, vecZ, icon);
		entityfx.pb = this;
		entityfx.renderOrder = renderOrder;
		return entityfx;
	}*/
	
	public EntityRotFX initParticle(EntityRotFX particle) {
		
		particle.setPrevPosX(particle.getPosX());
		particle.setPrevPosY(particle.getPosY());
		particle.setPrevPosZ(particle.getPosZ());
		/*particle.prevPosX = particle.getPosX();
		particle.prevPosY = particle.getPosY();
		particle.prevPosZ = particle.getPosZ();*/
		
		//keep AABB small, very important to performance
		particle.setSize(0.01F, 0.01F);
		
		return particle;
	}
	
	public static EntityRotFX setParticleRandoms(EntityRotFX particle, boolean yaw, boolean pitch) {
		Random rand = new Random();
		if (yaw) particle.rotationYaw = rand.nextInt(360);
		if (pitch) particle.rotationPitch = rand.nextInt(360);
		return particle;
	}
	
	public static EntityRotFX setParticleFire(EntityRotFX particle) {
		Random rand = new Random();
		particle.setColor(0.6F + (rand.nextFloat() * 0.4F), 0.2F + (rand.nextFloat() * 0.2F), 0);
		particle.setScale(0.25F + 0.2F * rand.nextFloat());
		particle.brightness = 1F;
		particle.setSize(0.1F, 0.1F);
		particle.setAlphaF(0.6F);
		return particle;
	}
	
	public static EntityRotFX setParticleCloud(EntityRotFX particle, float freezeY) {
		particle.spawnY = freezeY;
		particle.rotationPitch = 90F;
		//particle.renderDistanceWeight = 999D;
		//1.10.2 no known replacement for above
        //particle.noClip = true;
		particle.setCanCollide(false);
        particle.setSize(0.25F, 0.25F);
        particle.setScale(500F);
        //particle.particleScale = 200F;
        particle.callUpdateSuper = false;
        particle.callUpdatePB = false;
        particle.setMaxAge(500);
        particle.setColor(1F, 1F, 1F);
        particle.brightness = 0.3F;//- ((200F - particle.spawnY) * 0.05F);
        particle.renderRange = 999F;
        particle.setAlphaF(0F);
		return particle;
	}
	
	public void cleanup() {
		
	}
	
}
