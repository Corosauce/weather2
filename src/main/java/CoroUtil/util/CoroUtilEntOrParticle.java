package CoroUtil.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CoroUtilEntOrParticle {
	
	public static double getPosX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getX();
		} else {
			return getPosXParticle(obj);
		}
	}

	private static double getPosXParticle(Object obj) {
		return ((Particle)obj).x;
	}
	
	public static double getPosY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getY();
		} else {
			return getPosYParticle(obj);
		}
	}

	private static double getPosYParticle(Object obj) {
		return ((Particle)obj).y;
	}
	
	public static double getPosZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getZ();
		} else {
			return getPosZParticle(obj);
		}
	}

	private static double getPosZParticle(Object obj) {
		return ((Particle)obj).z;
	}
	
	public static double getMotionX(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getDeltaMovement().x;
		} else {
			return getMotionXParticle(obj);
		}
	}

	private static double getMotionXParticle(Object obj) {
		return ((Particle)obj).xd;
	}
	
	public static double getMotionY(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getDeltaMovement().y;
		} else {
			return getMotionYParticle(obj);
		}
	}
	
	private static double getMotionYParticle(Object obj) {
		return ((Particle)obj).yd;
	}
	
	public static double getMotionZ(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).getDeltaMovement().z;
		} else {
			return getMotionZParticle(obj);
		}
	}

	private static double getMotionZParticle(Object obj) {
		return ((Particle)obj).zd;
	}
	
	public static void setMotionX(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).setDeltaMovement(val, ((Entity)obj).getDeltaMovement().y, ((Entity)obj).getDeltaMovement().z);
		} else {
			setMotionXParticle(obj, val);
		}
	}

	private static void setMotionXParticle(Object obj, double val) {
		((Particle)obj).xd = val;
	}
	
	public static void setMotionY(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).setDeltaMovement(((Entity)obj).getDeltaMovement().y, val, ((Entity)obj).getDeltaMovement().z);
		} else {
			setMotionYParticle(obj, val);
		}
	}

	private static void setMotionYParticle(Object obj, double val) {
		((Particle)obj).yd = val;
	}
	
	public static void setMotionZ(Object obj, double val) {
		if (obj instanceof Entity) {
			((Entity)obj).setDeltaMovement(((Entity)obj).getDeltaMovement().y, ((Entity)obj).getDeltaMovement().y, val);
		} else {
			setMotionZParticle(obj, val);
		}
	}

	private static void setMotionZParticle(Object obj, double val) {
		((Particle)obj).zd = val;
	}
	
	public static Level getWorld(Object obj) {
		if (obj instanceof Entity) {
			return ((Entity)obj).level;
		} else {
			return getWorldParticle(obj);
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static Level getWorldParticle(Object obj) {
		return Minecraft.getInstance().level;
	}

	public static double getDistance(Object obj, double x, double y, double z)
	{
		double d0 = getPosX(obj) - x;
		double d1 = getPosY(obj) - y;
		double d2 = getPosZ(obj) - z;
		return (double) Mth.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
	}

	public static void setPosX(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity e = (Entity) obj;
			e.setPos(val, e.getY(), e.getZ());
		} else {
			setPosXParticle(obj, val);
		}
	}

	private static void setPosXParticle(Object obj, double val) {
		((Particle)obj).x = val;
	}

	public static void setPosY(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity e = (Entity) obj;
			e.setPos(e.getX(), val, e.getZ());
		} else {
			setPosYParticle(obj, val);
		}
	}

	private static void setPosYParticle(Object obj, double val) {
		((Particle)obj).y = val;
	}

	public static void setPosZ(Object obj, double val) {
		if (obj instanceof Entity) {
			Entity e = (Entity) obj;
			e.setPos(e.getX(), e.getY(), val);
		} else {
			setPosZParticle(obj, val);
		}
	}

	private static void setPosZParticle(Object obj, double val) {
		((Particle)obj).z = val;
	}
	
}
