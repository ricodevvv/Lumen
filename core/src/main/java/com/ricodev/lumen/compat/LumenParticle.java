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

/**
 * Particles Lumen uses, named like the modern Bukkit {@code Particle} enum.
 * Each one knows the first version it showed up in and what to show instead before that.
 */
public enum LumenParticle {

    ANGRY_VILLAGER,
    BLOCK,
    BUBBLE,
    CLOUD,
    CRIT,
    DRIPPING_LAVA,
    DRIPPING_WATER,
    DUST,
    ENCHANT,
    ENCHANTED_HIT,
    END_ROD(9, "FIREWORK"),
    EXPLOSION,
    EXPLOSION_EMITTER,
    FALLING_DUST(10, "BLOCK"),
    FIREWORK,
    FLAME,
    HAPPY_VILLAGER,
    HEART,
    INSTANT_EFFECT,
    ITEM,
    ITEM_SLIME,
    ITEM_SNOWBALL,
    LARGE_SMOKE,
    LAVA,
    NOTE,
    POOF,
    PORTAL,
    RAIN,
    SMOKE,
    SNOWFLAKE(17, "ITEM_SNOWBALL"),
    SOUL_FIRE_FLAME(16, "FLAME"),
    SPLASH,
    TOTEM_OF_UNDYING(11, "HAPPY_VILLAGER"),
    WITCH;

    private final int since;
    private final String fallback;

    LumenParticle() {
        this(8, null);
    }

    LumenParticle(int since, String fallback) {
        this.since = since;
        this.fallback = fallback;
    }

    /**
     * The particle that actually gets sent on this server.
     */
    public LumenParticle resolve() {
        if (fallback == null || ServerVersion.atLeast(since)) {
            return this;
        }
        return valueOf(fallback).resolve();
    }

    public boolean needsBlock() {
        return this == BLOCK || this == FALLING_DUST;
    }
}
