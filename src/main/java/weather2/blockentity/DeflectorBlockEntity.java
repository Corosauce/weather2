package weather2.blockentity;

import com.corosus.coroutil.util.CULog;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import weather2.ServerTickHandler;
import weather2.WeatherBlocks;

public class DeflectorBlockEntity extends BlockEntity {

    private boolean needsInit = true;

    public DeflectorBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(WeatherBlocks.BLOCK_ENTITY_DEFLECTOR.get(), p_155229_, p_155230_);
    }

    public static void tickHelper(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        ((DeflectorBlockEntity)blockEntity).tick();
    }

    public void tick() {
        if (needsInit) {
            needsInit = false;
            init();
        }
    }

    public void init() {
        if (!level.isClientSide()) {
            CULog.dbg("adding weather deflector poi at " + getBlockPos());
            ServerTickHandler.getWeatherManagerFor(level).registerDeflector(getBlockPos());
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        ServerTickHandler.getWeatherManagerFor(level).removeDeflector(getBlockPos());
    }
}
