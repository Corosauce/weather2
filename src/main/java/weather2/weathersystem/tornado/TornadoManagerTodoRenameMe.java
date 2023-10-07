package weather2.weathersystem.tornado;

import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.ParticleTexFX;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import weather2.weathersystem.tornado.simple.TornadoFunnelSimple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TornadoManagerTodoRenameMe {

    private Class lastScreenClass = null;

    private ParticleTexFX particleTest = null;
    private List<ParticleTexFX> particles = new ArrayList<>();

    private TornadoFunnel funnel;

    private TornadoFunnelSimple funnelSimple;

    //public CubicBezierCurve bezierCurve;
    public List<CubicBezierCurve> curves = new ArrayList<>();

    public Vector3f[] vecSpeeds = new Vector3f[10];

    public void tick(Level world) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (mc.level.getGameTime() % 1 == 0) {
            for (Player playerEntity : mc.level.players()) {
                if (true || mc.player.distanceTo(playerEntity) < 20) {

                    int particleCountCircle = 20;
                    int particleCountLayers = 40;

                    while (particles.size() < particleCountCircle * particleCountLayers) {
                        particleTest = new ParticleTexFX(mc.level, playerEntity.getX(), playerEntity.getY() + 2.2, playerEntity.getZ(), 0, 0, 0, ParticleRegistry.square16);
                        //particleTest.setSprite(ParticleRegistry.square16);
                        particleTest.setMaxAge(250);
                        //particleTest.setMotion(0, 0, 0);
                        particleTest.setScale(0.2F);
                        //particleTest.setColor(0.1F * (particles.size() % particleCountCircle), 0, 0);
                        particleTest.setColor(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
                        if (particles.size() < particleCountCircle * 5) {
                            particleTest.setColor(1, 1, 1);
                        }
                        float randGrey = 0.4F + (world.random.nextFloat() * 0.4F);
                        particleTest.setColor(randGrey, randGrey, randGrey);
                        //particleTest.move(0, -0.1, 0);
                        mc.particleEngine.add(particleTest);
                        //particleTest.setAngle(world.randomom.nextInt(360));
                        particles.add(particleTest);
                    }

                    int testY = 100;

                    Vector3f pos1 = new Vector3f(0.5F, 70, 0.5F);
                    Vector3f pos2 = new Vector3f(0.5F, 120, 0.5F);

                    /*float dist = (float)Math.sqrt(playerEntity.getDistanceSq(0.5, testY, 0.5));
                    Vector3f vecDiff = new Vector3f(
                            (float)(playerEntity.getPosX() - 0.5) / dist,
                            (float)(playerEntity.getPosY() - testY) / dist,
                            (float)(playerEntity.getPosZ() - 0.5) / dist);
                    Vector3f vecAngles = new Vector3f(
                            (float)Math.atan2(vecDiff.y(), vecDiff.z()),
                            (float)Math.atan2(vecDiff.z(), vecDiff.x()), //invert if needed
                            (float)Math.atan2(vecDiff.x(), vecDiff.y())); //invert if needed*/

                    float dist = getDistance(pos1, pos2);
                    Vector3f vecDiff = new Vector3f(
                            (pos1.x() - pos2.x()) / dist,
                            (pos1.y() - pos2.y()) / dist,
                            (pos1.z() - pos2.z()) / dist);
                    Vector3f vecAngles = new Vector3f(
                            (float)Math.atan2(vecDiff.y(), vecDiff.z()),
                            (float)Math.atan2(vecDiff.z(), vecDiff.x()), //invert if needed
                            (float)Math.atan2(vecDiff.x(), vecDiff.y())); //invert if needed

                    //convert to degrees
                    vecAngles = new Vector3f((float)Math.toDegrees(vecAngles.x()), (float)Math.toDegrees(vecAngles.y()), (float)Math.toDegrees(vecAngles.z()));

                    double xx = pos1.x() - pos2.x();
                    double zz = pos1.z() - pos2.z();
                    double xzDist = Math.sqrt(xx * xx + zz * zz);
                    float pitchAngle = (float)Math.toDegrees(Math.atan2(vecDiff.y(), xzDist / dist));

                    pitchAngle += 90;

                    /*if (playerEntity.isSprinting()) {
                        curves.clear();
                    }*/

                    while (curves.size() < 2) {
                        //Vector3f[] vecs = new Vector3f[4];
                        Vector3f[] vecs = new Vector3f[10];
                        for (int i = 0; i < vecs.length; i++) {
                            vecs[i] = new Vector3f(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
                        }
                        curves.add(new CubicBezierCurve(vecs));
                    }

                    //trying to smooth the 2 curves together...

                    //match the end and start of each curve
                    //tempoffcurves.get(1).P[0].set(curves.get(0).P[3].x(), curves.get(0).P[3].y(), curves.get(0).P[3].z());

                    //try to match the previous second last to the next second so it'd present a smooth curve
                    //doesnt work, or im doing it wrong
                    //curves.get(1).P[1].set(1F - curves.get(0).P[2].x(), 1F - curves.get(0).P[2].y(), 1F - curves.get(0).P[2].z());

                    //curves.get(1).P[0].set(1F-curves.get(0).P[3].x(), 1F-curves.get(0).P[3].y(), 1F-curves.get(0).P[3].z());
                    //curves.get(1).P[1].set(1F-curves.get(0).P[2].x(), 1F-curves.get(0).P[2].y(), 1F-curves.get(0).P[2].z());
                    //curves.get(1).P[2].set(1F-curves.get(0).P[1].x(), 1F-curves.get(0).P[1].y(), 1F-curves.get(0).P[1].z());
                    //curves.get(1).P[3].set(1F-curves.get(0).P[0].x(), 1F-curves.get(0).P[0].y(), 1F-curves.get(0).P[0].z());

                    CubicBezierCurve bezierCurve = curves.get(0);

                    /*if (bezierCurve == null) {
                        Vector3f[] vecs = new Vector3f[4];
                        for (int i = 0; i < vecs.length; i++) {
                            vecs[i] = new Vector3f(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
                        }
                        bezierCurve = new CubicBezierCurve(vecs);
                    }*/

                    /*if (bezierCurve != null) {
                        float randScale = 0.1F;
                        for (int i = 0; i < bezierCurve.P.length; i++) {
                            bezierCurve.P[i].add((world.random.nextFloat() - world.random.nextFloat()) * randScale, (world.random.nextFloat() - world.random.nextFloat()) * randScale, (world.random.nextFloat() - world.random.nextFloat()) * randScale);
                            bezierCurve.P[i].normalize();
                        }
                    }*/

                    if (bezierCurve != null && true) {
                        float randScale = 0.1F;
                        for (int i = 0; i < bezierCurve.P.length; i++) {
                            if (vecSpeeds[i] == null) {
                                vecSpeeds[i] = new Vector3f(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
                            }

                            bezierCurve.P[i].add(vecSpeeds[i].x() * 0.01F, vecSpeeds[i].y() * 0.01F, vecSpeeds[i].z() * 0.01F);

                            float maxY = 1F;
                            float minY = 0F;

                            /*if (i == 0) {
                                maxY = 0.25F;
                                minY = 0.0F;
                            } else if (i == 1) {
                                maxY = 0.9F;
                                minY = 0.1F;
                            } else if (i == 2) {
                                maxY = 0.9F;
                                minY = 0.1F;
                            } else if (i == 3) {
                                maxY = 1.0F;
                                minY = 0.75F;
                            }*/

                            //maxY += 2;
                            float minXZ = 0;
                            float maxXZ = 1;

                            float randSpeed = 1.5F;

                            if (bezierCurve.P[i].x() > maxXZ) {
                                vecSpeeds[i].set(world.random.nextFloat() * -1 * randSpeed, vecSpeeds[i].y(), vecSpeeds[i].z());
                            } else if (bezierCurve.P[i].x() < minXZ) {
                                vecSpeeds[i].set(world.random.nextFloat() * randSpeed, vecSpeeds[i].y(), vecSpeeds[i].z());
                            }
                            if (bezierCurve.P[i].y() > maxY) {
                                vecSpeeds[i].set(vecSpeeds[i].x(), world.random.nextFloat() * -1 * randSpeed, vecSpeeds[i].z());
                            } else if (bezierCurve.P[i].y() < minY) {
                                vecSpeeds[i].set(vecSpeeds[i].x(), world.random.nextFloat() * randSpeed, vecSpeeds[i].z());
                            }
                            if (bezierCurve.P[i].z() > maxXZ) {
                                vecSpeeds[i].set(vecSpeeds[i].x(), vecSpeeds[i].y(), world.random.nextFloat() * -1 * randSpeed);
                            } else if (bezierCurve.P[i].z() < minXZ) {
                                vecSpeeds[i].set(vecSpeeds[i].x(), vecSpeeds[i].y(), world.random.nextFloat() * randSpeed);
                            }
                            //bezierCurve.P[i].normalize();
                        }

                        //bezierCurve.P[0] = new Vector3f(0.5F, 0, 0.5F);

                        //base of tornado
                        //bezierCurve.P[1].set(bezierCurve.P[0].x(), bezierCurve.P[0].y() + 0.3F, bezierCurve.P[0].z());
                        //bezierCurve.P[0].set(bezierCurve.P[3].x(), 0, bezierCurve.P[3].z());
                        //bezierCurve.P[0].set(bezierCurve.P[0].x(), 0, bezierCurve.P[0].z());

                        //top of tornado
                        //bezierCurve.P[2].set(bezierCurve.P[3].x(), bezierCurve.P[3].y() - 0.3F, bezierCurve.P[3].z());
                        //bezierCurve.P[3].set(bezierCurve.P[3].x(), 1, bezierCurve.P[3].z());
                        if (bezierCurve.P.length == 6) {
                            //bezierCurve.P[5].set(bezierCurve.P[5].x(), 1, bezierCurve.P[5].z());
                        }

                        //bezierCurve.P[0].set(0.5F, 0, 0.5F);
                        //bezierCurve.P[1].set(0.5F, 0.8F, 0.5F);
                        //bezierCurve.P[2].set(0.5F, 0.2F, 0.5F);
                        //bezierCurve.P[3].set(0.5F, 0.99F, 0.5F);

                        //bezierCurve.P[3] = new Vector3f(0.5F, 1, 0.5F);
                    }

                    /*if (mc.level.getGameTime() % 40 == 0 && WATUT.playerManagerClient.getPlayerStatus(mc.player.getUniqueID()).getStatusType() == PlayerStatus.StatusType.CHAT) {
                        System.out.println("x: " + vecAngles.x());
                        System.out.println("y: " + vecAngles.y());
                        System.out.println("z: " + vecAngles.z());
                        //System.out.println("yDiff: " + (playerEntity.getPosY() - testY));
                        System.out.println("pitchAngle: " + pitchAngle);
                    }*/

                    Iterator<ParticleTexFX> it = particles.iterator();
                    int index = 0;

                    float adjustedCurvePos = 0;

                    while (it.hasNext()) {
                        ParticleTexFX particle = it.next();
                        if (!particle.isAlive()) {
                            it.remove();
                        } else {
                            //(index * (360 / particleCount))
                            float x = 0;//((world.getGameTime() * 0.5F) % 360);
                            float y2 = ((world.getGameTime() * 2) % 360) + ((index % particleCountCircle) * (360 / particleCountCircle));
                            float y = /*((world.getGameTime() * 3) % 360) + */((index % particleCountCircle) * (360 / particleCountCircle));
                            float z = 0;//((world.getGameTime() * 0.3F) % 360);

                            y = vecAngles.y() - 90;

                            int yDiff = (index / particleCountCircle) - (particleCountLayers / 2);
                            float yDiffDist = 0.01F;

                            int curLayer = (index / particleCountCircle);
                            float curvePoint = (float)curLayer / (float)particleCountLayers * 1F;
                            float curvePoint2 = (float)Math.min(1D, (float)(curLayer+1) / (float)particleCountLayers) * 1F;
                            float stretchCurveY = 4F;
                            float curveAmp = 2F;
                            y2 = ((world.getGameTime() * (7 + (particleCountLayers - curLayer) * (particleCountLayers - curLayer) * 0.02F)) % 360) + ((index % particleCountCircle) * (360 / particleCountCircle));
                            //y2 = ((index % particleCountCircle) * (360 / particleCountCircle));

                            float distFinal = dist / 2F;

                            /*Vector3f vecCurve1 = bezierCurve.getValue(curvePoint);
                            Vector3f vecCurve2 = bezierCurve.getValue((float)Math.min(1D, (float)(curLayer+1) / (float)particleCountLayers));*/
                            Vector3f vecCurve1 = getCurveValue(curvePoint);
                            Vector3f vecCurve2 = getCurveValue(curvePoint2);

                            Vec2 curvePointYawPitch = yawPitch(vecCurve2, vecCurve1);
                            float curveDist = getDistance(vecCurve1, vecCurve2);

                            if ((index % particleCountCircle) == 0) {
                                adjustedCurvePos += curveDist;
                                //System.out.println(getDistance(vecCurve1, vecCurve2));
                                //System.out.println(curvePointYawPitch.x + " - " + curvePointYawPitch.y);
                            }

                            //Quaternionf quaternionY = new Quaternionf(new Vector3f(0.0F, 1.0F, 0.0F), -y, true);
                            Quaternionf quaternionY = new Quaternionf(0.0F, 1.0F, 0.0F, Math.toRadians(-curvePointYawPitch.x - 90));
                            //adding quaternionY here cancels out the unwanted rotations from the bezier curve adjustments
                            Quaternionf quaternionYCircle = new Quaternionf(0.0F, 1.0F, 0.0F, Math.toRadians(-y2 + (curvePointYawPitch.x - 90)));

                            Quaternionf quatPitch = new Quaternionf(1.0F, 0.0F, 0.0F, Math.toRadians(curvePointYawPitch.y));
                            //Vector3f vecNew = new Vector3f(1F, 1 + ((float)yDiff) * yDiffDist, 0);
                            //Vector3d vecCurve = bezierCurve.getValue(curvePoint);
                            Vector3f vecCurve = getCurveValue(curvePoint);
                            //System.out.println("curvePoint: " + curvePoint + ", " + vecCurve);
                            //Vector3f vecNew = new Vector3f((float)vecCurve.x * curveAmp, (float)vecCurve.y * curveAmp * stretchCurveY * (((float)yDiff) * yDiffDist) + 10F, (float)vecCurve.z * curveAmp);
                            //Vector3f vecNew = new Vector3f((float)vecCurve.x() * curveAmp - curveAmp/2F, (1F + ((float)yDiff) * yDiffDist * (dist*2F)) - (dist/2F), (float)vecCurve.z() * curveAmp - curveAmp/2F);
                            //Vector3f vecNew = new Vector3f(1F * curveAmp, (1F + ((float)yDiff) * yDiffDist * (dist*2F)) - (dist/2F), 0F);
                            //Vector3f vecNew = new Vector3f(1F * curveAmp, (1F + ((float)yDiff) * yDiffDist * (dist*2F)) - (dist/2F), 1F * curveAmp);
                            //Vector3f vecNew = new Vector3f(1F * curveAmp, (((float)yDiff) * distFinal) - (dist/2F), 0);
                            //Vector3f vecNew = new Vector3f(1.3F + Math.min((curLayer * curLayer * 0.005F), 40)/* + (curLayer * 0.05F)*/, 0F, 0);
                            //Vector3f vecNew = new Vector3f(1.3F + 1/* + (curLayer * 0.05F)*/, 0F, 0);
                            Vector3f vecNew = new Vector3f(1/* + (curLayer * 0.05F)*/, 0F, 0);

                            float rotAroundPosX = 0;
                            float rotAroundPosY = 0;
                            float rotAroundPosZ = 0;
                            Matrix3f matrix = new Matrix3f();
                            matrix.rotation(quaternionY);
                            matrix.rotation(quatPitch);
                            matrix.rotation(quaternionYCircle);
                            vecNew.mulTranspose(matrix);

                            rotAroundPosX = vecNew.x();
                            rotAroundPosY = vecNew.y();
                            rotAroundPosZ = vecNew.z();

                            float tiltAdj = 1F;
                            /*tiltAdj = curvePoint;
                            if (tiltAdj > 0.5) {
                                tiltAdj = 1F - tiltAdj;
                            } else if (tiltAdj < 0.2) {
                                //tiltAdj = tiltAdj;
                            } else {
                                tiltAdj = 1F;
                            }*/

                            //particle.setPosition(pos1.x() + rotAroundPosX, pos1.y() + rotAroundPosY, pos1.z() + rotAroundPosZ);
                            //particle.setPosition(pos1.x() + (vecCurve1.x()*distFinal) + rotAroundPosX, pos1.y() + (vecCurve1.y()*distFinal) + rotAroundPosY, pos1.z() + (vecCurve1.z()*distFinal) + rotAroundPosZ);
                            //particle.setPosition(pos1.x() + (vecCurve1.x()*distFinal) + rotAroundPosX, pos1.y() + (curLayer) + rotAroundPosY, pos1.z() + (vecCurve1.z()*distFinal) + rotAroundPosZ);
                            //particle.setPosition(pos1.x() + (vecCurve1.x()*distFinal) + rotAroundPosX, pos1.y() + (curLayer) + (rotAroundPosY * (tiltAdj)), pos1.z() + (vecCurve1.z()*distFinal) + rotAroundPosZ);
                            particle.setPosition(pos1.x() + (vecCurve1.x()*distFinal) + rotAroundPosX, pos1.y() + (vecCurve1.y()*distFinal) + (rotAroundPosY * (tiltAdj)), pos1.z() + (vecCurve1.z()*distFinal) + rotAroundPosZ);
                            //particle.setPosition(0, 100, 0);
                            particle.setMotionX(0);
                            particle.setMotionY(0);
                            particle.setMotionZ(0);
                            /*particle.setPrevPosX(particle.getPosX());
                            particle.setPrevPosY(particle.getPosY());
                            particle.setPrevPosZ(particle.getPosZ());*/
                            //particle.setPosition(pos1.x() + (vecCurve1.x()*distFinal) + rotAroundPosX, pos1.y() + 1 + curLayer, pos1.z() + (vecCurve1.z()*distFinal) + rotAroundPosZ);
                        }
                        index++;
                    }
                }
            }
        }

        if (funnel == null) {
            funnel = new TornadoFunnel();
            funnel.pos = new Vector3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }

        //funnel.tickGame();

        /*if (funnelSimple == null) {
            ActiveTornadoConfig activeTornadoConfig = new ActiveTornadoConfig().setHeight(10).setRadiusOfBase(3).setSpinSpeed(360F / 20F).setRadiusIncreasePerLayer(0.5F);
            funnelSimple = new TornadoFunnelSimple(activeTornadoConfig);
            funnelSimple.pos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        }*/

        //funnelSimple.tickClient();

    }

    public float getDistance(Vector3f vec1, Vector3f vec2) {
        float f = (vec1.x() - vec2.x());
        float f1 = (vec1.y() - vec2.y());
        float f2 = (vec1.z() - vec2.z());
        return Mth.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    /**
     *
     * @param pos2
     * @param pos1
     * @return yaw and pitch in degrees
     */
    public Vec2 yawPitch(Vector3f pos2, Vector3f pos1) {
        float dist = getDistance(pos1, pos2);
        Vector3f vecDiff = new Vector3f(
                (pos1.x() - pos2.x()) / dist,
                (pos1.y() - pos2.y()) / dist,
                (pos1.z() - pos2.z()) / dist);
        Vector3f vecAngles = new Vector3f(
                (float)Math.atan2(vecDiff.y(), vecDiff.z()),
                (float)Math.atan2(vecDiff.z(), vecDiff.x()), //invert if needed
                (float)Math.atan2(vecDiff.x(), vecDiff.y())); //invert if needed

        double xx = pos1.x() - pos2.x();
        double zz = pos1.z() - pos2.z();
        double xzDist = Math.sqrt(xx * xx + zz * zz);
        double wat = xzDist / dist;
        float pitchAngle = (float)Math.toDegrees(Math.atan2(vecDiff.y(), xzDist / dist));

        vecAngles = new Vector3f((float)Math.toDegrees(vecAngles.x()), (float)Math.toDegrees(vecAngles.y()), (float)Math.toDegrees(vecAngles.z()));

        pitchAngle += 90;

        return new Vec2(vecAngles.y(), pitchAngle);
    }

    public Vector3f getCurveValue(float val) {
        int arrayEntry = (int)Math.floor(val);
        if (arrayEntry > curves.size()-1) {
            System.out.println("out of bounds on curve lookup, val: " + val + " curves: - " + curves.size());
            return new Vector3f(1F, 1F, 1F);
        }
        CubicBezierCurve curve = curves.get(arrayEntry);
        return curve.getValue(val % 1F);
    }

}
