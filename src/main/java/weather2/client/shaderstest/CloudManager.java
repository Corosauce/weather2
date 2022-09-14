package weather2.client.shaderstest;

import com.mojang.math.Vector3f;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import weather2.ClientTickHandler;

import java.util.*;

public class CloudManager {

    public List<Cloud> listClouds = new ArrayList<>();
    public List<CloudPiece> listCloudPieces = new ArrayList<>();

    private SimplexNoise simplexNoise;
    private PerlinNoise perlinNoise;
    private NormalNoise normalNoise;

    //private static CloudManager cloudManager = null;

    public HashMap<Long, CloudPiece> lookupPosToCloudPiece = new HashMap<>();

    public CloudManager() {
        Random random = new Random(5);
        simplexNoise = new SimplexNoise(new LegacyRandomSource(random.nextLong()));

        CloudManager.NoiseParameters noiseParameters = new CloudManager.NoiseParameters(-9, 1.0D, 0.0D, 3.0D, 3.0D, 3.0D, 3.0D);

        perlinNoise = PerlinNoise.create(new LegacyRandomSource(random.nextLong()), noiseParameters.firstOctave(), noiseParameters.amplitudes());
        //normalNoise = NormalNoise.create(new LegacyRandomSource(random.nextLong()), noiseParameters.firstOctave(), noiseParameters.amplitudes());
    }

    /*public static CloudManager getInstance() {
        if (cloudManager == null) cloudManager = new CloudManager();
        return cloudManager;
    }*/

    public SimplexNoise getSimplexNoise() {
        return simplexNoise;
    }

    public PerlinNoise getPerlinNoise() {
        return perlinNoise;
    }

    public NormalNoise getNormalNoise() {
        return normalNoise;
    }

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

    public CloudPiece getOrCreateCloudPieceAtPosition(int x, int y, int z) {
        long hash = BlockPos.asLong(x, y, z);
        CloudPiece cloudPiece = lookupPosToCloudPiece.get(hash);
        if (cloudPiece == null) {
            cloudPiece = new CloudPiece();
            lookupPosToCloudPiece.put(hash, cloudPiece);
            listCloudPieces.add(cloudPiece);
        }
        return cloudPiece;
    }

    public void tick() {

        int index = 0;

        /*Iterator<Map.Entry<Long, CloudPiece>> it = ClientTickHandler.weatherManager.cloudManager.getLookupPosToCloudPiece().entrySet().iterator();
        while (it.hasNext()) {
            CloudPiece cloudPiece = it.next().getValue();*/
        for (CloudPiece cloudPiece : ClientTickHandler.weatherManager.cloudManager.listCloudPieces) {
            float rate = 0;//(float)index / (float) listCloudPieces.size();
            //rate = (float)index / (float)1000;
            //rate = 1;
            rate = 0.3F + (float)(cloudPiece.posY-60) / (float)150;

            if (index == 25000) {
                //System.out.println("wat");
            }

            cloudPiece.prevRotX = cloudPiece.rotX;
            //cloudPiece.rotX = 0;
            //cloudPiece.rotX = (cloudPiece.rotX + 1F);
            //cloudPiece.rotX = (cloudPiece.rotX + 3F * rate);
            index++;
            cloudPiece.tick();
        }

        Random random = new Random();
        while (listClouds.size() < 10) {
            if (Minecraft.getInstance().player != null) {
                LocalPlayer player = Minecraft.getInstance().player;
                int range = 80;
                int x = random.nextInt(range) - random.nextInt(range);
                int y = random.nextInt(range) - random.nextInt(range);
                int z = random.nextInt(range) - random.nextInt(range);
                Cloud cloud = new Cloud(new Vector3f((float)player.getX() + x, (float)player.getY() + y, (float)player.getZ() + z));
                listClouds.add(cloud);
            }
        }

        for (Cloud cloud : listClouds) {
            cloud.tick();
        }
    }

    public HashMap<Long, CloudPiece> getLookupPosToCloudPiece() {
        return lookupPosToCloudPiece;
    }
}
