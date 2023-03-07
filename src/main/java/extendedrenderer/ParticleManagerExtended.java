package extendedrenderer;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import extendedrenderer.particle.entity.EntityRotFX;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ParticleManagerExtended implements PreparableReloadListener {
   private static final int MAX_PARTICLES_PER_LAYER = 16384;
   private static final List<ParticleRenderType> RENDER_ORDER = ImmutableList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM, EntityRotFX.SORTED_OPAQUE_BLOCK, EntityRotFX.SORTED_TRANSLUCENT);
   protected ClientLevel level;
   private final Map<ParticleRenderType, Queue<Particle>> particles = Maps.newTreeMap(net.minecraftforge.client.ForgeHooksClient.makeParticleRenderTypeComparator(RENDER_ORDER));
   private final Queue<TrackingEmitter> trackingEmitters = Queues.newArrayDeque();
   private final TextureManager textureManager;
   private final Random random = new Random();
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

   public <T extends ParticleOptions> void register(ParticleType<T> p_107382_, ParticleProvider<T> p_107383_) {
      this.providers.put(Registry.PARTICLE_TYPE.getKey(p_107382_), p_107383_);
   }

   public <T extends ParticleOptions> void register(ParticleType<T> p_107379_, ParticleManagerExtended.SpriteParticleRegistration<T> p_107380_) {
      ParticleManagerExtended.MutableSpriteSet particleengine$mutablespriteset = new ParticleManagerExtended.MutableSpriteSet();
      this.spriteSets.put(Registry.PARTICLE_TYPE.getKey(p_107379_), particleengine$mutablespriteset);
      this.providers.put(Registry.PARTICLE_TYPE.getKey(p_107379_), p_107380_.create(particleengine$mutablespriteset));
   }

   public CompletableFuture<Void> reload(PreparationBarrier p_107305_, ResourceManager p_107306_, ProfilerFiller p_107307_, ProfilerFiller p_107308_, Executor p_107309_, Executor p_107310_) {
      Map<ResourceLocation, List<ResourceLocation>> map = Maps.newConcurrentMap();
      CompletableFuture<?>[] completablefuture = Registry.PARTICLE_TYPE.keySet().stream().map((p_107315_) -> {
         return CompletableFuture.runAsync(() -> {
            this.loadParticleDescription(p_107306_, p_107315_, map);
         }, p_107309_);
      }).toArray((p_107303_) -> {
         return new CompletableFuture[p_107303_];
      });
      return CompletableFuture.allOf(completablefuture).thenApplyAsync((p_107324_) -> {
         p_107307_.startTick();
         p_107307_.push("stitching");
         TextureAtlas.Preparations textureatlas$preparations = this.textureAtlas.prepareToStitch(p_107306_, map.values().stream().flatMap(Collection::stream), p_107307_, 0);
         p_107307_.pop();
         p_107307_.endTick();
         return textureatlas$preparations;
      }, p_107309_).thenCompose(p_107305_::wait).thenAcceptAsync((p_107328_) -> {
         this.particles.clear();
         p_107308_.startTick();
         p_107308_.push("upload");
         this.textureAtlas.reload(p_107328_);
         p_107308_.popPush("bindSpriteSets");
         TextureAtlasSprite textureatlassprite = this.textureAtlas.getSprite(MissingTextureAtlasSprite.getLocation());
         map.forEach((p_172268_, p_172269_) -> {
            ImmutableList<TextureAtlasSprite> immutablelist = p_172269_.isEmpty() ? ImmutableList.of(textureatlassprite) : p_172269_.stream().map(this.textureAtlas::getSprite).collect(ImmutableList.toImmutableList());
            this.spriteSets.get(p_172268_).rebind(immutablelist);
         });
         p_107308_.pop();
         p_107308_.endTick();
      }, p_107310_);
   }

   public void close() {
      this.textureAtlas.clearTextureData();
   }

   private void loadParticleDescription(ResourceManager p_107317_, ResourceLocation p_107318_, Map<ResourceLocation, List<ResourceLocation>> p_107319_) {
      ResourceLocation resourcelocation = new ResourceLocation(p_107318_.getNamespace(), "particles/" + p_107318_.getPath() + ".json");

      try {
         Resource resource = p_107317_.getResource(resourcelocation);

         try {
            Reader reader = new InputStreamReader(resource.getInputStream(), Charsets.UTF_8);

            try {
               ParticleDescription particledescription = ParticleDescription.fromJson(GsonHelper.parse(reader));
               List<ResourceLocation> list = particledescription.getTextures();
               boolean flag = this.spriteSets.containsKey(p_107318_);
               if (list == null) {
                  if (flag) {
                     throw new IllegalStateException("Missing texture list for particle " + p_107318_);
                  }
               } else {
                  if (!flag) {
                     throw new IllegalStateException("Redundant texture list for particle " + p_107318_);
                  }

                  p_107319_.put(p_107318_, list.stream().map((p_107387_) -> {
                     return new ResourceLocation(p_107387_.getNamespace(), "particle/" + p_107387_.getPath());
                  }).collect(Collectors.toList()));
               }
            } catch (Throwable throwable2) {
               try {
                  reader.close();
               } catch (Throwable throwable1) {
                  throwable2.addSuppressed(throwable1);
               }

               throw throwable2;
            }

            reader.close();
         } catch (Throwable throwable3) {
            if (resource != null) {
               try {
                  resource.close();
               } catch (Throwable throwable) {
                  throwable3.addSuppressed(throwable);
               }
            }

            throw throwable3;
         }

         if (resource != null) {
            resource.close();
         }

      } catch (IOException ioexception) {
         throw new IllegalStateException("Failed to load description for particle " + p_107318_, ioexception);
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
      ParticleProvider<T> particleprovider = (ParticleProvider<T>)this.providers.get(Registry.PARTICLE_TYPE.getKey(p_107396_.getType()));
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
      this.particles.forEach((p_107349_, p_107350_) -> {
         this.level.getProfiler().push(p_107349_.toString());
         this.tickParticleList(p_107350_);
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
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
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
                  particleCount++;
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
      this.particles.clear();
      this.trackingEmitters.clear();
      this.trackedParticleCounts.clear();
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

   @OnlyIn(Dist.CLIENT)
   class MutableSpriteSet implements SpriteSet {
      private List<TextureAtlasSprite> sprites;

      public TextureAtlasSprite get(int p_107413_, int p_107414_) {
         return this.sprites.get(p_107413_ * (this.sprites.size() - 1) / p_107414_);
      }

      public TextureAtlasSprite get(Random p_107418_) {
         return this.sprites.get(p_107418_.nextInt(this.sprites.size()));
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
