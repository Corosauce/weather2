package weather2.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

//make SS load before mine so i can override theirs and add my compat for theirs, prevents theirs failing
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class Weather2LoadingPlugin implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass()
    {
        return new String[] { "weather2.asm.WorldTransformer" };
    }

    @Override
    public String getModContainerClass()
    {
        return null;
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) 
    {
    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}
