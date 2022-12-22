package weather2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import weather2.WeatherBlocks;

public class WeatherMachineBlockEntity extends BlockEntity {
    public WeatherMachineBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(WeatherBlocks.BLOCK_ENTITY_DEFLECTOR.get(), p_155229_, p_155230_);
    }

    public static void tickHelper(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {

    }
}
