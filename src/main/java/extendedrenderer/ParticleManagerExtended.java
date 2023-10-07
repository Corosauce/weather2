package extendedrenderer;

import com.google.common.collect.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import extendedrenderer.particle.entity.EntityRotFX;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ParticleManagerExtended implements PreparableReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final FileToIdConverter PARTICLE_LISTER = FileToIdConverter.json("particles");
   private static final ResourceLocation PARTICLES_ATLAS_INFO = new ResourceLocation("particles");
   private static final int MAX_PARTICLES_PER_LAYER = 16384;
   private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM, ParticleRenderType.CUSTOM, EntityRotFX.SORTED_OPAQUE_BLOCK, EntityRotFX.SORTED_TRANSLUCENT);
   protected ClientLevel level;
   private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.newTreeMap(net.minecraftforge.client.ForgeHooksClient.makeParticleRenderTypeComparator(RENDER_ORDER));
   private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
   private final TextureManager textureManager;
   private final RandomSource random = RandomSource.create();
   private final Map<ResourceLocation, ParticleProvider<?>> providers = new java.util.HashMap<>();
   private final Queue<Particle> particlesToAdd = Queues.newArrayDeque();
   private final Map<ResourceLocation, ParticleManagerExtended.MutableSpriteSet> spriteSets = Maps.newHashMap();
   private final TextureAtlas textureAtlas;
   private final Object2IntOpenHashMap<ParticleGroup> trackedParticleCounts = new Object2IntOpenHashMap<>();

   public ParticleManagerExtended(ClientLevel p_107299_, TextureManager p_107300_) {
      this.textureAtlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);
      //p_107300_.register(this.textureAtlas.location(), this.textureAtlas);
      this.level = p_107299_;
      this.textureManager = p_107300_;
   }

   /** @deprecated Register via {@link net.minecraftforge.client.event.RegisterParticleProvidersEvent} */
   @Deprecated
   public <T extends ParticleOptions> void register(ParticleType<T> p_107382_, ParticleProvider<T> p_107383_) {
      this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getKey(p_107382_), p_107383_);
   }

   /** @deprecated Register via {@link net.minecraftforge.client.event.RegisterParticleProvidersEvent} */
   @Deprecated
   public <T extends ParticleOptions> void register(ParticleType<T> p_273423_, ParticleProvider.Sprite<T> p_273134_) {
      this.register(p_273423_, (p_272320_) -> {
         return (p_272323_, p_272324_, p_272325_, p_272326_, p_272327_, p_272328_, p_272329_, p_272330_) -> {
            TextureSheetParticle texturesheetparticle = p_273134_.createParticle(p_272323_, p_272324_, p_272325_, p_272326_, p_272327_, p_272328_, p_272329_, p_272330_);
            if (texturesheetparticle != null) {
               texturesheetparticle.pickSprite(p_272320_);
            }

            return texturesheetparticle;
         };
      });
   }

   /** @deprecated Register via {@link net.minecraftforge.client.event.RegisterParticleProvidersEvent} */
   @Deprecated
   public <T extends ParticleOptions> void register(ParticleType<T> p_107379_, ParticleManagerExtended.SpriteParticleRegistration<T> p_107380_) {
      ParticleManagerExtended.MutableSpriteSet particleengine$mutablespriteset = new ParticleManagerExtended.MutableSpriteSet();
      this.spriteSets.put(BuiltInRegistries.PARTICLE_TYPE.getKey(p_107379_), particleengine$mutablespriteset);
      this.providers.put(BuiltInRegistries.PARTICLE_TYPE.getKey(p_107379_), p_107380_.create(particleengine$mutablespriteset));
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier p_107305_, ResourceManager p_107306_, ProfilerFiller p_107307_, ProfilerFiller p_107308_, Executor p_107309_, Executor p_107310_) {
      @OnlyIn(Dist.CLIENT)
      record ParticleDefinition(ResourceLocation id, Optional<List<ResourceLocation>> sprites) {
      }
      CompletableFuture<List<ParticleDefinition>> completablefuture = CompletableFuture.supplyAsync(() -> {
         return PARTICLE_LISTER.listMatchingResources(p_107306_);
      }, p_107309_).thenCompose((p_247914_) -> {
         List<CompletableFuture<ParticleDefinition>> list = new ArrayList<>(p_247914_.size());
         p_247914_.forEach((p_247903_, p_247904_) -> {
            ResourceLocation resourcelocation = PARTICLE_LISTER.fileToId(p_247903_);
            list.add(CompletableFuture.supplyAsync(() -> {
               return new ParticleDefinition(resourcelocation, this.loadParticleDescription(resourcelocation, p_247904_));
            }, p_107309_));
         });
         return Util.sequence(list);
      });
      CompletableFuture<SpriteLoader.Preparations> completablefuture1 = SpriteLoader.create(this.textureAtlas).loadAndStitch(p_107306_, PARTICLES_ATLAS_INFO, 0, p_107309_).thenCompose(SpriteLoader.Preparations::waitForUpload);
      return CompletableFuture.allOf(completablefuture1, completablefuture).thenCompose(p_107305_::wait).thenAcceptAsync((p_247900_) -> {
         this.clearParticles();
         p_107308_.startTick();
         p_107308_.push("upload");
         SpriteLoader.Preparations spriteloader$preparations = completablefuture1.join();
         this.textureAtlas.upload(spriteloader$preparations);
         p_107308_.popPush("bindSpriteSets");
         Set<ResourceLocation> set = new HashSet<>();
         TextureAtlasSprite textureatlassprite = spriteloader$preparations.missing();
         completablefuture.join().forEach((p_247911_) -> {
            Optional<List<ResourceLocation>> optional = p_247911_.sprites();
            if (!optional.isEmpty()) {
               List<TextureAtlasSprite> list = new ArrayList<>();

               for(ResourceLocation resourcelocation : optional.get()) {
                  TextureAtlasSprite textureatlassprite1 = spriteloader$preparations.regions().get(resourcelocation);
                  if (textureatlassprite1 == null) {
                     set.add(resourcelocation);
                     list.add(textureatlassprite);
                  } else {
                     list.add(textureatlassprite1);
                  }
               }

               if (list.isEmpty()) {
                  list.add(textureatlassprite);
               }

               this.spriteSets.get(p_247911_.id()).rebind(list);
            }
         });
         if (!set.isEmpty()) {
            LOGGER.warn("Missing particle sprites: {}", set.stream().sorted().map(ResourceLocation::toString).collect(Collectors.joining(",")));
         }

         p_107308_.pop();
         p_107308_.endTick();
      }, p_107310_);
   }

   public void close() {
      this.textureAtlas.clearTextureData();
   }

   private Optional<List<ResourceLocation>> loadParticleDescription(ResourceLocation p_250648_, Resource p_248793_) {
      if (!this.spriteSets.containsKey(p_250648_)) {
         LOGGER.debug("Redundant texture list for particle: {}", (Object)p_250648_);
         return Optional.empty();
      } else {
         try (Reader reader = p_248793_.openAsReader()) {
            ParticleDescription particledescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
            return Optional.of(particledescription.getTextures());
         } catch (IOException ioexception) {
            throw new IllegalStateException("Failed to load description for particle " + p_250648_, ioexception);
         }
      }
   }

   public void createTrackingEmitter(Entity p_107330_, ParticleOptions p_107331_) {
      this.trackingEmitters.add(new TrackingEmitter(this.level, p_107330_, p_107331_));
   }

   public void createTrackingEmitter(Entity p_107333_, ParticleOptions p_107334_, int p_107335_) {
      this.trackingEmitters.add(new TrackingEmitter(this.level, p_107333_, p_107334_, p_107335_));
   }

   @Nullable
   public Particle createParticle(ParticleOptions p_107371_, double p_107372_, double p_107373_, double p_107374_, double p_107375_, double p_107376_, double p_107377_) {
      Particle particle = this.makeParticle(p_107371_, p_107372_, p_107373_, p_107374_, p_107375_, p_107376_, p_107377_);
      if (particle != null) {
         this.add(particle);
         return particle;
      } else {
         return null;
      }
   }

   @Nullable
   private <T extends ParticleOptions> Particle makeParticle(T p_107396_, double p_107397_, double p_107398_, double p_107399_, double p_107400_, double p_107401_, double p_107402_) {
      ParticleProvider<T> particleprovider = (ParticleProvider<T>)this.providers.get(BuiltInRegistries.PARTICLE_TYPE.getKey(p_107396_.getType()));
      return particleprovider == null ? null : particleprovider.createParticle(p_107396_, this.level, p_107397_, p_107398_, p_107399_, p_107400_, p_107401_, p_107402_);
   }

   public void add(Particle p_107345_) {
      Optional<ParticleGroup> optional = p_107345_.getParticleGroup();
      if (optional.isPresent()) {
         if (this.hasSpaceInParticleLimit(optional.get())) {
            this.particlesToAdd.add(p_107345_);
            this.updateCount(optional.get(), 1);
         }
      } else {
         this.particlesToAdd.add(p_107345_);
      }

   }

   public void tick() {
      this.particles.forEach((p_288249_, p_288250_) -> {
         this.level.getProfiler().push(p_288249_.toString());
         this.tickParticleList(p_288250_);
         this.level.getProfiler().pop();
      });
      if (!this.trackingEmitters.isEmpty()) {
         List<TrackingEmitter> list = Lists.newArrayList();

         for(TrackingEmitter trackingemitter : this.trackingEmitters) {
            trackingemitter.tick();
            if (!trackingemitter.isAlive()) {
               list.add(trackingemitter);
            }
         }

         this.trackingEmitters.removeAll(list);
      }

      Particle particle;
      if (!this.particlesToAdd.isEmpty()) {
         while((particle = this.particlesToAdd.poll()) != null) {
            this.particles.computeIfAbsent(particle.getRenderType(), (p_107347_) -> {
               return EvictingQueue.create(16384 * 2);
            }).add(particle);
         }
      }

   }

   private void tickParticleList(Collection<Particle> p_107385_) {
      if (!p_107385_.isEmpty()) {
         Iterator<Particle> iterator = p_107385_.iterator();

         while(iterator.hasNext()) {
            Particle particle = iterator.next();
            this.tickParticle(particle);
            if (!particle.isAlive()) {
               particle.getParticleGroup().ifPresent((p_172289_) -> {
                  this.updateCount(p_172289_, -1);
               });
               iterator.remove();
            }
         }
      }

   }

   private void updateCount(ParticleGroup p_172282_, int p_172283_) {
      this.trackedParticleCounts.addTo(p_172282_, p_172283_);
   }

   private void tickParticle(Particle p_107394_) {
      try {
         p_107394_.tick();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking Particle");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being ticked");
         crashreportcategory.setDetail("Particle", p_107394_::toString);
         crashreportcategory.setDetail("Particle Type", p_107394_.getRenderType()::toString);
         throw new ReportedException(crashreport);
      }
   }

   /**@deprecated Forge: use {@link #render(PoseStack, MultiBufferSource.BufferSource, LightTexture, Camera, float, net.minecraft.client.renderer.culling.Frustum)} with Frustum as additional parameter*/
   @Deprecated
   public void render(PoseStack p_107337_, MultiBufferSource.BufferSource p_107338_, LightTexture p_107339_, Camera p_107340_, float p_107341_) {
       render(p_107337_, p_107338_, p_107339_, p_107340_, p_107341_, null);
   }

   public void render(PoseStack p_107337_, MultiBufferSource.BufferSource p_107338_, LightTexture p_107339_, Camera p_107340_, float p_107341_, @Nullable net.minecraft.client.renderer.culling.Frustum clippingHelper) {

      //if (true) return;
      float fogStart = RenderSystem.getShaderFogStart();
      float fogEnd = RenderSystem.getShaderFogEnd();
      RenderSystem.setShaderFogStart(fogStart * 4);
      RenderSystem.setShaderFogEnd(fogEnd * 4);

      p_107339_.turnOnLightLayer();
      RenderSystem.enableDepthTest();

      //these didnt exist in our 1.18 modification, why?
      RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
      RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);

      PoseStack posestack = RenderSystem.getModelViewStack();
      posestack.pushPose();
      posestack.mulPoseMatrix(p_107337_.last().pose());
      RenderSystem.applyModelViewMatrix();

      RenderSystem.disableCull();
      int particleCount = 0;

      for(ParticleRenderType particlerendertype : this.particles.keySet()) { // Forge: allow custom IParticleRenderType's
         if (particlerendertype == ParticleRenderType.NO_RENDER) continue;
         Iterable<Particle> iterable = this.particles.get(particlerendertype);
         if (iterable != null) {
            RenderSystem.setShader(GameRenderer::getParticleShader);
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tesselator.getBuilder();
            particlerendertype.begin(bufferbuilder, this.textureManager);

            for(Particle particle : iterable) {

               if (particle instanceof EntityRotFX) {
                  if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isVisible(((EntityRotFX)particle).getBoundingBoxForRender(p_107341_)))
                     continue;
               } else {
                  if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isVisible(particle.getBoundingBox()))
                     continue;
               }

               try {
                  particle.render(bufferbuilder, p_107340_, p_107341_);
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                  crashreportcategory.setDetail("Particle", particle::toString);
                  crashreportcategory.setDetail("Particle Type", particlerendertype::toString);
                  throw new ReportedException(crashreport);
               }
            }

            particlerendertype.end(tesselator);
         }
      }

      posestack.popPose();
      RenderSystem.applyModelViewMatrix();
      RenderSystem.depthMask(true);
      RenderSystem.disableBlend();
      p_107339_.turnOffLightLayer();

      RenderSystem.setShaderFogStart(fogStart);
      RenderSystem.setShaderFogEnd(fogEnd);
   }

   public void setLevel(@Nullable ClientLevel p_107343_) {
      this.level = p_107343_;
      this.clearParticles();
      this.trackingEmitters.clear();
   }

   public void destroy(BlockPos p_107356_, BlockState p_107357_) {
      if (!p_107357_.isAir()) {
         VoxelShape voxelshape = p_107357_.getShape(this.level, p_107356_);
         double d0 = 0.25D;
         voxelshape.forAllBoxes((p_172273_, p_172274_, p_172275_, p_172276_, p_172277_, p_172278_) -> {
            double d1 = Math.min(1.0D, p_172276_ - p_172273_);
            double d2 = Math.min(1.0D, p_172277_ - p_172274_);
            double d3 = Math.min(1.0D, p_172278_ - p_172275_);
            int i = Math.max(2, Mth.ceil(d1 / 0.25D));
            int j = Math.max(2, Mth.ceil(d2 / 0.25D));
            int k = Math.max(2, Mth.ceil(d3 / 0.25D));

            for(int l = 0; l < i; ++l) {
               for(int i1 = 0; i1 < j; ++i1) {
                  for(int j1 = 0; j1 < k; ++j1) {
                     double d4 = ((double)l + 0.5D) / (double)i;
                     double d5 = ((double)i1 + 0.5D) / (double)j;
                     double d6 = ((double)j1 + 0.5D) / (double)k;
                     double d7 = d4 * d1 + p_172273_;
                     double d8 = d5 * d2 + p_172274_;
                     double d9 = d6 * d3 + p_172275_;
                     this.add(new TerrainParticle(this.level, (double)p_107356_.getX() + d7, (double)p_107356_.getY() + d8, (double)p_107356_.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D, p_107357_, p_107356_).updateSprite(p_107357_, p_107356_));
                  }
               }
            }

         });
      }
   }

   public void crack(BlockPos p_107368_, Direction p_107369_) {
      BlockState blockstate = this.level.getBlockState(p_107368_);
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         int i = p_107368_.getX();
         int j = p_107368_.getY();
         int k = p_107368_.getZ();
         float f = 0.1F;
         AABB aabb = blockstate.getShape(this.level, p_107368_).bounds();
         double d0 = (double)i + this.random.nextDouble() * (aabb.maxX - aabb.minX - (double)0.2F) + (double)0.1F + aabb.minX;
         double d1 = (double)j + this.random.nextDouble() * (aabb.maxY - aabb.minY - (double)0.2F) + (double)0.1F + aabb.minY;
         double d2 = (double)k + this.random.nextDouble() * (aabb.maxZ - aabb.minZ - (double)0.2F) + (double)0.1F + aabb.minZ;
         if (p_107369_ == Direction.DOWN) {
            d1 = (double)j + aabb.minY - (double)0.1F;
         }

         if (p_107369_ == Direction.UP) {
            d1 = (double)j + aabb.maxY + (double)0.1F;
         }

         if (p_107369_ == Direction.NORTH) {
            d2 = (double)k + aabb.minZ - (double)0.1F;
         }

         if (p_107369_ == Direction.SOUTH) {
            d2 = (double)k + aabb.maxZ + (double)0.1F;
         }

         if (p_107369_ == Direction.WEST) {
            d0 = (double)i + aabb.minX - (double)0.1F;
         }

         if (p_107369_ == Direction.EAST) {
            d0 = (double)i + aabb.maxX + (double)0.1F;
         }

         this.add((new TerrainParticle(this.level, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate, p_107368_).updateSprite(blockstate, p_107368_)).setPower(0.2F).scale(0.6F));
      }
   }

   public String countParticles() {
      return String.valueOf(this.particles.values().stream().mapToInt(Collection::size).sum());
   }

   private boolean hasSpaceInParticleLimit(ParticleGroup p_172280_) {
      return this.trackedParticleCounts.getInt(p_172280_) < p_172280_.getLimit();
   }

   private void clearParticles() {
      this.particles.clear();
      this.particlesToAdd.clear();
      this.trackingEmitters.clear();
      this.trackedParticleCounts.clear();
   }

   @OnlyIn(Dist.CLIENT)
   static class MutableSpriteSet implements SpriteSet {
      private List<TextureAtlasSprite> sprites;

      public TextureAtlasSprite get(int p_107413_, int p_107414_) {
         return this.sprites.get(p_107413_ * (this.sprites.size() - 1) / p_107414_);
      }

      public TextureAtlasSprite get(RandomSource p_233889_) {
         return this.sprites.get(p_233889_.nextInt(this.sprites.size()));
      }

      public void rebind(List<TextureAtlasSprite> p_107416_) {
         this.sprites = ImmutableList.copyOf(p_107416_);
      }
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   public interface SpriteParticleRegistration<T extends ParticleOptions> {
      ParticleProvider<T> create(SpriteSet p_107420_);
   }
}
