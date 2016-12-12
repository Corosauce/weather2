package weather2.item;

import CoroUtil.util.Vec3;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorSandstorm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.ClientTickHandler;
import weather2.CommonProxy;
import weather2.client.SceneEnhancer;
import weather2.client.entity.particle.ParticleSandstorm;
import weather2.util.WeatherUtilBlock;

import javax.annotation.Nullable;
import java.util.Random;

public class ItemPocketSand extends Item {

    @SideOnly(Side.CLIENT)
    public ParticleBehaviorSandstorm particleBehavior;

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player, EnumHand hand) {
        if (!player.worldObj.isRemote) {
            if (!(player).capabilities.isCreativeMode)
            {
                if (itemStackIn.stackSize > 0) {
                    --itemStackIn.stackSize;
                }
            }
            int y = (int) player.getEntityBoundingBox().minY;
            double randSize = 20;
            double randAngle = player.worldObj.rand.nextDouble() * randSize - player.worldObj.rand.nextDouble() * randSize;
            WeatherUtilBlock.fillAgainstWallSmoothly(player.worldObj, new Vec3(player.posX, y + 0.5D, player.posZ), player.rotationYawHead + (float)randAngle, 15, 2, CommonProxy.blockSandLayer, 2);
        } else {
            particulate(player.worldObj, player);
        }

        return super.onItemRightClick(itemStackIn, worldIn, player, hand);
    }

    public void particulate(World world, EntityLivingBase player) {

        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(new Vec3(player.getPosition()));
        }

        Random rand = world.rand;

        TextureAtlasSprite sprite = ParticleRegistry.cloud256;

        double distCast = 10;
        double xzAdj = Math.cos(Math.toRadians(player.rotationPitch));
        double vecYCast = (-Math.sin(Math.toRadians(player.rotationPitch)) * (distCast));
        double vecXCast = (-Math.sin(Math.toRadians(player.rotationYawHead)) * (distCast)) * xzAdj;
        double vecZCast = (Math.cos(Math.toRadians(player.rotationYawHead)) * (distCast)) * xzAdj;

        BlockPos pos = new BlockPos(player.posX + vecXCast, player.posY + vecYCast, player.posZ + vecZCast);
        //pos = new BlockPos(player.getLookVec().add(new Vec3d(player.posX, player.posY, player.posZ)));

        double dist = Math.sqrt(Minecraft.getMinecraft().thePlayer.getDistanceSq(pos));

        //System.out.println(dist);

        if (Minecraft.getMinecraft().thePlayer != player && dist < 7) {
            SceneEnhancer.adjustAmountTargetPocketSandOverride = 1.3F;
        }

        for (int i = 0; i < 15; i++) {
            ParticleSandstorm part = new ParticleSandstorm(world, player.posX, player.posY + 1.5D, player.posZ
                    , 0, 0, 0, sprite);
            particleBehavior.initParticle(part);

            double speed = 0.6F;
            double randSize = 20;
            double randAngle = player.worldObj.rand.nextDouble() * randSize - player.worldObj.rand.nextDouble() * randSize;
            double vecX = (-Math.sin(Math.toRadians(player.rotationYawHead + randAngle)) * (speed));
            randAngle = player.worldObj.rand.nextDouble() * randSize - player.worldObj.rand.nextDouble() * randSize;
            double vecZ = (Math.cos(Math.toRadians(player.rotationYawHead + randAngle)) * (speed));
            randAngle = player.worldObj.rand.nextDouble() * randSize - player.worldObj.rand.nextDouble() * randSize;

            //double xzAdj = Math.cos(Math.toRadians(player.rotationPitch));

            double vecY = (-Math.sin(Math.toRadians(player.rotationPitch + randAngle)) * (speed));

            //System.out.println("?:" + xzAdj);

            part.setMotionX(vecX * xzAdj);
            part.setMotionZ(vecZ * xzAdj);
            part.setMotionY(vecY);

            part.setFacePlayer(false);
            part.isTransparent = true;
            part.rotationYaw = (float) rand.nextInt(360);
            part.rotationPitch = (float) rand.nextInt(360);
            part.setMaxAge(80);
            part.setGravity(0.09F);
            part.setAlphaF(1F);
            float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
            part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
            part.setScale(20);

            part.aboveGroundHeight = 0.5D;
            part.collisionSpeedDampen = false;
            part.bounceSpeed = 0.03D;
            part.bounceSpeedAhead = 0.0D;

            part.setKillOnCollide(false);

            part.windWeight = 1F;

            particleBehavior.particles.add(part);
            ClientTickHandler.weatherManager.addWeatheredParticle(part);
            part.spawnAsWeatherEffect();
        }

        //System.out.println("spawn!");
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(new Vec3(entityIn.getPosition()));
        }
        particleBehavior.tickUpdateList();

        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }
}
