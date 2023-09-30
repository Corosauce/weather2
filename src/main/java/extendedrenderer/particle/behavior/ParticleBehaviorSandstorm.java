package extendedrenderer.particle.behavior;

import com.corosus.coroutil.util.CoroUtilBlock;
import extendedrenderer.particle.entity.EntityRotFX;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import weather2.ClientTickHandler;
import weather2.ClientWeatherProxy;
import weather2.datatypes.PrecipitationType;

public class ParticleBehaviorSandstorm extends ParticleBehaviors {

	//Externally updated variables, adjusting how templated behavior works
	public int curTick = 0;
	public int ticksMax = 1;
	
	public ParticleBehaviorSandstorm(Vec3 source) {
		super(source);
	}
	
	public EntityRotFX initParticle(EntityRotFX particle) {
		super.initParticle(particle);
		
		//particle.particleGravity = 0.5F;
		//fog
		particle.rotationYaw = rand.nextInt(360);
		particle.rotationPitch = rand.nextInt(50)-rand.nextInt(50);
		
		//cloud
		//particle.rotationYaw = rand.nextInt(360);
		//particle.rotationPitch = -90+rand.nextInt(50)-rand.nextInt(50);
		
		particle.setLifetime(450+rand.nextInt(10));
		float randFloat = (rand.nextFloat() * 0.6F);
		float baseBright = 0.7F;
		float finalBright = Math.min(1F, baseBright+randFloat);
		particle.setColor(finalBright, finalBright, finalBright);
		//particle.setColor(72F/255F, 239F/255F, 8F/255F);
		
		//sand
		//particle.setColor(204F/255F, 198F/255F, 120F/255F);
		//red
		//particle.setColor(0.6F + (rand.nextFloat() * 0.4F), 0.2F + (rand.nextFloat() * 0.7F), 0);
		//green
		//particle.setColor(0, 0.4F + (rand.nextFloat() * 0.4F), 0);
		//tealy blue
		//particle.setColor(0, 0.4F + (rand.nextFloat() * 0.4F), 0.4F + (rand.nextFloat() * 0.4F));
		//particle.setColor(0.4F + (rand.nextFloat() * 0.4F), 0.4F + (rand.nextFloat() * 0.4F), 0.4F + (rand.nextFloat() * 0.4F));
		
		//location based color shift
		//particle.setColor((float) (0.4F + (Math.abs(particle.posX / 300D) * 0.6D)), 0.4F, (float) (0.4F + (Math.abs(particle.posZ / 300D) * 0.6D)));
		//particle.setScale(0.25F + 0.2F * rand.nextFloat());
		particle.brightness = 1F;
		particle.setAlpha(1F);
		
		float sizeBase = (float) (30+(rand.nextDouble()*4));
		
		particle.setScale(sizeBase);
		//particle.spawnY = (float) particle.posY;
		//particle.noClip = false;
		particle.setCanCollide(true);
		//entityfx.spawnAsWeatherEffect();
		
		particle.renderRange = 2048;
		
		particle.setFacePlayer(true);
		particle.setGravity(0.03F);
		
		return particle;
	}

	@Override
	public void tickUpdateAct(EntityRotFX particle) {
		//particle.particleScale = 900;
		//particle.rotationPitch = 30;
		//for (int i = 0; i < particles.size(); i++) {
			//EntityRotFX particle = particles.get(i);
			
			if (!particle.isAlive()) {
				particles.remove(particle);
			} else {
				//random rotation yaw adjustment
				if (particle.getEntityId() % 2 == 0) {
					particle.rotationYaw -= 0.1;
				} else {
					particle.rotationYaw += 0.1;
				}
				
				float ticksFadeInMax = 10;
				float ticksFadeOutMax = 10;
				
				//fade in and fade out near age edges
				if (particle.getAge() < ticksFadeInMax) {
					//System.out.println("particle.getAge(): " + particle.getAge());
					particle.setAlpha(Math.min(1F, particle.getAge() / ticksFadeInMax));
					//System.out.println(particle.getAge() / ticksFadeInMax);
					//particle.setAlphaF(1);
				} else if (particle.getAge() > particle.getLifetime() - ticksFadeOutMax) {
					float count = particle.getAge() - (particle.getLifetime() - ticksFadeOutMax);
					float val = (ticksFadeOutMax - (count)) / ticksFadeOutMax;
					//System.out.println(val);
					particle.setAlpha(val);
				} else {
					/*if (particle.getAlphaF() > 0) {
						particle.setAlphaF(particle.getAlphaF() - rateAlpha*1.3F);
					} else {
						particle.remove();
					}*/
					//particle.setAlphaF(1F);
				}
				
				//TEMP
				//particle.setAlphaF(1F);
				
				
				double moveSpeed = 0.001D;
				//1.10.2 no
				/*if (particle.onGround) {
					moveSpeed = 0.012D;
					particle.setMotionY(particle.getMotionY() + 0.01D);
				}*/
				
				//get pos a bit under particle
				BlockPos pos = CoroUtilBlock.blockPos(particle.getPosX(), particle.getPosY() - particle.aboveGroundHeight, particle.getPosZ());
				BlockState state = particle.getWorld().getBlockState(pos);
				//if particle is near ground, push it up to keep from landing
				if (!state.isAir()) {
					if (particle.getMotionY() < particle.bounceSpeedMax) {
						particle.setMotionY(particle.getMotionY() + particle.bounceSpeed);
					}
				//check ahead for better flowing over large cliffs
				} else {
					double aheadMultiplier = 20D;
					BlockPos posAhead = CoroUtilBlock.blockPos((particle.getPosX() + (particle.getMotionX() * aheadMultiplier)), particle.getPosY() - particle.aboveGroundHeight, particle.getPosZ() + (particle.getMotionZ() * aheadMultiplier));
					BlockState stateAhead = particle.getWorld().getBlockState(posAhead);
					if (!stateAhead.isAir()) {
						if (particle.getMotionY() < particle.bounceSpeedMaxAhead) {
							particle.setMotionY(particle.getMotionY() +  particle.bounceSpeedAhead);
						}
					}
				}
				
				//if (particle.isCollidedHorizontally) {
				/*if (particle.isCollided()) {
					particle.rotationYaw += 0.1;
				}*/
				
				
				/*particle.setMotionX(particle.getMotionX() - Math.sin(Math.toRadians((particle.rotationYaw + particle.getEntityId()) % 360)) * moveSpeed);
				particle.setMotionZ(particle.getMotionZ() + Math.cos(Math.toRadians((particle.rotationYaw + particle.getEntityId()) % 360)) * moveSpeed);*/
				
				double moveSpeedRand = 0.005D;
				
				particle.setMotionX(particle.getMotionX() + (rand.nextDouble() * moveSpeedRand - rand.nextDouble() * moveSpeedRand));
				particle.setMotionZ(particle.getMotionZ() + (rand.nextDouble() * moveSpeedRand - rand.nextDouble() * moveSpeedRand));
				
				//TEMPOFF?
				//particle.setScale(particle.getScale() - 0.1F);
				
				if (particle.spawnY != -1) {
					particle.setPos(particle.getPosX(), particle.spawnY, particle.getPosZ());
					//particle.posY = particle.spawnY;
				}
				
				//particle.remove();
			}
		//}
	}
}
