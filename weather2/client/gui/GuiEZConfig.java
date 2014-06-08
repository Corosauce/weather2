package weather2.client.gui;

import java.util.ArrayList;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import weather2.Weather;
import weather2.client.gui.elements.GuiButtonBoolean;

import CoroUtil.packet.PacketHelper;

public class GuiEZConfig extends GuiScreen {

	//TileEntitySession tEnt;
	
	//see paper notes for most of design, random extras here
	//game state: NONE/LOBBY/ACTIVE
	
	public int xCenter;
	public int yCenter;
	public int xStart;
	public int yStart;
	
	//public int selectedWorld;
    //public List<IScrollingElement> listElements = new ArrayList<IScrollingElement>();
    //public GuiSlotImpl guiScrollable;
    //public GuiButton guiSelectMap;
    
    public ResourceLocation resGUI = new ResourceLocation(Weather.modID + ":textures/gui/gui512.png");
    
    public String guiCur = "main";
	public String guiPrev = "";
	
	//Elements
	public HashMap<String, GuiButton> buttonsLookup = new HashMap<String, GuiButton>();
	public HashMap<Integer, String> buttonsLookupInt = new HashMap<Integer, String>();
	//public GuiTextField textboxWorldName;
	public NBTTagCompound nbtSendCache = new NBTTagCompound();
	
	//config commands for gui/packet handler - might only be needed in gui since it just sets data on server side
	public static int CMD_CLOSE = 0;
	
	//subguis
	public static int CMD_SUBGUI_WAVE = 40;
	public static int CMD_SUBGUI_ITEMS = 41;
	public static String GUI_SUBGUI_WAVE = "wave";
	public static String GUI_SUBGUI_ITEMS = "items";
	
	//other elements
	public static int CMD_BOOL_CUSTOMLIGHTING = 2;
	public static int CMD_BOOL_CUSTOMTIME = 3;
	public static int CMD_BOOL_SHOWSKY = 4;
	
	//public static String BOOL_CUSTOMLIGHTING = "ya";
	
	//public static String TXT_CUSTOMLIGHTING = "customLighting";
	
	/** The X size of the inventory window in pixels. */
    protected int xSize = 176;

    /** The Y size of the inventory window in pixels. */
    protected int ySize = 166;
	
	public GuiEZConfig () {
		super();
		nbtSendCache.setCompoundTag("mapData", new NBTTagCompound());
	}
	
	@Override
	public void onGuiClosed() {
		// TODO Auto-generated method stub
		super.onGuiClosed();
		//a fix for container using gui opening on client side that doesnt need slot manip - might not have been needed, below was doing initGui on main gui close
		Minecraft.getMinecraft().thePlayer.openContainer = Minecraft.getMinecraft().thePlayer.inventoryContainer;
	}
	
	public void addButton(String lookupName, GuiButton btn) {
		buttonsLookup.put(lookupName, btn);
		buttonsLookupInt.put(btn.id, lookupName);
		buttonList.add(btn);
	}
	
	/*public void addTextBox(String lookupName, GuiTextFieldZC textBox) {
		textBoxes.add(textBox);
		textBoxesLookup.put(lookupName, textBox);
	}*/
	
	public void resetGuiElements() {
		buttonList.clear();
		buttonsLookup.clear();
		buttonsLookupInt.clear();
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
		
		int yEleSize = 20;
		
		this.drawString(this.fontRenderer, "ZC Level Configuration" + (guiCur.equals("main") ? "" : " - SubGUI: " + guiCur), xStart+7, yStart-9, 16777215);
		
		if (guiCur.equals("main")) {
			String vals = "";
			World world = Minecraft.getMinecraft().theWorld;
			if (world != null) {
				for (int i = 0; i < 16; i++) {
					try {
						vals += (i != 0 ? "," : "") + String.valueOf(world.provider.lightBrightnessTable[i]).substring(0, 3);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			this.drawString(this.fontRenderer, "Custom Lighting", xStart+7, yStart+8, 16777215);
			
			//this.drawString(this.fontRenderer, "Current Vals 0-15: " + vals, xStart+5, yStart+6+40, 16777215);
			
			this.drawString(this.fontRenderer, "Lock sky: ", xStart+7, yStart+8+yEleSize*2, 16777215);
			
			//subguis
			//this.drawString(this.fontRenderer, "Wave ", xStart+7, yStart+8+130, 16777215);
		} else if (guiCur.equals("wave")) {
			
			int x2 = 90;
			int x3 = 180;
			int x4 = 270;
			
			this.drawString(this.fontRenderer, "Default Mob", xStart+7, yStart+8, 16777215);
			
			this.drawString(this.fontRenderer, "Starting count", xStart+7, yStart+8+yEleSize*2, 16777215);
			this.drawString(this.fontRenderer, "Count multiplier", xStart+7+x2, yStart+8+yEleSize*2, 16777215);
			
			this.drawString(this.fontRenderer, "Starting health", xStart+7, yStart+8+yEleSize*4, 16777215);
			this.drawString(this.fontRenderer, "Health multiplier", xStart+7+x2, yStart+8+yEleSize*4, 16777215);
			
			this.drawString(this.fontRenderer, "Starting speed", xStart+7, yStart+8+yEleSize*6, 16777215);
			this.drawString(this.fontRenderer, "Random speed", xStart+7+x2, yStart+8+yEleSize*6, 16777215);
			this.drawString(this.fontRenderer, "Speed multiplier", xStart+7+x3, yStart+8+yEleSize*6, 16777215);
			this.drawString(this.fontRenderer, "Speed max", xStart+7+x4, yStart+8+yEleSize*6, 16777215);
			
			this.drawString(this.fontRenderer, "Move Lead Dist", xStart+7, yStart+8+yEleSize*8, 16777215);
		}
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
	}
	
	@Override
	protected void keyTyped(char par1, int par2) {
		super.keyTyped(par1, par2);
		
		/*for (int i = 0; i < textBoxes.size(); i++) {
			GuiTextFieldZC gtf = textBoxes.get(i);
			if (gtf.isFocused()) {
				gtf.textboxKeyTyped(par1, par2);
				String newVal = gtf.getText();
				//SET YOUR NBT STUFF HERE!!!! DATA IS LOST ONCE THEY HIT BACK!
				nbtSendCache.getCompoundTag("mapData").setString(gtf.name, newVal);
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
		
		/*if (LevelConfig.nbtInfoClientMapConfig.getBoolean("markUpdated")) {
			LevelConfig.nbtInfoClientMapConfig.setBoolean("markUpdated", false);
			updateGuiElements();
		}*/
	}
	
	public void updateGuiElements() {
		//System.out.println("updateGuiElements");
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
        int btnWidthBool = 50;
        int btnHeight = 20;
        int btnHeightAndPadding = 24;
        int padding = 1;
        int btnSpacing = 22;
        
        if (guiCur.equals("main")) {
        	addButton("close", new GuiSmallButton(CMD_CLOSE, xStart + xSize - guiPadding - btnWidth, yStart + ySize - guiPadding - btnHeight, btnWidth, btnHeight, "Save & Close"));
        } else {
        	addButton("close", new GuiSmallButton(CMD_CLOSE, xStart + xSize - guiPadding - btnWidth, yStart + ySize - guiPadding - btnHeight, btnWidth, btnHeight, "Back"));
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
        
        //NBTTagCompound data = new NBTTagCompound();
        nbtSendCache.setBoolean("sync", true);
        PacketHelper.sendClientPacket(PacketHelper.createPacketForNBTHandler("MapConfig", nbtSendCache));
        updateGuiElements();
    }
	
	@Override
	protected void actionPerformed(GuiButton var1)
    {
		String guiForPacket = guiCur;
		boolean sendPacket = false;
		
		//current plan is to only send packet on gui close, covers all bases?
		//- due to subguis... element data might get lost..... but no...... its put in nbt send cache, so thats ok now?
		
		if (var1 instanceof GuiButtonBoolean) {
        	((GuiButtonBoolean) var1).setBooleanToggle();
            nbtSendCache.getCompoundTag("mapData").setBoolean(buttonsLookupInt.get(var1.id), ((GuiButtonBoolean) var1).getBoolean());
        }
		
		if (var1.id == CMD_SUBGUI_WAVE/* || */) {
        	guiPrev = guiCur;
        	if (var1.id == CMD_SUBGUI_WAVE) {
        		guiCur = GUI_SUBGUI_WAVE;
        	}
        	initGui();
        } else {
        	
        }
        
        if (var1.id == CMD_CLOSE) {
        	if (guiCur.equals("main")) {
        		sendPacket = true;
        		//mc.displayGuiScreen(null);
        		mc.thePlayer.closeScreen();
        	} else if (guiCur.equals(GUI_SUBGUI_WAVE)/* || */) {
        		guiCur = guiPrev;
        		initGui();
            }
        	
        }
        
        if (sendPacket) {
        	int val = 0;
    		//String username = "";
            //if (mc.thePlayer != null) username = mc.thePlayer.username;
            //nbtSendCache.setString("username", username); //irrelevant, overriden server side for safety
            nbtSendCache.setInteger("cmdID", var1.id);
            nbtSendCache.setString("guiCur", guiForPacket);
        	PacketHelper.sendClientPacket(PacketHelper.createPacketForNBTHandler("MapConfig", nbtSendCache));
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
