package weather2.weathersystem.storm;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import weather2.Weather;
import weather2.config.ConfigMisc;
import weather2.entity.EntityMovingBlock;
import weather2.util.WeatherUtil;
import weather2.util.WeatherUtilEntity;
import weather2.util.WeatherUtilSound;
import CoroUtil.util.CoroUtilBlock;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TornadoHelper {
	
	public StormObject storm;
	
	public int blockCount = 0;
	
	public int ripCount = 0;

    public long lastGrabTime = 0;
    public int tickGrabCount = 0;
    public int removeCount = 0;
    public int tryRipCount = 0;
    
    public int tornadoBaseSize = 5;
    public int grabDist = 100;
    
    //potentially an issue var
    public boolean lastTickPlayerClose;
	
	public TornadoHelper(StormObject parStorm) {
		storm = parStorm;
	}
	
	public int getTornadoBaseSize() {
        int sizeChange = 10;
		if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE5) {
        	return sizeChange * 9;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE4) {
        	return sizeChange * 7;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE3) {
        	return sizeChange * 5;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE2) {
        	return sizeChange * 4;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_STAGE1) {
        	return sizeChange * 3;
        } else if (storm.levelCurIntensityStage >= StormObject.STATE_FORMING) {
        	return sizeChange * 1;
        } else {
        	return 5;
        }
	}
	
	public void tick(World parWorld) {
		boolean seesLight = false;
        tickGrabCount = 0;
        removeCount = 0;
        tryRipCount = 0;
        int tryRipMax = 300;

        //startDissipate();
        
        //tornado profile changing from storm data
        tornadoBaseSize = getTornadoBaseSize();
        
        if (storm.stormType == storm.TYPE_WATER) {
        	tornadoBaseSize *= 3;
        }
        
        //Weather.dbg("getTornadoBaseSize: " + tornadoBaseSize + " - " + storm.levelCurIntensityStage);
        
        /*if (parWorld.isRemote) {
        	soundUpdates();
        }*/
        
        forceRotate(parWorld);
        
        Random rand = new Random();
        
        //confirm this is correct, changing to formation use!
        //int spawnYOffset = (int) storm.currentTopYBlock;
        int spawnYOffset = (int) storm.posBaseFormationPos.yCoord;

        if (!parWorld.isRemote && ConfigMisc.Storm_Tornado_grabBlocks/*getStorm().grabsBlocks*/)
        {
            int yStart = 00;
            int yEnd = (int)storm.pos.yCoord/* + 72*/;
            int yInc = 1;

            //commented out for weather2
            /*if (getStorm().type == getStorm().TYPE_HURRICANE)
            {
                yStart = 10;
                yEnd = 40;
            }*/
            BiomeGenBase bgb = parWorld.getBiomeGenForCoords(MathHelper.floor_double(storm.pos.xCoord), MathHelper.floor_double(storm.pos.zCoord));
        	
            //prevent grabbing in high areas (hills)
        	if (bgb != null && bgb.rootHeight + bgb.heightVariation <= 0.7) {
        		
	            for (int i = yStart; i < yEnd; i += yInc)
	            {
	                int YRand = i;//rand.nextInt(126)+2;
	                int ii = YRand / 4;
	
	                if (i > 20 && rand.nextInt(2) != 0)
	                {
	                    continue;
	                }
	
	                if (tryRipCount > tryRipMax)
	                {
	                    break;
	                }
	                
	                
	                int extraTry = (int) ((storm.levelCurIntensityStage+1 - storm.levelStormIntensityFormingStartVal) * 5);
	                int loopAmount = 5 + ii + extraTry;
	                
	                if (storm.stormType == StormObject.TYPE_WATER) {
	                	loopAmount = 1 + ii/2;
	                }
	
	                for (int k = 0; k < loopAmount; k++)
	                {
	                    //for (int k = 0; k < mod_EntMover.tornadoBaseSize/2+(ii/2); k++) {
	                    //for (int l = 0; l < mod_EntMover.tornadoBaseSize/2+(ii/2); l++) {
	                    //if (rand.nextInt(3) != 0) { continue; }
	                    if (tryRipCount > tryRipMax)
	                    {
	                        break;
	                    }
	
	                    int tryY = (int)(spawnYOffset + YRand - 1.5D); //mod_EntMover.tornadoBaseSize;
	
	                    if (tryY > 255)
	                    {
	                        tryY = 255;
	                    }
	
	                    //System.out.println(posY);
	                    //int tryX = (int)posX+k-((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                    //int tryZ = (int)posZ+l-((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                    int tryX = (int)storm.pos.xCoord + rand.nextInt(tornadoBaseSize + (ii)) - ((tornadoBaseSize / 2) + (ii / 2));
	                    int tryZ = (int)storm.pos.zCoord + rand.nextInt(tornadoBaseSize + (ii)) - ((tornadoBaseSize / 2) + (ii / 2));
	
	                    if (tryRipCount < tryRipMax)
	                    {
	                    	
	                    	
	                        Block blockID = parWorld.getBlock(tryX, tryY, tryZ);
	                        
	                        boolean performed = false;
	
	                        if (!CoroUtilBlock.isAir(blockID) && canGrab(parWorld, blockID)/* && Block.blocksList[blockID].blockMaterial == Material.ground*//* && worldObj.getHeightValue(tryX, tryZ)-1 == tryY*/)
	                        {
	                            /*if (blockID != 0 && canGrab(blockID) && (worldObj.getBlockId(tryX,tryY+1,tryZ) == 0 ||
	                                    worldObj.getBlockId(tryX+1,tryY,tryZ) == 0 ||
	                                    worldObj.getBlockId(tryX,tryY,tryZ+1) == 0 ||
	                                    worldObj.getBlockId(tryX-1,tryY,tryZ) == 0 ||
	                                    worldObj.getBlockId(tryX,tryY,tryZ-1) == 0)) {*/
	                            tryRipCount++;
	                            seesLight = tryRip(parWorld, tryX, tryY, tryZ, true);
	                            
	                            performed = seesLight;
	                        }
	                        
	                        if (!performed && ConfigMisc.Storm_Tornado_RefinedGrabRules) {
	                        	if (blockID == Blocks.grass) {
	                        		parWorld.setBlock(tryX, tryY, tryZ, Blocks.dirt);
	                        	}
	                        }
	                    	
	                    }
	
	                    /*tryX = (int)posX-k+((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                    tryZ = (int)posZ-l+((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	
	                    if (tryRipCount < tryRipMax) {
	                    	int blockID = this.worldObj.getBlockId(tryX,tryY,tryZ);
	                    	if (blockID != 0 && canGrab(blockID)) {
	                    		tryRipCount++;
	                    		seesLight = tryRip(tryX,tryY,tryZ, true);
	                    	}
	                    }*/
	                    //}
	                    //int tryX = (int)posX+this.rand.nextInt(mod_EntMover.tornadoBaseSize+(ii))-((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                    //int tryZ = (int)posZ+this.rand.nextInt(mod_EntMover.tornadoBaseSize+(ii))-((mod_EntMover.tornadoBaseSize/2)+(ii/2));
	                }
	            }
	
	            /*if (getStorm().type == getStorm().TYPE_TORNADO)
	            {*/
	                for (int k = 0; k < 10; k++)
	                {
	                    int tryX = (int)storm.pos.xCoord + rand.nextInt(40) - 20;
	                    int tryY = (int)spawnYOffset - 2 + rand.nextInt(8);
	                    int tryZ = (int)storm.pos.zCoord + rand.nextInt(40) - 20;
	
	                    if (tryRipCount < tryRipMax)
	                    {
	                        Block blockID = parWorld.getBlock(tryX, tryY, tryZ);
	
	                        if (!CoroUtilBlock.isAir(blockID) && canGrab(parWorld, blockID))
	                        {
	                            tryRipCount++;
	                            tryRip(parWorld, tryX, tryY, tryZ, true);
	                        }
	                    }
	                }
	            //}
	
	            /*if (tryRipCount >= tryRipMax)
	            {
	                hitMaxTriesLastTick = true;
	            }
	            else
	            {
	                hitMaxTriesLastTick = false;
	            }*/
        	}
        }
        else
        {
            seesLight = true;
        }

        if (Math.abs((spawnYOffset - storm.pos.yCoord)) > 5)
        {
            seesLight = true;
        }
	}
	
	public boolean isNoDigCoord(int x, int y, int z) {

        // MCPC start
          /*org.bukkit.entity.Entity bukkitentity = this.getBukkitEntity();
          if ((bukkitentity instanceof Player)) {
            Player player = (Player)bukkitentity;
            BlockBreakEvent breakev = new BlockBreakEvent(player.getWorld().getBlockAt(x, y, z), player);
            Bukkit.getPluginManager().callEvent(breakev);
            if (breakev.isCancelled()) {
                return true;
            }
          }*/
          // MCPC end
          
          return false;
    }

	public boolean tryRip(World parWorld, int tryX, int tryY, int tryZ, boolean notify)
    {
        if (!ConfigMisc.Storm_Tornado_grabBlocks) return true;
        
        if (isNoDigCoord(tryX, tryY, tryZ)) return true;
        
        if (parWorld.isRemote)
        {
            int what = 0;
        }

        boolean seesLight = false;
        Block blockID = parWorld.getBlock(tryX, tryY, tryZ);

        //System.out.println(parWorld.getHeightValue(tryX, tryZ));
        if (( /*(canGrab(blockID)) &&blockID != 0 ||*/
                ((parWorld.getHeightValue(tryX, tryZ) - 1 == tryY) ||
                        parWorld.getHeightValue(tryX + 1, tryZ) - 1 < tryY ||
                        parWorld.getHeightValue(tryX, tryZ + 1) - 1 < tryY ||
                        parWorld.getHeightValue(tryX - 1, tryZ) - 1 < tryY ||
                        parWorld.getHeightValue(tryX, tryZ - 1) - 1 < tryY))
                /*(parWorld.getBlockId(tryX,tryY+1,tryZ) == 0 ||
                 parWorld.getBlockId(tryX+1,tryY,tryZ) == 0 ||
                 parWorld.getBlockId(tryX,tryY,tryZ+1) == 0 ||
                 parWorld.getBlockId(tryX-1,tryY,tryZ) == 0 ||
                 parWorld.getBlockId(tryX,tryY,tryZ-1) == 0 ||
                 parWorld.getBlockId(tryX+1,tryY+1,tryZ) == 0 ||
                 parWorld.getBlockId(tryX,tryY+1,tryZ+1) == 0 ||
                 parWorld.getBlockId(tryX-1,tryY+1,tryZ) == 0 ||
                 parWorld.getBlockId(tryX,tryY+1,tryZ-1) == 0)*/
           )
        {
            if (WeatherUtil.shouldRemoveBlock(blockID))
            {
                removeCount++;

                if (notify)
                {
                    parWorld.setBlock(tryX, tryY, tryZ, Blocks.air, 0, 3);
                }
                else
                {
                    parWorld.setBlock(tryX, tryY, tryZ, Blocks.air, 0, 0);
                }
            }

            if (parWorld.getChunkProvider().chunkExists((int)storm.pos.xCoord / 16, (int)storm.pos.zCoord / 16) && /*mod_EntMover.getFPS() > mod_EntMover.safetyCutOffFPS && */blockCount <= ConfigMisc.Storm_Tornado_maxBlocksPerStorm && lastGrabTime < System.currentTimeMillis() && tickGrabCount < ConfigMisc.Storm_Tornado_maxBlocksGrabbedPerTick)
            {
                lastGrabTime = System.currentTimeMillis() - 5;
                //int blockMeta = this.parWorld.getBlockMetadata(tryX,tryY,tryZ);
                //rip noise, nm, forces particles
                //parWorld.playAuxSFX(2001, tryX, tryY, tryZ, blockID + blockMeta * 256);

                if (blockID != Blocks.snow && blockID != Blocks.glass)
                {
                    EntityMovingBlock mBlock;

                    if (parWorld.getClosestPlayer(storm.posBaseFormationPos.xCoord, storm.posBaseFormationPos.yCoord, storm.posBaseFormationPos.zCoord, 140) != null) {
	                    if (blockID == Blocks.grass)
	                    {
	                        mBlock = new EntityMovingBlock(parWorld, tryX, tryY, tryZ, Blocks.dirt, storm);
	                    }
	                    else
	                    {
	                        mBlock = new EntityMovingBlock(parWorld, tryX, tryY, tryZ, blockID, storm);
	                    }
	                    
	                    blockCount++;
	                    
	                    //if (WeatherMod.debug && parWorld.getWorldTime() % 60 == 0) System.out.println("ripping, count: " + WeatherMod.blockCount);

	                    mBlock.setPosition(tryX, tryY, tryZ);
	                    
	                    if (!parWorld.isRemote)
	                    {
	                        parWorld.spawnEntityInWorld(mBlock);
	                    }

	                    //this.activeBlocks.add(mBlock);
	                    tickGrabCount++;
	                    ripCount++;

	                    if (ripCount % 10 == 0)
	                    {
	                        //System.out.println(ripCount);
	                    }
	                    else
	                    {
	                        //System.out.print(ripCount + " - ");
	                    }

	                    //mBlock.controller = this;
	                    mBlock.type = 0;
	                    seesLight = true;
                    }

                    
                }
                else
                {
                    //depreciated - OR NOT!
                    if (blockID == Blocks.glass)
                    {
                        parWorld.playSoundEffect(tryX, tryY, tryZ, "random.glass", 5.0F, 1.0F);
                    }

                    //break snow effect goes here
                    //mc.effectRenderer.addBlockDestroyEffects(tryX,tryY,tryZ, blockID, 0);
                }
            }
        }

        return seesLight;
    }

    public boolean canGrab(World parWorld, Block blockID)
    {
        if (!CoroUtilBlock.isAir(blockID) && WeatherUtil.shouldGrabBlock(parWorld, blockID))
        {
            return true;
        }

        return false;
    }
	
    public boolean forceRotate(World parWorld/*Entity entity*/)
    {
    	
    	//changed for weather2:
    	//canEntityBeSeen commented out till replaced with coord one, might cause issues
    	
        double dist = grabDist;
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(storm.pos.xCoord, storm.currentTopYBlock, storm.pos.zCoord, storm.pos.xCoord, storm.currentTopYBlock, storm.pos.zCoord);
        List list = parWorld.getEntitiesWithinAABB(Entity.class, aabb.expand(dist, this.storm.maxHeight * 3, dist));
        boolean foundEnt = false;
        int killCount = 0;

        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Entity entity1 = (Entity)list.get(i);

                if (/*(entity1 instanceof EntityLivingBase || entity1 instanceof EntityItem || entity1 instanceof MovingBlock) && */(!(entity1 instanceof EntityPlayer) || ConfigMisc.Storm_Tornado_grabPlayer)/* && entity1 != entity*/)
                {
                	/*if (parWorld.isRemote) {
                		if (entity1 instanceof EntityPlayer) {
                			System.out.println("client grab try: " + storm.posGround + " - " + getDistanceXZ(storm.posGround, entity1.posX, entity1.posY, entity1.posZ));
                		}
                	}*/
                    if (getDistanceXZ(storm.posBaseFormationPos, entity1.posX, entity1.posY, entity1.posZ) < dist)
                    {
                        if ((entity1 instanceof EntityMovingBlock && !((EntityMovingBlock)entity1).collideFalling)/* || canEntityBeSeen(entity, entity1)*/)
                        {
                        	storm.spinEntity(entity1);
                            //spin(entity, conf, entity1);
                            foundEnt = true;
                        } else {
                        	if (entity1 instanceof EntityPlayer) {
                        		//dont waste cpu on server side doing LOS checks, since player movement is client side only, in all situations ive seen
                        		//actually we need to still change its motion var, otherwise weird things happen
                        		//if (entity1.worldObj.isRemote) {
	                        		if (WeatherUtilEntity.isEntityOutside(entity1)) {
	                        			//Weather.dbg("entity1.motionY: " + entity1.motionY);
	                        			storm.spinEntity(entity1);
	                        			//spin(entity, conf, entity1);
	                                    foundEnt = true;
	                                    
	                                    //Weather.dbg("spin player! client side?: " + entity1.worldObj.isRemote);
	                        			
	                        		}
                        		//} else {
                        			
                        			//this should match the amount in spinEntity
                        			/*if (entity1.motionY > -0.8) {
                                        entity1.fallDistance = 0F;
                                    }*/
                        		//}
                        	} else if (entity1 instanceof EntityLivingBase && WeatherUtilEntity.isEntityOutside(entity1, true)) {//OldUtil.canVecSeeCoords(parWorld, storm.pos, entity1.posX, entity1.posY, entity1.posZ)/*OldUtil.canEntSeeCoords(entity1, entity.posX, entity.posY + 80, entity.posZ)*/) {
                        		//trying only server side to fix warp back issue (which might mean client and server are mismatching for some rules)
                        		//if (!entity1.worldObj.isRemote) {
	                        		storm.spinEntity(entity1);
	                        		//spin(entity, conf, entity1);
	                                foundEnt = true;
                        		//}
                        	}
                        }
                    }
                }

                if (entity1 instanceof EntityMovingBlock && !entity1.isDead)
                {
                    int var3 = MathHelper.floor_double(entity1.posX);
                    int var4 = MathHelper.floor_double(entity1.posZ);
                    byte var5 = 32;
                    /*if(!entity1.worldObj.checkChunksExist(var3 - var5, 0, var4 - var5, var3 + var5, 128, var4 + var5) || !entity1.addedToChunk) {
                        entity1.setEntityDead();
                        mod_EntMover.blockCount--;
                    }*/
                }

                /*if (entity instanceof EntTornado)
                {*/
                    if (entity1 instanceof EntityMovingBlock)
                    {
                        if (blockCount + 5 > ConfigMisc.Storm_Tornado_maxBlocksPerStorm)
                        {
                            if (entity1.posY > 255)
                            {
                                entity1.setDead();
                                //System.out.println(blockCount);
                            }
                        }

                        /*if (entity1.motionX < 0.3F && entity1.motionY < 0.3F && entity1.motionZ < 0.3F && getFPS() < 20 && killCount < 20)
                        {
                            killCount++;
                            entity1.setDead();
                        }*/
                    }
                //}

                //deactivated for weather2
                //if (entity1 instanceof EntityItem && player != null)
                //{
                    //if (entity1.getDistanceToEntity(player) > 32F)
                    //{
                        //if ((((EntityItem) entity).item.itemID) == Block.sand.blockID) {
                        //entity1.setDead();
                        //}
                    //}
                //}
            }
        }

        return foundEnt;
    }
    
    public double getDistanceXZ(Vec3 parVec, double var1, double var3, double var5)
    {
        double var7 = parVec.xCoord - var1;
        //double var9 = ent.posY - var3;
        double var11 = parVec.zCoord - var5;
        return (double)MathHelper.sqrt_double(var7 * var7/* + var9 * var9*/ + var11 * var11);
    }
    
    public double getDistanceXZ(Entity ent, double var1, double var3, double var5)
    {
        double var7 = ent.posX - var1;
        //double var9 = ent.posY - var3;
        double var11 = ent.posZ - var5;
        return (double)MathHelper.sqrt_double(var7 * var7/* + var9 * var9*/ + var11 * var11);
    }
    
    @SideOnly(Side.CLIENT)
    public void soundUpdates(boolean playFarSound, boolean playNearSound)
    {
    	
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	
        if (mc.thePlayer == null)
        {
            return;
        }

        //close sounds
        int far = 200;
        int close = 120;
        if (storm.stormType == storm.TYPE_WATER) {
        	close = 200;
        }
        Vec3 plPos = Vec3.createVectorHelper(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        
        double distToPlayer = this.storm.posGround.distanceTo(plPos);
        
        float volScaleFar = (float) ((far - distToPlayer/*this.getDistanceToEntity(mc.thePlayer)*/) / far);
        float volScaleClose = (float) ((close - distToPlayer/*this.getDistanceToEntity(mc.thePlayer)*/) / close);

        if (volScaleFar < 0F)
        {
            volScaleFar = 0.0F;
        }

        if (volScaleClose < 0F)
        {
            volScaleClose = 0.0F;
        }

        if (distToPlayer < close)
        {
            if (!lastTickPlayerClose)
            {
                /*this.soundTimer[0] = System.currentTimeMillis();
                this.soundTimer[1] = System.currentTimeMillis();
                tryPlaySound(snd_dmg_close, 0, mc.thePlayer, volScaleClose);
                tryPlaySound(snd_wind_close, 1, mc.thePlayer, volScaleClose);*/
            }

            lastTickPlayerClose = true;
            //tryPlaySound(snd_dmg_close[0], 0);
            //tryPlaySound(snd_dmg_close[0], 0);
        }
        else
        {
            lastTickPlayerClose = false;
        }

        if (distToPlayer < far)
        {
            if (playFarSound) tryPlaySound(WeatherUtilSound.snd_wind_far, 2, mc.thePlayer, volScaleFar, far);
            //tryPlaySound(snd_dmg_close[0], 0);
            //tryPlaySound(snd_dmg_close[0], 0);
            if (playNearSound) tryPlaySound(WeatherUtilSound.snd_wind_close, 1, mc.thePlayer, volScaleClose, close);

            if (storm.levelCurIntensityStage >= storm.STATE_FORMING && storm.stormType == storm.TYPE_LAND/*getStorm().type == getStorm().TYPE_TORNADO*/)
            {
                tryPlaySound(WeatherUtilSound.snd_dmg_close, 0, mc.thePlayer, volScaleClose, close);
            }
        }

        /*if (distToPlayer < far && WeatherUtilSound.lastSoundPositionUpdate < System.currentTimeMillis())
        {
            //System.out.println(sndSys);
            //int j = (int)(field.getFloat(item)
        	WeatherUtilSound.lastSoundPositionUpdate = System.currentTimeMillis() + 100;

            //float gameVol = mc.gameSettings.soundVolume;
            if (WeatherUtilSound.soundID[0] > -1 && WeatherUtilSound.soundTimer[0] < System.currentTimeMillis())
            {
            	WeatherUtilSound.setVolume(new StringBuilder().append("sound_" + WeatherUtilSound.soundID[0]).toString(), volScaleClose);
            }

            if (WeatherUtilSound.soundID[1] > -1 && WeatherUtilSound.soundTimer[1] < System.currentTimeMillis())
            {
            	WeatherUtilSound.setVolume(new StringBuilder().append("sound_" + WeatherUtilSound.soundID[1]).toString(), volScaleClose);
            }

            if (WeatherUtilSound.soundID[2] > -1 && WeatherUtilSound.soundTimer[2] < System.currentTimeMillis())
            {
            	WeatherUtilSound.setVolume(new StringBuilder().append("sound_" + WeatherUtilSound.soundID[2]).toString(), volScaleFar);
            }
        }*/

        //System.out.println(volScaleClose);
        //System.out.println(distToPlayer);
        //worldObj.playRecord("destruction2", (int)posX, (int)posY, (int)posZ);
        //worldObj.playSoundEffect(posX, posY, posZ, "tornado.destruction", 1F, 1.0F);
        //worldObj.playSoundAtEntity(mc.thePlayer, "tornado.destruction", 1.0F, 1.0F);
        //worldObj.playRecord("tornado.destruction", (int)mc.thePlayer.posX, (int)mc.thePlayer.posY, (int)mc.thePlayer.posZ);
        //mc.ingameGUI.recordPlayingUpFor = 0;
    }

    public boolean tryPlaySound(String[] sound, int arrIndex, Entity source, float vol, float parCutOffRange)
    {
        Entity soundTarget = source;

        Random rand = new Random();
        
        // should i?
        //soundTarget = this;
        if (WeatherUtilSound.soundTimer[arrIndex] <= System.currentTimeMillis())
        {
            //worldObj.playSoundAtEntity(soundTarget, new StringBuilder().append("tornado."+sound).toString(), 1.0F, 1.0F);
            //((IWorldAccess)this.worldAccesses.get(var5)).playSound(var2, var1.posX, var1.posY - (double)var1.yOffset, var1.posZ, var3, var4);
        	/*WeatherUtilSound.soundID[arrIndex] = */WeatherUtilSound.playMovingSound(storm, new StringBuilder().append(Weather.modID + ":streaming." + sound[WeatherUtilSound.snd_rand[arrIndex]]).toString(), vol, 1.0F, parCutOffRange);
            //this.soundID[arrIndex] = mod_EntMover.getLastSoundID();
            //System.out.println(new StringBuilder().append("tornado."+sound[snd_rand[arrIndex]]).toString());
            //System.out.println(soundToLength.get(sound[snd_rand[arrIndex]]));
            int length = (Integer)WeatherUtilSound.soundToLength.get(sound[WeatherUtilSound.snd_rand[arrIndex]]);
            //-500L, for blending
            WeatherUtilSound.soundTimer[arrIndex] = System.currentTimeMillis() + length - 500L;
            WeatherUtilSound.snd_rand[arrIndex] = rand.nextInt(3);
        }

        return false;
    }
}
