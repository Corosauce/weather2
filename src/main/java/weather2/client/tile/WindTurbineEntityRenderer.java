package weather2.client.tile;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3f;
import weather2.ClientTickHandler;
import weather2.Weather;
import weather2.WeatherBlocks;
import weather2.blockentity.WindTurbineBlockEntity;
import weather2.blockentity.WindVaneBlockEntity;
import weather2.client.entity.model.WindTurbineModel;
import weather2.client.entity.model.WindVaneModel;
import weather2.weathersystem.WeatherManagerClient;
import weather2.weathersystem.wind.WindManager;

import java.util.Map;
import java.util.Random;

public class WindTurbineEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    private static Map<String, ResourceLocation> resLocMap = Maps.newHashMap();
    private static Map<String, Material> materialMap = Maps.newHashMap();

    public static Material getTEMaterial(final String path) {
        return materialMap.computeIfAbsent(path, m -> createTEMaterial(path));
    }

    private static Material createTEMaterial(final String path) {
        return new Material(TextureAtlas.LOCATION_BLOCKS, getTextureTE(path));
    }

    public static ResourceLocation getTextureTE(String path) {
        return getTexture(String.format("textures/blocks/te/%s.png", path));
    }

    public static ResourceLocation getTexture(String path) {
        return resLocMap.computeIfAbsent(path, k -> getResLoc(path));
    }

    private static ResourceLocation getResLoc(String path) {
        return new ResourceLocation(Weather.MODID, path);
    }

    public static void renderModel(final Material material, final Model model, PoseStack stack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        model.renderToBuffer(stack, buffer.getBuffer(model.renderType(material.texture())), combinedLightIn, combinedOverlayIn, 1, 1, 1, 1);
    }

    private final Block block;
    protected final WindTurbineModel model;

    public WindTurbineEntityRenderer(final BlockEntityRendererProvider.Context context) {
        super();
        this.block = WeatherBlocks.BLOCK_WIND_TURBINE.get();
        this.model = new WindTurbineModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(WindTurbineModel.LAYER_LOCATION));
    }

    @Override
    public void render(T te, float partialTicks, PoseStack stack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        this.model.root().getAllParts().forEach(ModelPart::resetPose);

        //fixes for block
        ModelPart root = this.model.root();
        root.x += 8;
        root.y += 8;
        root.z += 8;
        root.xRot += Math.toRadians(180);
        root.yRot += Math.toRadians(180);
        //te.getLevel().getBrightness(LightLayer.BLOCK, te.getBlockPos().above())

        root.y += 16;

        ModelPart top = this.model.root().getChild("root").getChild("shaft");
        if (top != null) {
            float lerpAngle = (float) Mth.lerp((double)partialTicks, ((WindTurbineBlockEntity) te).smoothAnglePrev, ((WindTurbineBlockEntity) te).smoothAngle);
            float renderAngle = lerpAngle;

            top.yRot = (float) Math.toRadians(renderAngle);
        }

        renderModel(getTEMaterial("wind_turbine"), model, stack, buffer, combinedLightIn, combinedOverlayIn);
    }
}
