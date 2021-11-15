package CoroUtil.util;

import net.minecraft.world.phys.Vec3;

import java.util.Random;


public class CoroUtilParticle {

    public static Vec3[] rainPositions;
    public static int maxRainDrops = 2000;
    
    public static Random rand = new Random();
    
    static {
    	rainPositions = new Vec3[maxRainDrops];
        
        float range = 10F;
        
        for (int i = 0; i < maxRainDrops; i++) {
        	rainPositions[i] = new Vec3((rand.nextFloat() * range) - (range/2), (rand.nextFloat() * range/1) - (range/2), (rand.nextFloat() * range) - (range/2));
        }
    }
	
}
