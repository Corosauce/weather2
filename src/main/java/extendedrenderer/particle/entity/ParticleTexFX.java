package extendedrenderer.particle.entity;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParticleTexFX extends EntityRotFX {

	public ParticleTexFX(ClientLevel worldIn, double posXIn, double posYIn, double posZIn, double mX, double mY, double mZ, TextureAtlasSprite par8Item)
    {
        super(worldIn, posXIn, posYIn, posZIn, mX, mY-0.5, mZ);
        this.setSprite(par8Item);
        //this.setParticleTexture(Minecraft.getInstance().getItemRenderer().getItemModelMesher().getParticleIcon(Items.IRON_AXE, 0));
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.gravity = 1F;
        this.quadSize = 0.15F;
        this.setLifetime(100);
        this.setCanCollide(false);
    }
	
	public float getParticleGravity() {
		return this.gravity;
	}

    /*@Override
    public int getFXLayer()
    {
        return 1;
    }*/
}
