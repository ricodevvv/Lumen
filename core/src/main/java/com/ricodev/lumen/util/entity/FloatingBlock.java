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
package com.ricodev.lumen.util.entity;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A block (or item) floating in the world without being placed. It's a display entity on 1.19.4+
 * and a falling block that never falls on older servers.
 */
public interface FloatingBlock {

    /**
     * Moves the block so its bottom center (or its center, for items) ends up at the location.
     */
    void teleport(Location location);

    /**
     * Puts the block back where it belongs next to its parent. Does nothing when it already rides
     * the parent, which is the case for display entities.
     */
    void follow();

    /**
     * Turns the block around its origin. Falling blocks can't be turned, so this does nothing there.
     */
    void setRotation(float yaw, float pitch);

    /**
     * Outlines the block in the color, or turns the outline off with null. Falling blocks can't be
     * outlined, the armor stands holding small cubes only get a white one from 1.9.
     */
    void glow(@Nullable Color color);

    /**
     * Swaps what an item or cube shows. Falling blocks keep theirs.
     */
    void setItem(ItemStack item);

    Location getLocation();

    @Nullable Entity getEntity();

    void remove();
}
