package weather2.client.shaderstest;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by corosus on 25/05/17.
 */
public class MeshBufferManagerParticle {

    //for prebuffering allowed max
    public static int numInstances = 50000;

    private static HashMap<TextureAtlasSprite, InstancedMeshParticle> lookupParticleToMesh = new HashMap<>();

    public static void setupMeshForParticle(TextureAtlasSprite sprite) {

        //drawn in order of a U shape starting top left
        float[] positions = null;

        positions = new float[]{
                -0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f
        };

        float[] texCoords = null;

        texCoords = new float[]{
                sprite.getU0(), sprite.getV0(),
                sprite.getU0(), sprite.getV1(),
                sprite.getU1(), sprite.getV1(),
                sprite.getU1(), sprite.getV0()
        };


        int[] indices = new int[] {
                0, 1, 3, 3, 1, 2
        };

        InstancedMeshParticle mesh = new InstancedMeshParticle(positions, texCoords, indices, numInstances);

        if (!lookupParticleToMesh.containsKey(sprite)) {
            lookupParticleToMesh.put(sprite, mesh);
        } else {
            System.out.println("WARNING: duplicate entry attempt for particle sprite: " + sprite);
        }
    }

    public static void cleanup() {
        for (Map.Entry<TextureAtlasSprite, InstancedMeshParticle> entry : lookupParticleToMesh.entrySet()) {
            entry.getValue().cleanup();
        }
        lookupParticleToMesh.clear();
    }

    public static InstancedMeshParticle getMesh(TextureAtlasSprite sprite) {
        return lookupParticleToMesh.get(sprite);
    }

    public static void setupMeshForParticleIfMissing(TextureAtlasSprite sprite) {
        if (sprite == null) return;
        if (!lookupParticleToMesh.containsKey(sprite)) {
            setupMeshForParticle(sprite);
        }
    }
}
