package weather2.client.shaders;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import javax.annotation.Nullable;
import java.io.IOException;

public class ShaderInstanceExtended extends ShaderInstance {

    @Nullable
    public final Uniform CUSTOM_TIME;

    public ShaderInstanceExtended(ResourceProvider p_173336_, ResourceLocation shaderLocation, VertexFormat p_173338_) throws IOException {
        super(p_173336_, shaderLocation, p_173338_);

        this.CUSTOM_TIME = this.getUniform("CustomTime");
    }

}
