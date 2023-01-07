package weather2.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import weather2.ServerTickHandler;
import weather2.WeatherBlocks;
import weather2.blockentity.DeflectorBlockEntity;
import weather2.weathersystem.WeatherManagerServer;

public class ForecastBlock extends Block {

    public ForecastBlock(Properties p_49224_) {
        super(p_49224_);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {

        if (!p_60504_.isClientSide) {
            WeatherManagerServer wm = ServerTickHandler.getWeatherManagerFor(p_60506_.getLevel().dimension());
            float chance = wm.getBiomeBasedStormSpawnChanceInArea(new BlockPos(p_60506_.blockPosition()));

            p_60506_.sendMessage(new TextComponent(String.format("Likelyhood of storms to spawn here within 1024 blocks: %.2f", (chance * 100F)) + "%"), p_60506_.getUUID());
        }

        return InteractionResult.CONSUME;
    }
}
