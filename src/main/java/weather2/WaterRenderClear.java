package weather2;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class WaterRenderClear {

    private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation(Weather.modID, "textures/misc/underwater.png");

    @SubscribeEvent
    public void onBlockOverlay(RenderBlockOverlayEvent event) {

        //durr
        ReflectionHelper.setPrivateValue(Block.class, Blocks.WATER, 0, "lightOpacity");

        if (event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.WATER) {
            IBlockState atPos = Minecraft.getMinecraft().theWorld.getBlockState(event.getBlockPos());
            if (atPos.getMaterial().isLiquid()) {
                event.setCanceled(true);
                Minecraft mc = Minecraft.getMinecraft();

                // ItemRenderer#renderOverlays

                mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
                Tessellator tessellator = Tessellator.getInstance();
                VertexBuffer vertexbuffer = tessellator.getBuffer();
                float f = mc.thePlayer.getBrightness(event.getRenderPartialTicks());
                GlStateManager.color(f, f, f, 0.6F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.pushMatrix();
                float f1 = 4.0F;
                float f2 = -1.0F;
                float f3 = 1.0F;
                float f4 = -1.0F;
                float f5 = 1.0F;
                float f6 = -0.5F;
                float f7 = -mc.thePlayer.rotationYaw / 64.0F;
                float f8 = mc.thePlayer.rotationPitch / 64.0F;
                vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
                vertexbuffer.pos(-1.0D, -1.0D, -0.5D).tex((double)(4.0F + f7), (double)(4.0F + f8)).endVertex();
                vertexbuffer.pos(1.0D, -1.0D, -0.5D).tex((double)(0.0F + f7), (double)(4.0F + f8)).endVertex();
                vertexbuffer.pos(1.0D, 1.0D, -0.5D).tex((double)(0.0F + f7), (double)(0.0F + f8)).endVertex();
                vertexbuffer.pos(-1.0D, 1.0D, -0.5D).tex((double)(4.0F + f7), (double)(0.0F + f8)).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableBlend();
            }
        }
    }
    
    @SubscribeEvent
    public void onFogDensity(EntityViewRenderEvent.FogDensity event) {
        if (event.getState().getMaterial().isLiquid()) {
        //if (event.getState().getBlock() == BlockRegistry.tropicsWater) {
            event.setCanceled(true);
            
            GlStateManager.setFog(GlStateManager.FogMode.EXP);
            event.setDensity(0.00115F);
        }
    }
}
