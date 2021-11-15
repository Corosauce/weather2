package weather2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class MovingSoundStreamingSource extends AbstractTickableSoundInstance {

	public float cutOffRange = 128;
	public Vec3 realSource = null;
	public boolean lockToPlayer = false;

	public MovingSoundStreamingSource(Vec3 parPos, SoundEvent event, SoundSource category, float parVolume, float parPitch, boolean lockToPlayer) {
		super(event, category);
		this.looping = false;
		this.volume = parVolume;
		this.pitch = parPitch;
		this.realSource = parPos;

		this.lockToPlayer = lockToPlayer;

		tick();
	}

	//constructor for non moving sounds
	public MovingSoundStreamingSource(Vec3 parPos, SoundEvent event, SoundSource category, float parVolume, float parPitch, float parCutOffRange)
	{
		super(event, category);
		this.looping = false;
		this.volume = parVolume;
		this.pitch = parPitch;
		cutOffRange = parCutOffRange;
		realSource = parPos;

		//sync position
		tick();
	}

	public void tick()
	{
		Player entP = Minecraft.getInstance().player;

		if (entP != null) {
			this.x = (float) entP.getX();
			this.y = (float) entP.getY();
			this.z = (float) entP.getZ();
		}

		//if locked to player, don't dynamically adjust volume
		if (!lockToPlayer) {
			float var3 = (float)((cutOffRange - (double)Mth.sqrt((float) getDistanceFrom(realSource, entP.position()))) / cutOffRange);

			if (var3 < 0.0F)
			{
				var3 = 0.0F;
			}

			volume = var3;
		}

	}

	public double getDistanceFrom(Vec3 source, Vec3 targ)
	{
		return source.subtract(targ).length();
	}
}
