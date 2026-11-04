# Lumen

Cosmetics plugin that runs on every version from 1.8.8 to 26.x with the same jar. It's a fork of
[ProCosmetics](https://github.com/FilleDev/ProCosmetics), which only supports 26.1 and newer.

What's in right now:

- Particle effects (36): wings, auras, rings, clouds, all of them
- Gadgets (19): TNT, paintball, flesh hook, melon launcher, chicken parachute, cowboy, etc.
- Pets: every mob the server version has (14 on 1.8, all 36 on 26.x)
- Arrow effects (14) and death effects (14)
- The menus, coins and purchases, gadget ammo, SQLite/MySQL/MariaDB storage, Vault and
  PlayerPoints, PlaceholderAPI, and the vanish hooks for Essentials and PremiumVanish/SuperVanish

Balloons, banners, emotes, miniatures, morphs, mounts, music, statuses and treasure chests from
ProCosmetics aren't ported yet. Same with the gadgets that build structures (trampoline, slide,
hot tub, rocket and friends), since those rely on display entities that 1.8 doesn't have.

## What you need

- Spigot or Paper, anything from 1.8.8 to 26.x
- [PacketEvents](https://modrinth.com/plugin/packetevents) 2.14+, which is how particles get sent
  on every version
- Java 8 or newer, whatever your server version already asks for

## How it works across versions

There's no NMS module per version. The core compiles against the 1.8.8 API with Java 8 bytecode,
and everything that changed since then lives behind `Compat`:

- `LegacyCompat` is 1.8. Stuff Bukkit didn't have back then (no AI, silent, invulnerable,
  item tags) goes through NBT with reflection.
- `ModernCompat` (the `modern` module) is 1.9 and up. It compiles against the 1.16.5 API and
  checks the version before using anything newer than that.
- Particles go out as PacketEvents packets, so `Particles.spawn(...)` works the same everywhere.
  Before 1.13 colored dust gets its color through the offsets, like the old client expects.
- Materials, sounds, entity types and titles go through XSeries.
- Events that don't exist on 1.8 get registered by class name with `Events.listen(...)`, so the
  listener doesn't blow up when the class isn't there.

Pets are real mobs with no AI that step after you every tick. That way they also walk properly on
1.8 without touching pathfinding.

The configs keep ProCosmetics' item format (`minecraft:player_head[profile=...]`). On 1.20.5+ the
server reads it as is. Older servers only use the material, the head texture and the
hide-tooltip bits, and items that don't exist yet fall back to something close or a barrier.
The console tells you which ones on startup.

## Building

```
./gradlew build
```

Needs JDK 21 to run Gradle (the bytecode still targets Java 8). The jar ends up in
`build/libs/Lumen.jar`, with its libraries relocated under `com.ricodev.lumen.libs`.

Tested by booting Paper 1.8.8, 1.12.2, 1.16.5, 1.21.11, 26.1.2 and 26.3 and running a bot through
every category on each one.

## Commands

- `/cosmetics` opens the menu
- `/equip <category> <cosmetic>`, `/unequip <category>`, `/unequipall`
- `/lumen` (or `/lm`) for admin stuff: coins, ammo, giving cosmetics, reload, info

Permissions are `lumen.cosmetic.<category>.<cosmetic>` to use one and `lumen.command.*` for the
commands.

## License

GPLv3, same as ProCosmetics. The original code is © FilleDev and contributors, the per-file
headers say so, and this fork keeps that license. If you hand the jar to someone, they get the
source too.
