package weather2.client.shaderstest;

import java.util.ArrayList;
import java.util.List;

public class Cloud {

    public List<CloudPiece> listClouds = new ArrayList<>();

    public void tick() {
        int index = 0;

        for (CloudPiece cloudPiece : listClouds) {
            float rate = (float)index / (float)listClouds.size();

            cloudPiece.prevRotX = cloudPiece.rotX;
            //cloudPiece.rotX = 0;
            cloudPiece.rotX = (cloudPiece.rotX + 5F * rate);
            index++;
        }
    }

}
