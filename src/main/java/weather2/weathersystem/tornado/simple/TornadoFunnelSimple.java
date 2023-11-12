package weather2.weathersystem.tornado.simple;

import com.corosus.coroutil.util.CULog;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.PivotingParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import weather2.Weather;
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

    private StormObject stormObject;

    private float targetSizeRadius = 0;
    private float sizeRadiusRate = 0;
    private float renderDistCutoff = 50;

    //hack to fix client data coming in late
    private boolean wasFirenado = false;

    public TornadoFunnelSimple(ActiveTornadoConfig config, StormObject stormObject) {
        this.config = config;
        this.stormObject = stormObject;
        config.setRadiusOfBase(stormObject.tornadoHelper.getTornadoBaseSize() / 2);
    }

    public void init() {
        listLayers.clear();
    }

    public void tick() {
        if (stormObject.isPet()) {
            heightPerLayer = 0.2F;
        }

        //TESTING
        //config.setEntityPullDistXZForY(90);

        //dynamic sizing
        targetSizeRadius = stormObject.tornadoHelper.getTornadoBaseSize() / 2;
        sizeRadiusRate = 0.01F;

        if (config.getRadiusOfBase() != targetSizeRadius) {
            //CULog.dbg("tornado size transitioning: " + config.getRadiusOfBase());
            if (config.getRadiusOfBase() < targetSizeRadius) {
                config.setRadiusOfBase(config.getRadiusOfBase() + sizeRadiusRate);
                if (config.getRadiusOfBase() > targetSizeRadius) config.setRadiusOfBase(targetSizeRadius);
            } else {
                config.setRadiusOfBase(config.getRadiusOfBase() - sizeRadiusRate);
                if (config.getRadiusOfBase() < targetSizeRadius) config.setRadiusOfBase(targetSizeRadius);
            }
        }

        int layers = (int) (config.getHeight() / heightPerLayer);
        float radiusMax = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (layers+1));

        for (int i = 0; i < layers; i++) {

            //grow layer count as height increases
            if (i >= listLayers.size()) {
                listLayers.add(new Layer(stormObject.posBaseFormationPos));
            }

            /**
             * get radius for current layer
             * convert to circumference (c = r2 * pi)
             * count = space per particle / circumference
             */

            float radius = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (i));

            Vec3 posLayer = listLayers.get(i).getPos();

            float relYDown1 = (heightPerLayer * (radius / radiusMax));

            Vec3 posLayerLower;
            if (i == 0) {
                posLayerLower = new Vec3(pos.x, pos.y, pos.z);
            } else {
                Vec3 temp = listLayers.get(i-1).getPos();
                posLayerLower = new Vec3(temp.x, temp.y + relYDown1, temp.z);
            }

            double dist = posLayer.distanceTo(posLayerLower);
            //easy way to fix the spawning at 0,0 issue
            if (dist > 50) {
                CULog.dbg("teleporting tornado layer to lower piece");
                listLayers.get(i).setPos(new Vec3(posLayerLower.x, posLayerLower.y, posLayerLower.z));
            } else if (dist > 0.1F * (radius / radiusMax)) {
                double dynamicSpeed = 15F * (Math.min(30F, dist) / 30F);
                double speed = dynamicSpeed;//0.01F;
                Vec3 moveVec = posLayer.vectorTo(posLayerLower).normalize().multiply(speed, speed * 1F, speed);
                Vec3 newPos = posLayer.add(moveVec);
                listLayers.get(i).setPos(new Vec3(newPos.x, newPos.y, newPos.z));
            }
        }

        Level level = stormObject.manager.getWorld();

        if (stormObject.isSharknado()) {
            if (!level.isClientSide()) {
                if (level.getGameTime() % 20 == 0) {
                    Entity ent = null;
                    if (Weather.isLoveTropicsInstalled()) {
                        /**
                         * TODO: for LT, turn back on when LT is needed, activates dependency on LTWeather / Tropicraft
                         */
                        //ent = new SharkEntity(TropicraftEntities.HAMMERHEAD.get(), level);
                    } else {
                        ent = new Dolphin(EntityType.DOLPHIN, level);
                    }
                    if (ent == null) {
                        CULog.log("SharkEntity not spawned, enable in weather mod");
                        ent = new Dolphin(EntityType.DOLPHIN, level);
                    }
                    Vec3 posRand = new Vec3(pos.x + 0, pos.y + 3, pos.z - 5);
                    ent.setPos(posRand);
                    ent.setDeltaMovement(3F, 0, 0);
                    level.addFreshEntity(ent);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient() {
        long gameTime = stormObject.getAge();

        Level level = stormObject.manager.getWorld();

        renderDistCutoff = Minecraft.getInstance().gameRenderer.getRenderDistance() * 4;

        int layers = (int) (config.getHeight() / heightPerLayer);
        float radiusMax = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (layers+1));

        boolean isBaby = stormObject.isBaby();
        boolean isPet = stormObject.isPet();

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
        if (!isPet) {
            if (Minecraft.getInstance().options.particles.get() == ParticleStatus.DECREASED) {
                adjustedRate = 0.6F;
            } else if (Minecraft.getInstance().options.particles.get() == ParticleStatus.MINIMAL) {
                adjustedRate = 0.3F;
            }
        }

        int layersWithDebris = stormObject.getAgeSinceTornadoTouchdown()/5;
        //CULog.dbg("layersWithDebris: " + layersWithDebris);

        for (int i = 0; i < layers; i++) {

            /**
             * get radius for current layer
             * convert to circumference (c = r2 * pi)
             * count = space per particle / circumference
             */

            List<PivotingParticle> listLayer = listLayers.get(i).getListParticles();
            List<PivotingParticle> listLayerExtra = listLayers.get(i).getListParticlesExtra();

            float radius = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (i));
            float radiusAdjustedForParticleSize = radius * (radius / radiusMax);

            float circumference = radius * 2 * Mth.PI;
            //float particleSpaceOccupy = 0.5F * (radius / radiusMax);
            float particleSpaceOccupy = (15F / adjustedRate) * (radius / radiusMax);
            if (isBaby) particleSpaceOccupy = (2F / adjustedRate) * (radius / radiusMax);
            if (isPet) particleSpaceOccupy = (0.2F / adjustedRate) * (radius / radiusMax);
            float particlesPerLayer = (float) /*Math.floor(*/circumference / particleSpaceOccupy/*)*/;

            Iterator<PivotingParticle> itt = listLayer.iterator();
            float indexx = 0;
            while (itt.hasNext()) {
                PivotingParticle particle = itt.next();
                if (!particle.isAlive() || indexx >= particlesPerLayer) {
                    particle.remove();
                    itt.remove();
                } else {
                    indexx++;
                }
            }
            //cleanupList(listLayer, (int)particlesPerLayer);

            int firstLayerForParticles = 6;
            if (stormObject.isBaby()) {
                firstLayerForParticles = 0;
            }

            while (listLayer.size() < particlesPerLayer && i >= firstLayerForParticles) {
                PivotingParticle particle = createParticle((ClientLevel) level, pos.x, pos.y, pos.z);
                particle.spawnAsWeatherEffect();
                listLayer.add(particle);
            }

            float particleSpacingDegrees = 360 / particlesPerLayer;
            float spinSpeedLayer = 1F - ((float)(i+1) / (float)layers) + 1F;

            if (isPet) {
                listLayers.get(i).setRotation(listLayers.get(i).getRotation() + (10F * spinSpeedLayer / (radiusAdjustedForParticleSize)));
            } else {
                listLayers.get(i).setRotation(listLayers.get(i).getRotation() + (50.22F * spinSpeedLayer / (radiusAdjustedForParticleSize)));
            }

            Iterator<PivotingParticle> it = listLayer.iterator();
            int index = 0;
            while (it.hasNext()) {
                particleCount++;
                PivotingParticle particle = it.next();

                float rot = ((particleSpacingDegrees * index) + listLayers.get(i).getRotation());
                particle.setPivotRotPrev(particle.getPivotRot());
                particle.setPivotRot(new Vec3(0, rot, 0));
                particle.setPivotPrev(particle.getPivot());
                particle.setPivot(new Vec3(0, radiusAdjustedForParticleSize, 0));

                Vec3 pivotedPosition = particle.getPivotedPosition(0);

                double vecX = 0 - pivotedPosition.x;
                double vecZ = 0 - pivotedPosition.z;

                particle.prevRotationYaw = particle.rotationYaw;
                particle.rotationYaw = -(float)(Mth.atan2(vecZ, vecX) * 180.0D / Math.PI) - 90.0F + 180F;
                int rotationVarianceSize = 90;
                particle.rotationYaw -= (particle.getEntityId() % rotationVarianceSize) - (rotationVarianceSize/2);
                particle.rotationPitch = -30;

                //fix interpolation when angle wraps around
                if (particle.rotationYaw > 0 && particle.prevRotationYaw < 0) {
                    particle.prevRotationYaw += 360;
                }/* else if (particle.rotationYaw < 0 && particle.prevRotationYaw > 0) {
                    particle.prevRotationYaw -= 360;
                }*/

                Vec3 posLayer = listLayers.get(i).getPos();
                particle.setPosition(posLayer.x, posLayer.y, posLayer.z);
                particle.setPrevPosX(particle.x);
                particle.setPrevPosY(particle.y);
                particle.setPrevPosZ(particle.z);

                particle.setScale(10F * (radius / radiusMax));
                if (isBaby) particle.setScale(10F / 3F * (radius / radiusMax));
                if (isPet) particle.setScale(10F / 3F / 7F * (radius / radiusMax));
                //allow fade in but stop age after
                if (particle.getAge() > particle.getTicksFadeInMax()+1) particle.setAge((int)particle.getTicksFadeInMax()+1);

                /*particle.setScale(0.3F);

                if (i % 2 == 0) {
                    particle.setColor(0, 0, 0);
                } else {
                    particle.setColor(1, 1, 1);
                }*/

                if (stormObject.isFirenado && !wasFirenado) {
                    if (particle.getSprite() == ParticleRegistry.cloud256) {
                        particle.setSprite(ParticleRegistry.cloud256_fire);
                    }

                    float baseBright = 0.8F;
                    float randFloat = (level.random.nextFloat() * 0.2F);
                    float finalBright = Math.min(1F, baseBright + randFloat);
                    particle.setColor(finalBright, finalBright, finalBright);
                }

                index++;
            }

            //extra debris

            particlesPerLayer = (int) (20 * adjustedRate);
            if (isBaby) particlesPerLayer = (int) (10 * adjustedRate);
            if (isPet) particlesPerLayer = (int) (5 * adjustedRate);
            if (stormObject.levelCurIntensityStage == StormObject.STATE_FORMING) {
                particlesPerLayer = 0;
            }

            cleanupList(listLayerExtra, (int)particlesPerLayer);

            //int particlesPerLayerDynamic = stormObject.getAgeSinceTornadoTouchdown()/20;

            if (i <= layersWithDebris && i >= firstLayerForParticles + 1) {
                while (listLayerExtra.size() < particlesPerLayer) {
                    PivotingParticle particle = createParticleDebris((ClientLevel) level, pos.x, pos.y, pos.z);
                    particle.spawnAsWeatherEffect();
                    listLayerExtra.add(particle);
                }
            }

            particleSpacingDegrees = 360 / particlesPerLayer;

            //TODO: oh god stop the copypasta
            it = listLayerExtra.iterator();
            index = 0;
            while (it.hasNext()) {
                particleCount++;
                PivotingParticle particle = it.next();

                float radAdj = (particle.getEntityId() % particlesPerLayer) / particlesPerLayer;
                radAdj = 0.4F + (radAdj * 0.6F);
                float moar = i * 0.5F;
                if (isPet) moar = 0.5F;
                radiusAdjustedForParticleSize = (radius * (radius / radiusMax) + moar) * radAdj;

                float rot = (particleSpacingDegrees * index) + listLayers.get(i).getRotation();
                particle.setPivotRotPrev(particle.getPivotRot());
                particle.setPivotRot(new Vec3(0, rot, 0));
                particle.setPivotPrev(particle.getPivot());
                particle.setPivot(new Vec3(0, radiusAdjustedForParticleSize, 0));

                particle.prevRotationYaw = particle.rotationYaw;
                particle.rotationYaw += 5F;
                particle.rotationPitch = -30;

                Vec3 posLayer = listLayers.get(i).getPos();
                particle.setPosition(posLayer.x, posLayer.y, posLayer.z);
                particle.setPrevPosX(particle.x);
                particle.setPrevPosY(particle.y);
                particle.setPrevPosZ(particle.z);

                particle.setScale(8 * 0.15F);
                if (isPet) particle.setScale(10F / 3F / 15F * (radius / radiusMax));

                if (particle.getAge() > particle.getTicksFadeInMax()+1) {
                    particle.setAge((int)particle.getTicksFadeInMax()+1);
                }
                particle.setGravity(0);
                //particle.setAlpha(1);
                index++;
            }

            //listLayers.get(i).setPos(new Vector3d(pos.x, pos.y, pos.z));

        }

        //CULog.dbg(particleCount + "");

        wasFirenado = stormObject.isFirenado;
    }

    public void cleanupList(List<PivotingParticle> list, int particlesPerLayer) {
        Iterator<PivotingParticle> it = list.iterator();
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
    }

    @OnlyIn(Dist.CLIENT)
    private PivotingParticle createParticle(ClientLevel world, double x, double y, double z) {
        //ParticleTexFX particle = new ParticleTexFX(world, x, y, z, 0, 0, 0, ParticleRegistry.square16);
        TextureAtlasSprite sprite = ParticleRegistry.cloud256;
        if (stormObject.isFirenado) {
            sprite = ParticleRegistry.cloud256_fire;
        }
        PivotingParticle particle = new PivotingParticle(world, x, y, z, 0, 0, 0, sprite);
        particle.setMaxAge(300);
        particle.setTicksFadeInMax(80);
        //particle.setTicksFadeOutMax(20);
        particle.setParticleSpeed(0, 0, 0);
        particle.setScale(0.1F);
        particle.setScale(5F);
        particle.setScale(15F);
        //particle.setColor(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
        if (!stormObject.isFirenado) {
            float baseBright = 0.3F;
            float randFloat = (world.random.nextFloat() * 0.6F);
            float finalBright = Math.min(1F, baseBright + randFloat);
            particle.setColor(finalBright - 0.2F, finalBright - 0.2F, finalBright - 0.2F);
        } else {
            float baseBright = 0.6F;
            float randFloat = (world.random.nextFloat() * 0.3F);
            float finalBright = Math.min(1F, baseBright + randFloat);
            particle.setColor(finalBright - 0.2F, finalBright - 0.2F, finalBright - 0.2F);
        }
        particle.setGravity(0);
        particle.rotationYaw = world.random.nextFloat() * 360;
        particle.setRenderDistanceCull(renderDistCutoff);
        return particle;
    }

    @OnlyIn(Dist.CLIENT)
    private PivotingParticle createParticleDebris(ClientLevel world, double x, double y, double z) {
        int chance = world.getRandom().nextInt(3);
        TextureAtlasSprite sprite = ParticleRegistry.debris_1;
        if (chance == 1) {
            sprite = ParticleRegistry.debris_2;
        } else if (chance == 2) {
            sprite = ParticleRegistry.debris_3;
        }
        PivotingParticle particle = new PivotingParticle(world, x, y, z, 0, 0, 0, sprite);
        particle.setMaxAge(25000);
        particle.setTicksFadeInMax(80);
        particle.setParticleSpeed(0, 0, 0);

        particle.setFacePlayer(false);
        particle.spinFast = true;
        particle.isTransparent = true;
        particle.rotationYaw = (float)world.getRandom().nextInt(360);
        particle.rotationPitch = (float)world.getRandom().nextInt(360);

        particle.setGravity(0F);
        float brightnessMulti = 1F - (world.getRandom().nextFloat() * 0.5F);
        particle.setColor(1F * brightnessMulti, 1F * brightnessMulti, 1F * brightnessMulti);
        particle.setScale(8 * 0.15F);
        particle.aboveGroundHeight = 0.5D;
        particle.collisionSpeedDampen = false;
        particle.bounceSpeed = 0.03D;
        particle.bounceSpeedAhead = 0.03D;

        particle.setKillOnCollide(false);

        particle.windWeight = 5F;
        particle.setRenderDistanceCull(renderDistCutoff);

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
            listLayers.get(i).getListParticles().stream().forEach(pivotingParticle -> disperseParticleSmoothly(pivotingParticle, true));
            listLayers.get(i).getListParticlesExtra().stream().forEach(pivotingParticle -> disperseParticleSmoothly(pivotingParticle, true));
            listLayers.get(i).getListParticles().clear();
            listLayers.get(i).getListParticlesExtra().clear();
        }
    }

    public void fadeOut() {
        for (int i = 0; i < listLayers.size(); i++) {
            listLayers.get(i).getListParticles().stream().forEach(pivotingParticle -> disperseParticleSmoothly(pivotingParticle, false));
            listLayers.get(i).getListParticlesExtra().stream().forEach(pivotingParticle -> disperseParticleSmoothly(pivotingParticle, false));
            listLayers.get(i).getListParticles().clear();
            listLayers.get(i).getListParticlesExtra().clear();
        }
    }

    public void disperseParticleSmoothly(PivotingParticle pivotingParticle, boolean explode) {
        pivotingParticle.prevRotationYaw = pivotingParticle.rotationYaw;
        pivotingParticle.setPivotPrev(pivotingParticle.getPivot());
        pivotingParticle.setPivotRotPrev(pivotingParticle.getPivotRot());
        Random rand = new Random();
        if (explode) {
            pivotingParticle.setMotionX((rand.nextFloat() - rand.nextFloat()) * 2F);
            pivotingParticle.setMotionZ((rand.nextFloat() - rand.nextFloat()) * 2F);
        } else {
            pivotingParticle.setMotionX((rand.nextFloat() - rand.nextFloat()) * 0.4F);
            pivotingParticle.setMotionZ((rand.nextFloat() - rand.nextFloat()) * 0.4F);
        }
        pivotingParticle.setAge(100);
        pivotingParticle.setMaxAge(200);
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
