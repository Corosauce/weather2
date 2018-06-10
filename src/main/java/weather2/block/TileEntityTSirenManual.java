package weather2.block;

import CoroUtil.util.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.CommonProxy;
import weather2.util.WeatherUtilSound;

import java.util.List;

public class TileEntityTSirenManual extends TileEntity implements ITickable
{
    public long lastPlayTime = 0L;

    @Override
    public void update()
    {
    	if (world.isRemote) {
    	    int meta = CommonProxy.blockTSiren.getMetaFromState(this.world.getBlockState(this.getPos()));
            if (BlockTSiren.isEnabled(meta)) {
                //System.out.println("enabled");
                tickClient();
            }

    	}
    }
    
    @SideOnly(Side.CLIENT)
    public void tickClient() {
    	
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	
    	if (this.lastPlayTime < System.currentTimeMillis())
        {
            Vec3 pos = new Vec3(getPos().getX(), getPos().getY(), getPos().getZ());

            this.lastPlayTime = System.currentTimeMillis() + 13000L;
            WeatherUtilSound.playNonMovingSound(pos, "streaming.siren", 1.0F, 1.0F, 120);

        }
    }
    
}
