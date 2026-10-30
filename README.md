<p align="center"><img src="https://raw.githubusercontent.com/GregTechCEu/Branding/refs/heads/master/gregtech_ceu_modern_logo_large_modern.png" alt="GregTech CEu Modern logo"></p>
<h1 align="center">GregTech CEu: Modern — Fabric</h1>
<p align="center"><b>An unofficial, fully AI-made port of GregTech CEu Modern to Fabric for Minecraft 26.3.</b></p>
<h1 align="center">
    <img src="https://img.shields.io/badge/Minecraft-26.3-62B47A?style=for-the-badge" alt="Minecraft 26.3">
    <img src="https://img.shields.io/badge/Loader-Fabric-DBD0B4?style=for-the-badge" alt="Fabric">
    <img src="https://img.shields.io/badge/Java-25-E76F00?style=for-the-badge" alt="Java 25">
    <br>
    <img src="https://img.shields.io/badge/Ported%20by-Claude%20(AI)-D97757?style=for-the-badge" alt="Ported by Claude (AI)">
    <img src="https://img.shields.io/badge/Status-Unofficial-red?style=for-the-badge" alt="Unofficial">
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-LGPL--3.0-blue?style=for-the-badge" alt="LGPL-3.0"></a>
</h1>

> [!WARNING]
> **This port was made entirely by AI.** Anthropic's Claude (Opus 5.5) wrote every change from the original NeoForge
> code to this Fabric version, working in Claude Code. That includes the compatibility layer, ModularUI, client
> rendering, the mod integrations and the tests. A person directed the work, but no human wrote or line-by-line
> reviewed the port's code.
>
> **This is not an official GregTech CEu release.** It is not affiliated with or endorsed by the GregTechCEu team.
> **Please don't report bugs from this port to the GregTech CEu team, their GitHub or their Discord.** Open an issue
> here instead.

## What is this?

[GregTech CEu: Modern](https://github.com/GregTechCEu/GregTech-Modern) brings the famously deep GregTech tech tree to
modern Minecraft on Forge and NeoForge. This fork moves all of it to **Fabric** on **Minecraft 26.3**: machines,
multiblocks, cables and pipes, materials, ore veins, covers, armour, and the ModularUI interface.

The original mod relies heavily on NeoForge APIs. Rather than rewriting GregTech, the port adds a Fabric compatibility
layer underneath it: an event bus, capabilities on Fabric's API Lookup, fluid and item handlers, registries,
networking and model loaders. That way the original game logic stays as close to upstream as possible.

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) **0.20.1+** for Minecraft **26.3**.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) in your `mods` folder.
3. Add the GregTech CEu Modern Fabric jar (`gtceu-8.0.0-fabric.26.3.jar`).

That's all you need. The config library and EvalEx are bundled inside the jar. GregTech has to be installed on
**both the client and the server**.

## Mod integrations

| Mod | Status | What you get |
| --- | :---: | --- |
| [JEI](https://modrinth.com/mod/jei) | ✅ | GT recipe categories, fluids and ModularUI ghost slots |
| [Jade](https://modrinth.com/mod/jade) | ✅ | Machine progress, energy, recipe outputs, multiblock and maintenance info |
| [CC: Tweaked](https://modrinth.com/mod/cc-tweaked) | ✅ | GT machines as peripherals (`getProgress`, `getEnergyStored`, …) |
| [JourneyMap](https://modrinth.com/mod/journeymap) | ✅ | Ore vein markers from prospecting |
| [Xaero's Minimap / World Map](https://modrinth.com/mod/xaeros-minimap) | ✅ | Ore vein markers and map buttons |
| [Sodium](https://modrinth.com/mod/sodium) / [Iris](https://modrinth.com/mod/iris) | ✅ | Compatible, including animated fluids in ModularUI previews |
| Fabric Transfer API | ✅ | GT machines and tanks work with hoppers, chests and other mods' pipes |
| REI | 🚧 | Not ported yet |
| EMI, AE2, Create, KubeJS | ⏳ | Waiting for Minecraft 26.3 Fabric builds |

## How it was tested

Because no human reviewed the code, the port relies on automated tests (`src/gametest`):

- **Client game tests** (`./gradlew runClientGameTest`):
  - World and machine rendering, machine UIs, JEI and Jade overlays, and the ore-vein map markers.
  - A macerator powered through a cable, fed by a hopper and auto-outputting into a chest.
  - Coke oven multiblock formation.
  - Steam boiler heat-up and steam output.
  - Fabric fluid transfer into GT tanks, including aborted transactions.
  - CC: Tweaked peripheral methods.
  - Machine state surviving a save and reload.
- **Production-like mode** (`-PprodLike=true`): runs the client tests the way players run the mod, with high-tier
  content off and no dev-only content.
- **Dedicated servers:**
  - The headless game-test server.
  - A real production Fabric server running the built jar, with a separate client joining it and opening machine UIs.
  - A bytecode checker that catches client-only code leaking into server classes.

Tests can only show what they cover, though. Expect rough edges, and please
[report them](https://github.com/Blazzy12/GregTech-Modern-Fabric/issues).
`PORTING_NOTES.md` has the porting decisions, the known gaps, and how to rerun everything.

## Building from source

You need **Java 25** and an internet connection the first time, so Gradle can download Minecraft and the
dependencies.

```bash
git clone https://github.com/Blazzy12/GregTech-Modern-Fabric.git
cd GregTech-Modern-Fabric
./gradlew build
```

The jar ends up in `build/libs/`. On Windows, use `gradlew.bat` instead of `./gradlew`.

### The `libs/` folder (JourneyMap API and XaeroLib)

The map integrations compile against two libraries that aren't published on any Maven repository. They only ship
**nested inside their mods' jars**, under `META-INF/jars/`:

| Library | Found inside |
| --- | --- |
| `journeymap-api-fabric-26.3-2.0.0.jar` | [JourneyMap](https://modrinth.com/mod/journeymap) `26.3-6.0.10+fabric` |
| `xaerolib-fabric-26.3-1.8.5.jar` | [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap) `fabric-26.3-26.6.2` (also in Xaero's World Map) |

They belong to their authors and aren't redistributed in this repo, so `libs/` is git-ignored. **You don't need to do
anything:** the `extractLibs` Gradle task downloads those mods from Modrinth's Maven and unpacks the two jars into
`libs/` automatically before compiling. To run it on its own:

```bash
./gradlew extractLibs
```

If you can't use the automatic step (offline, say), grab those mod versions from Modrinth and pull the jars out by
hand. A mod jar is just a zip file:

```bash
mkdir -p libs
unzip -j journeymap-26.3-6.0.10+fabric.jar 'META-INF/jars/journeymap-api-fabric-*.jar' -d libs
unzip -j xaeros-minimap-fabric-26.3-26.6.2.jar 'META-INF/jars/xaerolib-fabric-*.jar' -d libs
```

If you bump the JourneyMap or Xaero versions in `gradle.properties`, update the two `libs/...jar` file names in
`build.gradle` to match the new nested jars.

### Other useful tasks

```bash
./gradlew runClient                      # play in a dev client
./gradlew runClient -PwithSodium=true    # ... with Sodium and Iris
./gradlew runServer                      # dev dedicated server
./gradlew runClientGameTest              # full automated test suite
```

The dev environment uses [Fabric Loom](https://github.com/FabricMC/fabric-loom) with Mojang's official names. Install
the [Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok) for IntelliJ IDEA. The
[Minecraft Development plugin](https://plugins.jetbrains.com/plugin/8327-minecraft-development) is recommended.

## Credits

**The original mod:** all of GregTech CEu Modern's design, content, code and art are the work of the
[GregTechCEu team and contributors](https://github.com/GregTechCEu/GregTech-Modern/graphs/contributors). Go support the
original at [CurseForge](https://www.curseforge.com/minecraft/mc-mods/gregtechceu-modern),
[Modrinth](https://modrinth.com/mod/gregtechceu-modern) and its [wiki](https://gregtechceu.github.io/GregTech-Modern/).

**The Fabric port** was written by Claude (Anthropic), with a person directing it.

Credited works from the original mod:
- Most textures are originally from [Gregtech: Refreshed](https://modrinth.com/resourcepack/gregtech-refreshed) by @ULSTICK. With some consistency edits and additions by @Ghostipedia.
- Some textures are originally from the **[ZedTech GTCEu Resourcepack](https://github.com/brachy84/zedtech-ceu)**, with some changes made by the community.
- New material item textures by @TTFTCUTS and @Rosethorns.
- Wooden Forms, World Accelerators, and the Extreme Combustion Engine are from the **[GregTech: New Horizons Modpack](https://www.curseforge.com/minecraft/modpacks/gt-new-horizons)**.
- Primitive Water Pump is from the **[IMPACT: GREGTECH EDITION Modpack](https://gt-impact.github.io/#/)**.
- Ender Fluid Link Cover, Auto-Maintenance Hatch, Optical Fiber, and Data Bank Textures are from **[TecTech](https://github.com/Technus/TecTech)**.
- Steam Grinder is from **[GregTech++](https://www.curseforge.com/minecraft/mc-mods/gregtech-gt-gtplusplus)**.
- Certificate of Not Being a Noob Anymore is from **[Crops++](https://www.curseforge.com/minecraft/mc-mods/berries)**.

## License

Like the original, this port is licensed under the [GNU LGPL v3.0](LICENSE).
