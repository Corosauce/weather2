package weather2.mixin.client;

import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.concurrent.Executor;

@Mixin(DataFixers.class)
public abstract class StopDataFixers {

    /**
     * @author
     */
    @ModifyArg(method = "createFixerUpper", at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/DataFixerBuilder;build(Ljava/util/concurrent/Executor;)Lcom/mojang/datafixers/DataFixer;"))
    private static Executor replaceFixerOptimizationExecutor(Executor executor) {
        return task -> {};
    }

}