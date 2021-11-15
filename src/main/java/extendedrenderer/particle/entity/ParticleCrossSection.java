package extendedrenderer.particle.entity;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

public class ParticleCrossSection extends ParticleTexFX {

	public ParticleCrossSection(World worldIn, double posXIn, double posYIn,
                                double posZIn, double mX, double mY, double mZ,
                                TextureAtlasSprite par8Item) {
		super((ClientWorld) worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
	}

	@Override
	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {

		Vector3d Vector3d = renderInfo.getProjectedView();
		float f = (float)(MathHelper.lerp(partialTicks, this.prevPosX, this.posX) - Vector3d.getX());
		float f1 = (float)(MathHelper.lerp(partialTicks, this.prevPosY, this.posY) - Vector3d.getY());
		float f2 = (float)(MathHelper.lerp(partialTicks, this.prevPosZ, this.posZ) - Vector3d.getZ());
		Quaternion quaternion;
		if (this.facePlayer || (this.rotationPitch == 0 && this.rotationYaw == 0)) {
			quaternion = renderInfo.getRotation();
		} else {
			// override rotations
			quaternion = new Quaternion(0, 0, 0, 1);
			if (facePlayerYaw) {
				quaternion.multiply(Vector3f.YP.rotationDegrees(-renderInfo.getYaw()));
			} else {
				quaternion.multiply(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, this.prevRotationYaw, rotationYaw)));
			}
			quaternion.multiply(Vector3f.XP.rotationDegrees(MathHelper.lerp(partialTicks, this.prevRotationPitch, rotationPitch)));
		}

		Vector3f[] avector3f = new Vector3f[]{
				new Vector3f(-1.0F, -1.0F, 0.0F),
				new Vector3f(-1.0F, 1.0F, 0.0F),
				new Vector3f(1.0F, 1.0F, 0.0F),
				new Vector3f(1.0F, -1.0F, 0.0F)};

		Vector3f[] avector3f2 = new Vector3f[]{
				new Vector3f(0.0F, -1.0F, -1.0F),
				new Vector3f(0.0F, 1.0F, -1.0F),
				new Vector3f(0.0F, 1.0F, 1.0F),
				new Vector3f(0.0F, -1.0F, 1.0F)};

		Vector3f[] avector3f3 = new Vector3f[]{
				new Vector3f(-1.0F, 0.0F, -1.0F),
				new Vector3f(-1.0F, 0.0F, 1.0F),
				new Vector3f(1.0F, 0.0F, 1.0F),
				new Vector3f(1.0F, 0.0F, -1.0F)};

		float f4 = this.getScale(partialTicks);

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f[i];
			vector3f.transform(quaternion);
			vector3f.mul(f4);
			vector3f.add(f, f1, f2);
		}

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f2[i];
			vector3f.transform(quaternion);
			vector3f.mul(f4);
			vector3f.add(f, f1, f2);
		}

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f3[i];
			vector3f.transform(quaternion);
			vector3f.mul(f4);
			vector3f.add(f, f1, f2);
		}

		float f7 = this.getMinU();
		float f8 = this.getMaxU();
		float f5 = this.getMinV();
		float f6 = this.getMaxV();
		int j = this.getBrightnessForRender(partialTicks);
		if (j > 0) {
			lastNonZeroBrightness = j;
		} else {
			j = lastNonZeroBrightness;
		}
		buffer.pos(avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ()).tex(f8, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
		buffer.pos(avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ()).tex(f8, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
		buffer.pos(avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ()).tex(f7, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
		buffer.pos(avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ()).tex(f7, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();

		buffer.pos(avector3f2[0].getX(), avector3f2[0].getY(), avector3f2[0].getZ()).tex(f8, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
		buffer.pos(avector3f2[1].getX(), avector3f2[1].getY(), avector3f2[1].getZ()).tex(f8, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
		buffer.pos(avector3f2[2].getX(), avector3f2[2].getY(), avector3f2[2].getZ()).tex(f7, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
		buffer.pos(avector3f2[3].getX(), avector3f2[3].getY(), avector3f2[3].getZ()).tex(f7, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();

		buffer.pos(avector3f3[0].getX(), avector3f3[0].getY(), avector3f3[0].getZ()).tex(f8, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
		buffer.pos(avector3f3[1].getX(), avector3f3[1].getY(), avector3f3[1].getZ()).tex(f8, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
		buffer.pos(avector3f3[2].getX(), avector3f3[2].getY(), avector3f3[2].getZ()).tex(f7, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
		buffer.pos(avector3f3[3].getX(), avector3f3[3].getY(), avector3f3[3].getZ()).tex(f7, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();

	}
}
