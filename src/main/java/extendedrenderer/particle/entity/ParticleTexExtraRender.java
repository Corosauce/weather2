package extendedrenderer.particle.entity;

import CoroUtil.util.CoroUtilParticle;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;
import weather2.ClientTickHandler;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.wind.WindManager;

public class ParticleTexExtraRender extends ParticleTexFX {

	private int severityOfRainRate = 2;

	private int extraParticlesBaseAmount = 5;

	public boolean noExtraParticles = false;

	private float extraRandomSecondaryYawRotation = 360;

	//public float[] cachedLight;
	
	public ParticleTexExtraRender(ClientWorld worldIn, double posXIn, double posYIn,
								  double posZIn, double mX, double mY, double mZ,
								  TextureAtlasSprite par8Item) {
		super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);

		/*cachedLight = new float[CoroUtilParticle.rainPositions.length];
		if (worldObj.getGameTime() % 5 == 0) {
			for (int i = 0; i < cachedLight.length; i++) {
				Vector3 vec = CoroUtilParticle.rainPositions[i];
				cachedLight[i] = getBrightnessNonLightmap(new BlockPos(posX+vec.xCoord, posY+vec.yCoord, posZ+vec.zCoord), 1F);
			}
		}*/
	}

	public int getSeverityOfRainRate() {
		return severityOfRainRate;
	}

	public void setSeverityOfRainRate(int severityOfRainRate) {
		this.severityOfRainRate = severityOfRainRate;
	}

	public int getExtraParticlesBaseAmount() {
		return extraParticlesBaseAmount;
	}

	public void setExtraParticlesBaseAmount(int extraParticlesBaseAmount) {
		this.extraParticlesBaseAmount = extraParticlesBaseAmount;
	}

	@Override
	public void tickExtraRotations() {
		//super.tickExtraRotations();

		WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
		if (weatherMan == null) return;
		WindManager windMan = weatherMan.getWindManager();
		if (windMan == null) return;

		if (isSlantParticleToWind()) {
			double speed = motionX * motionX + motionZ * motionZ;
			rotationYaw = -(float)Math.toDegrees(Math.atan2(motionZ, motionX)) - 90;
			rotationPitch = Math.min(45, (float)(speed * 120));
			rotationPitch += (this.getEntityId() % 10) - 5;
		}

		windMan.applyWindForceNew(this, 1F / 2F, 0.5F);
	}

	@Override
	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
		//override rotations
        Vector3d Vector3d = renderInfo.getProjectedView();
        Quaternion quaternion;
        if (this.facePlayer || (this.rotationPitch == 0 && this.rotationYaw == 0)) {
           quaternion = renderInfo.getRotation();
        } else {
           // override rotations
           quaternion = new Quaternion(0, 0, 0, 1);
           quaternion.multiply(Vector3f.YP.rotationDegrees(this.rotationYaw));
           quaternion.multiply(Vector3f.XP.rotationDegrees(this.rotationPitch));
           if (extraRandomSecondaryYawRotation > 0) {
			   quaternion.multiply(Vector3f.YP.rotationDegrees(getEntityId() % extraRandomSecondaryYawRotation));
		   }
        }
        
        float posX = (float)(MathHelper.lerp((double)partialTicks, this.prevPosX, this.posX) - Vector3d.getX());
        float posY = (float)(MathHelper.lerp((double)partialTicks, this.prevPosY, this.posY) - Vector3d.getY());
        float posZ = (float)(MathHelper.lerp((double)partialTicks, this.prevPosZ, this.posZ) - Vector3d.getZ());


		float f = this.getMinU();
		float f1 = this.getMaxU();
		float f2 = this.getMinV();
		float f3 = this.getMaxV();

		float fixY = 0;

		float part = 16F / 3F;
		float offset = 0;
		float posBottom = (float)(this.posY - 10D);

		float height = world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(this.posX, this.posY, this.posZ)).getY();

		if (posBottom < height) {
			float diff = height - posBottom;
			offset = diff;
			fixY = 0;//diff * 1.0F;
			if (offset > part) offset = part;
		}

		int renderAmount = 0;
		if (noExtraParticles) {
			renderAmount = 1;
		} else {
			renderAmount = Math.min(extraParticlesBaseAmount + ((Math.max(0, severityOfRainRate-1)) * 5), CoroUtilParticle.maxRainDrops);
		}

		//catch code hotload crash, doesnt help much anyways
		try {
			for (int ii = 0; ii < renderAmount; ii++) {
				double xx = 0;
				double zz = 0;
				double yy = 0;
				if (ii != 0) {
					xx = CoroUtilParticle.rainPositions[ii].x;
					zz = CoroUtilParticle.rainPositions[ii].z;
					yy = CoroUtilParticle.rainPositions[ii].y;
				}

				//prevent precip under overhangs/inside for extra render
				if (this.isDontRenderUnderTopmostBlock()) {
					int height2 = world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(this.posX + xx, this.posY, this.posZ + zz)).getY();
					if (this.posY + yy <= height2) continue;
				}

				//TODO: 1.14 uncomment
				/*if (ii != 0) {
					RotatingParticleManager.debugParticleRenderCount++;
				}*/

				/*int height = entityIn.world.getPrecipitationHeight(new BlockPos(ActiveRenderInfo.getPosition().xCoord + f5, this.posY + f6, ActiveRenderInfo.getPosition().zCoord + f7)).getY();
				if (ActiveRenderInfo.getPosition().yCoord + f6 <= height) continue;*/

				int i = this.getBrightnessForRender(partialTicks);
				if (i > 0) {
					setLastNonZeroBrightness(i);
				} else {
					i = getLastNonZeroBrightness();
				}

		        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
		        float scale = this.getScale(partialTicks);

		        
		        for(int v = 0; v < 4; ++v) {
		           Vector3f vector3f = avector3f[v];
		           vector3f.transform(quaternion);
		           vector3f.mul(scale);
		           vector3f.add(posX, posY, posZ);
		        }

				buffer.pos(xx + avector3f[0].getX(), yy + avector3f[0].getY(), zz + avector3f[0].getZ()).tex(f1, f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(i).endVertex();
				buffer.pos(xx + avector3f[1].getX(), yy + avector3f[1].getY(), zz + avector3f[1].getZ()).tex(f1, f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(i).endVertex();
				buffer.pos(xx + avector3f[2].getX(), yy + avector3f[2].getY(), zz + avector3f[2].getZ()).tex(f, f2).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(i).endVertex();
				buffer.pos(xx + avector3f[3].getX(), yy + avector3f[3].getY(), zz + avector3f[3].getZ()).tex(f, f3).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(i).endVertex();
			}
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
		


        
	}

	//TODO: 1.14 uncomment
	/*public void renderParticleForShader(InstancedMeshParticle mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
										float partialTicks, float rotationX, float rotationZ,
										float rotationYZ, float rotationXY, float rotationXZ) {

		float posX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks);
		float posY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks);
		float posZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks);
		//Vector3f pos = new Vector3f((float) (entityIn.posX - particle.posX), (float) (entityIn.posY - particle.posY), (float) (entityIn.posZ - particle.posZ));

		int renderAmount = 0;
		if (noExtraParticles) {
			renderAmount = 1;
		} else {
			renderAmount = Math.min(extraParticlesBaseAmount + ((Math.max(0, severityOfRainRate-1)) * 5), CoroUtilParticle.maxRainDrops);
		}

		for (int iii = 0; iii < renderAmount; iii++) {

			if (mesh.curBufferPos >= mesh.numInstances) return;

			Vector3f pos;

			if (iii != 0) {
				pos = new Vector3f(posX + (float) CoroUtilParticle.rainPositions[iii].xCoord,
						posY + (float) CoroUtilParticle.rainPositions[iii].yCoord,
						posZ + (float) CoroUtilParticle.rainPositions[iii].zCoord);
			} else {
				pos = new Vector3f(posX, posY, posZ);
			}

			if (false && useRotationAroundCenter) {
				float deltaRot = rotationAroundCenterPrev + (rotationAroundCenter - rotationAroundCenterPrev) * partialTicks;
				float rotX = (float) Math.sin(Math.toRadians(deltaRot));
				float rotZ = (float) Math.cos(Math.toRadians(deltaRot));
				pos.x += rotX * rotationDistAroundCenter;
				pos.z += rotZ * rotationDistAroundCenter;
			}

			if (this.isDontRenderUnderTopmostBlock()) {
				int height = world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(pos.x, this.posY, pos.z)).getY();
				if (pos.y <= height) continue;
			}

			//adjust to relative to camera positions finally
			pos.x -= interpPosX;
			pos.y -= interpPosY;
			pos.z -= interpPosZ;

			Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);

			//adjust to perspective and camera
			//Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
			//upload to buffer
			modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), mesh.instanceDataBuffer);

			//brightness
			float brightness;
			//brightness = CoroUtilBlockLightCache.getBrightnessCached(worldObj, pos.x, pos.y, pos.z);
			//brightness = this.brightnessCache;
			if (fastLight) {
				brightness = CoroUtilBlockLightCache.brightnessPlayer;
			} else {
				brightness = CoroUtilBlockLightCache.getBrightnessCached(world, (float)this.posX, (float)this.posY, (float)this.posZ);
			}

			//brightness to buffer
			mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS, brightness);

			//rgba to buffer
			int rgbaIndex = 0;
			mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
					+ mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.particleRed);
			mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
					+ mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.particleGreen);
			mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
					+ mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.particleBlue);
			mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
					+ mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getAlphaF());

			mesh.curBufferPos++;
		}

	}*/

	/*@Override
	public void renderParticleForShaderTest(InstancedMeshParticle mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {


		float posX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks);
		float posY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks);
		float posZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks);

		int renderAmount = 0;
		if (noExtraParticles) {
			renderAmount = 1;
		} else {
			renderAmount = Math.min(extraParticlesBaseAmount + ((Math.max(0, severityOfRainRate-1)) * 5), CoroUtilParticle.maxRainDrops);
		}

		for (int iii = 0; iii < renderAmount; iii++) {

			if (mesh.curBufferPos >= mesh.numInstances) return;

			Vector3f pos;

			if (iii != 0) {
				pos = new Vector3f(posX + (float) CoroUtilParticle.rainPositions[iii].xCoord,
						posY + (float) CoroUtilParticle.rainPositions[iii].yCoord,
						posZ + (float) CoroUtilParticle.rainPositions[iii].zCoord);
			} else {
				pos = new Vector3f(posX, posY, posZ);
			}

			if (this.isDontRenderUnderTopmostBlock()) {
				int height = this.world.getPrecipitationHeight(new BlockPos(pos.x, this.posY, pos.z)).getY();
				if (pos.y <= height) continue;
			}

			//adjust to relative to camera positions finally
			pos.x -= interpPosX;
			pos.y -= interpPosY;
			pos.z -= interpPosZ;

			int rgbaIndex = 0;
			mesh.instanceDataBufferTest.put(mesh.INSTANCE_SIZE_FLOATS_TEST * (mesh.curBufferPos)
					+ (rgbaIndex++), this.getRedColorF());
			mesh.instanceDataBufferTest.put(mesh.INSTANCE_SIZE_FLOATS_TEST * (mesh.curBufferPos)
					+ (rgbaIndex++), this.getGreenColorF());
			mesh.instanceDataBufferTest.put(mesh.INSTANCE_SIZE_FLOATS_TEST * (mesh.curBufferPos)
					+ (rgbaIndex++), this.getBlueColorF());
			mesh.instanceDataBufferTest.put(mesh.INSTANCE_SIZE_FLOATS_TEST * (mesh.curBufferPos)
					+ (rgbaIndex++), this.getAlphaF());

			mesh.curBufferPos++;

		}

	}*/

	/*@Override
	public void updateQuaternion(Entity camera) {

		if (camera != null) {
			if (this.facePlayer) {
				this.rotationYaw = camera.rotationYaw;
				this.rotationPitch = camera.rotationPitch;
			} else if (facePlayerYaw) {
				this.rotationYaw = camera.rotationYaw;
			}
		}

		Quaternion qY = new Quaternion();
		Quaternion qX = new Quaternion();
		qY.setFromAxisAngle(new Vector4f(0, 1, 0, (float)Math.toRadians(-this.rotationYaw - 180F)));
		qX.setFromAxisAngle(new Vector4f(1, 0, 0, (float)Math.toRadians(-this.rotationPitch)));
		if (this.rotateOrderXY) {
			Quaternion.mul(qX, qY, this.rotation);
		} else {
			Quaternion.mul(qY, qX, this.rotation);

			if (extraYRotation != 0) {
				//float rot = (new Random()).nextFloat() * 360F;
				qY = new Quaternion();
				qY.setFromAxisAngle(new Vector4f(0, 1, 0, extraYRotation));
				Quaternion.mul(this.rotation, qY, this.rotation);
			}
		}
	}*/

}
