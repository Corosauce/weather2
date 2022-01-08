package weather2.mixin.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FireBlock.class)
public abstract class FireToCharcoal {

    @Redirect(method = "tryCatchFire",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    public boolean removeBlock(Level instance, BlockPos p_46623_, boolean p_46624_) {
        System.out.println("hooked remove block with fire!");
        BlockState state = instance.getBlockState(p_46623_);
        if (state.getMaterial() == Material.WOOD) {
            return instance.setBlock(p_46623_, Blocks.BLACKSTONE_WALL.defaultBlockState(), 3 | (p_46624_ ? 64 : 0));
        } else {
            return instance.removeBlock(p_46623_, p_46624_);
        }
    }

    @Redirect(method = "tryCatchFire",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    public boolean setBlock(Level instance, BlockPos p_46601_, BlockState p_46602_, int p_46603_) {
        System.out.println("hooked remove block with fire!");
        BlockState state = instance.getBlockState(p_46601_);
        if (state.getMaterial() == Material.WOOD) {
            return instance.setBlock(p_46601_, Blocks.BLACKSTONE_WALL.defaultBlockState(), p_46603_);
        } else {
            return instance.removeBlock(p_46601_, false);
        }
    }
}