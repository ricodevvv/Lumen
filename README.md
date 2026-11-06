# Lumen

Cosmetics plugin for every version from 1.8.8 to 26.x, same jar. It's a fork of
[ProCosmetics](https://github.com/FilleDev/ProCosmetics), which only runs on 26.1+, and it has
everything ProCosmetics has: gadgets (structures and the Hype Train included), pets, mounts, morphs,
particles, treasure chests, menus, coins and the rest.

On old versions stuff gets swapped for what the client can show: block displays become falling
blocks or armor stands, and mobs your version doesn't have just turn themselves off.

## Setup

Drop the jar in `plugins` with [PacketEvents](https://modrinth.com/plugin/packetevents) 2.14+.
Works on Spigot or Paper with whatever Java your server already uses.

`/cosmetics` opens the menu, `/lumen` is for admin stuff. Permissions are
`lumen.cosmetic.<category>.<cosmetic>` and `lumen.command.*`.

## Building

```
./gradlew build
```

Needs JDK 21 to run Gradle. The jar ends up in `build/libs/Lumen.jar`.

## License

GPLv3, same as ProCosmetics. The original code is © FilleDev and contributors.
