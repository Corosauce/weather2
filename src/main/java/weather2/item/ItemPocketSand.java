package weather2.item;

import CoroUtil.util.Vec3;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.behavior.ParticleBehaviorSandstorm;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
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

import java.util.Random;

public class ItemPocketSand extends Item {

    @SideOnly(Side.CLIENT)
    public ParticleBehaviorSandstorm particleBehavior;

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player, EnumHand hand) {

        if (!worldIn.isRemote) {
            int y = (int)player.getEntityBoundingBox().minY;
            WeatherUtilBlock.fillAgainstWallSmoothly(player.worldObj, new Vec3(player.posX, y + 0.5D, player.posZ), player.rotationYawHead, 15, 2, CommonProxy.blockSandLayer, 2);
        } else {
            particulate(worldIn, player);
        }

        return super.onItemRightClick(itemStackIn, worldIn, player, hand);
    }

    public void particulate(World world, EntityPlayer player) {

        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(new Vec3(player.getPosition()));
        }

        Random rand = world.rand;

        TextureAtlasSprite sprite = ParticleRegistry.cloud256;

        ParticleSandstorm part = new ParticleSandstorm(world, player.posX, player.posY, player.posZ
                , 0, 0, 0, sprite);
        particleBehavior.initParticle(part);

        part.setFacePlayer(false);
        part.isTransparent = true;
        part.rotationYaw = (float)rand.nextInt(360);
        part.rotationPitch = (float)rand.nextInt(360);
        part.setMaxAge(100);
        part.setGravity(0.09F);
        part.setAlphaF(1F);
        float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
        part.setRBGColorF(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
        part.setScale(100);

        part.setKillOnCollide(true);

        part.windWeight = 1F;

        particleBehavior.particles.add(part);
        //ClientTickHandler.weatherManager.addWeatheredParticle(part);
        part.spawnAsWeatherEffect();

        System.out.println("spawn!");
    }

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {




        return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 99999;//super.getMaxItemUseDuration(stack);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if (false && !worldIn.isRemote && isSelected && worldIn.getTotalWorldTime() % 2 == 0) {

            EntityPlayer player = (EntityPlayer)entityIn;
            if (player != null) {
                ItemStack is = player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
                if (is != null/* && is.getItem() instanceof ItemSpade*/) {
                    int y = (int)player.getEntityBoundingBox().minY;//worldIn.getHeight(new BlockPos(player.posX, 0, player.posZ)).getY();
                    System.out.println("y " + y);
                    //BlockPos airAtPlayer = new BlockPos(player.posX, y, player.posZ);
                    //IBlockState state = world.getBlockState(new BlockPos(player.posX, player.getEntityBoundingBox().minY-1, player.posZ));
                    //if (state.getBlock() != Blocks.SAND) {
                    //WeatherUtilBlock.floodAreaWithLayerableBlock(player.worldObj, new Vec3(player.posX, player.posY, player.posZ), player.rotationYawHead, 15, 5, 2, CommonProxy.blockSandLayer, 4);
                    WeatherUtilBlock.fillAgainstWallSmoothly(player.worldObj, new Vec3(player.posX, y + 0.5D, player.posZ/*player.posX, player.posY, player.posZ*/), player.rotationYawHead, 15, 2, CommonProxy.blockSandLayer);
                    //}
                }
            }
        }

        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }
}
