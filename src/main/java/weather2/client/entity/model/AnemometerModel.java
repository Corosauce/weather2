package weather2.client.entity.model;// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import weather2.Weather;

public class AnemometerModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Weather.MODID, "anemometer"), "main");
	private final ModelPart root;

	public AnemometerModel(ModelPart root) {
		this.root = root.getChild("root");
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(1, 1).addBox(-0.5F, -11.0F, -0.5F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition top = base.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.5F, 0.0F));

		PartDefinition arm1 = top.addOrReplaceChild("arm1", CubeListBuilder.create().texOffs(-5, -5).addBox(-0.5F, -0.5F, -8.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cup1 = arm1.addOrReplaceChild("cup1", CubeListBuilder.create().texOffs(1, 1).addBox(0.5F, -12.0F, -8.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(-1, -1).addBox(0.5F, -13.0F, -8.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(1, 1).addBox(0.5F, -12.0F, -6.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(-1, -1).addBox(0.5F, -11.0F, -8.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.5F, 0.0F));

		PartDefinition arm2 = top.addOrReplaceChild("arm2", CubeListBuilder.create().texOffs(-5, -5).addBox(-0.5F, -0.5F, -8.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cup2 = arm2.addOrReplaceChild("cup2", CubeListBuilder.create().texOffs(1, 1).addBox(0.5F, -12.0F, -8.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(-1, -1).addBox(0.5F, -13.0F, -8.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(1, 1).addBox(0.5F, -12.0F, -6.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(-1, -1).addBox(0.5F, -11.0F, -8.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.5F, 0.0F));

		PartDefinition arm3 = top.addOrReplaceChild("arm3", CubeListBuilder.create().texOffs(-5, -5).addBox(-0.5F, -0.5F, -8.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition cup3 = arm3.addOrReplaceChild("cup3", CubeListBuilder.create().texOffs(1, 1).addBox(0.5F, -12.0F, -8.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(-1, -1).addBox(0.5F, -13.0F, -8.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(1, 1).addBox(0.5F, -12.0F, -6.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(-1, -1).addBox(0.5F, -11.0F, -8.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.5F, 0.0F));

		PartDefinition arm4 = top.addOrReplaceChild("arm4", CubeListBuilder.create().texOffs(-5, -5).addBox(-0.5F, -0.5F, -8.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cup4 = arm4.addOrReplaceChild("cup4", CubeListBuilder.create().texOffs(1, 1).addBox(0.5F, -12.0F, -8.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(-1, -1).addBox(0.5F, -13.0F, -8.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(1, 1).addBox(0.5F, -12.0F, -6.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(-1, -1).addBox(0.5F, -11.0F, -8.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.5F, 0.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}