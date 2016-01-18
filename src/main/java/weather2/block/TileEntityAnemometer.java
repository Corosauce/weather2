package weather2.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.api.WindReader;
import weather2.util.WeatherUtilEntity;

public class TileEntityAnemometer extends TileEntity
{
	
	//since client receives data every couple seconds, we need to smooth out everything for best visual
	
	public float smoothAngle = 0;
	public float smoothAnglePrev = 0;
	public float smoothSpeed = 0;
	
	public float smoothAngleRotationalVel = 0;
	public float smoothAngleRotationalVelAccel = 0;
	
	public float smoothAngleAdj = 0.1F;
	public float smoothSpeedAdj = 0.1F;
	
	public boolean isOutsideCached = false;

    public void updateEntity()
    {
    	if (worldObj.isRemote) {
    		
    		if (worldObj.getTotalWorldTime() % 40 == 0) {
    			isOutsideCached = WeatherUtilEntity.isPosOutside(worldObj, new Vec3(getPos().getX()+0.5F, getPos().getY()+0.5F, getPos().getZ()+0.5F));
    		}
    		
    		if (isOutsideCached) {
	    		//x1 * y2 - y1 * x2 cross product to get optimal turn angle, errr i have angle and target angle, not positions...
	    		
	    		//smoothAngle = 0;
	    		//smoothAngleRotationalVel = 0;
	    		//smoothAngleRotationalVelAccel = 0;
	    		
	    		float targetAngle = WindReader.getWindAngle(worldObj, new Vec3(getPos().getX(), getPos().getY(), getPos().getZ()));
	    		float windSpeed = WindReader.getWindSpeed(worldObj, new Vec3(getPos().getX(), getPos().getY(), getPos().getZ()));
	    		
	    		smoothAngleRotationalVel += windSpeed * 1F;
	    		
	    		//Weather.dbg("smoothAngleRotationalVel: " + smoothAngleRotationalVel);
	    		
	    		if (smoothAngleRotationalVel > 50F) smoothAngleRotationalVel = 50F;
	    		
	    		if (smoothAngle >= 180) smoothAngle -= 360;
	    		if (smoothAnglePrev >= 180) smoothAnglePrev -= 360;
	    		
	    		
	    		
	    		
	    		
    		}
    		
    		smoothAnglePrev = smoothAngle;
    		smoothAngle += smoothAngleRotationalVel;
    		smoothAngleRotationalVel -= 0.1F;
    		
    		smoothAngleRotationalVel *= 0.97F;
    		
    		if (smoothAngleRotationalVel <= 0) smoothAngleRotationalVel = 0;
    	}
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
    	return AxisAlignedBB.getBoundingBox(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 3, getPos().getZ() + 1);
    }

    public void writeToNBT(NBTTagCompound var1)
    {
        super.writeToNBT(var1);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);

    }
}
