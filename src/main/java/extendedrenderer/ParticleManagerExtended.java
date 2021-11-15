package extendedrenderer;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;

@OnlyIn(Dist.CLIENT)
public class ParticleManagerExtended implements PreparableReloadListener {
   private static final List<ParticleRenderType> TYPES = ImmutableList.of(ParticleRenderType.TERRAIN_SHEET, ParticleRenderType.PARTICLE_SHEET_OPAQUE, ParticleRenderType.PARTICLE_SHEET_LIT, ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, ParticleRenderType.CUSTOM);
   protected ClientLevel world;
   private final Map<ParticleRenderType, Queue<Particle>> byType = Maps.newIdentityHashMap();
   //private final LinkedHashMap<TextureAtlasSprite, Map<IParticleRenderType, Queue<Particle>>> byType = new LinkedHashMap<>();
   private final Queue<TrackingEmitter> particleEmitters = Queues.newArrayDeque();
   private final TextureManager renderer;
   private final Random rand = new Random();
   private final Map<ResourceLocation, ParticleProvider<?>> factories = new java.util.HashMap<>();
   private final Queue<Particle> queue = Queues.newArrayDeque();
   private final Map<ResourceLocation, ParticleManagerExtended.AnimatedSpriteImpl> sprites = Maps.newHashMap();
   private final TextureAtlas atlas = new TextureAtlas(TextureAtlas.LOCATION_PARTICLES);

   public ParticleManagerExtended(ClientLevel world, TextureManager textureManager) {
      //textureManager.loadTexture(this.atlas.getTextureLocation(), this.atlas);
      this.world = world;
      this.renderer = textureManager;
      this.registerFactories();
      //this.setupRenderOrders();
   }

   /*private void setupRenderOrders() {
      addRenderLayer(ParticleRegistry.downfall3);
      addRenderLayer(ParticleRegistry.cloud256_6);
      addRenderLayer(ParticleRegistry.rain_white);
   }

   private void addRenderLayer(TextureAtlasSprite sprite) {
      byType.computeIfAbsent(sprite, (texture) -> {
         Map<IParticleRenderType, Queue<Particle>> byType2 = Maps.newIdentityHashMap();
         return byType2;
      });
   }*/

   private void registerFactories() {
      this.registerFactory(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobProvider::new);
      this.registerFactory(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerProvider::new);
      this.registerFactory(ParticleTypes.BARRIER, new BarrierParticle.Provider());
      this.registerFactory(ParticleTypes.BLOCK, new TerrainParticle.Provider());
      this.registerFactory(ParticleTypes.BUBBLE, BubbleParticle.Provider::new);
      this.registerFactory(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Provider::new);
      this.registerFactory(ParticleTypes.BUBBLE_POP, BubblePopParticle.Provider::new);
      this.registerFactory(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireSmokeParticle.CosyProvider::new);
      this.registerFactory(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireSmokeParticle.SignalProvider::new);
      this.registerFactory(ParticleTypes.CLOUD, PlayerCloudParticle.Provider::new);
      this.registerFactory(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFillProvider::new);
      this.registerFactory(ParticleTypes.CRIT, CritParticle.Provider::new);
      this.registerFactory(ParticleTypes.CURRENT_DOWN, WaterCurrentDownParticle.Provider::new);
      this.registerFactory(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorProvider::new);
      this.registerFactory(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Provider::new);
      this.registerFactory(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedProvider::new);
      this.registerFactory(ParticleTypes.DRIPPING_LAVA, DripParticle.LavaHangProvider::new);
      this.registerFactory(ParticleTypes.FALLING_LAVA, DripParticle.LavaFallProvider::new);
      this.registerFactory(ParticleTypes.LANDING_LAVA, DripParticle.LavaLandProvider::new);
      this.registerFactory(ParticleTypes.DRIPPING_WATER, DripParticle.WaterHangProvider::new);
      this.registerFactory(ParticleTypes.FALLING_WATER, DripParticle.WaterFallProvider::new);
      this.registerFactory(ParticleTypes.DUST, DustParticle.Provider::new);
      this.registerFactory(ParticleTypes.EFFECT, SpellParticle.Provider::new);
      this.registerFactory(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Provider());
      this.registerFactory(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicProvider::new);
      this.registerFactory(ParticleTypes.ENCHANT, EnchantmentTableParticle.Provider::new);
      this.registerFactory(ParticleTypes.END_ROD, EndRodParticle.Provider::new);
      this.registerFactory(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobProvider::new);
      this.registerFactory(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionSeedParticle.Provider());
      this.registerFactory(ParticleTypes.EXPLOSION, HugeExplosionParticle.Provider::new);
      this.registerFactory(ParticleTypes.FALLING_DUST, FallingDustParticle.Provider::new);
      this.registerFactory(ParticleTypes.FIREWORK, FireworkParticles.SparkProvider::new);
      this.registerFactory(ParticleTypes.FISHING, WakeParticle.Provider::new);
      this.registerFactory(ParticleTypes.FLAME, FlameParticle.Provider::new);
      this.registerFactory(ParticleTypes.SOUL, SoulParticle.Provider::new);
      this.registerFactory(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Provider::new);
      this.registerFactory(ParticleTypes.FLASH, FireworkParticles.FlashProvider::new);
      this.registerFactory(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerProvider::new);
      this.registerFactory(ParticleTypes.HEART, HeartParticle.Provider::new);
      this.registerFactory(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantProvider::new);
      this.registerFactory(ParticleTypes.ITEM, new BreakingItemParticle.Provider());
      this.registerFactory(ParticleTypes.ITEM_SLIME, new BreakingItemParticle.SlimeProvider());
      this.registerFactory(ParticleTypes.ITEM_SNOWBALL, new BreakingItemParticle.SnowballProvider());
      this.registerFactory(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Provider::new);
      this.registerFactory(ParticleTypes.LAVA, LavaParticle.Provider::new);
      this.registerFactory(ParticleTypes.MYCELIUM, SuspendedTownParticle.Provider::new);
      this.registerFactory(ParticleTypes.NAUTILUS, EnchantmentTableParticle.NautilusProvider::new);
      this.registerFactory(ParticleTypes.NOTE, NoteParticle.Provider::new);
      this.registerFactory(ParticleTypes.POOF, ExplodeParticle.Provider::new);
      this.registerFactory(ParticleTypes.PORTAL, PortalParticle.Provider::new);
      this.registerFactory(ParticleTypes.RAIN, WaterDropParticle.Provider::new);
      this.registerFactory(ParticleTypes.SMOKE, SmokeParticle.Provider::new);
      this.registerFactory(ParticleTypes.SNEEZE, PlayerCloudParticle.SneezeProvider::new);
      this.registerFactory(ParticleTypes.SPIT, SpitParticle.Provider::new);
      this.registerFactory(ParticleTypes.SWEEP_ATTACK, AttackSweepParticle.Provider::new);
      this.registerFactory(ParticleTypes.TOTEM_OF_UNDYING, TotemParticle.Provider::new);
      this.registerFactory(ParticleTypes.SQUID_INK, SquidInkParticle.Provider::new);
      this.registerFactory(ParticleTypes.UNDERWATER, SuspendedParticle.UnderwaterProvider::new);
      this.registerFactory(ParticleTypes.SPLASH, SplashParticle.Provider::new);
      this.registerFactory(ParticleTypes.WITCH, SpellParticle.WitchProvider::new);
      this.registerFactory(ParticleTypes.DRIPPING_HONEY, DripParticle.HoneyHangProvider::new);
      this.registerFactory(ParticleTypes.FALLING_HONEY, DripParticle.HoneyFallProvider::new);
      this.registerFactory(ParticleTypes.LANDING_HONEY, DripParticle.HoneyLandProvider::new);
      this.registerFactory(ParticleTypes.FALLING_NECTAR, DripParticle.NectarFallProvider::new);
      this.registerFactory(ParticleTypes.ASH, AshParticle.Provider::new);
      this.registerFactory(ParticleTypes.CRIMSON_SPORE, SuspendedParticle.CrimsonSporeProvider::new);
      this.registerFactory(ParticleTypes.WARPED_SPORE, SuspendedParticle.WarpedSporeProvider::new);
      this.registerFactory(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle.ObsidianTearHangProvider::new);
      this.registerFactory(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle.ObsidianTearFallProvider::new);
      this.registerFactory(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle.ObsidianTearLandProvider::new);
      this.registerFactory(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.ReversePortalProvider::new);
      this.registerFactory(ParticleTypes.WHITE_ASH, WhiteAshParticle.Provider::new);
   }

   public <T extends ParticleOptions> void registerFactory(ParticleType<T> particleTypeIn, ParticleProvider<T> particleFactoryIn) {
      this.factories.put(Registry.PARTICLE_TYPE.getKey(particleTypeIn), particleFactoryIn);
   }

   public <T extends ParticleOptions> void registerFactory(ParticleType<T> particleTypeIn, ParticleManagerExtended.IParticleMetaFactory<T> particleMetaFactoryIn) {
      ParticleManagerExtended.AnimatedSpriteImpl particlemanager$animatedspriteimpl = new ParticleManagerExtended.AnimatedSpriteImpl();
      this.sprites.put(Registry.PARTICLE_TYPE.getKey(particleTypeIn), particlemanager$animatedspriteimpl);
      this.factories.put(Registry.PARTICLE_TYPE.getKey(particleTypeIn), particleMetaFactoryIn.create(particlemanager$animatedspriteimpl));
   }

   public CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
      Map<ResourceLocation, List<ResourceLocation>> map = Maps.newConcurrentMap();
      CompletableFuture<?>[] completablefuture = Registry.PARTICLE_TYPE.keySet().stream().map((particleTextureID) -> {
         return CompletableFuture.runAsync(() -> {
            this.loadTextureLists(resourceManager, particleTextureID, map);
         }, backgroundExecutor);
      }).toArray((size) -> {
         return new CompletableFuture[size];
      });
      return CompletableFuture.allOf(completablefuture).thenApplyAsync((voidIn) -> {
         preparationsProfiler.startTick();
         preparationsProfiler.push("stitching");
         TextureAtlas.Preparations atlastexture$sheetdata = this.atlas.prepareToStitch(resourceManager, map.values().stream().flatMap(Collection::stream), preparationsProfiler, 0);
         preparationsProfiler.pop();
         preparationsProfiler.endTick();
         return atlastexture$sheetdata;
      }, backgroundExecutor).thenCompose(stage::wait).thenAcceptAsync((sheetData) -> {
         this.byType.clear();
         reloadProfiler.startTick();
         reloadProfiler.push("upload");
         this.atlas.reload(sheetData);
         reloadProfiler.popPush("bindSpriteSets");
         TextureAtlasSprite textureatlassprite = this.atlas.getSprite(MissingTextureAtlasSprite.getLocation());
         map.forEach((particleTextureID, particleTextures) -> {
            ImmutableList<TextureAtlasSprite> immutablelist = particleTextures.isEmpty() ? ImmutableList.of(textureatlassprite) : particleTextures.stream().map(this.atlas::getSprite).collect(ImmutableList.toImmutableList());
            this.sprites.get(particleTextureID).setSprites(immutablelist);
         });
         reloadProfiler.pop();
         reloadProfiler.endTick();
      }, gameExecutor);
   }

   public void close() {
      this.atlas.clearTextureData();
   }

   private void loadTextureLists(ResourceManager manager, ResourceLocation particleId, Map<ResourceLocation, List<ResourceLocation>> textures) {
      ResourceLocation resourcelocation = new ResourceLocation(particleId.getNamespace(), "particles/" + particleId.getPath() + ".json");

      try (
         Resource iresource = manager.getResource(resourcelocation);
         Reader reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);
      ) {
         ParticleDescription texturesparticle = ParticleDescription.fromJson(GsonHelper.parse(reader));
         List<ResourceLocation> list = texturesparticle.getTextures();
         boolean flag = this.sprites.containsKey(particleId);
         if (list == null) {
            if (flag) {
               throw new IllegalStateException("Missing texture list for particle " + particleId);
            }
         } else {
            if (!flag) {
               throw new IllegalStateException("Redundant texture list for particle " + particleId);
            }

            textures.put(particleId, list.stream().map((particleTextureID) -> {
               return new ResourceLocation(particleTextureID.getNamespace(), "particle/" + particleTextureID.getPath());
            }).collect(Collectors.toList()));
         }

      } catch (IOException ioexception) {
         throw new IllegalStateException("Failed to load description for particle " + particleId, ioexception);
      }
   }

   public void addParticleEmitter(Entity entityIn, ParticleOptions particleData) {
      this.particleEmitters.add(new TrackingEmitter(this.world, entityIn, particleData));
   }

   public void emitParticleAtEntity(Entity entityIn, ParticleOptions dataIn, int lifetimeIn) {
      this.particleEmitters.add(new TrackingEmitter(this.world, entityIn, dataIn, lifetimeIn));
   }

   @Nullable
   public Particle addParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      Particle particle = this.makeParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
      if (particle != null) {
         this.addEffect(particle);
         return particle;
      } else {
         return null;
      }
   }

   @Nullable
   private <T extends ParticleOptions> Particle makeParticle(T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      ParticleProvider<T> iparticlefactory = (ParticleProvider<T>)this.factories.get(Registry.PARTICLE_TYPE.getKey(particleData.getType()));
      return iparticlefactory == null ? null : iparticlefactory.createParticle(particleData, this.world, x, y, z, xSpeed, ySpeed, zSpeed);
   }

   public void addEffect(Particle effect) {
      this.queue.add(effect);
   }

   public void tick() {
      this.byType.forEach((renderType, particleQueue) -> {
         this.world.getProfiler().push(renderType.toString());
         this.tickParticleList(particleQueue);
         this.world.getProfiler().pop();
      });
      if (!this.particleEmitters.isEmpty()) {
         List<TrackingEmitter> list = Lists.newArrayList();

         for(TrackingEmitter emitterparticle : this.particleEmitters) {
            emitterparticle.tick();
            if (!emitterparticle.isAlive()) {
               list.add(emitterparticle);
            }
         }

         this.particleEmitters.removeAll(list);
      }

      Particle particle;
      if (!this.queue.isEmpty()) {
         while((particle = this.queue.poll()) != null) {
            this.byType.computeIfAbsent(particle.getRenderType(), (renderType) -> {
               return EvictingQueue.create(16384);
            }).add(particle);
         }
      }

   }

   private void tickParticleList(Collection<Particle> particlesIn) {
      if (!particlesIn.isEmpty()) {
         Iterator<Particle> iterator = particlesIn.iterator();

         while(iterator.hasNext()) {
            Particle particle = iterator.next();
            this.tickParticle(particle);
            if (!particle.isAlive()) {
               iterator.remove();
            }
         }
      }

   }

   private void tickParticle(Particle particle) {
      try {
         particle.tick();
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking Particle");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being ticked");
         crashreportcategory.setDetail("Particle", particle::toString);
         crashreportcategory.setDetail("Particle Type", particle.getRenderType()::toString);
         throw new ReportedException(crashreport);
      }
   }

   /**@deprecated Forge: use {@link #renderParticles(MatrixStack, IRenderTypeBuffer.Impl, LightTexture, ActiveRenderInfo, float, net.minecraft.client.renderer.culling.ClippingHelper)} with ClippingHelper as additional parameter*/
   @Deprecated
   public void renderParticles(PoseStack matrixStackIn, MultiBufferSource.BufferSource bufferIn, LightTexture lightTextureIn, Camera activeRenderInfoIn, float partialTicks) {
      renderParticles(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, null);
   }

   public void renderParticles(PoseStack matrixStackIn, MultiBufferSource.BufferSource bufferIn, LightTexture lightTextureIn, Camera activeRenderInfoIn, float partialTicks, @Nullable net.minecraft.client.renderer.culling.Frustum clippingHelper) {
      lightTextureIn.turnOnLightLayer();
      Runnable enable = () -> {
         RenderSystem.enableAlphaTest();
         RenderSystem.defaultAlphaFunc();
         RenderSystem.enableDepthTest();
         RenderSystem.enableFog();
         //RenderSystem.depthMask(false);
         RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE2);
         RenderSystem.enableTexture();
         RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
      };
      RenderSystem.pushMatrix();
      RenderSystem.multMatrix(matrixStackIn.last().pose());

      GlStateManager._disableCull();

      //temp?
      //enable.run();



      for(ParticleRenderType iparticlerendertype : this.byType.keySet()) { // Forge: allow custom IParticleRenderType's
         if (iparticlerendertype == ParticleRenderType.NO_RENDER) continue;
         enable.run(); //Forge: MC-168672 Make sure all render types have the correct GL state.
         Iterable<Particle> iterable = byType.get(iparticlerendertype);
         if (iterable != null) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            iparticlerendertype.begin(bufferbuilder, this.renderer);

            RenderSystem.depthMask(false);

            for(Particle particle : iterable) {
               if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isVisible(particle.getBoundingBox())) continue;
               try {
                  particle.render(bufferbuilder, activeRenderInfoIn, partialTicks);
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                  crashreportcategory.setDetail("Particle", particle::toString);
                  crashreportcategory.setDetail("Particle Type", iparticlerendertype::toString);
                  throw new ReportedException(crashreport);
               }
            }

            iparticlerendertype.end(tessellator);
         }
      }

      boolean extraRenderPass = Minecraft.useShaderTransparency();

      if (extraRenderPass) {
         GL11.glColorMask(false, false, false, false);

         for (ParticleRenderType iparticlerendertype : this.byType.keySet()) { // Forge: allow custom IParticleRenderType's
            if (iparticlerendertype == ParticleRenderType.NO_RENDER) continue;
            enable.run(); //Forge: MC-168672 Make sure all render types have the correct GL state.
            Iterable<Particle> iterable = byType.get(iparticlerendertype);
            if (iterable != null) {
               RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               Tesselator tessellator = Tesselator.getInstance();
               BufferBuilder bufferbuilder = tessellator.getBuilder();
               iparticlerendertype.begin(bufferbuilder, this.renderer);

               RenderSystem.depthMask(true);

               for (Particle particle : iterable) {
                  if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isVisible(particle.getBoundingBox()))
                     continue;
                  try {
                     particle.render(bufferbuilder, activeRenderInfoIn, partialTicks);
                  } catch (Throwable throwable) {
                     CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Particle");
                     CrashReportCategory crashreportcategory = crashreport.addCategory("Particle being rendered");
                     crashreportcategory.setDetail("Particle", particle::toString);
                     crashreportcategory.setDetail("Particle Type", iparticlerendertype::toString);
                     throw new ReportedException(crashreport);
                  }
               }

               iparticlerendertype.end(tessellator);
            }
         }

         GL11.glColorMask(true, true, true, true);
      }

      GlStateManager._enableCull();

      RenderSystem.popMatrix();
      RenderSystem.depthMask(true);
      RenderSystem.depthFunc(515);
      RenderSystem.disableBlend();
      RenderSystem.defaultAlphaFunc();
      lightTextureIn.turnOffLightLayer();
      RenderSystem.disableFog();
   }

   public void clearEffects(@Nullable ClientLevel worldIn) {
      this.world = worldIn;
      this.byType.clear();
      this.particleEmitters.clear();
   }

   /**
    * Adds block hit particles for the specified block
    */
   public void addBlockHitEffects(BlockPos pos, Direction side) {
      BlockState blockstate = this.world.getBlockState(pos);
      if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
         int i = pos.getX();
         int j = pos.getY();
         int k = pos.getZ();
         float f = 0.1F;
         AABB axisalignedbb = blockstate.getShape(this.world, pos).bounds();
         double d0 = (double)i + this.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - (double)0.2F) + (double)0.1F + axisalignedbb.minX;
         double d1 = (double)j + this.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - (double)0.2F) + (double)0.1F + axisalignedbb.minY;
         double d2 = (double)k + this.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - (double)0.2F) + (double)0.1F + axisalignedbb.minZ;
         if (side == Direction.DOWN) {
            d1 = (double)j + axisalignedbb.minY - (double)0.1F;
         }

         if (side == Direction.UP) {
            d1 = (double)j + axisalignedbb.maxY + (double)0.1F;
         }

         if (side == Direction.NORTH) {
            d2 = (double)k + axisalignedbb.minZ - (double)0.1F;
         }

         if (side == Direction.SOUTH) {
            d2 = (double)k + axisalignedbb.maxZ + (double)0.1F;
         }

         if (side == Direction.WEST) {
            d0 = (double)i + axisalignedbb.minX - (double)0.1F;
         }

         if (side == Direction.EAST) {
            d0 = (double)i + axisalignedbb.maxX + (double)0.1F;
         }

         this.addEffect((new TerrainParticle(this.world, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate)).init(pos).setPower(0.2F).scale(0.6F));
      }
   }

   public String getStatistics() {
      return "no";
   }

   @OnlyIn(Dist.CLIENT)
   class AnimatedSpriteImpl implements SpriteSet {
      private List<TextureAtlasSprite> sprites;

      private AnimatedSpriteImpl() {
      }

      public TextureAtlasSprite get(int particleAge, int particleMaxAge) {
         return this.sprites.get(particleAge * (this.sprites.size() - 1) / particleMaxAge);
      }

      public TextureAtlasSprite get(Random rand) {
         return this.sprites.get(rand.nextInt(this.sprites.size()));
      }

      public void setSprites(List<TextureAtlasSprite> sprites) {
         this.sprites = ImmutableList.copyOf(sprites);
      }
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   public interface IParticleMetaFactory<T extends ParticleOptions> {
      ParticleProvider<T> create(SpriteSet p_create_1_);
   }
}
