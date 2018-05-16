package weather2.client.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;

import weather2.config.ConfigMisc;
import weather2.entity.EntityIceBall;
import weather2.entity.EntityMovingBlock;
import CoroUtil.util.CoroUtilParticle;

@SideOnly(Side.CLIENT)
public class RenderFlyingBlock extends Render<Entity>
{
	Block renderBlock;
	
    public RenderFlyingBlock(RenderManager manager, Block parBlock)
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
		IBlockState state = null;

		if (entity instanceof EntityMovingBlock) {
			if (((EntityMovingBlock) entity).stateCached != null) {
				state = ((EntityMovingBlock) entity).stateCached;
			} else {
				state = ((EntityMovingBlock) entity).tile.getDefaultState();
			}
		} else {
			if (renderBlock != null) {
				state = renderBlock.getDefaultState();
			}
		}

		try {
			if (state != null)
			{
				IBlockState iblockstate = state;

				int age = entity.ticksExisted * 5;

				if (iblockstate.getRenderType() == EnumBlockRenderType.MODEL)
				{
					World world = entity.world;

					if (iblockstate != world.getBlockState(new BlockPos(entity)) && iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE)
					{
						this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
						GlStateManager.pushMatrix();
						GlStateManager.disableLighting();
						Tessellator tessellator = Tessellator.getInstance();
						BufferBuilder bufferbuilder = tessellator.getBuffer();

						if (this.renderOutlines)
						{
							GlStateManager.enableColorMaterial();
							GlStateManager.enableOutlineMode(this.getTeamColor(entity));
						}

						bufferbuilder.begin(7, DefaultVertexFormats.BLOCK);
						BlockPos blockpos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);
						//GlStateManager.translate((float)(x - (double)blockpos.getX() - 0.5D), (float)(y - (double)blockpos.getY()), (float)(z - (double)blockpos.getZ() - 0.5D));
						GlStateManager.translate((float)(x), (float)(y), (float)(z));
						bufferbuilder.setTranslation((double)((float)(-blockpos.getX()) - 0.5F), (double)(-blockpos.getY()), (double)((float)(-blockpos.getZ()) - 0.5F));
						GlStateManager.rotate((float)(age * 0.1F * 180.0D / 12.566370964050293D - 0.0D), 1.0F, 0.0F, 0.0F);
						GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 1.0F, 0.0F);
						GlStateManager.rotate((float)(age * 0.1F * 180.0D / (Math.PI * 2D) - 0.0D), 0.0F, 0.0F, 1.0F);
						if (entity instanceof EntityIceBall) {
							float iceScale = 0.3F;
							GlStateManager.scale(iceScale, iceScale, iceScale);
						}
						BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
						blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(iblockstate), iblockstate, blockpos, bufferbuilder, false, MathHelper.getPositionRandom(entity.getPosition()));
						bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
						tessellator.draw();

						if (this.renderOutlines)
						{
							GlStateManager.disableOutlineMode();
							GlStateManager.disableColorMaterial();
						}

						GlStateManager.enableLighting();
						GlStateManager.popMatrix();
						super.doRender(entity, x, y, z, entityYaw, partialTicks);
					}
				}
			}
		} catch (Exception ex) {
			if (ConfigMisc.consoleDebug) {
				ex.printStackTrace();
			}
		}


    }
}
