package weather2.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class SandLayerBlock extends Block {
   public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
   protected static final VoxelShape[] SHAPES = new VoxelShape[]{Shapes.empty(), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

   public SandLayerBlock(Properties properties) {
      super(properties);
      this.registerDefaultState(this.stateDefinition.any().setValue(LAYERS, Integer.valueOf(1)));
   }

   public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
      switch(type) {
      case LAND:
         return state.getValue(LAYERS) < 5;
      case WATER:
         return false;
      case AIR:
         return false;
      default:
         return false;
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(LAYERS)];
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(LAYERS) - 1];
   }

   public VoxelShape getBlockSupportShape(BlockState state, BlockGetter reader, BlockPos pos) {
      return SHAPES[state.getValue(LAYERS)];
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
      return SHAPES[state.getValue(LAYERS)];
   }

   public boolean useShapeForLightOcclusion(BlockState state) {
      return true;
   }

   public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
      BlockState blockstate = worldIn.getBlockState(pos.below());
      if (!blockstate.is(Blocks.ICE) && !blockstate.is(Blocks.PACKED_ICE) && !blockstate.is(Blocks.BARRIER)) {
         if (!blockstate.is(Blocks.HONEY_BLOCK) && !blockstate.is(Blocks.SOUL_SAND)) {
            return Block.isFaceFull(blockstate.getCollisionShape(worldIn, pos.below()), Direction.UP) || blockstate.getBlock() == this && blockstate.getValue(LAYERS) == 8;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
      return !stateIn.canSurvive(worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
      if (worldIn.getBrightness(LightLayer.BLOCK, pos) > 11) {
         dropResources(state, worldIn, pos);
         worldIn.removeBlock(pos, false);
      }

   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
      int i = state.getValue(LAYERS);
      return i == 1;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState blockstate = context.getLevel().getBlockState(context.getClickedPos());
      if (blockstate.is(this)) {
         int i = blockstate.getValue(LAYERS);
         return blockstate.setValue(LAYERS, Integer.valueOf(Math.min(8, i + 1)));
      } else {
         return super.getStateForPlacement(context);
      }
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(LAYERS);
   }
}