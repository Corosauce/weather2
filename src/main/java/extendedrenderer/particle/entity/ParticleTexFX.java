package extendedrenderer.particle.entity;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleTexFX extends EntityRotFX {

	public ParticleTexFX(ClientWorld worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item)
    {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY-0.5, mZ);
        this.setSprite(par8Item);
        //this.setParticleTexture(Minecraft.getInstance().getItemRenderer().getItemModelMesher().getParticleIcon(Items.IRON_AXE, 0));
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.particleGravity = 1F;
        this.particleScale = 0.15F;
        this.setMaxAge(100);
        this.setCanCollide(false);
    }
	
	public float getParticleGravity() {
		return this.particleGravity;
	}

    /*@Override
    public int getFXLayer()
    {
        return 1;
    }*/
}
