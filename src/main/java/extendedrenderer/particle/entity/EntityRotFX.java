package extendedrenderer.particle.entity;

import com.corosus.coroutil.util.CoroUtilBlock;
import com.corosus.coroutil.util.CoroUtilMisc;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import extendedrenderer.particle.behavior.ParticleBehaviors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import weather2.ClientTickHandler;
import weather2.IWindHandler;
import weather2.config.ConfigParticle;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.wind.WindManager;

import java.util.List;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class EntityRotFX extends TextureSheetParticle implements IWindHandler
{
    public static final ParticleRenderType SORTED_TRANSLUCENT = new ParticleRenderType() {

        @Override
        public void begin(BufferBuilder p_217600_1_, TextureManager p_217600_2_) {
            RenderSystem.disableCull();
            ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.begin(p_217600_1_, p_217600_2_);
        }

        @Override
        public void end(Tesselator p_217599_1_) {
            //TODO: not possible in 1.20 now i guess, cant remember why this line was important
            //p_217599_1_.getBuilder().setQuadSortOrigin(0, 0, 0);
            ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.end(p_217599_1_);
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_SORTED_TRANSLUCENT";
        }
    };
    public static final ParticleRenderType SORTED_OPAQUE_BLOCK = new ParticleRenderType() {

        @Override
        public void begin(BufferBuilder p_217600_1_, TextureManager p_217600_2_) {
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
            p_217600_1_.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator p_217599_1_) {
            //TODO: not possible in 1.20 now i guess, cant remember why this line was important
            //p_217599_1_.getBuilder().setQuadSortOrigin(0, 0, 0);
            ParticleRenderType.PARTICLE_SHEET_OPAQUE.end(p_217599_1_);
        }

        @Override
        public String toString() {
            return "PARTICLE_BLOCK_SHEET_SORTED_OPAQUE";
        }
    };
    public boolean weatherEffect = false;

    public float spawnY = -1;

    //this field and 2 methods below are for backwards compatibility with old particle system from the new icon based system
    public int particleTextureIndexInt = 0;

    public float brightness = 0.7F;

    public boolean callUpdateSuper = true;
    public boolean callUpdatePB = true;

    public float renderRange = 128F;

    //used in RotatingEffectRenderer to assist in solving some transparency ordering issues, eg, tornado funnel before clouds
    public int renderOrder = 0;

    //not a real entity ID now, just used for making rendering of entities slightly unique
    private int entityID = 0;

    public int debugID = 0;

    public float prevRotationYaw;
    public float rotationYaw;
    public float prevRotationPitch;
    public float rotationPitch;

    public float windWeight = 5;
    public boolean isTransparent = true;

    public boolean killOnCollide = false;
    public int killOnCollideActivateAtAge = 0;

    public boolean facePlayer = false;

    //facePlayer will override this
    public boolean facePlayerYaw = false;

    public boolean vanillaMotionDampen = true;

    //for particle behaviors
    public double aboveGroundHeight = 4.5D;
    public boolean checkAheadToBounce = true;
    public boolean collisionSpeedDampen = true;

    public double bounceSpeed = 0.05D;
    public double bounceSpeedMax = 0.15D;
    public double bounceSpeedAhead = 0.35D;
    public double bounceSpeedMaxAhead = 0.25D;

    public boolean bounceOnVerticalImpact = false;
    public double bounceOnVerticalImpactEnergy = 0.3F;

    public boolean spinFast = false;
    public float spinFastRate = 10F;
    public boolean spinTowardsMotionDirection = false;

    private float ticksFadeInMax = 0;
    private float ticksFadeOutMax = 0;

    private float fullAlphaTarget = 1F;

    private boolean dontRenderUnderTopmostBlock = false;

    private boolean killWhenUnderTopmostBlock = false;
    private int killWhenUnderTopmostBlock_ScanAheadRange = 0;

    public int killWhenUnderCameraAtLeast = 0;

    public int killWhenFarFromCameraAtLeast = 0;

    private float ticksFadeOutMaxOnDeath = -1;
    private float ticksFadeOutCurOnDeath = 0;
    protected boolean fadingOut = false;

    public float avoidTerrainAngle = 0;

    //this is for yaw only
    public boolean useRotationAroundCenter = false;
    public float rotationAroundCenter = 0;
    public float rotationAroundCenterPrev = 0;
    public float rotationSpeedAroundCenter = 0;
    public float rotationDistAroundCenter = 0;

    private boolean slantParticleToWind = false;

    /*public Quaternion rotation;
    public Quaternion rotationPrev;*/

    //set to true for direct quaternion control, not EULER conversion helper
    public boolean quatControl = false;

    public boolean fastLight = false;

    public float brightnessCache = 0.5F;

    public boolean rotateOrderXY = false;

    public float extraYRotation = 0;

    public boolean markCollided = false;

    public boolean isCollidedHorizontally = false;
    public boolean isCollidedVerticallyDownwards = false;
    public boolean isCollidedVerticallyUpwards = false;

    //used for translational rotation around a point
    public Vector3f rotationAround = new Vector3f();

    //workaround for particles that are fading out while partially in the ground, keeps them rendering at previous brightness instead of 0
    protected int lastNonZeroBrightness = 15728640;

    public ParticleBehaviors pb = null; //designed to be a reference to the central objects particle behavior

    //workaround for avoiding using vanilla bb which causes huge performance issues for large sizes
    private boolean useCustomBBForRenderCulling = false;
    private static final AABB INITIAL_AABB = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    private AABB bbRender = INITIAL_AABB;
    private float renderDistanceCull = -1;
    private boolean useDynamicWindSpeed = true;

    public EntityRotFX(ClientLevel par1World, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        super(par1World, par2, par4, par6, par8, par10, par12);
        setSize(0.3F, 0.3F);
        //this.isImmuneToFire = true;
        //this.setMaxAge(100);

        this.entityID = CoroUtilMisc.random.nextInt(100000);

        //rotation = new Quaternion();

        //TODO: 1.14 uncomment for shaders
        //brightnessCache = CoroUtilBlockLightCache.getBrightnessCached(world, (float)posX, (float)posY, (float)posZ);
    }

    public boolean isSlantParticleToWind() {
        return slantParticleToWind;
    }

    public void setSlantParticleToWind(boolean slantParticleToWind) {
        this.slantParticleToWind = slantParticleToWind;
    }

    public float getTicksFadeOutMaxOnDeath() {
        return ticksFadeOutMaxOnDeath;
    }

    public void setTicksFadeOutMaxOnDeath(float ticksFadeOutMaxOnDeath) {
        this.ticksFadeOutMaxOnDeath = ticksFadeOutMaxOnDeath;
    }

    public boolean isKillWhenUnderTopmostBlock() {
        return killWhenUnderTopmostBlock;
    }

    public void setKillWhenUnderTopmostBlock(boolean killWhenUnderTopmostBlock) {
        this.killWhenUnderTopmostBlock = killWhenUnderTopmostBlock;
    }

    public boolean isDontRenderUnderTopmostBlock() {
        return dontRenderUnderTopmostBlock;
    }

    public void setDontRenderUnderTopmostBlock(boolean dontRenderUnderTopmostBlock) {
        this.dontRenderUnderTopmostBlock = dontRenderUnderTopmostBlock;
    }

    public float getTicksFadeInMax() {
        return ticksFadeInMax;
    }

    public void setTicksFadeInMax(float ticksFadeInMax) {
        this.ticksFadeInMax = ticksFadeInMax;
    }

    public float getTicksFadeOutMax() {
        return ticksFadeOutMax;
    }

    public void setTicksFadeOutMax(float ticksFadeOutMax) {
        this.ticksFadeOutMax = ticksFadeOutMax;
    }

    public int getParticleTextureIndex()
    {
        return this.particleTextureIndexInt;
    }

    public void setLifetime(int par) {
        lifetime = par;
    }

    public float getAlphaF()
    {
        return this.alpha;
    }

    @Override
    public void remove() {
        if (pb != null) pb.particles.remove(this);
        super.remove();
    }

    @Override
    public void tick() {
        super.tick();
        this.prevRotationPitch = this.rotationPitch;
        if (!(this instanceof PivotingParticle)) {
            this.prevRotationYaw = this.rotationYaw;
        }

        Entity ent = Minecraft.getInstance().getCameraEntity();

        //if (this.entityID % 400 == 0) System.out.println("tick time: " + this.worldObj.getGameTime());

        if (!isVanillaMotionDampen()) {
            //cancel motion dampening (which is basically air resistance)
            //keep this up to date with the inverse of whatever Particle.tick uses
            this.xd /= 0.9800000190734863D;
            this.yd /= 0.9800000190734863D;
            this.zd /= 0.9800000190734863D;
        }

        if (!this.removed && !fadingOut) {
            if (killOnCollide && (killOnCollideActivateAtAge == 0 || age >= killOnCollideActivateAtAge)) {
                if (this.isCollided()) {
                    startDeath();
                }

            }

            BlockPos pos = CoroUtilBlock.blockPos(this.x, this.y, this.z);

            if (killWhenUnderTopmostBlock) {


                //int height = this.world.getPrecipitationHeight(new BlockPos(this.posX, this.posY, this.posZ)).getY();
                int height = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pos).getY();
                if (this.y - killWhenUnderTopmostBlock_ScanAheadRange <= height) {
                    startDeath();
                }
            }

            //case: when on high pillar and rain is falling far below you, start killing it / fading it out
            if (killWhenUnderCameraAtLeast != 0) {
                if (this.y < ent.getY() - killWhenUnderCameraAtLeast) {
                    startDeath();
                }
            }

            if (killWhenFarFromCameraAtLeast != 0) {
                if (getAge() > 20 && getAge() % 5 == 0) {

                    if (ent.distanceToSqr(this.x, this.y, this.z) > killWhenFarFromCameraAtLeast * killWhenFarFromCameraAtLeast) {
                        //System.out.println("far kill");
                        startDeath();
                    }
                }
            }
        }

        if (!collisionSpeedDampen) {
            //if (this.isCollided()) {
            if (this.onGround) {
                this.xd /= 0.699999988079071D;
                this.zd /= 0.699999988079071D;
            }
        }

        double speedXZ = Math.sqrt(getMotionX() * getMotionX() + /*getMotionY() * getMotionY() + */getMotionZ() * getMotionZ());
        double spinFastRateAdj = spinFastRate * speedXZ * 10F;
        //spinFastRateAdj = 0;

        if (spinFast) {
            this.rotationPitch += this.entityID % 2 == 0 ? spinFastRateAdj : -spinFastRateAdj;
            this.rotationYaw += this.entityID % 2 == 0 ? -spinFastRateAdj : spinFastRateAdj;
        }

        float angleToMovement = (float) (Math.toDegrees(Math.atan2(xd, zd)));

        if (spinTowardsMotionDirection) {
            this.rotationYaw = angleToMovement;
            this.rotationPitch += spinFastRate;
        }

        if (!fadingOut) {
            if (ticksFadeInMax > 0 && this.getAge() < ticksFadeInMax) {
                //System.out.println("this.getAge() / ticksFadeInMax: " + this.getAge() / ticksFadeInMax);
                this.setAlpha((float)this.getAge() / ticksFadeInMax * getFullAlphaTarget());
                //this.setAlphaF(0.15F);
            } else if (ticksFadeOutMax > 0 && this.getAge() > this.getLifetime() - ticksFadeOutMax) {
                float count = this.getAge() - (this.getLifetime() - ticksFadeOutMax);
                float val = (ticksFadeOutMax - (count)) / ticksFadeOutMax;
                //System.out.println(val);
                this.setAlpha(val * getFullAlphaTarget());
                //make sure fully visible otherwise
            } else if (ticksFadeInMax > 0 || ticksFadeOutMax > 0) {
                this.setAlpha(getFullAlphaTarget());
            }
        } else {
            if (ticksFadeOutCurOnDeath < ticksFadeOutMaxOnDeath) {
                ticksFadeOutCurOnDeath++;
            } else {
                this.remove();
            }
            float val = 1F - (ticksFadeOutCurOnDeath / ticksFadeOutMaxOnDeath);
            //System.out.println(val);
            this.setAlpha(val * getFullAlphaTarget());
        }

        if (level.getGameTime() % 5 == 0) {
            //TODO: 1.14 uncomment
            //brightnessCache = CoroUtilBlockLightCache.getBrightnessCached(world, (float)posX, (float)posY, (float)posZ);
        }

        rotationAroundCenter += rotationSpeedAroundCenter;
        rotationAroundCenter %= 360;
        /*while (rotationAroundCenter >= 360) {
            System.out.println(rotationAroundCenter);
            rotationAroundCenter -= 360;
        }*/

        tickExtraRotations();
    }

    public void tickExtraRotations() {
        if (slantParticleToWind) {
            double motionXZ = Math.sqrt(xd * xd + zd * zd);
            rotationPitch = (float)Math.atan2(yd, motionXZ);
        }

        WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
        if (weatherMan == null) return;
        WindManager windMan = weatherMan.getWindManager();
        if (windMan == null) return;
        if (this instanceof PivotingParticle) return;
        //particles on ground shouldnt get blown as hard (idea for hail)
        if (onGround) {
            windMan.applyWindForceNew(this, (1F / 20F) * 0.3F, 0.5F, useDynamicWindSpeed);
        } else {
            windMan.applyWindForceNew(this, 1F / 20F, 0.5F, useDynamicWindSpeed);
        }

        /*if (!quatControl) {
            rotationPrev = new Quaternion(rotation);
            Entity ent = Minecraft.getInstance().getRenderViewEntity();
            updateQuaternion(ent);
        }*/
    }

    public void startDeath() {
        if (ticksFadeOutMaxOnDeath > 0) {
            ticksFadeOutCurOnDeath = 0;//ticksFadeOutMaxOnDeath;
            fadingOut = true;
        } else {
            this.remove();
        }
    }
    
    /*public void setParticleTextureIndex(int par1)
    {
        this.particleTextureIndexInt = par1;
        if (this.getFXLayer() == 0) super.setParticleTextureIndex(par1);
    }*/

    /*@Override
    public int getFXLayer()
    {
        return 5;
    }*/

    public void spawnAsWeatherEffect()
    {
        weatherEffect = true;
        if (ConfigParticle.Particle_engine_weather2) {
            ClientTickHandler.particleManagerExtended().add(this);
        } else {
            Minecraft.getInstance().particleEngine.add(this);
        }
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        this.age = age;
    }

    public int getLifetime()
    {
        return lifetime;
    }

    public void setSize(float par1, float par2)
    {
        super.setSize(par1, par2);
        // MC-12269 - fix particle being offset to the NW
        this.setPos(x, y, z);
    }

    public void setGravity(float par) {
        gravity = par;
    }

    public float maxRenderRange() {
        return renderRange;
    }

    public void setScale(float parScale) {
        //dont set the AABB as big as the render scale, otherwise huge performance losses, we'll just use 0.3 in constructor for now
        //super.setSize(parScale, parScale);
        this.setSizeForRenderCulling(parScale, parScale);
        quadSize = parScale;
    }

    public Vector3f getPosition() {
        return new Vector3f((float)x, (float)y, (float)z);
    }

    /*@Override
    public Quaternion getQuaternion() {
        return this.rotation;
    }

    @Override
    public Quaternion getQuaternionPrev() {
        return this.rotationPrev;
    }*/

    public float getScale() {
        return quadSize;
    }

    public Vec3 getPos() {
        return new Vec3(x, y, z);
    }

    public double getPosX() {
        return x;
    }

    public void setPosX(double posX) {
        this.x = posX;
    }

    public double getPosY() {
        return y;
    }

    public void setPosY(double posY) {
        this.y = posY;
    }

    public double getPosZ() {
        return z;
    }

    public void setPosZ(double posZ) {
        this.z = posZ;
    }

    public double getMotionX() {
        return xd;
    }

    public void setMotionX(double motionX) {
        this.xd = motionX;
    }

    public double getMotionY() {
        return yd;
    }

    public void setMotionY(double motionY) {
        this.yd = motionY;
    }

    public double getMotionZ() {
        return zd;
    }

    public void setMotionZ(double motionZ) {
        this.zd = motionZ;
    }

    public double getPrevPosX() {
        return xo;
    }

    public void setPrevPosX(double prevPosX) {
        this.xo = prevPosX;
    }

    public double getPrevPosY() {
        return yo;
    }

    public void setPrevPosY(double prevPosY) {
        this.yo = prevPosY;
    }

    public double getPrevPosZ() {
        return zo;
    }

    public void setPrevPosZ(double prevPosZ) {
        this.zo = prevPosZ;
    }

    public int getEntityId() {
        return entityID;
    }

    public Level getWorld() {
        return this.level;
    }

    public void setCanCollide(boolean val) {
        this.hasPhysics = val;
    }

    public boolean getCanCollide() {
        return this.hasPhysics;
    }

    public boolean isCollided() {
        return this.onGround || isCollidedHorizontally;
    }

    public double getDistance(double x, double y, double z)
    {
        double d0 = this.x - x;
        double d1 = this.y - y;
        double d2 = this.z - z;
        return Mth.sqrt((float) (d0 * d0 + d1 * d1 + d2 * d2));
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        return this.quadSize;
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {

        Vec3 Vector3d = renderInfo.getPosition();
        Vec3 pivotedPosition = getPivotedPosition(partialTicks);
        float f = (float)(Mth.lerp(partialTicks, this.xo, this.x) + pivotedPosition.x - Vector3d.x());
        float f1 = (float)(Mth.lerp(partialTicks, this.yo, this.y) + pivotedPosition.y - Vector3d.y());
        float f2 = (float)(Mth.lerp(partialTicks, this.zo, this.z) + pivotedPosition.z - Vector3d.z());
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

        Quaternionf quaternionf;
        if (this.roll == 0.0F) {
            quaternionf = renderInfo.rotation();
        } else {
            quaternionf = new Quaternionf(renderInfo.rotation());
            quaternionf.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }

        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getQuadSize(partialTicks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }

        float f7 = this.getU0();
        float f8 = this.getU1();
        float f5 = this.getV0();
        float f6 = this.getV1();
        int j = this.getLightColor(partialTicks);
        //int j = 15728800;
        if (j > 0) {
            lastNonZeroBrightness = j;
        } else {
            j = lastNonZeroBrightness;
        }
        buffer.vertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).uv(f8, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).uv(f8, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        buffer.vertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).uv(f7, f6).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();

    }

    //TODO: 1.14 uncomment
	/*public void renderParticleForShader(InstancedMeshParticle mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                        float partialTicks, float rotationX, float rotationZ,
                                        float rotationYZ, float rotationXY, float rotationXZ) {

        if (mesh.curBufferPos >= mesh.numInstances) return;

        //camera relative positions, for world position, remove the interpPos values
        float posX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - this.interpPosX);
        float posY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - this.interpPosY);
        float posZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - this.interpPosZ);
        //Vector3f pos = new Vector3f((float) (entityIn.posX - particle.posX), (float) (entityIn.posY - particle.posY), (float) (entityIn.posZ - particle.posZ));
        Vector3f pos = new Vector3f(posX, posY, posZ);

        Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);

        //adjust to perspective and camera
        //Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
        //upload to buffer
        modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), mesh.instanceDataBuffer);

        //brightness
        float brightness;
        //brightness = CoroUtilBlockLightCache.getBrightnessCached(world, (float)this.posX, (float)this.posY, (float)this.posZ);
        brightness = brightnessCache;
        //brightness = -1F;
        //brightness = CoroUtilBlockLightCache.brightnessPlayer;
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS, brightness);

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
        
    }*/

    /*public void renderParticleForShaderTest(InstancedMeshParticle mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                            float partialTicks, float rotationX, float rotationZ,
                                            float rotationYZ, float rotationXY, float rotationXZ) {

        if (mesh.curBufferPos >= mesh.numInstances) return;

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
    }*/

    public void setKillOnCollide(boolean val) {
        this.killOnCollide = val;
    }

    //override for extra isCollided types
    @Override
    public void move(double x, double y, double z) {
        double xx = x;
        double yy = y;
        double zz = z;
        if (this.hasPhysics && (x != 0.0D || y != 0.0D || z != 0.0D)) {
            Vec3 Vector3d = Entity.collideBoundingBox(null, new Vec3(x, y, z), this.getBoundingBox(), this.level, List.of());
            x = Vector3d.x;
            y = Vector3d.y;
            z = Vector3d.z;
        }

        if (x != 0.0D || y != 0.0D || z != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(x, y, z));
            if (isUseCustomBBForRenderCulling()) {
                this.setBoundingBoxForRender(this.getBoundingBoxForRender(1F).move(x, y, z));
            }
            /*Vec3 pivotedPosition = getPivotedPosition(0);
            if (pivotedPosition != Vec3.ZERO) {
                this.setBoundingBox(this.getBoundingBox().move(x + pivotedPosition.x, y + pivotedPosition.y, z + pivotedPosition.z));
            } else {
                this.setBoundingBox(this.getBoundingBox().move(x, y, z));
            }*/

            this.setLocationFromBoundingbox();
        }

        this.onGround = yy != y && yy < 0.0D;
        this.isCollidedHorizontally = xx != x || zz != z;
        this.isCollidedVerticallyDownwards = yy < y;
        this.isCollidedVerticallyUpwards = yy > y;
        if (xx != x) {
            this.xd = 0.0D;
        }

        if (zz != z) {
            this.zd = 0.0D;
        }

        if (!markCollided) {
            if (onGround || isCollidedVerticallyDownwards || isCollidedHorizontally || isCollidedVerticallyUpwards) {
                onHit();
                markCollided = true;
            }

            if (bounceOnVerticalImpact && (onGround || isCollidedVerticallyDownwards)) {
                setMotionY(-getMotionY() * bounceOnVerticalImpactEnergy);
            }
        }

    }

    public void setFacePlayer(boolean val) {
        this.facePlayer = val;
    }

    public TextureAtlasSprite getParticleTexture() {
        return this.sprite;
    }

    public boolean isVanillaMotionDampen() {
        return vanillaMotionDampen;
    }

    public void setVanillaMotionDampen(boolean motionDampen) {
        this.vanillaMotionDampen = motionDampen;
    }

    @Override
    public int getLightColor(float p_189214_1_) {
        return super.getLightColor(p_189214_1_);//(int)((float)super.getBrightnessForRender(p_189214_1_))/* * this.world.getSunBrightness(1F))*/;
    }

    /*public void updateQuaternion(Entity camera) {

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
        }
    }*/

    @Override
    public void setColor(float particleRedIn, float particleGreenIn, float particleBlueIn) {
        super.setColor(particleRedIn, particleGreenIn, particleBlueIn);
        //TODO: 1.14 uncomment
        /*RotatingParticleManager.markDirtyVBO2();*/
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        //TODO: 1.14 uncomment
        /*RotatingParticleManager.markDirtyVBO2();*/
    }

    public int getKillWhenUnderTopmostBlock_ScanAheadRange() {
        return killWhenUnderTopmostBlock_ScanAheadRange;
    }

    public void setKillWhenUnderTopmostBlock_ScanAheadRange(int killWhenUnderTopmostBlock_ScanAheadRange) {
        this.killWhenUnderTopmostBlock_ScanAheadRange = killWhenUnderTopmostBlock_ScanAheadRange;
    }

    public boolean isCollidedVertically() {
        return isCollidedVerticallyDownwards || isCollidedVerticallyUpwards;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return SORTED_TRANSLUCENT;
    }

    @Override
    public void setSprite(TextureAtlasSprite sprite) {
        super.setSprite(sprite);
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public float getFullAlphaTarget() {
        return fullAlphaTarget;
    }

    public void setFullAlphaTarget(float fullAlphaTarget) {
        this.fullAlphaTarget = fullAlphaTarget;
    }

    public int getLastNonZeroBrightness() {
        return lastNonZeroBrightness;
    }

    public void setLastNonZeroBrightness(int lastNonZeroBrightness) {
        this.lastNonZeroBrightness = lastNonZeroBrightness;
    }

    public void onHit() {

    }

    public void setMaxAge(int par) {
        setLifetime(par);
    }

    public int getMaxAge() {
        return getLifetime();
    }

    public void setAlphaF(float val) {
        setAlpha(val);
    }

    public void setPosition(double posX, double posY, double posZ) {
        this.setPos(posX, posY, posZ);
    }

    public Vec3 getPivotedPosition(float partialTicks) {
        return Vec3.ZERO;
    }

    public void setBoundingBoxForRender(AABB p_107260_) {
        this.bbRender = p_107260_;
    }

    public AABB getBoundingBoxForRender(float partialTicks) {
        if (isUseCustomBBForRenderCulling()) {
            return bbRender;
        } else {
            return this.getBoundingBox();
        }
    }

    public void setSizeForRenderCulling(float p_107251_, float p_107252_) {
        if (p_107251_ != this.bbWidth || p_107252_ != this.bbHeight) {
            this.bbWidth = p_107251_;
            this.bbHeight = p_107252_;
            AABB aabb = this.getBoundingBox();
            double d0 = (aabb.minX + aabb.maxX - (double)p_107251_) / 2.0D;
            double d1 = (aabb.minZ + aabb.maxZ - (double)p_107251_) / 2.0D;
            this.setBoundingBoxForRender(new AABB(d0, aabb.minY, d1, d0 + (double)this.bbWidth, aabb.minY + (double)this.bbHeight, d1 + (double)this.bbWidth));
        }

    }

    public boolean isUseCustomBBForRenderCulling() {
        return useCustomBBForRenderCulling;
    }

    public void setUseCustomBBForRenderCulling(boolean useCustomBBForRenderCulling) {
        this.useCustomBBForRenderCulling = useCustomBBForRenderCulling;
    }

    @Override
    public float getWindWeight() {
        return windWeight;
    }

    @Override
    public int getParticleDecayExtra() {
        return 0;
    }

    public int getKillOnCollideActivateAtAge() {
        return killOnCollideActivateAtAge;
    }

    public void setKillOnCollideActivateAtAge(int killOnCollideActivateAtAge) {
        this.killOnCollideActivateAtAge = killOnCollideActivateAtAge;
    }

    public float getRenderDistanceCull() {
        return renderDistanceCull;
    }

    public void setRenderDistanceCull(float renderDistanceCull) {
        this.renderDistanceCull = renderDistanceCull;
    }

    public boolean isUseDynamicWindSpeed() {
        return useDynamicWindSpeed;
    }

    public void setUseDynamicWindSpeed(boolean useDynamicWindSpeed) {
        this.useDynamicWindSpeed = useDynamicWindSpeed;
    }
}
