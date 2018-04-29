package weather2.util;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Caches nbt data to remove redundant data sending over network
 *
 * @author cosmicdan
 *
 * revisions made to further integrate it into the newer design of WeatherObjects
 */
public class CachedNBTTagCompound {
	private NBTTagCompound newData;
	private NBTTagCompound cachedData;
	private boolean forced = false;

	public CachedNBTTagCompound() {
		this.newData = new NBTTagCompound();
		this.cachedData = new NBTTagCompound();
	}

	public void setCachedNBT(NBTTagCompound cachedData) {
		if (cachedData == null)
			cachedData = new NBTTagCompound();
		this.cachedData = cachedData;
	}

	public NBTTagCompound getCachedNBT() {
		return cachedData;
	}

	public NBTTagCompound getNewNBT() {
		return newData;
	}

	public void setNewNBT(NBTTagCompound newData) {
		this.newData = newData;
	}

	public void setUpdateForced(boolean forced) {
		this.forced = forced;
	}

	public long getLong(String key) {
		if (!newData.hasKey(key))
			newData.setLong(key, cachedData.getLong(key));
		return newData.getLong(key);
	}

	public void setLong(String key, long newVal) {
		if (!cachedData.hasKey(key) || cachedData.getLong(key) != newVal || forced) {
			newData.setLong(key, newVal);
		}
		cachedData.setLong(key, newVal);
	}

	public int getInteger(String key) {
		if (!newData.hasKey(key))
			newData.setInteger(key, cachedData.getInteger(key));
		return newData.getInteger(key);
	}

	public void setInteger(String key, int newVal) {
		if (!cachedData.hasKey(key) || cachedData.getInteger(key) != newVal || forced) {
			newData.setInteger(key, newVal);
		}
		cachedData.setInteger(key, newVal);
	}

	public short getShort(String key) {
		if (!newData.hasKey(key))
			newData.setShort(key, cachedData.getShort(key));
		return newData.getShort(key);
	}

	public void setShort(String key, short newVal) {
		if (!cachedData.hasKey(key) || cachedData.getShort(key) != newVal || forced) {
			newData.setShort(key, newVal);
		}
		cachedData.setShort(key, newVal);
	}

	public String getString(String key) {
		if (!newData.hasKey(key))
			newData.setString(key, cachedData.getString(key));
		return newData.getString(key);
	}

	public void setString(String key, String newVal) {
		if (!cachedData.hasKey(key) || cachedData.getString(key) != newVal || forced) {
			newData.setString(key, newVal);
		}
		cachedData.setString(key, newVal);
	}

	public boolean getBoolean(String key) {
		if (!newData.hasKey(key))
			newData.setBoolean(key, cachedData.getBoolean(key));
		return newData.getBoolean(key);
	}

	public void setBoolean(String key, boolean newVal) {
		if (!cachedData.hasKey(key) || cachedData.getBoolean(key) != newVal || forced) {
			newData.setBoolean(key, newVal);
		}
		cachedData.setBoolean(key, newVal);
	}

	public float getFloat(String key) {
		if (!newData.hasKey(key))
			newData.setFloat(key, cachedData.getFloat(key));
		return newData.getFloat(key);
	}

	public void setFloat(String key, float newVal) {
		if (!cachedData.hasKey(key) || cachedData.getFloat(key) != newVal || forced) {
			newData.setFloat(key, newVal);
		}
		cachedData.setFloat(key, newVal);
	}

	public double getDouble(String key) {
		if (!newData.hasKey(key))
			newData.setDouble(key, cachedData.getDouble(key));
		return newData.getDouble(key);
	}

	public void setDouble(String key, double newVal) {
		if (!cachedData.hasKey(key) || cachedData.getDouble(key) != newVal || forced) {
			newData.setDouble(key, newVal);
		}
		cachedData.setDouble(key, newVal);
	}

	public void updateCacheFromNew() {
		this.cachedData = this.newData;
	}

}