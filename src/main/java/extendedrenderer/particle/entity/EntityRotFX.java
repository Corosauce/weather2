package extendedrenderer.particle.entity;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.ClientTickHandler;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.wind.WindManager;

import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class EntityRotFX extends SpriteTexturedParticle
{
    protected static final IParticleRenderType SORTED_TRANSLUCENT = new IParticleRenderType() {

        @Override
        public void beginRender(BufferBuilder p_217600_1_, TextureManager p_217600_2_) {
            IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.beginRender(p_217600_1_, p_217600_2_);
        }

        @Override
        public void finishRender(Tessellator p_217599_1_) {
            p_217599_1_.getBuffer().sortVertexData(0, 0, 0);
            IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT.finishRender(p_217599_1_);
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_SORTED_TRANSLUCENT";
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

    public EntityRotFX(ClientWorld par1World, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        super(par1World, par2, par4, par6, par8, par10, par12);
        setSize(0.3F, 0.3F);
        //this.isImmuneToFire = true;
        //this.setMaxAge(100);

        this.entityID = par1World.rand.nextInt(100000);

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

    public void setMaxAge(int par) {
        maxAge = par;
    }

    public float getAlphaF()
    {
        return this.particleAlpha;
    }

    @Override
    public void setExpired() {
        super.setExpired();
    }

    @Override
    public void tick() {
        super.tick();
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;

        Entity ent = Minecraft.getInstance().getRenderViewEntity();

        //if (this.entityID % 400 == 0) System.out.println("tick time: " + this.worldObj.getGameTime());

        if (!isVanillaMotionDampen()) {
            //cancel motion dampening (which is basically air resistance)
            //keep this up to date with the inverse of whatever Particle.tick uses
            this.motionX /= 0.9800000190734863D;
            this.motionY /= 0.9800000190734863D;
            this.motionZ /= 0.9800000190734863D;
        }

        if (!this.isExpired && !fadingOut) {
            if (killOnCollide) {
                if (this.isCollided()) {
                    startDeath();
                }

            }

            BlockPos pos = new BlockPos(new BlockPos(this.posX, this.posY, this.posZ));

            if (killWhenUnderTopmostBlock) {


                //int height = this.world.getPrecipitationHeight(new BlockPos(this.posX, this.posY, this.posZ)).getY();
                int height = world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos).getY();
                if (this.posY - killWhenUnderTopmostBlock_ScanAheadRange <= height) {
                    startDeath();
                }
            }

            //case: when on high pillar and rain is falling far below you, start killing it / fading it out
            if (killWhenUnderCameraAtLeast != 0) {
                if (this.posY < ent.getPosY() - killWhenUnderCameraAtLeast) {
                    startDeath();
                }
            }

            if (killWhenFarFromCameraAtLeast != 0) {
                if (getAge() > 20 && getAge() % 5 == 0) {

                    if (ent.getDistanceSq(this.posX, this.posY, this.posZ) > killWhenFarFromCameraAtLeast * killWhenFarFromCameraAtLeast) {
                        //System.out.println("far kill");
                        startDeath();
                    }
                }
            }
        }

        if (!collisionSpeedDampen) {
            //if (this.isCollided()) {
            if (this.onGround) {
                this.motionX /= 0.699999988079071D;
                this.motionZ /= 0.699999988079071D;
            }
        }

        if (spinFast) {
            this.rotationPitch += this.entityID % 2 == 0 ? spinFastRate : -spinFastRate;
            this.rotationYaw += this.entityID % 2 == 0 ? -spinFastRate : spinFastRate;
        }

        float angleToMovement = (float) (Math.toDegrees(Math.atan2(motionX, motionZ)));

        if (spinTowardsMotionDirection) {
            this.rotationYaw = angleToMovement;
            this.rotationPitch += spinFastRate;
        }

        if (!fadingOut) {
            if (ticksFadeInMax > 0 && this.getAge() < ticksFadeInMax) {
                //System.out.println("this.getAge() / ticksFadeInMax: " + this.getAge() / ticksFadeInMax);
                this.setAlphaF((float)this.getAge() / ticksFadeInMax * getFullAlphaTarget());
                //particle.setAlphaF(1);
            } else if (ticksFadeOutMax > 0 && this.getAge() > this.getMaxAge() - ticksFadeOutMax) {
                float count = this.getAge() - (this.getMaxAge() - ticksFadeOutMax);
                float val = (ticksFadeOutMax - (count)) / ticksFadeOutMax;
                //System.out.println(val);
                this.setAlphaF(val * getFullAlphaTarget());
                //make sure fully visible otherwise
            } else if (ticksFadeInMax > 0 || ticksFadeOutMax > 0) {
                this.setAlphaF(getFullAlphaTarget());
            }
        } else {
            if (ticksFadeOutCurOnDeath < ticksFadeOutMaxOnDeath) {
                ticksFadeOutCurOnDeath++;
            } else {
                this.setExpired();
            }
            float val = 1F - (ticksFadeOutCurOnDeath / ticksFadeOutMaxOnDeath);
            //System.out.println(val);
            this.setAlphaF(val * getFullAlphaTarget());
        }

        if (world.getGameTime() % 5 == 0) {
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
            double motionXZ = Math.sqrt(motionX * motionX + motionZ * motionZ);
            rotationPitch = (float)Math.atan2(motionY, motionXZ);
        }

        WeatherManagerClient weatherMan = ClientTickHandler.weatherManager;
        if (weatherMan == null) return;
        WindManager windMan = weatherMan.getWindManager();
        if (windMan == null) return;
        windMan.applyWindForceNew(this, 1F / 20F, 0.5F);

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
            this.setExpired();
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
        //Minecraft.getInstance().particles.addEffect(this);
        ClientTickHandler.particleManagerExtended().addEffect(this);
    }

    public int getAge()
    {
        return age;
    }

    public void setAge(int age)
    {
        age = age;
    }

    public int getMaxAge()
    {
        return maxAge;
    }

    public void setSize(float par1, float par2)
    {
        super.setSize(par1, par2);
        // MC-12269 - fix particle being offset to the NW
        this.setPosition(posX, posY, posZ);
    }

    public void setGravity(float par) {
        particleGravity = par;
    }

    public float maxRenderRange() {
        return renderRange;
    }

    public void setScale(float parScale) {
        particleScale = parScale;
    }

    public Vector3f getPosition() {
        return new Vector3f((float)posX, (float)posY, (float)posZ);
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
        return particleScale;
    }

    public Vector3d getPos() {
        return new Vector3d(posX, posY, posZ);
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public double getPosZ() {
        return posZ;
    }

    public void setPosZ(double posZ) {
        this.posZ = posZ;
    }

    public double getMotionX() {
        return motionX;
    }

    public void setMotionX(double motionX) {
        this.motionX = motionX;
    }

    public double getMotionY() {
        return motionY;
    }

    public void setMotionY(double motionY) {
        this.motionY = motionY;
    }

    public double getMotionZ() {
        return motionZ;
    }

    public void setMotionZ(double motionZ) {
        this.motionZ = motionZ;
    }

    public double getPrevPosX() {
        return prevPosX;
    }

    public void setPrevPosX(double prevPosX) {
        this.prevPosX = prevPosX;
    }

    public double getPrevPosY() {
        return prevPosY;
    }

    public void setPrevPosY(double prevPosY) {
        this.prevPosY = prevPosY;
    }

    public double getPrevPosZ() {
        return prevPosZ;
    }

    public void setPrevPosZ(double prevPosZ) {
        this.prevPosZ = prevPosZ;
    }

    public int getEntityId() {
        return entityID;
    }

    public World getWorld() {
        return this.world;
    }

    public void setCanCollide(boolean val) {
        this.canCollide = val;
    }

    public boolean getCanCollide() {
        return this.canCollide;
    }

    public boolean isCollided() {
        return this.onGround || isCollidedHorizontally;
    }

    public double getDistance(double x, double y, double z)
    {
        double d0 = this.posX - x;
        double d1 = this.posY - y;
        double d2 = this.posZ - z;
        return (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    @Override
    public float getScale(float scaleFactor) {
        return this.particleScale;
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

        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getScale(partialTicks);

        for(int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.transform(quaternion);
            vector3f.mul(f4);
            vector3f.add(f, f1, f2);
        }

        float f7 = this.getMinU();
        float f8 = this.getMaxU();
        float f5 = this.getMinV();
        float f6 = this.getMaxV();
        int j = this.getBrightnessForRender(partialTicks);
        //int j = 15728800;
        if (j > 0) {
            lastNonZeroBrightness = j;
        } else {
            j = lastNonZeroBrightness;
        }
        buffer.pos(avector3f[0].getX(), avector3f[0].getY(), avector3f[0].getZ()).tex(f8, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
        buffer.pos(avector3f[1].getX(), avector3f[1].getY(), avector3f[1].getZ()).tex(f8, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
        buffer.pos(avector3f[2].getX(), avector3f[2].getY(), avector3f[2].getZ()).tex(f7, f5).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();
        buffer.pos(avector3f[3].getX(), avector3f[3].getY(), avector3f[3].getZ()).tex(f7, f6).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j).endVertex();

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

    //TODO: 1.14 now sets depth buffer use in IParticleRenderType types
    /*@Override
    public boolean shouldDisableDepth() {
    	return isTransparent;
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
        if (this.canCollide && (x != 0.0D || y != 0.0D || z != 0.0D)) {
            Vector3d Vector3d = Entity.collideBoundingBoxHeuristically((Entity)null, new Vector3d(x, y, z), this.getBoundingBox(), this.world, ISelectionContext.dummy(), new ReuseableStream<>(Stream.empty()));
            x = Vector3d.x;
            y = Vector3d.y;
            z = Vector3d.z;
        }

        if (x != 0.0D || y != 0.0D || z != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
            this.resetPositionToBB();
        }

        this.onGround = yy != y && yy < 0.0D;
        this.isCollidedHorizontally = xx != x || zz != z;
        this.isCollidedVerticallyDownwards = yy < y;
        this.isCollidedVerticallyUpwards = yy > y;
        if (xx != x) {
            this.motionX = 0.0D;
        }

        if (zz != z) {
            this.motionZ = 0.0D;
        }

        if (!markCollided) {
            if (onGround || isCollidedVerticallyDownwards || isCollidedHorizontally || isCollidedVerticallyUpwards) {
                onHit();
                markCollided = true;
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
    public int getBrightnessForRender(float p_189214_1_) {
        return super.getBrightnessForRender(p_189214_1_);//(int)((float)super.getBrightnessForRender(p_189214_1_))/* * this.world.getSunBrightness(1F))*/;
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
    public void setAlphaF(float alpha) {
        super.setAlphaF(alpha);
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
    public IParticleRenderType getRenderType() {
        //TODO: replaces getFXLayer of 5, possibly reimplement extra layers later for clouds etc
        //actually anything > 2 was custom texture sheet, then it just uses higher numbers for diff render orders, higher = later
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
}
