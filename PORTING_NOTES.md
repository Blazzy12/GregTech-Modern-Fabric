# GregTech CEu Modern → Minecraft 26.3 Fabric: porting notes

Source base: `source_code/GregTech-Modern-latest-1.21` (NeoForge 1.21.1, GTCEu 8.0.0-SNAPSHOT).
The 1.20.1 tree is older and not used as a base.

## Toolchain (verified working 2026-10-29)

| Thing | Version |
|---|---|
| Minecraft | 26.3 (latest release; 26.4 is in snapshots) |
| Fabric Loader | 0.20.1 |
| Fabric API | 0.165.0+26.3 |
| Fabric Loom | 1.19-SNAPSHOT, plugin id `net.fabricmc.fabric-loom` |
| Gradle | 9.8.2 |
| Java | 25 (`options.release = 25`) |

- 26.x is unobfuscated: there is no mappings line, and the names are Mojang's official names.
- `./gradlew build` and `./gradlew genSources` work, but run them as **separate** invocations.
  Combined, they trip a Gradle implicit-dependency validation error.
- Decompiled MC sources are extracted to `research/mc-26.3-src/` (7,301 files) for reference.

## Size of the source

- About 1,834 Java files and 250k lines in `src/main/java`, plus about 13.5k resource files.
- 539 files import NeoForge: events, capabilities, FluidStack, IItemHandler, energy, ModelData,
  config, networking, data attachments, and client extensions.
- 241 files import ModularUI (`brachy.modularui`), the GUI library.
- 78 files import Registrate.
- 4 files import Toma Configuration.
- Optional integrations: KubeJS (214 imports), Jade, JEI, EMI, AE2, Create, FTB, CC:Tweaked,
  Sodium/Embeddium/Iris, Xaero, JourneyMap, Curios, Sable.

## Library availability for 26.3 Fabric

- **ModularUI** (`brachy.modularui`): GTCEu maven only has `modularui-mc1.20.1` and
  `modularui-mc1.21.1` (NeoForge). No 26.x or Fabric build exists, so it must be ported too.
  The 1.21.1 sources jar has 661 files and is in `research/deps/`.
- **Registrate**: the tterrag maven tops out at MC1.20/1.21 Forge. No Fabric 26.x build exists,
  so it needs a replacement registration layer or a port.
- **Toma Configuration**: still unchecked for 26.3 Fabric. Its surface is small (4 files), so it
  can be replaced.

## Vanilla API drift, 1.21.1 → 26.3

98 of the 539 distinct `net.minecraft` classes GT imports no longer exist. Most of the other 441
have changed signatures. Key renames and removals:

- `ResourceLocation` → `net.minecraft.resources.Identifier`
- `GuiGraphics` → `GuiGraphicsExtractor`. The GUI now uses a render-state extraction model, and
  `LayeredDraw` is gone (see `Hud`).
- `RenderType` moved to `client.renderer.rendertype`. `RenderStateShard`, `MultiBufferSource`,
  `ShaderInstance` and `LightTexture` are gone, replaced by RenderPipelines and RenderSetup.
- `BakedModel`, `ItemOverrides`, `ItemTransforms`, `ModelResourceLocation`, `BlockModelShaper`,
  `BlockRenderDispatcher` and `ItemRenderer` are gone. Replacements: the `BlockStateModel` /
  `block.dispatch` system and `client.renderer.item` ItemModel definitions (`ClientItem`).
- `ItemProperties` and `ItemPropertyFunction` → item model `properties` (select, condition, range).
- `BlockColor` / `ItemColor` → tint sources.
- BlockEntity `loadAdditional(CompoundTag, Provider)` → `loadAdditional(ValueInput)`, and
  `saveAdditional` → `saveAdditional(ValueOutput)`.
- Block entity renderers now use `BlockEntityRenderState` (extract, then render).
- `InteractionResultHolder` → `InteractionResult` variants.
- `data.models.*` → `client.data.models.*` (model datagen rewritten).
- `FastColor` → `ARGB`, and `Tuple`, `MobSpawnType` and `RelativeMovement` are renamed or removed.

## NeoForge → Fabric mapping (plan)

- Capabilities → Fabric API Lookup (`BlockApiLookup` / `ItemApiLookup`).
- Item and fluid handlers → `fabric-transfer-api-v1` (Storage, Transaction).
  FluidStack → own `FluidStack` wrapper over `FluidVariant` + amount.
- Energy (FE) → GT EU is internal. Expose FE only if an energy API exists on 26.3.
- Events → Fabric event callbacks, plus mixins where Fabric has no hook.
- Data attachments → `fabric-data-attachment-api-v1`.
- Networking payloads → `fabric-networking-api-v1` (`PayloadTypeRegistry`).
- `ModelData` and dynamic models → `fabric-renderer-api-v1` / `fabric-model-loading-api-v1`.
- Access transformer → access widener (`accessWidener` in `fabric.mod.json`).
- `enum_extensions.json` → hand-written mixins, or avoid enum extension entirely.

## Decisions log

- **Integrations with no 26.3 Fabric build** are moved to `disabled/`, not deleted: EMI, KubeJS, AE2, Create,
  Curios, FTB suite, ModernFix, Embeddium, Sable, Heracles/Argonauts, GameStages. Integrations that do have builds
  (JEI 31.17, Jade 26.3.5, CC:Tweaked 1.120.3, Sodium 0.9.3, Iris 1.11.7, JourneyMap, Xaero) are also set aside
  until the core compiles. They are then ported against their new APIs.
- **NeoForge compatibility layer** lives in `com.gregtechceu.gtceu.compat.*` and mirrors NeoForge's package
  layout, so `net.neoforged.neoforge.X` became `com.gregtechceu.gtceu.compat.X` by a mechanical rewrite.
  Implementations are written for Fabric. Where NeoForge code is vendored, its LGPL-2.1 header is kept.
  - Capabilities are Fabric API Lookups (`BlockApiLookup`/`ItemApiLookup`/`EntityApiLookup`).
  - The event bus is a small reflective bus (`compat.bus.EventBus`). `addListener` must name the event class.
  - `DeferredHolder` is **not** a `Holder`, because `Holder` is sealed in 26.x. Use `getDelegate()`.
  - `FluidType` is reached through an injected `getFluidType()` on `Fluid` (`core.fabric.FluidTypeHolder`).
  - Custom item ingredients are Fabric `CustomIngredient`s. `IngredientType`s are bound to Fabric serializers
    when registered in `NeoForgeRegistries.INGREDIENT_TYPES`.
- **Registrate and ModularUI** are vendored as source (`com.tterrag.registrate`, `brachy.modularui`). Neither
  has a 26.x or Fabric build.
- **Model generation is runtime code**. GT generates material blockstates and models into a dynamic resource
  pack at runtime, so NeoForge's JSON model builders are vendored rather than stubbed.

## 26.3 gotchas found so far

- Fabric API **sources jars use stale intermediary names** (`class_1799`). Check real signatures with `javap`
  against the compiled jars.
- **`Holder` is a sealed interface** that permits only `Direct` and `Reference`.
- **Input is SDL**, not GLFW. LWJGL has no glfw module. Key codes are SDL scancodes (`InputConstants.KEY_A == 4`,
  `KEY_ESCAPE == 41`, `MOUSE_BUTTON_LEFT == 1`). Hard-coded GLFW numbers are wrong.
- **Java 25 adds `java.lang.IO`**, which makes a wildcard import of GT's `IO` ambiguous. Import it explicitly.
- `Level.isClientSide` is now a method.
- `DataComponentPredicate` (1.21.1) became `DataComponentExactPredicate`.
- Worldgen features were rebuilt: `Feature` is an interface, features carry their own config, and
  `ConfiguredFeature`, `FeatureConfiguration` and `OreConfiguration` are gone. Ore targets are `BlockReplacement`.
- `BlockAndTintGetter` moved to `net.minecraft.client.renderer.block`, so it is client-only.
- Armor is component-based: `ArmorItem` and `Equipable` are gone.
- Food is `FoodProperties` (nutrition only) plus a `Consumable` component (eat time, animation, effects).
  GT keeps the old builder shape in `api.item.component.GTFoodProperties`; `ComponentItem` adds the
  `food`/`consumable` default components when a `FoodStats` is attached.
- Brewing is data-driven (`crafting.BrewingRecipe`, server-only). `BreweryLogic` derives potion→potion mixes
  from those recipes; on a remote client the list is empty.
- Boats: one `EntityType` per boat kind (`GTEntityTypes.*_BOAT`), `BoatRenderer` takes a model layer and
  derives the texture from it. Entity types now register before items (`RegistrationLoader`).
- `ChatFormatting` lost getColor/getName/getByName/getChar: use `compat.common.util.ChatFormats`.
- `Item.inventoryTick(stack, ServerLevel, entity, @Nullable EquipmentSlot)` is server-only; `hurtEnemy` is void.
- Render layers are derived from texture transparency; registrate `addLayer`/`renderType` are kept as no-ops
  that take `ChunkSectionLayer`.
- Every item needs an `assets/<ns>/items/<id>.json` model definition. `ItemColor`/`BlockColor` are replaced by
  tint sources.
- **Fabric API module renames in 26.x**: item-group → `fabric-creative-tab-api-v1` (`CreativeModeTabEvents`,
  `FabricCreativeModeTabOutput`), key-binding → `fabric-key-mapping-api-v1`, screen-handler → `fabric-menu-api-v1`,
  block-view → `fabric-block-getter-api-v2`, loot → v3, command → v2, resource loader → v1 (`ResourceLoader`).
  `FabricBlockEntityType.addValidBlock`. The gradle cache also holds **older** Fabric API jars from other
  projects. Always javap the jars listed by `./gradlew printCompileClasspath`.

## Status and work order (keep updated)

Done so far:
- Fabric 26.3 build, sources imported, integrations without 26.3 builds moved to `disabled/`
- Compat layer: event bus, FML shims, fluids (stack/type/handlers/tanks/ingredients), items (handlers/wrappers),
  capabilities via Fabric API Lookup, energy, registries (DeferredRegister/Holder, RegistryBuilder,
  RegisterEvent replay in `compat.fml.loading.RegistrationLoader`), networking (PayloadRegistrar on Fabric
  networking), events (`compat.event.*`, generated POJOs), item abilities, tags, data maps → vanilla components,
  colors → tint sources (`compat.client.color.ColorHandlers`), client extensions → FluidRenderingRegistry,
  ModelData, attachments → Fabric attachments, Registrate datagen providers stubbed (datagen never runs on Fabric)
- Injected interfaces (classtweaker): Fluid.getFluidType, Level/Entity/ItemStack.getCapability,
  Item.canPerformAction + IItemExtension hooks, Ingredient.getItems/isCustom/hasNoItems
- Block entity NBT bridged to ValueInput/ValueOutput (`core.fabric.ValueIO`, ManagedSyncBlockEntity)
- Helper scripts now live in `research/tools/` (compile.sh, ranking.sh, fileerrs.py, fj.sh, addimport.py);
  the old session-scratchpad fixers were lost (fixrename.py, fixholder.py, fixfield.py,
  rmblock.py, strip_integrations.py, unused_imports.py, gen_events.py)

Remaining, in order:
1. Mechanical vanilla renames (ArmorItem.Type→ArmorType, etc.)
2. Armor: ArmorItem/ArmorMaterial (holder) → Equippable component + equipment.ArmorMaterial record
3. Worldgen: OreConfiguration/ConfiguredFeature/FeatureConfiguration removed → new Feature records
4. Recipes: GTRecipe vs 26.3 Recipe/RecipeSerializer/display; RecipeManager hooks; brewing is now
   `crafting.BrewingRecipe` (data-driven), so BreweryLogic must read recipes
5. Tools: Tier/SimpleTier → ToolMaterial; Item.Properties needs setId; tool components
6. Blocks/BEs: BlockBehaviour.Properties.setId, BlockEntityType ctor, boats (one EntityType per boat type)
7. Registrate builders on 26.3
8. Client: BakedModel→BlockStateModel adapter + Fabric renderer API; BERs to render states; bloom; MUI GUI
   (GuiGraphicsExtractor render-state model); key codes SDL
9. Entry points (GTCEu/ClientProxy → ModInitializer/ClientModInitializer + FabricEventBridge), mixins,
   fabric.mod.json, generated resources → 26.3 formats (items/*.json etc.)
10. Integrations with 26.3 builds: JEI, Jade, CC:Tweaked, Sodium/Iris, JourneyMap, Xaero
11. Run client + server, fix runtime errors

## Where I stopped (updated 2026-10-29, second session)

- `./gradlew compileJava`: about 1.9k unique errors. **Common code is ported** apart from ~60 errors that are GUI
  drawing (ContentOverlay, ProspectorMapTexture, ArmorUtils HUD, GTMatrixUtils, placeholder renderers) and the
  NeoForge entrypoint `GTCEu.java` (step 4). Everything else left is client rendering and ModularUI.
- Helper scripts: `research/tools/` (`compile.sh` writes `cur.log` and prints totals, `ranking.sh [all]` ranks files,
  `fileerrs.py LOG SUBSTR`, `fixnbt.py`, `fixrename.py LOG rules.py`, `fj.sh CLASS` = javap on the classpath).
- **ModularUI drawing (started):** 26.x GUI is render-state based (`GuiGraphicsExtractor` submits
  `GuiElementRenderState`s; no vertex buffers, no GL state). Done: `drawable/MuiGeometry` (records vertices, converts
  fans/strips to triangles, registers `modularui:pipeline/gui_triangles` and the textured variant), `GuiDraw`
  rewritten on it (texture methods take the `GuiGraphicsExtractor` instead of a `Matrix4f`; `bindTexture`/`setColor`
  emulate the old shader texture/colour; z is ignored), `utils/Stencil` now uses vanilla scissors (pose-aware),
  `compat.client.event.RenderTooltipEvent`. `MUIRenderTypes` is gone.
- **Next for ModularUI:** `ClientScreenHandler` listens to NeoForge `ScreenEvent`s for every screen. Plan: MUI's own
  wrapper screens call the handlers directly from their overrides (26.x `Screen` uses `KeyEvent`,
  `MouseButtonEvent`, `CharacterEvent`, `extractRenderState`); overlays on other screens use Fabric
  `ScreenKeyboardEvents`/`ScreenMouseEvents` allow-events and `ScreenEvents.afterExtract`. Then `GuiContext`
  (`getLastGraphicsPose` → graphics, `graphicsPose()` is a `Matrix3x2fStack` with `pushMatrix/popMatrix`),
  `TextRenderer`, the widget tree, and `drawable/schema` (3D previews: needs a picture-in-picture renderer).
- **GT client:** block/item models (BakedModel → BlockStateModel + Fabric renderer), BERs (render states +
  `SubmitNodeCollector`), bloom, special item models for lamps and machine items (BEWLR is gone), item decorators
  (`compat.client.IItemDecorator`, needs a hook), armour layer tints.

## Client models on 26.3 (design, 2026-10-29, third session)

26.3 has no `BakedModel`: blocks render `BlockStateModel`s (parts of immutable `BakedQuad` records without
per-vertex colour/light), and Fabric's renderer emits quads through `QuadEmitter` (`FabricBlockStateModel.emitQuads`
gets the level, pos and a cull test). GregTech's models are kept on a compat layer shaped like NeoForge's API:

- `compat.client.model.BakedQuad`: the pre-26 vertex layout (`IQuadTransformer` offsets: pos 0, colour 3 (ABGR),
  uv 4, lightmap 6, normal 7; stride 8) plus GT metadata (texture key, `FaceLayer`, source layer) that the
  `BakedQuadMixin` used to add. `of(vanillaQuad)` / `toVanilla(layer)` convert; `emit(emitter, layer, cullFace)` hands
  it to Fabric with colour, lightmap, emission, shade and AO.
- `compat.client.model.BakedModel` / `IDynamicBakedModel` / `ChunkRenderTypeSet` (of `ChunkSectionLayer`):
  `getQuads(state, side, rand, ModelData, layer)`, `getRenderTypes`, `getModelData(level, pos, state, data)`,
  `getParticleIcon(data)`. Old `RenderType renderType` parameters became `ChunkSectionLayer`
  (`solid()`→`SOLID`, `cutout()`/`cutoutMipped()`→`CUTOUT`, `translucent()`→`TRANSLUCENT`).
- `compat.client.model.GTBlockStateModel`: adapts one to `BlockStateModel`. `emitQuads` builds the model data from
  the level and emits each layer from `getRenderTypes` (cull test true = skip, like Fabric's vanilla encoder);
  `collectParts` falls back to the data-less quads converted to vanilla (falling blocks, particles, items).
- Custom JSON models (`gtceu:machine`, `gtceu:pipe`, ...) are Fabric `UnbakedModelDeserializer`s keyed by
  `"fabric:type"` (resources: rename `"loader"`). They implement a GT unbaked interface that lists extra
  dependencies (a `ModelDiscovery` mixin loads them, since 26.3 only follows `parent()`) and bakes a GT `BakedModel`;
  a `SingleVariant.Unbaked#bake` mixin returns a `GTBlockStateModel` for them, so blockstate JSON keeps working.
  Inline sub-models are resolved with their parent through a small `ResolvedModel` implementation.
- Bloom: the blur post effect is not ported; `BloomRenderer` draws registered bloom effects full-bright from
  Fabric `LevelRenderEvents.COLLECT_SUBMITS` (`IRenderSetup` is now just a `RenderType`, effects/particles draw
  into a `VertexConsumer`). Client init must call `BloomRenderer.init()` and `GTParticleManager.init()`.
- Client init must call `ClientProxy.registerSpecialItemRenderers()` (item definitions: lamps use `gtceu:lamp`,
  machines a composite of their model and `gtceu:machine_dynamic`) and `ClientProxy.registerModelLoaders()` (Fabric `UnbakedModelDeserializer`s) before resources load.
- Blockstate JSON: 26.3 variants have `x`, `y`, `z` natively, so `gtceu:z` becomes `z` in the resource conversion.

## Server runtime on 26.3 (fourth session, 2026-10-30)

`./gradlew runGametest` (headless game test server: loads all data, makes a world, runs the tests, exits) now
passes with a clean log. Run it with `research/tools/rungametest.sh` (writes `research/tools/gt.log`).
`research/tools/runserver.sh` runs the dedicated server, which stops at the EULA prompt (not accepted for the user).

Tools:
- `research/tools/mixcheck.py SRC MIXIN_JSON...` statically checks mixins against the classpath: method and
  INVOKE/FIELD targets, `@Shadow` names and field types, `@Accessor`/`@Invoker` names, types and `@Mutable` on final
  fields, and injector handler parameters against the target method. Compile-time checks miss all of these.
- `research/tools/convert_data.py DIR...` rewrites 1.21.1 data JSON in place (see below). Safe to rerun.

26.x runtime differences and what the port does about them:
- **Fabric runs server mod init after `Bootstrap.validate`**, which builds default entity attributes. GT's swim speed
  and creative flight attributes (`compat.common.NeoForgeMod`) are registered when that class loads, not deferred.
- **NeoForge stripped `@OnlyIn(Dist.CLIENT)` members on servers; Fabric strips `@Environment(EnvType.CLIENT)`**.
  All `@OnlyIn(Dist.CLIENT)` were converted, so subscriber classes with client-typed methods load on servers.
- **Item components are only bound when server data loads**, so `new ItemStack(...)` during mod init fails.
  `core.fabric.EarlyComponentBinder` binds defaults from `VanillaRegistries.createWorldLookup()` before and after
  GT's content setup; initializers that need modded data (GT's jukebox song) are skipped then and bound by the reload.
- **Block/Item properties need their id** (`setId`): Registrate's builders set it after the properties callback
  (callbacks may swap in `ofFullCopy(...)`). Block items use `useBlockDescriptionPrefix`.
- **Registration order**: fluids before blocks (material fluids create their blocks while fluids register), GT's
  recipe-type key maps to the vanilla registry and is posted once. Legacy id aliases (`CommonProxy
  .registerLegacyAliases`) run after registration: Fabric's `addAlias` needs the target to exist.
- `DeferredHolder.getDelegate()` returns itself while unbound, like NeoForge.
- **Tags load before the rest of the data and are bound only after it.** GT generates recipes at the start of
  `ReloadableServerResources.loadResources`. So: `ItemMaterialData.reinitializeMaterialData()` runs at
  `TagLoader.loadTagsForExistingRegistries` (GT's generated material tags need it), and recipe generation runs in
  `TagUtil.withPendingTags`, where `TagUtil.holderSet` hands out bound copies of the just-loaded tags
  (captured in `MappedRegistry.prepareTagReload`; membership answered from contents). Undefined tags are empty.
- A tag `Ingredient` never counts as empty (1.21.1 rule; `IngredientExtMixin`). Empty circuit tags for every tier
  are always defined, since 26.x rejects recipes naming undefined tags.
- GT's recipe lookups are built at `RecipeManager.finalizeRecipeLoading`, after tags are bound.
- Recipes and loot predicates are no registry while GT generates data: `data.pack.KeyOnlyLookups` hands out
  key-only holders and is part of the serialization context, so they are written as ids.
- 26.x places recipes per item: "item minus item-with-component" ingredients are empty. Netherite plating uses
  Fabric's components ingredient with a removal patch instead.
- NeoForge's `ServerStartingEvent` fired after levels load; it is posted from Fabric `SERVER_STARTED`.
- The configuration library on 26.3 separates config paths with `/`.

Data format changes handled by `convert_data.py` (static data under `src/*/resources/data`):
- Block/fluid states: `{"Name", "Properties"}` → `"id"` string or `{"id", "properties"}`.
- Density functions: `argument1/argument2` → `left/right`, `argument` → `input`; `interpolated` takes explicit
  `cell_size_xz`/`cell_size_y` (4/8, the overworld noise cell).
- Loot tables: `conditions: [...]` → `condition` (one, or `all_of`), a condition's `condition` key → `type`,
  `functions` → `modifier` (function `function` → `type`), `block_state_property` → `match_block`
  (`block`/`properties` → `blocks`/`state`). Old keys are **silently ignored** by 26.x.
- By hand: `worldgen/configured_feature` → `worldgen/feature`, feature `config` fields inlined, tree
  `dirt_provider`/`force_dirt` → `below_trunk_provider: "minecraft:soil_beneath_tree"`, simple state providers as
  bare states; `miner_pipe` loot table has no pool (the block has no item).

## Client, JEI and Jade (fifth session, 2026-10-30)

- `./gradlew runClientGameTest` (`src/gametest`): scripted flat world. It places blocks, opens the inventory, the
  LV macerator UI, JEI (inventory list + macerator recipes) and Jade's overlay, and screenshots each to
  `build/run/clientGameTest/screenshots`. It asserts a powered macerator turns iron ore into crushed ore and that a coke
  oven forms. All pass.
- Assets: `research/tools/convert_assets.py`. It handles `fabric:type` loaders, `z` rotation, shade, light emission,
  Shimmer tint indices (-101-10n → n), and fluid containers as layered models with a bucket fluid mask. At reload,
  `client.model.item.ItemDefinitions` writes missing 26.x item definitions. Machines use composite + `gtceu:machine_dynamic`,
  lamps use `gtceu:lamp`, and the facade uses composite + `gtceu:facade`.
- Runtime fixes:
  - `IBlockEntityExtension.onLoad/onChunkUnloaded` are now called from Fabric events. Machines set up their recipe handlers there.
  - Colour events are posted.
  - The client pack finder is added at the first `PackRepository.reload`.
  - The recipe-map hook runs before Fabric's recipe sync (mixin priority 500).
- JEI 31.17 is ported:
  - Fluids are `IJeiFluidIngredient`, measured in droplets.
  - Plugins are registered through the `jei_mod_plugin` entrypoint.
  - ModularUI slots are rebuilt on `RecipeSlotIngredients`. A null focus list means show all ingredients.
  - `RecipeLayoutBuilderMixin` was dropped, because JEI's bytecode reuses local slots.
- Jade 26.3.5 is ported:
  - `JadeUI`/`ProgressView`/`FluidView.Data` replace the old UI and view APIs.
  - Server data providers are separate wrappers, as Jade 26 requires.
  - Harvest tools go through `addHarvestPlugin`.
  - The AE2 providers stay disabled.
- Remaining:
  - **CC:Tweaked**: the dependency is in build.gradle and the sources are in `disabled/gtceu/integration/cctweaked`. On Fabric,
    generic peripherals only match the block entity, not capabilities. Plan: register a `PeripheralLookup` for GT machine
    block entities that exposes the capability objects, or make the machine block entity implement those interfaces.
  - JourneyMap/Xaero map integration (`integration/map` is common code in src; the mod-specific parts are not yet checked).
  - Sodium/Iris (`disabled/gtceu/integration/sodium`).
  - REI and EMI support for ModularUI (EMI has no 26.3 build).
  - Item-model particle texture warnings for machine items (cosmetic).
  - Dedicated-server client connection test: this needs the user to accept the EULA, which was not done on their behalf.

## Integrations, mechanics and server safety (sixth session, 2026-10-30)

Everything in the fifth session's "Remaining" list is done except REI/EMI:
- **CC:Tweaked 1.120.3**: `CCTweakedPlugin` registers each GT capability with the internal
  `Peripherals.addGenericLookup`, so CC's generic peripheral methods see GT machines, e.g. `getProgress` and
  `getEnergyStored`. The client test checks the methods are there.
- **JourneyMap 6.0.10 / Xaero minimap 26.6.2 + world map 1.47.2**: the API jars that ship nested in the mod jars (the JourneyMap API
  and XaeroLib) are extracted into `libs/` by the `extractLibs` Gradle task before compiling; `libs/` is not checked in. Xaero's ore-vein layer is ported to the new element API
  (`ElementReader`/`ElementRenderer`/`ElementRenderProvider`). Xaero mixins only apply when the mod is loaded
  (`GTMixinPlugin.MOD_IDS`). Item sprites are on `AtlasIds.ITEMS`, not the block atlas. The client test opens the
  world map with a vein marker.
- **Sodium 0.9.3 / Iris 1.11.7**: `SodiumCompat` marks fluid sprites in ModularUI schemas as active. Run with
  `./gradlew runClient -PwithSodium=true`.
- **Fabric Transfer API bridges** (`compat/transfer/TransferBridges`): GT item/fluid handlers are exposed as
  `ItemStorage.SIDED`/`FluidStorage.SIDED`, and Fabric storages are visible to GT capability lookups. Without these,
  GT machines could not push into chests, hoppers could not feed machines, and other mods' pipes could not reach GT
  tanks. GT-side changes apply immediately and are undone if the transaction aborts. Fluids convert at
  81 droplets per mB, and only whole mB move.
- **Particles**: geometry-loader models resolve a particle texture from their JSON or referenced models. Pipe
  restrictor, overlay and facade models got explicit particles. The only "missing particle" warning left is
  CC's own `turtle_elf_overlay`.

Tests (`src/gametest`; `./gradlew runClientGameTest` runs all three, `./gradlew runGametest` is the headless server):
- `GTClientGameTest`: rendering, UIs, JEI, Jade, machine processing, coke oven, CC methods, maps, and a check that
  GT's creative tabs have no duplicates. JEI's "duplicate items" warning comes from JEI's own list factory, not GT.
- `GTMechanicsGameTest`:
  - A creative source powers a macerator through a tin cable. A hopper feeds it and it auto-outputs into a chest.
  - At least 100 GT crafting recipes exist.
  - Fabric fluid insert/extract on a super tank works, including abort.
  - A steam boiler heats up and, once hot, makes steam. Low pressure gains 1° per 24 ticks, and steam needs 100°.
  - Machine state survives save and reload.
  - Note: `/setblock` pipes have no connections, so the test connects them with `setConnection`.
- `GTDedicatedServerGameTest`: a client joins a dedicated server, sees the machine working, opens its ModularUI and
  sees JEI recipes. That server runs **in the client JVM**, so it cannot catch client-class leaks. Use
  `runGametest`/`runServer` for that.

Dedicated-server safety (Fabric strips `@Environment(CLIENT)` classes and methods; NeoForge did not):
- Bug found and fixed: Jade's `ITooltip.add/append` take the client-only `LayoutElement`. Passing a Jade `Element`
  to them makes the bytecode verifier load `LayoutElement`, so `GTJadePlugin` crashed every dedicated server that has
  Jade. Calls now go through `integration/jade/JadeTooltips`.
- Same verifier issue in `ModularItemManagerUI`/`ModularArmorManagerUI.createScreen`, which are now `@Environment(CLIENT)`.
- `research/tools/srvcheck/SrvCheck.java` is an ASM checker for this. It flags non-client methods whose verification
  needs a stripped type: the target type is client-only, or the target is a class and the source is client-only. It
  also flags non-client classes that extend client types. Everything it still lists is client-only code.
  Rebuild, then: `java -cp srvcheck/out:<asm jars> SrvCheck ../../gtceu-fabric/build/classes/java/main cp.txt`.
- `runGametest` disables Xaero (`-Dfabric.debug.disableModIds`). XaeroLib freezes its config registry in a dedicated
  server hook that the game-test server never runs. Real dedicated servers with Xaero boot fine (`runServer` checked).

Still not done:
- REI support for ModularUI.
- EMI, AE2, Create and KubeJS have no 26.3 Fabric builds; they stay in `disabled/`.
- Dev runs turn high-tier content on (`GTCEuAPI.isHighTier` includes `GTCEu.isDev()`), which hid a production-only
  crash: equipment-foundry tiers whose items are not registered serialize the `Ingredients.EMPTY` stand-in as
  `#gtceu:empty`. That tag did not exist, so **production servers refused to load datapacks**. Fixed by shipping
  `data/gtceu/tags/item/empty.json`. To catch this kind of bug:
  - `./gradlew runClientGameTest -PprodLike=true` sets `-Dgtceu.prodLike=true`, so `FMLLoader.isProduction()` is true:
    high tier is off, and there is no dev content or ModularUI debug.
  - `research/tools/prodserver.sh` boots the built jar plus Fabric API on a real non-dev Fabric server assembled from the
    Gradle cache (`KnotServer` with `fabric.gameJarPath` pointing at the server bundler jar). Set `PRODSERVER_KEEP=1`
    to leave it running for `GTCEU_REMOTE_SERVER=localhost:25565 ./gradlew runClientGameTest`. Both pass.
- Only the machine UI has been opened on a real remote server; other UI types (covers, items, armour) are covered
  by `SrvCheck` alone.

Separate-process multiplayer: `research/tools/remotetest.sh` starts `runServer` in its own JVM, with client code
stripped. It temporarily turns off online mode and the whitelist. Then it runs `GTRemoteServerGameTest`, which only
runs when `GTCEU_REMOTE_SERVER` is set and skips the other tests. The client connects over TCP, the script places an
LV macerator, and the client opens its ModularUI. It passes, and the server log has no errors. ModularUI's
"Debug Options" overlay in screenshots is its dev-environment debug mode (`ModularUI.isTestEnv()`), not shown in
production.

## JEI fluids (seventh session, 2026-10-30)

Four bugs made GT fluids, gases most visibly, show up broken in JEI:
- **No textures:** GT material fluids rendered as `gtceu:block/void`, i.e. invisible. The fluid's icon-set texture
  is chosen by checking which textures exist, and that check ran during client init, before any resources had
  loaded. `GTClientFluidTypeExtensions` now resolves textures lazily and retries until it gets a real one. Fluid
  models are re-registered at the start of every model reload, through a `ModelLoadingPlugin` in
  `ModelEventHelper`, and Fabric reads them when it bakes fluids later in that reload.
- **Untranslated names:** fluids showed as `block.gtceu.hydrogen`. Fabric names fluids through
  `FluidVariantAttributes`, which by default uses the block's translation key. `compat/transfer/FluidVariantHandlers`
  registers GT's names, plus temperature, viscosity, light level and lighter-than-air, for every `BaseFlowingFluid`.
  `FluidVariantRenderHandlers` (client only) supplies per-stack tints such as potion colours.
- **Potion fluid names:** these used `fluid.gtceu.potion.effect.*` keys that no language has (upstream too). They now
  use vanilla's `item.minecraft.potion.effect.*` names, e.g. "Potion of Night Vision".
- **Empty recipe slots:** every fluid slot in GT's JEI recipes was blank, and only the ModularUI amount label showed.
  ModularUI passed GT's compat `FluidStack` to JEI, which only accepts `IJeiFluidIngredient` on Fabric, so JEI
  silently dropped the fluids. Every ModularUI–JEI crossing now goes through
  `JeiIngredientHandler.toJei`/`fromJei`: recipe slots, hover lookups, clickable ingredients, and dragging into ghost slots.

`GTJeiGameTest` (`GTCEU_TESTS=jei ./gradlew runClientGameTest` runs only that test; `GTCEU_TESTS` takes a comma list
of client/mechanics/jei/server) checks:
- All 435 GT fluids have a real name, a texture and a visible colour.
- All of GT's roughly 9.4k items have translated names.
- The coke oven's JEI slots, creosote included, are filled.

It also screenshots the gas list, a recipe with gas outputs, a fluid tooltip and a tank UI.

## REI (eighth session, 2026-10-30)

REI 26.3.824 (with Cloth Config 26.3.159 and Architectury 22.0.5, all from Modrinth's Maven) is supported.
`./gradlew runClient -PwithRei=true` runs REI instead of JEI.
- **ModularUI's REI layer** moved from `disabled/` to `brachy/modularui/integration/rei`. It was ported to REI 26's
  API: REI's own `GuiGraphics` wrapper, the new input event records, a `null` display serializer (displays are built
  on the client), and Architectury fluid amounts in droplets. `ModularUIReiPlugin` makes REI the current
  `RecipeViewerHandler`. The `DefaultDisplayViewingScreenMixin` lambda target is now `lambda$checkExportDisplays$0`,
  with `require = 0`.
- **GT's REI plugin** (`integration/recipeviewer/rei/GTREIPlugin`, `rei_client` entrypoint) mirrors `GTJEIPlugin`.
  `GTReiCategory` is a single generic category and display. Each display draws the same ModularUI widget the JEI
  categories use, and lists its inputs and outputs for lookups. It covers:
  - every GT recipe category, with machine workstations (the furnace maps to REI's smelting category)
  - multiblock previews and ore processing diagrams
  - ore veins, bedrock fluids and bedrock ores
  - programmed circuits
  - potion fluid entries
- **Two REI 26.3 problems worked around:**
  - REI's fluid renderer looks up the sprite and then draws nothing, so REI showed no fluid anywhere, vanilla water
    included. `ReiFluidRenderer` draws fluids instead and gives them JEI-style GT tooltips.
  - On Fabric, Architectury only knows sprites for its own fluids. `core/mixins/rei/ArchitecturyFluidSpriteMixin` falls
    back to the fluid's baked model.
- REI validates every display on its plugin reload thread by asking for its width. ModularUI can't lay out widgets
  there, and doing it for 30k recipes would be slow, so `ModularUIReiCategory` reports a fallback width until the
  real size is measured on the render thread.
- Tests now run through one entrypoint, `GTGameTests`, which only loads a test class when its mods are present (the
  JEI tests can't even load without JEI). `GTReiGameTest` (`GTCEU_TESTS=rei ./gradlew runClientGameTest
  -PwithRei=true`) checks that:
  - REI has the GT displays (30k+), and every category has some
  - the coke oven display lists its ingredients
  - Architectury now finds GT fluid sprites
  - GT fluids are in REI's list
  - recipe fluid slots convert to REI entries

  It also screenshots a few REI views.

## Gameplay sweep (ninth session, 2026-10-30)

New client tests (all run by `GTGameTests`; select them with `GTCEU_TESTS=world|logistics|tools`):
- `GTWorldGameTest`: a normal overworld with a fixed seed has:
  - GT ore veins: about 68k ore blocks of 140 kinds within 5 chunks
  - ores that drop their raw ore (plus, as upstream, a 0–1 secondary dust roll that can come out as an empty stack)
  - surface rocks and bedrock fluid veins
- `GTLogisticsGameTest`: hopper → tin item pipe → chest; pump cover → bronze fluid pipe → super tank; conveyor cover out of
  a super chest; a preheated steam boiler running a steam macerator; and a coke oven making charcoal and creosote. It
  also screenshots the pipes and covers.
- `GTToolsGameTest`:
  - a hammer plate recipe returns the damaged hammer
  - a GT pickaxe mines GT ore
  - innate tool enchantments count
  - a wrench sets the output side, and sneaking with it turns the machine
  - using a cover item on a machine places the cover through the real server click path
- `GTRemoteServerGameTest` now also places a conveyor, opens its cover UI with a screwdriver, and opens the
  programmed circuit's item UI, all against a real production server. Run it with `research/tools/prodremotetest.sh`.

Bugs found and fixed:
- **`Item#onItemUseFirst` was never called** (a NeoForge hook). Covers could not be placed by hand, and GT's other
  use-first behaviours (tools, data items, plungers, ...) did nothing. It is now bridged through Fabric's
  `UseBlockCallback` in `FabricEventBridge`.
- **Pipes rendered without their connections.** NeoForge seeds a model's `getModelData` with the block entity's own
  model data, which is where pipes keep their connection mask. `GTBlockStateModel` now does the same.
- **Innate tool enchantments were ignored**, because NeoForge's `getAllEnchantments` is never called by vanilla.
  `EnchantmentHelperMixin` adds them wherever enchantments are read. GT's own default tool enchantments still follow
  the `enchantedTools` config, which is off by default, as upstream.
- **Jade showed "1B" for every super tank.** It used the stored stack, which only marks the fluid type. It now shows
  the real long amount. Upstream shows 0 there.
- Two NeoForge hooks bridged to their Fabric equivalents:
  - GT tools now implement `FabricItem#allowComponentsUpdateAnimation` and `#allowContinuingBlockBreaking` (from
    `shouldCauseReequipAnimation`/`shouldCauseBlockBreakReset`), so tool data changes don't bob the hand or restart
    mining.
  - Nano and quantum boots walk on powder snow (`PowderSnowBlockMixin`).
- `IItemExtension` hooks with no callers and no GT override (`canEquip`, `onEntityItemUpdate`, ...) are left as they are.

Checked and fine as-is: a wrench without sneaking sets the output side (only sneaking turns the machine, as upstream);
conveyor covers need an item inventory, so they won't go on a super tank; covers can't go on a machine's front.
