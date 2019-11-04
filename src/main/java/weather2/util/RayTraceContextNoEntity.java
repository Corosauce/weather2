package weather2.util;

import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class RayTraceContextNoEntity {
   private final Vec3d startVec;
   private final Vec3d endVec;
   private final RayTraceContextNoEntity.BlockMode blockMode;
   private final RayTraceContextNoEntity.FluidMode fluidMode;
   private final ISelectionContext context;

   public RayTraceContextNoEntity(Vec3d p_i50009_1_, Vec3d p_i50009_2_, RayTraceContextNoEntity.BlockMode blockModeIn, RayTraceContextNoEntity.FluidMode fluidModeIn) {
      this.startVec = p_i50009_1_;
      this.endVec = p_i50009_2_;
      this.blockMode = blockModeIn;
      this.fluidMode = fluidModeIn;
      this.context = ISelectionContext.dummy();
   }

   public Vec3d func_222250_a() {
      return this.endVec;
   }

   public Vec3d func_222253_b() {
      return this.startVec;
   }

   public VoxelShape getBlockShape(BlockState p_222251_1_, IBlockReader p_222251_2_, BlockPos p_222251_3_) {
      return this.blockMode.get(p_222251_1_, p_222251_2_, p_222251_3_, this.context);
   }

   public VoxelShape getFluidShape(IFluidState p_222252_1_, IBlockReader p_222252_2_, BlockPos p_222252_3_) {
      return this.fluidMode.test(p_222252_1_) ? p_222252_1_.getShape(p_222252_2_, p_222252_3_) : VoxelShapes.empty();
   }

   public static enum BlockMode implements RayTraceContextNoEntity.IVoxelProvider {
      COLLIDER(BlockState::getCollisionShape),
      OUTLINE(BlockState::getShape);

      private final RayTraceContextNoEntity.IVoxelProvider provider;

      private BlockMode(RayTraceContextNoEntity.IVoxelProvider providerIn) {
         this.provider = providerIn;
      }

      public VoxelShape get(BlockState p_get_1_, IBlockReader p_get_2_, BlockPos p_get_3_, ISelectionContext p_get_4_) {
         return this.provider.get(p_get_1_, p_get_2_, p_get_3_, p_get_4_);
      }
   }

   public static enum FluidMode {
      NONE((p_222247_0_) -> {
         return false;
      }),
      SOURCE_ONLY(IFluidState::isSource),
      ANY((p_222246_0_) -> {
         return !p_222246_0_.isEmpty();
      });

      private final Predicate<IFluidState> fluidTest;

      private FluidMode(Predicate<IFluidState> fluidTestIn) {
         this.fluidTest = fluidTestIn;
      }

      public boolean test(IFluidState state) {
         return this.fluidTest.test(state);
      }
   }

   public interface IVoxelProvider {
      VoxelShape get(BlockState p_get_1_, IBlockReader p_get_2_, BlockPos p_get_3_, ISelectionContext p_get_4_);
   }
}