package weather2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.List;
import java.util.Random;

public class PerlinNoiseHelper {

    public static class NoiseParameters {
        private final int firstOctave;
        private final DoubleList amplitudes;
        public static final Codec<NoiseParameters> CODEC = RecordCodecBuilder.create((p_48510_) -> {
            return p_48510_.group(Codec.INT.fieldOf("firstOctave").forGetter(NoiseParameters::firstOctave), Codec.DOUBLE.listOf().fieldOf("amplitudes").forGetter(NoiseParameters::amplitudes)).apply(p_48510_, NoiseParameters::new);
        });

        public NoiseParameters(int p_48506_, List<Double> p_48507_) {
            this.firstOctave = p_48506_;
            this.amplitudes = new DoubleArrayList(p_48507_);
        }

        public NoiseParameters(int p_151854_, double... p_151855_) {
            this.firstOctave = p_151854_;
            this.amplitudes = new DoubleArrayList(p_151855_);
        }

        public int firstOctave() {
            return this.firstOctave;
        }

        public DoubleList amplitudes() {
            return this.amplitudes;
        }
    }

    private SimplexNoise simplexNoise;
    private PerlinNoise perlinNoise;
    private NormalNoise normalNoise;

    public SimplexNoise getSimplexNoise() {
        return simplexNoise;
    }

    public PerlinNoise getPerlinNoise() {
        return perlinNoise;
    }

    public NormalNoise getNormalNoise() {
        return normalNoise;
    }

    private static PerlinNoiseHelper instance = null;

    public static PerlinNoiseHelper get() {
        if (instance == null) instance = new PerlinNoiseHelper();
        return instance;
    }

    public PerlinNoiseHelper() {
        Random random = new Random(5);
        simplexNoise = new SimplexNoise(new LegacyRandomSource(random.nextLong()));
        NoiseParameters noiseParameters = new NoiseParameters(-9, 1.0D, 0.0D, 3.0D, 3.0D, 3.0D, 3.0D);
        this.perlinNoise = PerlinNoise.create(new LegacyRandomSource(random.nextLong()), noiseParameters.firstOctave(), noiseParameters.amplitudes());
    }

}
