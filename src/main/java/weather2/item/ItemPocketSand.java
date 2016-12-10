package weather2.item;

import CoroUtil.util.Vec3;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorSandstorm;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import weather2.CommonProxy;
import weather2.client.entity.particle.ParticleSandstorm;
import weather2.util.WeatherUtilBlock;

import javax.annotation.Nullable;
import java.util.Random;

public class ItemPocketSand extends Item {

    @SideOnly(Side.CLIENT)
    public ParticleBehaviorSandstorm particleBehavior;

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player, EnumHand hand) {
        player.setActiveHand(hand);
        return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
    }

    public void particulate(World world, EntityLivingBase player) {

        //System.out.println("aaaa " + world.getTotalWorldTime());

        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(new Vec3(player.getPosition()));
        }

        Random rand = world.rand;

        TextureAtlasSprite sprite = ParticleRegistry.cloud256;

        for (int i = 0; i < 5; i++) {
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

            double xzAdj = Math.cos(Math.toRadians(player.rotationPitch));

            double vecY = (-Math.sin(Math.toRadians(player.rotationPitch + randAngle)) * (speed));

            //System.out.println("?:" + xzAdj);

            part.setMotionX(vecX * xzAdj);
            part.setMotionZ(vecZ * xzAdj);
            part.setMotionY(vecY);

            part.setFacePlayer(false);
            part.isTransparent = true;
            part.rotationYaw = (float) rand.nextInt(360);
            part.rotationPitch = (float) rand.nextInt(360);
            part.setMaxAge(100);
            part.setGravity(0.09F);
            part.setAlphaF(1F);
            float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
            part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
            part.setScale(20);

            part.aboveGroundHeight = 0.5D;
            part.collisionSpeedDampen = false;
            part.bounceSpeed = 0.03D;
            part.bounceSpeedAhead = 0.03D;

            part.setKillOnCollide(false);

            part.windWeight = 1F;

            particleBehavior.particles.add(part);
            //ClientTickHandler.weatherManager.addWeatheredParticle(part);
            part.spawnAsWeatherEffect();
        }

        //System.out.println("spawn!");
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {


        //System.out.println("using use");

        return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 99999;//super.getMaxItemUseDuration(stack);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(new Vec3(entityIn.getPosition()));
        }
        particleBehavior.tickUpdateList();

        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        super.onUsingTick(stack, player, count);

        //System.out.println("using tick");

        if (!player.worldObj.isRemote) {
            if (player.worldObj.getTotalWorldTime() % 2 == 0) {
                int y = (int) player.getEntityBoundingBox().minY;
                double randSize = 20;
                double randAngle = player.worldObj.rand.nextDouble() * randSize - player.worldObj.rand.nextDouble() * randSize;
                WeatherUtilBlock.fillAgainstWallSmoothly(player.worldObj, new Vec3(player.posX, y + 0.5D, player.posZ), player.rotationYawHead + (float)randAngle, 15, 2, CommonProxy.blockSandLayer, 2);
            }
        } else {
            particulate(player.worldObj, player);
        }
    }

    @Nullable
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        //System.out.println("using finish");
        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        //System.out.println("using stop");
        super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
    }

    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        //System.out.println("using first");
        return super.onItemUseFirst(stack, player, world, pos, side, hitX, hitY, hitZ, hand);
    }
}
