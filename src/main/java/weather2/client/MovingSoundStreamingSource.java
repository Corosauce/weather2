package weather2.client;

import com.corosus.coroutil.util.CULog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import weather2.weathersystem.storm.StormObject;

public class MovingSoundStreamingSource extends AbstractTickableSoundInstance {

	private StormObject storm = null;
	public float cutOffRange = 128;
	public Vec3 realSource = null;
	public boolean lockToPlayer = false;
	private float extraVolumeAdjForDistScale = 1F;

	public MovingSoundStreamingSource(Vec3 parPos, SoundEvent event, SoundSource category, float parVolume, float parPitch, boolean lockToPlayer) {
		super(event, category, SoundInstance.createUnseededRandom());
		this.looping = false;
		this.volume = parVolume;
		this.extraVolumeAdjForDistScale = parVolume;
		this.pitch = parPitch;
		this.realSource = parPos;

		this.lockToPlayer = lockToPlayer;

		tick();
	}

	//constructor for non moving sounds
	public MovingSoundStreamingSource(Vec3 parPos, SoundEvent event, SoundSource category, float parVolume, float parPitch, float parCutOffRange)
	{
		super(event, category, SoundInstance.createUnseededRandom());
		this.looping = false;
		this.volume = parVolume;
		this.extraVolumeAdjForDistScale = parVolume;
		this.pitch = parPitch;
		cutOffRange = parCutOffRange;
		realSource = parPos;

		//sync position
		tick();
	}

	//constructor for moving sounds
	public MovingSoundStreamingSource(StormObject parStorm, SoundEvent event, SoundSource category, float parVolume, float parPitch, float parCutOffRange)
	{
		super(event, category, SoundInstance.createUnseededRandom());
		this.storm = parStorm;
		this.looping = false;
		this.volume = parVolume;
		this.extraVolumeAdjForDistScale = parVolume;
		this.pitch = parPitch;
		cutOffRange = parCutOffRange;

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

		if (storm != null) {
			realSource = this.storm.posGround;
		}

		//if locked to player, don't dynamically adjust volume
		if (!lockToPlayer) {
			double dist = getDistanceFrom(realSource, entP.position());
			if (dist > cutOffRange) {
				volume = 0;
			} else {
				volume = (float) (1F - (dist / cutOffRange)) * extraVolumeAdjForDistScale;
			}
			//CULog.dbg("sound: " + this.location + " vol: " + volume + " cutOffRange: " + cutOffRange + " dist: " + dist);
		}

	}

	public double getDistanceFrom(Vec3 source, Vec3 targ)
	{
		return source.distanceTo(targ);
	}
}
