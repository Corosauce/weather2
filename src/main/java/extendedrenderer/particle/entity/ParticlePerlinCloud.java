package extendedrenderer.particle.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.phys.Vec3;
import weather2.ClientTickHandler;

public class ParticlePerlinCloud extends ParticleTexFX {

	public ParticlePerlinCloud(Level worldIn, double posXIn, double posYIn,
                               double posZIn, double mX, double mY, double mZ,
                               TextureAtlasSprite par8Item) {
		super((ClientLevel) worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
	}

	@Override
	public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {

		Vec3 Vector3d = renderInfo.getPosition();
		float posXCamera = (float)(Mth.lerp(partialTicks, this.xo, this.x) - Vector3d.x());
		float posYCamera = (float)(Mth.lerp(partialTicks, this.yo, this.y) - Vector3d.y());
		float posZCamera = (float)(Mth.lerp(partialTicks, this.zo, this.z) - Vector3d.z());
		Quaternion quaternion;
		// override rotations
		quaternion = new Quaternion(0, 0, 0, 1);
		if (facePlayerYaw) {
			quaternion.mul(Vector3f.YP.rotationDegrees(-renderInfo.getYRot()));
		} else {
			quaternion.mul(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, this.prevRotationYaw, rotationYaw)));
		}
		quaternion.mul(Vector3f.XP.rotationDegrees(Mth.lerp(partialTicks, this.prevRotationPitch, rotationPitch)));

		/*Vector3f[] avector3f = new Vector3f[]{
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

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f[i];
			vector3f.transform(quaternion);
			vector3f.mul(scale);
			vector3f.add(posXCamera, posYCamera, posZCamera);
		}

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f2[i];
			vector3f.transform(quaternion);
			vector3f.mul(scale);
			vector3f.add(posXCamera, posYCamera, posZCamera);
		}

		for(int i = 0; i < 4; ++i) {
			Vector3f vector3f = avector3f3[i];
			vector3f.transform(quaternion);
			vector3f.mul(scale);
			vector3f.add(posXCamera, posYCamera, posZCamera);
		}*/

		float scale = this.getQuadSize(partialTicks);

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
		/*buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
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
		buffer.vertex(avector3f3[3].x(), avector3f3[3].y(), avector3f3[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();*/

		PerlinNoise perlinNoise = ClientTickHandler.weatherManager.cloudManager.getPerlinNoise();
		/*int indexX = index % xWide;
		int indexZ = index / xWide;*/
		double scaleNoise = 3;
		long time = (long) (Minecraft.getInstance().level.getGameTime() * 0.3F);
		double posYAdj = 0;
		int cloudRadius = 50;
		int spacingTest = 5;
		//not radial, but a quick test
		double distMax = cloudRadius;
		for (int x = -cloudRadius; x <= cloudRadius; x++) {
			for (int z = -cloudRadius; z <= cloudRadius; z++) {
				double distToCenter = Math.sqrt(x * x + z * z);
				int indexX = (int)Math.floor(this.x) + x;
				int indexZ = (int)Math.floor(this.z) + z;
				double noiseVal = perlinNoise.getValue(((indexX) * scaleNoise) + time, ((indexZ) * scaleNoise) + time, posYAdj) + 0.2F;
				noiseVal *= 1F;

				Vector3f[] square = new Vector3f[]{
						new Vector3f(-1.0F, 0.0F, -1.0F),
						new Vector3f(-1.0F, 0.0F, 1.0F),
						new Vector3f(1.0F, 0.0F, 1.0F),
						new Vector3f(1.0F, 0.0F, -1.0F)};

				/*Vector3f[] square = new Vector3f[]{
						new Vector3f(-1.0F + x, 0.0F, -1.0F + z),
						new Vector3f(-1.0F + x, 0.0F, 1.0F + z),
						new Vector3f(1.0F + x, 0.0F, 1.0F + z),
						new Vector3f(1.0F + x, 0.0F, -1.0F + z)};*/

				for(int i = 0; i < 4; ++i) {
					Vector3f vector3f = square[i];
					vector3f.transform(quaternion);
					vector3f.mul(spacingTest * 0.5F);
					//vector3f.add(posXCamera, posYCamera, posZCamera);
					vector3f.add(posXCamera + x * spacingTest, posYCamera, posZCamera + z * spacingTest);
				}

				if (noiseVal < 0) noiseVal = 0;
				if (noiseVal > 1) noiseVal = 1;
				this.alpha = 0.6F;

				float lowPass = 0.4F;
				lowPass = 0.2F;
				lowPass = 0.0F;
				double noiseThreshold = Math.sin(((time) / 150F)) * 0.35D;
				//noiseThreshold = 0.0D;
				double yDist = Math.abs(y - 3);
				//for perlin
				//noiseThreshold += yDist * 0.06F;
				double distAmp = 0.6D;
				noiseThreshold += ((distToCenter / distMax) * distAmp);
				lowPass += ((distToCenter / distMax) * distAmp);
				if (noiseVal > lowPass) {
					float r = (float) noiseVal;
					float g = (float) noiseVal;
					float b = (float) noiseVal;

					buffer.vertex(square[0].x(), square[0].y(), square[0].z()).uv(f8, f6).color(r, g, b, this.alpha).uv2(j).endVertex();
					buffer.vertex(square[1].x(), square[1].y(), square[1].z()).uv(f8, f5).color(r, g, b, this.alpha).uv2(j).endVertex();
					buffer.vertex(square[2].x(), square[2].y(), square[2].z()).uv(f7, f5).color(r, g, b, this.alpha).uv2(j).endVertex();
					buffer.vertex(square[3].x(), square[3].y(), square[3].z()).uv(f7, f6).color(r, g, b, this.alpha).uv2(j).endVertex();
				}
			}
		}

	}
}
