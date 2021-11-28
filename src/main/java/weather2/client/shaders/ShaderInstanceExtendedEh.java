package weather2.client.shaders;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@OnlyIn(Dist.CLIENT)
public class ShaderInstanceExtendedEh implements Shader, AutoCloseable {

    //@Nullable
    //public final Uniform CUSTOM_TIME;

    public ShaderInstanceExtendedEh(ResourceProvider p_173336_, ResourceLocation shaderLocation, VertexFormat p_173338_) throws IOException {
        //super(p_173336_, shaderLocation, p_173338_);

        /*ResourceLocation resourcelocation = new ResourceLocation(shaderLocation.getNamespace(), "shaders/core/" + shaderLocation.getPath() + ".json");
        Resource resource = null;

        try {
            resource = p_173336_.getResource(resourcelocation);
            JsonObject jsonobject = GsonHelper.parse(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            String s = GsonHelper.getAsString(jsonobject, "vertex");
            String s1 = GsonHelper.getAsString(jsonobject, "fragment");
            JsonArray jsonarray1 = GsonHelper.getAsJsonArray(jsonobject, "attributes", (JsonArray) null);
            if (jsonarray1 != null) {
                int j = 0;
                this.attributes = Lists.newArrayListWithCapacity(jsonarray1.size());
                this.attributeNames = Lists.newArrayListWithCapacity(jsonarray1.size());

                for (JsonElement jsonelement1 : jsonarray1) {
                    try {
                        this.attributeNames.add(GsonHelper.convertToString(jsonelement1, "attribute"));
                    } catch (Exception exception1) {
                        ChainedJsonException chainedjsonexception2 = ChainedJsonException.forException(exception1);
                        chainedjsonexception2.prependJsonKey("attributes[" + j + "]");
                        throw chainedjsonexception2;
                    }

                    ++j;
                }
            } else {
                this.attributes = null;
                this.attributeNames = null;
            }
        } catch (Exception exception3) {
            ChainedJsonException chainedjsonexception = ChainedJsonException.forException(exception3);
            chainedjsonexception.setFilenameAndFlush(resourcelocation.getPath());
            throw chainedjsonexception;
        } finally {
            IOUtils.closeQuietly((Closeable)resource);
        }

        this.CUSTOM_TIME = this.getUniform("CustomTime");*/
    }


    @Override
    public int getId() {
        return 0;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public Program getVertexProgram() {
        return null;
    }

    @Override
    public Program getFragmentProgram() {
        return null;
    }

    @Override
    public void attachToProgram() {

    }

    @Override
    public void close() throws Exception {

    }
}
