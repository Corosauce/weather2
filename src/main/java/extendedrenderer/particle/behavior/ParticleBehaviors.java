package extendedrenderer.particle.behavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.corosus.coroutil.util.CoroUtilBlock;
import com.corosus.coroutil.util.CoroUtilMisc;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleTexExtraRender;
import extendedrenderer.particle.entity.ParticleTexFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.ClientWeatherProxy;
import weather2.client.SceneEnhancer;
import weather2.datatypes.PrecipitationType;
import weather2.util.WeatherUtilParticle;

@OnlyIn(Dist.CLIENT)
public class ParticleBehaviors {

	public List<EntityRotFX> particles = new ArrayList<EntityRotFX>();
	public Vec3 coordSource;
	public Entity sourceEntity = null;
	public Random rand = new Random();
	
	//Visual tweaks
	public float rateDarken = 0.025F;
	public float rateBrighten = 0.010F;
	public float rateBrightenSlower = 0.003F;
	public float rateAlpha = 0.002F;
	public float rateScale = 0.1F;
	public int tickSmokifyTrigger = 40;

	float acidRainRed = 0.5F;
	float acidRainGreen = 1F;
	float acidRainBlue = 0.5F;

	float vanillaRainRed = 0.7F;
	float vanillaRainGreen = 0.7F;
	float vanillaRainBlue = 1F;
	
	public ParticleBehaviors(Vec3 source) {
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
			coordSource = sourceEntity.position();
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
			particle.setColor(particle.rCol - brightnessShiftRate, particle.gCol - brightnessShiftRate, particle.bCol - brightnessShiftRate);
		} else if (particle.getAge() == stateChangeTick) {
			particle.setColor(0,0,0);
		} else {
			brightnessShiftRate = rateBrighten;
			particle.setGravity(-0.05F);
			//particle.motionY *= 0.99F;
			if (particle.rCol < 0.3F) {
				
			} else {
				brightnessShiftRate = rateBrightenSlower;
			}
			
			particle.setColor(particle.rCol + brightnessShiftRate, particle.gCol + brightnessShiftRate, particle.bCol + brightnessShiftRate);
			
			if (particle.getAlphaF() > 0) {
				particle.setAlpha(particle.getAlphaF() - rateAlpha);
			} else {
				particle.remove();
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
			particle.setAlpha(particle.getAge() * 0.01F);
		} else {
			if (particle.getAlphaF() > 0) {
				particle.setAlpha(particle.getAlphaF() - rateAlpha*1.3F);
			} else {
				particle.remove();
			}
		}
	}
	
	public EntityRotFX spawnNewParticleIconFX(Level world, TextureAtlasSprite icon, double x, double y, double z, double vecX, double vecY, double vecZ) {
		return spawnNewParticleIconFX(world, icon, x, y, z, vecX, vecY, vecZ, 0);
	}
	
	public EntityRotFX spawnNewParticleIconFX(Level world, TextureAtlasSprite icon, double x, double y, double z, double vecX, double vecY, double vecZ, int renderOrder) {
		EntityRotFX entityfx = new ParticleTexFX((ClientLevel) world, x, y, z, vecX, vecY, vecZ, icon);
		entityfx.pb = this;
		entityfx.renderOrder = renderOrder;
		return entityfx;
	}
	
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

	public void initParticleRain(EntityRotFX particle, int extraRenderCount) {
		particle.setKillWhenUnderTopmostBlock(true);
		particle.setCanCollide(false);
		particle.killWhenUnderCameraAtLeast = 5;
		particle.setDontRenderUnderTopmostBlock(true);
		if (particle instanceof ParticleTexExtraRender) {
			((ParticleTexExtraRender)particle).setExtraParticlesBaseAmount(extraRenderCount);
		}
		particle.fastLight = true;
		particle.setSlantParticleToWind(true);
		particle.windWeight = 5F;
		particle.setFacePlayer(false);
		particle.setScale(2F * 0.15F);
		particle.isTransparent = true;
		particle.setGravity(1.8F);
		particle.setLifetime(50);
		particle.setTicksFadeInMax(5);
		particle.setTicksFadeOutMax(5);
		particle.setTicksFadeOutMaxOnDeath(3);
		particle.setFullAlphaTarget(0.6F);
		particle.setAlpha(0);
		particle.rotationYaw = CoroUtilMisc.random.nextInt(360) - 180F;
		particle.setMotionY(-0.5D);
		ClientTickHandler.getClientWeather().getWindManager().applyWindForceNew(particle, 10F, 0.5F);
		Player entP = Minecraft.getInstance().player;
		Biome biome = entP.level().getBiome(new BlockPos(Mth.floor(entP.getX()), (int)Math.floor(entP.getY()), Mth.floor(entP.getZ()))).value();
		if (ClientWeatherProxy.get().getPrecipitationType(biome) == PrecipitationType.ACID) {
			particle.rCol = acidRainRed;
			particle.gCol = acidRainGreen;
			particle.bCol = acidRainBlue;
		} else {
			particle.setFullAlphaTarget(0.8F);
			particle.rCol = vanillaRainRed;
			particle.gCol = vanillaRainGreen;
			particle.bCol = vanillaRainBlue;
		}
		particle.spawnAsWeatherEffect();
	}

	public void initParticleGroundSplash(EntityRotFX particle) {
		particle.setKillWhenUnderTopmostBlock(true);
		particle.setCanCollide(false);
		particle.killWhenUnderCameraAtLeast = 5;
		boolean upward = rand.nextBoolean();
		particle.windWeight = 20F;
		particle.setFacePlayer(upward);
		particle.setScale(0.2F + (rand.nextFloat() * 0.05F));
		particle.setLifetime(15);
		particle.setGravity(-0.0F);
		particle.setTicksFadeInMax(3);
		particle.setFullAlphaTarget(0.6F);
		particle.setAlpha(0);
		particle.setTicksFadeOutMax(4);
		particle.renderOrder = 2;
		particle.rotationYaw = CoroUtilMisc.random.nextInt(360) - 180F;
		particle.rotationPitch = 90;
		particle.setMotionY(0D);
		particle.setMotionX((rand.nextFloat() - 0.5F) * 0.01F);
		particle.setMotionZ((rand.nextFloat() - 0.5F) * 0.01F);
		//ClientTickHandler.getClientWeather().getWindManager().applyWindForceNew(particle, 1F / 5F, 0.5F);
		Player entP = Minecraft.getInstance().player;
		Biome biome = entP.level().getBiome(new BlockPos(Mth.floor(entP.getX()), Mth.floor(entP.getY()), Mth.floor(entP.getZ()))).value();
		if (ClientWeatherProxy.get().getPrecipitationType(biome) == PrecipitationType.ACID) {
			particle.rCol = acidRainRed;
			particle.gCol = acidRainGreen;
			particle.bCol = acidRainBlue;
		} else {
			particle.rCol = vanillaRainRed;
			particle.gCol = vanillaRainGreen;
			particle.bCol = vanillaRainBlue;
		}
	}

	public void initParticleRainDownfall(EntityRotFX particle) {
		particle.setCanCollide(false);
		particle.killWhenUnderCameraAtLeast = 15;
		particle.setKillWhenUnderTopmostBlock(true);
		particle.setKillWhenUnderTopmostBlock_ScanAheadRange(3);
		particle.setTicksFadeOutMaxOnDeath(10);
		particle.setDontRenderUnderTopmostBlock(false);
		particle.windWeight = 5F;
		particle.setFacePlayer(false);
		particle.facePlayerYaw = true;
		particle.setScale(12F + (rand.nextFloat() * 0.3F));
		particle.setSize(10, 50);
		particle.setLifetime(120);
		particle.setGravity(0.35F);
		particle.setTicksFadeInMax(20);
		particle.setFullAlphaTarget(1F);
		particle.setAlpha(0);
		particle.setTicksFadeOutMax(10);
		particle.rotationYaw = CoroUtilMisc.random.nextInt(360) - 180F;
		particle.rotationPitch = 0;
		particle.setMotionY(-0.3D);
		particle.setMotionX((rand.nextFloat() - 0.5F) * 0.01F);
		particle.setMotionZ((rand.nextFloat() - 0.5F) * 0.01F);
		Player entP = Minecraft.getInstance().player;
		Biome biome = entP.level().getBiome(CoroUtilBlock.blockPos(entP.getX(), entP.getY(), entP.getZ())).get();
		if (ClientWeatherProxy.get().getPrecipitationType(biome) == PrecipitationType.ACID) {
			particle.rCol = acidRainRed;
			particle.gCol = acidRainGreen;
			particle.bCol = acidRainBlue;
		} else {
			particle.rCol = vanillaRainRed;
			particle.gCol = vanillaRainGreen;
			particle.bCol = vanillaRainBlue;
		}
	}

	public void initParticleSnow(EntityRotFX particle, int extraRenderCount, float windSpeed) {
		float windScale = Math.max(0.1F, 1F - windSpeed);
		particle.setCanCollide(false);
		//particle.setKillWhenUnderTopmostBlock(true);
		particle.setTicksFadeOutMaxOnDeath(5);
		particle.setDontRenderUnderTopmostBlock(true);
		particle.setKillWhenUnderTopmostBlock(true);
		if (particle instanceof ParticleTexExtraRender) {
			((ParticleTexExtraRender)particle).setExtraParticlesBaseAmount(extraRenderCount);
		}
		particle.killWhenFarFromCameraAtLeast = 25;
		particle.setMotionX(0);
		particle.setMotionZ(0);
		particle.setMotionY(0);
		particle.setScale(1.3F * 0.15F);
		particle.setGravity(0.05F);
		particle.windWeight = 5F;
		particle.setMaxAge((int) (120F * 12F * windScale));
		particle.setFacePlayer(true);
		particle.setTicksFadeInMax(40 * windScale);
		particle.setAlphaF(0);
		particle.setTicksFadeOutMax(40 * windScale);
		particle.setTicksFadeOutMaxOnDeath(10);
		//particle.setTicksFadeOutMax(5);
		particle.rotationYaw = CoroUtilMisc.random.nextInt(360) - 180F;
		ClientTickHandler.getClientWeather().getWindManager().applyWindForceNew(particle, 1F, 0.5F);
	}

	public void initParticleSnowstorm(EntityRotFX particle, int extraRenderCount) {
		particle.setCanCollide(false);
		//particle.setKillWhenUnderTopmostBlock(true);
		particle.setTicksFadeOutMaxOnDeath(5);
		particle.setDontRenderUnderTopmostBlock(true);
		if (particle instanceof ParticleTexExtraRender) {
			((ParticleTexExtraRender)particle).setExtraParticlesBaseAmount(extraRenderCount);
		}
		particle.killWhenFarFromCameraAtLeast = 15;
		particle.setMotionX(0);
		particle.setMotionZ(0);
		particle.setMotionY(0D);
		particle.setScale(1.3F * 0.15F);
		particle.setGravity(0.05F);
		particle.windWeight = 5F;
		particle.setMaxAge(120);
		particle.setFacePlayer(false);
		particle.setTicksFadeInMax(5);
		particle.setAlphaF(0);
		particle.setTicksFadeOutMax(20);
		particle.rotationYaw = CoroUtilMisc.random.nextInt(360) - 180F;
		ClientTickHandler.getClientWeather().getWindManager().applyWindForceNew(particle, 1F, 0.5F);
	}

	public void initParticleHail(EntityRotFX particle) {
		particle.setKillWhenUnderTopmostBlock(false);
		particle.setCanCollide(true);
		particle.setKillOnCollide(true);
		particle.killWhenUnderCameraAtLeast = 5;
		particle.setDontRenderUnderTopmostBlock(true);
		particle.rotationYaw = rand.nextInt(360);
		particle.rotationPitch = rand.nextInt(360);
		particle.fastLight = true;
		particle.setSlantParticleToWind(true);
		particle.windWeight = 5F;
		particle.spinFast = true;
		particle.spinFastRate = 10F;
		particle.setFacePlayer(false);
		particle.setScale(0.7F * 0.15F);
		//particle.setScale(2F * 0.15F);
		particle.isTransparent = true;
		particle.setGravity(3.5F);
		particle.setLifetime(70);
		particle.setTicksFadeInMax(5);
		particle.setTicksFadeOutMax(5);
		particle.setTicksFadeOutMaxOnDeath(50);
		particle.setFullAlphaTarget(1F);
		particle.setAlpha(0);
		particle.rotationYaw = CoroUtilMisc.random.nextInt(360) - 180F;
		particle.setMotionY(-0.5D);
		ClientTickHandler.getClientWeather().getWindManager().applyWindForceNew(particle, 1F, 0.5F);
		particle.rCol = 0.9F;
		particle.gCol = 0.9F;
		particle.bCol = 0.9F;
		particle.bounceOnVerticalImpact = true;
		particle.bounceOnVerticalImpactEnergy = 0.2F;
	}

	public void initParticleCube(EntityRotFX particle) {
		particle.setKillWhenUnderTopmostBlock(false);
		particle.setCanCollide(true);
		particle.setKillOnCollide(true);
		particle.setKillOnCollideActivateAtAge(30);
		particle.killWhenUnderCameraAtLeast = 0;
		particle.setDontRenderUnderTopmostBlock(true);
		particle.rotationYaw = rand.nextInt(360);
		particle.rotationPitch = rand.nextInt(360);
		particle.fastLight = true;
		particle.windWeight = 5 + ((float)((Math.random() * 0.3) - (Math.random() * 0.3)));
		particle.spinFast = true;
		particle.spinFastRate = 1F;
		particle.setFacePlayer(false);
		particle.setScale(3F * 0.15F);
		particle.isTransparent = false;
		particle.setGravity(4F);
		particle.setLifetime(20*20);
		particle.setTicksFadeInMax(5);
		particle.setTicksFadeOutMax(5);
		particle.setTicksFadeOutMaxOnDeath(20);
		particle.setFullAlphaTarget(1F);
		particle.setAlpha(0);
		particle.rotationYaw = CoroUtilMisc.random.nextInt(360) - 180F;
		//particle.setMotionY(-0.5D);
		//ClientTickHandler.getClientWeather().getWindManager().applyWindForceNew(particle, 1F, 0.5F);
		/*float tempBrightness = 0.5F;
		particle.rCol = 0.5F * tempBrightness;
		particle.gCol = 0.9F * tempBrightness;
		particle.bCol = 0.5F * tempBrightness;*/
		particle.setVanillaMotionDampen(true);
		particle.bounceOnVerticalImpact = true;
		particle.bounceOnVerticalImpactEnergy = 0.2F;
	}

	public void initParticleDustAir(EntityRotFX particle) {
		particle.setKillWhenUnderTopmostBlock(false);
		particle.setCanCollide(false);
		particle.killWhenUnderCameraAtLeast = 5;
		particle.setTicksFadeOutMaxOnDeath(5);
		particle.setDontRenderUnderTopmostBlock(true);
		if (particle instanceof ParticleTexExtraRender) {
			((ParticleTexExtraRender)particle).setExtraParticlesBaseAmount(0);
		}
		particle.setMotionX(0);
		particle.setMotionZ(0);
		particle.setMotionY(0);
		particle.fastLight = true;
		particle.windWeight = 10F;
		particle.setFacePlayer(true);
		particle.setScale(0.1F * 0.15F);
		particle.isTransparent = true;
		particle.setGravity(0F);
		particle.setLifetime(80);
		particle.setTicksFadeInMax(20);
		particle.setTicksFadeOutMax(20);
		particle.setTicksFadeOutMaxOnDeath(20);
		particle.setFullAlphaTarget(0.6F);
		particle.setAlpha(0);
		float brightness = 0.5F + (rand.nextFloat() * 0.5F);
		particle.setColor(particle.rCol * brightness, particle.gCol * brightness, particle.bCol * brightness);
		particle.rotationYaw = CoroUtilMisc.random.nextInt(360) - 180F;
		//ClientTickHandler.getClientWeather().getWindManager().applyWindForceNew(particle, 10F, 0.5F);
	}

	public void initParticleDustGround(EntityRotFX particle, boolean spawnInside, boolean spawnAboveSnow) {
		particle.setKillOnCollide(false);
		particle.setKillWhenUnderTopmostBlock(false);
		particle.killWhenUnderCameraAtLeast = 5;
		particle.setDontRenderUnderTopmostBlock(false);
		particle.setMotionX(0);
		particle.setMotionZ(0);
		particle.setMotionY(0);
		particle.fastLight = true;
		particle.windWeight = 1F;
		particle.setFacePlayer(true);
		particle.setScale(0.15F * 0.15F);
		particle.isTransparent = true;
		particle.setGravity(0.06F);
		particle.setCanCollide(false);
		particle.setCanCollide(true);
		particle.collisionSpeedDampen = false;
		/*if (spawnInside) {
			particle.setGravity(0.05F);
			particle.setCanCollide(true);
		}*/
		particle.setLifetime(30);
		particle.setTicksFadeInMax(5);
		particle.setTicksFadeOutMax(5);
		particle.setTicksFadeOutMaxOnDeath(5);
		particle.setFullAlphaTarget(0.6F);
		particle.setAlpha(0);
		if (spawnAboveSnow || !spawnInside) {
			float brightness = 0.5F;
			particle.setColor(particle.rCol * brightness, particle.gCol * brightness, particle.bCol * brightness);
		}
		particle.rotationYaw = CoroUtilMisc.random.nextInt(360) - 180F;
	}

	public void initParticleLeaf(EntityRotFX particle, float particleAABB) {
		Vec3 windForce = ClientTickHandler.getClientWeather().getWindManager().getWindForce(WeatherUtilParticle.getPos(particle));
		particle.setMotionX(windForce.x / 2);
		particle.setMotionZ(windForce.z / 2);
		particle.setMotionY(windForce.y / 2);
		particle.setSize(particleAABB, particleAABB);
		particle.setGravity(0.05F);
		particle.setCanCollide(true);
		particle.setKillOnCollide(false);
		particle.collisionSpeedDampen = false;
		particle.killWhenUnderCameraAtLeast = 20;
		particle.killWhenFarFromCameraAtLeast = 20;
		particle.isTransparent = false;
		particle.rotationYaw = rand.nextInt(360);
		particle.rotationPitch = rand.nextInt(360);
	}

	public void initParticleSnowstormCloudDust(EntityRotFX particle) {
		boolean farSpawn = Minecraft.getInstance().player.isSpectator() || !SceneEnhancer.isPlayerOutside;
		Vec3 windForce = ClientTickHandler.getClientWeather().getWindManager().getWindForce(null);
		particle.setMotionX(windForce.x * 0.3);
		particle.setMotionZ(windForce.z * 0.3);
		particle.setFacePlayer(false);
		particle.isTransparent = true;
		particle.rotationYaw = (float)rand.nextInt(360);
		particle.rotationPitch = (float)rand.nextInt(360);
		particle.setLifetime(farSpawn ? 30 : 10);
		particle.setLifetime(20);
		particle.setGravity(0.09F);
		particle.setAlpha(0F);
		float brightnessMulti = 1F - (rand.nextFloat() * 0.4F);
		particle.setColor(1F * brightnessMulti, 1F * brightnessMulti, 1F * brightnessMulti);
		particle.setScale(30 * 0.15F);
		particle.aboveGroundHeight = 0.2D;
		particle.setKillOnCollide(true);
		particle.killWhenFarFromCameraAtLeast = 15;
		particle.windWeight = 1F;
		particle.setTicksFadeInMax(5);
		particle.setTicksFadeOutMax(3);
		particle.setTicksFadeOutMaxOnDeath(3);
		ClientTickHandler.getClientWeather().getWindManager().applyWindForceNew(particle, 1F / 5F, 0.5F);
	}

	public void initParticleSandstormDust(EntityRotFX particle) {
		Vec3 windForce = ClientTickHandler.getClientWeather().getWindManager().getWindForce(null);
		particle.setMotionX(windForce.x);
		particle.setMotionZ(windForce.z);
		particle.setFacePlayer(false);
		particle.isTransparent = true;
		particle.rotationYaw = (float)rand.nextInt(360);
		particle.rotationPitch = (float)rand.nextInt(360);
		particle.setLifetime(40);
		particle.setGravity(0.09F);
		particle.setAlpha(0F);
		float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
		particle.setColor(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
		particle.setScale(40 * 0.15F);
		particle.aboveGroundHeight = 0.2D;
		particle.setKillOnCollide(true);
		particle.killWhenFarFromCameraAtLeast = 15;
		particle.setTicksFadeInMax(5);
		particle.setTicksFadeOutMax(5);
		particle.setTicksFadeOutMaxOnDeath(5);
		particle.windWeight = 1F;
	}

	public void initParticleSandstormTumbleweed(EntityRotFX particle) {
		Vec3 windForce = ClientTickHandler.getClientWeather().getWindManager().getWindForce(null);
		particle.setMotionX(windForce.x);
		particle.setMotionZ(windForce.z);
		particle.setFacePlayer(false);
		particle.facePlayerYaw = false;
		particle.spinTowardsMotionDirection = true;
		particle.isTransparent = true;
		particle.rotationYaw = (float)rand.nextInt(360);
		particle.rotationPitch = (float)rand.nextInt(360);
		particle.setLifetime(80);
		particle.setGravity(0.3F);
		particle.setAlpha(0F);
		float brightnessMulti = 1F - (rand.nextFloat() * 0.2F);
		particle.setColor(1F * brightnessMulti, 1F * brightnessMulti, 1F * brightnessMulti);
		particle.setScale(8 * 0.15F);
		particle.aboveGroundHeight = 0.5D;
		particle.collisionSpeedDampen = false;
		particle.bounceSpeed = 0.03D;
		particle.bounceSpeedAhead = 0.03D;
		particle.setKillOnCollide(false);
		particle.killWhenFarFromCameraAtLeast = 30;
		particle.setTicksFadeInMax(5);
		particle.setTicksFadeOutMax(5);
		particle.setTicksFadeOutMaxOnDeath(5);
		particle.windWeight = 1F;
	}

	public void initParticleSandstormDebris(EntityRotFX particle) {
		Vec3 windForce = ClientTickHandler.getClientWeather().getWindManager().getWindForce(null);
		particle.setMotionX(windForce.x);
		particle.setMotionZ(windForce.z);
		particle.setFacePlayer(false);
		particle.spinFast = true;
		particle.spinFastRate = 2F;
		particle.isTransparent = true;
		particle.rotationYaw = (float)rand.nextInt(360);
		particle.rotationPitch = (float)rand.nextInt(360);
		particle.setLifetime(80);
		particle.setGravity(0.3F);
		particle.setAlpha(0F);
		float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
		particle.setColor(1F * brightnessMulti, 1F * brightnessMulti, 1F * brightnessMulti);
		particle.setScale(8 * 0.15F);
		particle.aboveGroundHeight = 0.5D;
		particle.collisionSpeedDampen = false;
		particle.bounceSpeed = 0.03D;
		particle.bounceSpeedAhead = 0.03D;
		particle.setKillOnCollide(false);
		particle.killWhenFarFromCameraAtLeast = 30;
		particle.setTicksFadeInMax(5);
		particle.setTicksFadeOutMax(5);
		particle.setTicksFadeOutMaxOnDeath(5);
		particle.windWeight = 1F;
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
		particle.setAlpha(0.6F);
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
        particle.setLifetime(500);
        particle.setColor(1F, 1F, 1F);
        particle.brightness = 0.3F;//- ((200F - particle.spawnY) * 0.05F);
        particle.renderRange = 999F;
        particle.setAlpha(0F);
		return particle;
	}
	
	public void cleanup() {
		
	}
	
}
