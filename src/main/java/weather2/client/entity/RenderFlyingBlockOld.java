package weather2.client.entity;

import CoroUtil.util.CoroUtilParticle;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityMovingBlock;

@SideOnly(Side.CLIENT)
public class RenderFlyingBlockOld extends Render
{
	Block renderBlock;

    public RenderFlyingBlockOld(RenderManager manager, Block parBlock)
    {
    	super(manager);
    	renderBlock = parBlock;
    }
    
    @Override

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

    @Override
    public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
    	if (true) return;
        GL11.glPushMatrix();
        //GL11.glDisable(GL11.GL_FOG);

        int age = entity.ticksExisted * 5;

        float size = 0.3F;// - (age * 0.03F);

        if (size < 0) size = 0;

        if (entity instanceof EntityMovingBlock) {
        	size = 1;
        }

        GL11.glTranslatef((float)x, (float)y, (float)z);
        this.bindEntityTexture(entity);
        //this.loadTexture("/terrain.png");
        World var11 = entity.world;
        //GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glScalef(size, size, size);

        /*RenderBlocks rb = new RenderBlocks(var1.world);
        //Tessellator tess = Tessellator.instance;
        //tess.setBrightness(255);
        //tess.setColorOpaque_F(255, 255, 255);
        //renderBlock = Block.netherrack;
        if (var1 instanceof EntityMovingBlock) {
        	Block dynamicRenderBlock = ((EntityMovingBlock) var1).tile;
        	GL11.glRotatef((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
            GL11.glRotatef((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
            GL11.glRotatef((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
        	rb.setRenderBoundsFromBlock(dynamicRenderBlock);
	        rb.renderBlockAsItem(dynamicRenderBlock, 0, 0.8F);
        } else {
        	//the real one
	        rb.setRenderBoundsFromBlock(renderBlock);
	        rb.renderBlockAsItem(renderBlock, 0, 0.8F);

	        //the fake ones
        	for (int i = 0; i < Math.min(4, WeatherUtilParticle.maxRainDrops); i++) {
        		GL11.glPushMatrix();
        		GL11.glTranslatef((float)WeatherUtilParticle.rainPositions[i].xCoord * 3F, (float)WeatherUtilParticle.rainPositions[i].yCoord * 3F, (float)WeatherUtilParticle.rainPositions[i].zCoord * 3F);
        		GL11.glRotatef((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
                GL11.glRotatef((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
                GL11.glRotatef((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
        		rb.setRenderBoundsFromBlock(renderBlock);
    	        rb.renderBlockAsItem(renderBlock, 0, 0.8F);
        		//GL11.glTranslatef((float)-WeatherUtilParticle.rainPositions[i].xCoord, (float)-WeatherUtilParticle.rainPositions[i].yCoord, (float)-WeatherUtilParticle.rainPositions[i].zCoord);
        		GL11.glPopMatrix();
        	}

        }*/

        IBlockState state = null;

        if (entity instanceof EntityMovingBlock) {
        	state = ((EntityMovingBlock) entity).tile.getDefaultState();
        } else {
        	if (renderBlock != null) {
        		state = renderBlock.getDefaultState();
        	}
        }

        //TODO: 1.8 fake hail rendering for extra effect without extra entities, see 1.7.10 code

        if (state != null)
        {
            this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            IBlockState iblockstate = state;
            Block block = iblockstate.getBlock();
            BlockPos blockpos = new BlockPos(entity);
            World world = var11;

            if (iblockstate != world.getBlockState(blockpos)/* && block.getRenderType(iblockstate) != -1*/)
            {
                if (block.getRenderType(iblockstate) == EnumBlockRenderType.MODEL)
                {
                	if (entity instanceof EntityMovingBlock) {
                		try {
                			GlStateManager.pushMatrix();
    	                    GlStateManager.translate((float)x, (float)y, (float)z);
    	                    GlStateManager.disableLighting();
    	                    Tessellator tessellator = Tessellator.getInstance();
    	                    BufferBuilder worldrenderer = tessellator.getBuffer();
    	                    worldrenderer.begin(7, DefaultVertexFormats.BLOCK);
    	                    int i = blockpos.getX();
    	                    int j = blockpos.getY();
    	                    int k = blockpos.getZ();
    	                    worldrenderer.setTranslation((double)((float)(-i) - 0.5F), (double)(-j), (double)((float)(-k) - 0.5F));
    	                    GlStateManager.rotate((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
    	                    GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
    	                    GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
    	                    BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
    	                    IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(iblockstate/*, world, (BlockPos)null*/);
    	                    blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, iblockstate, blockpos, worldrenderer, false);
    	                    worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
    	                    tessellator.draw();
    	                    GlStateManager.enableLighting();
    	                    GlStateManager.popMatrix();
						} catch (Exception e) {
							//catch this issue, need real fix, https://github.com/Corosauce/weather2/issues/62 - java.lang.IllegalArgumentException: Cannot get property PropertyEnum{name=variant, clazz=class biomesoplenty.common.enums.BOPTrees, values=[mangrove, palm, redwood, willow]} as it does not exist in BlockState{block=minecraft:air, properties=[]}
						}

                	} else if (entity instanceof EntityIceBall) {
                		for (int ii = 0; ii < Math.min(4, CoroUtilParticle.maxRainDrops); ii++) {
                			GlStateManager.pushMatrix();
    	                    //GlStateManager.translate((float)x, (float)y, (float)z);
                			GlStateManager.translate((float)CoroUtilParticle.rainPositions[ii].xCoord * 3F, (float)CoroUtilParticle.rainPositions[ii].yCoord * 3F, (float)CoroUtilParticle.rainPositions[ii].zCoord * 3F);
    	                    GlStateManager.disableLighting();
    	                    Tessellator tessellator = Tessellator.getInstance();
    	                    BufferBuilder worldrenderer = tessellator.getBuffer();
    	                    worldrenderer.begin(7, DefaultVertexFormats.BLOCK);
    	                    int i = blockpos.getX();
    	                    int j = blockpos.getY();
    	                    int k = blockpos.getZ();
    	                    worldrenderer.setTranslation((double)((float)(-i) - 0.5F), (double)(-j), (double)((float)(-k) - 0.5F));
    	                    GlStateManager.rotate((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
    	                    GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
    	                    GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
    	                    BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
    	                    IBakedModel ibakedmodel = blockrendererdispatcher.getModelForState(iblockstate/*, world, (BlockPos)null*/);
    	                    blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, iblockstate, blockpos, worldrenderer, false);
    	                    worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
    	                    tessellator.draw();
    	                    GlStateManager.enableLighting();
    	                    GlStateManager.popMatrix();
                		}
                	}
                }
            }
        }

        //GL11.glEnable(GL11.GL_FOG);
        //GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }
}
