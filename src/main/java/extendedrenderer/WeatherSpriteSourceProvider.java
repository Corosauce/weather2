/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package extendedrenderer;

import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;
import weather2.Weather;

import java.util.Optional;

public class WeatherSpriteSourceProvider extends SpriteSourceProvider
{
    public WeatherSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper)
    {
        super(output, fileHelper, Weather.MODID);
    }

    @Override
    protected void addSources()
    {
        atlas(SpriteSourceProvider.PARTICLES_ATLAS).addSource(new SingleFile(new ResourceLocation(Weather.MODID + "white"), Optional.empty()));
    }
}
