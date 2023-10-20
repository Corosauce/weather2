package extendedrenderer.particle.entity;

import com.corosus.coroutil.util.CULog;
import com.corosus.coroutil.util.CoroUtilBlock;
import com.corosus.coroutil.util.CoroUtilMisc;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import extendedrenderer.particle.ParticleRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleCube extends ParticleTexFX {

	public ParticleCube(Level worldIn, double posXIn, double posYIn,
                        double posZIn, double mX, double mY, double mZ,
                        BlockState state) {
		super((ClientLevel) worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, ParticleRegistry.potato);

		/**
		 * really basic way to get a sprite from a blockstate, could easily get the wrong one if multiple quads are used per direction
		 * should do fine for most blocks that have the same texture on every side
		 */
		TextureAtlasSprite sprite = getSpriteFromState(state);
		if (sprite != null) {
			setSprite(sprite);
		} else {
			CULog.dbg("unable to find sprite to use from block: " + state);
			sprite = getSpriteFromState(Blocks.DIRT.defaultBlockState());
			//if (CoroUtilMisc.random().nextBoolean()) sprite = getSpriteFromState(Blocks.GRASS.defaultBlockState());
			if (sprite != null) {
				setSprite(sprite);
			}
		}
		int multiplier = Minecraft.getInstance().getBlockColors().getColor(state, this.level, CoroUtilBlock.blockPos(posXIn, posYIn, posZIn), 0);
		float mr = ((multiplier >>> 16) & 0xFF) / 255f;
		float mg = ((multiplier >>> 8) & 0xFF) / 255f;
		float mb = (multiplier & 0xFF) / 255f;
		setColor(mr, mg, mb);
	}

	public TextureAtlasSprite getSpriteFromState(BlockState state) {
		BlockRenderDispatcher blockrenderdispatcher = Minecraft.getInstance().getBlockRenderer();
		BakedModel model = blockrenderdispatcher.getBlockModel(state);
		for(Direction direction : Direction.values()) {
			List<BakedQuad> list = model.getQuads(state, direction, RandomSource.create());
			if (list.size() > 0) {
				return list.get(0).getSprite();
			}
			//plan b
			if (model.getParticleIcon() != null) {
				return model.getParticleIcon();
			}
		}
		return null;
	}

	@Override
	public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
		//if (true) return;
		Vec3 Vector3d = renderInfo.getPosition();
		float f = (float)(Mth.lerp(partialTicks, this.xo, this.x) - Vector3d.x());
		float f1 = (float)(Mth.lerp(partialTicks, this.yo, this.y) - Vector3d.y());
		float f2 = (float)(Mth.lerp(partialTicks, this.zo, this.z) - Vector3d.z());
		Quaternionf quaternion;
		if (this.facePlayer || (this.rotationPitch == 0 && this.rotationYaw == 0)) {
			quaternion = renderInfo.rotation();
		} else {
			// override rotations
			quaternion = new Quaternionf(0, 0, 0, 1);
			if (facePlayerYaw) {
				quaternion.mul(Axis.YP.rotationDegrees(-renderInfo.getYRot()));
			} else {
				quaternion.mul(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, this.prevRotationYaw, rotationYaw)));
			}
			quaternion.mul(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, this.prevRotationPitch, rotationPitch)));
		}

		TextureAtlasSprite sprite = null;

		List<Vector3f[]> faces = new ArrayList<>();

		Vector3f[] face;

		//xy -z
		face = new Vector3f[]{
				new Vector3f(-1.0F, -1.0F, -1.0F),
				new Vector3f(-1.0F, 1.0F, -1.0F),
				new Vector3f(1.0F, 1.0F, -1.0F),
				new Vector3f(1.0F, -1.0F, -1.0F)};
		faces.add(face);

		//xy +z
		face = new Vector3f[]{
				new Vector3f(-1.0F, -1.0F, 1.0F),
				new Vector3f(-1.0F, 1.0F, 1.0F),
				new Vector3f(1.0F, 1.0F, 1.0F),
				new Vector3f(1.0F, -1.0F, 1.0F)};
		faces.add(face);

		//yz -x
		face = new Vector3f[]{
				new Vector3f(-1.0F, -1.0F, -1.0F),
				new Vector3f(-1.0F, 1.0F, -1.0F),
				new Vector3f(-1.0F, 1.0F, 1.0F),
				new Vector3f(-1.0F, -1.0F, 1.0F)};
		faces.add(face);

		//yz +x
		face = new Vector3f[]{
				new Vector3f(1.0F, -1.0F, -1.0F),
				new Vector3f(1.0F, 1.0F, -1.0F),
				new Vector3f(1.0F, 1.0F, 1.0F),
				new Vector3f(1.0F, -1.0F, 1.0F)};
		faces.add(face);

		//xz -y
		face = new Vector3f[]{
				new Vector3f(-1.0F, -1.0F, -1.0F),
				new Vector3f(-1.0F, -1.0F, 1.0F),
				new Vector3f(1.0F, -1.0F, 1.0F),
				new Vector3f(1.0F, -1.0F, -1.0F)};
		faces.add(face);

		//xz +y
		face = new Vector3f[]{
				new Vector3f(-1.0F, 1.0F, -1.0F),
				new Vector3f(-1.0F, 1.0F, 1.0F),
				new Vector3f(1.0F, 1.0F, 1.0F),
				new Vector3f(1.0F, 1.0F, -1.0F)};
		faces.add(face);

		float f4 = this.getQuadSize(partialTicks);

		for (Vector3f[] entryFace : faces) {
			for(int i = 0; i < 4; ++i) {
				entryFace[i].rotate(quaternion);
				entryFace[i].mul(f4);
				entryFace[i].add(f, f1, f2);
			}
		}

		float f7 = this.getU0();
		float f8 = this.getU1();
		float f5 = this.getV0();
		float f6 = this.getV1();
		if (sprite != null) {
			f7 = sprite.getU0();
			f8 = sprite.getU1();
			f5 = sprite.getV0();
			f6 = sprite.getV1();
		}
		int j = this.getLightColor(partialTicks);
		if (j > 0) {
			lastNonZeroBrightness = j;
		} else {
			j = lastNonZeroBrightness;
		}
		for (Vector3f[] entryFace : faces) {
			buffer.vertex(entryFace[0].x(), entryFace[0].y(), entryFace[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
			buffer.vertex(entryFace[1].x(), entryFace[1].y(), entryFace[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
			buffer.vertex(entryFace[2].x(), entryFace[2].y(), entryFace[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
			buffer.vertex(entryFace[3].x(), entryFace[3].y(), entryFace[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		}

	}

	@Override
	public ParticleRenderType getRenderType() {
		return SORTED_OPAQUE_BLOCK;
		//return super.getRenderType();
	}
}
