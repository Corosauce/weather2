package CoroUtil.util;

import net.minecraft.util.math.vector.Vector3d;

import java.util.Random;


public class CoroUtilParticle {

    public static Vector3d[] rainPositions;
    public static int maxRainDrops = 2000;
    
    public static Random rand = new Random();
    
    static {
    	rainPositions = new Vector3d[maxRainDrops];
        
        float range = 10F;
        
        for (int i = 0; i < maxRainDrops; i++) {
        	rainPositions[i] = new Vector3d((rand.nextFloat() * range) - (range/2), (rand.nextFloat() * range/1) - (range/2), (rand.nextFloat() * range) - (range/2));
        }
    }
	
}
