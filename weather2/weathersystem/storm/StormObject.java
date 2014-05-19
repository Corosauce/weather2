package weather2.weathersystem.storm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import weather2.Weather;
import weather2.WeatherUtil;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityMovingBlock;
import weather2.weathersystem.WeatherManagerBase;
import CoroUtil.entity.EntityTropicalFishHook;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorFog;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.EntityTexFX;

public class StormObject {

	//used on both server and client side, mark things SideOnly where needed
	
	//size, state
	
	//should they extend entity?
	
	//management stuff
	public static long lastUsedStormID = 0; //ID starts from 0 for each game start, no storm nbt disk reload for now
	public long ID; //loosely accurate ID for tracking, but we wanted to persist between world reloads..... need proper UUID??? I guess add in UUID later and dont persist, start from 0 per game run
	public WeatherManagerBase manager;
	
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesCloud = new ArrayList<EntityRotFX>();
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesFunnel = new ArrayList<EntityRotFX>();
	@SideOnly(Side.CLIENT)
	public ParticleBehaviorFog particleBehaviorFog;
	
	public int sizeMaxFunnelParticles = 300;
	
	//public WeatherEntityConfig conf = WeatherTypes.weatherEntTypes.get(1);
	public int curWeatherType = 1; //NEEDS SYNCING
	
	//basic info
	public static int staticYPos = 200;
	public Vec3 pos = Vec3.createVectorHelper(0, staticYPos, 0);
	public Vec3 posGround = Vec3.createVectorHelper(0, 0, 0);
	public Vec3 motion = Vec3.createVectorHelper(0, 0, 0);
	
	//growth / progression info
	public int size = 0;
	public int maxSize = 500;
	public boolean isGrowing = true;
	
	//spin speed for potential tornado formations, should go up with intensity increase;
	public double spinSpeed = 0.02D;
	
	//states that combine all lesser states
	public int state = STATE_NORMAL;
	public static int STATE_NORMAL = 0; //no spin
	public static int STATE_RAIN = 1; //no spin
	public static int STATE_THUNDER = 2; //no spin
	public static int STATE_FORMINGSPINNING = 3; //does spin
	public static int STATE_HAIL = 4; //does spin
	
	//attributes that can happen independantly
	public boolean attrib_highwind = false;
	public boolean attrib_tornado = false;
	public boolean attrib_hurricane = false;
	//F1 - F5 severity states (changes size and damage radius of tornado)
	public int attrib_tornado_severity = 0;
	public static int ATTRIB_FORMING = 1; //the tornado version of forming, not cloud forming
	public static int ATTRIB_F1 = 2;
	public static int ATTRIB_F2 = 3;
	public static int ATTRIB_F3 = 4;
	public static int ATTRIB_F4 = 5;
	public static int ATTRIB_F5 = 6;
	
	//copied from EntTornado
	
	//old non multiplayer friendly var, needs resdesign where this is used
	public int playerInAirTime = 0;
	
	//buildup var
	public float strength = 100;
	
	//unused tornado scale, always 1F
	public float scale = 1F;
	
	public int currentTopYBlock = -1;
	
	public int maxHeight = 60;
	
	public TornadoHelper tornadoHelper = new TornadoHelper(this);
	
	public StormObject(WeatherManagerBase parManager) {
		manager = parManager;
	}
	
	//not used yet
	public void init() {
		
	}
	
	//not used yet
	public void readFromNBT(NBTTagCompound var1)
    {
		
    }
	
	//not used yet
	public void writeToNBT(NBTTagCompound var1)
    {
		
    }
	
	//receiver method
	public void nbtSyncFromServer(NBTTagCompound parNBT) {
		ID = parNBT.getLong("ID");
		Weather.dbg("StormObject " + ID + " receiving sync");
		
		pos = Vec3.createVectorHelper(parNBT.getInteger("posX"), parNBT.getInteger("posY"), parNBT.getInteger("posZ"));
		size = parNBT.getInteger("size");
		maxSize = parNBT.getInteger("maxSize");
		
		state = parNBT.getInteger("state");
		
		attrib_tornado_severity = parNBT.getInteger("attrib_tornado_severity");
		
		attrib_highwind = parNBT.getBoolean("attrib_highwind");
		attrib_tornado = parNBT.getBoolean("attrib_tornado");
		attrib_hurricane = parNBT.getBoolean("attrib_hurricane");
		
		currentTopYBlock = parNBT.getInteger("currentTopYBlock");
	}
	
	//compose nbt data for packet (and serialization in future)
	public NBTTagCompound nbtSyncForClient() {
		NBTTagCompound data = new NBTTagCompound();
		
		data.setInteger("posX", (int)pos.xCoord);
		data.setInteger("posY", (int)pos.yCoord);
		data.setInteger("posZ", (int)pos.zCoord);
		
		data.setLong("ID", ID);
		data.setInteger("size", size);
		data.setInteger("maxSize", maxSize);
		
		data.setInteger("state", state);
		
		data.setInteger("attrib_tornado_severity", attrib_tornado_severity);
		
		data.setBoolean("attrib_highwind", attrib_highwind);
		data.setBoolean("attrib_tornado", attrib_tornado);
		data.setBoolean("attrib_hurricane", attrib_hurricane);
		
		data.setInteger("currentTopYBlock", currentTopYBlock);
		
		return data;
	}
	
	public void tick() {
		//Weather.dbg("ticking storm " + ID + " - manager: " + manager);
		
		//adjust posGround to be pos with the ground Y pos for convinient usage
		posGround = Vec3.createVectorHelper(pos.xCoord, pos.yCoord, pos.zCoord);
		posGround.yCoord = currentTopYBlock;
		
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT) {
			if (!WeatherUtil.isPaused()) {
				tickClient();
			}
		} else {

			//debug \\
			maxSize = 300;
			size = maxSize;
			isGrowing = true;
			state = STATE_HAIL;
			//state = STATE_NORMAL;
			attrib_hurricane = false;
			attrib_tornado = true;
			//attrib_tornado = false;
			attrib_highwind = false;
			attrib_tornado_severity = 0;
			attrib_tornado_severity = ATTRIB_FORMING;
			//debug //
			
			Random rand = new Random();
			World world = manager.getWorld();
			
			currentTopYBlock = world.getHeightValue((int)pos.xCoord, (int)pos.zCoord);
			
			if (state >= STATE_THUNDER) {
				if (rand.nextInt(100) == 0) {
					int x = (int) (pos.xCoord + rand.nextInt(size) - rand.nextInt(size));
					int z = (int) (pos.zCoord + rand.nextInt(size) - rand.nextInt(size));
					int y = world.getPrecipitationHeight(x, z);
					if (world.checkChunksExist(x, y, z, x, y, z)) {
						//if (world.canLightningStrikeAt(x, y, z)) {
							world.addWeatherEffect(new EntityLightningBolt(world, (double)x, (double)y, (double)z));
						//}
					}
				}
			}
			
			if (state >= STATE_HAIL) {
				//if (rand.nextInt(1) == 0) {
				for (int i = 0; i < 10; i++) {
					int x = (int) (pos.xCoord + rand.nextInt(size) - rand.nextInt(size));
					int z = (int) (pos.zCoord + rand.nextInt(size) - rand.nextInt(size));
					if (world.checkChunksExist(x, staticYPos, z, x, staticYPos, z) && (world.getClosestPlayer(x, 50, z, 80) != null)) {
						//int y = world.getPrecipitationHeight(x, z);
						//if (world.canLightningStrikeAt(x, y, z)) {
						EntityIceBall hail = new EntityIceBall(world);
						hail.setPosition(x, staticYPos, z);
						world.spawnEntityInWorld(hail);
						//world.addWeatherEffect(new EntityLightningBolt(world, (double)x, (double)y, (double)z));
						//}
						
						//System.out.println("spawned hail: " );
					} else {
						//System.out.println("nope");
					}
				}
			}
			
			//storm movement via wind
			float angle = this.manager.windMan.windAngleGlobal;
			
			double vecX = Math.sin(Math.toRadians(angle));
			double vecZ = Math.cos(Math.toRadians(angle));
			
			motion.xCoord = vecX * manager.windMan.windSpeed;
			motion.zCoord = vecZ * manager.windMan.windSpeed;
			
			double max = 0.2D;
			//max speed
			if (motion.xCoord < -max) motion.xCoord = -max;
			if (motion.xCoord > max) motion.xCoord = max;
			if (motion.zCoord < -max) motion.zCoord = -max;
			if (motion.zCoord > max) motion.zCoord = max;
			
			//actually move storm
			pos.xCoord += motion.xCoord;
			pos.zCoord += motion.zCoord;
			
			//System.out.println("cloud motion: " + motion + " wind angle: " + angle);
			
			//storm progression, heavy WIP
			if (world.getTotalWorldTime() % 1 == 0) {
				if (isGrowing) {
					if (size < maxSize) {
						size++;
					} else {
						isGrowing = false;
					}
				} else {
					if (size > 0) {
						size--;
					} else if (size <= 0) {
						//kill
						//manager.removeStormObject(ID);
					}
				}

				//System.out.println("cur size: " + size);
			}
		}
		
		//encoutered a bug where pos.yCoord was moved off static layer, force set it here...
		pos.yCoord = staticYPos;

		if (attrib_tornado_severity > 0) {
			tornadoHelper.tick(manager.getWorld());
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient() {
		if (particleBehaviorFog == null) {
			particleBehaviorFog = new ParticleBehaviorFog(Vec3.createVectorHelper(pos.xCoord, pos.yCoord, pos.zCoord));
			//particleBehaviorFog.sourceEntity = this;
		} else {
			if (!Minecraft.getMinecraft().isSingleplayer() || !(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu)) {
				particleBehaviorFog.tickUpdateList();
			}
		}
		
		EntityPlayer entP = Minecraft.getMinecraft().thePlayer;
		
		spinSpeed = 0.02D;
		double spinSpeedMax = 0.4D;
		if (attrib_hurricane) {
			spinSpeed = spinSpeedMax * 0.4D;
		} else if (attrib_tornado) {
			spinSpeed = spinSpeedMax * 0.2D;
		} else if (attrib_highwind) {
			spinSpeed = spinSpeedMax * 0.1D;
		} else {
			spinSpeed = spinSpeedMax * 0.05D;
		}
		
		int delay = 1;
		int loopSize = 5;
		Random rand = new Random();
		
		Vec3 playerAdjPos = Vec3.createVectorHelper(entP.posX, pos.yCoord, entP.posZ);
		double maxSpawnDistFromPlayer = 512;
		
		//spawn clouds
		if (this.manager.getWorld().getTotalWorldTime() % delay == 0) {
			for (int i = 0; i < loopSize; i++) {
				if (listParticlesCloud.size() < size*2) {
					double spawnRad = size;
					Vec3 tryPos = Vec3.createVectorHelper(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), pos.yCoord, pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
					if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
						EntityRotFX particle = spawnFogParticle(tryPos.xCoord, tryPos.yCoord, tryPos.zCoord);
						listParticlesCloud.add(particle);
					}
				}
				
				
			}
		}
		
		delay = 1;
		loopSize = 1;
		
		//spawn funnel
		if (attrib_tornado_severity != 0) {
			if (this.manager.getWorld().getTotalWorldTime() % delay == 0) {
				for (int i = 0; i < loopSize; i++) {
					//temp comment out
					//if (attrib_tornado_severity > 0) {
						if (listParticlesFunnel.size() < sizeMaxFunnelParticles) {
							double spawnRad = size/48;
							
							Vec3 tryPos = Vec3.createVectorHelper(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), pos.yCoord, pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
							//int y = entP.worldObj.getPrecipitationHeight((int)tryPos.xCoord, (int)tryPos.zCoord);
							
							if (tryPos.distanceTo(playerAdjPos) < maxSpawnDistFromPlayer) {
								EntityRotFX particle = spawnFogParticle(tryPos.xCoord, currentTopYBlock, tryPos.zCoord);
								
								//move these to a damn profile damnit!
								particle.setMaxAge(200 + rand.nextInt(100));
								particle.particleScale = 150;
								
								listParticlesFunnel.add(particle);
							}
						}
					//}
				}
			}
		}
		
		for (int i = 0; i < listParticlesFunnel.size(); i++) {
			EntityRotFX ent = listParticlesFunnel.get(i);
			if (ent.isDead) {
				listParticlesFunnel.remove(ent);
			} else {
				 double var16 = this.pos.xCoord - ent.posX;
                 double var18 = this.pos.zCoord - ent.posZ;
                 ent.rotationYaw = (float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
                 ent.rotationPitch = -30F;
                 
                 spinEntity(ent);
			}
		}
		
		for (int i = 0; i < listParticlesCloud.size(); i++) {
			EntityRotFX ent = listParticlesCloud.get(i);
			if (ent.isDead) {
				listParticlesCloud.remove(ent);
			} else {
				//ent.posX = pos.xCoord + i*10;
				/*float radius = 50 + (i/1F);
				float posX = (float) Math.sin(ent.entityId);
				float posZ = (float) Math.cos(ent.entityId);
				ent.setPosition(pos.xCoord + posX*radius, ent.posY, pos.zCoord + posZ*radius);*/
		        
				if (state >= STATE_FORMINGSPINNING) {
					double speed = spinSpeed + (rand.nextDouble() * 0.01D);
					double distt = size;//300D;
					double curDist = ent.getDistance(pos.xCoord, ent.posY, pos.zCoord);
					
					double vecX = ent.posX - pos.xCoord;
			        double vecZ = ent.posZ - pos.zCoord;
			        float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
			        //System.out.println("angle: " + angle);
			        
			        //fix speed causing inner part of formation to have a gap
			        angle += speed * 50D;
			        //angle += 20;
			        
			        angle -= (ent.entityId % 10) * 3D;
			        
			        //random addition
			        angle += rand.nextInt(10) - rand.nextInt(10);
			        
			        if (curDist > distt) {
			        	//System.out.println("curving");
			        	angle += 40;
			        	//speed = 1D;
			        }
			        
			        double curSpeed = Math.sqrt(ent.motionX * ent.motionX + ent.motionY * ent.motionY + ent.motionZ * ent.motionZ);
			        
	
			        
			        /*if (curDist < 30) {
			        	ent.motionY -= 0.2D;
			        	if (ent.rotationPitch > 0) {
			        		ent.rotationPitch--;
			        	} else if (ent.rotationPitch < 0) {
			        		ent.rotationPitch++;
			        	} else {
			        		ent.rotationPitch = 0;
			        	}
			        	
			        	angle = -45;
			        } else {
			        	if (ent.rotationPitch > 90) {
			        		ent.rotationPitch--;
			        	} else if (ent.rotationPitch < 90) {
			        		ent.rotationPitch++;
			        	} else {
			        		ent.rotationPitch = 90;
			        	}
			        	//angle = 90;
			        	
			        }*/
			        
			        if (ent.posY < pos.yCoord) {
		        		ent.motionY += 0.1D;
		        	} else {
		        		ent.motionY -= 0.1D;
		        	}
			        
			        if (curSpeed < speed * 20D) {
			        	ent.motionX += -Math.sin(Math.toRadians(angle)) * speed;
				        ent.motionZ += Math.cos(Math.toRadians(angle)) * speed;
			        }
				}
		        
		        
		        
		        //double distToGround = ent.worldObj.getHeightValue((int)pos.xCoord, (int)pos.zCoord);
		        
		        //ent.setPosition(ent.posX, pos.yCoord, ent.posZ);
			}
			/*if (ent.getAge() > 300) {
				ent.setDead();
				listParticles.remove(ent);
			}*/
		}
		
		//System.out.println("size: " + listParticlesCloud.size());
	}
	
	public void spinEntity(Entity entity1) {
		
		StormObject entT = this;
		StormObject entity = this;
		WeatherEntityConfig conf = WeatherTypes.weatherEntTypes.get(curWeatherType);
		
		Random rand = new Random();
		
    	/*if (entity instanceof EntTornado) {
    		entT = (EntTornado) entity;
    	}*/
    
    	boolean forTornado = entT != null;
    	
        //ConfigTornado.Storm_Tornado_height;
        double radius = 10D;
        double scale = conf.tornadoWidthScale;
        double d1 = entity.pos.xCoord - entity1.posX;
        double d2 = entity.pos.zCoord - entity1.posZ;
        float f = (float)((Math.atan2(d2, d1) * 180D) / Math.PI) - 90F;
        float f1;

        for (f1 = f; f1 < -180F; f1 += 360F) { }

        for (; f1 >= 180F; f1 -= 360F) { }

        double distY = entity.pos.yCoord - entity1.posY;
        double distXZ = Math.sqrt(Math.abs(d1)) + Math.sqrt(Math.abs(d2));

        if (entity1.posY - entity.pos.yCoord < 0.0D)
        {
            distY = 1.0D;
        }
        else
        {
            distY = entity1.posY - entity.pos.yCoord;
        }

        if (distY > maxHeight)
        {
            distY = maxHeight;
        }

        double grab = (10D / getWeight(entity1, forTornado))/* / ((distY / maxHeight) * 1D)*/ * ((Math.abs((maxHeight - distY)) / maxHeight));
        float pullY = 0.0F;

        //some random y pull
        if (rand.nextInt(5) != 0)
        {
            //pullY = 0.035F;
        }

        if (distXZ > 5D)
        {
            grab = grab * (radius / distXZ);
        }

        pullY += (float)(conf.tornadoLiftRate / (getWeight(entity1, forTornado) / 2F)/* * (Math.abs(radius - distXZ) / radius)*/);
        
        
        if (entity1 instanceof EntityPlayer)
        {
            double adjPull = 0.2D / ((getWeight(entity1, forTornado) * ((distXZ + 1D) / radius)));
            /*if (!entity1.onGround) {
            	adjPull /= (((float)(((double)playerInAirTime+1D) / 200D)) * 15D);
            }*/
            pullY += adjPull;
            //0.2D / ((getWeight(entity1) * ((distXZ+1D) / radius)) * (((distY) / maxHeight)) * 3D);
            //grab = grab + (10D * ((distY / maxHeight) * 1D));
            double adjGrab = (10D * (((float)(((double)playerInAirTime + 1D) / 400D))));

            if (adjGrab > 50)
            {
                adjGrab = 50D;
            }
            
            if (adjGrab < -50)
            {
                adjGrab = -50D;
            }

            grab = grab - adjGrab;

            if (entity1.motionY > -0.8)
            {
            	//System.out.println(entity1.motionY);
                entity1.fallDistance = 0F;
            } else if (entity1.motionY > -1.5) {
            	//entity1.fallDistance = 5F;
            	//System.out.println(entity1.fallDistance);
            }

            
        }
        else if (entity1 instanceof EntityLivingBase)
        {
            double adjPull = 0.005D / ((getWeight(entity1, forTornado) * ((distXZ + 1D) / radius)));
            /*if (!entity1.onGround) {
            	adjPull /= (((float)(((double)playerInAirTime+1D) / 200D)) * 15D);
            }*/
            pullY += adjPull;
            //0.2D / ((getWeight(entity1) * ((distXZ+1D) / radius)) * (((distY) / maxHeight)) * 3D);
            //grab = grab + (10D * ((distY / maxHeight) * 1D));
            int airTime = entity1.getEntityData().getInteger("timeInAir");
            double adjGrab = (10D * (((float)(((double)(airTime) + 1D) / 400D))));

            if (adjGrab > 50)
            {
                adjGrab = 50D;
            }
            
            if (adjGrab < -50)
            {
                adjGrab = -50D;
            }

            grab = grab - adjGrab;

            if (entity1.motionY > -2.0)
            {
                entity1.fallDistance = 0F;
            }

            if (forTornado) entity1.onGround = false;
            
            //System.out.println(adjPull);
        }
        
        
        grab += conf.relTornadoSize;
        f1 = (float)((double)f1 + (75D + grab - (10D * scale)));
        
        if (entT != null) {
        	
        	if (entT.scale != 1F) f1 += 20 - (20 * entT.scale);
        }
        
        float f3 = (float)Math.cos(-f1 * 0.01745329F - (float)Math.PI);
        float f4 = (float)Math.sin(-f1 * 0.01745329F - (float)Math.PI);
        float f5 = conf.tornadoPullRate * 1;
        
        if (entT != null) {
        	if (entT.scale != 1F) f5 *= entT.scale * 1.2F;
        }

        if (entity1 instanceof EntityLivingBase)
        {
            f5 /= (getWeight(entity1, forTornado) * ((distXZ + 1D) / radius));
        }
        
        //if player and not spout
        if (entity1 instanceof EntityPlayer && curWeatherType != 0) {
        	//System.out.println("grab: " + f5);
        	if (entity1.onGround) {
        		f5 *= 10.5F;
        	} else {
        		f5 *= 5F;
        	}
        	//if (entity1.worldObj.rand.nextInt(2) == 0) entity1.onGround = false;
        } else if (entity1 instanceof EntityLivingBase && curWeatherType != 0) {
        	f5 *= 1.5F;
        }

        if (conf.type == conf.TYPE_SPOUT && entity1 instanceof EntityLivingBase) {
        	f5 *= 0.3F;
        }
        
        float moveX = f3 * f5;
        float moveZ = f4 * f5;
        //tornado strength changes
        float str = 1F;

        /*if (entity instanceof EntTornado)
        {
            str = ((EntTornado)entity).strength;
        }*/
        
        str = strength;
        
        if (conf.type == conf.TYPE_SPOUT && entity1 instanceof EntityLivingBase) {
        	str *= 0.3F;
        }

        pullY *= str / 100F;
        
        if (entT != null) {
        	if (entT.scale != 1F) {
        		pullY *= entT.scale * 1.0F;
        		pullY += 0.002F;
        	}
        }
        
        setVel(entity1, -moveX, pullY, moveZ);
	}
	
	public void setVel(Entity entity, float f, float f1, float f2)
    {
        entity.motionX += f;
        entity.motionY += f1;
        entity.motionZ += f2;

        if (entity instanceof EntitySquid)
        {
            entity.setPosition(entity.posX + entity.motionX * 5F, entity.posY, entity.posZ + entity.motionZ * 5F);
        }
    }
	
	public float getWeight(Entity entity1) {
    	return getWeight(entity1, false);
    }
    
    public float getWeight(Entity entity1, boolean forTornado)
    {
    	
    	if (entity1 instanceof IWindHandler) {
    		return ((IWindHandler) entity1).getWindWeight();
    	}
    	
    	//commented out for weather2 copy
        if (entity1 instanceof EntityMovingBlock)
        {
            return 1F + ((float)((EntityMovingBlock) entity1).age / 200);
        }

        if (entity1 instanceof EntityPlayer)
        {
            if (entity1.onGround || entity1.handleWaterMovement())
            {
                playerInAirTime = 0;
            }
            else
            {
                //System.out.println(playerInAirTime);
                playerInAirTime++;
            }

            
            if (((EntityPlayer) entity1).capabilities.isCreativeMode) return 99999999F;
            
            int extraWeight = 0;
            
            if (((EntityPlayer)entity1).inventory != null && (((EntityPlayer)entity1).inventory.armorInventory[2] != null) && ((EntityPlayer)entity1).inventory.armorInventory[2].itemID == Item.plateIron.itemID)
            {
            	extraWeight = 2;
            }

            if (((EntityPlayer)entity1).inventory != null && (((EntityPlayer)entity1).inventory.armorInventory[2] != null) && ((EntityPlayer)entity1).inventory.armorInventory[2].itemID == Item.plateDiamond.itemID)
            {
            	extraWeight = 4;
            }

            if (forTornado) {
            	return 4.5F + extraWeight + ((float)(playerInAirTime / 400));
            } else {
            	return 5.0F + extraWeight + ((float)(playerInAirTime / 400));
            }
        }

        if (entity1.worldObj.isRemote && entity1 instanceof EntityRotFX)
        {
            float var = getParticleWeight((EntityRotFX)entity1);

            if (var != -1)
            {
                return var;
            }
        }

        if (entity1 instanceof EntitySquid)
        {
            return 400F;
        }

        /*if (entity1 instanceof EntityPlayerProxy) {
        	return 50F;
        }*/

        if (entity1 instanceof EntityLivingBase)
        {
            //if (entity1.onGround || entity1.handleWaterMovement())
            //{
                //entity1.onGround = false;
                //c_CoroWeatherUtil.setEntityAge((EntityLivingBase)entity1, -150);
        	int airTime = entity1.getEntityData().getInteger("timeInAir");
        	if (entity1.onGround || entity1.handleWaterMovement())
            {
                airTime = 0;
            }
            else {
            	airTime++;
            }
        	
        	//test
        	//airTime = 0;
        	
        	entity1.getEntityData().setInteger("timeInAir", airTime);
            //}

            //System.out.println(((EntityLivingBase)entity1).entityAge+150);
            //int age = ((Integer)entToAge.get(entity1)).intValue();
            //System.out.println(age);
            if (forTornado) {
            	//System.out.println(1.0F + ((c_CoroWeatherUtil.getEntityAge((EntityLivingBase)entity1) + 150) / 50));
            	return 1.5F + ((airTime) / 400);
            } else {
            	return 500.0F + (entity1.onGround ? 2.0F : 0.0F) + ((airTime) / 50);
            }
            
        }

        if (/*entity1 instanceof EntitySurfboard || */entity1 instanceof EntityBoat || entity1 instanceof EntityItem || entity1 instanceof EntityTropicalFishHook || entity1 instanceof EntityFishHook)
        {
            return 4000F;
        }

        if (entity1 instanceof EntityMinecart)
        {
            return 80F;
        }

        return 1F;
    }

    @SideOnly(Side.CLIENT)
    public static float getParticleWeight(EntityRotFX entity1)
    {
    	//commented out for weather2 copy
        /*if (entity1 instanceof EntityFallingRainFX)
        {
            return 1.1F;
        }*/

        if (entity1 instanceof EntityTexFX)
        {
            return 5.0F + ((float)entity1.getAge() / 200);
        }

        //commented out for weather2 copy
        /*if (entity1 instanceof EntityWindFX)
        {
            return 1.4F + ((float)entity1.getAge() / 200);
        }*/

        if (entity1 instanceof EntityFX)
        {
            return 5.0F + ((float)entity1.getAge() / 200);
        }

        return -1;
    }
	
	@SideOnly(Side.CLIENT)
    public EntityRotFX spawnFogParticle(double x, double y, double z) {
    	double speed = 0D;
		Random rand = new Random();
    	EntityRotFX entityfx = particleBehaviorFog.spawnNewParticleIconFX(Minecraft.getMinecraft().theWorld, ParticleRegistry.cloud256, x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed);
		particleBehaviorFog.initParticle(entityfx);
		//lock y
		//entityfx.spawnY = (float) entityfx.posY;
		//entityfx.spawnY = ((int)200 - 5) + rand.nextFloat() * 5;
		entityfx.noClip = true;
    	entityfx.callUpdatePB = false;
    	entityfx.setMaxAge((size/2) + rand.nextInt(100));
    	
    	float randFloat = (rand.nextFloat() * 0.6F);
		float baseBright = 0.7F;
		if (state >= STATE_RAIN) {
			baseBright = 0.3F;
		}
		float finalBright = Math.min(1F, baseBright+randFloat);
		entityfx.setRBGColorF(finalBright, finalBright, finalBright);
    	
		ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
		//entityfx.spawnAsWeatherEffect();
		particleBehaviorFog.particles.add(entityfx);
		return entityfx;
    }
	
	public void setDead() {
		Weather.dbg("storm killed");
	}
}
