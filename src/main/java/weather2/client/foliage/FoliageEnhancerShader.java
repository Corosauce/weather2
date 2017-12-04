package weather2.client.foliage;

import extendedrenderer.ExtendedRenderer;
import extendedrenderer.foliage.Foliage;
import extendedrenderer.foliage.FoliageLocationData;
import extendedrenderer.foliage.FoliageReplacerBase;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.render.FoliageRenderer;
import extendedrenderer.shader.InstancedMeshFoliage;
import extendedrenderer.shader.MeshBufferManagerFoliage;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class FoliageEnhancerShader {

    public static List<FoliageReplacerBase> listFoliageReplacers = new ArrayList<>();

    public static void init() {
        listFoliageReplacers.add(new FoliageReplacer1TallPlant(Blocks.TALLGRASS.getDefaultState(), ParticleRegistry.tallgrass));
        //listFoliageReplacers.add(new FoliageReplacerMultiTallPlant(Blocks.DOUBLE_PLANT.getDefaultState()));
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
         * double_plant - double height sway
         * tallgrass - sway
         * flowers - sway
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
            Iterator<Map.Entry<BlockPos, FoliageLocationData>> it = ExtendedRenderer.foliageRenderer.lookupPosToFoliage.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, FoliageLocationData> entry = it.next();
                if (!entry.getValue().foliageReplacer.validFoliageSpot(world, entry.getKey().down())) {
                    it.remove();
                    for (Foliage entry2 : entry.getValue().listFoliage) {
                        markMeshDirty(entry2.particleTexture, true);
                        ExtendedRenderer.foliageRenderer.getFoliageForSprite(entry2.particleTexture).remove(entry2);
                    }
                } else if (entityIn.getDistanceSq(entry.getKey()) > radialRange * radialRange) {
                    it.remove();
                    for (Foliage entry2 : entry.getValue().listFoliage) {
                        markMeshDirty(entry2.particleTexture, true);
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
                        if (!ExtendedRenderer.foliageRenderer.lookupPosToFoliage.containsKey(posScan)) {
                            if (entityIn.getDistanceSq(posScan) <= radialRange * radialRange) {
                                for (FoliageReplacerBase replacer : listFoliageReplacers) {
                                    if (replacer.validFoliageSpot(entityIn.world, posScan.down())) {
                                        replacer.addForPos(entityIn.world, posScan);

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

        Foliage.interpPosXThread = entityIn.posX;
        Foliage.interpPosYThread = entityIn.posY;
        Foliage.interpPosZThread = entityIn.posZ;

        //update all vbos that were flagged dirty
        for (Map.Entry<TextureAtlasSprite, List<Foliage>> entry : ExtendedRenderer.foliageRenderer.foliage.entrySet()) {
            InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(entry.getKey());

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

        //ExtendedRenderer.foliageRenderer.processQueue();

        float partialTicks = 1F;

        //set new static camera point for max precision and speed
		/*Foliage.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
		Foliage.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
		Foliage.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;*/

        //MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.tallgrass);
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

		/*for (List<Foliage> listFoliage : ExtendedRenderer.foliageRenderer.lookupPosToFoliage.values()) {
			for (Foliage foliage : listFoliage) {
				foliage.updateQuaternion(entityIn);

				//update vbo2
				foliage.renderForShaderVBO2(mesh, ExtendedRenderer.foliageRenderer.transformation, null, entityIn, partialTicks);
			}
		}*/

		/*System.out.println("foliage: " + ExtendedRenderer.foliageRenderer.lookupPosToFoliage.size() * FoliageClutter.clutterSize);
		System.out.println("vbo thread: mesh.curBufferPosVBO2: " + mesh.curBufferPosVBO2);*/

        mesh.instanceDataBufferVBO2.limit(mesh.curBufferPosVBO2 * mesh.INSTANCE_SIZE_FLOATS_SELDOM);


    }

    public static boolean validFoliageSpot(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() != Material.WATER && world.getBlockState(pos.up()).getMaterial() == Material.WATER;
        //return world.getBlockState(pos).getMaterial() == Material.GRASS/* && world.getBlockState(pos.up()).getBlock() == Blocks.TALLGRASS*//*world.isAirBlock(pos.up())*/;
    }

}
