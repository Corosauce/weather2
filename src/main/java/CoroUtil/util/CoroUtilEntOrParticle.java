package CoroUtil.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CoroUtilEntOrParticle {
	
	public static double getPosX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getPosX();
		} else {
			return getPosXParticle(obj);
		}
	}

	private static double getPosXParticle(Object obj) {
		return ((Particle)obj).posX;
	}
	
	public static double getPosY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getPosY();
		} else {
			return getPosYParticle(obj);
		}
	}

	private static double getPosYParticle(Object obj) {
		return ((Particle)obj).posY;
	}
	
	public static double getPosZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getPosZ();
		} else {
			return getPosZParticle(obj);
		}
	}

	private static double getPosZParticle(Object obj) {
		return ((Particle)obj).posZ;
	}
	
	public static double getMotionX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getMotion().x;
		} else {
			return getMotionXParticle(obj);
		}
	}

	private static double getMotionXParticle(Object obj) {
		return ((Particle)obj).motionX;
	}
	
	public static double getMotionY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getMotion().y;
		} else {
			return getMotionYParticle(obj);
		}
	}
	
	private static double getMotionYParticle(Object obj) {
		return ((Particle)obj).motionY;
	}
	
	public static double getMotionZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getMotion().z;
		} else {
			return getMotionZParticle(obj);
		}
	}

	private static double getMotionZParticle(Object obj) {
		return ((Particle)obj).motionZ;
	}
	
	public static void setMotionX(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).setMotion(val, ((Entity)obj).getMotion().y, ((Entity)obj).getMotion().z);
		} else {
			setMotionXParticle(obj, val);
		}
	}

	private static void setMotionXParticle(Object obj, double val) {
		((Particle)obj).motionX = val;
	}
	
	public static void setMotionY(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).setMotion(((Entity)obj).getMotion().y, val, ((Entity)obj).getMotion().z);
		} else {
			setMotionYParticle(obj, val);
		}
	}

	private static void setMotionYParticle(Object obj, double val) {
		((Particle)obj).motionY = val;
	}
	
	public static void setMotionZ(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).setMotion(((Entity)obj).getMotion().y, ((Entity)obj).getMotion().y, val);
		} else {
			setMotionZParticle(obj, val);
		}
	}

	private static void setMotionZParticle(Object obj, double val) {
		((Particle)obj).motionZ = val;
	}
	
	public static World getWorld(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).world;
		} else {
			return getWorldParticle(obj);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static World getWorldParticle(Object obj) {
		return Minecraft.getInstance().world;
	}

	public static double getDistance(Object obj, double x, double y, double z)
	{
		double d0 = getPosX(obj) - x;
		double d1 = getPosY(obj) - y;
		double d2 = getPosZ(obj) - z;
		return (double) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

	public static void setPosX(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity e = (Entity) obj;
			e.setPosition(val, e.getPosY(), e.getPosZ());
		} else {
			setPosXParticle(obj, val);
		}
	}

	private static void setPosXParticle(Object obj, double val) {
		((Particle)obj).posX = val;
	}

	public static void setPosY(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity e = (Entity) obj;
			e.setPosition(e.getPosX(), val, e.getPosZ());
		} else {
			setPosYParticle(obj, val);
		}
	}

	private static void setPosYParticle(Object obj, double val) {
		((Particle)obj).posY = val;
	}

	public static void setPosZ(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity e = (Entity) obj;
			e.setPosition(e.getPosX(), e.getPosY(), val);
		} else {
			setPosZParticle(obj, val);
		}
	}

	private static void setPosZParticle(Object obj, double val) {
		((Particle)obj).posZ = val;
	}
	
}
