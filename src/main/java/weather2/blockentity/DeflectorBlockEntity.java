package weather2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import weather2.WeatherBlocks;

public class DeflectorBlockEntity extends BlockEntity/* implements BlockEntityTicker*/ {
    public DeflectorBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(WeatherBlocks.BLOCK_ENTITY_DEFLECTOR.get(), p_155229_, p_155230_);
    }

    public static void tick(Level p_155181_, BlockPos p_155182_, BlockState p_155183_, BlockEntity p_155184_) {
        System.out.println("tick!");
    }

    /*@Override
    public void tick(Level p_155253_, BlockPos p_155254_, BlockState p_155255_, BlockEntity p_155256_) {

    }*/
}
