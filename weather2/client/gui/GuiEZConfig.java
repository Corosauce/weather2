package weather2.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;

import weather2.Weather;
import weather2.client.gui.elements.GuiButtonBoolean;
import weather2.client.gui.elements.GuiButtonCycle;
import weather2.util.WeatherUtilConfig;

import CoroUtil.packet.PacketHelper;

public class GuiEZConfig extends GuiScreen {

	//slightly different from ZC subgui with its main gui, this one we shouldnt need to show the main gui, just auto select first subgui, Performance
	//treat the subguis as tabs, clicking one auto saves data from that opened gui and switches to the tab they clicked, save & close always visible and always closes gui
	
	//given that all buttons dont exist at same time, we have to build nbtSendCache as pieces of it come in, and sync as much of it as we can
	//this will REQUIRE server side to check if a tag exists before applying it
	
	//populate 
	
	public int xCenter;
	public int yCenter;
	public int xStart;
	public int yStart;
	
	//public int selectedWorld;
    //public List<IScrollingElement> listElements = new ArrayList<IScrollingElement>();
    //public GuiSlotImpl guiScrollable;
    //public GuiButton guiSelectMap;
    
    public ResourceLocation resGUI = new ResourceLocation(Weather.modID + ":textures/gui/gui512.png");
    
    public String guiCur = GUI_SUBGUI_PERFORMANCE;
	//public String guiPrev = "";
	
	//Elements
	public HashMap<Integer, GuiButton> buttonsLookup = new HashMap<Integer, GuiButton>();
	//public HashMap<Integer, String> buttonsLookupInt = new HashMap<Integer, String>(); //shouldnt need, just use const button ids -> button
	//public GuiTextField textboxWorldName;
	public NBTTagCompound nbtSendCache = new NBTTagCompound();
	
	//config commands for gui/packet handler - might only be needed in gui since it just sets data on server side
	public static int CMD_CLOSE = 0;
	
	//subguis
	public static int CMD_SUBGUI_PERFORMANCE = 40;
	public static int CMD_SUBGUI_COMPATIBILITY = 41;
	public static int CMD_SUBGUI_PREFERENCE = 42;
	public static int CMD_SUBGUI_DIMENSIONS = 43;
	public static String GUI_SUBGUI_PERFORMANCE = "Performance";
	public static String GUI_SUBGUI_COMPATIBILITY = "Compatibility";
	public static String GUI_SUBGUI_PREFERENCE = "Preference";
	public static String GUI_SUBGUI_DIMENSIONS = "Dimensions";
	
	//other elements
	//see WeatherUtilConfig for button id entries
	
	//public static String BOOL_CUSTOMLIGHTING = "ya";
	
	//public static String TXT_CUSTOMLIGHTING = "customLighting";
	
	/** The X size of the inventory window in pixels. */
    protected int xSize = 176;

    /** The Y size of the inventory window in pixels. */
    protected int ySize = 166;
    
    public boolean canPlayerChangeServerSettings = false;
	
	public GuiEZConfig () {
		super();

		Weather.dbg("EZGUI constructor");
		
		if (MinecraftServer.getServer() != null && MinecraftServer.getServer().isSinglePlayer()) {
			canPlayerChangeServerSettings = true;
		}
		
		//only sync request on initial gui open
        NBTTagCompound data = new NBTTagCompound();
        data.setString("command", "syncRequest");
        PacketHelper.sendClientPacket(PacketHelper.createPacketForNBTHandler("EZGuiData", data));
		
        //prep send cache
		nbtSendCache.setCompoundTag("guiData", new NBTTagCompound());
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		
		
		//a fix for container using gui opening on client side that doesnt need slot manip - might not have been needed, below was doing initGui on main gui close
		//Minecraft.getMinecraft().thePlayer.openContainer = Minecraft.getMinecraft().thePlayer.inventoryContainer;
	}
	
	public void addButton(GuiButton btn) {
		buttonsLookup.put(btn.id, btn);
		//buttonsLookupInt.put(, lookupName);
		buttonList.add(btn);
	}
	
	/*public void addTextBox(String lookupName, GuiTextFieldZC textBox) {
		textBoxes.add(textBox);
		textBoxesLookup.put(lookupName, textBox);
	}*/
	
	public void resetGuiElements() {
		buttonList.clear();
		buttonsLookup.clear();
		//buttonsLookupInt.clear();
		/*textBoxes.clear();
		textBoxesLookup.clear();*/
	}

	@Override
	public void drawBackground(int par1)
    {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	    mc.getTextureManager().bindTexture(resGUI);
	    int x = (width - xSize) / 2;
	    int y = (height - ySize) / 2;
	    this.drawTexturedModalRect(x, y, 0, 0, 512, 512);
		
		/*for (int i = 0; i < textBoxes.size(); i++) {
			textBoxes.get(i).drawTextBox();
		}*/
		
		int yEleSize = 24;
		
		this.drawString(this.fontRenderer, "Weather2 EZ GUI Configuration" + (guiCur.equals("main") ? "" : " - GUI Tab: " + guiCur), xStart+7, yStart-9, 16777215);
		
		int yStart2 = yStart + 34;
		
		int xOP = 260;
		String op = "For OP/Singleplayer";
		
		//this.drawString(this.fontRenderer, "Server vals = OPs only", xStart+260, yStart+12, 16777215);
		
		this.drawString(this.fontRenderer, "--------------------------------------------------------", xStart+7, yStart2-3-4, 16777215);
		
		if (guiCur.equals(GUI_SUBGUI_PERFORMANCE)) {
			this.drawString(this.fontRenderer, "Cloud/Storm effects", xStart+7, yStart2+8, 16777215);
			this.drawString(this.fontRenderer, "Nature effects", xStart+7, yStart2+8+yEleSize*1, 16777215);
			this.drawString(this.fontRenderer, "Particle precipitation rate", xStart+7, yStart2+8+yEleSize*2, 16777215);
		} else if (guiCur.equals(GUI_SUBGUI_COMPATIBILITY)) {
			this.drawString(this.fontRenderer, "Storms when", xStart+7, yStart2+8, 16777215);
			this.drawString(this.fontRenderer, "Lock vanilla weather", xStart+7, yStart2+8+yEleSize*1, 16777215);
			this.drawString(this.fontRenderer, "Particle precipitation", xStart+7, yStart2+8+yEleSize*2, 16777215);
			this.drawString(this.fontRenderer, "Extra snowfall blocks", xStart+7, yStart2+8+yEleSize*3, 16777215);
			this.drawString(this.fontRenderer, "Wind only for vanilla particles", xStart+7, yStart2+8+yEleSize*4, 16777215);

			this.drawString(this.fontRenderer, op, xStart+xOP, yStart2+8, 16777215);
			this.drawString(this.fontRenderer, op, xStart+xOP, yStart2+8+yEleSize*1, 16777215);
			this.drawString(this.fontRenderer, op, xStart+xOP, yStart2+8+yEleSize*3, 16777215);
		} else if (guiCur.equals(GUI_SUBGUI_PREFERENCE)) {
			this.drawString(this.fontRenderer, "Rate of storms per each player", xStart+7, yStart2+8, 16777215);
			this.drawString(this.fontRenderer, "Chance of storms", xStart+7, yStart2+8+yEleSize*1, 16777215);
			this.drawString(this.fontRenderer, "Chance of rain", xStart+7, yStart2+8+yEleSize*2, 16777215);
			this.drawString(this.fontRenderer, "Block destruction", xStart+7, yStart2+8+yEleSize*3, 16777215);
			
			this.drawString(this.fontRenderer, op, xStart+xOP, yStart2+8, 16777215);
			this.drawString(this.fontRenderer, op, xStart+xOP, yStart2+8+yEleSize*1, 16777215);
			this.drawString(this.fontRenderer, op, xStart+xOP, yStart2+8+yEleSize*2, 16777215);
			this.drawString(this.fontRenderer, op, xStart+xOP, yStart2+8+yEleSize*3, 16777215);
		} else if (guiCur.equals(GUI_SUBGUI_DIMENSIONS)) {
			this.drawString(this.fontRenderer, "Not done yet! Just use /config and find the Dimension listings, ", xStart+7, yStart2+8, 16777215);
			this.drawString(this.fontRenderer, "then add the dimension ID", xStart+7, yStart2+8+yEleSize*1, 16777215);
		}
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawBackground(0);
		super.drawScreen(par1, par2, par3);
	}
	
	@Override
	protected void keyTyped(char par1, int par2) {
		
		if (par2 == 1) {
			//we can check here if they hit esc to close gui, but i dont need it
		}
		
		super.keyTyped(par1, par2);
		
		/*for (int i = 0; i < textBoxes.size(); i++) {
			GuiTextFieldZC gtf = textBoxes.get(i);
			if (gtf.isFocused()) {
				gtf.textboxKeyTyped(par1, par2);
				String newVal = gtf.getText();
				//SET YOUR NBT STUFF HERE!!!! DATA IS LOST ONCE THEY HIT BACK!
				nbtSendCache.getCompoundTag("guiData").setString(gtf.name, newVal);
			}
		}*/
	}
	
	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		super.mouseClicked(par1, par2, par3);
		
		/*for (int i = 0; i < textBoxes.size(); i++) {
			textBoxes.get(i).mouseClicked(par1, par2, par3);
		}*/
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		
		/*for (int i = 0; i < textBoxes.size(); i++) {
			textBoxes.get(i).updateCursorCounter();
		}*/
		
		if (WeatherUtilConfig.nbtClientCache.getBoolean("markUpdated")) {
			Weather.dbg("EZGUI client markUpdated detected");
			WeatherUtilConfig.nbtClientCache.setBoolean("markUpdated", false);
			updateGuiElements();
		}
		
		if (guiCur.equals(GUI_SUBGUI_COMPATIBILITY)) {
			if (((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_STORM)).getIndex() == 1) {
				((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_LOCK)).enabled = false;
				((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_LOCK)).setIndex(2);
			} else {
				((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_LOCK)).enabled = true;
			}
		}
		
		/*if (LevelConfig.nbtInfoClientMapConfig.getBoolean("markUpdated")) {
			LevelConfig.nbtInfoClientMapConfig.setBoolean("markUpdated", false);
			updateGuiElements();
		}*/
	}
	
	/*public NBTTagCompound nbtConvertGUIDataToNBT() {
		NBTTagCompound data = new NBTTagCompound();
		
		return data;
	}*/
	
	public void updateGuiElements() {
		System.out.println("updateGuiElements");
		
		canPlayerChangeServerSettings = WeatherUtilConfig.nbtClientCache.getBoolean("isPlayerOP");
		
		NBTTagCompound serverDataCache = WeatherUtilConfig.nbtClientCache.getCompoundTag("data");
		
		if (guiCur.equals(GUI_SUBGUI_PERFORMANCE)) {
			if (WeatherUtilConfig.nbtClientData.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_PERF_STORM)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PERF_STORM)).setIndex(WeatherUtilConfig.nbtClientData.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_PERF_STORM));
			if (WeatherUtilConfig.nbtClientData.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_PERF_NATURE)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PERF_NATURE)).setIndex(WeatherUtilConfig.nbtClientData.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_PERF_NATURE));
			if (WeatherUtilConfig.nbtClientData.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_PERF_PRECIPRATE)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PERF_PRECIPRATE)).setIndex(WeatherUtilConfig.nbtClientData.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_PERF_PRECIPRATE));
			
		} else if (guiCur.equals(GUI_SUBGUI_COMPATIBILITY)) {
			//Weather.dbg("WeatherUtilConfig.nbtClientCache: " + serverDataCache);
			//Weather.dbg("test val check: " + serverDataCache.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_COMP_STORM));
			if (serverDataCache.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_COMP_STORM)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_STORM)).setIndex(serverDataCache.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_COMP_STORM));
			if (serverDataCache.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_COMP_LOCK)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_LOCK)).setIndex(serverDataCache.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_COMP_LOCK));
			if (WeatherUtilConfig.nbtClientData.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_COMP_PARTICLEPRECIP)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_PARTICLEPRECIP)).setIndex(WeatherUtilConfig.nbtClientData.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_COMP_PARTICLEPRECIP));
			if (serverDataCache.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_COMP_SNOWFALLBLOCKS)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_SNOWFALLBLOCKS)).setIndex(serverDataCache.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_COMP_SNOWFALLBLOCKS));
			if (WeatherUtilConfig.nbtClientData.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_COMP_PARTICLESNOMODS)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_PARTICLESNOMODS)).setIndex(WeatherUtilConfig.nbtClientData.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_COMP_PARTICLESNOMODS));
			
			((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_STORM)).enabled = canPlayerChangeServerSettings;
			((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_LOCK)).enabled = canPlayerChangeServerSettings;
			((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_COMP_SNOWFALLBLOCKS)).enabled = canPlayerChangeServerSettings;
			
		} else if (guiCur.equals(GUI_SUBGUI_PREFERENCE)) {
			if (serverDataCache.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_PREF_RATEOFSTORM)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PREF_RATEOFSTORM)).setIndex(serverDataCache.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_PREF_RATEOFSTORM));
			if (serverDataCache.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFSTORM)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFSTORM)).setIndex(serverDataCache.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFSTORM));
			if (serverDataCache.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFRAIN)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFRAIN)).setIndex(serverDataCache.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFRAIN));
			if (serverDataCache.hasKey("btn_" + WeatherUtilConfig.CMD_BTN_PREF_BLOCKDESTRUCTION)) ((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PREF_BLOCKDESTRUCTION)).setIndex(serverDataCache.getInteger("btn_" + WeatherUtilConfig.CMD_BTN_PREF_BLOCKDESTRUCTION));
			
			((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PREF_RATEOFSTORM)).enabled = canPlayerChangeServerSettings;
			((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFSTORM)).enabled = canPlayerChangeServerSettings;
			((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFRAIN)).enabled = canPlayerChangeServerSettings;
			((GuiButtonCycle)buttonsLookup.get(WeatherUtilConfig.CMD_BTN_PREF_BLOCKDESTRUCTION)).enabled = canPlayerChangeServerSettings;
			
		}
		
		
		
		/*if (guiCur.equals("main")) {
			//custom lighting
			((GuiButtonBoolean)buttonsLookup.get(LevelConfig.nbtStrCustomLightingUse)).setBoolean(LevelConfig.nbtInfoClientMapConfig.getBoolean(LevelConfig.nbtStrCustomLightingUse));
			textBoxesLookup.get(LevelConfig.nbtStrCustomLightingMode).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrCustomLightingMode));
			
			//custom time
			((GuiButtonBoolean)buttonsLookup.get(LevelConfig.nbtStrCustomTimeUse)).setBoolean(LevelConfig.nbtInfoClientMapConfig.getBoolean(LevelConfig.nbtStrCustomTimeUse));
			textBoxesLookup.get(LevelConfig.nbtStrCustomTimeVal).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrCustomTimeVal));
			
		} else if (guiCur.equals(GUI_SUBGUI_WAVE)) {
			textBoxesLookup.get(LevelConfig.nbtStrWaveDefaultMobSpawned).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveDefaultMobSpawned));
			textBoxesLookup.get(LevelConfig.nbtStrWaveSpawnCountBase).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveSpawnCountBase));
			textBoxesLookup.get(LevelConfig.nbtStrWaveSpawnCountMultiplier).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveSpawnCountMultiplier));
			textBoxesLookup.get(LevelConfig.nbtStrWaveHealth).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveHealth));
			textBoxesLookup.get(LevelConfig.nbtStrWaveHealthAmp).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveHealthAmp));
			textBoxesLookup.get(LevelConfig.nbtStrWaveSpeedBase).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveSpeedBase));
			textBoxesLookup.get(LevelConfig.nbtStrWaveSpeedRand).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveSpeedRand));
			textBoxesLookup.get(LevelConfig.nbtStrWaveSpeedAmp).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveSpeedAmp));
			textBoxesLookup.get(LevelConfig.nbtStrWaveSpeedAmpMax).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveSpeedAmpMax));
			textBoxesLookup.get(LevelConfig.nbtStrWaveMoveLeadDist).setText(LevelConfig.nbtInfoClientMapConfig.getString(LevelConfig.nbtStrWaveMoveLeadDist));
		}*/
	}
	
	@Override
    public void initGui()
    {
		super.initGui();
		resetGuiElements();
		
		xSize = 372;
    	ySize = 250;
		ScaledResolution var8 = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int scaledWidth = var8.getScaledWidth();
        int scaledHeight = var8.getScaledHeight();
        
        //System.out.println(scaledWidth);

        xCenter = scaledWidth / 2;
        yCenter = scaledHeight / 2;
        
        xStart = xCenter - xSize/2;
        yStart = yCenter - ySize/2;

		int guiPadding = 8;
		
        int xStartPadded = xStart + guiPadding - 1;
        int yStartPadded = yStart + guiPadding - 1;
        
        int btnWidth = 80;
        int btnWidthAndPadding = 84;
        int btnWidthBool = 50;
        int btnHeight = 20;
        int btnHeightAndPadding = 24;
        int padding = 1;
        int btnSpacing = 22;
        
        int xStartPadded2 = xStartPadded + 168;
        int yStartPadded2 = yStartPadded + 30;
        
        addButton(new GuiSmallButton(CMD_CLOSE, xStart + xSize - guiPadding - btnWidth, yStart + ySize - guiPadding - btnHeight, btnWidth, btnHeight, "Save & Close"));
        
        addButton(new GuiButton(CMD_SUBGUI_PERFORMANCE, xStartPadded+btnWidthAndPadding*0, yStartPadded, btnWidth, btnHeight, (guiCur.equals(GUI_SUBGUI_PERFORMANCE) ? "\u00A7" + '2' : "") + GUI_SUBGUI_PERFORMANCE));
        addButton(new GuiButton(CMD_SUBGUI_COMPATIBILITY, xStartPadded+btnWidthAndPadding*1, yStartPadded, btnWidth, btnHeight, (guiCur.equals(GUI_SUBGUI_COMPATIBILITY) ? "\u00A7" + '2' : "") + GUI_SUBGUI_COMPATIBILITY));
        addButton(new GuiButton(CMD_SUBGUI_PREFERENCE, xStartPadded+btnWidthAndPadding*2, yStartPadded, btnWidth, btnHeight, (guiCur.equals(GUI_SUBGUI_PREFERENCE) ? "\u00A7" + '2' : "") + GUI_SUBGUI_PREFERENCE));
        addButton(new GuiButton(CMD_SUBGUI_DIMENSIONS, xStartPadded+btnWidthAndPadding*3, yStartPadded, btnWidth, btnHeight, (guiCur.equals(GUI_SUBGUI_DIMENSIONS) ? "\u00A7" + '2' : "") + GUI_SUBGUI_DIMENSIONS));
        
        if (guiCur.equals(GUI_SUBGUI_PERFORMANCE)) {
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_PERF_STORM, xStartPadded2+btnWidthAndPadding*0, yStartPadded2, btnWidth, btnHeight, WeatherUtilConfig.LIST_RATES, 0));
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_PERF_NATURE, xStartPadded2+btnWidthAndPadding*0, yStartPadded2+btnHeightAndPadding*1, btnWidth, btnHeight, WeatherUtilConfig.LIST_RATES2, 0));
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_PERF_PRECIPRATE, xStartPadded2+btnWidthAndPadding*0, yStartPadded2+btnHeightAndPadding*2, btnWidth, btnHeight, WeatherUtilConfig.LIST_RATES2, 0));
        } else if (guiCur.equals(GUI_SUBGUI_COMPATIBILITY)) {
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_COMP_STORM, xStartPadded2+btnWidthAndPadding*0, yStartPadded2, btnWidth, btnHeight, WeatherUtilConfig.LIST_STORMSWHEN, 0));
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_COMP_LOCK, xStartPadded2+btnWidthAndPadding*0, yStartPadded2+btnHeightAndPadding*1, btnWidth, btnHeight, WeatherUtilConfig.LIST_LOCK, 0));
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_COMP_PARTICLEPRECIP, xStartPadded2+btnWidthAndPadding*0, yStartPadded2+btnHeightAndPadding*2, btnWidth, btnHeight, WeatherUtilConfig.LIST_TOGGLE, 1));
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_COMP_SNOWFALLBLOCKS, xStartPadded2+btnWidthAndPadding*0, yStartPadded2+btnHeightAndPadding*3, btnWidth, btnHeight, WeatherUtilConfig.LIST_TOGGLE, 0));
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_COMP_PARTICLESNOMODS, xStartPadded2+btnWidthAndPadding*0, yStartPadded2+btnHeightAndPadding*4, btnWidth, btnHeight, WeatherUtilConfig.LIST_TOGGLE, 0));
        } else if (guiCur.equals(GUI_SUBGUI_PREFERENCE)) {
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_PREF_RATEOFSTORM, xStartPadded2+btnWidthAndPadding*0, yStartPadded2, btnWidth, btnHeight, WeatherUtilConfig.LIST_CHANCE, 1));
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFSTORM, xStartPadded2+btnWidthAndPadding*0, yStartPadded2+btnHeightAndPadding*1, btnWidth, btnHeight, WeatherUtilConfig.LIST_RATES, 0));
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_PREF_CHANCEOFRAIN, xStartPadded2+btnWidthAndPadding*0, yStartPadded2+btnHeightAndPadding*2, btnWidth, btnHeight, WeatherUtilConfig.LIST_RATES2, 0));
        	addButton(new GuiButtonCycle(WeatherUtilConfig.CMD_BTN_PREF_BLOCKDESTRUCTION, xStartPadded2+btnWidthAndPadding*0, yStartPadded2+btnHeightAndPadding*3, btnWidth, btnHeight, WeatherUtilConfig.LIST_TOGGLE, 1));
        }
        
        if (guiCur.equals("main")) {
	        
        }
        
        /*if (guiCur.equals("main")) {
	        addButton(GUI_SUBGUI_WAVE, new GuiButton(CMD_SUBGUI_WAVE, xStartPadded, yStartPadded+btnHeightAndPadding*6, btnWidth, btnHeight, "Conf. Wave"));
	        
	        addButton(LevelConfig.nbtStrCustomLightingUse, new GuiButtonBoolean(CMD_BOOL_CUSTOMLIGHTING, xStartPadded, yStartPadded+btnHeight-8, btnWidthBool, btnHeight, "Enabled", "Disabled"));
	        addTextBox(LevelConfig.nbtStrCustomLightingMode, new GuiTextFieldZC(LevelConfig.nbtStrCustomLightingMode, this.fontRenderer, xStartPadded + 55, yStartPadded+btnHeight-8, 220, 20));
	        
	        addButton(LevelConfig.nbtStrCustomTimeUse, new GuiButtonBoolean(CMD_BOOL_CUSTOMTIME, xStartPadded, yStartPadded+btnHeight*3-8, btnWidthBool, btnHeight, "Enabled", "Disabled"));
	        addTextBox(LevelConfig.nbtStrCustomTimeVal, new GuiTextFieldZC(LevelConfig.nbtStrCustomTimeVal, this.fontRenderer, xStartPadded + 55, yStartPadded+btnHeight*3-8, 220, 20));
        } else if (guiCur.equals(GUI_SUBGUI_WAVE)) {
        	
        	int x2 = 90;
        	
        	addTextBox(LevelConfig.nbtStrWaveDefaultMobSpawned, new GuiTextFieldZC(LevelConfig.nbtStrWaveDefaultMobSpawned, this.fontRenderer, xStartPadded, yStartPadded+btnHeight-8, 220, 20));
        	addTextBox(LevelConfig.nbtStrWaveSpawnCountBase, new GuiTextFieldZC(LevelConfig.nbtStrWaveSpawnCountBase, this.fontRenderer, xStartPadded, yStartPadded+btnHeight*3-8, 60, 20));
        	addTextBox(LevelConfig.nbtStrWaveSpawnCountMultiplier, new GuiTextFieldZC(LevelConfig.nbtStrWaveSpawnCountMultiplier, this.fontRenderer, xStartPadded + x2, yStartPadded+btnHeight*3-8, 60, 20));
        	addTextBox(LevelConfig.nbtStrWaveHealth, new GuiTextFieldZC(LevelConfig.nbtStrWaveHealth, this.fontRenderer, xStartPadded, yStartPadded+btnHeight*5-8, 60, 20));
        	addTextBox(LevelConfig.nbtStrWaveHealthAmp, new GuiTextFieldZC(LevelConfig.nbtStrWaveHealthAmp, this.fontRenderer, xStartPadded + x2, yStartPadded+btnHeight*5-8, 60, 20));
        	addTextBox(LevelConfig.nbtStrWaveSpeedBase, new GuiTextFieldZC(LevelConfig.nbtStrWaveSpeedBase, this.fontRenderer, xStartPadded, yStartPadded+btnHeight*7-8, 60, 20));
        	addTextBox(LevelConfig.nbtStrWaveSpeedRand, new GuiTextFieldZC(LevelConfig.nbtStrWaveSpeedRand, this.fontRenderer, xStartPadded + x2, yStartPadded+btnHeight*7-8, 60, 20));
        	addTextBox(LevelConfig.nbtStrWaveSpeedAmp, new GuiTextFieldZC(LevelConfig.nbtStrWaveSpeedAmp, this.fontRenderer, xStartPadded + x2*2, yStartPadded+btnHeight*7-8, 60, 20));
        	addTextBox(LevelConfig.nbtStrWaveSpeedAmpMax, new GuiTextFieldZC(LevelConfig.nbtStrWaveSpeedAmpMax, this.fontRenderer, xStartPadded + x2*3, yStartPadded+btnHeight*7-8, 60, 20));
        	addTextBox(LevelConfig.nbtStrWaveMoveLeadDist, new GuiTextFieldZC(LevelConfig.nbtStrWaveMoveLeadDist, this.fontRenderer, xStartPadded, yStartPadded+btnHeight*9-8, 60, 20));
        }*/
        
        //used to request sync here, but it now must be on initial gui open, so to not lose previous cache
        
        //this sets our 'defaults' that the server provides
        //what about the client ones though?!?!?!?!?
        updateGuiElements();
    }
	
	@Override
	protected void actionPerformed(GuiButton var1)
    {
		String guiForPacket = guiCur;
		boolean sendPacket = false;
		
		//current plan is to only send packet on gui close, covers all bases?
		//- due to subguis... element data might get lost..... but no...... its put in nbt send cache, so thats ok now?
		
		if (WeatherUtilConfig.listSettingsServer.contains(var1.id)) {
			if (var1 instanceof GuiButtonBoolean) {
	        	((GuiButtonBoolean) var1).setBooleanToggle();
	            nbtSendCache.getCompoundTag("guiData").setInteger("btn_" + var1.id, ((GuiButtonBoolean) var1).getBoolean() ? 1 : 0);
	        }
			
			if (var1 instanceof GuiButtonCycle) {
	        	((GuiButtonCycle) var1).cycleIndex();
	            nbtSendCache.getCompoundTag("guiData").setInteger("btn_" + var1.id, ((GuiButtonCycle) var1).getIndex());
	        }
		} else if (WeatherUtilConfig.listSettingsClient.contains(var1.id)) {
			if (var1 instanceof GuiButtonCycle) {
	        	((GuiButtonCycle) var1).cycleIndex();
	        	WeatherUtilConfig.nbtClientData.setInteger("btn_" + var1.id, ((GuiButtonCycle) var1).getIndex());
	        }
		}
		
		if (var1.id == CMD_SUBGUI_PERFORMANCE || var1.id == CMD_SUBGUI_COMPATIBILITY || var1.id == CMD_SUBGUI_PREFERENCE || var1.id == CMD_SUBGUI_DIMENSIONS) {
        	//guiPrev = guiCur;
        	if (var1.id == CMD_SUBGUI_PERFORMANCE) {
        		guiCur = GUI_SUBGUI_PERFORMANCE;
        	} else if (var1.id == CMD_SUBGUI_COMPATIBILITY) {
        		guiCur = GUI_SUBGUI_COMPATIBILITY;
        	} else if (var1.id == CMD_SUBGUI_PREFERENCE) {
        		guiCur = GUI_SUBGUI_PREFERENCE;
        	} else if (var1.id == CMD_SUBGUI_DIMENSIONS) {
        		guiCur = GUI_SUBGUI_DIMENSIONS;
        	}
        	initGui();
        } else {
        	
        }
        
        if (var1.id == CMD_CLOSE) {
        	sendPacket = true;
        	mc.thePlayer.closeScreen();
        	WeatherUtilConfig.processNBTToModConfigClient();
        	/*if (guiCur.equals("main")) {
        		sendPacket = true;
        		//mc.displayGuiScreen(null);
        		mc.thePlayer.closeScreen();
        	} else if (guiCur.equals(GUI_SUBGUI_PERFORMANCE) || guiCur.equals(GUI_SUBGUI_COMPATIBILITY) || guiCur.equals(GUI_SUBGUI_PREFERENCE)) {
        		guiCur = guiPrev;
        		initGui();
            }*/
        	
        }
        
        if (sendPacket) {
        	int val = 0;
    		//String username = "";
            //if (mc.thePlayer != null) username = mc.thePlayer.username;
            //nbtSendCache.setString("username", username); //irrelevant, overriden server side for safety
        	nbtSendCache.setString("command", "applySettings");
            //nbtSendCache.setInteger("cmdID", var1.id);
            //nbtSendCache.setString("guiCur", guiForPacket);
        	PacketHelper.sendClientPacket(PacketHelper.createPacketForNBTHandler("EZGuiData", nbtSendCache));
        }
    }
	
	public int sanitize(int val) {
		return sanitize(val, 0, 9999);
	}
	
	public int sanitize(int val, int min, int max) {
		if (val > max) val = max;
        if (val < min) val = min;
		return val;
	}
	
	public void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6)
    {
        float f = 0.00390625F / 2F;
        float f1 = 0.00390625F / 2F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + par6), (double)this.zLevel, (double)((float)(par3 + 0) * f), (double)((float)(par4 + par6) * f1));
        tessellator.addVertexWithUV((double)(par1 + par5), (double)(par2 + par6), (double)this.zLevel, (double)((float)(par3 + par5) * f), (double)((float)(par4 + par6) * f1));
        tessellator.addVertexWithUV((double)(par1 + par5), (double)(par2 + 0), (double)this.zLevel, (double)((float)(par3 + par5) * f), (double)((float)(par4 + 0) * f1));
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + 0), (double)this.zLevel, (double)((float)(par3 + 0) * f), (double)((float)(par4 + 0) * f1));
        tessellator.draw();
    }

}
