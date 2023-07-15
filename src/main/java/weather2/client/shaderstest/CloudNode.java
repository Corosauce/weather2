package weather2.client.shaderstest;

import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import weather2.ClientTickHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CloudNode {

    //null if its the first
    public Cloud cloud;
    public CloudNode parent;
    public List<CloudNode> listNodes = new ArrayList<>();
    public CloudPiece cloudPiece;

    //direction and distance this node extends from its parent
    public Vector3f direction;

    //extends direction
    public float length;
    public float lengthMax = 5;

    public int depth = 0;

    //expands from direction
    public float size = 0;
    public float sizeMax = 5;

    public static int depth_max = 6;

    public CloudNode(Cloud cloud, CloudNode parent, Vector3f direction) {
        this.cloud = cloud;
        this.parent = parent;
        this.direction = direction;
        if (parent != null) {
            depth = parent.depth + 1;
        }
    }

    public Vector3f getWorldPosition() {
        Vector3f parentPos;
        if (parent == null) {
            parentPos = cloud.pos;
        } else {
            parentPos = parent.getWorldPosition();
        }
        Vector3f pos = parentPos.copy();
        Vector3f dir = direction.copy();
        dir.mul(length);
        pos.add(dir);
        //grid align it
        pos.set((float)Math.floor(pos.x()), (float)Math.floor(pos.y()), (float)Math.floor(pos.z()));
        return pos;
    }

    public void tick() {

        sizeMax = 5;
        sizeMax = (depth_max - depth) * 2;
        lengthMax = 5;

        for (CloudNode node : listNodes) {
            node.tick();
        }

        if (depth < depth_max) {
            Random rand = new Random();
            //for (int i = 0; i < 3; i++) {
            while (listNodes.size() < 3) {
                CloudNode node = new CloudNode(cloud, this, new Vector3f(rand.nextFloat() - rand.nextFloat(), rand.nextFloat() - rand.nextFloat(), rand.nextFloat() - rand.nextFloat()));
                listNodes.add(node);
            }
        }

        long time = Minecraft.getInstance().level.getGameTime();
        if (time % 20 == 0) {
            if (length < lengthMax) {
                length += 0.3F;
            }
            if (size < sizeMax) {
                size += 0.3F;
            }
            Vector3f worldPos = getWorldPosition();
            //System.out.println(this + ": " + worldPos);

            if (cloudPiece == null) {
                cloudPiece = new CloudPiece();
                ClientTickHandler.weatherManager.cloudManager.listCloudPieces.add(cloudPiece);
            } else {
                cloudPiece.posX = worldPos.x() + 0.5F;
                cloudPiece.posY = worldPos.y() + 0.5F;
                cloudPiece.posZ = worldPos.z() + 0.5F;
                cloudPiece.scale = size;
            }

            /*for (int x = (int) -size; x < size; x++) {
                for (int z = (int) -size; z < size; z++) {
                    for (int y = (int) -size; y < size; y++) {
                        int xx = (int) Math.floor(worldPos.x() + x);
                        int yy = (int) Math.floor(worldPos.y() + y);
                        int zz = (int) Math.floor(worldPos.z() + z);
                        CloudPiece cloudPiece = ClientTickHandler.weatherManager.cloudManager.getOrCreateCloudPieceAtPosition(xx, yy, zz);
                        cloudPiece.posX = xx;
                        cloudPiece.posY = yy;
                        cloudPiece.posZ = zz;
                    }
                }
            }*/
        }
    }
}
