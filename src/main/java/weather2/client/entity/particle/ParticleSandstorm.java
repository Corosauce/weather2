package weather2.client.entity.particle;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import extendedrenderer.particle.entity.ParticleTexFX;

public class ParticleSandstorm extends ParticleTexFX {

	public double angleToStorm = 0;
	public int heightLayer = 0;
	public double distAdj = 0;
	public boolean lockPosition = false;
	
	public ParticleSandstorm(World worldIn, double posXIn, double posYIn,
			double posZIn, double mX, double mY, double mZ,
			TextureAtlasSprite par8Item) {
		super((ClientWorld) worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
	}

	/**
	 *TODO: prevent rendering of particle if outside visible area of thick sandstorm fog
	 * based on fps changes between performance settings, I suspect game is taking fps hit trying to render particles player cant see anyways due to sandstorm fog effect
	 */

	@Override
	public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float partialTicks) {
		super.renderParticle(buffer, renderInfo, partialTicks);
	}
}
