package weather2;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import weather2.block.SandLayerBlock;

public class WeatherBlocks {

    public static final String sand_layer = "sand_layer";

    public static Block blockSandLayer;

    @SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        addBlock(event, blockSandLayer = (new SandLayerBlock(AbstractBlock.Properties.create(Material.SAND).tickRandomly().hardnessAndResistance(0.1F).setRequiresTool().sound(SoundType.SAND))), sand_layer, false);
    }

    public static void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String unlocalizedName, boolean creativeTab) {
        parBlock.setRegistryName(Weather.MODID, unlocalizedName);

        if (event != null) {
            event.getRegistry().register(parBlock);
        }
    }

}
