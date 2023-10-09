package extendedrenderer.particle.entity;

import com.corosus.coroutil.util.CoroUtilColor;
import com.corosus.coroutil.util.CoroUtilMisc;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.apache.commons.lang3.ArrayUtils;

import java.util.concurrent.ConcurrentHashMap;

public class ParticleTexLeafColor extends ParticleTexFX {
    
	// Save a few stack depth by caching this
	private static BlockColors colors;

	//TODO: see forge note on field to use registry delegate
	//private static final Field _blockColorMap = null;//BlockColors.blockColors;
	//private static final Field _blockColorMap = ObfuscationReflectionHelper.findField(BlockColors.class, "blockColors");
	//private static Map<IRegistryDelegate<Block>, BlockColor> blockColorMap;

	private static ConcurrentHashMap<BlockState, int[]> colorCache = new ConcurrentHashMap<>();
	/*static {
		((SimpleReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(rm -> colorCache.clear());
	}*/

	//only use positives for now
	public float rotationYawMomentum;
	public float rotationPitchMomentum;

	public ParticleTexLeafColor(ClientLevel worldIn, double posXIn, double posYIn,
								double posZIn, double mX, double mY, double mZ,
								TextureAtlasSprite par8Item) {
		super(worldIn, posXIn, posYIn, posZIn, mX, mY, mZ, par8Item);
		
		if (colors == null) {
		    colors = Minecraft.getInstance().getBlockColors();
			/*try {
				blockColorMap = (Map<IRegistryDelegate<Block>, BlockColor>) _blockColorMap.get(colors);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}*/
		}
		
		BlockPos pos = new BlockPos((int)Math.floor(posXIn), (int)Math.floor(posYIn), (int)Math.floor(posZIn));
		BlockState state = worldIn.getBlockState(pos);

	    // top of double plants doesn't have variant property
		//TODO: 1.14 uncomment
		/*if (state.getBlock() instanceof DoublePlantBlock && state.get(DoublePlantBlock.HALF) == EnumBlockHalf.UP) {
		    state = state.with(DoublePlantBlock.name, worldIn.getBlockState(pos.down()).get(DoublePlantBlock.name));
		}*/

		int multiplier = this.colors.getColor(state, this.level, pos, 0);

		//was this supposed to be temp?!
		//colorCache.clear();
		int[] colors = colorCache.get(state);
		if (colors == null) {

//			colors = IntArrays.EMPTY_ARRAY;
		    colors = CoroUtilColor.getColors(state);

		    if (colors.length == 0) {

		    	//if there is no color to use AND theres no multiplier, fallback to good ol green
				if (!hasColor(state) || (multiplier & 0xFFFFFF) == 0xFFFFFF) {
					multiplier = 5811761; //color for vanilla leaf in forest biome
				}

				//add just white that will get colormultiplied
				//TODO: adjusted for LT19
				//colors = new int[] { 0xFFFFFF };
				colors = new int[] { 0x00FF00 };
		    }
		    // Remove duplicate colors from end of array, this will skew the random choice later
			if (colors.length > 1) {
				while (colors[colors.length - 1] == colors[colors.length - 2]) {
					colors = ArrayUtils.remove(colors, colors.length - 1);
				}
			}
		    colorCache.put(state, colors);
		}
		
		// Randomize the color with exponential decrease in likelihood. That is, the first color has a 50% chance, then 25%, etc.
		int randMax = 1 << (colors.length - 1);
		int choice = 32 - Integer.numberOfLeadingZeros(CoroUtilMisc.random.nextInt(randMax));
		int color = colors[choice];

		float mr = ((multiplier >>> 16) & 0xFF) / 255f;
		float mg = ((multiplier >>> 8) & 0xFF) / 255f;
		float mb = (multiplier & 0xFF) / 255f;

		this.rCol *= (float) (color >> 16 & 255) / 255.0F * mr;
		this.gCol *= (float) (color >> 8 & 255) / 255.0F * mg;
		this.bCol *= (float) (color & 255) / 255.0F * mb;
	}

	@Override
	public void tick() {
		super.tick();

		//make leafs catch on the ground and cause them to bounce up and slow a bit for effect
		if (isCollidedVerticallyDownwards && random.nextInt(10) == 0) {
			double speed = Math.sqrt(this.xd * this.xd + this.zd * this.zd);
			if (speed > 0.07) {
				this.yd = 0.02D + random.nextDouble() * 0.03D;
				this.xd *= 0.6D;
				this.zd *= 0.6D;

				rotationYawMomentum = 30;
				rotationPitchMomentum = 30;
			}
		}

		if (rotationYawMomentum > 0) {

			this.rotationYaw += rotationYawMomentum;

			rotationYawMomentum -= 1.5F;

			if (rotationYawMomentum < 0) {
				rotationYawMomentum = 0;
			}
		} else {
			rotationYawMomentum += random.nextDouble() * 30;
		}

		if (rotationPitchMomentum > 0) {

			this.rotationPitch += rotationPitchMomentum;

			rotationPitchMomentum -= 1.5F;

			if (rotationPitchMomentum < 0) {
				rotationPitchMomentum = 0;
			}
		} else {
			rotationPitchMomentum += random.nextDouble() * 30;
		}
	}

	private final boolean hasColor(BlockState state) {
		return false;//blockColorMap.containsKey(state.getBlock().delegate);
	}

}
