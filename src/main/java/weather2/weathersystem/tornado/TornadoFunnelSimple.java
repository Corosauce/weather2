package weather2.weathersystem.tornado;

import com.mojang.math.Vector3d;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleTexFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TornadoFunnelSimple {

    private ActiveTornadoConfig config;

    public Vector3d pos = new Vector3d(0, 0, 0);

    public List<List<EntityRotFX>> listLayers = new ArrayList<>();

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

        //temp stress testing
        config.setHeight(config.getHeight() + adjHeight);
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
        }

        Player entP = Minecraft.getInstance().player;
        ClientLevel level = (ClientLevel) entP.level;
        int layers = (int) (config.getHeight() / heightPerLayer);

        //cleanup layers beyond current size
        //while (listLayers.size() > layers) {
        for (int i = layers; i < listLayers.size(); i++) {
            List<EntityRotFX> listLayer = listLayers.get(i);
            Iterator<EntityRotFX> it = listLayer.iterator();
            while (it.hasNext()) {
                EntityRotFX particle = it.next();
                it.remove();
                particle.remove();
            }
        }

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

            List<EntityRotFX> listLayer = listLayers.get(i);

            float radius = config.getRadiusOfBase() + (config.getRadiusIncreasePerLayer() * (i+1));
            float circumference = radius * 2 * Mth.PI;
            float particleSpaceOccupy = 0.5F;
            float particlesPerLayer = (float) Math.floor(circumference / particleSpaceOccupy);

            Iterator<EntityRotFX> it = listLayer.iterator();
            float index = 0;
            while (it.hasNext()) {
                EntityRotFX particle = it.next();
                if (!particle.isAlive() || index >= particlesPerLayer) {
                    it.remove();
                    particle.remove();
                } else {
                    index++;
                }
            }

            while (listLayer.size() < particlesPerLayer) {
                EntityRotFX particle = createParticle(level, pos.x, pos.y, pos.z);
                Minecraft.getInstance().particleEngine.add(particle);
                listLayer.add(particle);
            }

            it = listLayer.iterator();
            index = 0;
            while (it.hasNext()) {
                EntityRotFX particle = it.next();
                float particleSpacingRadians = (float) Math.toRadians(360 / particlesPerLayer);
                //particleSpacingRadians = 5;
                float spinAdj = (float) (Math.toRadians(level.getGameTime() % 360) * 4.22F);
                float relX = -Mth.sin((particleSpacingRadians * index) + spinAdj) * radius;
                float relY = (float)(heightPerLayer * (i+1));
                float relZ = Mth.cos((particleSpacingRadians * index) + spinAdj) * radius;
                particle.setPosition(pos.x + relX, pos.y + relY, pos.z + relZ);
                particle.setPrevPosX(particle.x);
                particle.setPrevPosY(particle.y);
                particle.setPrevPosZ(particle.z);
                particle.setAge(0);
                index++;
            }

        }
    }

    private EntityRotFX createParticle(ClientLevel world, double x, double y, double z) {
        //ParticleTexFX particle = new ParticleTexFX(world, x, y, z, 0, 0, 0, ParticleRegistry.square16);
        ParticleTexFX particle = new ParticleTexFX(world, x, y, z, 0, 0, 0, ParticleRegistry.cloud256);
        particle.setMaxAge(250);
        //particle.setTicksFadeInMax(20);
        //particle.setTicksFadeOutMax(20);
        particle.setParticleSpeed(0, 0, 0);
        particle.setScale(0.1F);
        particle.setScale(5F);
        particle.setColor(world.random.nextFloat(), world.random.nextFloat(), world.random.nextFloat());
        particle.setGravity(0);
        particle.rotationYaw = world.random.nextFloat() * 360;
        return particle;
    }
}
