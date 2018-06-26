package weather2.client.tornado;

import CoroUtil.util.Vec3;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleCustomMatrix;
import extendedrenderer.particle.entity.ParticleTexExtraRender;
import extendedrenderer.particle.entity.ParticleTexFX;
import extendedrenderer.shader.Matrix4fe;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

import javax.vecmath.Vector3f;
import java.util.*;

/**
 * To contain the full funnel, with each component piece
 */
public class TornadoFunnel {

    public Vec3d pos = new Vec3d(0, 0, 0);

    public LinkedList<FunnelPiece> listFunnel = new LinkedList();

    //temp?

    public int amountPerLayer = 30;
    public int particleCount = amountPerLayer * 50;
    public int funnelPieces = 2;

    static class FunnelPiece {

        public List<ParticleCustomMatrix> listParticles = new ArrayList<>();

        public Vec3d posStart = new Vec3d(0, 0, 0);
        public Vec3d posEnd = new Vec3d(0, 20, 0);

        //public Vec3d vecDir = new Vec3d(0, 0, 0);
        public float vecDirX = 0;
        public float vecDirZ = 0;

        public boolean needInit = true;

    }

    public TornadoFunnel() {

    }

    public void tickGame() {

        amountPerLayer = 30;
        particleCount = amountPerLayer * 50;
        funnelPieces = 2;



        tickGameTestCreate();
        tickUpdateFunnel();
    }

    private void tickGameTestCreate() {

        EntityPlayer entP = Minecraft.getMinecraft().player;

        Random rand = new Random();

        while (listFunnel.size() < funnelPieces) {
            addPieceToEnd(new FunnelPiece());
        }

        //for (FunnelPiece piece : listFunnel) {
        for (int i = 0; i < listFunnel.size(); i++) {
            FunnelPiece piece = listFunnel.get(i);

            if (piece.needInit) {
                piece.needInit = false;

                int height = 10;
                //temp
                //TODO: LINK TO PREVIOUS OR NEXT PIECE IF THERE IS ONE
                if (i == 0) {
                    piece.posStart = new Vec3d(entP.posX, entP.posY, entP.posZ);
                    piece.posEnd = new Vec3d(entP.posX, entP.posY + height, entP.posZ);
                    //piece.posEnd = new Vec3d(entP.posX, entP.posY + entP.getEyeHeight(), entP.posZ);

                } else {
                    Vec3d prev = listFunnel.get(i-1).posEnd;
                    piece.posStart = new Vec3d(prev.x, prev.y, prev.z);
                    piece.posEnd = new Vec3d(piece.posStart.x, piece.posStart.y + height, piece.posStart.z);
                }

                if (i == funnelPieces - 1) {
                    piece.posEnd = new Vec3d(piece.posStart.x, piece.posStart.y + height, piece.posStart.z);
                }

                piece.vecDirX = rand.nextBoolean() ? 1 : -1;
                piece.vecDirZ = rand.nextBoolean() ? 1 : -1;
            }

            double dist = piece.posStart.distanceTo(piece.posEnd);

            double sizeXYParticle = 1;
            double funnelRadius = 3;

            double circumference = funnelRadius * 2D * Math.PI;

            amountPerLayer = (int) (circumference / sizeXYParticle);
            int layers = (int) (dist / sizeXYParticle);

            particleCount = layers * amountPerLayer;

            while (piece.listParticles.size() > particleCount) {
                piece.listParticles.get(piece.listParticles.size() - 1).setExpired();
                piece.listParticles.remove(piece.listParticles.size() - 1);
            }

            while (piece.listParticles.size() < particleCount) {
                BlockPos pos = new BlockPos(piece.posEnd.x, piece.posEnd.y, piece.posEnd.z);

                //if (entP.getDistanceSq(pos) < 10D * 10D) continue;

                //pos = world.getPrecipitationHeight(pos).add(0, 1, 0);

                ParticleCustomMatrix rain = new ParticleCustomMatrix(entP.world,
                        pos.getX() + rand.nextFloat(),
                        pos.getY(),
                        pos.getZ() + rand.nextFloat(),
                        0D, 0D, 0D, ParticleRegistry.white_square);
						/*ParticleTexExtraRender rain = new ParticleTexExtraRender(entP.world,
								15608.5F,
								70.5F,
								235.5F,
								0D, 0D, 0D, ParticleRegistry.test_texture);*/
                //rain.setCanCollide(true);
                //rain.setKillOnCollide(true);
                //rain.setKillWhenUnderTopmostBlock(true);
                //rain.setTicksFadeOutMaxOnDeath(5);

                //rain.particleTextureJitterX = 0;
                //rain.particleTextureJitterY = 0;

                //rain.setDontRenderUnderTopmostBlock(true);
                //rain.setExtraParticlesBaseAmount(5);
                //rain.setDontRenderUnderTopmostBlock(true);
                rain.setSlantParticleToWind(false);
                //rain.noExtraParticles = true;
                //rain.setExtraParticlesBaseAmount(1);
                //rain.setSeverityOfRainRate(0);
                rain.setDontRenderUnderTopmostBlock(false);

                boolean upward = rand.nextBoolean();

                rain.windWeight = 999999F;
                rain.setFacePlayer(false);

                rain.setScale(90F + (rand.nextFloat() * 3F));


                /**
                 * 64x64 particle, 18 blocks high exactly when scale 90 used
                 * 64x64 particle, 1 blocks high exactly when scale 5 used
                 * particle texture file size doesnt matter,
                 * scale 5 = 1 block size
                 *
                 */
                rain.setScale(5F);
                //rain.setScale(25F);
                rain.setMaxAge(100);
                rain.setGravity(0.0F);
                //opted to leave the popin for rain, its not as bad as snow, and using fade in causes less rain visual overall
                rain.setTicksFadeInMax(0);
                rain.setAlphaF(1);
                rain.setTicksFadeOutMax(0);

                rain.rotationYaw = 0;//rain.getWorld().rand.nextInt(360) - 180F;
                rain.rotationPitch = 90;
                rain.setMotionY(-0D);
									/*rain.setMotionX(0);
									rain.setMotionZ(0);*/
                rain.setMotionX((rand.nextFloat() - 0.5F) * 0.01F);
                rain.setMotionZ((rand.nextFloat() - 0.5F) * 0.01F);

                //rain.setRBGColorF(1F, 1F, 1F);
                rain.spawnAsWeatherEffect();
                rain.weatherEffect = false;
                //ClientTickHandler.weatherManager.addWeatheredParticle(rain);

                rain.isTransparent = false;

                rain.quatControl = true;
                rain.useRotationAroundCenter = true;

                piece.listParticles.add(rain);
            }
        }

        //reset
        /*for (int i = 0; i < listFunnel.size(); i++) {
            FunnelPiece piece = listFunnel.get(i);

            while (piece.listParticles.size() > particleCount) {
                piece.listParticles.get(piece.listParticles.size() - 1).setExpired();
                piece.listParticles.remove(piece.listParticles.size() - 1);
            }
        }*/
        //listFunnel.clear();


    }

    private void tickUpdateFunnel() {

        World world = Minecraft.getMinecraft().world;
        EntityPlayer player = Minecraft.getMinecraft().player;

        //for (FunnelPiece piece : listFunnel) {
        for (int ii = 0; ii < listFunnel.size(); ii++) {
            FunnelPiece piece = listFunnel.get(ii);

            /*if (ii == listFunnel.size() - 1) {
                piece.posEnd = new Vec3d(piece.posStart.x, piece.posStart.y + 20, piece.posStart.z);
            }*/

            double rate = 0.5F/* + (ii * 0.1F)*/;
            double distMax = 20;

            Random rand = new Random();

            //piece.posEnd = piece.posEnd.addVector(rate * piece.vecDirX, 0, rate * piece.vecDirZ);
            //piece.posEnd = piece.posEnd.addVector(rate * rand.nextFloat() * piece.vecDirX, 0, rate * rand.nextFloat() * piece.vecDirZ);

            int offset = 360 / listFunnel.size();
            long timeC = (world.getTotalWorldTime() * (ii+1) + (offset * ii)) * 1;
            float range = 35F;

            //piece.posEnd = new Vec3d(piece.posStart.x + Math.sin(Math.toRadians(timeC % 360)) * range, piece.posStart.y + 3, piece.posStart.z + Math.cos(Math.toRadians(timeC % 360)) * range);

            //piece.posEnd.

            //piece.posEnd = piece.posEnd.addVector(-1, 0, 0);

            double xx = piece.posEnd.x - piece.posStart.x;
            double zz = piece.posEnd.z - piece.posStart.z;
            double xzDist2 = (double)MathHelper.sqrt(xx * xx + zz * zz);

            if (xzDist2 > distMax) {
                if (piece.posEnd.x - piece.posStart.x > 0) {
                    piece.vecDirX = -1;
                }

                if (piece.posEnd.x - piece.posStart.x < 0) {
                    piece.vecDirX = 1;
                }

                if (piece.posEnd.z - piece.posStart.z > 0) {
                    piece.vecDirZ = -1;
                }

                if (piece.posEnd.z - piece.posStart.z < 0) {
                    piece.vecDirZ = 1;
                }
            }

            /*if (Math.abs(piece.posStart.x - piece.posEnd.x) > distMax) {
                piece.vecDirX *= -1;
            }

            if (Math.abs(piece.posStart.z - piece.posEnd.z) > distMax) {
                piece.vecDirZ *= -1;
            }*/

            if (ii > 0) {
                Vec3d prev = listFunnel.get(ii-1).posEnd;
                piece.posStart = new Vec3d(prev.x, prev.y, prev.z);
            }

            double dist = piece.posStart.distanceTo(piece.posEnd);

            double x = piece.posEnd.x - piece.posStart.x;
            double y = piece.posEnd.y - piece.posStart.y;
            double z = piece.posEnd.z - piece.posStart.z;
            Vec3d vec = new Vec3d(x / dist, y / dist, z / dist);

            double sizeXYParticle = 1;
            double funnelRadius = 3;

            double circumference = funnelRadius * 2D * Math.PI;

            amountPerLayer = (int) (circumference / sizeXYParticle);
            int layers = (int) (dist / sizeXYParticle);

            particleCount = layers * amountPerLayer;

            Iterator<ParticleCustomMatrix> it = piece.listParticles.iterator();
            int i = 0;
            while (it.hasNext()) {
                ParticleCustomMatrix part = it.next();
                if (part.isExpired) {
                    it.remove();
                } else {

                    int yIndex = i / amountPerLayer;
                    int rotIndex = i % amountPerLayer;
                    int yCount = particleCount / amountPerLayer;

                    //need 2 matrix maybe?
                    //relative to center matrix that uses translation and rotation
                    //relative to self matrix that uses rotation

                    long time = world.getTotalWorldTime();
                    long time2 = world.getTotalWorldTime() * 2;
                    long time3 = world.getTotalWorldTime() * 3;
						/*time = 0;
						time2 = 0;
						time3 = 0;*/

                    float speed = 1;



                    /*float angleX = (float)Math.atan2(piece.posEnd.x, piece.posStart.x);
                    float angleY = (float)Math.atan2(piece.posEnd.y, piece.posStart.y);
                    float angleZ = (float)Math.atan2(piece.posEnd.z, piece.posStart.z);*/

                    float angleY = -(float)(Math.atan2(vec.z, vec.x) - Math.toRadians(90));

                    //all sorts of wrong, ignore this
                    float angleX = (float)Math.atan2(vec.z, vec.y);
                    float angleZ = 0;//(float)Math.atan2(vec.x, vec.y);

                    //we need to get the pitch (X) from the 3d vectors
                    //need actual proper formula for this
                    //the rotation order needs adjusting too, rotate Y then X, aka yaw then pitch
                    //done and done, though:
                    //im still not sure if this will all work out with rotations off of that for the cylinder shape...

                    angleX = (float) Math.toRadians(90);
                    angleX = (float) Math.toRadians(time3 % 360);

                    double xzDist = (double)MathHelper.sqrt(vec.x * vec.x + vec.z * vec.z);

                    angleX = (float)(-Math.atan2(vec.y, xzDist) + Math.toRadians(90));

                    //double dp = piece.posStart.dotProduct(piece.posEnd);
                    //dp = (new Vec3d(0, 0, 0)).dotProduct(vec);
                    //angleX = (float) Math.acos(dp / (piece.posStart.lengthVector() * piece.posEnd.lengthVector()));

                    //System.out.println(angleY);

                    //Matrix4fe matrixFunnel = new Matrix4fe();

                    float spinAngle = (360F / amountPerLayer) * rotIndex;
                    float radius = 3F;

                    spinAngle += time3 * 12F;

                    /*if (spinAngle > 360) {
                        spinAngle -= 360;
                    }*/
                    spinAngle = spinAngle % 360;
                    //spinAngle = 1;

                    //matrixFunnel.rotateY(angleY);
                    ////matrixFunnel.rotateZ(angleZ);
                    //matrixFunnel.rotateX(angleX);

                    float yy = yIndex;

                    ////matrixFunnel.translate(new Vector3f(0, (yIndex - (yCount/2)) * 0.95F, 0));
                    //matrixFunnel.translate(new Vector3f(0, yy, 0));

                    //replaced with interpolatable version
                    /*matrixFunnel.translate(new Vector3f((float)Math.sin(Math.toRadians(spinAngle)) * radius,
                            0,
                            (float)Math.cos(Math.toRadians(spinAngle)) * radius));*/

                    part.rotationAroundCenterPrev = part.rotationAroundCenter;
                    part.rotationAroundCenter = spinAngle;
                    part.rotationDistAroundCenter = radius;

                    part.yy = yy;
                    part.angleX = angleX;
                    part.angleY = angleY;
                    part.angleZ = angleZ;

                    //Vector3f posParticle = matrixFunnel.getTranslation();

                    //part.setPosition(piece.posStart.x + posParticle.x, piece.posStart.y + posParticle.y, piece.posStart.z + posParticle.z);

                    //instead setup base position and calculate rest in render for interpolation sake
                    part.setPosition(piece.posStart.x, piece.posStart.y, piece.posStart.z);

                    Matrix4fe matrixSelf = new Matrix4fe();

                    //angle it to match funnel shape, done before y
                    //matrixSelf.rotateX((float)Math.toRadians(17.5F));

                    //rotate rest
                    //matrixSelf.rotateY((float)Math.toRadians(90 + (-time * speed) - (360F / (float)amountPerLayer * (float)rotIndex)));
                    //matrixSelf.rotateX((float)Math.sin(Math.toRadians((-time2 * 3) % 360)) * 0.5F);
                    //matrixSelf.rotateZ((float)Math.sin(Math.toRadians((-time * 3) % 360)) * 0.5F);

                    //new position based angle way
                    /*matrixSelf.rotateY((float)Math.toRadians(90) - angleY - (float)Math.toRadians(360F / (float)amountPerLayer * (float)rotIndex));
                    matrixSelf.rotateX(-angleX);
                    matrixSelf.rotateZ(-angleZ);*/

                    //TODO: need angle for center of cylinder, not using funnel vec
                    //HOW TO ANGLE CORRECTLY FOR ALL THINGS AND ORDERS?!?!?!

                    angleY = (float)(Math.atan2(vec.z, vec.x)/* - Math.toRadians(90)*/);

                    //angleX = (float) Math.toRadians(45F);



                    //matrixSelf.rotateX(angleX);
                    //matrixSelf.rotateY(angleY);
                    //matrixSelf.rotateY((float) Math.toRadians(45F));
                    //matrixSelf.rotateX((float) Math.toRadians(0));
                    //matrixSelf.rotate(angleX, 1F, 0F, 0F);
                    //matrixSelf.rotateTranslation(angleX, 1F, 0F, 0F, matrixSelf);
                    //matrixSelf.rotateX(angleX);
                    //matrixSelf.rotateX((float) Math.toRadians((360F / (float)amountPerLayer * (float)rotIndex) + 90));
                    //matrixSelf.rotateX((float) Math.toRadians(90F));

                    ////matrixSelf.rotateX((float)Math.sin(Math.toRadians(((-time - 40) * 3) % 360)) * 0.5F);

                    //Matrix4fe matrixSelf2 = new Matrix4fe();
                    //matrixSelf2.rotateX(angleX);

                    //matrixSelf.mul(matrixSelf2);

                    //part.rotation.setFromMatrix(matrixSelf.toLWJGLMathMatrix());


                    /*angleX = (float) Math.toRadians((time * 20) % 360);
                    angleY = (float) Math.toRadians((time * 30) % 360);
                    angleZ = (float) Math.toRadians((time * 40) % 360);*/

                    part.rotationPrev = new Quaternion(part.rotation);

                    Quaternion qY = new Quaternion();
                    Quaternion qX = new Quaternion();
                    Quaternion qZ = new Quaternion();
                    qY.setFromAxisAngle(new Vector4f(0, 1, 0, -angleY));

                    qZ.setFromAxisAngle(new Vector4f(0, 0, 1, -angleX));
                    Quaternion.mul(qY, qZ, part.rotation);

                    //rotate on y axis again, ok
                    angleZ = (float)Math.toRadians(90F + spinAngle);
                    qX.setFromAxisAngle(new Vector4f(0, 1, 0, angleZ));
                    Quaternion.mul(part.rotation, qX, part.rotation);



                    //part.rotation = new Quaternion();
                    //part.rotation = setEulerAnglesRad(angleY, angleX, angleZ);

                    part.setAge(40);

                    float r = 1F;
                    float g = 0F;
                    float b = 0F;

                    float stages = 100;

                    r = ((time + i*1) % stages) * (1F / stages);
                    g = ((time2 + i*1) % stages) * (1F / stages);
                    b = ((time3 + i*1) % stages) * (1F / stages);

                    part.setRBGColorF(0F, 0F, 0F);
                    part.setRBGColorF(r, g, b);
                    if (ii == 0) {
                        //part.setRBGColorF(r, 0, 0);
                    }
                    //part.setParticleTexture(ParticleRegistry.squareGrey);
                }

                i++;
            }
        }
    }

    public void addPieceToEnd(FunnelPiece piece) {
        listFunnel.addLast(piece);
    }

    public static Vec3d rotateVectorCC(Vec3d vec, Vec3d axis, double theta){
        double x, y, z;
        double u, v, w;
        x=vec.x;y=vec.y;z=vec.z;
        u=axis.x;v=axis.y;w=axis.z;
        double xPrime = u*(u*x + v*y + w*z)*(1d - Math.cos(theta))
                + x*Math.cos(theta)
                + (-w*y + v*z)*Math.sin(theta);
        double yPrime = v*(u*x + v*y + w*z)*(1d - Math.cos(theta))
                + y*Math.cos(theta)
                + (w*x - u*z)*Math.sin(theta);
        double zPrime = w*(u*x + v*y + w*z)*(1d - Math.cos(theta))
                + z*Math.cos(theta)
                + (-v*x + u*y)*Math.sin(theta);
        return new Vec3d(xPrime, yPrime, zPrime);
    }

    /** Sets the quaternion to the given euler angles in radians.
     * @param yaw the rotation around the y axis in radians
     * @param pitch the rotation around the x axis in radians
     * @param roll the rotation around the z axis in radians
     * @return this quaternion */
    public Quaternion setEulerAnglesRad (float yaw, float pitch, float roll) {
        final float hr = roll * 0.5f;
        final float shr = (float)Math.sin(hr);
        final float chr = (float)Math.cos(hr);
        final float hp = pitch * 0.5f;
        final float shp = (float)Math.sin(hp);
        final float chp = (float)Math.cos(hp);
        final float hy = yaw * 0.5f;
        final float shy = (float)Math.sin(hy);
        final float chy = (float)Math.cos(hy);
        final float chy_shp = chy * shp;
        final float shy_chp = shy * chp;
        final float chy_chp = chy * chp;
        final float shy_shp = shy * shp;

        Quaternion q = new Quaternion();

        q.x = (chy_shp * chr) + (shy_chp * shr); // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
        q.y = (shy_chp * chr) - (chy_shp * shr); // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
        q.z = (chy_chp * shr) - (shy_shp * chr); // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
        q.w = (chy_chp * chr) + (shy_shp * shr); // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
        return q;
    }

}
