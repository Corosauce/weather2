package weather2.client.shaderstest;

import com.mojang.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Cloud {

    public Vector3f pos;
    public CloudNode cloudNode;

    public Cloud(Vector3f pos) {
        this.pos = pos;
        cloudNode = new CloudNode(this,null, new Vector3f(0, 0, 0));
    }

    public void tick() {
        cloudNode.tick();
    }

}
