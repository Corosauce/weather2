package weather2.util;

import net.minecraft.nbt.CompoundTag;

/**
 * Caches nbt data to remove redundant data sending over network
 *
 * @author cosmicdan
 *
 * revisions made to further integrate it into the newer design of WeatherObjects
 */
public class CachedNBTTagCompound {
	private CompoundTag newData;
	private CompoundTag cachedData;
	private boolean forced = false;

	public CachedNBTTagCompound() {
		this.newData = new CompoundTag();
		this.cachedData = new CompoundTag();
	}

	public void setCachedNBT(CompoundTag cachedData) {
		if (cachedData == null)
			cachedData = new CompoundTag();
		this.cachedData = cachedData;
	}

	public CompoundTag getCachedNBT() {
		return cachedData;
	}

	public CompoundTag getNewNBT() {
		return newData;
	}

	public void setNewNBT(CompoundTag newData) {
		this.newData = newData;
	}

	public void setUpdateForced(boolean forced) {
		this.forced = forced;
	}

	public long getLong(String key) {
		if (!newData.contains(key))
			newData.putLong(key, cachedData.getLong(key));
		return newData.getLong(key);
	}

	public void putLong(String key, long newVal) {
		if (!cachedData.contains(key) || cachedData.getLong(key) != newVal || forced) {
			newData.putLong(key, newVal);
		}
		cachedData.putLong(key, newVal);
	}

	public int getInt(String key) {
		if (!newData.contains(key))
			newData.putInt(key, cachedData.getInt(key));
		return newData.getInt(key);
	}

	public void putInt(String key, int newVal) {
		if (!cachedData.contains(key) || cachedData.getInt(key) != newVal || forced) {
			newData.putInt(key, newVal);
		}
		cachedData.putInt(key, newVal);
	}

	public short getShort(String key) {
		if (!newData.contains(key))
			newData.putShort(key, cachedData.getShort(key));
		return newData.getShort(key);
	}

	public void putShort(String key, short newVal) {
		if (!cachedData.contains(key) || cachedData.getShort(key) != newVal || forced) {
			newData.putShort(key, newVal);
		}
		cachedData.putShort(key, newVal);
	}

	public String getString(String key) {
		if (!newData.contains(key))
			newData.putString(key, cachedData.getString(key));
		return newData.getString(key);
	}

	public void putString(String key, String newVal) {
		if (!cachedData.contains(key) || !cachedData.getString(key).equals(newVal) || forced) {
			newData.putString(key, newVal);
		}
		cachedData.putString(key, newVal);
	}

	public boolean getBoolean(String key) {
		if (!newData.contains(key))
			newData.putBoolean(key, cachedData.getBoolean(key));
		return newData.getBoolean(key);
	}

	public void putBoolean(String key, boolean newVal) {
		if (!cachedData.contains(key) || cachedData.getBoolean(key) != newVal || forced) {
			newData.putBoolean(key, newVal);
		}
		cachedData.putBoolean(key, newVal);
	}

	public float getFloat(String key) {
		if (!newData.contains(key))
			newData.putFloat(key, cachedData.getFloat(key));
		return newData.getFloat(key);
	}

	public void putFloat(String key, float newVal) {
		if (!cachedData.contains(key) || cachedData.getFloat(key) != newVal || forced) {
			newData.putFloat(key, newVal);
		}
		cachedData.putFloat(key, newVal);
	}

	public double getDouble(String key) {
		if (!newData.contains(key))
			newData.putDouble(key, cachedData.getDouble(key));
		return newData.getDouble(key);
	}

	public void putDouble(String key, double newVal) {
		if (!cachedData.contains(key) || cachedData.getDouble(key) != newVal || forced) {
			newData.putDouble(key, newVal);
		}
		cachedData.putDouble(key, newVal);
	}

	public CompoundTag get(String key) {
		return newData.getCompound(key);
	}

	/** warning, not cached **/
	public void put(String key, CompoundTag tag) {
		newData.put(key, tag);
		cachedData.put(key, tag);
	}

	public boolean contains(String key) {
		return newData.contains(key);
	}

	public void updateCacheFromNew() {
		this.cachedData = this.newData;
	}

}