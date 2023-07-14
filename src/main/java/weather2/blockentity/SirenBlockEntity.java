package weather2.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.WeatherBlocks;
import weather2.config.ConfigMisc;
import weather2.config.ConfigSand;
import weather2.util.WeatherUtilSound;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.storm.WeatherObjectParticleStorm;

public class SirenBlockEntity extends BlockEntity {

    public long lastPlayTime = 0L;

    public SirenBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(WeatherBlocks.BLOCK_ENTITY_TORNADO_SIREN.get(), p_155229_, p_155230_);
    }

    public static void tickHelper(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        ((SirenBlockEntity)blockEntity).tick();
    }

    public void tick() {
        if (level.isClientSide()) {
            tickClient();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient() {
        if (this.lastPlayTime < System.currentTimeMillis())
        {
            Vec3 pos = new Vec3(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());

            StormObject so = ClientTickHandler.weatherManager.getClosestStorm(pos, ConfigMisc.sirenActivateDistance, StormObject.STATE_FORMING);

            if (so != null)
            {
                this.lastPlayTime = System.currentTimeMillis() + 13000L;
                WeatherUtilSound.playNonMovingSound(pos, "streaming.siren", 1.0F, 1.0F, 120);
            } else {
                if (!ConfigSand.Sandstorm_Siren_PleaseNoDarude) {
                    WeatherObjectParticleStorm storm = ClientTickHandler.weatherManager.getClosestParticleStormByIntensity(pos, WeatherObjectParticleStorm.StormType.SANDSTORM);
                    if (storm == null) {
                        storm = ClientTickHandler.weatherManager.getClosestParticleStormByIntensity(pos, WeatherObjectParticleStorm.StormType.SNOWSTORM);
                    }

                    if (storm != null) {

                        if (pos.distanceTo(storm.pos) < storm.getSize()) {
                            String soundToPlay = "siren_sandstorm_5_extra";
                            if (level.random.nextBoolean()) {// ATTENTION
                                soundToPlay = "siren_sandstorm_6_extra";
                            }

                            float distScale = Math.max(0.1F, 1F - (float) ((pos.distanceTo(storm.pos)) / storm.getSize()));

                            this.lastPlayTime = System.currentTimeMillis() + 15000L;//WeatherUtilSound.soundToLength.get(soundToPlay) - 500L;
                            WeatherUtilSound.playNonMovingSound(pos, "streaming." + soundToPlay, 1F, distScale, storm.getSize());
                        }
                    }
                }
            }
        }
    }
}
