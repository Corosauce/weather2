package weather2.client.block;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

import org.lwjgl.opengl.GL11;

public class ModelWindVane extends ModelBase
{
    ModelRenderer bottom;
    ModelRenderer cross;
    ModelRenderer cross2;
    ModelRenderer N;
    ModelRenderer S;
    ModelRenderer E;
    ModelRenderer W;
    ModelRenderer Block2;
    ModelRenderer Block3;
    ModelRenderer top1;
    ModelRenderer chicken;
    ModelRenderer Block4;
    ModelRenderer Block5;
    ModelRenderer arrow;
    ModelRenderer point;
    ModelRenderer feather;
    
    public float scaleX = 1f;	
    public float scaleY = 1f;	
    public float scaleZ = 1f;
    public float scaleItem = 1;
    public float offsetX = 0;
    public float offsetY = 0;
    public float offsetZ = 0;
    public float offsetInvX = 0;
    public float offsetInvY = 0; 

    public ModelWindVane()
    {
        this( 0.0f );
    }

    public ModelWindVane( float par1 )
    {
        bottom = new ModelRenderer( this, 0, 0 );
        bottom.setTextureSize( 64, 32 );
        bottom.addBox( -0.5F, -8F, -0.5F, 1, 16, 1);
        bottom.setRotationPoint( 0F, 16F, 0F );
        cross = new ModelRenderer( this, 7, 3 );
        cross.setTextureSize( 64, 32 );
        cross.addBox( -0.5F, -0.5F, -5F, 1, 1, 10);
        cross.setRotationPoint( 0F, 9.500001F, 0F );
        cross2 = new ModelRenderer( this, 7, 3 );
        cross2.setTextureSize( 64, 32 );
        cross2.addBox( -0.5F, -0.5F, -5F, 1, 1, 10);
        cross2.setRotationPoint( 0F, 9.500001F, 0F );
        N = new ModelRenderer( this, 21, 0 );
        N.setTextureSize( 64, 32 );
        N.addBox( 0F, -1.5F, -1.5F, 0, 3, 3);
        N.setRotationPoint( -2.107342E-07F, 9.500001F, 6.5F );
        S = new ModelRenderer( this, 27, 0 );
        S.setTextureSize( 64, 32 );
        S.addBox( 0F, -1.5F, -1.5F, 0, 3, 3);
        S.setRotationPoint( 1.421085E-14F, 9.5F, -6.5F );
        E = new ModelRenderer( this, 33, 0 );
        E.setTextureSize( 64, 32 );
        E.addBox( 0F, -1.5F, -1.5F, 0, 3, 3);
        E.setRotationPoint( 6.5F, 9.5F, -4.768372E-07F );
        W = new ModelRenderer( this, 39, 0 );
        W.setTextureSize( 64, 32 );
        W.addBox( 0F, -1.5F, -1.5F, 0, 3, 3);
        W.setRotationPoint( -6.5F, 9.5F, -4.768372E-07F );
        Block2 = new ModelRenderer( this, 6, 0 );
        Block2.setTextureSize( 64, 32 );
        Block2.addBox( -1F, -1F, -1F, 2, 2, 2);
        Block2.setRotationPoint( 0F, 11F, 0F );
        Block3 = new ModelRenderer( this, 6, 0 );
        Block3.setTextureSize( 64, 32 );
        Block3.addBox( -1F, -1F, -1F, 2, 2, 2);
        Block3.setRotationPoint( 0F, 11F, 0F );
        top1 = new ModelRenderer( this, 0, 0 );
        top1.setTextureSize( 64, 32 );
        top1.addBox( -0.5F, -3F, -0.5F, 1, 6, 1);
        top1.setRotationPoint( 0F, 5F, 0F );
        chicken = new ModelRenderer( this, 5, 19 );
        chicken.setTextureSize( 64, 32 );
        chicken.addBox( -5F, -5F, 0F, 10, 10, 0);
        chicken.setRotationPoint( 0F, -3F, 0F );
        Block4 = new ModelRenderer( this, 6, 0 );
        Block4.setTextureSize( 64, 32 );
        Block4.addBox( -1F, -1F, -1F, 2, 2, 2);
        Block4.setRotationPoint( 0F, 6F, 0F );
        Block5 = new ModelRenderer( this, 6, 0 );
        Block5.setTextureSize( 64, 32 );
        Block5.addBox( -1F, -1F, -1F, 2, 2, 2);
        Block5.setRotationPoint( 0F, 6F, 0F );
        arrow = new ModelRenderer( this, 25, 16 );
        arrow.setTextureSize( 64, 32 );
        arrow.addBox( -5F, -0.5F, -0.5F, 10, 1, 1);
        arrow.setRotationPoint( 0F, 3F, 0F );
        point = new ModelRenderer( this, 31, 8 );
        point.setTextureSize( 64, 32 );
        point.addBox( 0F, -1.5F, -1.5F, 0, 3, 3);
        point.setRotationPoint( -6.5F, 3F, 0F );
        feather = new ModelRenderer( this, 37, 8 );
        feather.setTextureSize( 64, 32 );
        feather.addBox( 0F, -1.5F, -1.5F, 0, 3, 3);
        feather.setRotationPoint( 6.5F, 3F, 0F );
    }

   public void render(float scale, float topPieceRotation)
   {
	   	//fix backwards base orientation
	   	GL11.glRotatef(180, 0, 1, 0);
	   
        bottom.rotateAngleX = 0F;
        bottom.rotateAngleY = 0F;
        bottom.rotateAngleZ = 0F;
        bottom.renderWithRotation(scale);

        cross.rotateAngleX = 0F;
        cross.rotateAngleY = 0F;
        cross.rotateAngleZ = 0F;
        cross.renderWithRotation(scale);

        cross2.rotateAngleX = 0F;
        cross2.rotateAngleY = -1.570796F;
        cross2.rotateAngleZ = 0F;
        cross2.renderWithRotation(scale);

        N.rotateAngleX = 0F;
        N.rotateAngleY = 0;
        N.rotateAngleZ = 0F;
        N.renderWithRotation(scale);

        S.rotateAngleX = 0F;
        S.rotateAngleY = 4.561942E-08F;
        S.rotateAngleZ = 0F;
        S.renderWithRotation(scale);

        E.rotateAngleX = 0F;
        E.rotateAngleY = -1.570796F;
        E.rotateAngleZ = 0F;
        E.renderWithRotation(scale);

        W.rotateAngleX = 0F;
        W.rotateAngleY = -1.570796F;
        W.rotateAngleZ = 0F;
        W.renderWithRotation(scale);

        //GL11.glRotatef(0, 180, 0, 1);
        GL11.glRotatef(-180, 0, 1, 0);
        
        Block2.rotateAngleX = 0F;
        Block2.rotateAngleY = -0.7853982F;
        Block2.rotateAngleZ = 0F;
        Block2.renderWithRotation(scale);

        Block3.rotateAngleX = -9.134193E-09F;
        Block3.rotateAngleY = -3.362374E-08F;
        Block3.rotateAngleZ = -0.7853982F;
        Block3.renderWithRotation(scale);
        
        GL11.glRotatef(topPieceRotation, 0, 1, 0);
        
        top1.rotateAngleX = 0F;
        top1.rotateAngleY = 0F;
        top1.rotateAngleZ = 0F;
        top1.renderWithRotation(scale);

        chicken.rotateAngleX = 0F;
        chicken.rotateAngleY = 0F;
        chicken.rotateAngleZ = 0F;
        chicken.renderWithRotation(scale);

        Block4.rotateAngleX = 0F;
        Block4.rotateAngleY = -0.7853982F;
        Block4.rotateAngleZ = 0F;
        Block4.renderWithRotation(scale);

        Block5.rotateAngleX = -9.134193E-09F;
        Block5.rotateAngleY = -3.362374E-08F;
        Block5.rotateAngleZ = -0.7853982F;
        Block5.renderWithRotation(scale);

        arrow.rotateAngleX = 0F;
        arrow.rotateAngleY = 0F;
        arrow.rotateAngleZ = 0F;
        arrow.renderWithRotation(scale);

        point.rotateAngleX = 0F;
        point.rotateAngleY = -1.570796F;
        point.rotateAngleZ = 0F;
        point.renderWithRotation(scale);

        feather.rotateAngleX = 0F;
        feather.rotateAngleY = -1.570796F;
        feather.rotateAngleZ = 0F;
        feather.renderWithRotation(scale);

    }

}
