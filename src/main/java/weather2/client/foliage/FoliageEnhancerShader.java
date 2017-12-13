package weather2.client.foliage;

import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.foliage.Foliage;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.render.FoliageRenderer;
import extendedrenderer.shader.InstancedMeshFoliage;
import extendedrenderer.shader.MeshBufferManagerFoliage;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import weather2.config.ConfigMisc;
import weather2.util.WeatherUtilConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FoliageEnhancerShader implements Runnable {

    public static boolean useThread = true;

    public static List<FoliageReplacerBase> listFoliageReplacers = new ArrayList<>();

    //for position tracking mainly, to be used for all foliage types maybe?
    public static ConcurrentHashMap<BlockPos, FoliageLocationData> lookupPosToFoliage = new ConcurrentHashMap<>();

    /**
     * Called from shaders listener
     */
    public static void setupReplacersAndMeshes() {

        boolean test = false;

        if (!test) {
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.TALLGRASS.getDefaultState())
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/tallgrass")));

            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.YELLOW_FLOWER.getDefaultState())
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/flower_dandelion")).setBiomeColorize(false));

            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.RED_FLOWER.getDefaultState())
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/flower_allium")).setBiomeColorize(false)
                    .setStateSensitive(true)
                    .addComparable(Blocks.RED_FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.ALLIUM));

            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.RED_FLOWER.getDefaultState())
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/flower_blue_orchid")).setBiomeColorize(false)
                    .setStateSensitive(true)
                    .addComparable(Blocks.RED_FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.BLUE_ORCHID));

            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.RED_FLOWER.getDefaultState())
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/flower_rose")).setBiomeColorize(false)
                    .setStateSensitive(true)
                    .addComparable(Blocks.RED_FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.POPPY));

            List<TextureAtlasSprite> sprites = new ArrayList<>();
            sprites.add(getMeshAndSetupSprite("minecraft:blocks/double_plant_grass_bottom"));
            sprites.add(getMeshAndSetupSprite("minecraft:blocks/double_plant_grass_top"));
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DOUBLE_PLANT.getDefaultState(),2).setSprites(sprites)
                    .setStateSensitive(true)
                    .addComparable(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS));

            sprites = new ArrayList<>();
            sprites.add(getMeshAndSetupSprite("minecraft:blocks/double_plant_rose_bottom"));
            sprites.add(getMeshAndSetupSprite("minecraft:blocks/double_plant_rose_top"));
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DOUBLE_PLANT.getDefaultState(),2).setSprites(sprites)
                    .setBiomeColorize(false)
                    .setStateSensitive(true)
                    .addComparable(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.ROSE));


            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.REEDS.getDefaultState(), -1)
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/reeds"))
                    .setBaseMaterial(Material.SAND).setBiomeColorize(true).setRandomizeCoord(false));



            //TODO: support modded blocks or avoid messing with base json files like crop.json, or modded blocks that use it will be invis

            /**
             * WeightedBakedModel:
             * public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
             {
             return this.getRandomModel(rand).getQuads(state, side, rand);
             }

             ^ relating to the random model variants that are based on position, eg dirt, maybe handy in future
             */

            for (int i = 0; i < 8; i++) {
                int temp = i;
                if (temp >= 4) temp = 3;
                listFoliageReplacers.add(new FoliageReplacerCross(Blocks.WHEAT.getDefaultState())
                        .setBaseMaterial(Material.GROUND)
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/beetroots_stage_" + temp))
                        .setRandomizeCoord(false)
                        .setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, i));
            }



            /*for (int i = 0; i < 4; i++) {
                listFoliageReplacers.add(new FoliageReplacerCross(Blocks.BEETROOTS.getDefaultState())
                        .setBaseMaterial(Material.GROUND)
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/beetroots_stage_" + i))
                        .setRandomizeCoord(false)
                        .setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, i));
            }*/

            //if (true) return;

            //ugh
            HashMap<Integer, Integer> lookupStateToModel = new HashMap<>();
            lookupStateToModel.put(0, 0);
            lookupStateToModel.put(1, 0);
            lookupStateToModel.put(2, 1);
            lookupStateToModel.put(3, 1);
            lookupStateToModel.put(4, 2);
            lookupStateToModel.put(5, 2);
            lookupStateToModel.put(6, 2);
            lookupStateToModel.put(7, 3);

            for (Map.Entry<Integer, Integer> entrySet : lookupStateToModel.entrySet()) {
                listFoliageReplacers.add(new FoliageReplacerCross(Blocks.CARROTS.getDefaultState())
                        .setBaseMaterial(Material.GROUND)
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/carrots_stage_" + entrySet.getValue()))
                        .setRandomizeCoord(false)
                        .setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, entrySet.getKey()));
            }

            for (Map.Entry<Integer, Integer> entrySet : lookupStateToModel.entrySet()) {
                listFoliageReplacers.add(new FoliageReplacerCross(Blocks.POTATOES.getDefaultState())
                        .setBaseMaterial(Material.GROUND)
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/potatoes_stage_" + entrySet.getValue()))
                        .setRandomizeCoord(false)
                        .setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, entrySet.getKey()));
            }
        } else {
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.TALLGRASS.getDefaultState())
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/tallgrass")));
            /*listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DIAMOND_BLOCK.getDefaultState())
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/tallgrass")));*/

            if (true) return;

            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.YELLOW_FLOWER.getDefaultState())
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/flower_dandelion")).setBiomeColorize(false));
            List<TextureAtlasSprite> sprites = new ArrayList<>();
            sprites.add(getMeshAndSetupSprite("minecraft:blocks/double_plant_grass_bottom"));
            sprites.add(getMeshAndSetupSprite("minecraft:blocks/double_plant_grass_top"));
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DOUBLE_PLANT.getDefaultState(),2).setSprites(sprites));
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.REEDS.getDefaultState(), -1)
                    .setSprite(getMeshAndSetupSprite("minecraft:blocks/reeds"))
                    .setBaseMaterial(Material.SAND).setBiomeColorize(true).setRandomizeCoord(false));



            //TODO: support modded blocks or avoid messing with base json files like crop.json, or modded blocks that use it will be invis

            /**
             * WeightedBakedModel:
             * public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
             {
             return this.getRandomModel(rand).getQuads(state, side, rand);
             }

             ^ relating to the random model variants that are based on position, eg dirt, maybe handy in future
             */

            for (int i = 0; i < 8; i++) {
                /*listFoliageReplacers.add(new FoliageReplacerCross(Blocks.WHEAT.getDefaultState())
                        .setBaseMaterial(Material.GROUND)
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/wheat_stage_" + i))
                        .setRandomizeCoord(false)
                        .setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, i));*/
                listFoliageReplacers.add(new FoliageReplacerCross(Blocks.TALLGRASS.getDefaultState())
                        /*.setBaseMaterial(Material.GROUND)*/
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/wheat_stage_" + i))
                        .setRandomizeCoord(false)/*
                        .setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, i)*/);
            }



            for (int i = 0; i < 4; i++) {
                /*listFoliageReplacers.add(new FoliageReplacerCross(Blocks.BEETROOTS.getDefaultState())
                        .setBaseMaterial(Material.GROUND)
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/beetroots_stage_" + i))
                        .setRandomizeCoord(false)
                        .setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, i));*/
                listFoliageReplacers.add(new FoliageReplacerCross(Blocks.TALLGRASS.getDefaultState())
                        /*.setBaseMaterial(Material.GROUND)*/
                                .setSprite(getMeshAndSetupSprite("minecraft:blocks/beetroots_stage_" + i))
                                .setRandomizeCoord(false)
                        /*.setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, i)*/);
            }

            //ugh
            HashMap<Integer, Integer> lookupStateToModel = new HashMap<>();
            lookupStateToModel.put(0, 0);
            lookupStateToModel.put(1, 0);
            lookupStateToModel.put(2, 1);
            lookupStateToModel.put(3, 1);
            lookupStateToModel.put(4, 2);
            lookupStateToModel.put(5, 2);
            lookupStateToModel.put(6, 2);
            lookupStateToModel.put(7, 3);

            for (Map.Entry<Integer, Integer> entrySet : lookupStateToModel.entrySet()) {
                /*listFoliageReplacers.add(new FoliageReplacerCross(Blocks.CARROTS.getDefaultState())
                        .setBaseMaterial(Material.GROUND)
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/carrots_stage_" + entrySet.getValue()))
                        *//*.setRandomizeCoord(false)
                        .setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, entrySet.getKey())*//*);*/

                listFoliageReplacers.add(new FoliageReplacerCross(Blocks.TALLGRASS.getDefaultState())
                        /*.setBaseMaterial(Material.GROUND)*/
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/carrots_stage_" + entrySet.getValue())));
            }

            for (Map.Entry<Integer, Integer> entrySet : lookupStateToModel.entrySet()) {
                /*listFoliageReplacers.add(new FoliageReplacerCross(Blocks.POTATOES.getDefaultState())
                        .setBaseMaterial(Material.GROUND)
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/potatoes_stage_" + entrySet.getValue()))
                        *//*.setRandomizeCoord(false)
                        .setStateSensitive(true)
                        .addComparable(BlockCrops.AGE, entrySet.getKey())*//*);*/

                listFoliageReplacers.add(new FoliageReplacerCross(Blocks.TALLGRASS.getDefaultState())
                        /*.setBaseMaterial(Material.GROUND)*/
                        .setSprite(getMeshAndSetupSprite("minecraft:blocks/potatoes_stage_" + entrySet.getValue())));
            }
        }

        System.out.println(MeshBufferManagerFoliage.lookupParticleToMesh.size());

    }

    /**
     * Called from shaders listener
     */
    public static void shadersReset() {
        //TODO: for resource and shader system resets
        listFoliageReplacers.clear();
        lookupPosToFoliage.clear();
    }

    public static TextureAtlasSprite getMeshAndSetupSprite(String spriteLoc) {
        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite sprite = map.getAtlasSprite(spriteLoc);
        MeshBufferManagerFoliage.setupMeshIfMissing(sprite);
        return sprite;
    }

    @Override
    public void run() {
        if (useThread) {
            while (true) {
                try {
                    boolean gotLock = tickClientThreaded();
                    if (gotLock) {
                        Thread.sleep(ConfigMisc.Thread_Particle_Process_Delay);
                    } else {
                        //if we didnt get the lock, no work was done, aggressively retry until we get it
                        Thread.sleep(20);
                    }

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    //run from our newly created thread
    public static boolean tickClientThreaded() {
        Minecraft mc = FMLClientHandler.instance().getClient();

        if (mc.world != null && mc.player != null && WeatherUtilConfig.listDimensionsWindEffects.contains(mc.world.provider.getDimension())) {
            return tickThreaded();
        } else {
            return true;
        }
    }

    public static boolean tickThreaded() {
        if (ExtendedRenderer.foliageRenderer.lockVBO2.tryLock()) {
            //System.out.println("vbo thread: lock got");
            try {
                profileForFoliageShader();
            } finally {
                ExtendedRenderer.foliageRenderer.lockVBO2.unlock();
                return true;
            }
        } else {
            return false;
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

        //prevent circular distance check position from changing as thread runs
        double centerX = entityIn.posX;
        double centerY = entityIn.posY;
        double centerZ = entityIn.posZ;

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
                } else if (entry.getKey().distanceSq(centerX, centerY, centerZ)/*entityIn.getDistanceSq(entry.getKey())*/ > radialRange * radialRange) {
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
                            if (posScan.distanceSq(centerX, centerY, centerZ)/*entityIn.getDistanceSq(posScan)*/ <= radialRange * radialRange) {

                                //TEMP!!!
                                /*List<FoliageReplacerBase> listFoliageReplacers2 = listFoliageReplacers;
                                Collections.shuffle(listFoliageReplacers2);*/

                                boolean tryAll = true;

                                if (tryAll) {
                                    for (FoliageReplacerBase replacer : listFoliageReplacers) {

                                        if (replacer.validFoliageSpot(entityIn.world, posScan.down())) {
                                            //System.out.println("add");
                                            replacer.addForPos(entityIn.world, posScan);
                                            replacer.markMeshesDirty();


                                            //avoid more things trying to add foliage to spot?
                                            //break;
                                        }
                                    }
                                } else {
                                    int randTry = rand.nextInt(listFoliageReplacers.size());
                                    FoliageReplacerBase replacer = listFoliageReplacers.get(randTry);
                                    if (replacer.validFoliageSpot(entityIn.world, posScan.down())) {
                                        //System.out.println("add");
                                        replacer.addForPos(entityIn.world, posScan);
                                        replacer.markMeshesDirty();
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

            if (mesh.dirtyVBO2Flag) {
                mesh.interpPosXThread = entityIn.posX;
                mesh.interpPosYThread = entityIn.posY;
                mesh.interpPosZThread = entityIn.posZ;

                updateVBO2Threaded(entry.getKey());
            }
        }
    }

    public static void markMeshDirty(TextureAtlasSprite sprite, boolean flag) {
        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);

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
        if (mesh == null) {
            System.out.println("MESH NULL HERE, WHY???");
            return;
        }

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

        if (FoliageRenderer.testStaticLimit) {
            mesh.instanceDataBufferVBO2.limit(30000 * mesh.INSTANCE_SIZE_FLOATS_SELDOM);
        } else {
            mesh.instanceDataBufferVBO2.limit(mesh.curBufferPosVBO2 * mesh.INSTANCE_SIZE_FLOATS_SELDOM);
        }

        //mesh.instanceDataBufferVBO2.limit(mesh.curBufferPosVBO2 * mesh.INSTANCE_SIZE_FLOATS_SELDOM);


    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos) {
        addForPos(replacer, height, pos, true, true);
    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos, boolean randPosVar, boolean biomeColorize) {

        World world = Minecraft.getMinecraft().world;

        Random rand = new Random();
        //for (BlockPos pos : foliageQueueAdd) {
        IBlockState state = world.getBlockState(pos.down());
        //List<Foliage> listClutter = new ArrayList<>();
        FoliageLocationData data = new FoliageLocationData(replacer);
        //for (int heightIndex = 0; heightIndex < 2; heightIndex++) {

        int heightIndex;

        float variance = 0.4F;
        float randX = randPosVar ? (rand.nextFloat() - rand.nextFloat()) * variance : 0;
        float randZ = randPosVar ? (rand.nextFloat() - rand.nextFloat()) * variance : 0;

        int clutterSize = 2;
        int meshesPerLayer = 2;

        if (replacer instanceof FoliageReplacerCross) {
            clutterSize = 2 * height;
        }

        for (int i = 0; i < clutterSize; i++) {
                    /*if (i >= 2) {
                        heightIndex = 1;
                    }*/
            heightIndex = i / meshesPerLayer;

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

            if (biomeColorize) {
                int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(world.getBlockState(pos), world, pos/*.down()*/, 0);
                foliage.particleRed = (float) (color >> 16 & 255) / 255.0F;
                foliage.particleGreen = (float) (color >> 8 & 255) / 255.0F;
                foliage.particleBlue = (float) (color & 255) / 255.0F;
            }

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
