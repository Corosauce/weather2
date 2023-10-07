package extendedrenderer.particle.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ParticleCrossSection extends ParticleTexFX {

	public ParticleCrossSection(Level worldIn, double posXIn, double posYIn,
                                double posZIn, double mX, double mY, double mZ,
                                TextureAtlasSprite par8Item) {
		super((ClientLevel) worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
	}

	@Override
	public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {

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

		float f4 = this.getQuadSize(partialTicks);

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f[i];
			vector3f.rotate(quaternion);
			vector3f.mul(f4);
			vector3f.add(f, f1, f2);
		}

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f2[i];
			vector3f.rotate(quaternion);
			vector3f.mul(f4);
			vector3f.add(f, f1, f2);
		}

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f3[i];
			vector3f.rotate(quaternion);
			vector3f.mul(f4);
			vector3f.add(f, f1, f2);
		}

		float f7 = this.getU0();
		float f8 = this.getU1();
		float f5 = this.getV0();
		float f6 = this.getV1();
		int j = this.getLightColor(partialTicks);
		if (j > 0) {
			lastNonZeroBrightness = j;
		} else {
			j = lastNonZeroBrightness;
		}
		buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		buffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		buffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		buffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();

		buffer.vertex(avector3f2[0].x(), avector3f2[0].y(), avector3f2[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		buffer.vertex(avector3f2[1].x(), avector3f2[1].y(), avector3f2[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		buffer.vertex(avector3f2[2].x(), avector3f2[2].y(), avector3f2[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		buffer.vertex(avector3f2[3].x(), avector3f2[3].y(), avector3f2[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();

		buffer.vertex(avector3f3[0].x(), avector3f3[0].y(), avector3f3[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		buffer.vertex(avector3f3[1].x(), avector3f3[1].y(), avector3f3[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		buffer.vertex(avector3f3[2].x(), avector3f3[2].y(), avector3f3[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
		buffer.vertex(avector3f3[3].x(), avector3f3[3].y(), avector3f3[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();

	}
}
