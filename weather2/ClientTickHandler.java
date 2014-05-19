package weather2;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumSet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.world.World;
import weather2.config.ConfigMisc;
import weather2.weathersystem.EntityRendererProxyWeather2Mini;
import weather2.weathersystem.WeatherManagerClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler
{
	
	public static World lastWorld;
	
	public static WeatherManagerClient weatherManager;
	
    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {}

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
        if (type.equals(EnumSet.of(TickType.RENDER)))
        {
            onRenderScreenTick();
        }
        else if (type.equals(EnumSet.of(TickType.CLIENT)))
        {
            GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;

            if (guiscreen != null)
            {
                onTickInGUI(guiscreen);
            }
            else
            {
                
            }
            
            onTickInGame();
        }
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.RENDER, TickType.CLIENT);
    }

    @Override
    public String getLabel()
    {
        return null;
    }

    public void onRenderScreenTick()
    {
    	
    }

    public void onTickInGUI(GuiScreen guiscreen)
    {
        //onTickInGame();
    }
    
    public void onTickInGame()
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        World world = mc.theWorld;
        
        if (ConfigMisc.proxyRenderOverrideEnabled) {
        	if (!(mc.entityRenderer instanceof EntityRendererProxyWeather2Mini)) {
        		EntityRendererProxyWeather2Mini temp = new EntityRendererProxyWeather2Mini(mc);
		        mc.entityRenderer = temp;
        	}
    	} else {
    		if ((mc.entityRenderer instanceof EntityRendererProxyWeather2Mini)) {
    			mc.entityRenderer = new EntityRenderer(mc);
    		}
    	}
        
        if (world != null && world != lastWorld) {
        	lastWorld = world;
        	init(world);
        }
        
        if (world != null) {
        	weatherManager.tick();
        }
    }
    
    public static void init(World world) {
    	weatherManager = new WeatherManagerClient(world.provider.dimensionId);
    }

    static void getField(Field field, Object newValue) throws Exception
    {
        field.setAccessible(true);
        // remove final modifier from field
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, newValue);
    }
}
