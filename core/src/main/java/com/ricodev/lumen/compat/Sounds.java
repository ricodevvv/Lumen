/*
 * This file is part of Lumen, a fork of ProCosmetics - https://github.com/FilleDev/ProCosmetics
 * Copyright (C) 2025-2026 FilleDev and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ricodev.lumen.compat;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Plays sounds by their modern name and quietly skips the ones the server doesn't have.
 */
public final class Sounds {

    private Sounds() {
    }

    /**
     * Plays the sound in the world, everyone nearby hears it.
     */
    public static void playAt(Location location, @Nullable XSound sound, float volume, float pitch) {
        Sound bukkit = sound == null ? null : sound.get();

        if (bukkit != null && location.getWorld() != null) {
            location.getWorld().playSound(location, bukkit, volume, pitch);
        }
    }

    public static void playAt(Entity entity, @Nullable XSound sound, float volume, float pitch) {
        playAt(entity.getLocation(), sound, volume, pitch);
    }

    /**
     * Plays the sound only for that player, at their own position.
     */
    public static void playFor(Player player, @Nullable XSound sound, float volume, float pitch) {
        playFor(player, player.getLocation(), sound, volume, pitch);
    }

    public static void playFor(Player player, Location location, @Nullable XSound sound, float volume, float pitch) {
        Sound bukkit = sound == null ? null : sound.get();

        if (bukkit != null) {
            player.playSound(location, bukkit, volume, pitch);
        }
    }

    public static @Nullable XSound parse(@Nullable String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return XSound.of(name).orElse(null);
    }
}
