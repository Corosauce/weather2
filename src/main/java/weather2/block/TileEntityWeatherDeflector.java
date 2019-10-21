package weather2.block;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import weather2.ServerTickHandler;
import weather2.config.ConfigStorm;
import weather2.weathersystem.WeatherManagerServer;
import weather2.weathersystem.storm.StormObject;
import CoroUtil.util.Vec3;
import weather2.weathersystem.storm.WeatherObject;

public class TileEntityWeatherDeflector extends TileEntity implements ITickable
{

	//0 = kill storms, 1 = prevent block damage
	public int mode = 0;

	public static int MODE_KILLSTORMS = 0;
	public static int MODE_NOBLOCKDAMAGE = 1;

	@Override
	public void onLoad() {
		super.onLoad();
		maintainBlockDamageDeflect();
	}

	@Override
    public void tick()
    {
    	
    	if (!world.isRemote) {

    		if (mode == MODE_KILLSTORMS) {
				if (world.getGameTime() % 100 == 0) {
					WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(world.provider.getDimension());
					if (wm != null) {
						List<WeatherObject> storms = wm.getStormsAroundForDeflector(new Vec3(getPos().getX(), StormObject.layers.get(0), getPos().getZ()), ConfigStorm.Storm_Deflector_RadiusOfStormRemoval);

						for (int i = 0; i < storms.size(); i++) {
							WeatherObject storm = storms.get(i);

							if (storm != null) {
								wm.removeStormObject(storm.ID);
								wm.syncStormRemove(storm);
							}
						}
					}
				}
			}

			if (world.getGameTime() % 20 == 0) {
				maintainBlockDamageDeflect();
			}

    	}
    }

    public void maintainBlockDamageDeflect() {
		WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(world.provider.getDimension());
		if (wm != null) {
			if (mode == MODE_KILLSTORMS) {
				if (wm.getListWeatherBlockDamageDeflector().contains(getPos().toLong())) {
					wm.getListWeatherBlockDamageDeflector().remove(getPos().toLong());
				}
			} else if (mode == MODE_NOBLOCKDAMAGE) {
				if (!wm.getListWeatherBlockDamageDeflector().contains(getPos().toLong())) {
					wm.getListWeatherBlockDamageDeflector().add(getPos().toLong());
				}
			}
		}
	}

	public void rightClicked(World worldIn, BlockPos pos, BlockState state, PlayerEntity playerIn, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
		cycleMode();

		String modeMsg = "";

		if (mode == MODE_KILLSTORMS) {
			modeMsg = "Kill thunderstorms and deadlier";
		} else if (mode == MODE_NOBLOCKDAMAGE) {
			modeMsg = "Prevent block damage only";
			maintainBlockDamageDeflect();
		}

		playerIn.sendMessage(new StringTextComponent("Weather Deflector set to mode: " + modeMsg));
	}

	public void cycleMode() {
		mode++;

		if (mode > MODE_NOBLOCKDAMAGE) {
			mode = 0;
		}
	}

    public CompoundNBT write(CompoundNBT var1)
    {
    	var1.putInt("mode", mode);
        return super.write(var1);
    }

    public void read(CompoundNBT var1)
    {
        super.read(var1);
		mode = var1.getInt("mode");
    }

	@Override
	public void remove() {
		super.remove();

		if (!world.isRemote) {
			//always try to remove, incase they removed the block before the tick code could run after switching mode
			WeatherManagerServer wm = ServerTickHandler.lookupDimToWeatherMan.get(world.provider.getDimension());
			wm.getListWeatherBlockDamageDeflector().remove(getPos().toLong());
		}

	}
}

