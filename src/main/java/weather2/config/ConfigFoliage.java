package weather2.config;

import extendedrenderer.ExtendedRenderer;
import extendedrenderer.render.FoliageRenderer;
import modconfig.IConfigCategory;
import weather2.Weather;

import java.io.File;

public class ConfigFoliage implements IConfigCategory {

    public static int foliageShaderRange = 40;
    public static int Thread_Foliage_Process_Delay = 400;

    @Override
    public String getName() {
        return "Foliage";
    }

    @Override
    public String getRegistryName() {
        return Weather.modID + getName();
    }

    @Override
    public String getConfigFileName() {
        return "Weather2" + File.separator + getName();
    }

    @Override
    public String getCategory() {
        return "Weather2: " + getName();
    }

    @Override
    public void hookUpdatedValues() {
        FoliageRenderer.radialRange = foliageShaderRange;
    }
}
