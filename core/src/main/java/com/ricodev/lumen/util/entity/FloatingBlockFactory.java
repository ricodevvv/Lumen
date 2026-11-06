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

import com.ricodev.lumen.api.util.structure.StructureBlock;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Creates the entities behind {@link FloatingBlock}s for the running server version.
 */
public interface FloatingBlockFactory {

    /**
     * @param location where the bottom center of the block goes
     * @param scale    the block size, only display entities can do anything but 1
     * @param parent   the entity the block moves along with, or null for a block that stays put
     */
    FloatingBlock block(Location location, StructureBlock block, float scale, @Nullable Entity parent);

    /**
     * A small cube for blocks that need to be smaller than one block. It's an armor stand wearing the
     * block before 1.19.4, so the size there is either 0.3125 or 0.625.
     *
     * @param location where the center of the cube goes
     */
    FloatingBlock cube(Location location, StructureBlock block, float scale);

    /**
     * @param location where the center of the item goes
     */
    FloatingBlock item(Location location, ItemStack item, float scale);

    /**
     * An invisible entity for a player to sit on, which the blocks of a moving structure follow.
     */
    Entity seat(Location location);

    boolean canScale();
}
