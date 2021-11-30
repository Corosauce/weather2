package weather2.client.shaderstest;

import java.util.ArrayList;
import java.util.List;

public class Cloud {

    public List<CloudPiece> listClouds = new ArrayList<>();

    public void tick() {
        int index = 0;

        for (CloudPiece cloudPiece : listClouds) {
            float rate = (float)index / (float)listClouds.size();
            //rate = (float)index / (float)1000;
            //rate = 1;
            rate = 0.3F + (float)(cloudPiece.posY-60) / (float)150;

            if (index == 25000) {
                //System.out.println("wat");
            }

            cloudPiece.prevRotX = cloudPiece.rotX;
            //cloudPiece.rotX = 0;
            //cloudPiece.rotX = (cloudPiece.rotX + 1F);
            cloudPiece.rotX = (cloudPiece.rotX + 3F * rate);
            index++;
            cloudPiece.tick();
        }
    }

}
