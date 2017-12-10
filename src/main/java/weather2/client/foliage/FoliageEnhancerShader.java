package weather2.client.foliage;

import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.foliage.Foliage;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.render.FoliageRenderer;
import extendedrenderer.shader.InstancedMeshFoliage;
import extendedrenderer.shader.MeshBufferManagerFoliage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FoliageEnhancerShader {

    public static List<FoliageReplacerBase> listFoliageReplacers = new ArrayList<>();

    //for position tracking mainly, to be used for all foliage types maybe?
    public static ConcurrentHashMap<BlockPos, FoliageLocationData> lookupPosToFoliage = new ConcurrentHashMap<>();

    /**
     * Called from shaders listener
     */
    public static void setupReplacersAndMeshes() {
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.TALLGRASS.getDefaultState())
                .setSprite(getMeshAndSetupSprite("minecraft:blocks/tallgrass")));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.YELLOW_FLOWER.getDefaultState())
                .setSprite(getMeshAndSetupSprite("minecraft:blocks/flower_dandelion")));
        List<TextureAtlasSprite> sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite("minecraft:blocks/double_plant_grass_bottom"));
        sprites.add(getMeshAndSetupSprite("minecraft:blocks/double_plant_grass_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DOUBLE_PLANT.getDefaultState(),2).setSprites(sprites));
    }

    /**
     * Called from shaders listener
     */
    public static void shadersReset() {
        //TODO: for resource and shader system resets
    }

    public static TextureAtlasSprite getMeshAndSetupSprite(String spriteLoc) {
        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite sprite = map.getAtlasSprite(spriteLoc);
        MeshBufferManagerFoliage.setupMeshIfMissing(sprite);
        return sprite;
    }

    public static void tickThreaded() {
        if (ExtendedRenderer.foliageRenderer.lockVBO2.tryLock()) {
            //System.out.println("vbo thread: lock got");
            try {
                profileForFoliageShader();
            } finally {
                ExtendedRenderer.foliageRenderer.lockVBO2.unlock();
            }
        } else {
            //System.out.println("vbo thread: cant lock");
        }
    }

    public static void profileForFoliageShader() {

        /**
         *
         * TODO: if thread couldnt get lock, dont wait the full Thread_Particle_Process_Delay for the next tick
         *
         * double_plant - double height sway
         * tallgrass - sway
         * flowers - sway
         * crops - sway
         * - all wheat stages
         * -- how to keep them rendering far? eg https://i.imgur.com/WltFr7x.png
         * vines - variable negative height sway
         * - not cross stitched, specific angles
         * -- shader should be able to handle it fine as is
         *
         * extra ideas:
         * regular grass with my own texture
         * tree leafs with cross stitch render
         *
         * mod support:
         * - plant material blocks
         * - find and override in our resource pack
         *
         */

        World world = Minecraft.getMinecraft().world;
        Entity entityIn = Minecraft.getMinecraft().player;
        BlockPos pos = entityIn.getPosition();

        boolean add = true;
        boolean trim = true;

        int radialRange = FoliageRenderer.radialRange;

        int xzRange = radialRange;
        int yRange = radialRange;
        Random rand = new Random();

        //boolean dirtyVBO2 = false;

        //cleanup list
        if (trim) {
            Iterator<Map.Entry<BlockPos, FoliageLocationData>> it = lookupPosToFoliage.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, FoliageLocationData> entry = it.next();
                if (!entry.getValue().foliageReplacer.validFoliageSpot(world, entry.getKey().down())) {
                    //System.out.println("remove");
                    it.remove();
                    //TODO: consider relocating Foliage list to within foliagereplacer, as there is some redundancy happening here
                    for (Foliage entry2 : entry.getValue().listFoliage) {
                        //markMeshDirty(entry2.particleTexture, true);
                        entry.getValue().foliageReplacer.markMeshesDirty();
                        ExtendedRenderer.foliageRenderer.getFoliageForSprite(entry2.particleTexture).remove(entry2);
                    }
                } else if (entityIn.getDistanceSq(entry.getKey()) > radialRange * radialRange) {
                    //System.out.println("remove");
                    it.remove();
                    for (Foliage entry2 : entry.getValue().listFoliage) {
                        //markMeshDirty(entry2.particleTexture, true);
                        entry.getValue().foliageReplacer.markMeshesDirty();
                        ExtendedRenderer.foliageRenderer.getFoliageForSprite(entry2.particleTexture).remove(entry2);
                    }
                }
            }
        }

        //scan and add foliage around player
        if (add) {
            for (int x = -xzRange; x <= xzRange; x++) {
                for (int z = -xzRange; z <= xzRange; z++) {
                    for (int y = -yRange; y <= yRange; y++) {
                        BlockPos posScan = pos.add(x, y, z);
                        //IBlockState state = entityIn.world.getBlockState(posScan.down());
                        if (!lookupPosToFoliage.containsKey(posScan)) {
                            if (entityIn.getDistanceSq(posScan) <= radialRange * radialRange) {
                                for (FoliageReplacerBase replacer : listFoliageReplacers) {
                                    if (replacer.validFoliageSpot(entityIn.world, posScan.down())) {
                                        //System.out.println("add");
                                        replacer.addForPos(entityIn.world, posScan);
                                        replacer.markMeshesDirty();


                                        //avoid more things trying to add foliage to spot?
                                        break;
                                    }
                                }
                            }
                            /*if (validFoliageSpot(entityIn.world, posScan.down())) {
                                if (entityIn.getDistanceSq(posScan) <= radialRange * radialRange) {

                                    TextureAtlasSprite sprite = ParticleRegistry.tallgrass;
                                    ExtendedRenderer.foliageRenderer.addForPos(sprite, posScan);
                                    markMeshDirty(sprite, true);

                                }
                            }*/
                        } else {

                        }
                    }
                }
            }
        }

        /*ExtendedRenderer.foliageRenderer.addForPosSeaweed(posScan);
        for (TextureAtlasSprite sprite : ParticleRegistry.listSeaweed) {
            markMeshDirty(sprite, true);
        }*/

        //update all vbos that were flagged dirty
        for (Map.Entry<TextureAtlasSprite, List<Foliage>> entry : ExtendedRenderer.foliageRenderer.foliage.entrySet()) {
            InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(entry.getKey());

            mesh.interpPosXThread = entityIn.posX;
            mesh.interpPosYThread = entityIn.posY;
            mesh.interpPosZThread = entityIn.posZ;

            if (mesh.dirtyVBO2Flag) {
                updateVBO2Threaded(entry.getKey());
            }
        }
    }

    public static void markMeshDirty(TextureAtlasSprite sprite, boolean flag) {
        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);

        //TODO: this is a patch, setup init better
        if (mesh == null) {
            MeshBufferManagerFoliage.setupMeshIfMissing(sprite);
            mesh = MeshBufferManagerFoliage.getMesh(sprite);
        }

        if (mesh != null) {
            mesh.dirtyVBO2Flag = flag;
        } else {
            System.out.println("MESH NULL HERE, FIX INIT ORDER");
        }
    }

    public static void updateVBO2Threaded(TextureAtlasSprite sprite) {

        Minecraft mc = Minecraft.getMinecraft();
        Entity entityIn = mc.getRenderViewEntity();

        float partialTicks = 1F;

        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);
        if (mesh == null) return;

        mesh.curBufferPosVBO2 = 0;
        mesh.instanceDataBufferVBO2.clear();

        //System.out.println("vbo 2 update");

        for (Foliage foliage : ExtendedRenderer.foliageRenderer.getFoliageForSprite(sprite)) {
            foliage.updateQuaternion(entityIn);

            //update vbo2
            foliage.renderForShaderVBO2(mesh, ExtendedRenderer.foliageRenderer.transformation, null, entityIn, partialTicks);
        }

		/*System.out.println("foliage: " + ExtendedRenderer.foliageRenderer.lookupPosToFoliage.size() * FoliageClutter.clutterSize);
		System.out.println("vbo thread: mesh.curBufferPosVBO2: " + mesh.curBufferPosVBO2);*/

        mesh.instanceDataBufferVBO2.limit(mesh.curBufferPosVBO2 * mesh.INSTANCE_SIZE_FLOATS_SELDOM);


    }

    public static void addForPos(FoliageReplacerBase replacer, BlockPos pos) {

        World world = Minecraft.getMinecraft().world;

        Random rand = new Random();
        //for (BlockPos pos : foliageQueueAdd) {
        IBlockState state = world.getBlockState(pos.down());
        //List<Foliage> listClutter = new ArrayList<>();
        FoliageLocationData data = new FoliageLocationData(replacer);
        //for (int heightIndex = 0; heightIndex < 2; heightIndex++) {

        int heightIndex;

        float variance = 0.4F;
        float randX = (rand.nextFloat() - rand.nextFloat()) * variance;
        float randZ = (rand.nextFloat() - rand.nextFloat()) * variance;

        int clutterSize = 2;

        if (replacer instanceof FoliageReplacerCross) {
            clutterSize = 2 * ((FoliageReplacerCross) replacer).expectedHeight;
        }

        for (int i = 0; i < clutterSize; i++) {
                    /*if (i >= 2) {
                        heightIndex = 1;
                    }*/
            heightIndex = i / 2;

            TextureAtlasSprite sprite = replacer.sprites.get(0);
            if (replacer instanceof FoliageReplacerCross) {
                if (heightIndex < replacer.sprites.size()) {
                    sprite = replacer.sprites.get(heightIndex);
                }
            }

            Foliage foliage = new Foliage(sprite);
            foliage.setPosition(pos.add(0, 0, 0));
            foliage.posY += 0.0F;
            foliage.prevPosY = foliage.posY;
            foliage.heightIndex = heightIndex;
                                        /*foliage.posX += 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.8F;
                                        foliage.prevPosX = foliage.posX;
                                        foliage.posZ += 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.8F;
                                        foliage.prevPosZ = foliage.posZ;*/
            foliage.posX += 0.5F + randX;
            foliage.prevPosX = foliage.posX;
            foliage.posZ += 0.5F + randZ;
            foliage.prevPosZ = foliage.posZ;
            foliage.rotationYaw = 0;
            //foliage.rotationYaw = 90;
            foliage.rotationYaw = world.rand.nextInt(360);

            //cross sectionize for each second one
                    /*if ((i+1) % 2 == 0) {
                        foliage.rotationYaw = (listClutter.get(0).rotationYaw + 90) % 360;
                    }*/

            //temp?
            foliage.rotationYaw = 45;
            if ((i+1) % 2 == 0) {
                foliage.rotationYaw += 90;
            }

            //for seaweed render
            foliage.rotationYaw = 0;
            if ((i+1) % 2 == 0) {
                //use as a marker for GLSL
                foliage.rotationYaw = 1;
            }

            //foliage.rotationPitch = rand.nextInt(90) - 45;
            foliage.particleScale /= 0.2;

            int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, pos.down(), 0);
            foliage.particleRed = (float) (color >> 16 & 255) / 255.0F;
            foliage.particleGreen = (float) (color >> 8 & 255) / 255.0F;
            foliage.particleBlue = (float) (color & 255) / 255.0F;

            /*foliage.particleRed -= 0.2F;
            foliage.particleGreen -= 0.2F;
            foliage.particleBlue = 1F;*/

                    /*foliage.particleRed = rand.nextFloat();
                    foliage.particleGreen = rand.nextFloat();
                    foliage.particleBlue = rand.nextFloat();*/

            //debug
                    /*if (heightIndex == 0) {
                        foliage.particleRed = 1F;
                    } else if (heightIndex == 1) {
                        foliage.particleGreen = 1F;
                    } else if (heightIndex == 2) {
                        foliage.particleBlue = 1F;
                    }*/

            foliage.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer;

            //temp
            if ((i+1) % 2 == 0) {
                //foliage.particleGreen = 0;
            }

            data.listFoliage.add(foliage);
            ExtendedRenderer.foliageRenderer.getFoliageForSprite(sprite).add(foliage);

        }

        lookupPosToFoliage.put(pos, data);

    }

    public void addForPosSeaweed(BlockPos pos) {

        World world = Minecraft.getMinecraft().world;

        Random rand = new Random();
        //for (BlockPos pos : foliageQueueAdd) {
        IBlockState state = world.getBlockState(pos.down());
        List<Foliage> listClutter = new ArrayList<>();
        //for (int heightIndex = 0; heightIndex < 2; heightIndex++) {

        int heightIndex = 0;

        float variance = 0.4F;
        float randX = (rand.nextFloat() - rand.nextFloat()) * variance;
        float randZ = (rand.nextFloat() - rand.nextFloat()) * variance;

        int clutterSize = 14;

        clutterSize = rand.nextInt(7) * 2;

        for (int i = 0; i < clutterSize; i++) {
                    /*if (i >= 2) {
                        heightIndex = 1;
                    }*/
            heightIndex = i / 2;
            TextureAtlasSprite sprite = ParticleRegistry.listSeaweed.get(heightIndex);
            Foliage foliage = new Foliage(sprite);
            foliage.setPosition(pos.add(0, 0, 0));
            foliage.posY += 0.0F;
            foliage.prevPosY = foliage.posY;
            foliage.heightIndex = heightIndex;
                                        /*foliage.posX += 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.8F;
                                        foliage.prevPosX = foliage.posX;
                                        foliage.posZ += 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.8F;
                                        foliage.prevPosZ = foliage.posZ;*/
            foliage.posX += 0.5F + randX;
            foliage.prevPosX = foliage.posX;
            foliage.posZ += 0.5F + randZ;
            foliage.prevPosZ = foliage.posZ;
            foliage.rotationYaw = 0;
            //foliage.rotationYaw = 90;
            foliage.rotationYaw = world.rand.nextInt(360);

            //cross sectionize for each second one
                    /*if ((i+1) % 2 == 0) {
                        foliage.rotationYaw = (listClutter.get(0).rotationYaw + 90) % 360;
                    }*/

            //temp?
            foliage.rotationYaw = 45;
            if ((i+1) % 2 == 0) {
                foliage.rotationYaw += 90;
            }

            //for seaweed render
            foliage.rotationYaw = 0;
            if ((i+1) % 2 == 0) {
                //use as a marker for GLSL
                foliage.rotationYaw = 1;
            }

            //foliage.rotationPitch = rand.nextInt(90) - 45;
            foliage.particleScale /= 0.2;

            int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, pos.down(), 0);
            foliage.particleRed = (float) (color >> 16 & 255) / 255.0F;
            foliage.particleGreen = (float) (color >> 8 & 255) / 255.0F;
            foliage.particleBlue = (float) (color & 255) / 255.0F;

            foliage.particleRed = 1F;
            foliage.particleGreen = 1F;
            foliage.particleBlue = 1F;

                    /*foliage.particleRed = rand.nextFloat();
                    foliage.particleGreen = rand.nextFloat();
                    foliage.particleBlue = rand.nextFloat();*/

            //debug
                    /*if (heightIndex == 0) {
                        foliage.particleRed = 1F;
                    } else if (heightIndex == 1) {
                        foliage.particleGreen = 1F;
                    } else if (heightIndex == 2) {
                        foliage.particleBlue = 1F;
                    }*/

            foliage.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer;

            //temp
            if ((i+1) % 2 == 0) {
                //foliage.particleGreen = 0;
            }

            listClutter.add(foliage);
            ExtendedRenderer.foliageRenderer.getFoliageForSprite(sprite).add(foliage);

        }

        //lookupPosToFoliage.put(pos, listClutter);

    }

}
