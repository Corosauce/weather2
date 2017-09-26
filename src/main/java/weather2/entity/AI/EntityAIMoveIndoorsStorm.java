package weather2.entity.AI;

import CoroUtil.ai.ITaskInitializer;
import CoroUtil.util.CoroUtilPhysics;
import CoroUtil.util.Vec3;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;
import weather2.ServerTickHandler;
import weather2.config.ConfigMisc;
import weather2.weathersystem.WeatherManagerBase;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

import java.util.List;

/**
 * Based off of EntityAIMoveIndoors
 *
 * If global overcast is on, this probably isnt needed as original task executes on global rain active
 *
 * Inject with same priority as original task, do not override original task
 */
public class EntityAIMoveIndoorsStorm extends EntityAIBase implements ITaskInitializer
{
    private EntityCreature entityObj;
    private VillageDoorInfo doorInfo;
    private int insidePosX = -1;
    private int insidePosZ = -1;

    public EntityAIMoveIndoorsStorm() {
        this.setMutexBits(1);
    }

    public EntityAIMoveIndoorsStorm(EntityCreature entityObjIn)
    {
        this();
        this.entityObj = entityObjIn;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {

        WeatherManagerBase weatherManager = ServerTickHandler.getWeatherSystemForDim(entityObj.world.provider.getDimension());
        if (weatherManager == null) return false;

        BlockPos blockpos = new BlockPos(this.entityObj);
        Vec3 pos = new Vec3(blockpos);

        boolean runInside = false;
        if (!this.entityObj.world.isDaytime()) {
            runInside = true;
        } else {
            StormObject so = weatherManager.getClosestStorm(pos, ConfigMisc.Villager_MoveInsideForStorms_Dist, StormObject.STATE_THUNDER);

            if (so != null) {
                runInside = true;
            } else {
                //sandstorms check
                WeatherObjectSandstorm sandstorm = weatherManager.getClosestSandstormByIntensity(pos);

                if (sandstorm != null) {
                    List<Vec3> points = sandstorm.getSandstormAsShape();

                    if (CoroUtilPhysics.getDistanceToShape(pos, points) < ConfigMisc.Villager_MoveInsideForStorms_Dist) {
                        runInside = true;
                    }
                }
            }
        }

        if (runInside)
        {
            /*if (this.entityObj.getRNG().nextInt(10) != 0)
            {
                return false;
            }
            else */
            //if villager is right next to its safe spot, cancel
            if (this.insidePosX != -1 && this.entityObj.getDistanceSq((double)this.insidePosX, this.entityObj.posY, (double)this.insidePosZ) < 4.0D)
            {
                return false;
            }
            else
            {
                Village village = this.entityObj.world.getVillageCollection().getNearestVillage(blockpos, 14);

                if (village == null)
                {
                    return false;
                }
                else
                {
                    this.doorInfo = village.getDoorInfo(blockpos);
                    return this.doorInfo != null;
                }
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */

    @Override
    public boolean shouldContinueExecuting()
    {
        return !this.entityObj.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.insidePosX = -1;
        BlockPos blockpos = this.doorInfo.getInsideBlockPos();
        int i = blockpos.getX();
        int j = blockpos.getY();
        int k = blockpos.getZ();

        if (this.entityObj.getDistanceSq(blockpos) > 256.0D)
        {
            Vec3d vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(this.entityObj, 14, 3, new Vec3d((double)i + 0.5D, (double)j, (double)k + 0.5D));

            if (vec3d != null)
            {
                this.entityObj.getNavigator().tryMoveToXYZ(vec3d.x, vec3d.y, vec3d.z, 1.0D);
            }
        }
        else
        {
            this.entityObj.getNavigator().tryMoveToXYZ((double)i + 0.5D, (double)j, (double)k + 0.5D, 1.0D);
        }
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask()
    {
        this.insidePosX = this.doorInfo.getInsideBlockPos().getX();
        this.insidePosZ = this.doorInfo.getInsideBlockPos().getZ();
        this.doorInfo = null;
    }

    @Override
    public void setEntity(EntityCreature creature) {
        this.entityObj = creature;
    }
}