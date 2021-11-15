package CoroUtil.util;

public class CoroUtilMisc {
	public static float adjVal(float source, float target, float adj) {
		if (source < target) {
			source += adj;
			//fix over adjust
			if (source > target) {
				source = target;
			}
		} else if (source > target) {
			source -= adj;
			//fix over adjust
			if (source < target) {
				source = target;
			}
		}
		return source;
	}
}
