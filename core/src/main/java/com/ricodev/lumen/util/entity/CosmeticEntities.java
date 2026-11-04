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

import com.ricodev.lumen.api.entity.CosmeticEntity;
import com.ricodev.lumen.api.entity.EntityTracker;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Where cosmetic entities come from, it replaces ProCosmetics' NMS manager.
 */
public final class CosmeticEntities {

    private CosmeticEntities() {
    }

    public static CosmeticEntityImpl create(World world, EntityType type) {
        return new CosmeticEntityImpl(world, type, null, false);
    }

    public static CosmeticEntityImpl create(World world, EntityType type, @Nullable EntityTracker tracker) {
        return new CosmeticEntityImpl(world, type, tracker, false);
    }

    /**
     * Same as {@link #create(World, EntityType)} but the mob keeps its AI, which mounts need so
     * velocity still moves them.
     */
    public static CosmeticEntityImpl createWithAI(World world, EntityType type) {
        return new CosmeticEntityImpl(world, type, null, true);
    }

    /**
     * An invisible armor stand wearing the item, the stand-in for block and item displays.
     */
    public static CosmeticEntityImpl createItemHolder(World world, ItemStack itemStack, @Nullable EntityTracker tracker) {
        return new CosmeticEntityImpl(world, EntityType.ARMOR_STAND, tracker, false).asItemHolder(itemStack);
    }

    public static CosmeticEntity wrap(Entity entity) {
        return new CosmeticEntityImpl(entity);
    }
}
