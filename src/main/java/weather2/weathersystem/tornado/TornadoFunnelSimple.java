package weather2.weathersystem.tornado;

import com.corosus.coroutil.util.CULog;
import com.mojang.math.Vector3d;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.PivotingParticle;
import extendedrenderer.particle.entity.ParticleTexFX;
import extendedrenderer.particle.entity.PivotingParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TornadoFunnelSimple {

    private ActiveTornadoConfig config;

    public Vector3d pos = new Vector3d(0, 0, 0);

    public List<List<PivotingParticle>> listLayers = new ArrayList<>();

    //public int amountPerLayer = 30;
    //public int particleCount = amountPerLayer * 50;

    private float heightPerLayer = 1F;

    private float adjHeight = 0.1F;
    private float adjRadiusOfBase = 0.1F;
    private float adjRadiusIncreasePerLayer = 0.1F;

    public TornadoFunnelSimple(ActiveTornadoConfig config) {
        this.config = config;
    }

    public void init() {
        listLayers.clear();
    }

    public void tickClient() {

        //config.setRadiusIncreasePerLayer(0.2F);

        //config.setHeight(10).setRadiusOfBase(3).setSpinSpeed(360F / 20F).setRadiusIncreasePerLayer(0.5F);
        //config.setHeight(20).setRadiusOfBase(2).setSpinSpeed(360F / 20F).setRadiusIncreasePerLayer(0.1F);

        //adjHeight = 1;

        config.setRadiusOfBase(5F);
        config.setHeight(30);
        config.setRadiusIncreasePerLayer(0.3F);
        pos.set(6026, 79, 7239);

        //temp stress testing
        /*config.setHeight(config.getHeight() + adjHeight);
        if (config.getHeight() > 40) {
            adjHeight = -0.1F;
        } else if (config.getHeight() <= 0) {
            adjHeight = 0.1F;
        }

        config.setRadiusOfBase(config.getRadiusOfBase() + adjRadiusOfBase * 0.3F);
        if (config.getRadiusOfBase() > 10) {
            adjRadiusOfBase = -0.1F;
        } else if (config.getRadiusOfBase() <= 0) {
            adjRadiusOfBase = 0.1F;
        }

        config.setRadiusIncreasePerLayer(config.getRadiusIncreasePerLayer() + adjRadiusIncreasePerLayer * 0.01F);
        if (config.getRadiusIncreasePerLayer() > 0.9) {
            adjRadiusIncreasePerLayer *= -1;
        } else if (config.getRadiusIncreasePerLayer() <= 0.01F) {
            adjRadiusIncreasePerLayer *= -1;
        }*/

        Player entP = Minecraft.getInstance().player;
        ClientLevel level = (ClientLevel) entP.level;
        int layers = (int) (config.getHeight() / heightPerLayer);
        float radiusMax = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (layers+1));

        //cleanup layers beyond current size
        //while (listLayers.size() > layers) {
        for (int i = layers; i < listLayers.size(); i++) {
            List<PivotingParticle> listLayer = listLayers.get(i);
            Iterator<PivotingParticle> it = listLayer.iterator();
            while (it.hasNext()) {
                PivotingParticle particle = it.next();
                it.remove();
                particle.remove();
            }
        }

        int particleCount = 0;

        for (int i = 0; i < layers; i++) {

            //grow layer count as height increases
            if (i < listLayers.size()+1) {
                listLayers.add(new ArrayList<>());
            }

            /**
             * get radius for current layer
             * convert to circumference (c = r2 * pi)
             * count = space per particle / circumference
             */

            List<PivotingParticle> listLayer = listLayers.get(i);

            float radius = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (i+1));
            float radiusAdjustedForParticleSize = radius * (radius / radiusMax);

            float circumference = radius * 2 * Mth.PI;
            float particleSpaceOccupy = 0.5F * (radius / radiusMax);
            float particlesPerLayer = (float) Math.floor(circumference / particleSpaceOccupy);

            Iterator<PivotingParticle> it = listLayer.iterator();
            float index = 0;
            while (it.hasNext()) {
                PivotingParticle particle = it.next();
                if (!particle.isAlive() || index >= particlesPerLayer) {
                    particle.remove();
                    it.remove();
                } else {
                    index++;
                }
            }

            while (listLayer.size() < particlesPerLayer) {
                PivotingParticle particle = createParticle(level, pos.x, pos.y, pos.z);
                Minecraft.getInstance().particleEngine.add(particle);
                listLayer.add(particle);
            }

            it = listLayer.iterator();
            index = 0;
            while (it.hasNext()) {
                particleCount++;
                PivotingParticle particle = it.next();
                boolean pivotingRotation = true;
                if (!pivotingRotation) {
                    float particleSpacingRadians = (float) Math.toRadians(360 / particlesPerLayer);
                    //particleSpacingRadians = 5;
                    float spinAdj = (float) (Math.toRadians(level.getGameTime() % 360) * 4.22F);
                    float relX = -Mth.sin((particleSpacingRadians * index) + spinAdj) * radius;
                    float relY = (float) (heightPerLayer * (i + 1));
                    float relZ = Mth.cos((particleSpacingRadians * index) + spinAdj) * radius;
                    particle.setPosition(pos.x + relX, pos.y + relY, pos.z + relZ);
                    particle.setPrevPosX(particle.x);
                    particle.setPrevPosY(particle.y);
                    particle.setPrevPosZ(particle.z);
                } else {
                    float particleSpacingDegrees = 360 / particlesPerLayer;
                    //float spinSpeedLayer = (float)layers / (float)(i+1);
                    float spinSpeedLayer = 1F - ((float)(i+1) / (float)layers) + 1F;
                    //float spinSpeedLayer = 1;//(float)layers / (float)(i+1);
                    float spinAdj = (level.getGameTime() % 360) * 20.22F * spinSpeedLayer / (radiusAdjustedForParticleSize);
                    float rot = (particleSpacingDegrees * index) + spinAdj;
                    particle.setPivotRotPrev(particle.getPivotRot());
                    particle.setPivotRot(new Vec3(0, rot, 0));
                    particle.setPivotPrev(particle.getPivot());
                    particle.setPivot(new Vec3(0, radiusAdjustedForParticleSize, 0));

                    Vec3 pivotedPosition = particle.getPivotedPosition(0);
                    int randSizePos = 5;
                    Random rand = new Random(particle.getEntityId());
                    float randX = (rand.nextFloat() * (particle.getEntityId() % randSizePos)) - (randSizePos/2);
                    float randZ = (rand.nextFloat() * (particle.getEntityId() % randSizePos)) - (randSizePos/2);
                    randX = 0;
                    randZ = 0;

                    double var16 = 0 - pivotedPosition.x + randX;
                    double var18 = 0 - pivotedPosition.z + randZ;

                    /*ent.rotationYaw = -(float)(Math.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F;
                    ent.rotationYaw -= ent.getEntityId() % 90;*/

                    particle.prevRotationYaw = particle.rotationYaw;
                    particle.rotationYaw = -(float)(Mth.atan2(var18, var16) * 180.0D / Math.PI) - 90.0F + 180F;
                    //particle.rotationYaw = Mth.wrapDegrees(particle.rotationYaw);
                    //CULog.dbg(particle.rotationYaw + "");
                    int randSize = 60;
                    particle.rotationYaw -= (particle.getEntityId() % randSize) - (randSize/2);
                    particle.rotationPitch = -30;

                    //fix interpolation when angle wraps around
                    if (particle.rotationYaw > 0 && particle.prevRotationYaw < 0) {
                        particle.prevRotationYaw += 360;
                    }/* else if (particle.rotationYaw < 0 && particle.prevRotationYaw > 0) {
                        particle.prevRotationYaw -= 360;
                    }*/

                    //particle.prevRotationYaw = particle.rotationYaw;
                    //particle.rotationYaw += 3;

                    float relY = (float) (heightPerLayer * (i + 1) * (radius / radiusMax));
                    particle.setPosition(pos.x + randX, pos.y + relY, pos.z + randZ);
                    particle.setPrevPosX(particle.x);
                    particle.setPrevPosY(particle.y);
                    particle.setPrevPosZ(particle.z);

                    particle.setScale(5F * (radius / radiusMax));
                    particle.setAlpha(1F);
                }
                particle.setAge(0);
                //particle.setColor(1, 1, 1);
                index++;
            }

        }

        //CULog.dbg(particleCount + "");
    }

    private PivotingParticle createParticle(ClientLevel world, double x, double y, double z) {
        //ParticleTexFX particle = new ParticleTexFX(world, x, y, z, 0, 0, 0, ParticleRegistry.square16);
        PivotingParticle particle = new PivotingParticle(world, x, y, z, 0, 0, 0, ParticleRegistry.cloud256);
        particle.setMaxAge(250);
        //particle.setTicksFadeInMax(20);
        //particle.setTicksFadeOutMax(20);
        particle.setParticleSpeed(0, 0, 0);
        particle.setScale(0.1F);
        particle.setScale(5F);
        //particle.setColor(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
        float baseBright = 0.3F;
        float randFloat = (world.random.nextFloat() * 0.6F);
        float finalBright = Math.min(1F, baseBright+randFloat);
        particle.setColor(finalBright-0.2F, finalBright-0.2F, finalBright-0.2F);
        particle.setGravity(0);
        particle.rotationYaw = world.random.nextFloat() * 360;
        return particle;
    }
}
