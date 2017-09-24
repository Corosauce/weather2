package weather2.client.gui.elements;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiButtonCycle extends GuiButton
{
    
    /*public int texID = 0;
    public static final ResourceLocation resGUI = new ResourceLocation(ZombieCraftMod.modID + ":textures/gui/zceditgui.png");*/

	public List<String> listDescEntries = new ArrayList<String>();
	public int index = 0;
	
	/*boolean boolState = false;
	
	String strEnabled = "";
	String strDisabled = "";*/

    public GuiButtonCycle(int par1, int par2, int par3, int par4, int par5, List<String> parEntries, int parDefaultIndex)
    {
    	super(par1, par2, par3, par4, par5, "unused");
        this.width = 20;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = par1;
        this.x = par2;
        this.y = par3;
        this.width = par4;
        this.height = par5;
        this.displayString = "unused";
        this.listDescEntries = parEntries;
        this.index = parDefaultIndex;
    }
    
    public void cycleIndex() {
    	if (!enabled) return;
    	index++;
    	if (index >= listDescEntries.size()) {
    		index = 0;
    	}
    }
    
    public int getIndex() {
    	return index;
    }
    
    public void setIndex(int parIndex) {
    	index = parIndex;
    }
    
    public String getDisplayString() {
    	return listDescEntries.get(index);
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    @Override
    public int getHoverState(boolean par1)
    {
        byte var2 = 1;

        if (par1)
        {
            var2 = 2;
        }
        else if (!this.enabled)
        {
            var2 = 0;
        }

        return var2;
    }

    /**
     * Draws this button to the screen.
     */

    @Override
    public void drawButton(Minecraft par1Minecraft, int par2, int par3, float partialTicks)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = par1Minecraft.fontRenderer;
            par1Minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = par2 >= this.x && par3 >= this.y && par2 < this.x + this.width && par3 < this.y + this.height;
            int k = this.getHoverState(this.hovered);
            this.drawTexturedModalRect(this.x, this.y, 0, 46 + k * 20, this.width / 2, this.height);
            this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
            this.mouseDragged(par1Minecraft, par2, par3);
            int l = 14737632;

            if (!this.enabled)
            {
                l = -6250336;
            }
            else if (this.hovered)
            {
                l = 16777120;
            }
            String str = "";
            
            str = getDisplayString();
            
            /*if (boolState) {
            	str = "\u00A7" + '2' + strEnabled;
            } else {
            	str = "\u00A7" + 'c' + strDisabled;
            }*/
            
            this.drawCenteredString(fontrenderer, str, this.x + this.width / 2, this.y + (this.height - 8) / 2, l);
        }
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    @Override
    protected void mouseDragged(Minecraft par1Minecraft, int par2, int par3) {}

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    @Override
    public void mouseReleased(int par1, int par2) {}

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    @Override
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3)
    {
        return /*this.enabled && */this.visible && par2 >= this.x && par3 >= this.y && par2 < this.x + this.width && par3 < this.y + this.height;
    }
}
