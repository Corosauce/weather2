package weather2.util;

import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Full of repurposed stack overflow examples
 * 
 * @author Corosus
 *
 */
public class WeatherUtilPhysics {

    /**
     * Return true if the given point is contained inside the boundary.
     * See: http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
     * @param test The point to check
     * @return true if the point is inside the boundary, false otherwise
     *
     */
    public static boolean isInConvexShape(Vec3 test, List<Vec3> points) {
    	int i;
    	int j;
    	boolean result = false;
    	for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
    		Vec3 vecI = points.get(i);
    		Vec3 vecJ = points.get(j);
    		if ((vecI.z > test.z) != (vecJ.z > test.z) &&
    				(test.x < (vecJ.x - vecI.x) * (test.z - vecI.z) / (vecJ.z-vecI.z) + vecI.x)) {
    			result = !result;
    		}
    	}
    	return result;
    }
    
    /**
     * Gets minimum distance to shape, 2D only, y not used
     * Doesnt check if you are inside shape, use isInConvexShape for that
     * 
     * @param point
     * @param points
     * @return
     */
    public static double getDistanceToShape(Vec3 point, List<Vec3> points) {
    	float closestDist1 = 9999;
    	float closestDist2 = 9999;
    	
    	Vec3 closestPoint1 = null;
    	Vec3 closestPoint2 = null;
    	
    	//loop twice to account for edge case where points are ordered in increasing order of closeness, causing second closest clause to never trigger
    	for (int i = 0; i < 2; i++) {
	    	for (Vec3 pointTest : points) {
	    		double dist = pointTest.distanceTo(point);
	    		
	    		if (dist < closestDist1) {
	    			closestDist1 = (float) dist;
	    			closestPoint1 = pointTest;
	    		} else if (dist < closestDist2 && pointTest != closestPoint1) {
	    			closestDist2 = (float) dist;
	    			closestPoint2 = pointTest;
	    		}
	    	}
    	}
    	
    	if (closestPoint1 == null || closestPoint2 == null) {
    		//should never happen unless too few points
    		return -1;
    	}
    	
    	return distBetweenPointAndLine(point.x, point.z, closestPoint1.x, closestPoint1.z, closestPoint2.x, closestPoint2.z);
    }
    
    /**
     * x and y are point, rest is line, 2D only
     * 
     * @param x
     * @param y
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double distBetweenPointAndLine(double x, double y, double x1, double y1, double x2, double y2) {
    	// A - the standalone point (x, y)
    	// B - start point of the line segment (x1, y1)
    	// C - end point of the line segment (x2, y2)
    	// D - the crossing point between line from A to BC

    	double AB = distBetween(x, y, x1, y1);
    	double BC = distBetween(x1, y1, x2, y2);
    	double AC = distBetween(x, y, x2, y2);

    	// Heron's formula
    	double s = (AB + BC + AC) / 2;
    	double area = Math.sqrt(s * (s - AB) * (s - BC) * (s - AC));

    	// but also area == (BC * AD) / 2
    	// BC * AD == 2 * area
    	// AD == (2 * area) / BC
    	// TODO: check if BC == 0
    	double AD = (2 * area) / BC;
    	return AD;
    }

    public static double distBetween(double x, double y, double x1, double y1) {
    	double xx = x1 - x;
    	double yy = y1 - y;

    	return Math.sqrt(xx * xx + yy * yy);
    }
	
}
