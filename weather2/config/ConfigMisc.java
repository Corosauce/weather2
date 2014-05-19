package weather2.config;

import java.io.File;

import weather2.WeatherUtil;

import modconfig.IConfigCategory;


public class ConfigMisc implements IConfigCategory {

	public static boolean proxyRenderOverrideEnabled = true;
	public static boolean takeControlOfGlobalRain = true;
	public static boolean windOn = true;
	
	public static boolean Storm_Tornado_grabPlayer = true;
	public static boolean Storm_Tornado_grabBlocks = true;
	public static boolean Storm_Tornado_GrabCond_StrengthGrabbing = true;
	public static boolean Storm_Tornado_GrabCond_List = false;
	public static boolean Storm_Tornado_GrabListBlacklistMode = false;
	public static String Storm_Tornado_GrabList = "";
	public static int Storm_Tornado_maxBlocks = 800;
	public static int Storm_Tornado_rarityOfDisintegrate = 15;
	public static int Storm_Tornado_rarityOfBreakOnFall = 5;
	public static int Storm_Tornado_rarityOfFirenado = -1;
	public static boolean Storm_FlyingBlocksHurt = true;

	public ConfigMisc() {
		
	}
	
	@Override
	public String getConfigFileName() {
		return "Weather2" + File.separator + "Misc";
	}

	@Override
	public String getCategory() {
		return "Weather2: Misc";
	}

	@Override
	public void hookUpdatedValues() {
		WeatherUtil.doBlockList();
	}

}
