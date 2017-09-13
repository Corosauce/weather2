package weather2.volcano;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.Weather;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilBlock;
import weather2.weathersystem.WeatherManagerBase;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.Vec3;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviors;
import extendedrenderer.particle.entity.EntityRotFX;

public class VolcanoObject {

	//used on both server and client side, mark things SideOnly where needed
	
	//management stuff
	public static long lastUsedID = 0; //ID starts from 0, set on nbt load, who would max this out for volcanos? surely this will never fail...
	public long ID;
	public WeatherManagerBase manager;
	
	@SideOnly(Side.CLIENT)
	public List<EntityRotFX> listParticlesSmoke = new ArrayList<EntityRotFX>();
	@SideOnly(Side.CLIENT)
	public ParticleBehaviors particleBehaviors;
	
	public int sizeMaxParticles = 300;
	
	//basic info
	public static int staticYPos = 200;
	public Vec3 pos = new Vec3(0, staticYPos, 0);

	//public boolean isGrowing = true;
	public int processRateDelay = 20; //make configurable
	public Block topBlockID = Blocks.AIR;
	public int startYPos = -1;
	public int curRadius = 5;
	public int curHeight = 3;
	
	//growth / progression info:
	public int state = 0;
	
	//state 0 = initial gen
	//state 1 = grow volcano to max
	//state 2 = leak lava up to top
	//state 3 = build pressure
	//state 4 = explode top and PARTICLES
	//state 5 = cool off and harden top, make sure ring forms
	//state 6 = non state, repeat 2-6
	
	//state 1
	public int size = 0;
	public int maxSize = 20; //make configurable
	
	//state 2
	public int step = 0;
	
	//state 3
	public int stepsBuildupMax = 20;
	
	//state 4
	public int ticksToErupt = 20*30;//20*60*2;
	public int ticksPerformedErupt = 0;
	
	//state 4
	public int ticksToCooldown = 20*30;//20*60*5; //this should ideally be larger than maxSize so it can full cooldown (does 1 y layer per 1 processRateDelay)
	public int ticksPerformedCooldown = 0;
	
	
	//public static int STATE_FORMING = 0;
	//public static int STATE_ERUPTING = 1;
	
	public int growthStage = 0;
	
	public VolcanoObject(WeatherManagerBase parManager) {
		manager = parManager;
	}
	
	public void initFirstTime() {
		ID = VolcanoObject.lastUsedID++;
	}
	
	public void initPost() {
		
	}
	
	public void resetEruption() {
		step = 0;
		ticksPerformedErupt = 0;
		ticksPerformedCooldown = 0;
		state = 2;
		ticksPerformedErupt = 0;
		ticksPerformedCooldown = 0;
	}
	
	public void readFromNBT(NBTTagCompound data)
    {
		ID = data.getLong("ID");
		
		pos = new Vec3(data.getInteger("posX"), data.getInteger("posY"), data.getInteger("posZ"));
		size = data.getInteger("size");
		maxSize = data.getInteger("maxSize");
		
		state = data.getInteger("state");
		//isGrowing = data.getBoolean("isGrowing");
		
		curRadius = data.getInteger("curRadius");
		curHeight = data.getInteger("curHeight");
		topBlockID = (Block)Block.REGISTRY.getObject(new ResourceLocation(data.getString("topBlockID")));
		//topBlockID = data.getInteger("topBlockID");
		startYPos = data.getInteger("startYPos");
		
		step = data.getInteger("step");
		ticksPerformedErupt = data.getInteger("ticksPerformedErupt");
		ticksPerformedCooldown = data.getInteger("ticksPerformedCooldown");
		
    }
	
	public void writeToNBT(NBTTagCompound data)
    {
		data.setLong("ID", ID);
		
		data.setInteger("posX", (int)pos.xCoord);
		data.setInteger("posY", (int)pos.yCoord);
		data.setInteger("posZ", (int)pos.zCoord);
		
		data.setInteger("size", size);
		data.setInteger("maxSize", maxSize);
		
		data.setInteger("state", state);
		//data.setBoolean("isGrowing", isGrowing);
		
		data.setInteger("curRadius", curRadius);
		data.setInteger("curHeight", curHeight);
		data.setString("topBlockID", Block.REGISTRY.getNameForObject(topBlockID).toString());
		//data.setInteger("topBlockID", topBlockID);
		data.setInteger("startYPos", startYPos);
		
		data.setInteger("step", step);
		data.setInteger("ticksPerformedErupt", ticksPerformedErupt);
		data.setInteger("ticksPerformedCooldown", ticksPerformedCooldown);
    }
	
	//receiver method
	public void nbtSyncFromServer(NBTTagCompound parNBT) {
		ID = parNBT.getLong("ID");
		Weather.dbg("VolcanoObject " + ID + " receiving sync");
		
		pos = new Vec3(parNBT.getInteger("posX"), parNBT.getInteger("posY"), parNBT.getInteger("posZ"));
		size = parNBT.getInteger("size");
		maxSize = parNBT.getInteger("maxSize");
		
		state = parNBT.getInteger("state");
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
		
		return data;
	}
	
	public void tick() {
		//Weather.dbg("ticking storm " + ID + " - manager: " + manager);
		

		//debug
		//maxSize = 800;
		//size = maxSize;
		//isGrowing = true;
		processRateDelay = 10;
		
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		if (side == Side.CLIENT) {
			if (!WeatherUtil.isPaused()) {
				tickClient();
			}
		} else {
			
			World world = manager.getWorld();

			float res = 5;
			
			if (state == 0) {
				
				//quantify and ground level coords
				pos.xCoord = Math.floor(pos.xCoord);
				pos.zCoord = Math.floor(pos.zCoord);
				
				pos.yCoord = WeatherUtilBlock.getPrecipitationHeightSafe(world, new BlockPos((int)pos.xCoord, 0, (int)pos.zCoord)).getY();
				startYPos = (int) pos.yCoord;

				IBlockState statez = world.getBlockState(new BlockPos(MathHelper.floor(pos.xCoord), MathHelper.floor(pos.yCoord-1), MathHelper.floor(pos.zCoord)));
				topBlockID = statez.getBlock();
				
				if (CoroUtilBlock.isAir(topBlockID) || !statez.getMaterial().isSolid()) {
					topBlockID = world.getBlockState(new BlockPos((int)pos.xCoord, (int)pos.yCoord-1, (int)pos.zCoord)).getBlock();
				}
				
				for (int yy = startYPos + curHeight; yy > 2; yy--) {
					for (int dist = 0; dist <= curRadius; dist++) {
						
						double vecX = dist;
						double vecZ = 0;
						
						if (yy > startYPos) {
							vecX = dist + (startYPos - yy);
						}
						
						for (double angle = 0; angle <= 360; angle += res) {
							
							Vec3 vec = new Vec3(vecX, 0, vecZ);
							vec.rotateAroundY((float)angle);
							
							int posX = (int)Math.floor((pos.xCoord)+vec.xCoord+0.5);
							int posZ = (int)Math.floor((pos.zCoord)+vec.zCoord+0.5);
							
							Block blockID = Blocks.OBSIDIAN;
							
							if (yy >= startYPos) {
								blockID = topBlockID;
							} else if (dist < curRadius) {
								blockID = Blocks.LAVA;
							}
							
							//skip derpy top layer
							if (yy != startYPos + curHeight) {
								Block idScan = world.getBlockState(new BlockPos(posX, yy, posZ)).getBlock();
								if (CoroUtilBlock.isAir(idScan) || idScan.getMaterial(idScan.getDefaultState()) == Material.WATER) {
									world.setBlockState(new BlockPos(posX, yy, posZ), blockID.getDefaultState());
								}
							}
						}
					}
				}
				
				state++;
				
				System.out.println("initial volcano created");
				
			} else if (state == 1) {
				if (this.manager.getWorld().getTotalWorldTime() % processRateDelay == 0) {
					//if (isGrowing) {
						size++;
						curHeight++;
						curRadius++;
						if (size >= maxSize) {
							//isGrowing = false;
							state++;
						}
						
						res = 1;
						
						for (int yy = 0; yy <= curHeight; yy++) {
							
							int radiusForLayer = Math.max(0, curRadius - yy - 2);
							
							//System.out.println("rad: " + radiusForLayer);
							
							double vecX = radiusForLayer;
							double vecZ = 0;
							
							/*if (yy > startYPos) {
								vecX = dist + (startYPos - yy);
							}*/
							
							for (double angle = 0; angle <= 360; angle += res) {
								
								Vec3 vec = new Vec3(vecX, 0, vecZ);
								vec.rotateAroundY((float)angle);
								
								int posX = (int)Math.floor((pos.xCoord)+vec.xCoord+0.5);
								int posZ = (int)Math.floor((pos.zCoord)+vec.zCoord+0.5);
								
								Block blockID = topBlockID;
								
								Random rand = new Random();
								
								//some random chance of placing a block here
								if (rand.nextInt(4) == 0) {
								
									//skip top layers
									if (yy != curHeight) {
										if (CoroUtilBlock.isAir(world.getBlockState(new BlockPos(posX, startYPos+yy, posZ)).getBlock())) {
											world.setBlockState(new BlockPos(posX, startYPos+yy, posZ), blockID.getDefaultState());
										}
									}
									
									//handle growth under expanded area
									int underY = startYPos+yy-1;
									Block underBlockID = world.getBlockState(new BlockPos(posX, underY, posZ)).getBlock();
									while ((CoroUtilBlock.isAir(underBlockID) || underBlockID.getMaterial(underBlockID.getDefaultState()) == Material.WATER) && underY > 1) {
										world.setBlockState(new BlockPos(posX, underY, posZ), Blocks.DIRT.getDefaultState());
										underY--;
										underBlockID = world.getBlockState(new BlockPos(posX, underY, posZ)).getBlock();
									}
								}
							}
							
						}
					//}

					System.out.println("cur size: " + size + " - " + curHeight + " - " + curRadius);
				}
			} else if (state == 2) {
				//build up pressure, somehow, just a timer and increasing particle effects?
				//occasional rumble and shake
				//buildup lava through center, once it hits top, thats when actual pressure builds
				
				//temp remove self - this might have a bug, make sure it works properly
				if (this.manager.getWorld().getTotalWorldTime() % processRateDelay == 0) {
					
					if (step <= maxSize) {
						int posX = (int)Math.floor((pos.xCoord));
						int posY = (int)Math.floor((startYPos)) + step;
						int posZ = (int)Math.floor((pos.zCoord));
						
						world.setBlockState(new BlockPos(posX, posY, posZ), Blocks.LAVA.getDefaultState());
						world.setBlockState(new BlockPos(posX+1, posY, posZ), Blocks.LAVA.getDefaultState());
						world.setBlockState(new BlockPos(posX-1, posY, posZ), Blocks.LAVA.getDefaultState());
						world.setBlockState(new BlockPos(posX, posY, posZ+1), Blocks.LAVA.getDefaultState());
						world.setBlockState(new BlockPos(posX, posY, posZ-1), Blocks.LAVA.getDefaultState());
					} else {
						step = 0;
						state++;
					}
					
					step++;
					
				}
				
			} else if (state == 3) {
				
				//slowly increase smoking particles here
				if (this.manager.getWorld().getTotalWorldTime() % processRateDelay == 0) {
					step++;
					if (step > stepsBuildupMax) {
						step = 0;
						state++;	
					}
				}
				
			} else if (state == 4) {
				
				
				
				if (ticksPerformedErupt == 0) {
					
					Weather.dbg("volcano " + ID + " is erupting");
					
					for (int i = 0; i < 3; i++) {
						int posX = (int)Math.floor((pos.xCoord));
						int posY = (int)Math.floor((startYPos)) + maxSize + i;
						int posZ = (int)Math.floor((pos.zCoord));
						
						Block blockID = Blocks.LAVA;
						
						world.setBlockState(new BlockPos(posX, posY, posZ), blockID.getDefaultState());
						world.setBlockState(new BlockPos(posX+1, posY, posZ), blockID.getDefaultState());
						world.setBlockState(new BlockPos(posX-1, posY, posZ), blockID.getDefaultState());
						world.setBlockState(new BlockPos(posX, posY, posZ+1), blockID.getDefaultState());
						world.setBlockState(new BlockPos(posX, posY, posZ-1), blockID.getDefaultState());
						world.setBlockState(new BlockPos(posX+1, posY, posZ+1), blockID.getDefaultState());
						world.setBlockState(new BlockPos(posX-1, posY, posZ-1), blockID.getDefaultState());
						world.setBlockState(new BlockPos(posX-1, posY, posZ+1), blockID.getDefaultState());
						world.setBlockState(new BlockPos(posX+1, posY, posZ-1), blockID.getDefaultState());
					}
				}
				
				ticksPerformedErupt++;
				if (ticksPerformedErupt > ticksToErupt) {
					state++;
				}
				
			} else if (state == 5) {
				
				if (ticksPerformedCooldown == 0) {
					Weather.dbg("volcano " + ID + " is cooling");
				}
				
				if (ticksPerformedCooldown % processRateDelay == 0) {
					int posX = (int)Math.floor((pos.xCoord));
					int posY = (int)Math.floor((startYPos)) + maxSize - step + 2;
					int posZ = (int)Math.floor((pos.zCoord));
					
					Block blockID = Blocks.STONE;
					
					world.setBlockState(new BlockPos(posX, posY, posZ), blockID.getDefaultState());
					world.setBlockState(new BlockPos(posX+1, posY, posZ), blockID.getDefaultState());
					world.setBlockState(new BlockPos(posX-1, posY, posZ), blockID.getDefaultState());
					world.setBlockState(new BlockPos(posX, posY, posZ+1), blockID.getDefaultState());
					world.setBlockState(new BlockPos(posX, posY, posZ-1), blockID.getDefaultState());
					world.setBlockState(new BlockPos(posX+1, posY, posZ+1), blockID.getDefaultState());
					world.setBlockState(new BlockPos(posX-1, posY, posZ-1), blockID.getDefaultState());
					world.setBlockState(new BlockPos(posX-1, posY, posZ+1), blockID.getDefaultState());
					world.setBlockState(new BlockPos(posX+1, posY, posZ-1), blockID.getDefaultState());
					
					step++;
				}
				
				ticksPerformedCooldown++;
				if (ticksPerformedCooldown > ticksToCooldown) {
					state++;
				}
				
			} else if (state == 6) {
				
				Weather.dbg("volcano " + ID + " has reset!");
				
				//go to step 2
				resetEruption();
				//manager.removeVolcanoObject(ID);
				
			}
			
			//manager.getVolcanoObjects().clear();
			
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	public void tickClient() {
		
		//Weather.dbg("ticking client volcano " + ID + " - " + state);
		
		if (particleBehaviors == null) {
			particleBehaviors = new ParticleBehaviors(new Vec3(pos.xCoord, pos.yCoord, pos.zCoord));
			//particleBehaviorFog.sourceEntity = this;
		} else {
			if (!Minecraft.getMinecraft().isSingleplayer() || !(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu)) {
				particleBehaviors.tickUpdateList();
			}
		}
		
		int delay = 1;
		int loopSize = 1;
		Random rand = new Random();
		
		//temo
		//if (state == 3 || state == 4) {
			if (this.manager.getWorld().getTotalWorldTime() % delay == 0) {
				for (int i = 0; i < loopSize; i++) {
					if (listParticlesSmoke.size() < 500) {
						double spawnRad = size/48;
						EntityRotFX particle = spawnSmokeParticle(pos.xCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad), pos.yCoord + size + 2, pos.zCoord + (rand.nextDouble()*spawnRad) - (rand.nextDouble()*spawnRad));
						listParticlesSmoke.add(particle);
					}
				}
			}
		//}
		
		delay = 1;
		loopSize = 2;
		
		for (int i = 0; i < listParticlesSmoke.size(); i++) {
			EntityRotFX ent = listParticlesSmoke.get(i);
			if (!ent.isAlive()) {
				listParticlesSmoke.remove(ent);
			} else {
				//ent.posX = pos.xCoord + i*10;
				/*float radius = 50 + (i/1F);
				float posX = (float) Math.sin(ent.entityId);
				float posZ = (float) Math.cos(ent.entityId);
				ent.setPosition(pos.xCoord + posX*radius, ent.posY, pos.zCoord + posZ*radius);*/
		        
				double speed = 0.4D + (rand.nextDouble() * 1D * 0.01D);
				double distt = 300D;
				double curDist = ent.getDistance(pos.xCoord, staticYPos, pos.zCoord);
				
				double vecX = ent.getPosX() - pos.xCoord;
		        double vecZ = ent.getPosZ() - pos.zCoord;
		        float angle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI);
		        //System.out.println("angle: " + angle);
		        angle += 50;
		        
		        angle -= (ent.getEntityId() % 10) * 3D;
		        
		        //random addition
		        angle += rand.nextInt(10) - rand.nextInt(10);
		        
		        if (curDist > distt) {
		        	angle += 20;
		        	//speed = 1D;
		        }
		        
		        double curSpeed = Math.sqrt(ent.getMotionX() * ent.getMotionX() + ent.getMotionY() * ent.getMotionY() + ent.getMotionZ() * ent.getMotionZ());
		        

		        
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
		        
		        /*if (ent.posY < staticYPos) {
	        		ent.motionY += 0.1D;
	        	} else {
	        		ent.motionY -= 0.1D;
	        	}
		        
		        if (curSpeed < 3D) {
		        	ent.motionX += -Math.sin(Math.toRadians(angle)) * speed;
			        ent.motionZ += Math.cos(Math.toRadians(angle)) * speed;
		        }*/
		        
		        
		        
		        //double distToGround = ent.world.getHeightValue((int)pos.xCoord, (int)pos.zCoord);
		        
		        //ent.setPosition(ent.posX, pos.yCoord, ent.posZ);
			}
			/*if (ent.getAge() > 300) {
				ent.setDead();
				listParticles.remove(ent);
			}*/
		}
		
		//System.out.println("size: " + listParticlesCloud.size());
	}
	
	@SideOnly(Side.CLIENT)
    public EntityRotFX spawnSmokeParticle(double x, double y, double z) {
    	double speed = 0D;
		Random rand = new Random();
    	EntityRotFX entityfx = particleBehaviors.spawnNewParticleIconFX(Minecraft.getMinecraft().world, ParticleRegistry.cloud256, x, y, z, (rand.nextDouble() - rand.nextDouble()) * speed, 0.0D/*(rand.nextDouble() - rand.nextDouble()) * speed*/, (rand.nextDouble() - rand.nextDouble()) * speed);
    	particleBehaviors.initParticle(entityfx);
    	particleBehaviors.setParticleRandoms(entityfx, true, true);
    	particleBehaviors.setParticleFire(entityfx);
		//lock y
		//entityfx.spawnY = (float) entityfx.posY;
		//entityfx.spawnY = ((int)200 - 5) + rand.nextFloat() * 5;
		entityfx.setCanCollide(false);
    	entityfx.callUpdatePB = false;
    	entityfx.setMaxAge(400 + rand.nextInt(200));
    	entityfx.setScale(50);
    	
    	float randFloat = (rand.nextFloat() * 0.6F);
		float baseBright = 0.1F;
		float finalBright = Math.min(1F, baseBright+randFloat);
		entityfx.setRBGColorF(finalBright, finalBright, finalBright);
    	
		ExtendedRenderer.rotEffRenderer.addEffect(entityfx);
		//entityfx.spawnAsWeatherEffect();
		particleBehaviors.particles.add(entityfx);
		return entityfx;
    }
	
	public void reset() {
		setDead();
	}
	
	public void setDead() {
		Weather.dbg("volcano... killed? NO ONE KILLS A VOLCANO!");
	}
}
