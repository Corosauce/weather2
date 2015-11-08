package weather2;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.grid.block.BlockDataPoint;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandlerForge {

	@SubscribeEvent
	public void worldSave(Save event) {
		Weather.writeOutData(false);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
		ClientTickHandler.checkClientWeather();
		ClientTickHandler.weatherManager.tickRender(event.partialTicks);
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent event) {
		if (event.map.getTextureType() == 1) {
			ClientProxy.radarIconRain = event.map.registerIcon(Weather.modID + ":radar/radarIconRain");
			ClientProxy.radarIconLightning = event.map.registerIcon(Weather.modID + ":radar/radarIconLightning");
			ClientProxy.radarIconWind = event.map.registerIcon(Weather.modID + ":radar/radarIconWind");
			ClientProxy.radarIconHail = event.map.registerIcon(Weather.modID + ":radar/radarIconHail");
			ClientProxy.radarIconTornado = event.map.registerIcon(Weather.modID + ":radar/radarIconTornado");
			ClientProxy.radarIconCyclone = event.map.registerIcon(Weather.modID + ":radar/radarIconCyclone");
		}
	}
	
	@SubscribeEvent
	public void entityTick(LivingUpdateEvent event) {
		EntityLivingBase ent = event.entityLiving;
		int walkOnRate = 5;
		
		if (!ent.worldObj.isRemote) {
			if (ent.worldObj.getTotalWorldTime() % walkOnRate == 0) {
				double speed = Math.sqrt(ent.motionX * ent.motionX + ent.motionY * ent.motionY + ent.motionZ * ent.motionZ);
				if (speed > 0.08) {
					//System.out.println(entityId + " - speed: " + speed);
					int newX = MathHelper.floor_double(ent.posX);
					int newY = MathHelper.floor_double(ent.boundingBox.minY - 1);
					int newZ = MathHelper.floor_double(ent.posZ);
					Block id = ent.worldObj.getBlock(newX, newY, newZ);
					
					//check for block that can have beaten path data
					
					if (id == Blocks.grass) {
						BlockDataPoint bdp = WorldDirectorManager.instance().getBlockDataGrid(ent.worldObj).getBlockData(newX, newY, newZ);// ServerTickHandler.wd.getBlockDataGrid(worldObj).getBlockData(newX, newY, newZ);
						
						//add depending on a weight?
						bdp.walkedOnAmount += 0.25F;
						
						System.out.println("inc walk amount: " + bdp.walkedOnAmount);
						
						if (bdp.walkedOnAmount > 5F) {
							//System.out.println("dirt!!!");
							ent.worldObj.setBlock(newX, newY, newZ, Blocks.dirt);//BlockRegistry.dirtPath.blockID);
							//cleanup for memory
							WorldDirectorManager.instance().getBlockDataGrid(ent.worldObj).removeBlockData(newX, newY, newZ);
							//ServerTickHandler.wd.getBlockDataGrid(worldObj).removeBlockData(newX, newY, newZ);
						}
					}
				}
			}
		}
	}
}
