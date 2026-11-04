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
package com.ricodev.lumen.util;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tiny per-player cooldowns keyed by name, since {@code Player#setCooldown(ItemStack)} only exists on recent versions.
 */
public final class Cooldowns {

    private static final Map<String, Long> EXPIRY = new ConcurrentHashMap<>();

    private Cooldowns() {
    }

    public static void set(Player player, String key, int ticks) {
        EXPIRY.put(id(player.getUniqueId(), key), System.currentTimeMillis() + ticks * 50L);
    }

    public static boolean has(Player player, String key) {
        String id = id(player.getUniqueId(), key);
        Long expiry = EXPIRY.get(id);

        if (expiry == null) {
            return false;
        }
        if (expiry < System.currentTimeMillis()) {
            EXPIRY.remove(id);
            return false;
        }
        return true;
    }

    private static String id(UUID uuid, String key) {
        return uuid + ":" + key;
    }
}
