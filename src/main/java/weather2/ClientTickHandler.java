package weather2;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.lwjgl.input.Mouse;

import weather2.client.SceneEnhancer;
import weather2.client.gui.GuiEZConfig;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtilConfig;
import weather2.weathersystem.EntityRendererProxyWeather2Mini;
import weather2.weathersystem.WeatherManagerClient;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ClientTickHandler
{
	
	public static World lastWorld;
	
	public static WeatherManagerClient weatherManager;
	public static SceneEnhancer sceneEnhancer;
	
	public boolean hasOpenedConfig = false;
	
	public GuiButton configButton;
	
	public ClientTickHandler() {
		//this constructor gets called multiple times when created from proxy, this prevents multiple inits
		if (sceneEnhancer == null) {
			sceneEnhancer = new SceneEnhancer();
			(new Thread(sceneEnhancer, "Weather2 Scene Enhancer")).start();
		}
	}

    public void onRenderScreenTick()
    {
    	Minecraft mc = FMLClientHandler.instance().getClient();
    	if (mc.currentScreen instanceof GuiIngameMenu) {
    		ScaledResolution scaledresolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
    		int k = Mouse.getX() * i / mc.displayWidth;
            int l = j - Mouse.getY() * j / mc.displayHeight - 1;
    		configButton = new GuiButton(0, (i/2)-100, 0, 200, 20, "Weather2 Config (unpauses game)");
    		configButton.drawButton(mc, k, l);
    		
    		if (k >= configButton.xPosition && l >= configButton.yPosition && k < configButton.xPosition + 200 && l < configButton.yPosition + 20) {
    			if (Mouse.isButtonDown(0)) {
    				mc.displayGuiScreen(new GuiEZConfig());
    			}
    		}
    	}
    	
    	
    }

    public void onTickInGUI(GuiScreen guiscreen)
    {
        //onTickInGame();
    }
    
    public void onTickInGame()
    {
        Minecraft mc = FMLClientHandler.instance().getClient();
        World world = mc.theWorld;
        
        if (ConfigMisc.Misc_proxyRenderOverrideEnabled) {
        	if (!(mc.entityRenderer instanceof EntityRendererProxyWeather2Mini)) {
        		EntityRendererProxyWeather2Mini temp = new EntityRendererProxyWeather2Mini(mc, mc.getResourceManager());
		        mc.entityRenderer = temp;
        	}
    	} else {
    		if ((mc.entityRenderer instanceof EntityRendererProxyWeather2Mini)) {
    			mc.entityRenderer = new EntityRenderer(mc, mc.getResourceManager());
    		}
    	}
        
        if (world != null && world != lastWorld) {
        	init(world);
        }
        
        if (world != null) {
        	weatherManager.tick();
        	
        	if (ConfigMisc.Misc_ForceVanillaCloudsOff && world.provider.dimensionId == 0) {
            	mc.gameSettings.clouds = false;
            }
        }
        
        if (world != null && WeatherUtilConfig.listDimensionsWindEffects.contains(world.provider.dimensionId)) {
        	//weatherManager.tick();
        	
        	sceneEnhancer.tickClient();
            
            
        }
        
        if (world != null) {
        	if (mc.ingameGUI.getChatGUI().getSentMessages().size() > 0) {
	            String msg = (String) mc.ingameGUI.getChatGUI().getSentMessages().get(mc.ingameGUI.getChatGUI().getSentMessages().size()-1);
	            
	            if (msg.equals("/weather2 config")) {
	            	mc.ingameGUI.getChatGUI().getSentMessages().remove(mc.ingameGUI.getChatGUI().getSentMessages().size()-1);
	            	mc.displayGuiScreen(new GuiEZConfig());
	            }
            }
        }
        
    }
	
    public static void checkClientWeather() {
    	try {
    		if (ClientTickHandler.weatherManager == null) {
    			ClientTickHandler.init(FMLClientHandler.instance().getClient().theWorld);
        	}
    	} catch (Exception ex) {
			//always get output for this
			System.out.println("Warning, Weather2 weatherManager init failed for some reason, printing stacktrace, report to https://github.com/Corosauce/weather2/issues");
			ex.printStackTrace();
			//Weather.dbg("Warning, Weather2 client received packet before it was ready to use, and failed to init client weather due to null world");
    	}
    }
    
    public static void init(World world) {
    	Weather.dbg("Initializing WeatherManagerClient for client world");
    	lastWorld = world;
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
