package extendedrenderer;

import com.google.common.base.Charsets;
import com.google.common.collect.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
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

@OnlyIn(Dist.CLIENT)
public class ParticleManagerExtended implements IFutureReloadListener {
   private static final List<IParticleRenderType> TYPES = ImmutableList.of(IParticleRenderType.TERRAIN_SHEET, IParticleRenderType.PARTICLE_SHEET_OPAQUE, IParticleRenderType.PARTICLE_SHEET_LIT, IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT, IParticleRenderType.CUSTOM);
   protected ClientWorld world;
   private final Map<IParticleRenderType, Queue<Particle>> byType = Maps.newIdentityHashMap();
   //private final LinkedHashMap<TextureAtlasSprite, Map<IParticleRenderType, Queue<Particle>>> byType = new LinkedHashMap<>();
   private final Queue<EmitterParticle> particleEmitters = Queues.newArrayDeque();
   private final TextureManager renderer;
   private final Random rand = new Random();
   private final Map<ResourceLocation, IParticleFactory<?>> factories = new java.util.HashMap<>();
   private final Queue<Particle> queue = Queues.newArrayDeque();
   private final Map<ResourceLocation, ParticleManagerExtended.AnimatedSpriteImpl> sprites = Maps.newHashMap();
   private final AtlasTexture atlas = new AtlasTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);

   public ParticleManagerExtended(ClientWorld world, TextureManager textureManager) {
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
      this.registerFactory(ParticleTypes.AMBIENT_ENTITY_EFFECT, SpellParticle.AmbientMobFactory::new);
      this.registerFactory(ParticleTypes.ANGRY_VILLAGER, HeartParticle.AngryVillagerFactory::new);
      this.registerFactory(ParticleTypes.BARRIER, new BarrierParticle.Factory());
      this.registerFactory(ParticleTypes.BLOCK, new DiggingParticle.Factory());
      this.registerFactory(ParticleTypes.BUBBLE, BubbleParticle.Factory::new);
      this.registerFactory(ParticleTypes.BUBBLE_COLUMN_UP, BubbleColumnUpParticle.Factory::new);
      this.registerFactory(ParticleTypes.BUBBLE_POP, BubblePopParticle.Factory::new);
      this.registerFactory(ParticleTypes.CAMPFIRE_COSY_SMOKE, CampfireParticle.CozySmokeFactory::new);
      this.registerFactory(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, CampfireParticle.SignalSmokeFactory::new);
      this.registerFactory(ParticleTypes.CLOUD, CloudParticle.Factory::new);
      this.registerFactory(ParticleTypes.COMPOSTER, SuspendedTownParticle.ComposterFactory::new);
      this.registerFactory(ParticleTypes.CRIT, CritParticle.Factory::new);
      this.registerFactory(ParticleTypes.CURRENT_DOWN, CurrentDownParticle.Factory::new);
      this.registerFactory(ParticleTypes.DAMAGE_INDICATOR, CritParticle.DamageIndicatorFactory::new);
      this.registerFactory(ParticleTypes.DRAGON_BREATH, DragonBreathParticle.Factory::new);
      this.registerFactory(ParticleTypes.DOLPHIN, SuspendedTownParticle.DolphinSpeedFactory::new);
      this.registerFactory(ParticleTypes.DRIPPING_LAVA, DripParticle.DrippingLavaFactory::new);
      this.registerFactory(ParticleTypes.FALLING_LAVA, DripParticle.FallingLavaFactory::new);
      this.registerFactory(ParticleTypes.LANDING_LAVA, DripParticle.LandingLavaFactory::new);
      this.registerFactory(ParticleTypes.DRIPPING_WATER, DripParticle.DrippingWaterFactory::new);
      this.registerFactory(ParticleTypes.FALLING_WATER, DripParticle.FallingWaterFactory::new);
      this.registerFactory(ParticleTypes.DUST, RedstoneParticle.Factory::new);
      this.registerFactory(ParticleTypes.EFFECT, SpellParticle.Factory::new);
      this.registerFactory(ParticleTypes.ELDER_GUARDIAN, new MobAppearanceParticle.Factory());
      this.registerFactory(ParticleTypes.ENCHANTED_HIT, CritParticle.MagicFactory::new);
      this.registerFactory(ParticleTypes.ENCHANT, EnchantmentTableParticle.EnchantmentTable::new);
      this.registerFactory(ParticleTypes.END_ROD, EndRodParticle.Factory::new);
      this.registerFactory(ParticleTypes.ENTITY_EFFECT, SpellParticle.MobFactory::new);
      this.registerFactory(ParticleTypes.EXPLOSION_EMITTER, new HugeExplosionParticle.Factory());
      this.registerFactory(ParticleTypes.EXPLOSION, LargeExplosionParticle.Factory::new);
      this.registerFactory(ParticleTypes.FALLING_DUST, FallingDustParticle.Factory::new);
      this.registerFactory(ParticleTypes.FIREWORK, FireworkParticle.SparkFactory::new);
      this.registerFactory(ParticleTypes.FISHING, WaterWakeParticle.Factory::new);
      this.registerFactory(ParticleTypes.FLAME, FlameParticle.Factory::new);
      this.registerFactory(ParticleTypes.SOUL, SoulParticle.Factory::new);
      this.registerFactory(ParticleTypes.SOUL_FIRE_FLAME, FlameParticle.Factory::new);
      this.registerFactory(ParticleTypes.FLASH, FireworkParticle.OverlayFactory::new);
      this.registerFactory(ParticleTypes.HAPPY_VILLAGER, SuspendedTownParticle.HappyVillagerFactory::new);
      this.registerFactory(ParticleTypes.HEART, HeartParticle.Factory::new);
      this.registerFactory(ParticleTypes.INSTANT_EFFECT, SpellParticle.InstantFactory::new);
      this.registerFactory(ParticleTypes.ITEM, new BreakingParticle.Factory());
      this.registerFactory(ParticleTypes.ITEM_SLIME, new BreakingParticle.SlimeFactory());
      this.registerFactory(ParticleTypes.ITEM_SNOWBALL, new BreakingParticle.SnowballFactory());
      this.registerFactory(ParticleTypes.LARGE_SMOKE, LargeSmokeParticle.Factory::new);
      this.registerFactory(ParticleTypes.LAVA, LavaParticle.Factory::new);
      this.registerFactory(ParticleTypes.MYCELIUM, SuspendedTownParticle.Factory::new);
      this.registerFactory(ParticleTypes.NAUTILUS, EnchantmentTableParticle.NautilusFactory::new);
      this.registerFactory(ParticleTypes.NOTE, NoteParticle.Factory::new);
      this.registerFactory(ParticleTypes.POOF, PoofParticle.Factory::new);
      this.registerFactory(ParticleTypes.PORTAL, PortalParticle.Factory::new);
      this.registerFactory(ParticleTypes.RAIN, RainParticle.Factory::new);
      this.registerFactory(ParticleTypes.SMOKE, SmokeParticle.Factory::new);
      this.registerFactory(ParticleTypes.SNEEZE, CloudParticle.SneezeFactory::new);
      this.registerFactory(ParticleTypes.SPIT, SpitParticle.Factory::new);
      this.registerFactory(ParticleTypes.SWEEP_ATTACK, SweepAttackParticle.Factory::new);
      this.registerFactory(ParticleTypes.TOTEM_OF_UNDYING, TotemOfUndyingParticle.Factory::new);
      this.registerFactory(ParticleTypes.SQUID_INK, SquidInkParticle.Factory::new);
      this.registerFactory(ParticleTypes.UNDERWATER, UnderwaterParticle.UnderwaterFactory::new);
      this.registerFactory(ParticleTypes.SPLASH, SplashParticle.Factory::new);
      this.registerFactory(ParticleTypes.WITCH, SpellParticle.WitchFactory::new);
      this.registerFactory(ParticleTypes.DRIPPING_HONEY, DripParticle.DrippingHoneyFactory::new);
      this.registerFactory(ParticleTypes.FALLING_HONEY, DripParticle.FallingHoneyFactory::new);
      this.registerFactory(ParticleTypes.LANDING_HONEY, DripParticle.LandingHoneyFactory::new);
      this.registerFactory(ParticleTypes.FALLING_NECTAR, DripParticle.FallingNectarFactory::new);
      this.registerFactory(ParticleTypes.ASH, AshParticle.Factory::new);
      this.registerFactory(ParticleTypes.CRIMSON_SPORE, UnderwaterParticle.CrimsonSporeFactory::new);
      this.registerFactory(ParticleTypes.WARPED_SPORE, UnderwaterParticle.WarpedSporeFactory::new);
      this.registerFactory(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, DripParticle.DrippingObsidianTearFactory::new);
      this.registerFactory(ParticleTypes.FALLING_OBSIDIAN_TEAR, DripParticle.FallingObsidianTearFactory::new);
      this.registerFactory(ParticleTypes.LANDING_OBSIDIAN_TEAR, DripParticle.LandingObsidianTearFactory::new);
      this.registerFactory(ParticleTypes.REVERSE_PORTAL, ReversePortalParticle.Factory::new);
      this.registerFactory(ParticleTypes.WHITE_ASH, WhiteAshParticle.Factory::new);
   }

   public <T extends IParticleData> void registerFactory(ParticleType<T> particleTypeIn, IParticleFactory<T> particleFactoryIn) {
      this.factories.put(Registry.PARTICLE_TYPE.getKey(particleTypeIn), particleFactoryIn);
   }

   public <T extends IParticleData> void registerFactory(ParticleType<T> particleTypeIn, ParticleManagerExtended.IParticleMetaFactory<T> particleMetaFactoryIn) {
      ParticleManagerExtended.AnimatedSpriteImpl particlemanager$animatedspriteimpl = new ParticleManagerExtended.AnimatedSpriteImpl();
      this.sprites.put(Registry.PARTICLE_TYPE.getKey(particleTypeIn), particlemanager$animatedspriteimpl);
      this.factories.put(Registry.PARTICLE_TYPE.getKey(particleTypeIn), particleMetaFactoryIn.create(particlemanager$animatedspriteimpl));
   }

   public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
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
         preparationsProfiler.startSection("stitching");
         AtlasTexture.SheetData atlastexture$sheetdata = this.atlas.stitch(resourceManager, map.values().stream().flatMap(Collection::stream), preparationsProfiler, 0);
         preparationsProfiler.endSection();
         preparationsProfiler.endTick();
         return atlastexture$sheetdata;
      }, backgroundExecutor).thenCompose(stage::markCompleteAwaitingOthers).thenAcceptAsync((sheetData) -> {
         this.byType.clear();
         reloadProfiler.startTick();
         reloadProfiler.startSection("upload");
         this.atlas.upload(sheetData);
         reloadProfiler.endStartSection("bindSpriteSets");
         TextureAtlasSprite textureatlassprite = this.atlas.getSprite(MissingTextureSprite.getLocation());
         map.forEach((particleTextureID, particleTextures) -> {
            ImmutableList<TextureAtlasSprite> immutablelist = particleTextures.isEmpty() ? ImmutableList.of(textureatlassprite) : particleTextures.stream().map(this.atlas::getSprite).collect(ImmutableList.toImmutableList());
            this.sprites.get(particleTextureID).setSprites(immutablelist);
         });
         reloadProfiler.endSection();
         reloadProfiler.endTick();
      }, gameExecutor);
   }

   public void close() {
      this.atlas.clear();
   }

   private void loadTextureLists(IResourceManager manager, ResourceLocation particleId, Map<ResourceLocation, List<ResourceLocation>> textures) {
      ResourceLocation resourcelocation = new ResourceLocation(particleId.getNamespace(), "particles/" + particleId.getPath() + ".json");

      try (
         IResource iresource = manager.getResource(resourcelocation);
         Reader reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);
      ) {
         TexturesParticle texturesparticle = TexturesParticle.deserialize(JSONUtils.fromJson(reader));
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

   public void addParticleEmitter(Entity entityIn, IParticleData particleData) {
      this.particleEmitters.add(new EmitterParticle(this.world, entityIn, particleData));
   }

   public void emitParticleAtEntity(Entity entityIn, IParticleData dataIn, int lifetimeIn) {
      this.particleEmitters.add(new EmitterParticle(this.world, entityIn, dataIn, lifetimeIn));
   }

   @Nullable
   public Particle addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      Particle particle = this.makeParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
      if (particle != null) {
         this.addEffect(particle);
         return particle;
      } else {
         return null;
      }
   }

   @Nullable
   private <T extends IParticleData> Particle makeParticle(T particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      IParticleFactory<T> iparticlefactory = (IParticleFactory<T>)this.factories.get(Registry.PARTICLE_TYPE.getKey(particleData.getType()));
      return iparticlefactory == null ? null : iparticlefactory.makeParticle(particleData, this.world, x, y, z, xSpeed, ySpeed, zSpeed);
   }

   public void addEffect(Particle effect) {
      this.queue.add(effect);
   }

   public void tick() {
      this.byType.forEach((renderType, particleQueue) -> {
         this.world.getProfiler().startSection(renderType.toString());
         this.tickParticleList(particleQueue);
         this.world.getProfiler().endSection();
      });
      if (!this.particleEmitters.isEmpty()) {
         List<EmitterParticle> list = Lists.newArrayList();

         for(EmitterParticle emitterparticle : this.particleEmitters) {
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
         CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Particle");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
         crashreportcategory.addDetail("Particle", particle::toString);
         crashreportcategory.addDetail("Particle Type", particle.getRenderType()::toString);
         throw new ReportedException(crashreport);
      }
   }

   /**@deprecated Forge: use {@link #renderParticles(MatrixStack, IRenderTypeBuffer.Impl, LightTexture, ActiveRenderInfo, float, net.minecraft.client.renderer.culling.ClippingHelper)} with ClippingHelper as additional parameter*/
   @Deprecated
   public void renderParticles(MatrixStack matrixStackIn, IRenderTypeBuffer.Impl bufferIn, LightTexture lightTextureIn, ActiveRenderInfo activeRenderInfoIn, float partialTicks) {
      renderParticles(matrixStackIn, bufferIn, lightTextureIn, activeRenderInfoIn, partialTicks, null);
   }

   public void renderParticles(MatrixStack matrixStackIn, IRenderTypeBuffer.Impl bufferIn, LightTexture lightTextureIn, ActiveRenderInfo activeRenderInfoIn, float partialTicks, @Nullable net.minecraft.client.renderer.culling.ClippingHelper clippingHelper) {
      lightTextureIn.enableLightmap();
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
      RenderSystem.multMatrix(matrixStackIn.getLast().getMatrix());

      GlStateManager.disableCull();

      //temp?
      //enable.run();



      for(IParticleRenderType iparticlerendertype : this.byType.keySet()) { // Forge: allow custom IParticleRenderType's
         if (iparticlerendertype == IParticleRenderType.NO_RENDER) continue;
         enable.run(); //Forge: MC-168672 Make sure all render types have the correct GL state.
         Iterable<Particle> iterable = byType.get(iparticlerendertype);
         if (iterable != null) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            iparticlerendertype.beginRender(bufferbuilder, this.renderer);

            RenderSystem.depthMask(false);

            for(Particle particle : iterable) {
               if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isBoundingBoxInFrustum(particle.getBoundingBox())) continue;
               try {
                  particle.renderParticle(bufferbuilder, activeRenderInfoIn, partialTicks);
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                  CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                  crashreportcategory.addDetail("Particle", particle::toString);
                  crashreportcategory.addDetail("Particle Type", iparticlerendertype::toString);
                  throw new ReportedException(crashreport);
               }
            }

            iparticlerendertype.finishRender(tessellator);
         }
      }

      boolean extraRenderPass = Minecraft.isFabulousGraphicsEnabled();

      if (extraRenderPass) {
         GL11.glColorMask(false, false, false, false);

         for (IParticleRenderType iparticlerendertype : this.byType.keySet()) { // Forge: allow custom IParticleRenderType's
            if (iparticlerendertype == IParticleRenderType.NO_RENDER) continue;
            enable.run(); //Forge: MC-168672 Make sure all render types have the correct GL state.
            Iterable<Particle> iterable = byType.get(iparticlerendertype);
            if (iterable != null) {
               RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
               Tessellator tessellator = Tessellator.getInstance();
               BufferBuilder bufferbuilder = tessellator.getBuffer();
               iparticlerendertype.beginRender(bufferbuilder, this.renderer);

               RenderSystem.depthMask(true);

               for (Particle particle : iterable) {
                  if (clippingHelper != null && particle.shouldCull() && !clippingHelper.isBoundingBoxInFrustum(particle.getBoundingBox()))
                     continue;
                  try {
                     particle.renderParticle(bufferbuilder, activeRenderInfoIn, partialTicks);
                  } catch (Throwable throwable) {
                     CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                     CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                     crashreportcategory.addDetail("Particle", particle::toString);
                     crashreportcategory.addDetail("Particle Type", iparticlerendertype::toString);
                     throw new ReportedException(crashreport);
                  }
               }

               iparticlerendertype.finishRender(tessellator);
            }
         }

         GL11.glColorMask(true, true, true, true);
      }

      GlStateManager.enableCull();

      RenderSystem.popMatrix();
      RenderSystem.depthMask(true);
      RenderSystem.depthFunc(515);
      RenderSystem.disableBlend();
      RenderSystem.defaultAlphaFunc();
      lightTextureIn.disableLightmap();
      RenderSystem.disableFog();
   }

   public void clearEffects(@Nullable ClientWorld worldIn) {
      this.world = worldIn;
      this.byType.clear();
      this.particleEmitters.clear();
   }

   /**
    * Adds block hit particles for the specified block
    */
   public void addBlockHitEffects(BlockPos pos, Direction side) {
      BlockState blockstate = this.world.getBlockState(pos);
      if (blockstate.getRenderType() != BlockRenderType.INVISIBLE) {
         int i = pos.getX();
         int j = pos.getY();
         int k = pos.getZ();
         float f = 0.1F;
         AxisAlignedBB axisalignedbb = blockstate.getShape(this.world, pos).getBoundingBox();
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

         this.addEffect((new DiggingParticle(this.world, d0, d1, d2, 0.0D, 0.0D, 0.0D, blockstate)).setBlockPos(pos).multiplyVelocity(0.2F).multiplyParticleScaleBy(0.6F));
      }
   }

   public String getStatistics() {
      return "no";
   }

   @OnlyIn(Dist.CLIENT)
   class AnimatedSpriteImpl implements IAnimatedSprite {
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
   public interface IParticleMetaFactory<T extends IParticleData> {
      IParticleFactory<T> create(IAnimatedSprite p_create_1_);
   }
}
