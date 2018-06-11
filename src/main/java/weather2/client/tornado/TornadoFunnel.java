package weather2.client.tornado;

import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.ParticleTexExtraRender;
import extendedrenderer.shader.Matrix4fe;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
    public int funnelPieces = 1;

    static class FunnelPiece {

        public List<EntityRotFX> listParticles = new ArrayList<>();

        public Vec3d posStart = new Vec3d(0, 0, 0);
        public Vec3d posEnd = new Vec3d(0, 0, 0);

    }

    public TornadoFunnel() {

    }

    public void tickGame() {

        amountPerLayer = 30;
        particleCount = amountPerLayer * 50;
        funnelPieces = 1;

        tickGameTestCreate();
        tickUpdateFunnel();
    }

    private void tickGameTestCreate() {

        EntityPlayer entP = Minecraft.getMinecraft().player;

        Random rand = new Random();

        while (listFunnel.size() < funnelPieces) {
            addPieceToEnd(new FunnelPiece());
        }

        for (FunnelPiece piece : listFunnel) {
            while (piece.listParticles.size() < particleCount) {
                BlockPos pos = new BlockPos(entP);

                //if (entP.getDistanceSq(pos) < 10D * 10D) continue;

                //pos = world.getPrecipitationHeight(pos).add(0, 1, 0);

                ParticleTexExtraRender rain = new ParticleTexExtraRender(entP.world,
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
                rain.setExtraParticlesBaseAmount(1);
                rain.setSeverityOfRainRate(0);
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
                rain.setTicksFadeInMax(20);
                rain.setAlphaF(0);
                rain.setTicksFadeOutMax(20);

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

                piece.listParticles.add(rain);
            }
        }

    }

    private void tickUpdateFunnel() {

        World world = Minecraft.getMinecraft().world;
        EntityPlayer player = Minecraft.getMinecraft().player;

        for (FunnelPiece piece : listFunnel) {

            //temp
            if (piece.posStart.x == 0) {
                piece.posStart = new Vec3d(player.posX, player.posY, player.posZ);
                piece.posEnd = new Vec3d(player.posX + 10, player.posY + 10, player.posZ);
            }

            Iterator<EntityRotFX> it = piece.listParticles.iterator();
            int i = 0;
            while (it.hasNext()) {
                EntityRotFX part = it.next();
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

                    float angleX = (float)Math.atan2(piece.posEnd.x, piece.posStart.x);
                    float angleY = (float)Math.atan2(piece.posEnd.y, piece.posStart.y);
                    float angleZ = (float)Math.atan2(piece.posEnd.z, piece.posStart.z);

                    Matrix4fe matrixFunnel = new Matrix4fe();
                    ////matrixFunnel.rotateX((float)Math.sin(Math.toRadians(((time - 40) * 3) % 360)) * 0.5F);

                    //old testing
                    //matrixFunnel.rotateZ((float)Math.sin(Math.toRadians((time * 3) % 360)) * 0.5F);
                    //matrixFunnel.rotateX((float)Math.sin(Math.toRadians((time2 * 3) % 360)) * 0.5F);
                    //matrixFunnel.rotateY((float)Math.toRadians((time * speed) + (360F / (float)amountPerLayer * (float)rotIndex)));

                    matrixFunnel.rotateZ(angleZ);
                    matrixFunnel.rotateX(angleX);
                    matrixFunnel.rotateY(angleY + (float)Math.toRadians(360F / (float)amountPerLayer * (float)rotIndex));

                    //matrixFunnel.rotateY((float)Math.toRadians((5 * 10) + (360F / (float)amountPerLayer * (float)rotIndex)));

                    //matrixFunnel.translate(new Vector3f((yIndex + 1) * 0.3F, 0, 0));
                    matrixFunnel.translate(new Vector3f(4.4F, 0, 0));
                    //matrixFunnel.translate(new Vector3f(1.45F, 0, 0));
                    //matrixFunnel.translate(new Vector3f(2 + (float) Math.sin(Math.toRadians((time * 3) % 360)), 0, 0));

                    matrixFunnel.translate(new Vector3f(0, (yIndex - (yCount/2)) * 0.95F, 0));

                    Vector3f posParticle = matrixFunnel.getTranslation();

                    part.setPosition(piece.posStart.x + posParticle.x, piece.posStart.y + posParticle.y, piece.posStart.z + posParticle.z);

                    Matrix4fe matrixSelf = new Matrix4fe();

                    //angle it to match funnel shape, done before y
                    //matrixSelf.rotateX((float)Math.toRadians(17.5F));

                    //rotate rest
                    //matrixSelf.rotateY((float)Math.toRadians(90 + (-time * speed) - (360F / (float)amountPerLayer * (float)rotIndex)));
                    //matrixSelf.rotateX((float)Math.sin(Math.toRadians((-time2 * 3) % 360)) * 0.5F);
                    //matrixSelf.rotateZ((float)Math.sin(Math.toRadians((-time * 3) % 360)) * 0.5F);

                    //new position based angle way
                    matrixSelf.rotateY((float)Math.toRadians(90) - angleY - (float)Math.toRadians(360F / (float)amountPerLayer * (float)rotIndex));
                    matrixSelf.rotateX(-angleX);
                    matrixSelf.rotateZ(-angleZ);

                    ////matrixSelf.rotateX((float)Math.sin(Math.toRadians(((-time - 40) * 3) % 360)) * 0.5F);

                    part.rotation.setFromMatrix(matrixSelf.toLWJGLMathMatrix());

                    //part.setAge(40);

                    float r = 1F;
                    float g = 0F;
                    float b = 0F;

                    float stages = 100;

                    r = ((time + i*1) % stages) * (1F / stages);
                    g = ((time2 + i*1) % stages) * (1F / stages);
                    b = ((time3 + i*1) % stages) * (1F / stages);

                    part.setRBGColorF(0F, 0F, 0F);
                    part.setRBGColorF(r, g, b);
                    //part.setParticleTexture(ParticleRegistry.squareGrey);
                }

                i++;
            }
        }
    }

    public void addPieceToEnd(FunnelPiece piece) {
        listFunnel.addLast(piece);
    }

}
