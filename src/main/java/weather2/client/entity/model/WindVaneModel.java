package weather2.client.entity.model;// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import weather2.Weather;
import weather2.blockentity.AnemometerBlockEntity;
import weather2.blockentity.WindVaneBlockEntity;

public class WindVaneModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Weather.MODID, "wind_vane"), "main");
	private final ModelPart root;

	public WindVaneModel(ModelPart root) {
		this.root = root;
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition base = root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(13, 15).addBox(-0.25F, -7.0F, -0.25F, 0.5F, 7.0F, 0.5F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.75F, -0.5F, -0.75F, 1.5F, 0.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition middle = base.addOrReplaceChild("middle", CubeListBuilder.create().texOffs(17, 14).addBox(-0.25F, 0.5F, -0.25F, 0.5F, 3.0F, 0.5F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.75F, 3.25F, -0.75F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.5F, 0.0F));

		PartDefinition cube_r1 = middle.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -0.75F, -0.75F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r2 = middle.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -0.75F, -0.75F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r3 = middle.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -0.75F, -0.75F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition arm1 = middle.addOrReplaceChild("arm1", CubeListBuilder.create().texOffs(4, 13).addBox(-0.25F, -0.25F, -4.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 28).addBox(-0.05F, -1.0F, -6.0F, 0.05F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition top = middle.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -0.75F, -0.75F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F))
				.texOffs(23, 0).addBox(-0.05F, -5.0F, -2.5F, 0.05F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r4 = top.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -0.75F, -0.75F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r5 = top.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -0.75F, -0.75F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r6 = top.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, -0.75F, -0.75F, 1.5F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition toparm1 = top.addOrReplaceChild("toparm1", CubeListBuilder.create().texOffs(12, 0).addBox(-0.25F, -0.25F, -4.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(20, 28).addBox(-0.05F, -1.0F, -6.0F, 0.05F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition toparm2 = top.addOrReplaceChild("toparm2", CubeListBuilder.create().texOffs(4, 9).addBox(-0.25F, -0.25F, -4.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(25, 28).addBox(-0.05F, -1.0F, -6.0F, 0.05F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition arm2 = middle.addOrReplaceChild("arm2", CubeListBuilder.create().texOffs(-1, 11).addBox(-0.25F, -0.25F, -4.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(15, 28).addBox(-0.05F, -1.0F, -6.0F, 0.05F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition arm3 = middle.addOrReplaceChild("arm3", CubeListBuilder.create().texOffs(9, 10).addBox(-0.25F, -0.25F, -4.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(10, 28).addBox(-0.05F, -1.0F, -6.0F, 0.05F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition arm4 = middle.addOrReplaceChild("arm4", CubeListBuilder.create().texOffs(10, 6).addBox(-0.25F, -0.25F, -4.0F, 0.5F, 0.5F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(5, 28).addBox(-0.05F, -1.0F, -6.0F, 0.05F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}