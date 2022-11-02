package weather2.weathersystem.tornado.simple;

import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.PivotingParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.weathersystem.storm.StormObject;
import weather2.weathersystem.tornado.ActiveTornadoConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TornadoFunnelSimple {

    private ActiveTornadoConfig config;

    public Vec3 pos = new Vec3(0, 0, 0);
    public List<Layer> listLayers = new ArrayList<>();

    private float heightPerLayer = 1F;

    private float adjHeight = 0.1F;
    private float adjRadiusOfBase = 0.1F;
    private float adjRadiusIncreasePerLayer = 0.1F;

    private StormObject stormObject;

    public TornadoFunnelSimple(ActiveTornadoConfig config, StormObject stormObject) {
        this.config = config;
        this.stormObject = stormObject;
    }

    public void init() {
        listLayers.clear();
    }

    //TODO: sync the coords to client
    public void tick() {
        Level level = stormObject.manager.getWorld();

        config.setRadiusOfBase(5F + (stormObject.tornadoHelper.getTornadoBaseSize() / 2));
        config.setRadiusOfBase(5F + (stormObject.tornadoHelper.getTornadoBaseSize() / 3));
        config.setRadiusOfBase(5F + 5F);
        config.setHeight(150);
        config.setRadiusIncreasePerLayer(0.5F);
        config.setRadiusIncreasePerLayer(0.2F);
        //config.setRadiusIncreasePerLayer(0.1F);

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


        int layers = (int) (config.getHeight() / heightPerLayer);
        float radiusMax = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (layers+1));

        for (int i = 0; i < layers; i++) {

            //grow layer count as height increases
            //if (i < listLayers.size()+1) {
            if (i >= listLayers.size()) {
                listLayers.add(new Layer(pos));
            }
            /*if (i < layers+1) {
                listLayers.add(new Layer(pos));
            }*/

            /**
             * get radius for current layer
             * convert to circumference (c = r2 * pi)
             * count = space per particle / circumference
             */

            float radius = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (i+1));
            float radiusAdjustedForParticleSize = radius * (radius / radiusMax);

            float circumference = radius * 2 * Mth.PI;
            //float particleSpaceOccupy = 0.5F * (radius / radiusMax);
            float particleSpaceOccupy = 15F * (radius / radiusMax);
            float particlesPerLayer = (float) Math.floor(circumference / particleSpaceOccupy);

            int debrisPerLayer = 20;
            //debrisPerLayer = 0;

            Vec3 posLayer = listLayers.get(i).getPos();

            float relYDown1 = (float) (heightPerLayer * (radius / radiusMax));

            Vec3 posLayerLower;
            if (i == 0) {
                posLayerLower = new Vec3(pos.x, pos.y, pos.z);
            } else {
                Vec3 temp = listLayers.get(i-1).getPos();
                posLayerLower = new Vec3(temp.x, temp.y + relYDown1, temp.z);
            }

            //if (i != 0) {
            double dist = posLayer.distanceTo(posLayerLower);
            if (dist > 0.1F * (radius / radiusMax)) {
                double dynamicSpeed = 10F * (Math.min(30F, dist) / 30F);
                double speed = dynamicSpeed;//0.01F;
                Vec3 moveVec = posLayer.vectorTo(posLayerLower).normalize().multiply(speed, speed * 1F, speed);
                Vec3 newPos = posLayer.add(moveVec);
                listLayers.get(i).setPos(new Vec3(newPos.x, newPos.y, newPos.z));
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient() {

        Player entP = Minecraft.getInstance().player;
        //ClientLevel level = (ClientLevel) entP.level;
        Level level = stormObject.manager.getWorld();

        int layers = (int) (config.getHeight() / heightPerLayer);
        float radiusMax = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (layers+1));

        //cleanup layers beyond current size
        //while (listLayers.size() > layers) {
        for (int i = layers; i < listLayers.size(); i++) {
            List<PivotingParticle> listLayer = listLayers.get(i).getListParticles();
            Iterator<PivotingParticle> it = listLayer.iterator();
            while (it.hasNext()) {
                PivotingParticle particle = it.next();
                it.remove();
                particle.remove();
            }
        }

        int particleCount = 0;

        float adjustedRate = 1F;
        if (Minecraft.getInstance().options.particles == ParticleStatus.DECREASED) {
            adjustedRate = 0.6F;
        } else if (Minecraft.getInstance().options.particles == ParticleStatus.MINIMAL) {
            adjustedRate = 0.3F;
        }

        for (int i = 0; i < layers; i++) {

            /**
             * get radius for current layer
             * convert to circumference (c = r2 * pi)
             * count = space per particle / circumference
             */

            List<PivotingParticle> listLayer = listLayers.get(i).getListParticles();
            List<PivotingParticle> listLayerExtra = listLayers.get(i).getListParticlesExtra();

            float radius = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (i+1));
            float radiusAdjustedForParticleSize = radius * (radius / radiusMax);

            float circumference = radius * 2 * Mth.PI;
            //float particleSpaceOccupy = 0.5F * (radius / radiusMax);
            float particleSpaceOccupy = (15F / adjustedRate) * (radius / radiusMax);
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
                PivotingParticle particle = createParticle((ClientLevel) level, pos.x, pos.y, pos.z);
                particle.spawnAsWeatherEffect();
                listLayer.add(particle);
            }

            //extra debris
            particlesPerLayer = (int) (20 * adjustedRate);

            //extra stuff
            Iterator<PivotingParticle> it2 = listLayerExtra.iterator();
            float index2 = 0;
            while (it2.hasNext()) {
                PivotingParticle particle = it2.next();
                if (!particle.isAlive() || index2 >= particlesPerLayer) {
                    particle.remove();
                    it2.remove();
                } else {
                    index2++;
                }
            }

            //int debrisPerLayer = (int) (20 * adjustedRate);
            //debrisPerLayer = 0;

            if (stormObject.manager.getWorld().isClientSide()) {
                while (listLayerExtra.size() < particlesPerLayer) {
                    PivotingParticle particle = createParticleDebris((ClientLevel) level, pos.x, pos.y, pos.z);
                    particle.spawnAsWeatherEffect();
                    listLayerExtra.add(particle);
                }
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
                    float spinAdj = (level.getGameTime()) * 50.22F * spinSpeedLayer / (radiusAdjustedForParticleSize);
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
                    int randSize = 90;
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

                    Vec3 posLayer = listLayers.get(i).getPos();

                    float relY = (float) (heightPerLayer * (i + 1) * (radius / radiusMax));
                    relY = 0;
                    posLayer = listLayers.get(i).getPos();
                    particle.setPosition(posLayer.x + randX, posLayer.y + relY, posLayer.z + randZ);
                    particle.setPrevPosX(particle.x);
                    particle.setPrevPosY(particle.y);
                    particle.setPrevPosZ(particle.z);

                    particle.setScale(10F * (radius / radiusMax));
                    particle.setAlpha(1F);
                }
                particle.setAge(0);
                //particle.setColor(1, 1, 1);
                index++;
            }

            //TODO: oh god stop the copypasta
            it = listLayerExtra.iterator();
            index = 0;
            while (it.hasNext()) {
                particleCount++;
                PivotingParticle particle = it.next();
                //particle.remove();
                float radAdj = (float)(particle.getEntityId() % particlesPerLayer) / (float)particlesPerLayer;
                radAdj = 0.4F + (radAdj * 0.6F);
                float moar = i + (i * 0.5F);
                moar = i * 0.5F;
                radiusAdjustedForParticleSize = (radius * (radius / radiusMax) + moar) * radAdj;
                //radiusAdjustedForParticleSize = radius * (3F * radAdj);
                //radiusAdjustedForParticleSize = radius * 1.1F;
                //radiusAdjustedForParticleSize = 20F;

                float particleSpacingDegrees = 360 / particlesPerLayer;
                //float spinSpeedLayer = (float)layers / (float)(i+1);
                float spinSpeedLayer = 1F - ((float)(i+1) / (float)layers) + 1F;
                //float spinSpeedLayer = 1;//(float)layers / (float)(i+1);
                float spinAdj = (level.getGameTime()) * 50.22F * spinSpeedLayer / (radiusAdjustedForParticleSize);
                float rot = (particleSpacingDegrees * index) + spinAdj;
                particle.setPivotRotPrev(particle.getPivotRot());
                particle.setPivotRot(new Vec3(0, rot, 0));
                particle.setPivotPrev(particle.getPivot());
                particle.setPivot(new Vec3(0, radiusAdjustedForParticleSize, 0));

                Vec3 pivotedPosition = particle.getPivotedPosition(0);
                int randSizePos = 5;
                Random rand = new Random(particle.getEntityId());

                particle.prevRotationYaw = particle.rotationYaw;
                particle.rotationYaw += 5F;
                //particle.rotationYaw = Mth.wrapDegrees(particle.rotationYaw);
                //CULog.dbg(particle.rotationYaw + "");
                int randSize = 60;
                //particle.rotationYaw -= (particle.getEntityId() % randSize) - (randSize/2);
                particle.rotationPitch = -30;

                //particle.prevRotationYaw = particle.rotationYaw;
                //particle.rotationYaw += 3;

                Vec3 posLayer = listLayers.get(i).getPos();

                float relY = (float) (heightPerLayer * (i + 1) * (radius / radiusMax));
                relY = 0;
                particle.setPosition(posLayer.x, posLayer.y + relY, posLayer.z);
                particle.setPrevPosX(particle.x);
                particle.setPrevPosY(particle.y);
                particle.setPrevPosZ(particle.z);

                particle.setScale(8 * 0.15F);
                particle.setAlpha(1F);

                particle.setAge(0);
                particle.setGravity(0);
                //particle.setColor(1, 1, 1);
                index++;
            }

            //listLayers.get(i).setPos(new Vector3d(pos.x, pos.y, pos.z));

        }

        //CULog.dbg(particleCount + "");
    }

    @OnlyIn(Dist.CLIENT)
    private PivotingParticle createParticle(ClientLevel world, double x, double y, double z) {
        //ParticleTexFX particle = new ParticleTexFX(world, x, y, z, 0, 0, 0, ParticleRegistry.square16);
        PivotingParticle particle = new PivotingParticle(world, x, y, z, 0, 0, 0, ParticleRegistry.cloud256);
        particle.setMaxAge(250);
        //particle.setTicksFadeInMax(20);
        //particle.setTicksFadeOutMax(20);
        particle.setParticleSpeed(0, 0, 0);
        particle.setScale(0.1F);
        particle.setScale(5F);
        particle.setScale(15F);
        //particle.setColor(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
        float baseBright = 0.3F;
        float randFloat = (world.random.nextFloat() * 0.6F);
        float finalBright = Math.min(1F, baseBright+randFloat);
        particle.setColor(finalBright-0.2F, finalBright-0.2F, finalBright-0.2F);
        particle.setGravity(0);
        particle.rotationYaw = world.random.nextFloat() * 360;
        return particle;
    }

    @OnlyIn(Dist.CLIENT)
    private PivotingParticle createParticleDebris(ClientLevel world, double x, double y, double z) {
        //ParticleTexFX particle = new ParticleTexFX(world, x, y, z, 0, 0, 0, ParticleRegistry.square16);
        int chance = world.getRandom().nextInt(3);
        TextureAtlasSprite sprite = ParticleRegistry.debris_1;
        if (chance == 1) {
            sprite = ParticleRegistry.debris_2;
        } else if (chance == 2) {
            sprite = ParticleRegistry.debris_3;
        }/* else if (chance == 3) {
            sprite = ParticleRegistry.tumbleweed;
        }*/
        PivotingParticle particle = new PivotingParticle(world, x, y, z, 0, 0, 0, sprite);
        /*if (chance == 3) {
            particle = new ParticleCrossSection(world, x, y, z, 0, 0, 0, sprite);
        }*/
        particle.setMaxAge(250);
        //particle.setTicksFadeInMax(20);
        //particle.setTicksFadeOutMax(20);
        particle.setParticleSpeed(0, 0, 0);

        particle.setFacePlayer(false);
        particle.spinFast = true;
        particle.isTransparent = true;
        particle.rotationYaw = (float)world.getRandom().nextInt(360);
        particle.rotationPitch = (float)world.getRandom().nextInt(360);

        particle.setLifetime(80);
        particle.setGravity(0.3F);
        particle.setAlpha(0F);
        float brightnessMulti = 1F - (world.getRandom().nextFloat() * 0.5F);
        //particle.setColor(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
        particle.setColor(1F * brightnessMulti, 1F * brightnessMulti, 1F * brightnessMulti);
        particle.setScale(8 * 0.15F);
        particle.aboveGroundHeight = 0.5D;
        particle.collisionSpeedDampen = false;
        particle.bounceSpeed = 0.03D;
        particle.bounceSpeedAhead = 0.03D;

        particle.setKillOnCollide(false);

        particle.windWeight = 1F;

        return particle;
    }

    public Vec3 getPosTop() {
        if (listLayers.size() == 0) return pos;
        return listLayers.get(listLayers.size()-1).getPos();
    }

    public StormObject getStormObject() {
        return stormObject;
    }

    public void setStormObject(StormObject stormObject) {
        this.stormObject = stormObject;
    }

    public void cleanup() {
        listLayers.clear();
    }

    /**
     * Dramatic version for effect
     */
    public void cleanupClient() {
        for (int i = 0; i < listLayers.size(); i++) {
            listLayers.get(i).getListParticles().stream().forEach(pivotingParticle -> disperseParticleSmoothly(pivotingParticle));
            listLayers.get(i).getListParticlesExtra().stream().forEach(pivotingParticle -> disperseParticleSmoothly(pivotingParticle));
            listLayers.get(i).getListParticles().clear();
            listLayers.get(i).getListParticlesExtra().clear();
        }
    }

    public void disperseParticleSmoothly(PivotingParticle pivotingParticle) {
        pivotingParticle.prevRotationYaw = pivotingParticle.rotationYaw;
        pivotingParticle.setPivotPrev(pivotingParticle.getPivot());
        pivotingParticle.setPivotRotPrev(pivotingParticle.getPivotRot());
        Random rand = new Random();
        pivotingParticle.setMotionX((rand.nextFloat() - rand.nextFloat()) * 2F);
        pivotingParticle.setMotionZ((rand.nextFloat() - rand.nextFloat()) * 2F);
        pivotingParticle.setMaxAge(100);
        pivotingParticle.setTicksFadeOutMax(80);
        pivotingParticle.spinFast = false;
    }

    public void cleanupClientQuick() {
        for (int i = 0; i < listLayers.size(); i++) {
            listLayers.get(i).getListParticles().stream().forEach(pivotingParticle -> pivotingParticle.remove());
            listLayers.get(i).getListParticlesExtra().stream().forEach(pivotingParticle -> pivotingParticle.remove());
            listLayers.get(i).getListParticles().clear();
            listLayers.get(i).getListParticlesExtra().clear();
        }
    }

    public ActiveTornadoConfig getConfig() {
        return config;
    }

    public void setConfig(ActiveTornadoConfig config) {
        this.config = config;
    }
}
